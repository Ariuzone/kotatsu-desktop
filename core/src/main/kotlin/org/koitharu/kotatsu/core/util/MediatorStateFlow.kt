/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.util

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger

abstract class MediatorStateFlow<T>(initialValue: T) : StateFlow<T> {

	private val delegate = MutableStateFlow(initialValue)
	private val collectors = AtomicInteger(0)

	final override val replayCache: List<T>
		get() = delegate.replayCache

	override val value: T
		get() = delegate.value

	final override suspend fun collect(collector: FlowCollector<T>): Nothing {
		try {
			if (collectors.getAndIncrement() == 0) {
				onActive()
			}
			delegate.collect(collector)
		} finally {
			if (collectors.decrementAndGet() == 0) {
				onInactive()
			}
		}
	}

	protected fun publishValue(v: T) {
		delegate.value = v
	}

	protected abstract fun onActive()

	protected abstract fun onInactive()
}
