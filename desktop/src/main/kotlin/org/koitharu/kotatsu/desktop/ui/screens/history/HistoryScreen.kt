/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose
 * Desktop ; 2026-11-08 — phase P2 : l'entrée « Options de liste » du menu ⋮ ouvre la
 * ListOptionsSheet (callback onListOptionsClick, feuille hébergée par Main).
 *
 * Écran « Historique » — reproduit HistoryListFragment (fragment_list_simple.xml) :
 * barre de recherche pill + menu ⋮ (fidèle aux captures de référence ; menu = opt_history :
 * Effacer l'historique, Options de liste, Mode incognito, Paramètres), liste de mangas groupée
 * par date de dernière lecture (en-têtes DateTimeAgo, cf. HistoryListViewModel#mapList +
 * item_header.xml), items fidèles à item_manga_list_details.xml (couverture 118dp ratio 2:3,
 * coins 8dp, ReadingProgressView 32dp/offset 8dp, titre BodyLarge 3 lignes/lineHeight 18dp,
 * sous-titres BodyMedium/BodySmall) et état vide (item_empty_state + ic_empty_history Totoro,
 * textes values-fr exacts).
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.core.prefs.ListMode
import org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode
import org.koitharu.kotatsu.core.ui.model.DateTimeAgo
import org.koitharu.kotatsu.desktop.ui.components.CARD_INDICATOR_OFFSET
import org.koitharu.kotatsu.desktop.ui.components.CARD_INDICATOR_SIZE
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.ReadingProgressIndicator
import org.koitharu.kotatsu.desktop.ui.components.TotoroIllustration
import org.koitharu.kotatsu.desktop.ui.components.TopSearchBar
import org.koitharu.kotatsu.desktop.ui.components.TopSearchMenuAction
import org.koitharu.kotatsu.desktop.ui.components.calculateTimeAgo
import org.koitharu.kotatsu.desktop.ui.components.format
import org.koitharu.kotatsu.desktop.ui.components.percentPattern
import org.koitharu.kotatsu.desktop.ui.strings.FR_CLEAR_HISTORY
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_OPTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_HISTORY_HOLDER_PRIMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_HISTORY_HOLDER_SECONDARY
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// strings.xml d'origine (values-fr)
private const val EMPTY_PRIMARY = FR_TEXT_HISTORY_HOLDER_PRIMARY // text_history_holder_primary
private const val EMPTY_SECONDARY = FR_TEXT_HISTORY_HOLDER_SECONDARY // text_history_holder_secondary
private const val CONTINUE_READING_CD = "Continuer la lecture" // action « lire la suite »
private const val UNKNOWN = "Inconnu" // string unknown

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
	listMode: ListMode = ListMode.DETAILED_LIST,
	gridSize: Int = 100,
	progressMode: ProgressIndicatorMode = ProgressIndicatorMode.PERCENT_READ,
	isBadgesEnabled: Boolean = false,
	favouriteUrls: Set<String> = emptySet(),
	onListOptionsClick: (() -> Unit)? = null,
	onClearHistory: (() -> Unit)? = null,
	onSearchClick: () -> Unit = {},
	onSettingsClick: () -> Unit = {},
	onIncognitoToggle: (Boolean) -> Unit = {},
) {
	var incognito by remember { mutableStateOf(false) }
	Column(modifier = modifier.fillMaxSize()) {
		// Barre de recherche pill + menu ⋮ (fidèle aux captures ; menu = opt_history)
		TopSearchBar(
			onSearchClick = onSearchClick,
			menuActions = buildList {
				if (onClearHistory != null) {
					add(TopSearchMenuAction(FR_CLEAR_HISTORY, onClearHistory))
				}
				add(TopSearchMenuAction(label = FR_LIST_OPTIONS, onClick = { onListOptionsClick?.invoke() }))
				add(
					TopSearchMenuAction(
						label = FR_INCOGNITO_MODE,
						onClick = {
							incognito = !incognito
							onIncognitoToggle(incognito)
						},
						checkable = true,
						checked = incognito,
					),
				)
				add(TopSearchMenuAction(FR_SETTINGS, onSettingsClick))
			},
		)
		if (historyItems.isEmpty()) {
			// getEmptyState(hasFilters = false) de HistoryListViewModel (Totoro ic_empty_history)
			EmptyStateScreen(
				illustration = TotoroIllustration.HISTORY,
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
			when (listMode) {
				ListMode.GRID -> {
					// « Taille de la grille » : 100 % = 110dp (tuile de référence de l'app)
					val cellWidth = (110.dp * gridSize.coerceIn(50, 200) / 100f)
					LazyVerticalGrid(
						columns = GridCells.Adaptive(cellWidth),
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(LIST_SPACING),
						horizontalArrangement = Arrangement.spacedBy(LIST_SPACING),
						verticalArrangement = Arrangement.spacedBy(LIST_SPACING),
					) {
						items(rows, key = { row ->
							when (row) {
								is HistoryRow.Header -> "header_${row.text}"
								is HistoryRow.Entry -> row.item.manga.url
							}
						}, span = { row ->
							if (row is HistoryRow.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1)
						}) { row ->
							when (row) {
								is HistoryRow.Header -> ListHeaderRow(text = row.text)
								is HistoryRow.Entry -> HistoryGridCard(
									item = row.item,
									progressText = progressText(progressMode, row.item),
									showBadge = isBadgesEnabled && row.item.manga.url in favouriteUrls,
									onClick = { onMangaClick(row.item.manga) },
								)
							}
						}
					}
				}
				ListMode.LIST -> {
					// « Compact » : couverture réduite, titre 2 lignes
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
								is HistoryRow.Entry -> HistoryCompactCard(
									item = row.item,
									progressText = progressText(progressMode, row.item),
									onClick = { onMangaClick(row.item.manga) },
								)
							}
						}
					}
				}
				ListMode.DETAILED_LIST -> {
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
									progressText = progressText(progressMode, row.item),
									onClick = { onMangaClick(row.item.manga) },
									onContinueClick = { onContinueClick(row.item) },
								)
							}
						}
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
	progressText: String?,
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
			if (progressText != null) {
				ReadingProgressIndicator(
					percent = item.percent,
					text = progressText,
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(CARD_INDICATOR_OFFSET)
						.size(CARD_INDICATOR_SIZE),
				)
			}
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


