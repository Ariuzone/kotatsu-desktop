/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : Parcelable/androidx.work.Data retirés — la tâche est passée en mémoire
 * (file d'attente de DownloadManager) au lieu d'être sérialisée dans WorkManager.
 */
package org.koitharu.kotatsu.download.domain

import org.koitharu.kotatsu.core.prefs.DownloadFormat
import java.io.File

class DownloadTask(
	val mangaId: Long,
	val isPaused: Boolean = false,
	val isSilent: Boolean = false,
	val chaptersIds: LongArray? = null,
	val destination: File? = null,
	val format: DownloadFormat? = null,
	val allowMeteredNetwork: Boolean = true,
) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as DownloadTask

		if (mangaId != other.mangaId) return false
		if (isPaused != other.isPaused) return false
		if (isSilent != other.isSilent) return false
		if (!(chaptersIds contentEquals other.chaptersIds)) return false
		if (destination != other.destination) return false
		if (format != other.format) return false
		if (allowMeteredNetwork != other.allowMeteredNetwork) return false

		return true
	}

	override fun hashCode(): Int {
		var result = mangaId.hashCode()
		result = 31 * result + isPaused.hashCode()
		result = 31 * result + isSilent.hashCode()
		result = 31 * result + (chaptersIds?.contentHashCode() ?: 0)
		result = 31 * result + (destination?.hashCode() ?: 0)
		result = 31 * result + (format?.hashCode() ?: 0)
		result = 31 * result + allowMeteredNetwork.hashCode()
		return result
	}
}
