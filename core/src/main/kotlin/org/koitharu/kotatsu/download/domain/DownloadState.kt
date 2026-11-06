/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : WorkManager Data (toWorkData / get*) retiré — la progression est publiée via
 * StateFlow ; la constante PROGRESS_NONE (list/domain, non porté) est remplacée par -1f.
 */
package org.koitharu.kotatsu.download.domain

import org.koitharu.kotatsu.local.domain.model.LocalManga
import org.koitharu.kotatsu.parsers.model.Manga

/** Équivalent desktop de DownloadState (l'original dépend d'androidx.work.Data). */
data class DownloadState(
	val manga: Manga,
	val isIndeterminate: Boolean,
	val isPaused: Boolean = false,
	val isStopped: Boolean = false,
	val error: Throwable? = null,
	val errorMessage: String? = null,
	val totalChapters: Int = 0,
	val currentChapter: Int = 0,
	val totalPages: Int = 0,
	val currentPage: Int = 0,
	val eta: Long = -1L,
	val isStuck: Boolean = false,
	val localManga: LocalManga? = null,
	val downloadedChapters: Int = 0,
	val timestamp: Long = System.currentTimeMillis(),
) {

	val max: Int = totalChapters * totalPages

	val progress: Int = totalPages * currentChapter + currentPage + 1

	val percent: Float = if (max > 0) progress.toFloat() / max else PROGRESS_NONE

	val isFinalState: Boolean
		get() = localManga != null || (error != null && !isPaused)

	val isParticularProgress: Boolean
		get() = localManga == null && error == null && !isPaused && !isStopped && max > 0 && !isIndeterminate

	companion object {

		/** Anciennement ReadingProgress.PROGRESS_NONE (module list, non porté sur desktop). */
		const val PROGRESS_NONE = -1f
	}
}
