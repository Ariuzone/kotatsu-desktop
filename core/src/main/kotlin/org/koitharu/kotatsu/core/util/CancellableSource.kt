/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import okio.Buffer
import okio.ForwardingSource
import okio.Source

class CancellableSource(
	private val job: Job?,
	delegate: Source,
) : ForwardingSource(delegate) {

	override fun read(sink: Buffer, byteCount: Long): Long {
		job?.ensureActive()
		return super.read(sink, byteCount)
	}
}
