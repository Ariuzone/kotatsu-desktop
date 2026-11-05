/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * NB : androidx.collection.SieveCache est multiplateforme (artifact androidx.collection 1.5), conservé tel quel.
 */
package org.koitharu.kotatsu.core.util

import androidx.collection.SieveCache

class SynchronizedSieveCache<K : Any, V : Any>(
	private val delegate: SieveCache<K, V>,
) {

	constructor(maxSize: Int) : this(SieveCache<K, V>(maxSize))

	private val lock = Any()

	operator fun get(key: K): V? = synchronized(lock) {
		delegate[key]
	}

	fun put(key: K, value: V): V? = synchronized(lock) {
		delegate.put(key, value)
	}

	fun remove(key: K) = synchronized(lock) {
		delegate.remove(key)
	}

	fun evictAll() = synchronized(lock) {
		delegate.evictAll()
	}

	fun trimToSize(maxSize: Int) = synchronized(lock) {
		delegate.trimToSize(maxSize)
	}

	fun removeIf(predicate: (K, V) -> Boolean) = synchronized(lock) {
		delegate.removeIf(predicate)
	}
}
