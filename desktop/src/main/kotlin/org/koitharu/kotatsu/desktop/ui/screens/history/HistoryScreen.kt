/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Historique » — reproduit HistoryListFragment (fragment_list_simple.xml) :
 * liste de mangas groupée par date de dernière lecture (en-têtes DateTimeAgo, cf.
 * HistoryListViewModel#mapList + item_header.xml), items fidèles à item_manga_list_details.xml
 * (couverture 118dp ratio 2:3, coins 8dp, ReadingProgressView 32dp/offset 8dp, titre BodyLarge
 * 3 lignes/lineHeight 18dp, sous-titres BodyMedium/BodySmall) et état vide
 * (item_empty_state + ic_empty_history, « What you read will be displayed here »).
 *
 * Les données viennent en paramètres (la couche DB Room est portée ailleurs), comme
 * CatalogScreen reçoit `fetchList`.
 */
package org.koitharu.kotatsu.desktop.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.core.ui.model.DateTimeAgo
import org.koitharu.kotatsu.desktop.ui.components.CARD_INDICATOR_OFFSET
import org.koitharu.kotatsu.desktop.ui.components.CARD_INDICATOR_SIZE
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.ReadingProgressIndicator
import org.koitharu.kotatsu.desktop.ui.components.calculateTimeAgo
import org.koitharu.kotatsu.desktop.ui.components.format
import org.koitharu.kotatsu.desktop.ui.components.percentPattern
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// strings.xml d'origine
public const val HISTORY_TITLE: String = "History"
private const val EMPTY_PRIMARY = "What you read will be displayed here" // text_history_holder_primary
private const val EMPTY_SECONDARY = "Find what to read in the «Explore» section" // text_history_holder_secondary
private const val CONTINUE_READING_CD = "Continue reading" // action « lire la suite »
private const val UNKNOWN = "Unknown" // string unknown

private val LIST_SPACING = 8.dp // list_spacing_normal (padding du RecyclerView)
private val GRID_SPACING = 8.dp // grid_spacing (padding des en-têtes)
private val DETAILED_COVER_HEIGHT = 118.dp // item_manga_list_details.xml
private val DETAILED_COVER_CORNER = 8.dp // ShapeAppearanceOverlay.Kotatsu.Cover.Medium
private val ROW_START_MARGIN = 16.dp
private val ROW_END_MARGIN = 12.dp
private val TEXT_SPACING = 4.dp
private val TITLE_LINE_HEIGHT = 18.sp // app:lineHeight="18dp"

/**
 * Données d'une entrée d'historique (substitut local tant que la couche DB n'est pas fusionnée).
 * @param manga manga lu
 * @param chapter dernier chapitre lu (sous-titre de la ligne)
 * @param percent progression de lecture 0f..1f (pastille de progression sur la couverture)
 * @param updatedAt date de dernière lecture (en-têtes de groupes + ligne de date)
 */
public data class HistoryItem(
	public val manga: Manga,
	public val chapter: MangaChapter?,
	public val percent: Float,
	public val updatedAt: Instant,
)

private sealed interface HistoryRow {
	public data class Header(val text: String) : HistoryRow
	public data class Entry(val item: HistoryItem) : HistoryRow
}

private fun timeAgoText(instant: Instant): String {
	val ago = calculateTimeAgo(instant)
		?: DateTimeAgo.Absolute(LocalDate.ofInstant(instant, ZoneId.systemDefault()))
	return ago.format()
}

@Composable
public fun HistoryScreen(
	historyItems: List<HistoryItem>,
	onMangaClick: (Manga) -> Unit,
	onContinueClick: (HistoryItem) -> Unit,
	modifier: Modifier = Modifier,
	isGroupingEnabled: Boolean = true,
	onClearHistory: (() -> Unit)? = null,
) {
	Column(modifier = modifier.fillMaxSize()) {
		// Barre de titre (l'app d'origine affiche « History » dans l'app bar + menu opt_history)
		Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = HISTORY_TITLE,
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				if (onClearHistory != null) {
					IconButton(onClick = onClearHistory) {
						Icon(
							imageVector = Icons.Outlined.DeleteSweep,
							contentDescription = "Clear history", // string clear_history
						)
					}
				}
			}
		}
		if (historyItems.isEmpty()) {
			// getEmptyState(hasFilters = false) de HistoryListViewModel
			EmptyStateScreen(
				icon = Icons.Outlined.History, // ic_empty_history
				textPrimary = EMPTY_PRIMARY,
				textSecondary = EMPTY_SECONDARY,
				modifier = Modifier.fillMaxSize(),
			)
		} else {
			val rows = remember(historyItems, isGroupingEnabled) {
				buildList {
					var prevHeader: DateTimeAgo? = null
					for (item in historyItems) {
						if (isGroupingEnabled) {
							val header = calculateTimeAgo(item.updatedAt)
								?: DateTimeAgo.Absolute(LocalDate.ofInstant(item.updatedAt, ZoneId.systemDefault()))
							if (header != prevHeader) {
								add(HistoryRow.Header(header.format()))
								prevHeader = header
							}
						}
						add(HistoryRow.Entry(item))
					}
				}
			}
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(LIST_SPACING),
				verticalArrangement = Arrangement.spacedBy(LIST_SPACING),
			) {
				items(rows, key = { row ->
					when (row) {
						is HistoryRow.Header -> "header_${row.text}"
						is HistoryRow.Entry -> row.item.manga.url
					}
				}) { row ->
					when (row) {
						is HistoryRow.Header -> ListHeaderRow(text = row.text)
						is HistoryRow.Entry -> HistoryListCard(
							item = row.item,
							onClick = { onMangaClick(row.item.manga) },
							onContinueClick = { onContinueClick(row.item) },
						)
					}
				}
			}
		}
	}
}

