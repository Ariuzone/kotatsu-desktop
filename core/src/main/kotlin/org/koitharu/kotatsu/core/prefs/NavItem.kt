/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : identifiants Android (R.id/R.string/R.drawable) retirés — l'identifiant stable est
 * le nom de l'enum ; icônes et libellés définis côté UI desktop.
 */
package org.koitharu.kotatsu.core.prefs

enum class NavItem {

	HISTORY,
	FAVORITES,
	LOCAL,
	EXPLORE,
	SOURCES,
	SUGGESTIONS,
	FEED,
	UPDATED,
	BOOKMARKS,
	;

	fun isAvailable(settings: AppSettings): Boolean = when (this) {
		SUGGESTIONS -> settings.isSuggestionsEnabled
		UPDATED, FEED -> settings.isTrackerEnabled
		else -> true
	}
}
