/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P4 : écran
 * « Nouveaux chapitres » (Paramètres > « Trouver des mangas » > Nouveaux chapitres), reproduit
 * de la vidéo (video-analysis/half1-inventory.md, frames t≈3:05-3:20) :
 *   - interrupteur « Vérifier les nouveaux chapitres et les notifier » (KEY_TRACKER_ENABLED) ;
 *   - « Fréquence de vérification » (dialog radio : Par défaut / Toutes les heures /
 *     Toutes les 6 heures / Quotidienne, stocké dans trackerFrequency — l'app Android
 *     n'expose pas clairement ses valeurs dans la vidéo, choix desktop documenté en KDoc) ;
 *   - « Rechercher les mises à jour » (callback onUpdateNow, à brancher sur TrackerScheduler
 *     par l'intégrateur) ;
 *   - section « Favoris » > « Catégories favorites » (dialog multi-choix, persisté dans
 *     trackerFavouriteCategories — ensemble vide = toutes activées, comme l'original) ;
 *   - « Télécharger de nouveaux chapitres » (dialog radio sur l'enum TrackerDownloadStrategy) ;
 *   - section « Dépannage » : « Vérification du journal des nouveaux chapitres » (onOpenLog)
 *     et « Désactiver l'optimisation de la batterie » (entrée grisée : concept Android sans
 *     équivalent desktop — divergence documentée).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans) — libellés déplacés vers ui/strings/FrSettings.kt.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.TrackerDownloadStrategy
import org.koitharu.kotatsu.desktop.ui.components.preferences.MultiChoiceDialog
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_BATTERY_OPTIMIZATION
import org.koitharu.kotatsu.desktop.ui.strings.FR_BATTERY_OPTIMIZATION_SUBTITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_CATEGORIES_EMPTY
import org.koitharu.kotatsu.desktop.ui.strings.FR_CHECK_UPDATES
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOAD_DISABLED
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOAD_DOWNLOADED
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOAD_NEW
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOAD_RECENTLY_READ
import org.koitharu.kotatsu.desktop.ui.strings.FR_FEED_LOG
import org.koitharu.kotatsu.desktop.ui.strings.FR_FEED_LOG_SUBTITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_FREQUENCY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FREQUENCY_DEFAULT
import org.koitharu.kotatsu.desktop.ui.strings.FR_FREQUENCY_DAILY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FREQUENCY_EVERY6H
import org.koitharu.kotatsu.desktop.ui.strings.FR_FREQUENCY_HOURLY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES_CATEGORIES
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_CHAPTERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_SECTION_TROUBLESHOOTING
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKER_SWITCH
import org.koitharu.kotatsu.desktop.ui.strings.frSourcesEnabled

// Chaînes FR de l'écran : voir ui/strings/FrSettings.kt ; FR_NEW_CHAPTERS, FR_FAVOURITES,
// FR_FAVOURITES_CATEGORIES, FR_OK et frSourcesEnabled viennent de Fr.kt.

/**
 * Valeurs de fréquence stockées dans [AppSettings.trackerFrequency] (P4, clé
 * KEY_TRACKER_FREQUENCY). L'app Android n'expose pas clairement ses valeurs dans la vidéo :
 * le desktop retient DEFAULT / HOURLY / EVERY6H / DAILY. À noter : [org.koitharu.kotatsu.desktop.tracker.TrackerScheduler]
 * lit aujourd'hui une autre clé (`tracker_freq`, facteur numérique) — le branchement de cette
 * préférence sur le planificateur est à la charge de l'intégrateur principal.
 */
private data class FrequencyOption(val value: String, val label: String)

private val FREQUENCY_OPTIONS = listOf(
	FrequencyOption("DEFAULT", FR_FREQUENCY_DEFAULT),
	FrequencyOption("HOURLY", FR_FREQUENCY_HOURLY),
	FrequencyOption("EVERY6H", FR_FREQUENCY_EVERY6H),
	FrequencyOption("DAILY", FR_FREQUENCY_DAILY),
)

/** Dialogs ouverts sur l'écran. */
private enum class FeedDialog {
	FREQUENCY,
	DOWNLOAD,
	CATEGORIES,
}

/**
 * Écran « Nouveaux chapitres » (Paramètres > « Trouver des mangas » > Nouveaux chapitres).
 *
 * [favouriteCategories] : libellés des catégories favorites à proposer dans le dialog
 * multi-choix (liste vide → sous-titre « 0 de 0 activé(s) » et dialog à état vide explicatif).
 * [onUpdateNow] : lance une vérification immédiate (à brancher sur TrackerScheduler.startNow()
 * par l'intégrateur principal). [onOpenLog] : ouvre [FeedLogScreen] (journal de vérification).
 *
 * Divergence desktop : « Désactiver l'optimisation de la batterie » est affichée mais
 * désactivée (grisée) — l'optimisation de batterie Android n'a pas d'équivalent desktop.
 */
@Composable
public fun FeedSettingsScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
	favouriteCategories: List<String> = emptyList(),
	onUpdateNow: () -> Unit = {},
	onOpenLog: () -> Unit = {},
) {
	// États locaux reflétés dans le store (FileKeyValueStore n'est pas observable)
	var trackerEnabled by remember { mutableStateOf(appSettings.isTrackerEnabled) }
	var frequency by remember { mutableStateOf(appSettings.trackerFrequency) }
	var favouriteSelection by remember {
		mutableStateOf(appSettings.trackerFavouriteCategories)
	}
	var downloadStrategy by remember { mutableStateOf(appSettings.trackerDownload) }

	// Sélection locale du dialog « Catégories favorites » (vide = toutes activées)
	var pendingCategories by remember { mutableStateOf(emptySet<String>()) }
	var openDialog by remember { mutableStateOf<FeedDialog?>(null) }

	val frequencyLabel = FREQUENCY_OPTIONS
		.firstOrNull { it.value == frequency }?.label ?: FR_FREQUENCY_DEFAULT
	val downloadLabel = when (downloadStrategy) {
		TrackerDownloadStrategy.DISABLED -> FR_DOWNLOAD_DISABLED
		TrackerDownloadStrategy.DOWNLOADED -> FR_DOWNLOAD_DOWNLOADED
		TrackerDownloadStrategy.RECENTLY_READ -> FR_DOWNLOAD_RECENTLY_READ
	}

	// « N de M activé(s) » : ensemble vide = toutes activées (sémantique d'origine).
	val enabledCount = if (favouriteSelection.isEmpty()) {
		favouriteCategories.size
	} else {
		favouriteCategories.count { it in favouriteSelection }
	}
	val categoriesSubtitle = frSourcesEnabled(enabledCount, favouriteCategories.size)

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_NEW_CHAPTERS,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			item {
				PreferenceSwitchRow(
					icon = Icons.Outlined.NotificationsActive,
					title = FR_TRACKER_SWITCH,
					checked = trackerEnabled,
					onCheckedChange = {
						trackerEnabled = it
						appSettings.isTrackerEnabled = it
					},
				)
				PreferenceDivider()
				PreferenceRow(
					icon = Icons.Outlined.Schedule,
					title = FR_FREQUENCY,
					subtitle = frequencyLabel,
					onClick = { openDialog = FeedDialog.FREQUENCY },
				)
				PreferenceDivider()
				PreferenceRow(
					icon = Icons.Outlined.Sync,
					title = FR_CHECK_UPDATES,
					onClick = onUpdateNow,
				)
				PreferenceCategoryHeader(title = FR_FAVOURITES)
				PreferenceRow(
					icon = Icons.Outlined.FavoriteBorder,
					title = FR_FAVOURITES_CATEGORIES,
					subtitle = categoriesSubtitle,
					onClick = {
						// Initialisation du dialog : ensemble vide = toutes cochées
						pendingCategories = if (favouriteSelection.isEmpty()) {
							favouriteCategories.toSet()
						} else {
							favouriteSelection
						}
						// Le dialog affiche un état vide explicatif si la liste est vide
						openDialog = FeedDialog.CATEGORIES
					},
				)
				PreferenceDivider()
				PreferenceRow(
					icon = Icons.Outlined.Download,
					title = FR_DOWNLOAD_NEW,
					subtitle = downloadLabel,
					onClick = { openDialog = FeedDialog.DOWNLOAD },
				)
				PreferenceCategoryHeader(title = FR_SECTION_TROUBLESHOOTING)
				PreferenceRow(
					icon = Icons.Outlined.ReceiptLong,
					title = FR_FEED_LOG,
					subtitle = FR_FEED_LOG_SUBTITLE,
					onClick = onOpenLog,
				)
				PreferenceDivider()
				// Concept Android (ignorer l'optimisation de batterie) sans équivalent desktop :
				// entrée présente mais grisée, divergence documentée en KDoc.
				PreferenceRow(
					icon = Icons.Outlined.BatteryAlert,
					title = FR_BATTERY_OPTIMIZATION,
					subtitle = FR_BATTERY_OPTIMIZATION_SUBTITLE,
					enabled = false,
					onClick = {},
				)
			}
		}
	}

	when (openDialog) {
		FeedDialog.FREQUENCY -> RadioDialog(
			title = FR_FREQUENCY,
			options = FREQUENCY_OPTIONS.map { it.label },
			selectedIndex = FREQUENCY_OPTIONS.indexOfFirst { it.value == frequency }
				.coerceAtLeast(0),
			onDismiss = { openDialog = null },
			onSelect = { index ->
				frequency = FREQUENCY_OPTIONS[index].value
				appSettings.trackerFrequency = FREQUENCY_OPTIONS[index].value
			},
		)

		FeedDialog.DOWNLOAD -> RadioDialog(
			title = FR_DOWNLOAD_NEW,
			options = listOf(
				FR_DOWNLOAD_DISABLED,
				FR_DOWNLOAD_DOWNLOADED,
				FR_DOWNLOAD_RECENTLY_READ,
			),
			selectedIndex = TrackerDownloadStrategy.entries.indexOf(downloadStrategy)
				.coerceAtLeast(0),
			onDismiss = { openDialog = null },
			onSelect = { index ->
				downloadStrategy = TrackerDownloadStrategy.entries[index]
				// Écriture via AppSettings.trackerDownload (var) — le getter public
				// trackerDownloadStrategy est en lecture seule dans AppSettings.
				appSettings.trackerDownload = TrackerDownloadStrategy.entries[index]
			},
		)

		FeedDialog.CATEGORIES -> if (favouriteCategories.isEmpty()) {
			// État vide explicatif : aucune catégorie favorite à proposer
			AlertDialog(
				onDismissRequest = { openDialog = null },
				title = { Text(text = FR_FAVOURITES_CATEGORIES) },
				text = {
					Text(
						text = FR_CATEGORIES_EMPTY,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				},
					confirmButton = {
						TextButton(onClick = { openDialog = null }) {
							Text(text = FR_OK)
						}
					},
				)
			} else {
				MultiChoiceDialog(
					title = FR_FAVOURITES_CATEGORIES,
				options = favouriteCategories,
				checked = favouriteCategories.map { it in pendingCategories },
				onDismiss = { openDialog = null },
				onCheckedChange = { index, checked ->
					pendingCategories = if (checked) {
						pendingCategories + favouriteCategories[index]
					} else {
						pendingCategories - favouriteCategories[index]
					}
				},
				onConfirm = {
					// Persiste : ensemble vide = toutes activées (sémantique d'origine).
					// Toutes cochées → vide (forme canonique) ; tout décocher → vide aussi,
					// donc interprété « toutes activées » (limite de la clé d'origine, cf. KDoc).
					favouriteSelection = if (
						pendingCategories.size == favouriteCategories.size
					) {
						emptySet()
					} else {
						pendingCategories
					}
					appSettings.trackerFavouriteCategories = favouriteSelection
					openDialog = null
				},
			)
		}

		null -> Unit
	}
}
