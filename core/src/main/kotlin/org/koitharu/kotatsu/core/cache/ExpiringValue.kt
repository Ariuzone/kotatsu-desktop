/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : SystemClock.elapsedRealtime() remplacé par System.nanoTime() (horloge monotone JVM).
 */
package org.koitharu.kotatsu.core.cache

import java.util.concurrent.TimeUnit

class ExpiringValue<T>(
	private val value: T,
	lifetime: Long,
	timeUnit: TimeUnit,
) {

	private val expiresAt = System.nanoTime() / 1_000_000L + timeUnit.toMillis(lifetime)

	val isExpired: Boolean
		get() = System.nanoTime() / 1_000_000L >= expiresAt

	fun get(): T? = if (isExpired) null else value

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ExpiringValue<*>

		if (value != other.value) return false
		return expiresAt == other.expiresAt
	}

	override fun hashCode(): Int {
		var result = value?.hashCode() ?: 0
		result = 31 * result + expiresAt.hashCode()
		return result
	}
}
