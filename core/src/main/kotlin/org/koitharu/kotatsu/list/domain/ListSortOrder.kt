/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.list.domain.ListSortOrder`
 * (sans R.string.titleResId). À REMPLACER par la version complète d'origine à la fusion.
 */
package org.koitharu.kotatsu.list.domain

import org.koitharu.kotatsu.parsers.util.find
import java.util.EnumSet

enum class ListSortOrder {
	NEWEST,
	OLDEST,
	PROGRESS,
	UNREAD,
	ALPHABETIC,
	ALPHABETIC_REVERSE,
	RATING,
	RELEVANCE,
	NEW_CHAPTERS,
	LAST_READ,
	LONG_AGO_READ,
	UPDATED,
	;

	fun isGroupingSupported() = this == LAST_READ || this == NEWEST || this == PROGRESS

	companion object {

		val HISTORY: Set<ListSortOrder> = EnumSet.of(
			LAST_READ,
			LONG_AGO_READ,
			NEWEST,
			OLDEST,
			PROGRESS,
			UNREAD,
			ALPHABETIC,
			ALPHABETIC_REVERSE,
			NEW_CHAPTERS,
			UPDATED,
		)
		val FAVORITES: Set<ListSortOrder> = EnumSet.of(
			ALPHABETIC,
			ALPHABETIC_REVERSE,
			NEWEST,
			OLDEST,
			RATING,
			NEW_CHAPTERS,
			PROGRESS,
			UNREAD,
			LAST_READ,
			LONG_AGO_READ,
			UPDATED,
		)
		val SUGGESTIONS: Set<ListSortOrder> = EnumSet.of(RELEVANCE)

		operator fun invoke(value: String, fallback: ListSortOrder) = entries.find(value) ?: fallback
	}
}
