/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.download.domain

data class DownloadProgress(
	val totalChapters: Int,
	val currentChapter: Int,
	val totalPages: Int,
	val currentPage: Int,
)
