/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « Effacement des données » (Data removal, cf. Settings > Téléchargements et stockage) :
 * liste d'actions avec dialog de confirmation « Êtes-vous sûr… ? » chacune :
 *  - « Effacer l'historique de lecture » : HistoryDao.clear() existe (suppression logique via
 *    deleted_at) → câblé via AppContainer.getInstance().database.getHistoryDao() ;
 *  - « Effacer les suggestions » : SuggestionDao.deleteAll() existe → câblé ;
 *  - « Vider le cache réseau » : suppression du contenu du dossier okhttp-cache/ ;
 *  - « Effacer les cookies » : suppression de cookies.json (fichier KeyValueStore du réseau).
 * Chaque exécution est protégée par runCatching dans un rememberCoroutineScope (Dispatchers.IO)
 * et le résultat est affiché comme un message temporaire (équivalent snackbar desktop).
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.AppContainer
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL
import java.io.File

// Chaînes FR de l'écran
private const val FR_TITLE = "Effacement des données"
private const val FR_CLEAR_HISTORY = "Effacer l'historique de lecture"
private const val FR_CLEAR_SUGGESTIONS = "Effacer les suggestions"
private const val FR_CLEAR_NETWORK_CACHE = "Vider le cache réseau"
private const val FR_CLEAR_COOKIES = "Effacer les cookies"
private const val FR_CONFIRM_TITLE = "Êtes-vous sûr ?"
private const val FR_CONFIRM_HISTORY =
	"Êtes-vous sûr de vouloir effacer tout l'historique de lecture ? Cette action est irréversible."
private const val FR_CONFIRM_SUGGESTIONS =
	"Êtes-vous sûr de vouloir effacer toutes les suggestions ? Cette action est irréversible."
private const val FR_CONFIRM_NETWORK_CACHE =
	"Êtes-vous sûr de vouloir vider le cache réseau ? Les pages déjà chargées seront retéléchargées."
private const val FR_CONFIRM_COOKIES =
	"Êtes-vous sûr de vouloir effacer les cookies ? Vous serez déconnecté des sources."
private const val FR_DONE_HISTORY = "Historique de lecture effacé"
private const val FR_DONE_SUGGESTIONS = "Suggestions effacées"
private const val FR_DONE_NETWORK_CACHE = "Cache réseau vidé"
private const val FR_DONE_COOKIES = "Cookies effacés"
private const val FR_ERROR = "Une erreur est survenue : %s"
private const val FR_DELETE = "Effacer"

private const val FEEDBACK_DURATION_MS = 4000L
private val SCREEN_HORIZONTAL_PADDING = 16.dp
private val FEEDBACK_CORNER_RADIUS = 12.dp

/**
 * Écran « Effacement des données » : chaque action demande une confirmation puis s'exécute en
 * tâche de fond (runCatching) ; le résultat est affiché sous la liste, façon snackbar.
 */
