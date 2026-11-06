/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.core.model.FavouriteCategory`
 * (sans Parcelable/ListModel, dépendances UI Android). À REMPLACER par la version
 * complète d'origine à la fusion.
 */
package org.koitharu.kotatsu.core.model

import org.koitharu.kotatsu.list.domain.ListSortOrder
import java.time.Instant

data class FavouriteCategory(
	val id: Long,
	val title: String,
	val sortKey: Int,
	val order: ListSortOrder,
	val createdAt: Instant,
	val isTrackingEnabled: Boolean,
	val isVisibleInLibrary: Boolean,
)
