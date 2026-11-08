/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — phase P1 : écran
 * « Paramètres du lecteur », reproduit pref_reader + ReaderSettingsFragment (captures frames
 * 8-19 de la vidéo) : sections Pages / Webtoon / Avancé avec les libellés exacts de l'app
 * d'origine. Les réglages sont écrits dans AppSettings (mêmes clés KEY_* que l'original) et
 * appliqués à l'ouverture suivante du lecteur. Les entrées sans effet sur desktop (boutons de
 * volume, plein écran Android…) sont omises ou désactivées — divergences documentées dans
 * docs/UI_DIVERGENCES.md.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.NetworkPolicy
import org.koitharu.kotatsu.core.prefs.ReaderAnimation
import org.koitharu.kotatsu.core.prefs.ReaderBackground
import org.koitharu.kotatsu.core.prefs.ReaderMode
import org.koitharu.kotatsu.core.prefs.ScaleMode
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.frReaderModeLabel
import org.koitharu.kotatsu.desktop.ui.strings.frReaderAnimationLabel
import org.koitharu.kotatsu.desktop.ui.strings.frReaderBackgroundLabel
import org.koitharu.kotatsu.desktop.ui.strings.frReaderScaleLabel
import org.koitharu.kotatsu.desktop.ui.strings.frNetworkPolicyLabel

/**
 * Écran « Paramètres du lecteur ». [onNavigate] ouvre « Images masquées ».
 */
