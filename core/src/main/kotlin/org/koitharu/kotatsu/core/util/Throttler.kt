/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : SystemClock.elapsedRealtime() remplacé par System.nanoTime() (horloge monotone JVM).
 */
package org.koitharu.kotatsu.core.util

class Throttler(
	private val timeoutMs: Long,
) {

	private var lastTick = 0L

	fun throttle(): Boolean {
		val now = System.nanoTime() / 1_000_000L
		return if (lastTick + timeoutMs <= now) {
			lastTick = now
			true
		} else {
			false
		}
	}

	fun reset() {
		lastTick = 0L
	}
}
