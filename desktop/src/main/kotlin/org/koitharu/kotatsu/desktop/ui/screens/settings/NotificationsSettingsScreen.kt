/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « Notifications » (parité UI FR avec l'app Android) : section « Nouveaux chapitres »
 * (notification à l'apparition de nouveaux chapitres, exclusion des mangas pour adultes, renvoi
 * vers les réglages des nouveaux chapitres) et section « Suggestions » (notification des
 * suggestions). Préférences via AppSettings.isTrackerNotificationsEnabled /
 * isTrackerNoNsfw (clés KEY_TRACKER_NOTIFICATIONS / KEY_TRACKER_NO_NSFW d'origine), mutables.
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
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_CHAPTERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_NOTIFICATIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_OPEN_FEED_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS_NOTIFICATIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS_NOTIFICATIONS_UNAVAILABLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKER_FREQUENCY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKER_FREQUENCY_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKER_NO_NSFW
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKER_NOTIFICATIONS

// Chaînes FR de l'écran « Notifications » : voir ui/strings/FrSettings.kt (FR_NOTIFICATIONS,
// FR_NEW_CHAPTERS et FR_SUGGESTIONS viennent de Fr.kt).

/**
 * Écran « Notifications » (Paramètres > Notifications) : notifications des nouveaux chapitres
 * et des suggestions. [onOpenFeedSettings], s'il est fourni, affiche une ligne de renvoi vers
 * l'écran « Paramètres des nouveaux chapitres ».
 */
@Composable
public fun NotificationsSettingsScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
	onOpenFeedSettings: (() -> Unit)? = null,
) {
	// États locaux miroir des préférences (AppSettings n'est pas observable) ; écriture immédiate
	var trackerNotifications by remember { mutableStateOf(appSettings.isTrackerNotificationsEnabled) }
	var trackerNoNsfw by remember { mutableStateOf(appSettings.isTrackerNoNsfw) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_NOTIFICATIONS,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			// 1. Section « Nouveaux chapitres »
			PreferenceCategoryHeader(title = FR_NEW_CHAPTERS)

			// Notification des nouveaux chapitres (KEY_TRACKER_NOTIFICATIONS)
			PreferenceSwitchRow(
				icon = Icons.Outlined.NotificationsActive,
				title = FR_TRACKER_NOTIFICATIONS,
				checked = trackerNotifications,
				onCheckedChange = {
					trackerNotifications = it
					appSettings.isTrackerNotificationsEnabled = it
				},
			)
			PreferenceDivider()

			// Pas de notification pour les mangas pour adultes (KEY_TRACKER_NO_NSFW)
			PreferenceSwitchRow(
				icon = Icons.Outlined.Shield,
				title = FR_TRACKER_NO_NSFW,
				checked = trackerNoNsfw,
				onCheckedChange = {
					trackerNoNsfw = it
					appSettings.isTrackerNoNsfw = it
				},
			)
			PreferenceDivider()

			// Renvoi vers la fréquence de vérification (informatif ; l'écran principal peut
			// câbler [onOpenFeedSettings] vers « Nouveaux chapitres »)
			PreferenceRow(
				icon = Icons.Outlined.Schedule,
				title = FR_TRACKER_FREQUENCY,
				subtitle = FR_TRACKER_FREQUENCY_SUMMARY,
				enabled = onOpenFeedSettings != null,
				onClick = { onOpenFeedSettings?.invoke() },
			)
			PreferenceDivider()

			// Ligne de renvoi optionnelle vers « Paramètres des nouveaux chapitres »
			if (onOpenFeedSettings != null) {
				PreferenceRow(
					icon = Icons.Outlined.Newspaper,
					title = FR_OPEN_FEED_SETTINGS,
					onClick = onOpenFeedSettings,
				)
				PreferenceDivider()
			}

			// 2. Section « Suggestions »
			PreferenceCategoryHeader(title = FR_SUGGESTIONS)

			// Notification des suggestions : aucune clé AppSettings correspondante n'existe
			// côté desktop — entrée d'information désactivée.
			PreferenceSwitchRow(
				icon = Icons.Outlined.Newspaper,
				title = FR_SUGGESTIONS_NOTIFICATIONS,
				subtitle = FR_SUGGESTIONS_NOTIFICATIONS_UNAVAILABLE,
				checked = false,
				onCheckedChange = {},
				enabled = false,
			)
		}
	}
}
