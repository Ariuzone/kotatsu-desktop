/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Round-trip CBZ/dossier -> LocalMangaParser -> LocalMangaIndex/LocalMangaRepository sur
 * fichiers temporaires (pas de réseau).
 */
package org.koitharu.kotatsu.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.koitharu.kotatsu.core.util.ext.MimeType
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koitharu.kotatsu.core.db.MangaDatabase_Impl
import org.koitharu.kotatsu.core.model.LocalMangaSource
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.DownloadFormat
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.local.data.LocalMangaRepository
import org.koitharu.kotatsu.local.data.LocalStorageManager
import org.koitharu.kotatsu.local.data.index.LocalMangaIndex
import org.koitharu.kotatsu.local.data.input.LocalMangaParser
import org.koitharu.kotatsu.local.data.output.LocalMangaOutput
import org.koitharu.kotatsu.local.domain.MangaLock
import org.koitharu.kotatsu.local.domain.model.LocalManga
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import java.io.File
import java.util.zip.ZipFile

class LocalMangaRoundTripTest {

	@get:Rule
	val tempFolder = TemporaryFolder()

	private lateinit var db: org.koitharu.kotatsu.core.db.MangaDatabase
	private lateinit var repository: LocalMangaRepository
	private lateinit var index: LocalMangaIndex
	private lateinit var storageRoot: File

	private val fakeCover = ByteArray(64) { (it % 113).toByte() }
	private val fakePage = ByteArray(128) { (it % 89).toByte() }

	@Before
	fun setUp() {
		storageRoot = tempFolder.newFolder("manga")
		db = Room
			.inMemoryDatabaseBuilder(::MangaDatabase_Impl)
			.setDriver(BundledSQLiteDriver())
			.build()
		index = LocalMangaIndex(
			db = db,
			localMangaRepositoryProvider = { repository },
			prefsFile = File(tempFolder.root, "_local_index.json"),
		)
		repository = LocalMangaRepository(
			storageManager = LocalStorageManager(defaultDir = storageRoot),
			localMangaIndex = index,
			localStorageChanges = MutableSharedFlow(),
			settings = AppSettings(FileKeyValueStore(File(tempFolder.root, "settings.json"))),
			lock = MangaLock(),
		)
	}

	private fun testManga(id: Long, title: String, chaptersCount: Int) = Manga(
		id = id,
		title = title,
		altTitles = emptySet(),
		url = "https://example.org/manga/$title",
		publicUrl = "https://example.org/manga/$title",
		rating = 0.8f,
		contentRating = null,
		coverUrl = "https://example.org/cover.jpg",
		tags = emptySet(),
		state = null,
		authors = setOf("Author"),
		largeCoverUrl = null,
		description = "Test description",
		chapters = (0 until chaptersCount).map { i ->
			MangaChapter(
				id = "$id-ch$i".hashCode().toLong(),
				title = "Chapter $i",
				number = i + 1f,
				volume = 0,
				url = "https://example.org/manga/$title/ch$i",
				scanlator = null,
				uploadDate = 0L,
				branch = null,
				source = MangaParserSource.MANGADEX,
			)
		},
		source = MangaParserSource.MANGADEX,
	)

	private fun newImage(dir: File, name: String, data: ByteArray): File {
		val file = File(dir, name)
		file.writeBytes(data)
		return file
	}

	/** Télécharge « manuellement » un manga complet via LocalMangaOutput (cf. DownloadManager). */
	private fun downloadManga(manga: Manga, format: DownloadFormat): File {
		return kotlinx.coroutines.runBlocking {
			val output = LocalMangaOutput.getOrCreate(
				root = storageRoot,
				manga = manga,
				format = format,
			)
			try {
				val cover = newImage(tempFolder.newFolder("src"), "cover.png", fakeCover)
				output.addCover(cover, null)
				val chapters = checkNotNull(manga.chapters)
				for ((i, chapter) in chapters.withIndex()) {
					for (p in 0 until 2) {
						val page = newImage(tempFolder.newFolder("src$p$i"), "page.png", fakePage)
						output.addPage(IndexedValue(i, chapter), page, p, MimeType("image/png"))
					}
					output.flushChapter(chapter)
				}
				output.finish()
			} finally {
				output.close()
			}
			LocalMangaOutput.get(storageRoot, manga)!!.rootFile
		}
	}

