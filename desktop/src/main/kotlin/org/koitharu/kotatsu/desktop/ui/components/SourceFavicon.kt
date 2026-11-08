/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — favicon de source
 * partagé (grille « Sources des mangas », catalogue, recherche). Chaîne de secours : favicon du
 * site (<domaine>/favicon.ico) → service Google s2 favicons (128px) → icône générique MenuBook.
 * L'app Android utilise sa propre base de favicons (FaviconsDao) — non portée, divergence
 * documentée dans docs/UI_DIVERGENCES.md.
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent

@Composable
public fun SourceFavicon(
	domain: String?,
	contentDescription: String,
	modifier: Modifier = Modifier,
) {
	var attempt by remember(domain) { mutableIntStateOf(0) }
	val candidates = remember(domain) {
		listOfNotNull(
			domain?.let { "https://$it/favicon.ico" },
			domain?.let { "https://www.google.com/s2/favicons?domain=$it&sz=128" },
		)
	}
	if (domain == null || attempt >= candidates.size) {
		// Icône générique : la tuile/ligne reste lisible et cliquable
		Box(modifier = modifier, contentAlignment = Alignment.Center) {
			Icon(
				imageVector = Icons.AutoMirrored.Filled.MenuBook,
				contentDescription = contentDescription,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.fillMaxSize(),
			)
		}
		return
	}
	SubcomposeAsyncImage(
		model = candidates[attempt],
		contentDescription = contentDescription,
		contentScale = ContentScale.Fit,
		modifier = modifier,
	) {
		when (val state = painter.state) {
			is AsyncImagePainter.State.Error -> {
				LaunchedEffect(state) { attempt++ }
				Icon(
					imageVector = Icons.AutoMirrored.Filled.MenuBook,
					contentDescription = contentDescription,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
			else -> SubcomposeAsyncImageContent()
		}
	}
}
