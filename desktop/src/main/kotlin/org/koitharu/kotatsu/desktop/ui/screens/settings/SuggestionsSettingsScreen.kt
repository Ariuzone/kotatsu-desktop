/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P4 : écran
 * « Suggestions » (vidéo de référence, t≈2:40-3:05) : bannière interrupteur bleue « Activer les
 * suggestions », « Inclure les sources désactivées », « Exclure des genres » (dialog avec champ
 * de saisie + chips des genres déjà exclus, croix de suppression) et note d'information
 * « Toutes les données sont analysées localement... ». Préférences persistées via
 * AppSettings.isSuggestionsEnabled / isSuggestionsIncludeDisabled / excludedSuggestionGenres.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS

// Chaînes FR de l'écran (vidéo t≈2:40-3:05)
private const val FR_SUGGESTIONS_ENABLE = "Activer les suggestions"
private const val FR_SUGGESTIONS_INCLUDE_DISABLED = "Inclure les sources désactivées"
private const val FR_SUGGESTIONS_INCLUDE_DISABLED_SUMMARY =
	"Afficher les suggestions de toutes les sources de mangas, y compris celles désactivées"
private const val FR_SUGGESTIONS_EXCLUDE_GENRES = "Exclure des genres"
private const val FR_SUGGESTIONS_EXCLUDE_GENRES_SUMMARY =
	"Spécifiez les genres que vous ne voulez pas voir apparaître dans les suggestions"
private const val FR_SUGGESTIONS_LOCAL_NOTE =
	"Toutes les données sont analysées localement sur cet appareil et ne sont jamais envoyées ailleurs."
private const val FR_SUGGESTIONS_GENRE_HINT = "Nom du genre"

// Bannière interrupteur : carte arrondie pleine largeur (bannière bleue de la vidéo)
private val BANNER_HORIZONTAL_PADDING = 16.dp
private val BANNER_VERTICAL_PADDING = 12.dp
private val BANNER_CORNER_RADIUS = 16.dp
private val SCREEN_HORIZONTAL_PADDING = 8.dp
private val NOTE_ICON_SIZE = 20.dp

/**
 * Écran « Suggestions » (Paramètres > Trouver des mangas > Suggestions) : interrupteur global,
 * inclusion des sources désactivées, genres exclus (dialog avec saisie + chips) et note locale.
 */
@Composable
public fun SuggestionsSettingsScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
) {
	// État local miroir des préférences (AppSettings n'est pas observable) ; écriture immédiate
	var suggestionsEnabled by remember { mutableStateOf(appSettings.isSuggestionsEnabled) }
	var includeDisabled by remember { mutableStateOf(appSettings.isSuggestionsIncludeDisabled) }
	var excludedGenres by remember { mutableStateOf(appSettings.excludedSuggestionGenres) }
	var showGenresDialog by remember { mutableStateOf(false) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_SUGGESTIONS,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			// 1. Bannière interrupteur bleue pleine largeur « Activer les suggestions »
			SwitchBanner(
				title = FR_SUGGESTIONS_ENABLE,
				checked = suggestionsEnabled,
				onCheckedChange = {
					suggestionsEnabled = it
					appSettings.isSuggestionsEnabled = it
				},
				modifier = Modifier.padding(SCREEN_HORIZONTAL_PADDING),
			)

			// 2. « Inclure les sources désactivées »
			PreferenceSwitchRow(
				icon = Icons.Outlined.Source,
				title = FR_SUGGESTIONS_INCLUDE_DISABLED,
				subtitle = FR_SUGGESTIONS_INCLUDE_DISABLED_SUMMARY,
				checked = includeDisabled,
				onCheckedChange = {
					includeDisabled = it
					appSettings.isSuggestionsIncludeDisabled = it
				},
			)
			PreferenceDivider()

			// 3. « Exclure des genres » → dialog de saisie
			PreferenceRow(
				icon = Icons.Outlined.Style,
				title = FR_SUGGESTIONS_EXCLUDE_GENRES,
				subtitle = FR_SUGGESTIONS_EXCLUDE_GENRES_SUMMARY,
				onClick = { showGenresDialog = true },
			)
			PreferenceDivider()

			// 4. Note locale (icône i, texte secondaire, en bas de l'écran dans la vidéo)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = SCREEN_HORIZONTAL_PADDING, vertical = 12.dp),
				verticalAlignment = Alignment.Top,
			) {
				Icon(
					imageVector = Icons.Outlined.Info,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(top = 2.dp),
				)
				Text(
					text = FR_SUGGESTIONS_LOCAL_NOTE,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(start = 12.dp),
				)
			}
		}
	}

	if (showGenresDialog) {
		ExcludedGenresDialog(
			excludedGenres = excludedGenres,
			onDismiss = { showGenresDialog = false },
			onAddGenre = { genre ->
				excludedGenres = excludedGenres + genre
				appSettings.excludedSuggestionGenres = excludedGenres
			},
			onRemoveGenre = { genre ->
				excludedGenres = excludedGenres - genre
				appSettings.excludedSuggestionGenres = excludedGenres
			},
		)
	}
}

/**
 * Bannière interrupteur pleine largeur (aucun composant équivalent dans PreferenceWidgets) :
 * Surface arrondie colorée en primaryContainer quand activée (bannière bleue de la vidéo),
 * surfaceVariant sinon ; le clic sur la ligne bascule l'interrupteur.
 */
@Composable
private fun SwitchBanner(
	title: String,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(BANNER_CORNER_RADIUS),
		color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = BANNER_HORIZONTAL_PADDING, vertical = BANNER_VERTICAL_PADDING),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.weight(1f),
			)
			Switch(
				checked = checked,
				onCheckedChange = onCheckedChange,
			)
		}
	}
}

/**
 * Dialog « Exclure des genres » : champ de saisie (OK ajoute le genre, Annuler ferme) et chips
 * des genres déjà exclus avec croix de suppression ; chaque ajout/suppression persiste
 * immédiatement dans AppSettings.excludedSuggestionGenres.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExcludedGenresDialog(
	excludedGenres: Set<String>,
	onDismiss: () -> Unit,
	onAddGenre: (String) -> Unit,
	onRemoveGenre: (String) -> Unit,
) {
	var input by remember { mutableStateOf("") }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = FR_SUGGESTIONS_EXCLUDE_GENRES) },
		text = {
			Column {
				OutlinedTextField(
					value = input,
					onValueChange = { input = it },
					singleLine = true,
					label = { Text(text = FR_SUGGESTIONS_GENRE_HINT) },
					modifier = Modifier.fillMaxWidth(),
				)
				if (excludedGenres.isNotEmpty()) {
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 12.dp),
					) {
						excludedGenres.forEach { genre ->
							InputChip(
								selected = false,
								onClick = { onRemoveGenre(genre) },
								label = { Text(text = genre) },
								trailingIcon = {
									Icon(
										imageVector = Icons.Filled.Close,
										contentDescription = null,
										modifier = Modifier.size(NOTE_ICON_SIZE),
									)
								},
							)
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					val genre = input.trim()
					if (genre.isNotEmpty() && genre !in excludedGenres) {
						onAddGenre(genre)
					}
					input = ""
					onDismiss()
				},
			) {
				Text(text = FR_OK)
			}
		},
		dismissButton = {
			TextButton(
				onClick = {
					input = ""
					onDismiss()
				},
			) {
				Text(text = FR_CANCEL)
			}
		},
	)
}
