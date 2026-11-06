/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — les illustrations
 * vectorielles d'origine (ic_empty_history, ic_empty_favourites, ic_empty_feed, ic_empty_common)
 * sont désormais portées en ImageVector (voir TotoroVectors.kt) ; la variante par icône Material
 * est conservée pour compatibilité.
 *
 * État vide — reproduit item_empty_state.xml : colonne centrée, illustration 192dp,
 * texte primaire TitleLarge, texte secondaire BodyMedium, bouton tonal optionnel
 * (button_retry), padding horizontal 32dp.
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val EMPTY_ICON_SIZE = 192.dp
private val EMPTY_HORIZONTAL_PADDING = 32.dp
private val EMPTY_SPACING = 16.dp // margin_normal

/**
 * État vide commun (item_empty_state.xml). [iconTint] : teinte douce, les illustrations
 * d'origine étant monochromes/atténuées.
 */
@Composable
public fun EmptyStateView(
	icon: ImageVector,
	textPrimary: String,
	modifier: Modifier = Modifier,
	textSecondary: String? = null,
	actionText: String? = null,
	onActionClick: (() -> Unit)? = null,
	iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = EMPTY_HORIZONTAL_PADDING),
		contentAlignment = Alignment.Center,
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Box(
				modifier = Modifier.size(EMPTY_ICON_SIZE),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = iconTint,
					modifier = Modifier.size(EMPTY_ICON_SIZE / 2),
				)
			}
			Text(
				text = textPrimary,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = EMPTY_SPACING),
			)
			if (!textSecondary.isNullOrEmpty()) {
				Text(
					text = textSecondary,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(top = EMPTY_SPACING),
				)
			}
			if (!actionText.isNullOrEmpty() && onActionClick != null) {
				FilledTonalButton(
					onClick = onActionClick,
					colors = ButtonDefaults.filledTonalButtonColors(),
					modifier = Modifier.padding(top = EMPTY_SPACING),
				) {
					Text(text = actionText)
				}
			}
		}
	}
}

/** État vide plein écran, centré verticalement comme item_empty_state.xml (gravity=center). */
@Composable
public fun EmptyStateScreen(
	icon: ImageVector,
	textPrimary: String,
	modifier: Modifier = Modifier,
	textSecondary: String? = null,
	actionText: String? = null,
	onActionClick: (() -> Unit)? = null,
	iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
	Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		EmptyStateView(
			icon = icon,
			textPrimary = textPrimary,
			textSecondary = textSecondary,
			actionText = actionText,
			onActionClick = onActionClick,
			iconTint = iconTint,
		)
	}
}

/**
 * État vide avec l'illustration Totoro portée des drawables d'origine (voir TotoroVectors.kt).
 * Rendue à 192dp comme item_empty_state.xml (l'illustration d'origine fait 200dp).
 */
@Composable
public fun EmptyStateView(
	illustration: TotoroIllustration,
	textPrimary: String,
	modifier: Modifier = Modifier,
	textSecondary: String? = null,
	actionText: String? = null,
	onActionClick: (() -> Unit)? = null,
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = EMPTY_HORIZONTAL_PADDING),
		contentAlignment = Alignment.Center,
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Image(
				imageVector = illustration.vector(),
				contentDescription = null,
				modifier = Modifier.size(EMPTY_ICON_SIZE),
			)
			Text(
				text = textPrimary,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = EMPTY_SPACING),
			)
			if (!textSecondary.isNullOrEmpty()) {
				Text(
					text = textSecondary,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(top = EMPTY_SPACING),
				)
			}
			if (!actionText.isNullOrEmpty() && onActionClick != null) {
				FilledTonalButton(
					onClick = onActionClick,
					colors = ButtonDefaults.filledTonalButtonColors(),
					modifier = Modifier.padding(top = EMPTY_SPACING),
				) {
					Text(text = actionText)
				}
			}
		}
	}
}

/** État vide plein écran (illustration Totoro), centré verticalement comme item_empty_state.xml. */
@Composable
public fun EmptyStateScreen(
	illustration: TotoroIllustration,
	textPrimary: String,
	modifier: Modifier = Modifier,
	textSecondary: String? = null,
	actionText: String? = null,
	onActionClick: (() -> Unit)? = null,
) {
	Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		EmptyStateView(
			illustration = illustration,
			textPrimary = textPrimary,
			textSecondary = textSecondary,
			actionText = actionText,
			onActionClick = onActionClick,
		)
	}
}