@Composable
public fun DataRemovalScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	// Action en attente de confirmation (null = aucun dialog) ; état miroir local
	var pendingAction by remember { mutableStateOf<RemovalAction?>(null) }
	// Message temporaire façon snackbar (résultat de l'action)
	var feedback by remember { mutableStateOf<String?>(null) }
	var busy by remember { mutableStateOf(false) }

	// Effacement automatique du message temporaire
	LaunchedEffect(feedback) {
		if (feedback != null) {
			delay(FEEDBACK_DURATION_MS)
			feedback = null
		}
	}

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_TITLE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Box(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
			) {
				PreferenceRow(
					icon = Icons.Outlined.History,
					title = FR_CLEAR_HISTORY,
					subtitle = null,
					enabled = !busy,
					onClick = { pendingAction = RemovalAction.HISTORY },
				)
				PreferenceDivider()
				PreferenceRow(
					icon = Icons.Outlined.Lightbulb,
					title = FR_CLEAR_SUGGESTIONS,
					subtitle = null,
					enabled = !busy,
					onClick = { pendingAction = RemovalAction.SUGGESTIONS },
				)
				PreferenceDivider()
				PreferenceRow(
					icon = Icons.Outlined.DeleteSweep,
					title = FR_CLEAR_NETWORK_CACHE,
					subtitle = null,
					enabled = !busy,
					onClick = { pendingAction = RemovalAction.NETWORK_CACHE },
				)
				PreferenceDivider()
				PreferenceRow(
					icon = Icons.Outlined.Cookie,
					title = FR_CLEAR_COOKIES,
					subtitle = null,
					enabled = !busy,
					onClick = { pendingAction = RemovalAction.COOKIES },
				)
			}

			// « Snackbar » desktop : message temporaire en bas d'écran
			feedback?.let { message ->
				Surface(
					shape = RoundedCornerShape(FEEDBACK_CORNER_RADIUS),
					color = MaterialTheme.colorScheme.inverseSurface,
					modifier = Modifier
						.align(androidx.compose.ui.Alignment.BottomCenter)
						.fillMaxWidth()
						.padding(SCREEN_HORIZONTAL_PADDING),
				) {
					Text(
						text = message,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.inverseOnSurface,
						modifier = Modifier.padding(SCREEN_HORIZONTAL_PADDING),
					)
				}
			}
		}
	}

	// Dialog de confirmation de l'action en attente
	pendingAction?.let { action ->
		AlertDialog(
			onDismissRequest = { pendingAction = null },
			title = { Text(text = FR_CONFIRM_TITLE) },
			text = { Text(text = action.confirmation) },
			confirmButton = {
				TextButton(
					enabled = !busy,
					onClick = {
						pendingAction = null
						busy = true
						scope.launch {
							feedback = withContext(Dispatchers.IO) {
								runCatching { action.execute() }
									.fold(
										onSuccess = { it },
										onFailure = { FR_ERROR.format(it.message ?: it.javaClass.simpleName) },
									)
							}
							busy = false
						}
					},
				) {
					Text(text = FR_DELETE)
				}
			},
			dismissButton = {
				TextButton(onClick = { pendingAction = null }) {
					Text(text = FR_CANCEL)
				}
			},
		)
	}
}

/** Actions d'effacement disponibles, avec confirmation et exécution câblée. */
private enum class RemovalAction(
	val confirmation: String,
) {

	HISTORY(FR_CONFIRM_HISTORY) {
		// HistoryDao.clear() existe (suppression logique deleted_at) : câblé sur la DB Room
		override suspend fun execute(): String {
			AppContainer.getInstance().database.getHistoryDao().clear()
			return FR_DONE_HISTORY
		}
	},
	SUGGESTIONS(FR_CONFIRM_SUGGESTIONS) {
		// SuggestionDao.deleteAll() existe : câblé
		override suspend fun execute(): String {
			AppContainer.getInstance().database.getSuggestionDao().deleteAll()
			return FR_DONE_SUGGESTIONS
		}
	},
	NETWORK_CACHE(FR_CONFIRM_NETWORK_CACHE) {
		// Suppression du contenu du dossier okhttp-cache de l'app (%APPDATA%/Kotatsu/okhttp-cache)
		override suspend fun execute(): String {
			val appDir = appDirectory()
			deleteChildren(File(appDir, "okhttp-cache"))
			return FR_DONE_NETWORK_CACHE
		}
	},
	COOKIES(FR_CONFIRM_COOKIES) {
		// cookies.json : KeyValueStore du cookieJar (NetworkModule)
		override suspend fun execute(): String {
			val file = File(appDirectory(), "cookies.json")
			if (file.exists()) {
				file.delete()
			}
			return FR_DONE_COOKIES
		}
	}, ;

	/** Exécute l'effacement ; appelé en tâche de fond, protégé par runCatching. */
	abstract suspend fun execute(): String

	/** Dossier de données de l'app (même résolution que AppContainer.defaultAppDir). */
	protected fun appDirectory(): File {
		val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
		return File(appData, "Kotatsu")
	}
}

/** Supprime le contenu d'un dossier (sans supprimer le dossier lui-même). */
private fun deleteChildren(dir: File) {
	if (!dir.exists()) return
	dir.listFiles()?.forEach { child ->
		if (child.isDirectory) {
			child.deleteRecursively()
		} else {
			child.delete()
		}
	}
}
