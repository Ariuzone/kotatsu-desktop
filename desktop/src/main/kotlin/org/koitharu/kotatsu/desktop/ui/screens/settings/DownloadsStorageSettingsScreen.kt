/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « Téléchargements et stockage » (Settings > Téléchargements et stockage de l'app d'origine) :
 *  - ligne « Dossier pour les téléchargements » avec le chemin réel (appDir/local) ; le clic ouvre
 *    un JFileChooser proposant CE dossier. Divergence desktop : contrairement à Android, changer
 *    le dossier n'est pas appliqué automatiquement — un dialog explique que le déplacement des
 *    téléchargements se fera manuellement (le dossier de stockage n'est pas reconfigurable à
 *    chaud dans ce portage) ;
 *  - entrée « Ouvrir le dossier » (java.awt.Desktop.open si supporté, sinon message de secours) ;
 *  - ligne « Utilisation du stockage » avec la taille totale (base de données + local +
 *    okhttp-cache) calculée en tâche de fond ;
 *  - ligne « Data removal » (« Effacer les données inutiles ») ;
 *  - section « Create or restore a backup » (libellé EN conservé, non traduit dans l'original)
 *    avec la ligne « Sauvegardes ».
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans) — libellés déplacés vers ui/strings/FrSettings.kt.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_BANNER
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUPS
import org.koitharu.kotatsu.desktop.ui.strings.FR_DATA_REMOVAL
import org.koitharu.kotatsu.desktop.ui.strings.FR_DATA_REMOVAL_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_DIR_CHANGED_MESSAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_DIR_CHANGED_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_DIR
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_STORAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_ELLIPSIS
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_OPEN_DIR
import org.koitharu.kotatsu.desktop.ui.strings.FR_OPEN_DIR_FAILED
import org.koitharu.kotatsu.desktop.ui.strings.FR_SAME_DIR_MESSAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_STORAGE_USAGE
import org.koitharu.kotatsu.desktop.ui.strings.frFileSize
import java.io.File
import java.io.IOException
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

// Chaînes FR de l'écran : voir ui/strings/FrSettings.kt (FR_DOWNLOADS_STORAGE, FR_OK viennent de Fr.kt).

/**
 * Écran « Téléchargements et stockage » : dossier de téléchargements (choix via JFileChooser,
 * divergence desktop documentée), ouverture du dossier, utilisation du stockage (taille calculée
 * en tâche de fond), effacement des données et accès à la sauvegarde/restauration.
 */
