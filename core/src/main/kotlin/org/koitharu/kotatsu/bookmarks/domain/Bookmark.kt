/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.bookmarks.domain.Bookmark`
 * (sans ListModel ni toMangaPage/MimeTypes). À REMPLACER par la version complète d'origine
 * à la fusion.
 */
package org.koitharu.kotatsu.bookmarks.domain

import org.koitharu.kotatsu.parsers.model.Manga
import java.time.Instant

data class Bookmark(
	val manga: Manga,
	val pageId: Long,
	val chapterId: Long,
	val page: Int,
	val scroll: Int,
	val imageUrl: String,
	val createdAt: Instant,
	val percent: Float,
)