/** item_header.xml : TextView TitleSmall, une ligne, padding grid_spacing (8dp). */
@Composable
private fun ListHeaderRow(text: String) {
	Text(
		text = text,
		style = MaterialTheme.typography.titleSmall,
		color = MaterialTheme.colorScheme.onSurface,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
		modifier = Modifier
			.fillMaxWidth()
			.padding(GRID_SPACING),
	)
}

/**
 * Ligne d'historique, fidèle à item_manga_list_details.xml : couverture 118dp (ratio 2:3,
 * coins 8dp, fond colorSurfaceContainer), ReadingProgressView 32dp en bas à droite de la
 * couverture (offset 8dp), titre BodyLarge (3 lignes), sous-titre = dernier chapitre lu
 * (BodyMedium), date de dernière lecture (BodySmall), bouton « lire la suite » (PlayArrow,
 * équivalent de l'action de lecture des cartes grille).
 */
@Composable
private fun HistoryListCard(
	item: HistoryItem,
	onClick: () -> Unit,
	onContinueClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box {
			Box(
				modifier = Modifier
					.height(DETAILED_COVER_HEIGHT)
					.aspectRatio(2f / 3f)
					.clip(RoundedCornerShape(DETAILED_COVER_CORNER))
					.background(MaterialTheme.colorScheme.surfaceContainer),
				contentAlignment = Alignment.Center,
			) {
				AsyncImage(
					model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
						.data(item.manga.coverUrl)
						.crossfade(true)
						.build(),
					contentDescription = item.manga.title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
				)
			}
			// progressView : bottom|end de la couverture, marge card_indicator_offset (8dp)
			ReadingProgressIndicator(
				percent = item.percent,
				text = percentPattern(item.percent),
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(CARD_INDICATOR_OFFSET)
					.size(CARD_INDICATOR_SIZE),
			)
		}
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = ROW_START_MARGIN, end = ROW_END_MARGIN),
		) {
			Text(
				text = item.manga.title,
				style = MaterialTheme.typography.bodyLarge.copy(lineHeight = TITLE_LINE_HEIGHT),
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 3,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = item.chapter?.name ?: UNKNOWN,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(top = TEXT_SPACING),
			)
			Text(
				text = timeAgoText(item.updatedAt),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(top = TEXT_SPACING),
			)
		}
		IconButton(onClick = onContinueClick) {
			Icon(
				imageVector = Icons.Filled.PlayArrow,
				contentDescription = CONTINUE_READING_CD,
				tint = MaterialTheme.colorScheme.primary,
			)
		}
	}
}
