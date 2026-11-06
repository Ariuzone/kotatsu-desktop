/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : annotations androidx retirées, identique à l'original sinon.
 */
package org.koitharu.kotatsu.core.zip

import androidx.collection.ArraySet
import okio.Closeable
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.core.util.ext.withChildren
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipOutput(
	val file: File,
	private val compressionLevel: Int = Deflater.DEFAULT_COMPRESSION,
) : Closeable {

	private val entryNames = ArraySet<String>()
	private var cachedOutput: ZipOutputStream? = null
	private var append: Boolean = false
	// Portage desktop : java.util.zip ne sait pas ajouter à une archive existante.
	// En reprise, on réécrit l'archive (anciennes entrées copiées) vers un fichier
	// temporaire, substitué à l'original à la fermeture.
	private var pendingReplace: File? = null

	fun put(name: String, file: File): Boolean = withOutput { output ->
		output.appendFile(file, name)
	}

	fun put(name: String, content: String): Boolean = withOutput { output ->
		output.appendText(content, name)
	}

	fun addDirectory(name: String): Boolean {
		val entry = if (name.endsWith("/")) {
			ZipEntry(name)
		} else {
			ZipEntry("$name/")
		}
		return if (entryNames.add(entry.name)) {
			withOutput { output ->
				output.putNextEntry(entry)
				output.closeEntry()
			}
			true
		} else {
			false
		}
	}

	fun copyEntryFrom(other: ZipFile, entry: ZipEntry): Boolean {
		return if (entryNames.add(entry.name)) {
			val zipEntry = ZipEntry(entry.name)
			withOutput { output ->
				output.putNextEntry(zipEntry)
				try {
					other.getInputStream(entry).use { input ->
						input.copyTo(output)
					}
				} finally {
					output.closeEntry()
				}
			}
			true
		} else {
			false
		}
	}

	// Portage desktop : java.util.zip.ZipOutputStream ne permet pas de continuer à écrire
	// après finish() (contrairement à la variante libcore d'Android). On flush simplement :
	// le contrat utile (le flux reste utilisable) est conservé, la fermeture finale écrit
	// l'annuaire central.
	fun finish() = withOutput { output ->
		output.flush()
	}

	@Synchronized
	override fun close() {
		cachedOutput?.closeSafe()
		cachedOutput = null
		pendingReplace?.let { tmp ->
			pendingReplace = null
			if (tmp.isFile) {
				if (file.exists()) file.delete()
				if (!tmp.renameTo(file)) {
					tmp.copyTo(file, overwrite = true)
					tmp.delete()
				}
			}
		}
	}

	private fun ZipOutputStream.appendFile(fileToZip: File, name: String): Boolean {
		if (fileToZip.isDirectory) {
			val entry = if (name.endsWith("/")) {
				ZipEntry(name)
			} else {
				ZipEntry("$name/")
			}
			if (!entryNames.add(entry.name)) {
				return false
			}
			putNextEntry(entry)
			closeEntry()
			fileToZip.withChildren { children ->
				children.forEach { childFile ->
					appendFile(childFile, "$name/${childFile.name}")
				}
			}
		} else {
			FileInputStream(fileToZip).use { fis ->
				if (!entryNames.add(name)) {
					return false
				}
				val zipEntry = ZipEntry(name)
				putNextEntry(zipEntry)
				try {
					fis.copyTo(this)
				} finally {
					closeEntry()
				}
			}
		}
		return true
	}

	private fun ZipOutputStream.appendText(content: String, name: String): Boolean {
		if (!entryNames.add(name)) {
			return false
		}
		val zipEntry = ZipEntry(name)
		putNextEntry(zipEntry)
		try {
			content.byteInputStream().copyTo(this)
		} finally {
			closeEntry()
		}
		return true
	}

	@Synchronized
	private fun <T> withOutput(block: (ZipOutputStream) -> T): T {
		return try {
			(cachedOutput ?: newOutput(append)).withOutputImpl(block).also {
				append = true // after 1st success write
			}
		} catch (e: NullPointerException) { // probably NullPointerException: Deflater has been closed
			e.printStackTraceDebug()
			newOutput(append).withOutputImpl(block)
		}
	}

	private fun <T> ZipOutputStream.withOutputImpl(block: (ZipOutputStream) -> T): T {
		val res = block(this)
		flush()
		return res
	}

	private fun newOutput(append: Boolean): ZipOutputStream {
		val target: File = if (append && file.isFile) {
			// Reprise : réécrit l'archive existante vers un fichier temporaire
			// en conservant toutes les entrées déjà écrites (dédupliquées).
			val tmp = File(file.parentFile, file.name + ".tmp")
			if (tmp.isFile) tmp.delete()
			ZipOutputStream(FileOutputStream(tmp)).use { newStream ->
				newStream.setLevel(compressionLevel)
				ZipFile(file).use { existing ->
					for (entry in existing.entries()) {
						if (entry.isDirectory) continue
						if (!entryNames.add(entry.name)) continue
						newStream.putNextEntry(ZipEntry(entry.name))
						try {
							existing.getInputStream(entry).use { it.copyTo(newStream) }
						} finally {
							newStream.closeEntry()
						}
					}
				}
			}
			pendingReplace = tmp
			tmp
		} else {
			file
		}
		return ZipOutputStream(FileOutputStream(target, false)).also {
			it.setLevel(compressionLevel)
			cachedOutput?.closeSafe()
			cachedOutput = it
		}
	}

	private fun Closeable.closeSafe() {
		try {
			close()
		} catch (e: NullPointerException) {
			// Don't throw the "Deflater has been closed" exception
			e.printStackTraceDebug()
		}
	}
}