@Composable
public fun DownloadsStorageSettingsScreen(
	appDir: java.io.File,
	onBackClick: () -> Unit,
	onOpenUsage: () -> Unit,
	onOpenDataRemoval: () -> Unit,
	onOpenBackup: () -> Unit,
) {
	val downloadsDir = remember(appDir) { File(appDir, "local") }

	// Taille totale (DB + local + okhttp-cache) calculée en tâche de fond, état miroir local
	var totalSizeText by remember { mutableStateOf<String?>(null) }
	LaunchedEffect(appDir) {
		val bytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
			dataBaseSize(appDir) + directorySize(downloadsDir) + directorySize(File(appDir, "okhttp-cache"))
		}
		totalSizeText = frFileSize(bytes)
	}

	// Dialog informatif affiché après le choix d'un dossier différent
	var dirMessage by remember { mutableStateOf<String?>(null) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_DOWNLOADS_STORAGE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			PreferenceCategoryHeader(title = FR_DOWNLOADS_STORAGE)

			// « Dossier pour les téléchargements » : chemin réel, JFileChooser proposant CE dossier
			PreferenceRow(
				icon = Icons.Outlined.Folder,
				title = FR_DOWNLOADS_DIR,
				subtitle = downloadsDir.absolutePath,
				onClick = { dirMessage = chooseDownloadsDir(downloadsDir) },
			)
			PreferenceDivider()

			// « Ouvrir le dossier » : java.awt.Desktop.open si supporté, sinon message de secours
			PreferenceRow(
				icon = Icons.Outlined.OpenInNew,
				title = FR_OPEN_DIR,
				subtitle = null,
				onClick = {
					dirMessage = openDirectory(downloadsDir)
				},
			)
			PreferenceDivider()

			// « Utilisation du stockage » : taille totale lisible, calculée en arrière-plan
			PreferenceRow(
				icon = Icons.Outlined.Storage,
				title = FR_STORAGE_USAGE,
				subtitle = totalSizeText ?: FR_ELLIPSIS,
				onClick = onOpenUsage,
			)
			PreferenceDivider()

			// « Data removal » : écran d'effacement des données inutiles
			PreferenceRow(
				icon = Icons.Outlined.DeleteSweep,
				title = FR_DATA_REMOVAL,
				subtitle = FR_DATA_REMOVAL_SUMMARY,
				onClick = onOpenDataRemoval,
			)
			PreferenceDivider()

			// Section sauvegarde : libellé EN non traduit dans l'app d'origine (libellé vidéo)
			PreferenceCategoryHeader(title = FR_BACKUP_BANNER)

			// Ligne « Sauvegardes » (simple info, ouvre l'écran de sauvegarde/restauration)
			PreferenceRow(
				icon = Icons.Outlined.SettingsBackupRestore,
				title = FR_BACKUPS,
				subtitle = null,
				onClick = onOpenBackup,
			)
		}
	}

	// Message informatif (dialog) après un changement de dossier ou une ouverture ratée
	dirMessage?.let { message ->
		AlertDialog(
			onDismissRequest = { dirMessage = null },
			title = { Text(text = FR_DIR_CHANGED_TITLE) },
			text = { Text(text = message) },
			confirmButton = {
				TextButton(onClick = { dirMessage = null }) {
					Text(text = FR_OK)
				}
			},
		)
	}
}

/**
 * Ouvre un JFileChooser (javax.swing) proposant le dossier de téléchargements actuel.
 * Retourne un message à afficher si l'utilisateur a choisi un dossier différent (divergence
 * desktop : le déplacement des téléchargements est manuel), sinon null.
 */
private fun chooseDownloadsDir(currentDir: File): String? {
	return runCatching {
		val chooser = JFileChooser(FileSystemView.getFileSystemView().createFileObject(currentDir.absolutePath))
		chooser.dialogTitle = FR_DOWNLOADS_DIR
		chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		chooser.isMultiSelectionEnabled = false
		val result = chooser.showOpenDialog(null)
		if (result != JFileChooser.APPROVE_OPTION) return null
		val selected = chooser.selectedFile ?: return null
		if (selected.absolutePath.equals(currentDir.absolutePath, ignoreCase = true)) {
			FR_SAME_DIR_MESSAGE
		} else {
			FR_DIR_CHANGED_MESSAGE
		}
	}.getOrNull()
}

/**
 * Ouvre le dossier dans l'explorateur Windows via java.awt.Desktop.open.
 * Retourne un message d'échec si Desktop n'est pas supporté, sinon null.
 */
private fun openDirectory(dir: File): String? {
	if (!java.awt.Desktop.isDesktopSupported()) {
		return FR_OPEN_DIR_FAILED
	}
	return runCatching {
		if (!dir.exists()) {
			dir.mkdirs()
		}
		java.awt.Desktop.getDesktop().open(dir)
		null
	}.recover { _ ->
		FR_OPEN_DIR_FAILED
	}.getOrNull()
}

/** Taille récursive d'un dossier (octets), 0 si absent. */
private fun directorySize(dir: File): Long {
	if (!dir.exists()) return 0L
	return dir.walkBottomUp().filter { it.isFile }.fold(0L) { acc, file -> acc + file.length() }
}

/** Taille des fichiers de base de données (kotatsu.db + -wal/-shm). */
private fun dataBaseSize(appDir: File): Long {
	var total = 0L
	val db = File(appDir, "kotatsu.db")
	if (db.exists()) total += db.length()
	val wal = File(appDir, "kotatsu.db-wal")
	if (wal.exists()) total += wal.length()
	val shm = File(appDir, "kotatsu.db-shm")
	if (shm.exists()) total += shm.length()
	return total
}
