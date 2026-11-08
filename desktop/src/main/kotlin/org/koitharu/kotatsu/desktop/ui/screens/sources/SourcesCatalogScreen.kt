/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — « Sources catalogue »
 * (SourcesCatalogFragment d'origine), fidèle aux captures de référence : barre du haut (←, titre,
 * loupe), chips « Toutes les langues ▾ / Nouveaux / Marque-pages », liste favicon + titre +
 * « Mangas, <langue> » + bouton + (séparateur vertical), état vide Totoro (ic_empty_feed).
 * 2026-11-08 — phase P3 : parité vidéo — chips de filtres par catégorie (« Nouveaux », « Mangas »,
 * « Hentais », « Comics », « Autre », « Manhwa » déduites du ContentType du parser), compteur
 * « Disponible : N », menu de langues PLEIN ÉCRAN (liste natives + « Langues diverses »), état
 * vide FR exact de la vidéo, et bouton + fonctionnel : quand [appSettings] est fourni, le bouton
 * écrit directement dans appSettings.hiddenSources (source installée = coche ✓, sinon +).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 */
package org.koitharu.kotatsu.desktop.ui.screens.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import java.util.Locale
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.TotoroIllustration
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_REMOVE
import org.koitharu.kotatsu.desktop.ui.strings.FR_ADD
import org.koitharu.kotatsu.desktop.ui.strings.FR_ALL_LANGUAGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_AVAILABLE_COUNT
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACK
import org.koitharu.kotatsu.desktop.ui.strings.FR_CATEGORY_COMICS
import org.koitharu.kotatsu.desktop.ui.strings.FR_CATEGORY_OTHER
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTENT_TYPE_HENTAI
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTENT_TYPE_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_EMPTY_SOURCES_CATALOG_TEXT
import org.koitharu.kotatsu.desktop.ui.strings.FR_EMPTY_SOURCES_CATALOG_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANHWA
import org.koitharu.kotatsu.desktop.ui.strings.FR_MISC_LANGUAGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEWEST
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCES_CATALOG
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaParserSource

private val FAVICON_SIZE = 48.dp
private val FAVICON_CORNER = 12.dp // coins arrondis du favicon (capture)
private val CATALOG_ROW_HEIGHT = 88.dp // hauteur d'une ligne du catalogue (capture)

/** Filtres de contenu du catalogue (chips vidéo) ; AUTRE = tout le reste du ContentType parser. */
private enum class CatalogContentFilter { MANGAS, HENTAIS, COMICS, AUTRE, MANHWA }

/**
 * Catalogue des sources. @param isAdded indique si la source est déjà ajoutée à l'utilisateur ;
 * @param onSourceToggle bascule l'ajout/retrait (bouton + à droite) — utilisé uniquement quand
 * [appSettings] est null ; @param appSettings si non null, le bouton + écrit directement dans
 * appSettings.hiddenSources (source installée = coche ✓, sinon +), comme sur Android.
 */
