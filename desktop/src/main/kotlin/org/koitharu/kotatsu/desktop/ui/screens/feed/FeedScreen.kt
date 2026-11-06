/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — onglet « Flux »
 * (FeedFragment d'origine, tracker/ui/feed), fidèle aux captures de référence : barre de
 * recherche pill + menu ⋮ (Mettre à Jour, Afficher mis à jour [case], Effacer le flux),
 * liste des entrées (item_feed.xml : couverture 40dp coins 8dp, titre TitleSmall, résumé
 * « N nouveau(x) chapitre(s) » avec indicateur « nouveau ») et état vide Totoro (ic_empty_feed,
 * « C'est un peu vide ici… » / « Les nouveaux chapitres de ce que vous lisez sont affichés ici »).
 *
 * Les données sont injectées : [entries] (l'orchestrateur branchera la DB Room / FeedDao).
 */
package org.koitharu.kotatsu.desktop.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.TotoroIllustration
import org.koitharu.kotatsu.desktop.ui.components.TopSearchBar
import org.koitharu.kotatsu.desktop.ui.components.TopSearchMenuAction
import org.koitharu.kotatsu.desktop.ui.strings.FR_CLEAR_FEED
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SHOW_UPDATED
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_EMPTY_HOLDER_PRIMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_FEED_HOLDER
import org.koitharu.kotatsu.desktop.ui.strings.FR_UPDATE
import org.koitharu.kotatsu.desktop.ui.strings.frNewChapters
import org.koitharu.kotatsu.parsers.model.Manga

private val FEED_COVER_SIZE = 40.dp
private val FEED_COVER_CORNER = 8.dp // ShapeAppearanceOverlay.Kotatsu.Cover.Medium

/**
 * Entrée du flux (substitut local de FeedItem d'origine : id + manga + compteur).
 * @param count nombre de nouveaux chapitres
 * @param isNew true si l'entrée n'a pas encore été consultée (indicateur)
 */
public data class FeedEntry(
	public val id: Long,
	public val manga: Manga,
	public val count: Int,
	public val isNew: Boolean,
)

/**
 * Onglet « Flux ». @param entries nouveaux chapitres des mangas suivis (injecté, DB Room).
 */
@Composable
public fun FeedScreen(
	entries: List<FeedEntry>,
	onEntryClick: (FeedEntry) -> Unit,
	onUpdateClick: () -> Unit,
	onClearFeedClick: () -> Unit,
	onShowUpdatedToggle: (Boolean) -> Unit,
	onSearchClick: () -> Unit,
	onSettingsClick: () -> Unit,
	onIncognitoToggle: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
) {
	var showUpdated by remember { mutableStateOf(false) }
	var incognito by remember { mutableStateOf(false) }

	Column(modifier = modifier.fillMaxSize()) {
		// Barre de recherche pill + menu ⋮ (fidèle aux captures ; menu = opt_feed)
		TopSearchBar(
			onSearchClick = onSearchClick,
			menuActions = listOf(
				TopSearchMenuAction(FR_UPDATE, onUpdateClick),
				TopSearchMenuAction(
					label = FR_SHOW_UPDATED,
					onClick = {
						showUpdated = !showUpdated
						onShowUpdatedToggle(showUpdated)
					},
					checkable = true,
					checked = showUpdated,
				),
				TopSearchMenuAction(FR_CLEAR_FEED, onClearFeedClick),
				TopSearchMenuAction(
					label = FR_INCOGNITO_MODE,
					onClick = {
						incognito = !incognito
						onIncognitoToggle(incognito)
					},
					checkable = true,
					checked = incognito,
				),
				TopSearchMenuAction(FR_SETTINGS, onSettingsClick),
			),
		)
		if (entries.isEmpty()) {
			// getEmptyState de FeedViewModel (Totoro ic_empty_feed, textes values-fr)
			EmptyStateScreen(
				illustration = TotoroIllustration.FEED,
				textPrimary = FR_TEXT_EMPTY_HOLDER_PRIMARY,
				textSecondary = FR_TEXT_FEED_HOLDER,
				modifier = Modifier.fillMaxSize(),
			)
		} else {
			val visible = if (showUpdated) entries.filter { it.isNew } else entries
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(visible, key = { it.id }) { entry ->
					FeedRow(
						entry = entry,
						onClick = { onEntryClick(entry) },
					)
					HorizontalDivider(
						color = MaterialTheme.colorScheme.outlineVariant,
						thickness = 0.5.dp,
					)
				}
			}
		}
	}
}

/** Ligne du flux, fidèle à item_feed.xml : 72dp, couverture 40dp (coins 8dp), titre, résumé. */
@Composable
private fun FeedRow(entry: FeedEntry, onClick: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(72.dp)
			.clickable(onClick = onClick)
			.padding(horizontal = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(FEED_COVER_SIZE)
				.clip(RoundedCornerShape(FEED_COVER_CORNER))
				.background(MaterialTheme.colorScheme.surfaceContainer),
			contentAlignment = Alignment.Center,
		) {
			AsyncImage(
				model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
					.data(entry.manga.coverUrl)
					.crossfade(true)
					.build(),
				contentDescription = entry.manga.title,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize(),
			)
		}
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = 16.dp, end = 8.dp),
		) {
			Text(
				text = entry.manga.title,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = frNewChapters(entry.count),
				style = MaterialTheme.typography.bodySmall,
				color = if (entry.isNew) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.onSurfaceVariant
				},
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}
