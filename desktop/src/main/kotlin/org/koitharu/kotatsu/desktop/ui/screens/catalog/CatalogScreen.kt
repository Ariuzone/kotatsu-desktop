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
import androidx.compose.runtime.setValue
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
	val gridState = rememberLazyGridState()

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
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp),
						contentAlignment = Alignment.Center,
					) {
						if (isLoadingMore) {
							CircularProgressIndicator()
						}
					}
				}
			}
			// Pagination : charge la page suivante quand on approche de la fin (comme l'app d'origine)
			val shouldLoadMore = !isLoading && !isLoadingMore &&
				gridState.layoutInfo.visibleItemsInfo.any { it.index >= items.size - 6 } && items.isNotEmpty()
			LaunchedEffect(shouldLoadMore, items.size) {
				if (shouldLoadMore) {
					isLoadingMore = true
					try {
						val next = fetchList(source, items.size)
						items = items + next
					} catch (_: Exception) {
						// pied d'erreur silencieux comme item_error_footer
					} finally {
						isLoadingMore = false
					}
				}
			}
		}
	}
}
