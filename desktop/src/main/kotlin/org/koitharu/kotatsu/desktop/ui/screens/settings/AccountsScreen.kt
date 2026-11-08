/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P6 : écran
 * « Comptes et synchronisation » (parité UI FR avec l'app Android, vidéo t≈3:50-4:00) :
 * section « Services de suivi » (AniList, Kitsu, MyAnimeList, Shikimori), ligne « Community »
 * (libellé EN conservé) et ligne « Discord Rich Presence », chaque ligne ouvrant un dialog
 * d'information (suivi et Discord non portés sur desktop) ; note de bas d'écran sur la
 * synchronisation multi-appareils.
 * Icônes : approximation Material Icons (l'app Android a ses icônes propres) — AniList
 * = Interests, Kitsu = Pets (mascotte renard), MyAnimeList = MenuBook (liste de mangas),
 * Shikimori = Public (site web), Community = Groups, Discord = Forum (bulles de discussion :
 * l'icône de marque Discord n'existe pas dans material-icons-extended 1.7.3).
 * Chaînes : ui/strings/FrAccounts.kt + FR_OK de Fr.kt (bouton du dialog).
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACCOUNTS_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_COMMUNITY_ROW
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISABLED
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISCORD_ROW
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISCORD_UNAVAILABLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_SERVICE_ANILIST
import org.koitharu.kotatsu.desktop.ui.strings.FR_SERVICE_KITSU
import org.koitharu.kotatsu.desktop.ui.strings.FR_SERVICE_MAL
import org.koitharu.kotatsu.desktop.ui.strings.FR_SERVICE_SHIKIMORI
import org.koitharu.kotatsu.desktop.ui.strings.FR_SYNC_UNAVAILABLE_NOTE
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKERS_HEADER
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRACKERS_UNAVAILABLE

private val NOTE_ICON_SIZE = 20.dp
private val NOTE_BOTTOM_PADDING = 24.dp
private val NOTE_TEXT_PADDING = 8.dp

/**
 * Écran « Comptes et synchronisation » (Paramètres > Comptes et synchronisation) : liste des
 * services de suivi et intégrations, tous désactivés dans le portage desktop. Chaque clic
 * ouvre un dialog d'information ; pas de loupe dans la top bar (captures de référence).
 */
@Composable
public fun AccountsScreen(
	onBackClick: () -> Unit,
) {
	var dialogTitle by remember { mutableStateOf<String?>(null) }
	var dialogText by remember { mutableStateOf("") }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_ACCOUNTS_TITLE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			// 1. Services de suivi (AniList, Kitsu, MyAnimeList, Shikimori)
			PreferenceCategoryHeader(title = FR_TRACKERS_HEADER)
			TrackerLine(
				name = FR_SERVICE_ANILIST,
				icon = Icons.Filled.Interests,
				onClick = {
					dialogTitle = FR_SERVICE_ANILIST
					dialogText = FR_TRACKERS_UNAVAILABLE
				},
			)
			TrackerLine(
				name = FR_SERVICE_KITSU,
				icon = Icons.Filled.Pets,
				onClick = {
					dialogTitle = FR_SERVICE_KITSU
					dialogText = FR_TRACKERS_UNAVAILABLE
				},
			)
			TrackerLine(
				name = FR_SERVICE_MAL,
				icon = Icons.AutoMirrored.Outlined.MenuBook,
				onClick = {
					dialogTitle = FR_SERVICE_MAL
					dialogText = FR_TRACKERS_UNAVAILABLE
				},
			)
			TrackerLine(
				name = FR_SERVICE_SHIKIMORI,
				icon = Icons.Outlined.Public,
				onClick = {
					dialogTitle = FR_SERVICE_SHIKIMORI
					dialogText = FR_TRACKERS_UNAVAILABLE
				},
			)

			// 2. Community (libellé EN conservé, cf. inventaire ligne 68)
			TrackerLine(
				name = FR_COMMUNITY_ROW,
				icon = Icons.Outlined.Groups,
				onClick = {
					dialogTitle = FR_COMMUNITY_ROW
					dialogText = FR_TRACKERS_UNAVAILABLE
				},
			)
			PreferenceDivider()

			// 3. Discord Rich Presence (divergence : RPC non porté)
			TrackerLine(
				name = FR_DISCORD_ROW,
				icon = Icons.Outlined.Forum,
				onClick = {
					dialogTitle = FR_DISCORD_ROW
					dialogText = FR_DISCORD_UNAVAILABLE
				},
			)
			PreferenceDivider()

			// 4. Note d'information (divergence : synchronisation multi-appareils non portée)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = NOTE_BOTTOM_PADDING),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					imageVector = Icons.Outlined.Info,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(NOTE_ICON_SIZE),
				)
				Text(
					text = FR_SYNC_UNAVAILABLE_NOTE,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(start = NOTE_TEXT_PADDING),
				)
			}
		}
	}

	// Dialog d'information partagé par toutes les lignes
	if (dialogTitle != null) {
		AlertDialog(
			onDismissRequest = { dialogTitle = null },
			title = { Text(text = dialogTitle.orEmpty()) },
			text = { Text(text = dialogText) },
			confirmButton = {
				TextButton(onClick = { dialogTitle = null }) {
					Text(text = FR_OK) // Fr.kt (string/ok), pas de duplication
				}
			},
		)
	}
}

/** Ligne de service : icône + nom + sous-titre « Désactivé » + chevron (via PreferenceRow). */
@Composable
private fun TrackerLine(
	name: String,
	icon: ImageVector,
	onClick: () -> Unit,
) {
	PreferenceRow(
		icon = icon,
		title = name,
		subtitle = FR_DISABLED,
		onClick = onClick,
	)
}
