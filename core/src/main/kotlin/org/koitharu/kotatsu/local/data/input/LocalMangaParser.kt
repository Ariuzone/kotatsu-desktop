/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : android.net.Uri (SAF) remplacé par une adressage fichiers pur —
 * chemin absolu pour les fichiers/dossiers, « file+zip://<archive>#<entrée> » pour les
 * entrées d'archives CBZ/ZIP (cf. LocalManga.URI_ZIP_SCHEME_PREFIX). La structure
 * (index.json, patterns d'entries, découverte sans index) est conservée à l'identique.
 */
package org.koitharu.kotatsu.local.data.input

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.openZip
import org.koitharu.kotatsu.core.model.LocalMangaSource
import org.koitharu.kotatsu.core.util.AlphanumComparator
import org.koitharu.kotatsu.core.util.MimeTypes
import org.koitharu.kotatsu.core.util.ext.isDirectory
import org.koitharu.kotatsu.core.util.ext.isImage
import org.koitharu.kotatsu.core.util.ext.isRegularFile
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.core.util.ext.toFileNameSafe
import org.koitharu.kotatsu.core.util.ext.toListSorted
import org.koitharu.kotatsu.local.data.MangaIndex
import org.koitharu.kotatsu.local.data.hasZipExtension
import org.koitharu.kotatsu.local.data.input.LocalMangaParser.Companion.URI_SCHEME_ZIP
import org.koitharu.kotatsu.local.data.isZipArchive
import org.koitharu.kotatsu.local.data.output.LocalMangaOutput.Companion.ENTRY_NAME_INDEX
import org.koitharu.kotatsu.local.domain.model.LocalManga
import org.koitharu.kotatsu.local.domain.model.LocalManga.Companion.URI_ZIP_SCHEME_PREFIX
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.util.longHashCode
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import org.koitharu.kotatsu.parsers.util.toTitleCase
import java.io.File

/**
 * Manga root {dir or zip file}
 * |--- index.json (optional)
 * |--- Page 1.png
 * |--- Page 2.png
 * |---Chapter 1/(dir or zip, optional)
 * |------Page 1.1.png
 * :
 * L--- Page x.png
 */
class LocalMangaParser(private val rootFile: File) {

	suspend fun getManga(withDetails: Boolean): LocalManga = runInterruptible(Dispatchers.IO) {
		rootFile.resolveFsAndPath().use { (fileSystem, rootPath) ->
			val index = MangaIndex.read(fileSystem, rootPath / ENTRY_NAME_INDEX)
			val mangaInfo = index?.getMangaInfo()
			if (mangaInfo != null) {
				val coverEntry: Path? = index.getCoverEntry()?.toPath()?.let { rootPath / it }?.takeIf {
					fileSystem.exists(it)
				}
				mangaInfo.copy(
					source = LocalMangaSource,
					url = rootFile.absolutePath,
					coverUrl = coverEntry?.let {
						rootFile.toPageUrl(it)
					} ?: fileSystem.findFirstImage(rootPath)?.let { rootFile.toPageUrl(it) },
					largeCoverUrl = null,
					chapters = if (withDetails) {
						mangaInfo.chapters?.mapNotNull { c ->
							val path = index.getChapterFileName(c.id)?.toPath()
							if (path != null && !fileSystem.exists(rootPath / path)) {
								null
							} else {
								c.copy(
									url = path?.let {
										File(rootFile, it.toString()).absolutePath
									} ?: rootFile.absolutePath,
									source = LocalMangaSource,
								)
							}
						}
					} else {
						null
					},
				)
			} else {
				val title = rootFile.name.fileNameToTitle()
				Manga(
					id = rootFile.absolutePath.longHashCode(),
					title = title,
					url = rootFile.absolutePath,
					publicUrl = rootFile.absolutePath,
					source = LocalMangaSource,
					coverUrl = fileSystem.findFirstImage(rootPath)?.let { rootFile.toPageUrl(it) },
					chapters = if (withDetails) {
						val chapters = fileSystem.listRecursively(rootPath)
							.mapNotNullTo(HashSet()) { path ->
								when {
									!fileSystem.isRegularFile(path) -> null
									path.isImage() -> path.parent
									hasZipExtension(path.name) -> path
									else -> null
								}
							}.sortedWith(compareBy(AlphanumComparator()) { x -> x.toString() })
						chapters.mapIndexed { i, p ->
							val s = p.relativeTo(rootPath).toString().removePrefix(Path.DIRECTORY_SEPARATOR)
							MangaChapter(
								id = "$i$s".longHashCode(),
								title = p.userFriendlyName(),
								number = 0f,
								volume = 0,
								source = LocalMangaSource,
								uploadDate = 0L,
								url = File(p.toString()).absolutePath,
								scanlator = null,
								branch = null,
							)
						}
					} else {
						null
					},
					altTitles = emptySet(),
					rating = -1f,
					contentRating = null,
					tags = emptySet(),
					state = null,
					authors = emptySet(),
					largeCoverUrl = null,
					description = null,
				)
			}.let { LocalManga(it, rootFile) }
		}
	}

	suspend fun getMangaInfo(): Manga? = runInterruptible(Dispatchers.IO) {
		rootFile.resolveFsAndPath().use { (fileSystem, rootPath) ->
			val index = MangaIndex.read(fileSystem, rootPath / ENTRY_NAME_INDEX)
			index?.getMangaInfo()
		}
	}

	suspend fun getPages(chapter: MangaChapter): List<MangaPage> = runInterruptible(Dispatchers.IO) {
		val chapterFile = resolveChapterFile(chapter.url)
		chapterFile.resolveFsAndPath().use { (fileSystem, rootPath) ->
			val index = MangaIndex.read(fileSystem, rootPath / ENTRY_NAME_INDEX)
			val entries = fileSystem.listRecursively(rootPath)
				.filter { fileSystem.isRegularFile(it) }
			val filtered = try {
				if (index != null) {
					val pattern = index.getChapterNamesPattern(chapter)
					entries.filter { x -> x.name.substringBefore('.').matches(pattern) }
				} else {
					entries.filter { x -> x.isImage() && x.parent == rootPath }
				}
			} catch (e: Exception) {
				// chapter not present in index.json (foreign archive) : fallback to direct images
				entries.filter { x -> x.isImage() }
			}
			filtered.toListSorted(compareBy(AlphanumComparator()) { x -> x.toString() })
				.map { x ->
					val pageUrl = chapterFile.toPageUrl(x)
					MangaPage(
						id = pageUrl.longHashCode(),
						url = pageUrl,
						preview = null,
						source = LocalMangaSource,
					)
				}
		}
	}

	/**
	 * Convertit une entrée [entry] (sous le fileSystem de [ref]) en URL de page :
	 * chemin absolu pour un dossier, « file+zip://<archive>#<entrée> » pour une archive.
	 */
	private fun File.toPageUrl(entry: Path): String = when {
		isZipArchive -> "$URI_SCHEME_ZIP$absolutePath#${entry.toString()}"

		else -> File(entry.toString()).absolutePath
	}

	/** Archive (ou dossier) référencée par une URL de chapitre. */
	private fun resolveChapterFile(url: String): File {
		val path = url.substringAfter(URI_ZIP_SCHEME_PREFIX, url).substringBefore('#')
		return File(path)
	}

	private fun File.resolveFsAndPath(): FsAndPath = when {
		isZipArchive -> FsAndPath(
			fileSystem = FileSystem.SYSTEM.openZip(this.toOkioPath()),
			// le fileSystem zip d'okio utilise toujours « / » comme séparateur (même sous Windows)
			path = "/".toPath(),
			isCloseable = true,
		)

		isDirectory -> FsAndPath(FileSystem.SYSTEM, toOkioPath(), isCloseable = false)

		else -> throw IllegalArgumentException("Unsupported local manga file: $absolutePath")
	}

	private fun FileSystem.findFirstImage(
		rootPath: Path,
		recursive: Boolean = false,
	): Path? = runCatchingCancellable {
		val list = list(rootPath)
		for (file in list.sortedWith(compareBy(AlphanumComparator()) { x -> x.name })) {
			if (isRegularFile(file)) {
				if (file.isImage()) {
					return@runCatchingCancellable file
				}
				if (recursive && hasZipExtension(file.name)) {
					openZip(file).use { zipFs ->
						zipFs.findFirstImage("/".toPath())?.let { subPath ->
							return@runCatchingCancellable subPath
						}
					}
				}
			} else if (recursive && isDirectory(file)) {
				findFirstImage(file, true)?.let {
					return@runCatchingCancellable it
				}
			}
		}
		if (recursive) {
			null
		} else {
			findFirstImage(rootPath, recursive = true)
		}
	}.onFailure { e ->
		e.printStackTraceDebug()
	}.getOrNull()

	private fun Path.userFriendlyName(): String = name.substringBeforeLast('.')
		.replace('_', ' ')
		.toTitleCase()

	private class FsAndPath(
		val fileSystem: FileSystem,
		val path: Path,
		private val isCloseable: Boolean,
	) : AutoCloseable {

		override fun close() {
			if (isCloseable) {
				fileSystem.close()
			}
		}

		operator fun component1() = fileSystem

		operator fun component2() = path
	}

	companion object {

		/** Préfixe des URLs pointant dans une archive : « file+zip://<chemin>#<entrée> ». */
		const val URI_SCHEME_ZIP = LocalManga.URI_ZIP_SCHEME_PREFIX

		fun getOrNull(file: File): LocalMangaParser? = if ((file.isDirectory || file.isZipArchive) && file.canRead()) {
			LocalMangaParser(file)
		} else {
			null
		}

		suspend fun find(roots: Iterable<File>, manga: Manga): LocalMangaParser? = channelFlow {
			val fileName = manga.title.toFileNameSafe()
			for (root in roots) {
				launch {
					val parser = getOrNull(File(root, fileName)) ?: getOrNull(File(root, "$fileName.cbz"))
					val info = runCatchingCancellable { parser?.getMangaInfo() }.getOrNull()
					if (info?.id == manga.id) {
						send(parser)
					}
				}
			}
		}.flowOn(Dispatchers.Default).firstOrNull()

		private fun Path.isImage(): Boolean = MimeTypes.getMimeTypeFromExtension(name)?.isImage == true

		private fun String.fileNameToTitle() = substringBeforeLast('.')
			.replace('_', ' ')
			.replaceFirstChar { it.uppercase() }
	}
}
