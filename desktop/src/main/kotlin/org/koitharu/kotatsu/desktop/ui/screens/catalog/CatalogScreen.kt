/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Catalogue d'une source — reproduit MangaListFragment (mode grille) : liste paged avec
 * indicateur de chargement (item_loading_state) et pied d'erreur (item_error_footer).
 */
package org.koitharu.kotatsu.desktop.ui.screens.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.layout.Arrangement
import org.koitharu.kotatsu.desktop.ui.components.MangaGridCard
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaParserSource

private val CATALOG_CELL_WIDTH = 120.dp

@Composable
public fun CatalogScreen(
	source: MangaParserSource,
	fetchList: suspend (source: MangaParserSource, offset: Int) -> List<Manga>,
	onMangaClick: (Manga) -> Unit,
	onBackClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var items by remember(source) { mutableStateOf<List<Manga>>(emptyList()) }
	var isLoading by remember(source) { mutableStateOf(true) }
	var isLoadingMore by remember(source) { mutableStateOf(false) }
	var error by remember(source) { mutableStateOf<String?>(null) }
	// Pagination : fin de liste atteinte / erreur de la dernière page (pied item_error_footer)
	var canLoadMore by remember(source) { mutableStateOf(true) }
	var loadMoreError by remember(source) { mutableStateOf<String?>(null) }
	val gridState = rememberLazyGridState()
	val scope = rememberCoroutineScope()

	LaunchedEffect(source) {
		try {
			items = fetchList(source, 0)
			error = null
		} catch (e: Exception) {
			error = e.message ?: e.javaClass.simpleName
		} finally {
			isLoading = false
		}
	}

	/** Charge la page suivante (déclenchée par le défilement ou le bouton « Réessayer »). */
	suspend fun loadMore() {
		if (isLoadingMore || !canLoadMore) return
		isLoadingMore = true
		loadMoreError = null
		try {
			val next = fetchList(source, items.size)
			if (next.isEmpty()) {
				canLoadMore = false // fin de liste
			} else {
				items = (items + next).distinctBy { it.url }
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
					Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
				}
				Text(
					text = source.title,
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
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
								Text("Réessayer")
							}
						}
					}
				}
			}
		}
	}
}
