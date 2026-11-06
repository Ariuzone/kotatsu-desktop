/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.local.data

import java.io.File
import java.io.FileFilter

class TempFileFilter : FileFilter {

	override fun accept(file: File): Boolean {
		return file.name.endsWith(".tmp", ignoreCase = true)
	}
}
