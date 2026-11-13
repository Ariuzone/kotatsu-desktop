/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-07 — phase P2 : écran
 * « Principales sections de l'écran » (captures frames 2:05-2:10 de la vidéo) : liste des
 * sections du rail avec réordonnancement et suppression, ligne « Ajouter » ouvrant le dialog
 * du même nom. Adaptation desktop : boutons flèche ↑/↓ à la place du geste de glissement
 * (divergence documentée dans docs/UI_DIVERGENCES.md). Ordre persisté via
 * AppSettings.mainSections (clé main_sections_order) et appliqué au rail immédiatement.
 * Modification : 2026-11-08 — phase P2 : dialog « Ajouter » aligné sur la capture (~2:07) :
 * options exactes « Sur l'appareil, Suggestions, Mis à jour, Marque-pages » + « Annuler » ;
 * « Suggestions » et « Mis à jour » affichées mais désactivées (écrans portés en P4).
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.ui.nav.NavItem
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_ADD
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_REMOVE
import org.koitharu.kotatsu.desktop.ui.strings.FR_BOOKMARKS
import org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_MAIN_SECTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_ON_DEVICE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_UPDATES
import org.koitharu.kotatsu.desktop.ui.strings.currentUiLang
import org.koitharu.kotatsu.desktop.ui.strings.frNavItemLabel

/** Chaîne dépendant de la langue d'interface courante (P9 : zéro chaîne FR-only en anglais). */
private fun t(fr: String, en: String): String =
	if (currentUiLang() == org.koitharu.kotatsu.desktop.ui.strings.UiLang.EN) en else fr

@Composable
public fun SectionsSettingsScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
) {
	// Ordre courant (noms NavItem) ; le rail lit la préférence au retour
	var sections by remember { mutableStateOf(appSettings.mainSections) }
	var showAddDialog by remember { mutableStateOf(false) }

	fun persist(newList: List<String>) {
		sections = newList
		appSettings.mainSections = newList
	}

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_MAIN_SECTIONS,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			itemsIndexed(sections, key = { _, name -> name }) { index, name ->
				val item = runCatching { NavItem.valueOf(name) }.getOrNull()
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 2.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Icon(
						imageVector = item?.filledIcon ?: Icons.Outlined.Tune,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Text(
						text = item?.let { frNavItemLabel(it) } ?: name,
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier
							.weight(1f)
							.padding(horizontal = 16.dp),
					)
					// Adaptation desktop : ↑/↓ au lieu du geste de glissement Android
					IconButton(
						onClick = {
							if (index > 0) {
								persist(sections.toMutableList().apply {
									add(index - 1, removeAt(index))
								})
							}
						},
						enabled = index > 0,
					) {
						Icon(Icons.Filled.KeyboardArrowUp, contentDescription = t("Monter", "Move up"))
					}
					IconButton(
						onClick = {
							if (index < sections.lastIndex) {
								persist(sections.toMutableList().apply {
									add(index + 1, removeAt(index))
								})
							}
						},
						enabled = index < sections.lastIndex,
					) {
						Icon(Icons.Filled.KeyboardArrowDown, contentDescription = t("Descendre", "Move down"))
					}
					IconButton(
						onClick = { persist(sections.filterIndexed { i, _ -> i != index }) },
					) {
						Icon(
							Icons.Outlined.Delete,
							contentDescription = FR_ACTION_REMOVE,
							tint = MaterialTheme.colorScheme.error,
						)
					}
				}
				PreferenceDivider()
			}
			item {
				// Ligne « Ajouter » (capture : dialog Sur l'appareil / Suggestions / Mis à jour / Marque-pages)
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { showAddDialog = true }
						.padding(horizontal = 16.dp, vertical = 12.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Icon(
						imageVector = Icons.Filled.Add,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
					)
					Text(
						text = FR_ADD,
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier.padding(start = 16.dp),
					)
				}
			}
		}
	}

	if (showAddDialog) {
		// Options exactes du dialog Android (capture ~2:07) : « Sur l'appareil, Suggestions,
		// Mis à jour, Marque-pages ». « Suggestions » et « Mis à jour » (écrans P4) sont
		// affichées mais désactivées, faute d'écran associé sur desktop (divergence documentée).
		val addOptions: List<Pair<String, NavItem?>> = listOf(
			FR_ON_DEVICE to NavItem.LOCAL,
			FR_SUGGESTIONS to null,
			FR_UPDATES to null,
			FR_BOOKMARKS to NavItem.BOOKMARKS,
		)
		AlertDialog(
			onDismissRequest = { showAddDialog = false },
			title = { Text(text = FR_ADD) },
			text = {
				Column {
					if (addOptions.none { (_, item) -> item != null && item.name !in sections }) {
						Text(
							text = t(
								"Toutes les sections disponibles sont déjà affichées",
								"All available sections are already shown",
							),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
					addOptions.forEach { (label, item) ->
						val alreadyShown = item != null && item.name in sections
						val enabled = item != null && !alreadyShown
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable(enabled = enabled) {
									item?.let {
										persist(sections + it.name)
									}
									showAddDialog = false
								}
								.padding(vertical = 10.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							Icon(
								imageVector = item?.filledIcon ?: Icons.Outlined.Info,
								contentDescription = null,
								tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
								else MaterialTheme.colorScheme.outline,
							)
							Text(
								text = label,
								style = MaterialTheme.typography.bodyLarge,
								color = if (enabled) MaterialTheme.colorScheme.onSurface
								else MaterialTheme.colorScheme.outline,
								modifier = Modifier.padding(start = 16.dp),
							)
						}
					}
				}
			},
			confirmButton = {},
			dismissButton = {
				TextButton(onClick = { showAddDialog = false }) {
					Text(text = FR_CANCEL)
				}
			},
		)
	}
}
