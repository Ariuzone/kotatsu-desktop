/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : les fonctions dépendantes du cycle de vie Android (lifecycleScope, BroadcastReceiver)
 * sont retirées.
 */
package org.koitharu.kotatsu.core.util.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import org.koitharu.kotatsu.parsers.util.cancelAll
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import kotlin.coroutines.cancellation.CancellationException

fun <T> Deferred<T>.getCompletionResultOrNull(): Result<T>? = if (isCompleted) {
	getCompletionExceptionOrNull()?.let { error ->
		Result.failure(error)
	} ?: Result.success(getCompleted())
} else {
	null
}

fun <T> Deferred<T>.peek(): T? = if (isCompleted) {
	runCatchingCancellable {
		getCompleted()
	}.getOrNull()
} else {
	null
}

@Suppress("SuspendFunctionOnCoroutineScope")
suspend fun CoroutineScope.cancelChildrenAndJoin(cause: CancellationException? = null) {
	val jobs = coroutineContext[Job]?.children?.toList() ?: return
	jobs.cancelAll(cause)
	jobs.joinAll()
}
