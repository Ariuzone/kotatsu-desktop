/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « Sauvegarde et restauration » (équivalent desktop du BackupFragment d'origine) :
 *  - bannière « Create or restore a backup » (libellé EN conservé, non traduit dans l'original) ;
 *  - « Créer une sauvegarde » : JFileChooser (javax.swing) en mode enregistrement puis création
 *    d'un ZIP (java.util.zip) contenant kotatsu.db (+ -wal/-shm s'ils existent, copie brutale
 *    car la base Room est ouverte) et settings.json de appDir ; message de succès avec le chemin ;
 *    RECOMMANDATION : fermer l'application avant de sauvegarder/restaurer garantit la cohérence
 *    de la base (WAL inclus dans l'archive) ;
 *  - « Restaurer une sauvegarde » : JFileChooser avec filtre .zip, dialog d'avertissement
 *    (« L'application sera fermée pour restaurer la sauvegarde. Continuer ? ») puis extraction
 *    des fichiers vers appDir (écrasement, protection zip-slip) et fermeture immédiate de l'app
 *    (exitProcess(0)) — l'utilisateur relance l'application ensuite (divergence desktop) ;
 *    si le zip ne contient pas kotatsu.db → « Format de sauvegarde invalide ».
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans) — libellés déplacés vers ui/strings/FrSettings.kt.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_BANNER
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_CREATED
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_CREATED_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_FAILED
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_RESTORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_CHOOSE_OPEN_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_CHOOSE_SAVE_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_CLOSE_APP_NOTE
import org.koitharu.kotatsu.desktop.ui.strings.FR_CREATE_BACKUP
import org.koitharu.kotatsu.desktop.ui.strings.FR_CREATE_BACKUP_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_DB_REQUIRED
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_BACKUP
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_BACKUP_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_CONFIRM
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_FAILED
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_INVALID
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_WARNING
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESTORE_WARNING_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS_FILE
import org.koitharu.kotatsu.desktop.ui.strings.FR_ZIP_FILTER_DESCRIPTION
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess

// Chaînes FR de l'écran : voir ui/strings/FrSettings.kt et Fr.kt (FR_BACKUP_RESTORE, FR_OK, FR_CANCEL).

private val BANNER_HORIZONTAL_PADDING = 16.dp
private val BANNER_VERTICAL_PADDING = 12.dp
private val BANNER_CORNER_RADIUS = 16.dp
private val SCREEN_HORIZONTAL_PADDING = 16.dp
private val NOTE_ICON_SIZE = 20.dp

/**
 * Écran « Sauvegarde et restauration » : création d'un ZIP (DB + WAL + settings.json) vers un
 * chemin choisi, restauration d'un ZIP avec écrasement de appDir puis fermeture de l'app.
 */
@Composable
public fun BackupRestoreScreen(
	appDir: java.io.File,
	onBackClick: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	// État local miroir : message informatif (dialog) et sauvegarde en attente de confirmation
	var infoMessage by remember { mutableStateOf<String?>(null) }
	var infoTitle by remember { mutableStateOf(FR_BACKUP_RESTORE) }
	var pendingRestoreFile by remember { mutableStateOf<File?>(null) }
	var busy by remember { mutableStateOf(false) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_BACKUP_RESTORE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			// Bannière « Create or restore a backup » (libellé EN de l'original)
			Surface(
				modifier = Modifier
					.fillMaxWidth()
					.padding(SCREEN_HORIZONTAL_PADDING),
				shape = RoundedCornerShape(BANNER_CORNER_RADIUS),
				color = MaterialTheme.colorScheme.primaryContainer,
			) {
				Text(
					text = FR_BACKUP_BANNER,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = BANNER_HORIZONTAL_PADDING, vertical = BANNER_VERTICAL_PADDING),
				)
			}

			// « Créer une sauvegarde » : JFileChooser save puis ZIP (DB + WAL + settings)
			PreferenceRow(
				icon = Icons.Outlined.Backup,
				title = FR_CREATE_BACKUP,
				subtitle = FR_CREATE_BACKUP_SUMMARY,
				enabled = !busy,
				onClick = {
					val target = chooseSaveFile()
					if (target != null) {
						busy = true
						scope.launch {
							val message = withContext(Dispatchers.IO) {
								runCatching { createBackup(appDir, target) }
									.fold(
										onSuccess = { path -> FR_BACKUP_CREATED.format(path) },
										onFailure = { FR_BACKUP_FAILED.format(it.message ?: it.javaClass.simpleName) },
									)
							}
							infoTitle = FR_BACKUP_CREATED_TITLE
							infoMessage = message
							busy = false
						}
					}
				},
			)
			PreferenceDivider()

			// « Restaurer une sauvegarde » : JFileChooser open (.zip) puis avertissement
			PreferenceRow(
				icon = Icons.Outlined.Restore,
				title = FR_RESTORE_BACKUP,
				subtitle = FR_RESTORE_BACKUP_SUMMARY,
				enabled = !busy,
				onClick = {
					val selected = chooseZipFile()
					if (selected != null) {
						pendingRestoreFile = selected
					}
				},
			)
			PreferenceDivider()

			// Note : recommandation de fermer l'app avant sauvegarde/restauration
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(SCREEN_HORIZONTAL_PADDING),
				verticalAlignment = Alignment.Top,
			) {
				Icon(
					imageVector = Icons.Outlined.Info,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(top = 2.dp),
				)
				Text(
					text = FR_CLOSE_APP_NOTE,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(start = 12.dp),
				)
			}
		}
	}

	// Dialog d'avertissement avant restauration (l'app sera fermée)
	pendingRestoreFile?.let { zipFile ->
		AlertDialog(
			onDismissRequest = { pendingRestoreFile = null },
			title = { Text(text = FR_RESTORE_WARNING_TITLE) },
			text = { Text(text = FR_RESTORE_WARNING) },
			confirmButton = {
				TextButton(
					enabled = !busy,
					onClick = {
						pendingRestoreFile = null
						busy = true
						scope.launch {
							val message = withContext(Dispatchers.IO) {
								runCatching { restoreBackup(appDir, zipFile) }
									.fold(
										onSuccess = { null }, // succès → l'app se ferme (exitProcess)
										onFailure = { FR_RESTORE_FAILED.format(it.message ?: it.javaClass.simpleName) },
									)
							}
							busy = false
							if (message != null) {
								infoTitle = FR_RESTORE_WARNING_TITLE
								infoMessage = message
							}
						}
					},
				) {
					Text(text = FR_RESTORE_CONFIRM)
				}
			},
			dismissButton = {
				TextButton(onClick = { pendingRestoreFile = null }) {
					Text(text = FR_CANCEL)
				}
			},
		)
	}

	// Dialog informatif (succès de sauvegarde avec chemin, erreurs, format invalide)
	infoMessage?.let { message ->
		AlertDialog(
			onDismissRequest = { infoMessage = null },
			title = { Text(text = infoTitle) },
			text = { Text(text = message) },
			confirmButton = {
				TextButton(onClick = { infoMessage = null }) {
					Text(text = FR_OK)
				}
			},
		)
	}
}

