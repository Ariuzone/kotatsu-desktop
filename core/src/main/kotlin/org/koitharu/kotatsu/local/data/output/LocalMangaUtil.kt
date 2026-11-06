/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : android.net.Uri remplacé par java.io.File.
 */
package org.koitharu.kotatsu.local.data.output

import org.koitharu.kotatsu.local.domain.model.isLocal
import org.koitharu.kotatsu.parsers.model.Manga
import java.io.File

class LocalMangaUtil(
	private val manga: Manga,
) {

	init {
		require(manga.isLocal) { "Expected LOCAL source but ${manga.source} found" }
	}

	suspend fun deleteChapters(ids: Set<Long>) {
		val file = File(manga.url)
		if (file.isDirectory) {
			LocalMangaDirOutput(file, manga).use { output ->
				output.deleteChapters(ids)
				output.finish()
			}
		} else {
			LocalMangaZipOutput.filterChapters(file, manga, ids)
		}
	}
}
