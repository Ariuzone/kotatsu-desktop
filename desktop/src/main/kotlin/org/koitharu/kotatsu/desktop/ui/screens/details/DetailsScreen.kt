/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Détails du manga » — reproduit activity_details.xml + fragment_chapters.xml de l'app
 * d'origine : couverture avec fond, titre TitleLarge, sous-titre (auteur), chips de tags,
 * description (tronquée + « More »), liste des chapitres, barre d'actions Read / Download /
 * Favorite (chaîne exactes de strings.xml).
 */
package org.koitharu.kotatsu.desktop.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.parsers.model.Manga

private val DETAILS_COVER_WIDTH = 100.dp
private val DETAILS_COVER_HEIGHT = 152.dp
private val DETAILS_COVER_CORNER = 12.dp
private val DETAILS_HEADER_HORIZONTAL_PADDING = 16.dp
private val DETAILS_HEADER_VERTICAL_PADDING = 12.dp

@Composable
public fun DetailsScreen(
	manga: Manga?,
	chapters: List<String>,
	isFavourite: Boolean,
	onBackClick: () -> Unit,
	onChapterClick: (Int) -> Unit,
	onFavouriteClick: () -> Unit,
	onDownloadClick: () -> Unit,
	onReadClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxSize()) {
		LazyColumn(modifier = Modifier.weight(1f)) {
			item {
				if (manga != null) {
					DetailsHeader(manga = manga, onBackClick = onBackClick)
				}
			}
			item {
				DescriptionSection(description = manga?.description.orEmpty())
			}
			item {
				Text(
					text = "Chapters",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(
						horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
						vertical = 8.dp,
					),
				)
			}
			items(chapters, key = { it }) { chapterTitle ->
				ChapterRow(title = chapterTitle, onClick = { onChapterClick(chapters.indexOf(chapterTitle)) })
				HorizontalDivider(
					color = MaterialTheme.colorScheme.outlineVariant,
					thickness = 0.5.dp,
				)
			}
		}
		DetailsActionsBar(
			isFavourite = isFavourite,
			onReadClick = onReadClick,
			onFavouriteClick = onFavouriteClick,
			onDownloadClick = onDownloadClick,
		)
	}
}

@Composable
private fun DetailsHeader(manga: Manga, onBackClick: () -> Unit) {
	Box(modifier = Modifier.fillMaxWidth()) {
		// Fond : couverture floutée (imageView_cover_bg d'origine)
		AsyncImage(
			model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
				.data(manga.coverUrl)
				.crossfade(true)
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.matchParentSize()
				.blur(24.dp),
		)
		Box(
			modifier = Modifier
				.matchParentSize()
				.background(
					Brush.verticalGradient(
						listOf(
							MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
							MaterialTheme.colorScheme.surface,
						),
					),
				),
		)
		Column(modifier = Modifier.fillMaxWidth()) {
			IconButton(onClick = onBackClick) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onSurface,
				)
			}
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
						vertical = DETAILS_HEADER_VERTICAL_PADDING,
					),
			) {
				AsyncImage(
					model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
						.data(manga.coverUrl)
						.crossfade(true)
						.build(),
					contentDescription = manga.title,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.width(DETAILS_COVER_WIDTH)
						.height(DETAILS_COVER_HEIGHT)
						.clip(RoundedCornerShape(DETAILS_COVER_CORNER))
						.background(MaterialTheme.colorScheme.surfaceContainer),
				)
				Column(
					modifier = Modifier
						.weight(1f)
						.padding(start = 16.dp),
				) {
					Text(
						text = manga.title,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 3,
						overflow = TextOverflow.Ellipsis,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = manga.author.orEmpty(),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					Spacer(modifier = Modifier.height(8.dp))
					// chips des tags (chips_tags d'origine), limités à 2 rangées visuelles
					Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
						for (tag in manga.tags.take(3)) {
							AssistChip(
								onClick = {},
								label = {
									Text(
										text = tag.title,
										maxLines = 1,
										overflow = TextOverflow.Ellipsis,
									)
								},
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun DescriptionSection(description: String) {
	if (description.isBlank()) return
	var expanded by remember { mutableStateOf(false) }
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
				vertical = 8.dp,
			),
	) {
		Text(
			text = "Description",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)
		Text(
			text = description,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = if (expanded) Int.MAX_VALUE else 4,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 4.dp),
		)
		Text(
			text = if (expanded) "Less" else "More",
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier
				.padding(top = 4.dp)
				.clickable { expanded = !expanded },
		)
	}
}

@Composable
private fun ChapterRow(title: String, onClick: () -> Unit) {
	Text(
		text = title,
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(
				horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
				vertical = 12.dp,
			),
	)
}

@Composable
private fun DetailsActionsBar(
	isFavourite: Boolean,
	onReadClick: () -> Unit,
	onFavouriteClick: () -> Unit,
	onDownloadClick: () -> Unit,
) {
	Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			// « Read » (button_read d'origine — split button dans le sheet des chapitres)
			Button(onClick = onReadClick, modifier = Modifier.weight(1f)) {
				Text(text = "Read")
			}
			IconButton(onClick = onFavouriteClick) {
				Icon(
					imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
					contentDescription = "Favorite this",
					tint = MaterialTheme.colorScheme.primary,
				)
			}
			IconButton(onClick = onDownloadClick) {
				Icon(
					imageVector = Icons.Filled.Download,
					contentDescription = "Download",
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
	}
}
