/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.explore.data

// TODO(merge) : réintégrer `@StringRes val titleResId` (R.string) quand les ressources
// Android seront remplacées par la i18n desktop.

enum class SourcesSortOrder {
	ALPHABETIC,

	/**
	 * Note this is *local* popularity - how many manga from the source are in this device's own
	 * database. [SCORE] is the community measure.
	 */
	POPULARITY,
	MANUAL,
	LAST_USED,

	/**
	 * Community reliability and popularity blended with this device's own experience of each source
	 * (`sourcescore/domain/SourceRank.kt`).
	 *
	 * Ordered in Kotlin rather than SQL, because the scores live in a separate Room database - kept
	 * apart so upstream Kotatsu's migrations never collide with this fork's.
	 */
	SCORE,
	;

	/** True when the ordering cannot be expressed as an ORDER BY over the sources table alone. */
	val isExternallyRanked: Boolean get() = this == SCORE
}
