/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.tracker.domain.model

import org.koitharu.kotatsu.parsers.model.Manga
import java.time.Instant

/**
 * État de suivi d'un manga par le tracker.
 * Portage fidèle de `tracker/domain/model/MangaTracking.kt` (Android) — sans DI ni annotations.
 */
data class MangaTracking(
	val manga: Manga,
	val lastChapterId: Long,
	val lastCheck: Instant?,
	val lastChapterDate: Instant?,
	val newChapters: Int,
) {

	fun isEmpty(): Boolean {
		return lastChapterId == 0L
	}
}