@Composable
public fun SourcesCatalogScreen(
	domainOf: (MangaParserSource) -> String?,
	onSourceClick: (MangaParserSource) -> Unit,
	onSourceToggle: (MangaParserSource) -> Unit,
	isAdded: (MangaParserSource) -> Boolean,
	onBackClick: () -> Unit,
	onSearchClick: () -> Unit,
	modifier: Modifier = Modifier,
	appSettings: AppSettings? = null,
) {
	val allSources = remember { MangaParserSource.entries.filterNot { it.isBroken } }
	var selectedLocale by remember { mutableStateOf<String?>(null) } // null = toutes les langues
	var newestOnly by remember { mutableStateOf(false) }
	var contentFilter by remember { mutableStateOf<CatalogContentFilter?>(null) }
	var languageMenuOpen by remember { mutableStateOf(false) }
	// Révision du cache des sources masquées : incrémenté après chaque écriture dans appSettings
	var hiddenRevision by remember { mutableStateOf(0) }
	val hiddenSources = remember(hiddenRevision) { appSettings?.hiddenSources ?: emptySet() }

	// Tri comme côté Android : par titre, langues affichées dans leur langue
	val availableLocales = remember(allSources) {
		allSources.map { it.locale }.filter { it.isNotBlank() }.distinct()
			.sortedBy { localeLabel(it).lowercase(Locale.ROOT) }
	}
	val currentContentFilter = contentFilter
	val filtered = remember(allSources, selectedLocale, newestOnly, currentContentFilter) {
		allSources
			.filter { selectedLocale == null || it.locale == selectedLocale }
			.filter {
				newestOnly || currentContentFilter == null ||
					matchesContentFilter(it, currentContentFilter)
			}
			.sortedBy { it.title.lowercase() }
	}

	Column(modifier = modifier.fillMaxSize()) {
		// Barre du haut : ←  « Sources catalogue »  🔍
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.padding(horizontal = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(onClick = onBackClick) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = FR_BACK)
			}
			Text(
				text = FR_SOURCES_CATALOG,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.weight(1f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			IconButton(onClick = onSearchClick) {
				Icon(Icons.Filled.Search, contentDescription = FR_SOURCES_CATALOG)
			}
		}
		// Chips : langue ▾ + « Nouveaux » + catégories (« Mangas », « Hentais », « Comics »…)
		LazyRow(
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			item {
				FilterChip(
					selected = false,
					onClick = { languageMenuOpen = true },
					label = { Text(selectedLocale?.let { localeLabel(it) } ?: FR_ALL_LANGUAGES) },
					leadingIcon = {
						Icon(
							imageVector = Icons.Filled.Translate,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					},
					trailingIcon = {
						Icon(
							imageVector = Icons.Filled.ArrowDropDown,
							contentDescription = null,
							modifier = Modifier.size(18.dp),
						)
					},
					colors = FilterChipDefaults.filterChipColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
					),
				)
			}
			item {
				FilterChip(
					selected = newestOnly,
					onClick = { newestOnly = !newestOnly },
					label = { Text(FR_NEWEST) },
					leadingIcon = {
						// icône horloge comme la capture (chip « Nouveaux »)
						Icon(
							imageVector = Icons.Outlined.History,
							contentDescription = null,
							modifier = Modifier.size(18.dp),
						)
					},
					colors = FilterChipDefaults.filterChipColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
					),
				)
			}
			items(CatalogContentFilter.entries.toList(), key = { it.name }) { filter ->
				FilterChip(
					selected = contentFilter == filter,
					onClick = { contentFilter = if (contentFilter == filter) null else filter },
					label = { Text(contentFilterLabel(filter)) },
					colors = FilterChipDefaults.filterChipColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
					),
				)
			}
		}
		if (languageMenuOpen) {
			// Menu de langues PLEIN ÉCRAN (vidéo t≈2:30) : liste natives + « Langues diverses »
			LanguageMenuOverlay(
				locales = availableLocales,
				selectedLocale = selectedLocale,
				onSelect = { locale ->
					selectedLocale = locale
					languageMenuOpen = false
				},
			)
		} else {
			// Compteur de sources disponibles du catalogue (vidéo : « Disponible : 596 »)
			Text(
				text = FR_AVAILABLE_COUNT + allSources.size,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
			)
			if (filtered.isEmpty()) {
				EmptyStateScreen(
					illustration = TotoroIllustration.FEED, // ic_empty_feed comme SourcesCatalogViewModel
					textPrimary = FR_EMPTY_SOURCES_CATALOG_TITLE,
					textSecondary = FR_EMPTY_SOURCES_CATALOG_TEXT,
				)
			} else {
				LazyColumn(modifier = Modifier.fillMaxSize()) {
					items(filtered, key = { it.name }) { source ->
						SourceCatalogRow(
							source = source,
							domain = domainOf(source),
							added = isSourceAdded(source, appSettings, hiddenSources, isAdded),
							onClick = { onSourceClick(source) },
							onToggle = { toggleSource(source, appSettings) { onSourceToggle(source) }; hiddenRevision++ },
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
}

/** Décide de l'état « installée » : appSettings.hiddenSources si fourni, sinon [isAdded]. */
private fun isSourceAdded(
	source: MangaParserSource,
	appSettings: AppSettings?,
	hiddenSources: Set<String>,
	isAdded: (MangaParserSource) -> Boolean,
): Boolean = if (appSettings != null) {
	source.name !in hiddenSources
} else {
	isAdded(source)
}

/** Toggle du bouton + : écrit directement dans appSettings.hiddenSources si [appSettings] != null. */
private inline fun toggleSource(
	source: MangaParserSource,
	appSettings: AppSettings?,
	fallback: () -> Unit,
) {
	if (appSettings != null) {
		val hidden = appSettings.hiddenSources
		appSettings.hiddenSources = if (source.name in hidden) {
			hidden - source.name
		} else {
			hidden + source.name
		}
	} else {
		fallback()
	}
}

/** Menu de langues plein écran : « Toutes les langues », langues natives triées, « Langues diverses ». */
@Composable
private fun LanguageMenuOverlay(
	locales: List<String>,
	selectedLocale: String?,
	onSelect: (String?) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceContainerHigh),
	) {
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			item(key = "__all") {
				LanguageMenuItem(
					label = FR_ALL_LANGUAGES,
					selected = selectedLocale == null,
				) { onSelect(null) }
			}
			items(locales, key = { it }) { locale ->
				LanguageMenuItem(
					label = localeLabel(locale),
					selected = selectedLocale == locale,
				) { onSelect(locale) }
			}
			item(key = "__misc") {
				LanguageMenuItem(
					label = FR_MISC_LANGUAGES,
					selected = selectedLocale == "",
				) { onSelect("") }
			}
		}
	}
}

@Composable
private fun LanguageMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
	Text(
		text = label,
		style = MaterialTheme.typography.bodyLarge,
		color = if (selected) {
			MaterialTheme.colorScheme.primary
		} else {
			MaterialTheme.colorScheme.onSurface
		},
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 24.dp, vertical = 14.dp),
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
	)
}

