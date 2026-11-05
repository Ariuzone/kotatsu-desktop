/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : la branche StreamImpl (java.nio, disponible sur toute JVM) est conservée telle quelle ;
 * la garde Build.VERSION est retirée.
 */
package org.koitharu.kotatsu.core.fs

import org.koitharu.kotatsu.core.util.CloseableSequence
import org.koitharu.kotatsu.core.util.iterator.MappingIterator
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

sealed interface FileSequence : CloseableSequence<File> {

	class StreamImpl(dir: File) : FileSequence {

		private val stream = Files.newDirectoryStream(dir.toPath())

		override fun iterator(): Iterator<File> = MappingIterator(stream.iterator(), Path::toFile)

		override fun close() = stream.close()
	}

	class ListImpl(dir: File) : FileSequence {

		private val list = dir.listFiles().orEmpty()

		override fun iterator(): Iterator<File> = list.iterator()

		override fun close() = Unit
	}
}
