/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : SAF/Context/StatFs retirés — les dossiers de stockage manga sont fournis
 * explicitement par l'hôte desktop (dossier par défaut + dossiers supplémentaires).
 */
package org.koitharu.kotatsu.local.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.koitharu.kotatsu.core.util.ext.isReadable
import org.koitharu.kotatsu.core.util.ext.isWriteable
import org.koitharu.kotatsu.core.util.ext.takeIfWriteable
import java.io.File

class LocalStorageManager(
	defaultDir: File?,
	extraDirs: Collection<File> = emptySet(),
) {

	private val defaultDir: File? = defaultDir?.takeIf { it.exists() || it.mkdirs() }
	private val storageDirs: Set<File> = buildSet {
		defaultDir?.let(::add)
		addAll(extraDirs)
	}.filter { it.exists() || it.mkdirs() }.toSet()

	suspend fun getReadableDirs(): List<File> = runInterruptible(Dispatchers.IO) {
		storageDirs.filter { it.isReadable() }
	}

	suspend fun getWriteableDirs(): List<File> = runInterruptible(Dispatchers.IO) {
		storageDirs.filter { it.isWriteable() }
	}

	suspend fun getDefaultWriteableDir(): File? = runInterruptible(Dispatchers.IO) {
		defaultDir?.takeIfWriteable()
	}
}