/**
 * Ouvre un JFileChooser en mode enregistrement ; ajoute l'extension .zip si absente.
 * Retourne le fichier choisi, ou null si annulé.
 */
private fun chooseSaveFile(): File? = runCatching {
	val chooser = JFileChooser()
	chooser.dialogTitle = FR_CHOOSE_SAVE_TITLE
	chooser.fileSelectionMode = JFileChooser.FILES_ONLY
	val result = chooser.showSaveDialog(null)
	if (result != JFileChooser.APPROVE_OPTION) return null
	val selected = chooser.selectedFile ?: return null
	if (selected.extension.equals("zip", ignoreCase = true)) {
		selected
	} else {
		File(selected.parentFile, "${selected.name}.zip")
	}
}.getOrNull()

/**
 * Ouvre un JFileChooser avec filtre .zip ; retourne le fichier choisi, ou null si annulé.
 */
private fun chooseZipFile(): File? = runCatching {
	val chooser = JFileChooser()
	chooser.dialogTitle = FR_CHOOSE_OPEN_TITLE
	chooser.fileSelectionMode = JFileChooser.FILES_ONLY
	chooser.fileFilter = FileNameExtensionFilter(FR_ZIP_FILTER_DESCRIPTION, "zip")
	chooser.isAcceptAllFileFilterUsed = false
	val result = chooser.showOpenDialog(null)
	if (result != JFileChooser.APPROVE_OPTION) return null
	chooser.selectedFile
}.getOrNull()

/**
 * Crée le ZIP de sauvegarde : kotatsu.db (copie brute, la base Room est ouverte), kotatsu.db-wal
 * et kotatsu.db-shm s'ils existent (WAL inclus pour la cohérence), settings.json s'il existe.
 * Retourne le chemin du fichier créé.
 */
private fun createBackup(appDir: File, target: File): String {
	target.parentFile?.mkdirs()
	ZipOutputStream(FileOutputStream(target)).use { zip ->
		addToZip(zip, appDir, FR_DB_REQUIRED)
		addToZip(zip, appDir, "$FR_DB_REQUIRED-wal")
		addToZip(zip, appDir, "$FR_DB_REQUIRED-shm")
		addToZip(zip, appDir, FR_SETTINGS_FILE)
	}
	return target.absolutePath
}

/** Ajoute un fichier de appDir au ZIP s'il existe (entrée à la racine de l'archive). */
private fun addToZip(zip: ZipOutputStream, appDir: File, name: String) {
	val file = File(appDir, name)
	if (!file.exists()) return
	zip.putNextEntry(ZipEntry(name))
	file.inputStream().use { input -> input.copyTo(zip) }
	zip.closeEntry()
}

/**
 * Valide et restaure le ZIP vers appDir (écrasement). Le zip doit contenir kotatsu.db sinon
 * une IllegalStateException (« Format de sauvegarde invalide ») est levée. Les chemins des
 * entrées sont vérifiés (protection zip-slip). En cas de succès, ferme l'application
 * (exitProcess(0)) : l'utilisateur relance l'application pour retrouver les données restaurées.
 */
private fun restoreBackup(appDir: File, zipFile: File) {
	appDir.mkdirs()
	ZipFile(zipFile).use { zip ->
		val entries = zip.entries().asSequence().toList()
		// Le zip doit contenir la base de données, sinon format invalide
		if (entries.none { it.name == FR_DB_REQUIRED }) {
			error(FR_RESTORE_INVALID)
		}
		val appDirCanonical = appDir.canonicalPath
		entries.filterNot { it.isDirectory }.forEach { entry ->
			val target = File(appDir, entry.name)
			if (!target.canonicalPath.startsWith(appDirCanonical + File.separator) &&
				target.canonicalPath != appDirCanonical
			) {
				error(FR_RESTORE_INVALID) // entrée hors du dossier cible (zip-slip)
			}
			target.parentFile?.mkdirs()
			zip.getInputStream(entry).use { input ->
				target.outputStream().use { output -> input.copyTo(output) }
			}
		}
	}
	// Restauration terminée : fermeture immédiate (divergence desktop documentée)
	exitProcess(0)
}
