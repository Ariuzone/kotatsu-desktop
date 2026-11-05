/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.cache

import kotlinx.coroutines.Deferred

class SafeDeferred<T>(
	private val delegate: Deferred<Result<T>>,
) {

	suspend fun await(): T {
		return delegate.await().getOrThrow()
	}

	suspend fun awaitOrNull(): T? {
		return delegate.await().getOrNull()
	}

	fun cancel() {
		delegate.cancel()
	}
}
