/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.io

import java.io.OutputStream
import java.util.Objects

class NullOutputStream : OutputStream() {

	override fun write(b: Int) = Unit

	override fun write(b: ByteArray, off: Int, len: Int) {
		Objects.checkFromIndexSize(off, len, b.size)
	}
}