@Composable
public fun ReaderSettingsScreen(
	settings: AppSettings,
	onBackClick: () -> Unit,
	onNavigate: (SettingsRoute) -> Unit,
) {
	// États locaux reflétés dans le store (FileKeyValueStore n'est pas observable)
	var readerMode by remember { mutableStateOf(settings.readerMode) }
	var autoDetect by remember { mutableStateOf(settings.isReaderModeDetect) }
	var scaleMode by remember { mutableStateOf(settings.readerScaleMode) }
	var animation by remember { mutableStateOf(settings.readerAnimation) }
	var background by remember { mutableStateOf(settings.readerBackground) }
	var pagesNumbered by remember { mutableStateOf(settings.isPagesNumberingEnabled) }
	var webtoonSeparators by remember { mutableStateOf(settings.isWebtoonSeparatorsEnabled) }
	var preloadPolicy by remember { mutableStateOf(settings.pagesPreloadPolicy) }

	// Dialogs ouverts
	var openDialog by remember { mutableStateOf<ReaderDialog?>(null) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_READER_SETTINGS,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			// --- Pages ---
			item {
				PreferenceCategoryHeader(title = "Pages")
				PreferenceRow(
					icon = Icons.Outlined.Tune,
					title = "Mode par défaut",
					subtitle = frReaderModeLabel(readerMode),
					onClick = { openDialog = ReaderDialog.MODE },
				)
				PreferenceSwitchRow(
					icon = Icons.Outlined.Shield,
					title = "Mode de détection automatique du lecteur",
					subtitle = "Détecter automatiquement si un manga est un webtoon",
					checked = autoDetect,
					onCheckedChange = {
						autoDetect = it
						settings.isReaderModeDetect = it
					},
				)
				PreferenceRow(
					icon = Icons.Filled.OpenWith,
					title = "Mode mise à l'échelle",
					subtitle = frReaderScaleLabel(scaleMode),
					onClick = { openDialog = ReaderDialog.SCALE },
				)
				PreferenceRow(
					icon = Icons.Outlined.SwapVert,
					title = "Animation de page",
					subtitle = frReaderAnimationLabel(animation),
					onClick = { openDialog = ReaderDialog.ANIMATION },
				)
				// « Recadrer les pages » : sans effet sur desktop (divergence documentée)
				PreferenceRow(
					icon = Icons.Filled.Texture,
					title = "Recadrer les pages",
					subtitle = "Désactivé (non disponible sur desktop)",
					enabled = false,
					onClick = {},
				)
				PreferenceRow(
					icon = Icons.Filled.FormatPaint,
					title = "Arrière-plan",
					subtitle = frReaderBackgroundLabel(background),
					onClick = { openDialog = ReaderDialog.BACKGROUND },
				)
				PreferenceSwitchRow(
					icon = Icons.Outlined.Numbers,
					title = "Pages numérotées",
					subtitle = "Afficher les numéros de page dans le coin inférieur",
					checked = pagesNumbered,
					onCheckedChange = {
						pagesNumbered = it
						settings.isPagesNumberingEnabled = it
					},
				)
			}
			// --- Webtoon ---
			item {
				PreferenceCategoryHeader(title = "Webtoon")
				PreferenceSwitchRow(
					icon = Icons.Outlined.Tune,
					title = "Séparateurs en mode webtoon",
					subtitle = "Afficher des espaces verticaux entre les pages en mode webtoon",
					checked = webtoonSeparators,
					onCheckedChange = {
						webtoonSeparators = it
						settings.isWebtoonSeparatorsEnabled = it
					},
				)
				// Zoom Webtoon : zoom autorisé par Ctrl+molette / double-clic sur desktop
				PreferenceRow(
					icon = Icons.Outlined.Info,
					title = "Zoom Webtoon",
					subtitle = "Ctrl+molette ou double-clic dans le lecteur (desktop)",
					enabled = false,
					onClick = {},
				)
				PreferenceRow(
					icon = Icons.Outlined.Shield,
					title = "Images masquées",
					subtitle = "Tout ce que vous avez effacé. Retirez une image pour la revoir",
					onClick = { onNavigate(SettingsRoute.MASKED_IMAGES) },
				)
			}
			// --- Avancé ---
			item {
				PreferenceCategoryHeader(title = "Avancé")
				PreferenceRow(
					icon = Icons.Outlined.WbSunny,
					title = "Précharger les pages",
					subtitle = frNetworkPolicyLabel(preloadPolicy),
					onClick = { openDialog = ReaderDialog.PRELOAD },
				)
				PreferenceDivider()
			}
		}
	}

	when (openDialog) {
		ReaderDialog.MODE -> RadioDialog(
			title = "Mode par défaut",
			options = ReaderMode.entries.map { frReaderModeLabel(it) },
			selectedIndex = ReaderMode.entries.indexOf(readerMode),
			onDismiss = { openDialog = null },
			onSelect = {
				readerMode = ReaderMode.entries[it]
				settings.readerMode = readerMode
			},
		)
		ReaderDialog.SCALE -> RadioDialog(
			title = "Mode mise à l'échelle",
			options = ScaleMode.entries.map { frReaderScaleLabel(it) },
			selectedIndex = ScaleMode.entries.indexOf(scaleMode),
			onDismiss = { openDialog = null },
			onSelect = {
				scaleMode = ScaleMode.entries[it]
				settings.readerScaleMode = scaleMode
			},
		)
		ReaderDialog.ANIMATION -> RadioDialog(
			title = "Animation de page",
			options = ReaderAnimation.entries.map { frReaderAnimationLabel(it) },
			selectedIndex = ReaderAnimation.entries.indexOf(animation),
			onDismiss = { openDialog = null },
			onSelect = {
				animation = ReaderAnimation.entries[it]
				settings.readerAnimation = animation
			},
		)
		ReaderDialog.BACKGROUND -> RadioDialog(
			title = "Arrière-plan",
			options = ReaderBackground.entries.map { frReaderBackgroundLabel(it) },
			selectedIndex = ReaderBackground.entries.indexOf(background),
			onDismiss = { openDialog = null },
			onSelect = {
				background = ReaderBackground.entries[it]
				settings.readerBackground = background
			},
		)
		ReaderDialog.PRELOAD -> RadioDialog(
			title = "Précharger les pages",
			options = listOf(NetworkPolicy.ALWAYS, NetworkPolicy.NON_METERED, NetworkPolicy.NEVER)
				.map { frNetworkPolicyLabel(it) },
			selectedIndex = when (preloadPolicy) {
				NetworkPolicy.ALWAYS -> 0
				NetworkPolicy.NON_METERED -> 1
				NetworkPolicy.NEVER -> 2
			},
			onDismiss = { openDialog = null },
			onSelect = {
				preloadPolicy = when (it) {
					0 -> NetworkPolicy.ALWAYS
					1 -> NetworkPolicy.NON_METERED
					else -> NetworkPolicy.NEVER
				}
				settings.pagesPreloadPolicy = preloadPolicy
			},
		)
		null -> {}
	}
}

/** Dialogs radio ouverts depuis l'écran Paramètres du lecteur. */
private enum class ReaderDialog {
	MODE, SCALE, ANIMATION, BACKGROUND, PRELOAD,
}
