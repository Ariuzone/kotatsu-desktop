/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : les fonctions dépendantes d'Android (Context, Uri, ContentResolver, Environment,
 * Build.VERSION) sont retirées ; la branche StreamImpl (java.nio) est utilisée sans garde de version.
 */
package org.koitharu.kotatsu.core.util.ext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.jetbrains.annotations.Blocking
import org.koitharu.kotatsu.core.fs.FileSequence
import org.koitharu.kotatsu.core.util.MimeTypes
import java.io.BufferedReader
import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.readAttributes
import kotlin.io.path.walk

fun File.subdir(name: String) = File(this, name).also {
	if (!it.exists()) it.mkdirs()
}

fun File.takeIfReadable() = takeIf { it.isReadable() }

fun File.takeIfWriteable() = takeIf { it.isWriteable() }

fun File.isNotEmpty() = length() != 0L

@Blocking
fun ZipFile.readText(entry: ZipEntry) = getInputStream(entry).use { output ->
	output.bufferedReader().use(BufferedReader::readText)
}

suspend fun File.deleteAwait() = runInterruptible(Dispatchers.IO) {
	delete() || deleteRecursively()
}

suspend fun File.computeSize(): Long = runInterruptible(Dispatchers.IO) {
	walkCompat(includeDirectories = false).sumOf { it.length() }
}

inline fun <R> File.withChildren(block: (children: Sequence<File>) -> R): R = FileSequence(this).use(block)

fun FileSequence(dir: File): FileSequence = FileSequence.StreamImpl(dir)

val File.creationTime
	get() = toPath().readAttributes<BasicFileAttributes>().creationTime().toMillis()

@OptIn(ExperimentalPathApi::class)
fun File.walkCompat(includeDirectories: Boolean): Sequence<File> {
	val walk = if (includeDirectories) {
		toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES)
	} else {
		toPath().walk()
	}
	return walk.map { it.toFile() }
}

val File.normalizedExtension: String?
	get() = MimeTypes.getNormalizedExtension(name)

fun File.isReadable() = runCatching {
	canRead()
}.getOrDefault(false)

fun File.isWriteable() = runCatching {
	canWrite()
}.getOrDefault(false)
