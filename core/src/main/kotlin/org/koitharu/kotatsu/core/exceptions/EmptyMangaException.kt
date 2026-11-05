/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : EmptyMangaReason est ici défini localement (version desktop sans @StringRes) ;
 * l'original le déclare dans le module details.ui (Android).
 */
package org.koitharu.kotatsu.core.exceptions

import org.koitharu.kotatsu.parsers.model.Manga

enum class EmptyMangaReason {
	NO_CHAPTERS,
	LOADING_ERROR,
	RESTRICTED,
}

class EmptyMangaException(
	val reason: EmptyMangaReason?,
	val manga: Manga,
	cause: Throwable?,
) : IllegalStateException(cause)
