/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — barre de recherche
 * pill des écrans principaux (« Rechercher un manga », loupe à gauche, menu ⋮ à droite),
 * fidèle aux captures de référence. Le menu reprend les entrées des menus d'origine des
 * écrans Historique / Favoris / Discover / Sources / Flux (checks « Mode incognito » inclus).
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_MANGA

private val SEARCH_BAR_HEIGHT = 56.dp
private val SEARCH_BAR_CORNER = 28.dp
private val SEARCH_BAR_HORIZONTAL_MARGIN = 16.dp

/**
 * Action du menu ⋮ de la barre de recherche. [checkable] rend une case à cocher à droite
 * (comme « Mode incognito » dans l'app d'origine) ; l'état [checked] est possédé par l'appelant.
 */
public data class TopSearchMenuAction(
	public val label: String,
	public val onClick: () -> Unit,
	public val checkable: Boolean = false,
	public val checked: Boolean = false,
)

/**
 * Barre de recherche pill des écrans principaux (captures de référence) : loupe + placeholder
 * « Rechercher un manga » + ⋮. Un clic sur la zone ouvre la recherche ([onSearchClick]).
 * [trailingIcon] : icône supplémentaire avant le ⋮ (ex. icône sources de l'onglet Source).
 */
@Composable
public fun TopSearchBar(
	onSearchClick: () -> Unit,
	modifier: Modifier = Modifier,
	menuActions: List<TopSearchMenuAction> = emptyList(),
	trailingIcon: ImageVector? = null,
	onTrailingIconClick: (() -> Unit)? = null,
) {
	var menuOpen by remember { mutableStateOf(false) }
	val pillShape = CircleShape

	Box(modifier = modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = SEARCH_BAR_HORIZONTAL_MARGIN)
				.height(SEARCH_BAR_HEIGHT)
				.background(MaterialTheme.colorScheme.surfaceContainerHighest, pillShape)
				.clickable(onClick = onSearchClick),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				imageVector = Icons.Filled.Search,
				contentDescription = FR_SEARCH_MANGA,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(start = 20.dp).size(24.dp),
			)
			Text(
				text = FR_SEARCH_MANGA,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier
					.weight(1f)
					.padding(horizontal = 16.dp),
				maxLines = 1,
			)
			if (trailingIcon != null && onTrailingIconClick != null) {
				Icon(
					imageVector = trailingIcon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.padding(end = 4.dp)
						.size(24.dp)
						.clickable(onClick = onTrailingIconClick),
				)
			}
			IconButton(
				onClick = { menuOpen = true },
				modifier = Modifier.padding(end = 4.dp),
			) {
				Icon(
					imageVector = Icons.Filled.MoreVert,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
		DropdownMenu(
			expanded = menuOpen,
			onDismissRequest = { menuOpen = false },
			modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp)),
		) {
			for (action in menuActions) {
				DropdownMenuItem(
					text = { Text(text = action.label) },
					onClick = {
						menuOpen = false
						action.onClick()
					},
					trailingIcon = if (action.checkable) {
						{ CheckBoxBox(checked = action.checked) }
					} else {
						null
					},
				)
			}
		}
	}
}

/** Case à cocher du menu (case carrée arrondie, comme l'app d'origine). */
@Composable
private fun CheckBoxBox(checked: Boolean) {
	Box(
		modifier = Modifier
			.padding(start = 16.dp)
			.size(18.dp)
			.border(
				width = 2.dp,
				color = if (checked) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.onSurfaceVariant
				},
				shape = RoundedCornerShape(3.dp),
			)
			.background(
				if (checked) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
				RoundedCornerShape(3.dp),
			),
	)
}
