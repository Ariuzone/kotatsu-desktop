/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.util.iterator

class MappingIterator<T, R>(
	private val upstream: Iterator<T>,
	private val mapper: (T) -> R,
) : Iterator<R> {

	override fun hasNext(): Boolean = upstream.hasNext()

	override fun next(): R = mapper(upstream.next())
}
