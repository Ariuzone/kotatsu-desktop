/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.util

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ContinuationResumeRunnable(
	private val continuation: Continuation<Unit>,
) : Runnable {

	override fun run() {
		continuation.resume(Unit)
	}
}
