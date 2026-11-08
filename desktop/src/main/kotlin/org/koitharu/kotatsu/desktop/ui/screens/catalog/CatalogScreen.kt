/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Catalogue d'une source — reproduit MangaListFragment (mode grille) : liste paged avec
 * indicateur de chargement (item_loading_state) et pied d'erreur (item_error_footer).
 *
 * Modification : 2026-11-08 — phase P8 : filtres d'une source (vidéo t≈6:50-7:10 Source Weeb
 * Central) — icône entonnoir (FilterAlt) dans la top bar et chip d'ordre de tri « Mis à jour ▾ »
 * sous la top bar, ouvrant tous deux la bottom sheet « Trier et filtrer » : section « Ordre de
 * tri » (radios, libellés FR de [frSortOrderLabel]) et section « Genres » (chips multiselect
 * avec case, comme les chips de DiscoverScreen).
 *
 * Divergences desktop (documentées, cf. rapport P8) :
 *  - La liste est chargée via [fetchList] (injecté par Main, sans paramètre de tri). Le tri et le
 *    filtre de genres sont donc appliqués EN MÉMOIRE sur la liste reçue ; la pagination continue
 *    d'utiliser la taille de la liste brute comme offset. Le tri côté source (AppContainer.getList
 *    accepte déjà `order: SortOrder?`, et MangaListFilteraccepte les tags) est un POINT
 *    D'INTÉGRATION MAIN restant : câbler [fetchList]/[fetchFilterOptions] dans Main suffira.
 *  - POPULARITY/RATING : le modèle Manga desktop n'a pas de champ popularité — la note (rating)
 *    est utilisée comme approximation décroissante.
 *  - UPDATED/NEWEST/ADDED : aucune date n'est exposée sur Manga dans une liste — l'ordre renvoyé
 *    par la source est conservé.
 *  - Genres : si [fetchFilterOptions] n'est pas branché (défaut), les chips de genres sont
 *    dérivées des tags des mangas déjà chargés (pas de set statique vide).
 */
package org.koitharu.kotatsu.desktop.ui.screens.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import org.koitharu.kotatsu.desktop.ui.components.MangaGridCard
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.desktop.ui.strings.DEFAULT_CATALOG_SORT_ORDERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACK
import org.koitharu.kotatsu.desktop.ui.strings.FR_GENRES
import org.koitharu.kotatsu.desktop.ui.strings.FR_RETRY
import org.koitharu.kotatsu.desktop.ui.strings.FR_SORT_AND_FILTER
import org.koitharu.kotatsu.desktop.ui.strings.FR_SORT_ORDER
import org.koitharu.kotatsu.desktop.ui.strings.frSortOrderLabel
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaListFilterOptions
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.parsers.model.SortOrder

