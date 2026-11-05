/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : @StringRes retiré (libellés définis côté UI desktop).
 */
package org.koitharu.kotatsu.core.prefs

enum class SearchSuggestionType {

	GENRES,
	QUERIES_RECENT,
	QUERIES_SUGGEST,
	MANGA,
	SOURCES,
	RECENT_SOURCES,
	AUTHORS,
}
