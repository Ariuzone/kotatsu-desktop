/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P2 :
 * nouveau fichier — bottom sheet « Options de liste » (ListOptionsBottomSheet d'origine,
 * ouverte depuis le menu ⋮ des écrans Historique / Favoris ; cf. vidéo de référence, frame ~0:30).
 *
 * Contenu fidèle à la vidéo : poignée de glissement, section « Mode liste » (3 segments
 * Compact / Détails / Grille), « Taille de la grille » (slider 50..200 avec valeur « N % »
 * à droite), « Ordre de tri » (menu déroulant « Dernier lu » / « Par nom »), « Groupe »
 * (interrupteur — la vidéo montre un interrupteur activé, pas un dropdown).
 *
 * Chaque interaction est appliquée IMMÉDIATEMENT dans AppSettings (comme l'original :
 * pas de bouton « valider »). Câblage : listMode et gridSize via les clés d'origine
 * (list_mode_2, grid_size) ; tri persisté via historySortOrder (clé history_order, l'app
 * desktop n'a pas encore le module list/portage ListSortOrder) et appliqué au chargement
 * de l'historique dans Main ; « Groupe » persisté via isHistoryGroupingEnabled
 * (clé history_grouping, ajoutée dans AppSettings).
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.ListMode
import org.koitharu.kotatsu.desktop.ui.strings.FR_GRID_SIZE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SORT_ORDER
import org.koitharu.kotatsu.desktop.ui.strings.currentUiLang
import org.koitharu.kotatsu.desktop.ui.strings.frListModeLabel
import org.koitharu.kotatsu.parsers.model.SortOrder

/** Chaîne dépendant de la langue d'interface courante (P9 : zéro chaîne FR-only en anglais). */
private fun t(fr: String, en: String): String =
	if (currentUiLang() == org.koitharu.kotatsu.desktop.ui.strings.UiLang.EN) en else fr

// strings.xml d'origine (values-fr) — « Ordre de tri » est déjà une constante i18n
// (FR_SORT_ORDER, FrSources.kt) ; les trois autres libellés passent par le pattern t() (P9).
// Anciennes clés : list_sort_last_read « Dernier lu », list_sort_name « Par nom », group « Groupe ».

private val SHEET_HORIZONTAL_PADDING = 16.dp
private val SHEET_BOTTOM_PADDING = 24.dp
private val SECTION_SPACING = 20.dp
private val CONTROL_SPACING = 8.dp
private val GRID_SIZE_MIN = 50
private val GRID_SIZE_MAX = 200

/**
 * État partagé de la feuille « Options de liste » : conservé par l'appelant (Main) pour que
 * les écrans (Historique) se recomposent dès qu'une valeur change. Chaque écriture est
 * répercutée immédiatement dans [AppSettings] (pas de bouton valider, comme l'original).
 */
public class ListOptionsState(private val settings: AppSettings) {

	private var listModeState by mutableStateOf(settings.listMode)
	private var gridSizeState by mutableStateOf(settings.gridSize.coerceIn(GRID_SIZE_MIN, GRID_SIZE_MAX))
	private var sortOrderState by mutableStateOf(settings.historySortOrder)
	private var isGroupingEnabledState by mutableStateOf(settings.isHistoryGroupingEnabled)

	/** Mode d'affichage des listes (Historique) — clé list_mode_2. */
	public var listMode: ListMode
		get() = listModeState
		set(value) {
			listModeState = value
			settings.listMode = value
		}

	/** « Taille de la grille » en % (50..200) — clé grid_size. */
	public var gridSize: Int
		get() = gridSizeState
		set(value) {
			gridSizeState = value.coerceIn(GRID_SIZE_MIN, GRID_SIZE_MAX)
			settings.gridSize = gridSizeState
		}

	/**
	 * « Ordre de tri » — persisté via [AppSettings.historySortOrder] (clé history_order) :
	 * mapping « Dernier lu » -> NEWEST, « Par nom » -> ALPHABETICAL. Le tri effectif de la
	 * liste d'historique n'est pas encore branché (cf. rapport, lot P2).
	 */
	public var sortOrder: SortOrder
		get() = sortOrderState
		set(value) {
			sortOrderState = value
			settings.historySortOrder = value
		}

	/**
	 * « Groupe » (regrouper l'historique par date de dernière lecture) — persisté via
	 * [AppSettings.isHistoryGroupingEnabled] (clé history_grouping) : survit au redémarrage.
	 */
	public var isGroupingEnabled: Boolean
		get() = isGroupingEnabledState
		set(value) {
			isGroupingEnabledState = value
			settings.isHistoryGroupingEnabled = value
		}
}

/** Libellé (FR/EN) d'un ordre de tri proposé dans la feuille. */
private fun sortLabel(order: SortOrder): String = when (order) {
	SortOrder.ALPHABETICAL, SortOrder.ALPHABETICAL_DESC -> t("Par nom", "By name")
	else -> t("Dernier lu", "Last read") // défaut / NEWEST : « Dernier lu »
}

/** Ordres de tri proposés (comme l'original : dernier lu, par nom). */
private val SORT_OPTIONS = listOf(SortOrder.NEWEST, SortOrder.ALPHABETICAL)

/**
 * Bottom sheet « Options de liste » (ListOptionsBottomSheet d'origine) : mode liste
 * (segments), taille de la grille (slider + « N % »), ordre de tri (dropdown), groupe
 * (interrupteur). Les changements sont appliqués directement à chaque interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ListOptionsSheet(
	state: ListOptionsState,
	onDismiss: () -> Unit,
) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = SHEET_HORIZONTAL_PADDING)
				.padding(bottom = SHEET_BOTTOM_PADDING),
			verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
		) {
			// --- « Mode liste » : 3 segments Compact / Détails / Grille ---
			Column(verticalArrangement = Arrangement.spacedBy(CONTROL_SPACING)) {
				Text(
					text = FR_LIST_MODE,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
					val modes = ListMode.entries
					for ((index, mode) in modes.withIndex()) {
						SegmentedButton(
							selected = state.listMode == mode,
							onClick = { state.listMode = mode },
							shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
							label = { Text(text = frListModeLabel(mode), maxLines = 1) },
						)
					}
				}
			}

			// --- « Taille de la grille » : slider 50..200 + valeur « N % » à droite ---
			Column(verticalArrangement = Arrangement.spacedBy(CONTROL_SPACING)) {
				Text(
					text = FR_GRID_SIZE,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Row(verticalAlignment = Alignment.CenterVertically) {
					Slider(
						value = state.gridSize.toFloat(),
						onValueChange = { state.gridSize = it.toInt() },
						valueRange = GRID_SIZE_MIN.toFloat()..GRID_SIZE_MAX.toFloat(),
						modifier = Modifier.weight(1f),
					)
					Text(
						text = PERCENT_PATTERN.format(state.gridSize),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(start = CONTROL_SPACING),
						maxLines = 1,
					)
				}
			}

			// --- « Ordre de tri » : dropdown « Dernier lu » / « Par nom » ---
			var sortOpen by remember { mutableStateOf(false) }
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(CONTROL_SPACING),
			) {
				Text(
					text = FR_SORT_ORDER,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				Box {
					TextButton(
						onClick = { sortOpen = true },
						colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
					) {
						Text(
							text = sortLabel(state.sortOrder),
							style = MaterialTheme.typography.bodyLarge,
							maxLines = 1,
						)
						Icon(
							imageVector = Icons.Filled.ArrowDropDown,
							contentDescription = null,
						)
					}
					DropdownMenu(
						expanded = sortOpen,
						onDismissRequest = { sortOpen = false },
					) {
						for (order in SORT_OPTIONS) {
							DropdownMenuItem(
								text = { Text(text = sortLabel(order)) },
								onClick = {
									state.sortOrder = order
									sortOpen = false
								},
							)
						}
					}
				}
			}

			// --- « Groupe » : interrupteur (cf. vidéo, frame ~0:30 : interrupteur activé) ---
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(CONTROL_SPACING),
			) {
				Text(
					text = t("Groupe", "Group"),
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				Switch(
					checked = state.isGroupingEnabled,
					onCheckedChange = { state.isGroupingEnabled = it },
				)
			}
		}
	}
}

private const val PERCENT_PATTERN = "%d %%"