private val CATALOG_CELL_WIDTH = 120.dp
private val FILTERS_ROW_PADDING = 8.dp
private val SHEET_HORIZONTAL_PADDING = 16.dp
private val SHEET_BOTTOM_PADDING = 24.dp
private val SHEET_SECTION_SPACING = 20.dp
private val SHEET_CONTROL_SPACING = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun CatalogScreen(
	source: MangaParserSource,
	fetchList: suspend (source: MangaParserSource, offset: Int) -> List<Manga>,
	onMangaClick: (Manga) -> Unit,
	onBackClick: () -> Unit,
	modifier: Modifier = Modifier,
	// Ordres de tri proposés dans la sheet (Main pourra passer les sortOrders réels du source)
	sortOrders: Collection<SortOrder> = DEFAULT_CATALOG_SORT_ORDERS,
	// Ordre affiché/mémorisé par défaut : POPULARITY approxime le tri par défaut du source
	// (« Popular & reliable » de la vidéo) faute d'accès au tri côté source sans Main.
	defaultSortOrder: SortOrder = SortOrder.POPULARITY,
	// POINT D'INTÉGRATION MAIN : renvoyer ParserMangaRepository.getFilterOptions() (tags
	// disponibles du source). Défaut : null → genres dérivés des mangas déjà chargés.
	fetchFilterOptions: suspend (source: MangaParserSource) -> MangaListFilterOptions? = { null },
) {
	// Liste brute paginée (offset de pagination) — le tri/filtre est appliqué en mémoire dessus
	var rawItems by remember(source) { mutableStateOf<List<Manga>>(emptyList()) }
	var isLoading by remember(source) { mutableStateOf(true) }
	var isLoadingMore by remember(source) { mutableStateOf(false) }
	var error by remember(source) { mutableStateOf<String?>(null) }
	// Pagination : fin de liste atteinte / erreur de la dernière page (pied item_error_footer)
	var canLoadMore by remember(source) { mutableStateOf(true) }
	var loadMoreError by remember(source) { mutableStateOf<String?>(null) }
	// Filtres P8 : ordre de tri courant + clés de tags sélectionnés (multiselect)
	var selectedOrder by remember(source) { mutableStateOf(defaultSortOrder) }
	var selectedTagKeys by remember(source) { mutableStateOf<Set<String>>(emptySet()) }
	var showFilters by remember(source) { mutableStateOf(false) }
	var filterOptions by remember(source) { mutableStateOf<MangaListFilterOptions?>(null) }
	val gridState = rememberLazyGridState()
	val scope = rememberCoroutineScope()

	LaunchedEffect(source) {
		try {
			rawItems = fetchList(source, 0)
			error = null
		} catch (e: Exception) {
			error = e.message ?: e.javaClass.simpleName
		} finally {
			isLoading = false
		}
	}

	// Genres disponibles : MangaListFilterOptions du source si branché (Main), sinon les tags
	// des mangas déjà chargés (découverte progressive, pas de set statique vide).
	LaunchedEffect(source) {
		filterOptions = runCatching { fetchFilterOptions(source) }.getOrNull()
	}
	val availableTags: Set<MangaTag> = remember(filterOptions, rawItems) {
		filterOptions?.availableTags
			?: rawItems.flatMapTo(LinkedHashSet()) { it.tags }
	}

	// Liste affichée : filtre genres (inclusif : au moins un tag matché, comme les tags
	// inclusifs de MangaListFilter) puis tri en mémoire selon l'ordre choisi.
	val items = remember(rawItems, selectedOrder, selectedTagKeys) {
		rawItems
			.filter { manga ->
				selectedTagKeys.isEmpty() || manga.tags.any { it.key in selectedTagKeys }
			}
			.sortedFor(selectedOrder)
	}

	/** Charge la page suivante (déclenchée par le défilement ou le bouton « Réessayer »). */
	suspend fun loadMore() {
		if (isLoadingMore || !canLoadMore) return
		isLoadingMore = true
		loadMoreError = null
		try {
			// offset = taille de la liste BRUTE (le filtre mémoire ne décale pas la pagination)
			val next = fetchList(source, rawItems.size)
			if (next.isEmpty()) {
				canLoadMore = false // fin de liste
			} else {
				rawItems = (rawItems + next).distinctBy { it.url }
			}
		} catch (e: Exception) {
			loadMoreError = e.message ?: e.javaClass.simpleName
		} finally {
			isLoadingMore = false
		}
	}

	// Pagination : charge la page suivante quand on approche de la fin (comme l'app d'origine) ;
	// snapshotFlow reste actif après chaque échec, contrairement à l'ancien effet à clés figées.
	LaunchedEffect(gridState, items.size, canLoadMore, loadMoreError) {
		if (items.isEmpty()) return@LaunchedEffect
		snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
			.collect { lastVisible ->
				if (!isLoadingMore && canLoadMore && loadMoreError == null &&
					lastVisible >= items.size - 6
				) {
					loadMore()
				}
			}
	}

	Column(modifier = modifier.fillMaxSize()) {
		Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 4.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				IconButton(onClick = onBackClick) {
					Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = FR_BACK)
				}
				Text(
					text = source.title,
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				// Icône entonnoir « Trier et filtrer » (vidéo t≈6:50, à côté de la recherche)
				IconButton(onClick = { showFilters = true }) {
					Icon(Icons.Filled.FilterAlt, contentDescription = FR_SORT_AND_FILTER)
				}
			}
		}
		if (error != null) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				Text(
					text = error.orEmpty(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.error,
				)
			}
		} else if (isLoading) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				CircularProgressIndicator()
			}
		} else {
			// Chip d'ordre de tri sous la top bar (« Mis à jour ▾ » de la vidéo) : ouvre la
			// même sheet « Trier et filtrer »
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = FILTERS_ROW_PADDING, vertical = FILTERS_ROW_PADDING),
			) {
				FilterChip(
					selected = false,
					onClick = { showFilters = true },
					label = { Text(text = frSortOrderLabel(selectedOrder)) },
					trailingIcon = {
						Icon(
							imageVector = Icons.Filled.ArrowDropDown,
							contentDescription = null,
						)
					},
					colors = FilterChipDefaults.filterChipColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
					),
					border = FilterChipDefaults.filterChipBorder(
						enabled = true,
						selected = false,
						borderColor = MaterialTheme.colorScheme.outlineVariant,
					),
				)
			}
			LazyVerticalGrid(
				columns = GridCells.Adaptive(CATALOG_CELL_WIDTH),
				state = gridState,
				modifier = Modifier.fillMaxSize(),
				contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp),
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
				itemsIndexed(items) { _, manga ->
					MangaGridCard(manga = manga, onClick = { onMangaClick(manga) })
				}
				item(span = { GridItemSpan(maxLineSpan) }) {
					// Pied de liste : spinner de chargement ou erreur + « Réessayer »
					// (item_loading_state / item_error_footer de l'app d'origine)
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						if (isLoadingMore) {
							CircularProgressIndicator()
						}
						if (loadMoreError != null) {
							Text(
								text = loadMoreError.orEmpty(),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.error,
							)
							FilledTonalButton(
								onClick = { scope.launch { loadMore() } },
								modifier = Modifier.padding(top = 8.dp),
							) {
								Text(FR_RETRY)
							}
						}
					}
				}
			}
		}
	}

	if (showFilters) {
		CatalogFilterSheet(
			sortOrders = sortOrders,
			selectedOrder = selectedOrder,
			onSelectOrder = { selectedOrder = it },
			availableTags = availableTags,
			selectedTagKeys = selectedTagKeys,
			onToggleTag = { key ->
				selectedTagKeys = if (key in selectedTagKeys) {
					selectedTagKeys - key
				} else {
					selectedTagKeys + key
				}
			},
			onDismiss = { showFilters = false },
		)
	}
}

