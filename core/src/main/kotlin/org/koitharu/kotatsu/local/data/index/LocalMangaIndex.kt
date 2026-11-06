/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : SharedPreferences remplacé par FileKeyValueStore ; MangaDataRepository non porté —
 * l'upsert manga+tags est fait ici via les DAO ; `db.withTransaction` (room-ktx, Android seul)
 * remplacé par des appels DAO séquentiels protégés par le mutex ; injection Hilt retirée
 * (le dépôt local est fourni via un provider paresseux pour casser la dépendance circulaire).
 */
package org.koitharu.kotatsu.local.data.index

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.entity.toEntities
import org.koitharu.kotatsu.core.db.entity.toEntity
import org.koitharu.kotatsu.local.domain.model.isLocal
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.local.data.LocalMangaRepository
import org.koitharu.kotatsu.local.data.input.LocalMangaParser
import org.koitharu.kotatsu.local.domain.model.LocalManga
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import java.io.File

class LocalMangaIndex(
	private val db: MangaDatabase,
	private val localMangaRepositoryProvider: () -> LocalMangaRepository,
	prefsFile: File,
) : FlowCollector<LocalManga?> {

	private val prefs = FileKeyValueStore(prefsFile)
	private val mutex = Mutex()

	private var currentVersion: Int
		get() = prefs.getInt(KEY_VERSION, 0)
		set(value) = prefs.edit { putInt(KEY_VERSION, value) }

	override suspend fun emit(value: LocalManga?) {
		if (value != null) {
			put(value)
		}
	}

	suspend fun update() = mutex.withLock {
		rebuild()
	}

	suspend fun updateIfRequired() {
		if (isUpdateRequired()) {
			mutex.withLock {
				// The first build may have finished while this caller was waiting for it
				if (isUpdateRequired()) {
					rebuild()
				}
			}
		}
	}

	suspend fun get(mangaId: Long, withDetails: Boolean): LocalManga? {
		updateIfRequired()
		// A rebuild in progress leaves the previous index in place until it commits, so a miss here
		// is an answer rather than a reason to wait for the whole storage scan
		val path = db.getLocalMangaIndexDao().findPath(mangaId) ?: return null
		return runCatchingCancellable {
			LocalMangaParser(File(path)).getManga(withDetails)
		}.onFailure {
			it.printStackTraceDebug()
		}.getOrNull()
	}

	suspend operator fun contains(mangaId: Long): Boolean {
		return db.getLocalMangaIndexDao().findPath(mangaId) != null
	}

	suspend fun put(manga: LocalManga) = mutex.withLock {
		storeManga(manga.manga, replaceExisting = true)
		db.getLocalMangaIndexDao().upsert(manga.toEntity())
	}

	suspend fun delete(mangaId: Long) {
		db.getLocalMangaIndexDao().delete(mangaId)
	}

	suspend fun getAvailableTags(skipNsfw: Boolean): List<String> {
		val dao = db.getLocalMangaIndexDao()
		return if (skipNsfw) {
			dao.findTags(isNsfw = false)
		} else {
			dao.findTags()
		}
	}

	/**
	 * Scans local storage first and writes the index afterwards.
	 *
	 * À la différence de l'original Android, les écritures ne sont pas regroupées dans une
	 * transaction Room unique : `withTransaction` n'existe pas sur le runtime Room JVM (room-ktx
	 * est Android-only) et les DAO suspendus ne peuvent pas s'exécuter sur la connexion
	 * d'écriture ouverte manuellement. Le mutex garantit l'exclusivité du rebuild.
	 */
	private suspend fun rebuild() {
		val found = localMangaRepositoryProvider()
			.getRawListAsFlow()
			.toList()
		db.getLocalMangaIndexDao().clear()
		found.forEach { entity ->
			storeManga(entity.manga, replaceExisting = true)
			db.getLocalMangaIndexDao().upsert(entity.toEntity())
		}
		currentVersion = VERSION
	}

	/**
	 * Équivalent minimal de MangaDataRepository.storeManga (le stockage des chapitres est
	 * inutile pour les manga locaux, comme dans l'original).
	 */
	private suspend fun storeManga(manga: org.koitharu.kotatsu.parsers.model.Manga, replaceExisting: Boolean) {
		if (!replaceExisting && db.getMangaDao().find(manga.id) != null) {
			return
		}
		// avoid storing local manga if remote one is already stored
		val existing = if (manga.isLocal) {
			db.getMangaDao().find(manga.id)?.manga
		} else {
			null
		}
		if (existing == null || existing.source == manga.source.name) {
			val tags = manga.tags.toEntities()
			db.getTagsDao().upsert(tags)
			db.getMangaDao().upsert(manga.toEntity(), tags)
		}
	}

	private fun LocalManga.toEntity() = LocalMangaIndexEntity(
		mangaId = manga.id,
		path = file.path,
	)

	private fun isUpdateRequired() = currentVersion < VERSION

	companion object {

		private const val KEY_VERSION = "ver"
		private const val VERSION = 1
	}
}