/** Ligne du catalogue : favicon, titre, « Mangas, Русский », séparateur vertical, bouton +. */
@Composable
private fun SourceCatalogRow(
	source: MangaParserSource,
	domain: String?,
	added: Boolean,
	onClick: () -> Unit,
	onToggle: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(CATALOG_ROW_HEIGHT)
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(FAVICON_SIZE)
				.clip(RoundedCornerShape(FAVICON_CORNER))
				.background(MaterialTheme.colorScheme.surfaceContainerHighest),
			contentAlignment = Alignment.Center,
		) {
			// Chaîne de secours : favicon du site → service Google → icône générique
			org.koitharu.kotatsu.desktop.ui.components.SourceFavicon(
				domain = domain,
				contentDescription = source.title,
				modifier = Modifier
					.fillMaxSize()
					.padding(6.dp),
			)
		}
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = 16.dp),
		) {
			Text(
				text = source.title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = contentTypeLabel(source) + ", " + localeLabel(source.locale),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
		// Séparateur vertical avant le bouton + (capture)
		Box(
			modifier = Modifier
				.padding(horizontal = 8.dp)
				.width(1.dp)
				.height(40.dp)
				.background(MaterialTheme.colorScheme.outlineVariant),
		)
		IconButton(onClick = onToggle) {
			Icon(
				imageVector = if (added) Icons.Filled.Check else Icons.Filled.Add,
				contentDescription = if (added) FR_ACTION_REMOVE else FR_ADD,
				tint = MaterialTheme.colorScheme.onSurface,
			)
		}
	}
}

/** « Mangas » / « Hentais » (strings/content_type_*). */
private fun contentTypeLabel(source: MangaParserSource): String = when (source.contentType) {
	ContentType.HENTAI -> FR_CONTENT_TYPE_HENTAI
	else -> FR_CONTENT_TYPE_MANGA
}

/** Libellé des chips de catégorie (vidéo t≈9:35). */
private fun contentFilterLabel(filter: CatalogContentFilter): String = when (filter) {
	CatalogContentFilter.MANGAS -> FR_CONTENT_TYPE_MANGA
	CatalogContentFilter.HENTAIS -> FR_CONTENT_TYPE_HENTAI
	CatalogContentFilter.COMICS -> FR_CATEGORY_COMICS
	CatalogContentFilter.AUTRE -> FR_CATEGORY_OTHER
	CatalogContentFilter.MANHWA -> FR_MANHWA
}

/** Filtre par catégorie, déduit des métadonnées du parser (ContentType de la source). */
private fun matchesContentFilter(source: MangaParserSource, filter: CatalogContentFilter): Boolean =
	when (filter) {
		CatalogContentFilter.MANGAS -> source.contentType == ContentType.MANGA
		CatalogContentFilter.HENTAIS -> source.contentType == ContentType.HENTAI
		CatalogContentFilter.COMICS -> source.contentType == ContentType.COMICS
		CatalogContentFilter.MANHWA -> source.contentType == ContentType.MANHWA
		CatalogContentFilter.AUTRE -> source.contentType !in setOf(
			ContentType.MANGA,
			ContentType.MANHWA,
			ContentType.HENTAI,
			ContentType.COMICS,
		)
	}

/** Libellé de langue dans sa propre langue (« Русский », « Français »…). */
private fun localeLabel(locale: String): String {
	if (locale.isBlank()) return FR_MISC_LANGUAGES
	val l = runCatching { Locale.forLanguageTag(locale) }.getOrNull() ?: return locale
	val display = l.getDisplayLanguage(l).ifBlank { locale }
	return display.replaceFirstChar { it.uppercase(l) }
}