/**
 * Bottom sheet « Trier et filtrer » du catalogue (vidéo t≈6:50-7:10) : section « Ordre de tri »
 * (radios) puis « Genres » (chips multiselect avec case, appliquées à chaque interaction, sans
 * bouton valider — comme la ListOptionsSheet).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CatalogFilterSheet(
	sortOrders: Collection<SortOrder>,
	selectedOrder: SortOrder,
	onSelectOrder: (SortOrder) -> Unit,
	availableTags: Set<MangaTag>,
	selectedTagKeys: Set<String>,
	onToggleTag: (String) -> Unit,
	onDismiss: () -> Unit,
) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = SHEET_HORIZONTAL_PADDING)
				.padding(bottom = SHEET_BOTTOM_PADDING),
			verticalArrangement = Arrangement.spacedBy(SHEET_SECTION_SPACING),
		) {
			Text(
				text = FR_SORT_AND_FILTER,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			// --- « Ordre de tri » : radios (SortOrder pertinents du source) ---
			Column(verticalArrangement = Arrangement.spacedBy(SHEET_CONTROL_SPACING)) {
				Text(
					text = FR_SORT_ORDER,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				for (order in sortOrders) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable(onClick = { onSelectOrder(order) })
							.padding(vertical = 2.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						RadioButton(selected = order == selectedOrder, onClick = null)
						Text(
							text = frSortOrderLabel(order),
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier.padding(start = SHEET_CONTROL_SPACING),
						)
					}
				}
			}
			// --- « Genres » : chips multiselect avec case (tags du source) ---
			if (availableTags.isNotEmpty()) {
				Column(verticalArrangement = Arrangement.spacedBy(SHEET_CONTROL_SPACING)) {
					Text(
						text = FR_GENRES,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(SHEET_CONTROL_SPACING),
					) {
						for (tag in availableTags.sortedBy { it.title.lowercase() }) {
							val isSelected = tag.key in selectedTagKeys
							FilterChip(
								selected = isSelected,
								onClick = { onToggleTag(tag.key) },
								label = { Text(text = tag.title, maxLines = 1) },
								leadingIcon = if (isSelected) {
									{
										Icon(
											imageVector = Icons.Filled.Check,
											contentDescription = null,
											modifier = Modifier.size(18.dp),
										)
									}
								} else {
									null
								},
								colors = FilterChipDefaults.filterChipColors(
									containerColor = MaterialTheme.colorScheme.surfaceContainer,
									selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
								),
								border = FilterChipDefaults.filterChipBorder(
									enabled = true,
									selected = isSelected,
									borderColor = MaterialTheme.colorScheme.outlineVariant,
								),
							)
						}
					}
				}
			}
		}
	}
}

/**
 * Tri EN MÉMOIRE (divergence desktop : le tri côté source n'est pas passable sans toucher Main ;
 * cf. AppContainer.getList qui accepte déjà `order: SortOrder?` — point d'intégration restant).
 * POPULARITY/RATING : approximés par la note décroissante (Manga n'expose pas de popularité) ;
 * UPDATED/NEWEST/ADDED : l'ordre du source est conservé (aucune date sur Manga).
 */
private fun List<Manga>.sortedFor(order: SortOrder): List<Manga> = when (order) {
	SortOrder.POPULARITY, SortOrder.RATING -> sortedByDescending { it.rating }
	SortOrder.POPULARITY_ASC, SortOrder.RATING_ASC -> sortedBy { it.rating }
	SortOrder.ALPHABETICAL -> sortedBy { it.title.lowercase() }
	SortOrder.ALPHABETICAL_DESC -> sortedByDescending { it.title.lowercase() }
	else -> this // UPDATED, NEWEST, ADDED, RELEVANCE, fenêtres de popularité : ordre du source
}
