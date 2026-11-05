/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.util.progress

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ProgressDeferred<T, P>(
	private val deferred: Deferred<T>,
	private val progress: StateFlow<P>,
) : Deferred<T> by deferred {

	val progressValue: P
		get() = progress.value

	fun progressAsFlow(): Flow<P> = progress
}
