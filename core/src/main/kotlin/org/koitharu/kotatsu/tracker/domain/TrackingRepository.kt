/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : version desktop simplifiée et fidèle de `tracker/domain/TrackingRepository.kt`.
 * — Sans Hilt ni `androidx.room.withTransaction` (room-ktx, Android-only) : les écritures sont
 *   sérialisées par un Mutex interne, ce qui suffit pour une application desktop mono-processus.
 * — Pas de TrackLog GC/stale policy/progress-update (modules non portés) : seule l'insertion
 *   du log de mise à jour est conservée.
 * — `updateTracks()` synchronise history + favourites comme l'original (défaut Android :
 *   TRACK_FAVOURITES seul ; ici les deux sont actifs par défaut, paramétrable).
 */

package org.koitharu.kotatsu.tracker.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.entity.toManga
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.tracker.data.TrackEntity
import org.koitharu.kotatsu.tracker.data.TrackLogEntity
import org.koitharu.kotatsu.tracker.domain.model.MangaTracking
import org.koitharu.kotatsu.tracker.domain.model.MangaUpdates
import java.time.Instant
import java.util.concurrent.TimeUnit

private const val NO_ID = 0L

/**
 * Dépôt du tracker : enregistre les mangas suivis (`tracks`) et persiste les mises à jour.
 * Réutilise le lot DB déjà porté (`tracker/data` : TracksDao, TrackWithManga, TrackLogEntity).
 */
