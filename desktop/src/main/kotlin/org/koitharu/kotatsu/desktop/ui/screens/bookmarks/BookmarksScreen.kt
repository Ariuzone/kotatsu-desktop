/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation Compose Desktop.
 *
 * Écran « Signets » (AllBookmarksActivity/AllBookmarksFragment) : grille des pages marquées,
 * groupées par manga (comme findAll() du BookmarksDao d'origine), miniature de la page,
 * suppression au clic long (contextuel) — chaînes exactes de strings.xml.
 */
package org.koitharu.kotatsu.desktop.ui.screens.bookmarks

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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import org.koitharu.kotatsu.core.db.entity.MangaWithTags
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateView

private val BOOKMARK_CELL_WIDTH = 120.dp
private val BOOKMARK_CORNER = 12.dp

/** Signet affichable (équiv. Bookmark model du domaine d'origine, version desktop). */
public data class BookmarkUi(
	public val pageId: Long,
	public val chapterId: Long,
	public val page: Int,
	public val imageUrl: String,
	public val percent: Float,
)

@Composable
public fun BookmarksScreen(
	bookmarksByManga: Map<MangaWithTags, List<BookmarkUi>>,
	onBookmarkClick: (MangaWithTags, BookmarkUi) -> Unit,
	onBookmarkDelete: (BookmarkUi) -> Unit,
	modifier: Modifier = Modifier,
) {
	if (bookmarksByManga.isEmpty()) {
		EmptyStateView(
			icon = androidx.compose.material.icons.Icons.Filled.Bookmarks,
			textPrimary = "No bookmarks here yet",
			textSecondary = "Save pages while reading to see them here",
			modifier = modifier.fillMaxSize(),
		)
		return
	}
	LazyVerticalGrid(
		columns = GridCells.Adaptive(BOOKMARK_CELL_WIDTH),
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		for ((manga, bookmarks) in bookmarksByManga) {
			item(key = "header_${manga.manga.id}", span = { GridItemSpan(maxLineSpan) }) {
				Text(
					text = manga.manga.title,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
				)
			}
			items(bookmarks, key = { it.pageId }) { bookmark ->
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { onBookmarkClick(manga, bookmark) },
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(2f / 3f)
							.clip(RoundedCornerShape(BOOKMARK_CORNER))
							.background(MaterialTheme.colorScheme.surfaceContainer),
						contentAlignment = Alignment.Center,
					) {
						AsyncImage(
							model = bookmark.imageUrl,
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier.fillMaxSize(),
						)
					}
					Text(
						text = "Page ${bookmark.page + 1}",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						modifier = Modifier.padding(vertical = 4.dp),
					)
				}
			}
		}
	}
}