	@Test
	fun `single cbz round trip`() = runBlocking {
		val manga = testManga(id = 101L, title = "Test CBZ Manga", chaptersCount = 2)
		val file = downloadManga(manga, format = DownloadFormat.SINGLE_CBZ)
		assertTrue("Expected a single cbz file: $file", file.isFile && file.extension == "cbz")
		ZipFile(file).use { zip ->
			val names = zip.entries().toList().map { it.name }
			assertTrue("index.json expected in $names", "index.json" in names)
			assertEquals(5, names.count { it != "index.json" }) // cover + 4 pages
		}

		val parser = LocalMangaParser(file)
		val parsed = parser.getManga(withDetails = true)
		assertEquals(manga.id, parsed.manga.id)
		assertEquals(manga.title, parsed.manga.title)
		assertEquals(LocalMangaSource, parsed.manga.source)
		val chapters = checkNotNull(parsed.manga.chapters)
		assertEquals(2, chapters.size)

		val pages = parser.getPages(chapters[0])
		assertEquals(2, pages.size)
		assertTrue("Page url must address the archive: ${pages[0].url}", pages[0].url.startsWith("file+zip://"))
		assertEquals(pages[0].url, repository.getPageUrl(pages[0]))

		val pagesOfChapter2 = parser.getPages(chapters[1])
		assertEquals(2, pagesOfChapter2.size)
	}

	@Test
	fun `multiple cbz (dir) round trip`() = runBlocking {
		val manga = testManga(id = 202L, title = "Test Dir Manga", chaptersCount = 2)
		val dir = downloadManga(manga, format = DownloadFormat.MULTIPLE_CBZ)
		assertTrue("Expected a directory: $dir", dir.isDirectory)
		println("DBG dir=$dir exists=${dir.exists()} children=${dir.listFiles()?.map { it.name }}")
		println("DBG index.json=" + File(dir, "index.json").readText())
		val chapterFiles = dir.listFiles { f -> f.extension == "cbz" }.orEmpty()
		assertEquals(2, chapterFiles.size)

		val parser = LocalMangaParser(dir)
		val info = parser.getMangaInfo()
		println("DBG info=${info?.chapters}")
		val parsed = parser.getManga(withDetails = true)
		println("DBG parsed chapters=${parsed.manga.chapters} url=${parsed.manga.url}")
		assertEquals(manga.id, parsed.manga.id)
		val chapters = checkNotNull(parsed.manga.chapters)
		assertEquals(2, chapters.size)

		val pages = parser.getPages(chapters[0])
		val cbzFile = File(chapters[0].url)
		println("DBG cbz exists=${cbzFile.isFile} size=${cbzFile.length()}")
		if (cbzFile.isFile) {
			println("DBG entries=" + java.util.zip.ZipFile(cbzFile).use { z -> z.entries().toList().map { it.name } })
		}
		println("DBG pages=$pages")
		assertEquals(2, pages.size)
		// pages d'un chapitre CBZ : référencées comme entrées d'archive
		assertTrue(pages[0].url.startsWith("file+zip://"))
	}

	@Test
	fun `folder of images without index`() = runBlocking {
		val mangaDir = tempFolder.newFolder("manga", "Plain Folder")
		newImage(mangaDir, "001.png", fakePage)
		newImage(mangaDir, "002.png", fakePage)
		val chapterDir = File(mangaDir, "Chapter 1").also { it.mkdirs() }
		newImage(chapterDir, "p1.png", fakePage)
		newImage(chapterDir, "p2.png", fakePage)

		val parser = LocalMangaParser(mangaDir)
		val parsed = parser.getManga(withDetails = true)
		assertEquals("Plain Folder", parsed.manga.title)
		val chapters = checkNotNull(parsed.manga.chapters)
		assertEquals(2, chapters.size) // root images + "Chapter 1" dir

		val rootPages = parser.getPages(chapters.first { it.url == mangaDir.absolutePath })
		assertEquals(2, rootPages.size)
		assertTrue(File(rootPages[0].url).isFile)
	}

	@Test
	fun `repository and index round trip`() = runBlocking {
		val manga = testManga(id = 303L, title = "Repository Manga", chaptersCount = 1)
		val file = downloadManga(manga, format = DownloadFormat.SINGLE_CBZ)

		// ré-indexation complète depuis le stockage
		index.update()
		val path = db.getLocalMangaIndexDao().findPath(manga.id)
		assertEquals(file.absolutePath, path)

		val listed = repository.getList(offset = 0, order = org.koitharu.kotatsu.parsers.model.SortOrder.ALPHABETICAL, filter = null)
		assertTrue(listed.any { it.id == manga.id })

		val local: LocalManga? = index.get(manga.id, withDetails = true)
		assertNotNull(local)
		val chapters = checkNotNull(local!!.manga.chapters)
		assertEquals(1, chapters.size)

		// lecture via le dépôt local (source LOCAL)
		val details = repository.getDetails(
			manga.copy(source = LocalMangaSource, url = file.absolutePath),
		)
		val localChapters = checkNotNull(details.chapters)
		assertEquals(1, localChapters.size)

		val pages = repository.getPages(localChapters[0])
		assertEquals(2, pages.size)
		assertTrue(pages[0].url.startsWith("file+zip://"))
		assertEquals(pages[0].url, repository.getPageUrl(pages[0]))

		// recherche d'un manga distant enregistré localement
		val saved = repository.findSavedManga(manga, withDetails = true)
		assertNotNull(saved)
	}
}