class TrackingRepository(
	private val db: MangaDatabase,
	/** Sources de suivi actives, cf. AppSettings.trackSources Android (défaut : favoris uniquement). */
	val trackSources: Set<String> = DEFAULT_TRACK_SOURCES,
) {

	private val writeMutex = Mutex()


	/** Crée/supprime les lignes de suivi d'après l'historique et les favoris (cf. original). */
	suspend fun updateTracks(): Int = writeMutex.withLock {
		val dao = db.getTracksDao()
		val existingIds = dao.findAllIds().toMutableSet()
			val size = existingIds.size
			val handled = HashSet<Long>(size)
			// A manga can be tracked by both history and favourites: once the history pass has
			// handled an id, the favourites pass must not touch it (cf. original).
			if (TRACK_HISTORY in trackSources) {
				for (mangaId in db.getHistoryDao().findAllIds()) {
					if (handled.add(mangaId) && !existingIds.remove(mangaId)) {
						dao.upsert(TrackEntity.create(mangaId))
					}
				}
			}
			if (TRACK_FAVOURITES in trackSources) {
				for (mangaId in db.getFavouritesDao().findIdsWithTrack()) {
					if (handled.add(mangaId) && !existingIds.remove(mangaId)) {
						dao.upsert(TrackEntity.create(mangaId))
					}
				}
			}
		for (mangaId in existingIds) {
			dao.delete(mangaId)
		}
		return size - existingIds.size
	}

	/** Lot de pistes à vérifier (cf. GetTracksUseCase : activité récente < 90 j, re-check > 7 j). */
	suspend fun getTracks(limit: Int): List<MangaTracking> {
		val now = System.currentTimeMillis()
		val minActivityTime = now - TimeUnit.DAYS.toMillis(MAX_INACTIVE_DAYS)
		val staleCheckTime = now - TimeUnit.DAYS.toMillis(STALE_CHECK_DAYS)
		val items = db.getTracksDao().findAll(
			offset = 0,
			limit = limit,
			minActivityTime = minActivityTime,
			staleCheckTime = staleCheckTime,
		)
		return items.map { item ->
			MangaTracking(
				manga = item.manga.toManga(emptySet(), null),
				lastChapterId = item.track.lastChapterId,
				lastCheck = item.track.lastCheckTime.toInstantOrNull(),
				lastChapterDate = item.track.lastChapterDate.toInstantOrNull(),
				newChapters = item.track.newChapters,
			)
		}
	}

	suspend fun getTracksCount(): Int = db.getTracksDao().getTracksCount()

	suspend fun getNewChaptersCount(mangaId: Long): Int = db.getTracksDao().findNewChapters(mangaId)

	suspend fun getTrackOrNull(manga: Manga): MangaTracking? {
		val track = db.getTracksDao().find(manga.id) ?: return null
		return MangaTracking(
			manga = manga,
			lastChapterId = track.lastChapterId,
			lastCheck = track.lastCheckTime.toInstantOrNull(),
			lastChapterDate = track.lastChapterDate.toInstantOrNull(),
			newChapters = track.newChapters,
		)
	}

	/** Persiste le résultat d'une vérification et trace la mise à jour dans les logs. */
	suspend fun saveUpdates(updates: MangaUpdates) = writeMutex.withLock {
		val track = getOrCreateTrack(updates.manga.id).mergeWith(updates)
		db.getTracksDao().upsert(track)
		if (updates is MangaUpdates.Success && updates.isValid && updates.isNotEmpty()) {
			db.getTrackLogsDao().insert(
				TrackLogEntity(
					mangaId = updates.manga.id,
					chapters = updates.newChapters.joinToString("\n") { x -> x.name },
					createdAt = System.currentTimeMillis(),
					isUnread = true,
				),
			)
		}
	}

	suspend fun mergeWith(tracking: MangaTracking) {
		db.getTracksDao().upsert(
			TrackEntity(
				mangaId = tracking.manga.id,
				lastChapterId = tracking.lastChapterId,
				newChapters = tracking.newChapters,
				lastCheckTime = tracking.lastCheck?.toEpochMilli() ?: 0L,
				lastChapterDate = tracking.lastChapterDate?.toEpochMilli() ?: 0L,
				lastResult = TrackEntity.RESULT_EXTERNAL_MODIFICATION,
				lastError = null,
			),
		)
	}

	suspend fun clearCounter(mangaId: Long) = db.getTracksDao().clearCounter(mangaId)

	private suspend fun getOrCreateTrack(mangaId: Long): TrackEntity {
		return db.getTracksDao().find(mangaId) ?: TrackEntity.create(mangaId)
	}

	private fun TrackEntity.mergeWith(updates: MangaUpdates): TrackEntity = when (updates) {
		is MangaUpdates.Failure -> TrackEntity(
			mangaId = mangaId,
			lastChapterId = lastChapterId,
			newChapters = newChapters,
			lastCheckTime = System.currentTimeMillis(),
			lastChapterDate = lastChapterDate,
			lastResult = TrackEntity.RESULT_FAILED,
			lastError = updates.error?.toString(),
		)

		is MangaUpdates.Success -> {
			val chapters = updates.manga.chapters
			TrackEntity(
				mangaId = mangaId,
				lastChapterId = chapters?.lastOrNull()?.id ?: NO_ID,
				// Cap at the total chapter count (cf. original).
				newChapters = if (updates.isValid) {
					(newChapters + updates.newChapters.size).coerceIn(0, chapters?.size ?: 0)
				} else {
					0
				},
				lastCheckTime = System.currentTimeMillis(),
				lastChapterDate = updates.lastChapterDate().takeIf { it != 0L } ?: lastChapterDate,
				lastResult = if (updates.isNotEmpty()) TrackEntity.RESULT_HAS_UPDATE else TrackEntity.RESULT_NO_UPDATE,
				lastError = null,
			)
		}
	}

	private fun Long.toInstantOrNull(): Instant? = if (this == 0L) null else Instant.ofEpochMilli(this)

	companion object {

		const val TRACK_HISTORY = "history"
		const val TRACK_FAVOURITES = "favourites"
		val DEFAULT_TRACK_SOURCES: Set<String> = setOf(TRACK_HISTORY, TRACK_FAVOURITES)

		const val MAX_INACTIVE_DAYS = 90L
		const val STALE_CHECK_DAYS = 7L
	}
}
