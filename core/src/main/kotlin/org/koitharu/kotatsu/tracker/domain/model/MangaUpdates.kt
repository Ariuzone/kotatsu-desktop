/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.tracker.domain.model

import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.util.ifZero

/**
 * Résultat d'une vérification de nouveaux chapitres.
 * Portage fidèle de `tracker/domain/model/MangaUpdates.kt` (Android).
 */
sealed interface MangaUpdates {

	val manga: Manga

	data class Success(
		override val manga: Manga,
		val branch: String?,
		val newChapters: List<MangaChapter>,
		val isValid: Boolean,
	) : MangaUpdates {

		fun isNotEmpty() = newChapters.isNotEmpty()

		fun lastChapterDate(): Long {
			val lastChapter = newChapters.lastOrNull()
			return lastChapter?.uploadDate?.ifZero { System.currentTimeMillis() }
				?: (manga.chapters?.lastOrNull()?.uploadDate ?: 0L)
		}
	}

	data class Failure(
		override val manga: Manga,
		val error: Throwable?,
	) : MangaUpdates
}
