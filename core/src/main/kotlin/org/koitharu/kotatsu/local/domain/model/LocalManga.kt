/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : android.net.Uri remplacé par une représentation fichiers (chemins absolus,
 * entrées d'archives adressées via le schéma « file+zip://<chemin>#<entrée> »).
 */
package org.koitharu.kotatsu.local.domain.model

import org.koitharu.kotatsu.core.util.ext.contains
import org.koitharu.kotatsu.core.util.ext.creationTime
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaTag
import java.io.File

data class LocalManga(
	val manga: Manga,
	val file: File = File(manga.url.substringAfter(URI_ZIP_SCHEME_PREFIX, manga.url).substringBefore('#')),
) {

	var createdAt: Long = -1L
		private set
		get() {
			if (field == -1L) {
				field = file.creationTime
			}
			return field
		}

	fun toUrlString(): String = manga.url

	fun isMatchesQuery(query: String): Boolean {
		return manga.title.contains(query, ignoreCase = true) ||
			manga.altTitles.contains(query, ignoreCase = true) ||
			manga.authors.contains(query, ignoreCase = true)
	}

	fun containsTags(tags: Collection<String>): Boolean {
		return tags.all { tag -> tag in manga.tags }
	}

	fun containsAnyTag(tags: Collection<String>): Boolean {
		return tags.any { tag -> tag in manga.tags }
	}

	private operator fun Collection<MangaTag>.contains(title: String): Boolean {
		return any { it.title.equals(title, ignoreCase = true) }
	}

	override fun toString(): String {
		return "LocalManga(${file.path}: ${manga.title})"
	}

	companion object {

		/** Préfixe des URLs d'archives locales : « file+zip://<chemin absolu>#<entrée> ». */
		const val URI_ZIP_SCHEME_PREFIX = "file+zip://"
	}
}
