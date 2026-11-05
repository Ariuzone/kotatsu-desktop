/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.core.model.MangaHistory`
 * (sans Parcelable / dépendances UI). À REMPLACER par la version complète d'origine à la fusion.
 */
package org.koitharu.kotatsu.core.model

import java.time.Instant

data class MangaHistory(
	val createdAt: Instant,
	val updatedAt: Instant,
	val chapterId: Long,
	val page: Int,
	val scroll: Int,
	val percent: Float,
	val chaptersCount: Int,
)
