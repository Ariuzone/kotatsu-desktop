/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : annotations androidx retirées ; context element conservé tel quel
 * (remplace les broadcasts PausingReceiver de l'app Android).
 */
package org.koitharu.kotatsu.download.domain

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class PausingHandle : AbstractCoroutineContextElement(PausingHandle) {

	private val paused = MutableStateFlow(false)
	private val skipError = MutableStateFlow(false)

	@Volatile
	private var skipAllErrors = false

	val isPaused: Boolean
		get() = paused.value

	suspend fun awaitResumed() {
		paused.first { !it }
	}

	fun pause() {
		paused.value = true
	}

	fun resume() {
		skipError.value = false
		paused.value = false
	}

	fun skip() {
		skipError.value = true
		paused.value = false
	}

	fun skipAll() {
		skipAllErrors = true
		skip()
	}

	suspend fun yield() {
		if (paused.value) {
			paused.first { !it }
		}
	}

	fun skipAllErrors(): Boolean = skipAllErrors

	fun skipCurrentError(): Boolean = skipError.compareAndSet(expect = true, update = false)

	companion object : CoroutineContext.Key<PausingHandle> {

		suspend fun current() = checkNotNull(currentCoroutineContext()[this]) {
			"PausingHandle not found in current context"
		}
	}
}
