/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P3 : dialogue
 * « Nouveau préréglage » (créateur de préréglages de sources, vidéo t≈5:30) : barre du haut
 * (✕, titre, « Annuler », « Enregistrer » désactivé tant qu'aucune langue n'est choisie) et
 * chips de langues natives (« Español », « Français », « Italiano », …) construites depuis les
 * locales des sources du registry. L'enregistrement est délégué au parent via [onSave]
 * (persistance SourcePresetsDao côté SourcePresetsScreen — voir ce fichier).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 */
package org.koitharu.kotatsu.desktop.ui.screens.sources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_PRESET
import org.koitharu.kotatsu.desktop.ui.strings.FR_SAVE
import org.koitharu.kotatsu.parsers.model.MangaParserSource

/**
 * Créateur de préréglages (vidéo t≈5:30) : sélection d'une ou plusieurs langues parmi les
 * locales du registry, « Enregistrer » désactivé tant que rien n'est choisi, « Annuler »/✕
 * referment sans rien enregistrer.
 *
 * @param onSave appelé à l'enregistrement avec un titre par défaut (« Français + English »)
 * et la liste des codes de langue choisis (« fr », « en », …) ; la persistance est à la charge
 * du parent (SourcePresetsDao — même backend Room que côté Android).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun PresetCreatorDialog(
	onDismiss: () -> Unit,
	onSave: (title: String, languages: List<String>) -> Unit,
	modifier: Modifier = Modifier,
) {
	// Locales du registry (sources non cassées), affichées dans leur langue native comme la vidéo
	val locales = remember {
		MangaParserSource.entries.filterNot { it.isBroken }
			.map { it.locale }
			.filter { it.isNotBlank() }
			.distinct()
			.sortedBy { localeLabel(it).lowercase(Locale.ROOT) }
	}
	var selected by remember { mutableStateOf(setOf<String>()) }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Surface(
			modifier = modifier
				.fillMaxSize()
				.padding(24.dp),
			shape = RoundedCornerShape(16.dp),
			color = MaterialTheme.colorScheme.surfaceContainerHigh,
		) {
			Column(modifier = Modifier.fillMaxSize()) {
				// Barre du haut : ✕  « Nouveau préréglage »  [Annuler] [Enregistrer]
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp)
						.padding(horizontal = 4.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					IconButton(onClick = onDismiss) {
						Icon(Icons.Filled.Close, contentDescription = FR_CANCEL)
					}
					Text(
						text = FR_NEW_PRESET,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.weight(1f),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					TextButton(onClick = onDismiss) {
						Text(text = FR_CANCEL)
					}
					TextButton(
						onClick = {
							val languages = selected.toList()
							val title = languages.joinToString(" + ") { localeLabel(it) }
							onSave(title, languages)
						},
						enabled = selected.isNotEmpty(),
					) {
						Text(text = FR_SAVE)
					}
				}
				// Chips de langues natives (vidéo : Español, Français, Indonesia, Italiano, …)
				FlowRow(
					modifier = Modifier
						.fillMaxSize()
						.verticalScroll(rememberScrollState())
						.padding(horizontal = 16.dp, vertical = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					for (locale in locales) {
						FilterChip(
							selected = locale in selected,
							onClick = {
								selected = if (locale in selected) {
									selected - locale
								} else {
									selected + locale
								}
							},
							label = { Text(localeLabel(locale)) },
						)
					}
				}
			}
		}
	}
}

/** Libellé de langue dans sa propre langue (« Русский », « Français »…) — cf. SourcesCatalogScreen. */
private fun localeLabel(locale: String): String {
	val l = runCatching { Locale.forLanguageTag(locale) }.getOrNull() ?: return locale
	val display = l.getDisplayLanguage(l).ifBlank { locale }
	return display.replaceFirstChar { it.uppercase(l) }
}
