/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : portage fidèle et simplifié de `tracker/domain/CheckNewChaptersUseCase.kt`.
 * — Les détails manga sont obtenus via [MangaDetailsLoader] (abstraction testable sur
 *   `MangaRepository.Factory.create(source).getDetails(manga)`).
 * — La comparaison ignore les branches (« branch = null » : liste complète des chapitres) ;
 *   les ancres de l'original sont conservées : lastChapterId puis lastChapterDate (fallback
 *   sur l'uploadDate quand l'id n'est plus dans la liste), re-baseline silencieux sinon.
 * — Le filtrage par position de lecture (history) est omis : HistoryRepository n'est pas
 *   encore câblé au tracker desktop.
 */

package org.koitharu.kotatsu.tracker.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.tracker.domain.model.MangaTracking
import org.koitharu.kotatsu.tracker.domain.model.MangaUpdates

class CheckNewChaptersUseCase(
	private val repository: TrackingRepository,
	private val detailsLoader: MangaDetailsLoader,
) {

	private val mutexes = java.util.concurrent.ConcurrentHashMap<Long, Mutex>()

	/** Vérifie les nouveaux chapitres d'un manga suivi, puis persiste le résultat. */
	suspend operator fun invoke(track: MangaTracking): MangaUpdates = mutex(track.manga.id).withLock {
		invokeImpl(track)
	}

	/** Variante prenant le manga : récupère la piste existante (ou la crée à vide). */
	suspend operator fun invoke(manga: Manga): MangaUpdates {
		repository.updateTracks()
		val track = repository.getTrackOrNull(manga) ?: MangaTracking(
			manga = manga,
			lastChapterId = 0L,
			lastCheck = null,
			lastChapterDate = null,
			newChapters = 0,
		)
		return invoke(track)
	}

	private suspend fun invokeImpl(track: MangaTracking): MangaUpdates {
		val details = getFullManga(track.manga)
		val updates = compare(track, details)
		repository.saveUpdates(updates)
		return updates
	}

	private suspend fun getFullManga(manga: Manga): Manga =
		if (manga.chapters.isNullOrEmpty()) detailsLoader.getDetails(manga) else manga

	/**
	 * Fonction principale du tracker : compare [track] aux chapitres actuels de [manga].
	 * Ancres, dans l'ordre (comme l'original) :
	 *  1. [MangaTracking.lastChapterId] — baseline du tracker.
	 *  2. [MangaTracking.lastChapterDate] — date d'upload du dernier chapitre connu ; robuste
	 *     au churn d'ids de certaines sources.
	 *  3. piste vide ou sans ancre utilisable -> re-baseline silencieux (pas de notification).
	 */
	private fun compare(
		track: MangaTracking,
		manga: Manga,
	): MangaUpdates {
		val chapters = manga.chapters.orEmpty()
		if (track.isEmpty()) {
			// Premier contrôle (installation, restauration, nouveau manga) : baseline silencieuse.
			return MangaUpdates.Success(manga = manga, branch = null, newChapters = emptyList(), isValid = false)
		}
		compareAgainst(manga, chapters, track.lastChapterId)?.let { return it }
		// lastChapterId est absent de la liste fraîche -> baseline par date.
		compareByDate(manga, chapters, track.lastChapterDate?.toEpochMilli() ?: 0L)?.let { return it }
		// Aucune ancre utilisable : re-baseline silencieux.
		return MangaUpdates.Success(manga = manga, branch = null, newChapters = emptyList(), isValid = false)
	}

	/**
	 * Chapitres strictement après [anchorChapterId] ; `null` si l'ancre est absente de la liste
	 * (comme l'original : liste entière « nouvelle » = ancre introuvable, on tente le fallback date).
	 */
	private fun compareAgainst(
		manga: Manga,
		chapters: List<MangaChapter>,
		anchorChapterId: Long,
	): MangaUpdates.Success? {
		val newChapters = chapters.takeLastWhile { x -> x.id != anchorChapterId }
		return when {
			newChapters.isEmpty() -> MangaUpdates.Success(
				manga = manga,
				branch = null,
				newChapters = emptyList(),
				isValid = chapters.lastOrNull()?.id == anchorChapterId,
			)

			newChapters.size == chapters.size -> null // ancre introuvable dans la liste

			else -> MangaUpdates.Success(manga, null, newChapters, isValid = true)
		}
	}

	/**
	 * Fallback par date : chapitres uploadés strictement après [lastChapterDateMillis].
	 * `null` quand la date est inutilisable (zéro, ou antérieure à tous les chapitres —
	 * probablement une donnée corrompue qui marquerait toute la liste comme nouvelle).
	 */
	private fun compareByDate(
		manga: Manga,
		chapters: List<MangaChapter>,
		lastChapterDateMillis: Long,
	): MangaUpdates.Success? {
		if (lastChapterDateMillis <= 0L) return null
		val newChapters = chapters.filter { it.uploadDate > lastChapterDateMillis }
		return when {
			newChapters.isEmpty() -> MangaUpdates.Success(manga, null, emptyList(), isValid = true)
			newChapters.size == chapters.size -> null
			else -> MangaUpdates.Success(manga, null, newChapters, isValid = true)
		}
	}

	private fun mutex(mangaId: Long): Mutex = mutexes.getOrPut(mangaId) { Mutex() }
}