/** Texte de l'indicateur de progression selon la préférence ; null = indicateur masqué. */
private fun progressText(mode: ProgressIndicatorMode, item: HistoryItem): String? = when (mode) {
	ProgressIndicatorMode.NONE -> null
	ProgressIndicatorMode.PERCENT_READ -> percentPattern(item.percent)
	ProgressIndicatorMode.PERCENT_LEFT -> percentPattern((1f - item.percent).coerceIn(0f, 1f))
	// Chapitres lus/restants : le compteur fiable arrive avec la fusion DB (P8) ;
	// en attendant, repli sur le pourcentage.
	ProgressIndicatorMode.CHAPTERS_READ, ProgressIndicatorMode.CHAPTERS_LEFT -> percentPattern(item.percent)
}

/** Carte en mode Grille : couverture + pastille de progression + badge favori (capture). */
@Composable
private fun HistoryGridCard(
	item: HistoryItem,
	progressText: String?,
	showBadge: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 2.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box {
			Box(
				modifier = Modifier
					.fillMaxWidth()
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
			if (showBadge) {
				Icon(
					imageVector = Icons.Filled.Favorite,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.align(Alignment.TopEnd)
						.padding(6.dp)
						.size(18.dp),
				)
			}
			if (progressText != null) {
				ReadingProgressIndicator(
					percent = item.percent,
					text = progressText,
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(CARD_INDICATOR_OFFSET)
						.size(CARD_INDICATOR_SIZE),
				)
			}
		}
		Text(
			text = item.manga.title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 4.dp),
		)
	}
}

/** Ligne compacte : couverture 56dp, titre 2 lignes (Options de liste « Compact »). */
@Composable
private fun HistoryCompactCard(
	item: HistoryItem,
	progressText: String?,
	onClick: () -> Unit,
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
					.width(56.dp)
					.aspectRatio(2f / 3f)
					.clip(RoundedCornerShape(6.dp))
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
			if (progressText != null) {
				ReadingProgressIndicator(
					percent = item.percent,
					text = progressText,
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(2.dp)
						.size(24.dp),
				)
			}
		}
		Text(
			text = item.manga.title,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = ROW_START_MARGIN),
		)
	}
}
