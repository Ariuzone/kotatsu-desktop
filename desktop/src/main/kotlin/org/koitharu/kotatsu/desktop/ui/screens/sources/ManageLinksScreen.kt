/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P3 : écran
 * « Gérer les liens » (ManageLinksFragment / entrée « Gérer les liens » des préférences sources
 * d'origine). Adaptation desktop MINIMALE : sur Android, cet écran gère les intents deep-links
 * (« liens vers des mangas à partir d'applications externes ») — un mécanisme sans équivalent
 * direct sur bureau. Ici : top bar retour + texte explicatif FR de la vidéo + liste des domaines
 * miroir connus en lecture seule (vide à ce jour : aucun registre de miroirs n'est encore câblé
 * sur desktop — voir rapport P3-B / UI_DIVERGENCES.md).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 */
package org.koitharu.kotatsu.desktop.ui.screens.sources

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACK
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE_LINKS
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE_LINKS_DESKTOP_NOTE
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE_LINKS_EXPLANATION

/**
 * « Gérer les liens » — adaptation desktop minimale (lecture seule). Sur Android cet écran gère
 * les intents deep-links ; ici il expose le texte explicatif d'origine et un état vide explicatif,
 * en attendant un éventuel registre de domaines miroir.
 */
@Composable
public fun ManageLinksScreen(
	onBackClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxSize()) {
		// Barre du haut : ←  « Gérer les liens »
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.padding(horizontal = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(onClick = onBackClick) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = FR_BACK)
			}
			Text(
				text = FR_MANAGE_LINKS,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.weight(1f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
		Box(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
			) {
				Text(
					text = FR_MANAGE_LINKS_EXPLANATION,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Text(
					text = FR_MANAGE_LINKS_DESKTOP_NOTE,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(top = 12.dp),
				)
			}
		}
	}
}
