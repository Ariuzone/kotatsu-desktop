/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Explore » — reproduit fragment_explore.xml de l'app d'origine (sources regroupées par
 * langue, item_explore_source_grid). Le titre de chaque source est celui du dépôt de parsers.
 */
package org.koitharu.kotatsu.desktop.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import java.util.Locale

private val EXPLORE_CELL_WIDTH = 120.dp
private val EXPLORE_CORNER = 12.dp

@Composable
public fun ExploreScreen(
	onSourceClick: (MangaParserSource) -> Unit,
	modifier: Modifier = Modifier,
) {
	// Sources regroupées par langue, triées comme l'app d'origine (ExploreFragment) ;
	// sources « broken » exclues comme côté Android.
	val sourcesByLocale = remember group@{
		MangaParserSource.entries
			.filterNot { it.isBroken }
			.groupBy { it.locale.ifBlank { "multi" } }
			.toSortedMap(compareBy { localeKey ->
				localeKey
			})
	}

	LazyVerticalGrid(
		columns = GridCells.Adaptive(EXPLORE_CELL_WIDTH),
		modifier = modifier.fillMaxSize(),
		contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		for ((locale, sources) in sourcesByLocale) {
			item(key = "header_$locale", span = { GridItemSpan(maxLineSpan) }) {
				Text(
					text = localeHeader(locale),
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
				)
			}
			items(sources, key = { it.name }) { source ->
				SourceCard(source = source, onClick = { onSourceClick(source) })
			}
		}
	}
}

private fun localeHeader(locale: String): String = if (locale == "multi") {
	"Multi-language"
} else {
	runCatching {
		Locale(locale).getDisplayName(Locale.getDefault()).replaceFirstChar { it.uppercase() }
	}.getOrDefault(locale)
}

@Composable
private fun SourceCard(source: MangaParserSource, onClick: () -> Unit) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(2.dp),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(2f / 3f)
				.clickable(onClick = onClick)
				.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(EXPLORE_CORNER)),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				imageVector = Icons.Filled.Image,
				contentDescription = source.title,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
		Text(
			text = source.title,
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(vertical = 4.dp),
		)
		Text(
			text = source.name.lowercase().replace('_', ' '),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}
