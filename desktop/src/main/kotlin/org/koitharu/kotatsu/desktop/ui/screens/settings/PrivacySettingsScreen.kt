/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P6 : écran
 * « Confidentialité et contenu », reproduit pref_privacy + PrivacySettingsFragment (captures
 * frames t≈4:00-4:05 de la vidéo, inventaire lines 68-78). Ordre des entrées : les sections
 * visibles en vidéo (« Sécurité de l'application », « Votre activité », frame 74) avec les
 * entrées sans en-tête (Contenu pour adultes, Mode incognito) en haut.
 * Câblage :
 *  - Contenu pour adultes → AppSettings.isNsfwDisabled (clé d'origine « no_nsfw »). NB : à
 *    ce stade, aucune code du portage ne lit cette clé en dehors d'AppSettings (grep vérifié)
 *    : le réglage est persisté mais ne masque pas encore les sources NSFW.
 *  - Politique relative aux captures d'écran → AppSettings.screenshotsPolicy (clé
 *    « screenshots_policy », enum ScreenshotsPolicy à 4 valeurs) + dialog radio.
 *  - Statistiques de lecture → AppSettings.isStatsEnabled (clé « stats_on ») : propriété
 *    devenue var dans AppSettings (suite P6) — interrupteur entièrement câblé.
 *  - Mode incognito → ligne info désactivée : le toggle vit dans le menu ⋮ de chaque onglet
 *    (câblé P2). L'option incognito_nsfw (TriStateOption) est modifiable via dialog radio.
 *  - Suggestions 18+ → AppSettings.isSuggestionsExcludeNsfw (clé « suggestions_exclude_nsfw »,
 *    devenue var — suite P6) : interrupteur câblé, sens direct de la clé (ON = exclure les
 *    mangas pour adultes des suggestions).
 *  - Masquer le contenu NSFW sur Discord → ligne désactivée : Discord Rich Presence non
 *    porté (même divergence que l'écran « Comptes et synchronisation », AccountsScreen).
 *  - Protéger l'application → état réel lu via AppSettings.getAllValues() (clé
 *    ProtectManager.KEY_APP_PASSWORD, même stock que ProtectManager.isProtectionEnabled,
 *    hors périmètre ici) : sous-titre « Activé »/« Désactivé », clic désactivé — la
 *    configuration du mot de passe se fait via ProtectScreen/ProtectManager (P-protect).
 *  - Partager la fiabilité des sources → aucune clé dans AppSettings desktop : ligne
 *    désactivée, « Non disponible dans le portage desktop » (divergence documentée).
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.ScreenshotsPolicy
import org.koitharu.kotatsu.core.prefs.TriStateOption
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.screens.protect.ProtectManager
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_ADULT_CONTENT
import org.koitharu.kotatsu.desktop.ui.strings.FR_ADULT_CONTENT_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISCORD_HIDE_NSFW
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISCORD_HIDE_NSFW_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_NSFW
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_TABS_HINT
import org.koitharu.kotatsu.desktop.ui.strings.FR_PRIVACY_CONTENT
import org.koitharu.kotatsu.desktop.ui.strings.FR_PRIVACY_SECTION_ACTIVITY
import org.koitharu.kotatsu.desktop.ui.strings.FR_PRIVACY_SECTION_SECURITY
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_APP
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_APP_DISABLED
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_APP_ENABLED
import org.koitharu.kotatsu.desktop.ui.strings.FR_READING_STATS
import org.koitharu.kotatsu.desktop.ui.strings.FR_READING_STATS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SCREENSHOTS_ALLOW
import org.koitharu.kotatsu.desktop.ui.strings.FR_SCREENSHOTS_BLOCK_ALL
import org.koitharu.kotatsu.desktop.ui.strings.FR_SCREENSHOTS_BLOCK_INCOGNITO
import org.koitharu.kotatsu.desktop.ui.strings.FR_SCREENSHOTS_BLOCK_NSFW
import org.koitharu.kotatsu.desktop.ui.strings.FR_SCREENSHOTS_POLICY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SHARE_SOURCE_RELIABILITY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS_18_PLUS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS_18_PLUS_SUMMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRISTATE_ASK
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRISTATE_DISABLED
import org.koitharu.kotatsu.desktop.ui.strings.FR_TRISTATE_ENABLED

/** Libellé FR d'une valeur de [ScreenshotsPolicy] (dialog « Politique relative aux captures d'écran »). */
private fun frScreenshotsPolicyLabel(policy: ScreenshotsPolicy): String = when (policy) {
	ScreenshotsPolicy.ALLOW -> FR_SCREENSHOTS_ALLOW
	ScreenshotsPolicy.BLOCK_NSFW -> FR_SCREENSHOTS_BLOCK_NSFW
	ScreenshotsPolicy.BLOCK_INCOGNITO -> FR_SCREENSHOTS_BLOCK_INCOGNITO
	ScreenshotsPolicy.BLOCK_ALL -> FR_SCREENSHOTS_BLOCK_ALL
}

/** Libellé FR d'une valeur de [TriStateOption] (dialog « Mode incognito pour le contenu pour adultes »). */
private fun frIncognitoNsfwLabel(option: TriStateOption): String = when (option) {
	TriStateOption.ENABLED -> FR_TRISTATE_ENABLED
	TriStateOption.ASK -> FR_TRISTATE_ASK
	TriStateOption.DISABLED -> FR_TRISTATE_DISABLED
}

/**
 * Écran « Confidentialité et contenu » (pref_privacy d'origine).
 */
@Composable
public fun PrivacySettingsScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
) {
	// États locaux reflétés dans le store (FileKeyValueStore n'est pas observable)
	var nsfwDisabled by remember { mutableStateOf(appSettings.isNsfwDisabled) }
	var screenshotsPolicy by remember { mutableStateOf(appSettings.screenshotsPolicy) }
	var statsEnabled by remember { mutableStateOf(appSettings.isStatsEnabled) }
	var incognitoNsfw by remember { mutableStateOf(appSettings.incognitoModeForNsfw) }
	var suggestionsExcludeNsfw by remember { mutableStateOf(appSettings.isSuggestionsExcludeNsfw) }
	// État réel de la protection (même clé que ProtectManager.isProtectionEnabled)
	val appProtected by remember {
		val raw = appSettings.getAllValues()[ProtectManager.KEY_APP_PASSWORD]
		mutableStateOf(raw is String && raw.isNotEmpty())
	}

	// Dialogs ouverts
	var openDialog by remember { mutableStateOf<PrivacyDialog?>(null) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_PRIVACY_CONTENT,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			// --- Contenu (sans en-tête, comme en vidéo avant « Sécurité de l'application ») ---
			item {
				PreferenceSwitchRow(
					icon = Icons.Outlined.VisibilityOff,
					title = FR_ADULT_CONTENT,
					subtitle = FR_ADULT_CONTENT_SUMMARY,
					checked = nsfwDisabled,
					onCheckedChange = {
						nsfwDisabled = it
						appSettings.isNsfwDisabled = it
					},
				)
				// Mode incognito : toggle dans le menu ⋮ de chaque onglet (câblé P2) — ligne info
				PreferenceRow(
					icon = Icons.Outlined.Shield,
					title = FR_INCOGNITO_MODE,
					subtitle = FR_INCOGNITO_TABS_HINT,
					enabled = false,
					onClick = {},
				)
				PreferenceRow(
					icon = Icons.Outlined.Shield,
					title = FR_INCOGNITO_NSFW,
					subtitle = frIncognitoNsfwLabel(incognitoNsfw),
					onClick = { openDialog = PrivacyDialog.INCOGNITO_NSFW },
				)
				// « Suggestions 18+ » (frame 78, icône bulbe) : ON = exclure les mangas pour
				// adultes des suggestions (sens direct de la clé suggestions_exclude_nsfw)
				PreferenceSwitchRow(
					icon = Icons.Outlined.Lightbulb,
					title = FR_SUGGESTIONS_18_PLUS,
					subtitle = FR_SUGGESTIONS_18_PLUS_SUMMARY,
					checked = suggestionsExcludeNsfw,
					onCheckedChange = {
						suggestionsExcludeNsfw = it
						appSettings.isSuggestionsExcludeNsfw = it
					},
				)
				// Discord Rich Presence non porté (divergence, cf. AccountsScreen)
				PreferenceRow(
					icon = Icons.Outlined.Forum,
					title = FR_DISCORD_HIDE_NSFW,
					subtitle = FR_DISCORD_HIDE_NSFW_SUMMARY,
					enabled = false,
					onClick = {},
				)
			}
			// --- Sécurité de l'application ---
			item {
				PreferenceCategoryHeader(title = FR_PRIVACY_SECTION_SECURITY)
				// Configuration du mot de passe : ProtectScreen / ProtectManager (hors périmètre)
				PreferenceRow(
					icon = Icons.Outlined.Lock,
					title = FR_PROTECT_APP,
					subtitle = if (appProtected) FR_PROTECT_APP_ENABLED else FR_PROTECT_APP_DISABLED,
					enabled = false,
					onClick = {},
				)
				PreferenceRow(
					icon = Icons.Outlined.Screenshot,
					title = FR_SCREENSHOTS_POLICY,
					subtitle = frScreenshotsPolicyLabel(screenshotsPolicy),
					onClick = { openDialog = PrivacyDialog.SCREENSHOTS },
				)
			}
			// --- Votre activité ---
			item {
				PreferenceCategoryHeader(title = FR_PRIVACY_SECTION_ACTIVITY)
				// isStatsEnabled : var dans AppSettings (suite P6) — interrupteur câblé
				PreferenceSwitchRow(
					icon = Icons.Outlined.BarChart,
					title = FR_READING_STATS,
					subtitle = FR_READING_STATS_SUMMARY,
					checked = statsEnabled,
					onCheckedChange = {
						statsEnabled = it
						appSettings.isStatsEnabled = it
					},
				)
				// Aucune clé « share source reliability » dans AppSettings desktop — divergence
				PreferenceRow(
					icon = Icons.Outlined.Share,
					title = FR_SHARE_SOURCE_RELIABILITY,
					subtitle = FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE,
					enabled = false,
					onClick = {},
				)
				PreferenceDivider()
			}
		}
	}

	when (openDialog) {
		PrivacyDialog.INCOGNITO_NSFW -> RadioDialog(
			title = FR_INCOGNITO_NSFW,
			options = TriStateOption.entries.map { frIncognitoNsfwLabel(it) },
			selectedIndex = TriStateOption.entries.indexOf(incognitoNsfw),
			onDismiss = { openDialog = null },
			onSelect = {
				incognitoNsfw = TriStateOption.entries[it]
				appSettings.incognitoModeForNsfw = incognitoNsfw
			},
		)
		PrivacyDialog.SCREENSHOTS -> RadioDialog(
			title = FR_SCREENSHOTS_POLICY,
			options = ScreenshotsPolicy.entries.map { frScreenshotsPolicyLabel(it) },
			selectedIndex = ScreenshotsPolicy.entries.indexOf(screenshotsPolicy),
			onDismiss = { openDialog = null },
			onSelect = {
				screenshotsPolicy = ScreenshotsPolicy.entries[it]
				appSettings.screenshotsPolicy = screenshotsPolicy
			},
		)
		null -> {}
	}
}

/** Dialogs radio ouverts depuis l'écran « Confidentialité et contenu ». */
private enum class PrivacyDialog {
	INCOGNITO_NSFW, SCREENSHOTS,
}
