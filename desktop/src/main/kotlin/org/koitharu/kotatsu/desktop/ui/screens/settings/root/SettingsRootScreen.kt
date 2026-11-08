/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — écran racine
 * « Paramètres », reproduit pref_main + SettingsActivity de l'app d'origine (captures frames
 * 7-8 de la vidéo de référence) : 4 sections (Lecture / Trouver des mangas / Vos données /
 * Application) avec lignes icône + titre + sous-titre + chevron. Les sous-écrans encore non
 * portés affichent un état vide explicatif (phases P1–P6 du plan).
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.strings.FR_ABOUT
import org.koitharu.kotatsu.desktop.ui.strings.FR_ABOUT_VERSION
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPEARANCE
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPEARANCE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPLICATION
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_RESTORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_RESTORE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_COMING_SOON
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_STORAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_STORAGE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FIND_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANGA_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_NETWORK
import org.koitharu.kotatsu.desktop.ui.strings.FR_NETWORK_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_CHAPTERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_CHAPTERS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_NOTIFICATIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_NOTIFICATIONS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_PRIVACY_CONTENT
import org.koitharu.kotatsu.desktop.ui.strings.FR_PRIVACY_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_READING
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_SETTINGS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_YOUR_DATA
import org.koitharu.kotatsu.desktop.ui.strings.frSourcesEnabled

/**
 * Destinations internes de l'arbre des Paramètres (équivalent du PreferenceScreen racine
 * d'origine ; chaque clé correspond à un pref_*.xml / fragment de l'app Android).
 */
public enum class SettingsRoute {
	ROOT,
	READING, // Paramètres du lecteur (P1)
	APPEARANCE, // Apparence (implémenté)
	SOURCES, // Sources des mangas (P3)
	SUGGESTIONS, // Suggestions (P4)
	FEED_SETTINGS, // Nouveaux chapitres (P4)
	DATA, // Téléchargements et stockage (P5)
	BACKUP, // Sauvegarde et restauration (P5)
	NOTIFICATIONS, // Notifications (P5)
	NETWORK, // Réseau (P5)
	PRIVACY, // Confidentialité et contenu (P6)
	ABOUT, // À propos (P5)
	MASKED_IMAGES, // Images masquées (P1)
	MAIN_SECTIONS, // Principales sections de l'écran (P2)
}

/** Top bar des écrans Paramètres : flèche retour + titre + loupe (captures de référence). */
@Composable
public fun SettingsTopBar(
	title: String,
	onBackClick: () -> Unit,
	onSearchClick: (() -> Unit)?,
) {
	Surface(color = MaterialTheme.colorScheme.surface) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.padding(horizontal = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(onClick = onBackClick) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface,
				)
			}
			Text(
				text = title,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Spacer(modifier = Modifier.weight(1f))
			if (onSearchClick != null) {
				IconButton(onClick = onSearchClick) {
					Icon(
						imageVector = Icons.Filled.Search,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface,
					)
				}
			}
		}
	}
}

/**
 * Écran racine « Paramètres ». [enabledSources] / [totalSources] alimentent le sous-titre
 * « N de M activé(s) » de « Sources des mangas ».
 */
@Composable
public fun SettingsRootScreen(
	enabledSources: Int,
	totalSources: Int,
	onNavigate: (SettingsRoute) -> Unit,
	onBackClick: () -> Unit,
	onSearchClick: () -> Unit,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_SETTINGS,
			onBackClick = onBackClick,
			onSearchClick = onSearchClick,
		)
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			// --- Lecture ---
			item {
				PreferenceCategoryHeader(title = FR_READING)
				PreferenceRow(
					icon = Icons.Outlined.PlayCircle,
					title = FR_READER_SETTINGS,
					subtitle = FR_READER_SETTINGS_SUMMARY,
					onClick = { onNavigate(SettingsRoute.READING) },
				)
			}
			// --- Apparence (section visuellement distincte, comme pref_main d'origine) ---
			item {
				PreferenceCategoryHeader(title = FR_APPEARANCE)
				PreferenceRow(
					icon = Icons.Filled.Palette,
					title = FR_APPEARANCE,
					subtitle = FR_APPEARANCE_SUMMARY,
					onClick = { onNavigate(SettingsRoute.APPEARANCE) },
				)
			}
			// --- Trouver des mangas ---
			item {
				PreferenceCategoryHeader(title = FR_FIND_MANGA)
				PreferenceRow(
					icon = Icons.Filled.Source,
					title = FR_MANGA_SOURCES,
					subtitle = frSourcesEnabled(enabledSources, totalSources),
					onClick = { onNavigate(SettingsRoute.SOURCES) },
				)
				PreferenceRow(
					icon = Icons.Outlined.TipsAndUpdates,
					title = FR_SUGGESTIONS,
					subtitle = FR_SUGGESTIONS_SUMMARY,
					onClick = { onNavigate(SettingsRoute.SUGGESTIONS) },
				)
				PreferenceRow(
					icon = Icons.Outlined.CloudDownload,
					title = FR_NEW_CHAPTERS,
					subtitle = FR_NEW_CHAPTERS_SUMMARY,
					onClick = { onNavigate(SettingsRoute.FEED_SETTINGS) },
				)
			}
			// --- Vos données ---
			item {
				PreferenceCategoryHeader(title = FR_YOUR_DATA)
				PreferenceRow(
					icon = Icons.Outlined.Download,
					title = FR_DOWNLOADS_STORAGE,
					subtitle = FR_DOWNLOADS_STORAGE_SUMMARY,
					onClick = { onNavigate(SettingsRoute.DATA) },
				)
				PreferenceRow(
					icon = Icons.Filled.Backup,
					title = FR_BACKUP_RESTORE,
					subtitle = FR_BACKUP_RESTORE_SUMMARY,
					onClick = { onNavigate(SettingsRoute.BACKUP) },
				)
			}
			// --- Application ---
			item {
				PreferenceCategoryHeader(title = FR_APPLICATION)
				PreferenceRow(
					icon = Icons.Filled.Notifications,
					title = FR_NOTIFICATIONS,
					subtitle = FR_NOTIFICATIONS_SUMMARY,
					onClick = { onNavigate(SettingsRoute.NOTIFICATIONS) },
				)
				PreferenceRow(
					icon = Icons.Filled.Shield,
					title = FR_PRIVACY_CONTENT,
					subtitle = FR_PRIVACY_SUMMARY,
					onClick = { onNavigate(SettingsRoute.PRIVACY) },
				)
				PreferenceRow(
					icon = Icons.Filled.Language,
					title = FR_NETWORK,
					subtitle = FR_NETWORK_SUMMARY,
					onClick = { onNavigate(SettingsRoute.NETWORK) },
				)
				PreferenceRow(
					icon = Icons.Filled.Info,
					title = FR_ABOUT,
					subtitle = FR_ABOUT_VERSION,
					onClick = { onNavigate(SettingsRoute.ABOUT) },
				)
			}
		}
	}
}

private const val FR_YOUR_DATA_STR = "Vos données"

/**
 * Sous-écran de Paramètres non encore porté (phases P1–P6) : top bar + état vide explicatif.
 * Garantit que chaque entrée du racine est atteignable sans crash.
 */
@Composable
public fun SettingsStubScreen(
	title: String,
	onBackClick: () -> Unit,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = title,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		EmptyStateScreen(
			icon = Icons.Outlined.Settings,
			textPrimary = title,
			textSecondary = FR_COMING_SOON,
		)
	}
}
