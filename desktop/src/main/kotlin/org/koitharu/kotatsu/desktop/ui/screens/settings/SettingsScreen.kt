/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-07 — phase P2 : écran
 * « Apparence » complet (pref_appearance + AppearanceSettingsFragment, captures frames 9-25 de
 * la vidéo) : Thème (schémas de couleurs d'origine, Langue), Liste des mangas (Mode liste,
 * Taille de la grille, filtres rapides, indicateurs de progression, badges), Page du manga
 * (Réduire la description longue ; miniatures/onglet/temps estimé/connexes : P8, entrées
 * désactivées), Navigation (bouton flottant Continuer et Confirmation de sortie câblés ;
 * Principales sections de l'écran → écran dédié). Les réglages de liste s'appliquent à
 * l'historique/favoris via les préférences core (mêmes clés KEY_* que l'original).
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.ListMode
import org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPEARANCE
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPEARANCE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLLAPSE_LONG_DESCRIPTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_DEFAULT_TAB
import org.koitharu.kotatsu.desktop.ui.strings.FR_DEFAULT_TAB_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_EXIT_CONFIRMATION
import org.koitharu.kotatsu.desktop.ui.strings.FR_FLOATING_CONTINUE
import org.koitharu.kotatsu.desktop.ui.strings.FR_FLOATING_CONTINUE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_GRID_SIZE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LANGUAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LANGUAGE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_BADGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_BADGES_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_SECTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_MAIN_SECTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANGA_PAGE_SECTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_NAVIGATION_SECTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_PAGES_THUMBNAILS
import org.koitharu.kotatsu.desktop.ui.strings.FR_PAGES_THUMBNAILS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_PIN_NAVIGATION
import org.koitharu.kotatsu.desktop.ui.strings.FR_PIN_NAVIGATION_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROGRESS_INDICATORS
import org.koitharu.kotatsu.desktop.ui.strings.FR_QUICK_FILTERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_QUICK_FILTERS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_READING_TIME
import org.koitharu.kotatsu.desktop.ui.strings.FR_READING_TIME_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_RECENT_SHORTCUTS
import org.koitharu.kotatsu.desktop.ui.strings.FR_RELATED_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_RELATED_MANGA_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_SUGGESTIONS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.frListModeLabel
import org.koitharu.kotatsu.desktop.ui.strings.frProgressModeLabel
import org.koitharu.kotatsu.desktop.ui.theme.ThemeMode

private val SCHEME_TITLES = mapOf(
	"DEFAULT" to "Totoro",
	"ASUKA" to "Asuka",
	"MION" to "Mion",
	"RIKKA" to "Rikka",
	"SAKURA" to "Sakura",
	"MAMIMI" to "Mamimi",
	"KANADE" to "Kanade",
	"ITSUKA" to "Itsuka",
	"MIKU" to "Miku",
)

@Composable
public fun AppearanceSettingsScreen(
	settings: DesktopSettings,
	appSettings: AppSettings? = null,
	modifier: Modifier = Modifier,
	onBackClick: (() -> Unit)? = null,
	onNavigate: (SettingsRoute) -> Unit = {},
) {
	if (onBackClick != null) {
		// Accès depuis l'écran racine « Paramètres » : top bar avec flèche retour
		Column(modifier = modifier.fillMaxSize()) {
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
						text = FR_APPEARANCE,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
					)
				}
			}
			AppearanceSettingsContent(settings = settings, appSettings = appSettings, onNavigate = onNavigate)
		}
	} else {
		// Accès direct depuis le rail (comportement historique, sans top bar)
		AppearanceSettingsContent(settings = settings, appSettings = appSettings, modifier = modifier, onNavigate = onNavigate)
	}
}

private enum class AppearanceDialog {
	LIST_MODE, PROGRESS_INDICATORS,
}

