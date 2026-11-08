/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — phase P1 : écran
 * « Images masquées » (capture frame ~1:30 de la vidéo) : état vide avec le texte exact
 * « Rien de masqué pour l'instant. Frottez une image du pouce pendant la lecture pour la
 * masquer ». La gomme (effacement d'une image) est prévue en P7 — l'écran est déjà le bon.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_IMAGES_MASKED
import org.koitharu.kotatsu.desktop.ui.strings.FR_IMAGES_MASKED_EMPTY

@Composable
public fun MaskedImagesScreen(
	onBackClick: () -> Unit,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_IMAGES_MASKED,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		EmptyStateScreen(
			icon = Icons.Outlined.SearchOff,
			textPrimary = FR_IMAGES_MASKED_EMPTY,
		)
	}
}
