/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.favourites.domain.model.Cover`
 * (sans la propriété `mangaSource` dépendant de core/model MangaSource). À REMPLACER par
 * la version complète d'origine à la fusion.
 */
package org.koitharu.kotatsu.favourites.domain.model

data class Cover(
	val url: String?,
	val source: String,
)
