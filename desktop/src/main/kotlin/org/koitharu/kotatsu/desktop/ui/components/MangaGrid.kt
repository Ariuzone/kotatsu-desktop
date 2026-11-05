/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Grille de mangas — reproduit item_manga_grid.xml + fragment_list.xml de l'app d'origine :
 * carte marge 2dp/padding 6dp, couverture coins 12dp (ShapeAppearanceOverlay.Kotatsu.Cover),
 * titre sur 2 lignes (TextAppearance.Kotatsu.GridTitle = Material3 TitleSmall, paddingV 4dp),
 * grille adaptative (preferred_grid_width = 120dp, manga_grid_spacing = 8dp).
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.parsers.model.Manga

private val GRID_CARD_MARGIN = 2.dp
private val GRID_CARD_PADDING = 6.dp
private val COVER_CORNER_RADIUS = 12.dp
private val GRID_CELL_WIDTH = 120.dp
private val GRID_SPACING = 8.dp
private val TITLE_PADDING_VERTICAL = 4.dp

/**
 * Carte manga (grille), fidèle à item_manga_grid.xml.
 */
@Composable
public fun MangaGridCard(
	manga: Manga,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.padding(GRID_CARD_MARGIN)
			.clickable(onClick = onClick)
			.padding(GRID_CARD_PADDING),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(2f / 3f)
				.clip(RoundedCornerShape(COVER_CORNER_RADIUS))
				.background(MaterialTheme.colorScheme.surfaceContainer),
			contentAlignment = Alignment.Center,
		) {
			AsyncImage(
				model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
					.data(manga.coverUrl)
					.crossfade(true)
					.build(),
				contentDescription = manga.title,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize(),
			)
		}
		Text(
			text = manga.title,
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = TITLE_PADDING_VERTICAL),
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			textAlign = TextAlign.Start,
		)
	}
}

/**
 * Grille de mangas, fidèle à fragment_list.xml (mode grille) : cellules adaptatives de 120dp,
 * espacement 8dp, padding extérieur 2dp (grid_spacing_outer).
 */
@Composable
public fun MangaGrid(
	mangaList: List<Manga>,
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	contentPadding: PaddingValues = PaddingValues(GRID_CARD_MARGIN),
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(GRID_CELL_WIDTH),
		modifier = modifier.fillMaxSize(),
		contentPadding = contentPadding,
		horizontalArrangement = Arrangement.spacedBy(GRID_SPACING - GRID_CARD_MARGIN),
		verticalArrangement = Arrangement.spacedBy(GRID_SPACING - GRID_CARD_MARGIN),
	) {
		itemsIndexed(mangaList, key = { _, manga -> manga.url }) { _, manga ->
			MangaGridCard(
				manga = manga,
				onClick = { onMangaClick(manga) },
			)
		}
	}
}
