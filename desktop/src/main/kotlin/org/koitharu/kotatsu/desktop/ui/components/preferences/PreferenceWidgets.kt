/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — composants de
 * préférences réutilisables, reproduits des layouts d'origine :
 *  - ligne de préférence icône + titre + sous-titre + contrôle à droite (pref_*.xml)
 *  - titre de section (couleur primaire, comme les headers des PreferenceFragment)
 *  - dialogs radio et multi-choix avec bouton « Annuler » (strings cancel / ok)
 * Sert de socle aux phases P0–P6 du plan d'implémentation.
 */
package org.koitharu.kotatsu.desktop.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK

private val PREF_ROW_MIN_HEIGHT = 64.dp // min 2 lignes (pref d'origine)
private val PREF_HORIZONTAL_PADDING = 16.dp
private val PREF_ICON_SIZE = 24.dp

/** Ligne de préférence cliquable avec chevron droit (PreferenceScreen d'origine). */
@Composable
public fun PreferenceRow(
	icon: ImageVector,
	title: String,
	subtitle: String? = null,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	onClick: () -> Unit = {},
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.heightIn(min = PREF_ROW_MIN_HEIGHT)
			.clickable(enabled = enabled, onClick = onClick)
			.padding(horizontal = PREF_HORIZONTAL_PADDING, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
			modifier = Modifier.size(PREF_ICON_SIZE),
		)
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = PREF_HORIZONTAL_PADDING),
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
			)
			if (!subtitle.isNullOrEmpty()) {
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
		Icon(
			imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(PREF_ICON_SIZE),
		)
	}
}

/** Ligne de préférence avec interrupteur à droite (SwitchPreferenceCompat d'origine). */
@Composable
public fun PreferenceSwitchRow(
	icon: ImageVector,
	title: String,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	subtitle: String? = null,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.heightIn(min = PREF_ROW_MIN_HEIGHT)
			.clickable(enabled = enabled) { onCheckedChange(!checked) }
			.padding(horizontal = PREF_HORIZONTAL_PADDING, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(PREF_ICON_SIZE),
		)
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = PREF_HORIZONTAL_PADDING),
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				color = if (enabled) MaterialTheme.colorScheme.onSurface
				else MaterialTheme.colorScheme.onSurfaceVariant,
			)
			if (!subtitle.isNullOrEmpty()) {
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange,
			enabled = enabled,
		)
	}
}

/** Titre de section (header des PreferenceFragment d'origine, couleur primaire). */
@Composable
public fun PreferenceCategoryHeader(
	title: String,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.padding(horizontal = PREF_HORIZONTAL_PADDING, vertical = 8.dp),
		)
		HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
	}
}

/** Séparateur entre lignes de préférences (diviseurs fins des listes d'origine). */
@Composable
public fun PreferenceDivider(modifier: Modifier = Modifier) {
	HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = modifier)
}

/**
 * Dialog radio (DialogFragment d'origine, ex. « Précharger les pages », « Afficher les
 * indicateurs de progression de lecture ») : options exclusives + bouton « Annuler ».
 */
@Composable
public fun RadioDialog(
	title: String,
	options: List<String>,
	selectedIndex: Int,
	onDismiss: () -> Unit,
	onSelect: (Int) -> Unit,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = title) },
		text = {
			Column {
				options.forEachIndexed { index, label ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								onSelect(index)
								onDismiss()
							},
						verticalAlignment = Alignment.CenterVertically,
					) {
						RadioButton(selected = index == selectedIndex, onClick = null)
						Text(
							text = label,
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.padding(start = 8.dp),
						)
					}
				}
			}
		},
		confirmButton = {},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = FR_CANCEL)
			}
		},
	)
}

/**
 * Dialog multi-choix à cases (DialogFragment d'origine, ex. « Rechercher les mises à jour »)
 * avec boutons « Annuler » / « OK ».
 */
@Composable
public fun MultiChoiceDialog(
	title: String,
	options: List<String>,
	checked: List<Boolean>,
	onDismiss: () -> Unit,
	onCheckedChange: (Int, Boolean) -> Unit,
	onConfirm: () -> Unit,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = title) },
		text = {
			Column {
				options.forEachIndexed { index, label ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onCheckedChange(index, !checked[index]) },
						verticalAlignment = Alignment.CenterVertically,
					) {
						Checkbox(checked = checked[index], onCheckedChange = null)
						Text(
							text = label,
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.padding(start = 8.dp),
						)
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(text = FR_OK)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = FR_CANCEL)
			}
		},
	)
}
