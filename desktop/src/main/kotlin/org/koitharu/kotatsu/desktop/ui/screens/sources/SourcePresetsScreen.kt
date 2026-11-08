/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — onglet « Source »
 * (SourcesScreen d'origine, fragment_sources.xml), fidèle aux captures de référence :
 * barre de recherche pill + menu ⋮ (Préréglages de sources, Gérer les sources, Mode incognito,
 * Paramètres), boutons rapides pill (Stockage local / Marque-pages / Aléatoire / Téléchargements /
 * Préréglages de sources ▾), section « Suggestions » (pager + points) et « Sources des mangas »
 * (grille de favicons, 🔥 devant les sources avec de nouveaux chapitres).
 *
 * Les données sont injectées : [suggestions] (List<Manga>), [sources] (List<MangaParserSource>),
 * [domainOf] (favicon URL, déjà disponible via AppContainer.domainOf).
 *
 * 2026-11-08 — phase P3 : créateur de préréglages (vidéo t≈5:30) — action « Nouveau préréglage »
 * dans le menu ⋮ ouvrant PresetCreatorDialog ; l'enregistrement persiste via [presetsDao]
 * (SourcePresetsDao Room, même backend que côté Android — passer container.database.
 * getSourcePresetsDao() depuis Main.kt).
 */
package org.koitharu.kotatsu.desktop.ui.screens.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.crossfade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.desktop.ui.components.TopSearchBar
import org.koitharu.kotatsu.desktop.ui.components.TopSearchMenuAction
import org.koitharu.kotatsu.desktop.ui.strings.FR_BOOKMARKS
import org.koitharu.kotatsu.desktop.ui.strings.FR_CATALOG
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LOCAL_STORAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANGA_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_MORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_RANDOM
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCE_PRESETS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_EMPTY_HOLDER_PRIMARY
import org.koitharu.kotatsu.explore.data.SourcePresetsDao
import org.koitharu.kotatsu.explore.data.SourcePresetEntity
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaParserSource

private val QUICK_BUTTON_HEIGHT = 64.dp
private val QUICK_BUTTON_CORNER = 32.dp
private val SOURCE_FAVICON_SIZE = 64.dp

// Chaîne FR locale (phase P3) — ne pas déplacer dans Fr.kt (réservé à l'agent UI FR)
private const val FR_NEW_PRESET = "Nouveau préréglage"

/** Boutons rapides de l'onglet Source (captures de référence). */
public enum class SourceQuickAction {
	LOCAL_STORAGE,
	BOOKMARKS,
	RANDOM,
	DOWNLOADS,
	SOURCE_PRESETS,
}

/**
 * Onglet « Source ». @param suggestions mangas suggérés (pager) ; @param sources sources à
 * afficher dans « Sources des mangas » ; @param hasNewChapters ajoute le marqueur 🔥 (nouveaux
 * chapitres) devant le nom de la source ; @param presetsDao si non null, le créateur de
 * préréglages (« Nouveau préréglage » du menu ⋮) persiste dans la table source_presets.
 */
@Composable
public fun SourcePresetsScreen(
	suggestions: List<Manga>,
	sources: List<MangaParserSource>,
	domainOf: (MangaParserSource) -> String?,
	hasNewChapters: (MangaParserSource) -> Boolean,
	onSuggestionClick: (Manga) -> Unit,
	onSourceClick: (MangaParserSource) -> Unit,
	onCatalogClick: () -> Unit,
	onMoreSuggestionsClick: () -> Unit,
	onQuickAction: (SourceQuickAction) -> Unit,
	onSearchClick: () -> Unit,
	onIncognitoToggle: (Boolean) -> Unit,
	onSettingsClick: () -> Unit,
	onManageSourcesClick: () -> Unit,
	modifier: Modifier = Modifier,
	presetsDao: SourcePresetsDao? = null,
) {
	var incognito by remember { mutableStateOf(false) }
	var showPresetCreator by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	if (showPresetCreator) {
		PresetCreatorDialog(
			onDismiss = { showPresetCreator = false },
			onSave = { title, languages ->
				showPresetCreator = false
				val dao = presetsDao
				if (dao != null) {
					scope.launch(Dispatchers.IO) {
						runCatching {
							dao.insert(
								SourcePresetEntity(
									presetId = 0L, // autoGenerate
									title = title,
									languages = languages.joinToString(","),
									sources = "",
									createdAt = System.currentTimeMillis(),
									sortKey = dao.getNextSortKey(),
									deletedAt = 0L,
								),
							)
						}
					}
				}
			},
		)
	}

	Column(modifier = modifier.fillMaxSize()) {
		TopSearchBar(
			onSearchClick = onSearchClick,
			menuActions = listOf(
				TopSearchMenuAction(FR_NEW_PRESET, { showPresetCreator = true }),
				TopSearchMenuAction(FR_SOURCE_PRESETS, { onQuickAction(SourceQuickAction.SOURCE_PRESETS) }),
				TopSearchMenuAction(FR_MANAGE_SOURCES, onManageSourcesClick),
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
			trailingIcon = Icons.Filled.Source,
			onTrailingIconClick = onCatalogClick,
		)
		LazyVerticalGrid(
			// Colonnes adaptatives : sur desktop, la fenêtre est bien plus large qu'un téléphone ;
			// les tuiles gardent une taille raisonnable au lieu d'occuper un tiers d'écran
			columns = GridCells.Adaptive(110.dp),
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			// Boutons rapides : 2x2 + pleine largeur (captures)
			item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
				QuickButtons(
					onQuickAction = onQuickAction,
				)
			}
			// « Suggestions » + « Plus »
			item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
				SectionHeader(
					title = FR_SUGGESTIONS,
					actionLabel = FR_MORE,
					onActionClick = onMoreSuggestionsClick,
				)
			}
			item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
				SuggestionsPager(
					suggestions = suggestions,
					onSuggestionClick = onSuggestionClick,
				)
			}
			// « Sources des mangas » + « Catalogue »
			item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
				SectionHeader(
					title = FR_MANGA_SOURCES,
					actionLabel = FR_CATALOG,
					onActionClick = onCatalogClick,
				)
			}
			items(sources, key = { it.name }) { source ->
				SourceIcon(
					source = source,
					domain = domainOf(source),
					hasNewChapters = hasNewChapters(source),
					onClick = { onSourceClick(source) },
				)
			}
		}
	}
}

