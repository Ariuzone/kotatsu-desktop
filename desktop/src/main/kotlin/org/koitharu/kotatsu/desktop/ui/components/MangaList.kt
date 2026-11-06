/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Liste de mangas — reproduit item_manga_list.xml de l'app d'origine : couverture 40dp coins 4dp
 * (ShapeAppearanceOverlay.Kotatsu.Cover.Small), titre TitleSmall 1 ligne, sous-titre BodySmall
 * 1 ligne, marges 8dp/16dp. Sous-titre = auteur pour l'app d'origine (MangaListAD).
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.parsers.model.Manga

private val LIST_ITEM_VERTICAL_PADDING = 8.dp
private val LIST_ITEM_HORIZONTAL_PADDING = 8.dp
private val LIST_COVER_SIZE = 40.dp
private val LIST_COVER_CORNER_RADIUS = 4.dp
private val LIST_TITLE_START_MARGIN = 16.dp
private val LIST_TITLE_END_MARGIN = 8.dp

/**
 * Ligne de manga (mode liste), fidèle à item_manga_list.xml.
 */
@Composable
public fun MangaListCard(
	manga: Manga,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(
				horizontal = LIST_ITEM_HORIZONTAL_PADDING,
				vertical = LIST_ITEM_VERTICAL_PADDING,
			),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(LIST_COVER_SIZE)
				.clip(RoundedCornerShape(LIST_COVER_CORNER_RADIUS))
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
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = LIST_TITLE_START_MARGIN, end = LIST_TITLE_END_MARGIN),
		) {
			Text(
				text = manga.title,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = manga.author.orEmpty(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

/**
 * Liste verticale de mangas (mode liste, cf. fragment_list.xml).
 */
@Composable
public fun MangaList(
	mangaList: List<Manga>,
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
) {
	LazyColumn(modifier = modifier.fillMaxSize()) {
		itemsIndexed(mangaList) { index, manga ->
			MangaListCard(
				manga = manga,
				onClick = { onMangaClick(manga) },
			)
		}
	}
}