@Composable
private fun AppearanceSettingsContent(
	settings: DesktopSettings,
	appSettings: AppSettings?,
	modifier: Modifier = Modifier,
	onNavigate: (SettingsRoute) -> Unit = {},
) {
	// États locaux reflétés dans le store (FileKeyValueStore n'est pas observable)
	var listMode by remember { mutableStateOf(appSettings?.listMode ?: ListMode.GRID) }
	var gridSize by remember { mutableFloatStateOf((appSettings?.gridSize ?: 100).toFloat()) }
	var progressMode by remember { mutableStateOf(appSettings?.progressIndicators ?: ProgressIndicatorMode.PERCENT_READ) }
	var quickFilters by remember { mutableStateOf(appSettings?.isQuickFilterEnabled ?: true) }
	var badges by remember { mutableStateOf(appSettings?.isListBadgesEnabled ?: true) }
	var collapseDescription by remember { mutableStateOf(appSettings?.isLongDescriptionCollapsed ?: true) }
	var floatingContinue by remember { mutableStateOf(appSettings?.isFloatingContinueEnabled ?: true) }
	var exitConfirmation by remember { mutableStateOf(appSettings?.isExitConfirmationEnabled ?: false) }
	var openDialog by remember { mutableStateOf<AppearanceDialog?>(null) }

	LazyColumn(modifier = modifier.fillMaxSize()) {
		// --- Thème ---
		item {
			Text(
				text = "Theme",
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)
		}
		items(listOf("SYSTEM", "LIGHT", "DARK")) { mode ->
			val selected = settings.themeMode.value == ThemeMode.valueOf(mode)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { settings.setThemeMode(ThemeMode.valueOf(mode)) }
					.padding(horizontal = 16.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				RadioButton(selected = selected, onClick = { settings.setThemeMode(ThemeMode.valueOf(mode)) })
				Text(
					text = when (mode) {
						"SYSTEM" -> "Follow system"
						"LIGHT" -> "Light"
						else -> "Dark"
					},
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}
		item {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = "AMOLED theme",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.weight(1f),
				)
				Switch(
					checked = settings.isAmoled.value,
					onCheckedChange = { settings.setAmoled(it) },
				)
			}
			PreferenceDivider()
		}
		item {
			Text(
				text = "Color scheme",
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)
		}
		items(listOf("DEFAULT", "ASUKA", "MION", "RIKKA", "SAKURA", "MAMIMI", "KANADE", "ITSUKA", "MIKU")) { scheme ->
			val selected = settings.colorScheme.value == scheme
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { settings.setColorScheme(scheme) }
					.padding(horizontal = 16.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				RadioButton(selected = selected, onClick = { settings.setColorScheme(scheme) })
				Text(
					text = SCHEME_TITLES[scheme] ?: scheme,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}
		item {
			// Langue : l'app desktop est FR seule (divergence documentée)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_LANGUAGE,
				subtitle = FR_LANGUAGE_SUMMARY,
				enabled = false,
			)
		}

		// --- Liste des mangas ---
		item {
			PreferenceCategoryHeader(title = FR_LIST_SECTION)
			PreferenceRow(
				icon = Icons.Outlined.Style,
				title = FR_LIST_MODE,
				subtitle = frListModeLabel(listMode),
				onClick = { if (appSettings != null) openDialog = AppearanceDialog.LIST_MODE },
			)
			Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
				Text(
					text = FR_GRID_SIZE,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(top = 8.dp),
				)
				Row(verticalAlignment = Alignment.CenterVertically) {
					Slider(
						value = gridSize,
						onValueChange = {
							gridSize = it
							appSettings?.gridSize = it.toInt()
						},
						valueRange = 50f..200f,
						modifier = Modifier.weight(1f),
					)
					Text(
						text = "${gridSize.toInt()} %",
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(start = 12.dp),
					)
				}
			}
			PreferenceSwitchRow(
				icon = Icons.Outlined.FilterAlt,
				title = FR_QUICK_FILTERS,
				subtitle = FR_QUICK_FILTERS_SUMMARY,
				checked = quickFilters,
				onCheckedChange = {
					quickFilters = it
					appSettings?.isQuickFilterEnabled = it
				},
			)
			PreferenceRow(
				icon = Icons.Outlined.Numbers,
				title = FR_PROGRESS_INDICATORS,
				subtitle = frProgressModeLabel(progressMode),
				onClick = { if (appSettings != null) openDialog = AppearanceDialog.PROGRESS_INDICATORS },
			)
			PreferenceSwitchRow(
				icon = Icons.Outlined.Checklist,
				title = FR_LIST_BADGES,
				subtitle = FR_LIST_BADGES_SUMMARY,
				checked = badges,
				onCheckedChange = {
					badges = it
					appSettings?.isListBadgesEnabled = it
				},
			)
		}

		// --- Page du manga ---
		item {
			PreferenceCategoryHeader(title = FR_MANGA_PAGE_SECTION)
			PreferenceSwitchRow(
				icon = Icons.Outlined.Tune,
				title = FR_COLLAPSE_LONG_DESCRIPTION,
				checked = collapseDescription,
				onCheckedChange = {
					collapseDescription = it
					appSettings?.isLongDescriptionCollapsed = it
				},
			)
			// Miniatures des pages / onglet par défaut / temps estimé / connexes : P8
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_PAGES_THUMBNAILS,
				subtitle = FR_PAGES_THUMBNAILS_SUMMARY,
				enabled = false,
			)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_DEFAULT_TAB,
				subtitle = FR_DEFAULT_TAB_SUMMARY,
				enabled = false,
			)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_READING_TIME,
				subtitle = FR_READING_TIME_SUMMARY,
				enabled = false,
			)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_RELATED_MANGA,
				subtitle = FR_RELATED_MANGA_SUMMARY,
				enabled = false,
			)
		}

		// --- Navigation ---
		item {
			PreferenceCategoryHeader(title = FR_NAVIGATION_SECTION)
			// Épingler l'interface : rien ne se masque au défilement sur desktop (divergence)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_PIN_NAVIGATION,
				subtitle = FR_PIN_NAVIGATION_SUMMARY,
				enabled = false,
			)
			PreferenceSwitchRow(
				icon = Icons.Outlined.ExitToApp,
				title = FR_FLOATING_CONTINUE,
				subtitle = FR_FLOATING_CONTINUE_SUMMARY,
				checked = floatingContinue,
				onCheckedChange = {
					floatingContinue = it
					appSettings?.isFloatingContinueEnabled = it
				},
			)
			PreferenceSwitchRow(
				icon = Icons.Outlined.ExitToApp,
				title = FR_EXIT_CONFIRMATION,
				subtitle = "Demander confirmation avant de fermer la fenêtre (desktop)",
				checked = exitConfirmation,
				onCheckedChange = {
					exitConfirmation = it
					appSettings?.isExitConfirmationEnabled = it
				},
			)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_SEARCH_SUGGESTIONS,
				subtitle = FR_SEARCH_SUGGESTIONS_SUMMARY,
				enabled = false,
			)
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_RECENT_SHORTCUTS,
				subtitle = "Geste long-press de l'icône Android, sans équivalent sur desktop",
				enabled = false,
			)
			PreferenceRow(
				icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
				title = FR_MAIN_SECTIONS,
				onClick = { onNavigate(SettingsRoute.MAIN_SECTIONS) },
			)
			PreferenceDivider()
		}
	}

	when (openDialog) {
		AppearanceDialog.LIST_MODE -> RadioDialog(
			title = FR_LIST_MODE,
			options = ListMode.entries.map { frListModeLabel(it) },
			selectedIndex = ListMode.entries.indexOf(listMode),
			onDismiss = { openDialog = null },
			onSelect = {
				listMode = ListMode.entries[it]
				appSettings?.listMode = listMode
			},
		)
		AppearanceDialog.PROGRESS_INDICATORS -> RadioDialog(
			title = FR_PROGRESS_INDICATORS,
			options = ProgressIndicatorMode.entries.map { frProgressModeLabel(it) },
			selectedIndex = ProgressIndicatorMode.entries.indexOf(progressMode),
			onDismiss = { openDialog = null },
			onSelect = {
				progressMode = ProgressIndicatorMode.entries[it]
				appSettings?.progressIndicators = progressMode
			},
		)
		null -> {}
	}
}