@Composable
private fun QuickButtons(onQuickAction: (SourceQuickAction) -> Unit) {
	Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
		Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			QuickButton(
				label = FR_LOCAL_STORAGE,
				icon = Icons.Outlined.SdCard,
				onClick = { onQuickAction(SourceQuickAction.LOCAL_STORAGE) },
				modifier = Modifier.weight(1f),
			)
			QuickButton(
				label = FR_BOOKMARKS,
				icon = Icons.Filled.Bookmark,
				onClick = { onQuickAction(SourceQuickAction.BOOKMARKS) },
				modifier = Modifier.weight(1f),
			)
		}
		Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			QuickButton(
				label = FR_RANDOM,
				icon = Icons.Outlined.Casino,
				onClick = { onQuickAction(SourceQuickAction.RANDOM) },
				modifier = Modifier.weight(1f),
			)
			QuickButton(
				label = FR_DOWNLOADS,
				icon = Icons.Filled.Download,
				onClick = { onQuickAction(SourceQuickAction.DOWNLOADS) },
				modifier = Modifier.weight(1f),
			)
		}
		QuickButton(
			label = FR_SOURCE_PRESETS,
			icon = Icons.Filled.FormatListBulleted,
			trailingArrow = true,
			onClick = { onQuickAction(SourceQuickAction.SOURCE_PRESETS) },
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

/** Bouton pill des raccourcis : icône + libellé en couleur primaire (captures). */
@Composable
private fun QuickButton(
	label: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	trailingArrow: Boolean = false,
) {
	Row(
		modifier = modifier
			.height(QUICK_BUTTON_HEIGHT)
			.clip(RoundedCornerShape(QUICK_BUTTON_CORNER))
			.background(MaterialTheme.colorScheme.surfaceContainerHighest)
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			imageVector = icon,
			contentDescription = label,
			tint = MaterialTheme.colorScheme.primary,
			modifier = Modifier.size(24.dp),
		)
		Text(
			text = label,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.primary,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = 12.dp),
		)
		if (trailingArrow) {
			Icon(
				imageVector = Icons.Filled.ArrowDropDown,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
			)
		}
	}
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onActionClick: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f),
		)
		Text(
			text = actionLabel,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.clickable(onClick = onActionClick),
		)
	}
}

/** Pager « Suggestions » : une suggestion par page + points de pagination (captures). */
@Composable
private fun SuggestionsPager(
	suggestions: List<Manga>,
	onSuggestionClick: (Manga) -> Unit,
) {
	if (suggestions.isEmpty()) {
		// État vide FR : aucune suggestion disponible (fetch en cours ou préférences vides)
		Text(
			text = FR_TEXT_EMPTY_HOLDER_PRIMARY,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 24.dp),
			textAlign = TextAlign.Center,
		)
		return
	}
	val pagerState = rememberPagerState { suggestions.size }
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxWidth(),
		) { page ->
			val manga = suggestions[page]
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { onSuggestionClick(manga) }
					.padding(vertical = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(RoundedCornerShape(8.dp))
						.background(MaterialTheme.colorScheme.surfaceContainer),
					contentAlignment = Alignment.Center,
				) {
					AsyncImage(
						model = coil3.request.ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
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
						.padding(start = 16.dp),
				) {
					Text(
						text = manga.title,
						style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					Text(
						text = manga.tags.joinToString(", ") { it.title },
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}
		// Points de pagination (point actif plus grand, comme la capture)
		Row(
			modifier = Modifier.padding(top = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			repeat(suggestions.size) { index ->
				Box(
					modifier = Modifier
						.size(if (index == pagerState.currentPage) 8.dp else 4.dp)
						.clip(CircleShape)
						.background(
							if (index == pagerState.currentPage) {
								MaterialTheme.colorScheme.onSurface
							} else {
								MaterialTheme.colorScheme.onSurfaceVariant
							},
						),
				)
			}
		}
	}
}

/** Favicon de source (grille « Sources des mangas »), 🔥 si nouveaux chapitres. */
@Composable
private fun SourceIcon(
	source: MangaParserSource,
	domain: String?,
	hasNewChapters: Boolean,
	onClick: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 4.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box(
			modifier = Modifier
				.size(SOURCE_FAVICON_SIZE)
				.clip(RoundedCornerShape(16.dp))
				.border(
					width = 1.dp,
					color = MaterialTheme.colorScheme.outlineVariant,
					shape = RoundedCornerShape(16.dp),
				)
				.background(MaterialTheme.colorScheme.surfaceContainerHighest),
			contentAlignment = Alignment.Center,
		) {
			// Chaîne de secours : favicon du site → service Google → icône générique
			org.koitharu.kotatsu.desktop.ui.components.SourceFavicon(
				domain = domain,
				contentDescription = source.title,
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp),
			)
		}
		Text(
			text = (if (hasNewChapters) "🔥 " else "") + source.title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 8.dp),
		)
	}
}
