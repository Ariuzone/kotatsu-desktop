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
 * Modification : 2026-11-08 — phase P2 : section « Thème » traduite en français, rangée
 * horizontale de cartes de palette pour « Schéma de couleurs » (ColorSchemeCardsRow,
 * frames 1:40-2:25) à la place des rangées radio, libellés FR pour le mode de thème et
 * l'option AMOLED (« Noir »). Logique setThemeMode/setColorScheme/setAmoled inchangée.
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
import org.koitharu.kotatsu.desktop.ui.components.preferences.ColorSchemeCardsRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute
import org.koitharu.kotatsu.desktop.ui.strings.FR_AMOLED
import org.koitharu.kotatsu.desktop.ui.strings.FR_AMOLED_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPEARANCE
import org.koitharu.kotatsu.desktop.ui.strings.FR_APPEARANCE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLLAPSE_LONG_DESCRIPTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLOR_SCHEME
import org.koitharu.kotatsu.desktop.ui.strings.FR_DARK
import org.koitharu.kotatsu.desktop.ui.strings.FR_DEFAULT_TAB
import org.koitharu.kotatsu.desktop.ui.strings.FR_DEFAULT_TAB_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_EXIT_CONFIRMATION
import org.koitharu.kotatsu.desktop.ui.strings.FR_EXIT_CONFIRMATION_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FLOATING_CONTINUE
import org.koitharu.kotatsu.desktop.ui.strings.FR_FLOATING_CONTINUE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FOLLOW_SYSTEM
import org.koitharu.kotatsu.desktop.ui.strings.FR_GRID_SIZE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LANGUAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LANGUAGE_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIGHT
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
import org.koitharu.kotatsu.desktop.ui.strings.FR_RECENT_SHORTCUTS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_RELATED_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_RELATED_MANGA_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_SUGGESTIONS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_THEME
import org.koitharu.kotatsu.desktop.ui.strings.currentUiLang
import org.koitharu.kotatsu.desktop.ui.strings.frListModeLabel
import org.koitharu.kotatsu.desktop.ui.strings.frProgressModeLabel
import org.koitharu.kotatsu.desktop.ui.theme.ThemeMode
import org.koitharu.kotatsu.desktop.ui.theme.isDarkTheme

/** Chaîne dépendant de la langue d'interface courante (P9 : zéro chaîne FR-only en anglais). */
private fun t(fr: String, en: String): String =
	if (currentUiLang() == org.koitharu.kotatsu.desktop.ui.strings.UiLang.EN) en else fr

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
				text = FR_THEME,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)
		}
		item {
			// Schéma de couleurs : rangée horizontale scrollable de cartes de palette
			Text(
				text = FR_COLOR_SCHEME,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(horizontal = 16.dp),
			)
			ColorSchemeCardsRow(
				selected = settings.colorScheme.value,
				dark = isDarkTheme(settings.themeMode.value),
				onSelect = { settings.setColorScheme(it) },
				modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
			)
			PreferenceDivider()
		}
		items(ThemeMode.entries) { mode ->
			val selected = settings.themeMode.value == mode
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { settings.setThemeMode(mode) }
					.padding(horizontal = 16.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				RadioButton(selected = selected, onClick = { settings.setThemeMode(mode) })
				Text(
					text = when (mode) {
						ThemeMode.SYSTEM -> FR_FOLLOW_SYSTEM
						ThemeMode.LIGHT -> FR_LIGHT
						ThemeMode.DARK -> FR_DARK
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
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = FR_AMOLED,
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
					)
					Text(
						text = FR_AMOLED_SUMMARY,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
				Switch(
					checked = settings.isAmoled.value,
					onCheckedChange = { settings.setAmoled(it) },
				)
			}
			PreferenceDivider()
		}
		item {
			// Langue (1.1.1) : « Suivre le système » (repli anglais), English, Français —
			// appliquée aux interfaces de maintenance (installateur/désinstallateur) ;
			// infrastructure prête pour la traduction de l'interface principale.
			var langMenu by remember { mutableStateOf(false) }
			var langValue by remember { mutableStateOf(appSettings?.appLanguage ?: "system") }
			PreferenceRow(
				icon = Icons.Outlined.Info,
				title = FR_LANGUAGE,
				subtitle = when (langValue) {
					"en" -> t("English", "English")
					"fr" -> t("Français", "French")
					else -> FR_LANGUAGE_SUMMARY
				},
				onClick = { if (appSettings != null) langMenu = true },
				enabled = appSettings != null,
			)
			if (langMenu) {
				RadioDialog(
					title = FR_LANGUAGE,
					options = listOf(
						FR_LANGUAGE_SUMMARY, // « Suivre le système » / « Follow the system »
						t("English", "English"),
						t("Français", "French"),
					),
					selectedIndex = when (langValue) {
						"en" -> 1
						"fr" -> 2
						else -> 0
					},
					onDismiss = { langMenu = false },
					onSelect = { index ->
						langValue = when (index) {
							1 -> "en"
							2 -> "fr"
							else -> "system"
						}
						appSettings?.appLanguage = langValue
						// 1.1.1 : application immédiate des chaînes (recalcul à la recomposition)
						org.koitharu.kotatsu.desktop.ui.strings.applyUiLanguagePreference(langValue)
					},
				)
			}
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
				subtitle = FR_EXIT_CONFIRMATION_SUMMARY,
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
				subtitle = FR_RECENT_SHORTCUTS_SUMMARY,
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
