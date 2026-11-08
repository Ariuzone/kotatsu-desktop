/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « Utilisation du stockage » (depuis « Téléchargements et stockage ») : tailles réelles par
 * catégorie — « Base de données » (kotatsu.db + -wal/-shm), « Téléchargements » (dossier local/),
 * « Cache réseau » (okhttp-cache/) et « Total » — calculées en tâche de fond (Dispatchers.IO)
 * et affichées en Mo/Go avec une barre de progression proportionnelle sobre M3.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import java.io.File

// Chaînes FR de l'écran
private const val FR_TITLE = "Utilisation du stockage"
private const val FR_DATABASE = "Base de données"
private const val FR_DOWNLOADS = "Téléchargements"
private const val FR_NETWORK_CACHE = "Cache réseau"
private const val FR_TOTAL = "Total"
private const val FR_COMPUTING = "Calcul…"

private val ROW_HORIZONTAL_PADDING = 24.dp
private val ROW_VERTICAL_PADDING = 12.dp

/** Répartition d'espace disque par catégorie, en octets. */
private data class StorageUsage(
	val database: Long,
	val downloads: Long,
	val networkCache: Long,
) {
	val total: Long get() = database + downloads + networkCache
}

/**
 * Écran « Utilisation du stockage » : tailles réelles par catégorie calculées en arrière-plan,
 * avec une barre proportionnelle par ligne (rendu sobre M3, équivalent du StorageUsage d'origine).
 */
@Composable
public fun StorageUsageScreen(
	appDir: java.io.File,
	onBackClick: () -> Unit,
) {
	// État miroir : usage calculé en tâche de fond (null tant que le calcul est en cours)
	var usage by remember { mutableStateOf<StorageUsage?>(null) }
	LaunchedEffect(appDir) {
		usage = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
			StorageUsage(
				database = databaseFilesSize(appDir),
				downloads = directorySize(File(appDir, "local")),
				networkCache = directorySize(File(appDir, "okhttp-cache")),
			)
		}
	}

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_TITLE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			val current = usage
			if (current == null) {
				Text(
					text = FR_COMPUTING,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(ROW_HORIZONTAL_PADDING),
				)
			} else {
				UsageRow(
					title = FR_DATABASE,
					bytes = current.database,
					total = current.total,
				)
				UsageRow(
					title = FR_DOWNLOADS,
					bytes = current.downloads,
					total = current.total,
				)
				UsageRow(
					title = FR_NETWORK_CACHE,
					bytes = current.networkCache,
					total = current.total,
				)
				UsageRow(
					title = FR_TOTAL,
					bytes = current.total,
					total = current.total,
				)
			}
		}
	}
}

/** Ligne d'usage : libellé + taille à droite + barre de progression proportionnelle au total. */
@Composable
private fun UsageRow(
	title: String,
	bytes: Long,
	total: Long,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.weight(1f),
			)
			Text(
				text = formatFileSize(bytes),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
		if (total > 0) {
			LinearProgressIndicator(
				progress = { (bytes.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 6.dp),
			)
		}
	}
}

/** Taille récursive d'un dossier (octets), 0 si absent. */
private fun directorySize(dir: File): Long {
	if (!dir.exists()) return 0L
	return dir.walkBottomUp().filter { it.isFile }.fold(0L) { acc, file -> acc + file.length() }
}

/** Taille des fichiers de base de données (kotatsu.db + -wal/-shm). */
private fun databaseFilesSize(appDir: File): Long {
	var total = 0L
	for (name in listOf("kotatsu.db", "kotatsu.db-wal", "kotatsu.db-shm")) {
		val file = File(appDir, name)
		if (file.exists()) total += file.length()
	}
	return total
}

/** Taille lisible en Ko/Mo/Go (chaînes FR locales à l'écran). */
private fun formatFileSize(bytes: Long): String {
	return when {
		bytes >= 1_000_000_000L -> String.format("%.1f Go", bytes / 1_000_000_000.0)
		bytes >= 1_000_000L -> String.format("%.1f Mo", bytes / 1_000_000.0)
		bytes >= 1_000L -> String.format("%.0f Ko", bytes / 1_000.0)
		else -> String.format("%d o", bytes)
	}
}
