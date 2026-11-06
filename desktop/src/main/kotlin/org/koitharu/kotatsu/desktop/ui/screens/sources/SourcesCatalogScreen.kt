/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — « Sources catalogue »
 * (SourcesCatalogFragment d'origine), fidèle aux captures de référence : barre du haut (←, titre,
 * loupe), chips « Toutes les langues ▾ / Nouveaux / Marque-pages », liste favicon + titre +
 * « Mangas, <langue> » + bouton + (séparateur vertical), état vide Totoro (ic_empty_feed).
 */
package org.koitharu.kotatsu.desktop.ui.screens.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
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
import java.util.Locale
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.TotoroIllustration
import org.koitharu.kotatsu.desktop.ui.strings.FR_ALL_LANGUAGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTENT_TYPE_HENTAI
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTENT_TYPE_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_MISC_LANGUAGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEWEST
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCES_CATALOG
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_EMPTY_HOLDER_PRIMARY
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaParserSource

private val FAVICON_SIZE = 48.dp

/**
 * Catalogue des sources. @param isAdded indique si la source est déjà ajoutée à l'utilisateur ;
 * @param onSourceToggle bascule l'ajout/retrait (bouton + à droite).
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
) {
	val allSources = remember { MangaParserSource.entries.filterNot { it.isBroken } }
	var selectedLocale by remember { mutableStateOf<String?>(null) } // null = toutes les langues
	var newestOnly by remember { mutableStateOf(false) }
	var languageMenuOpen by remember { mutableStateOf(false) }

	// Tri comme côté Android : par titre, langues affichées dans leur langue
	val availableLocales = remember(allSources) {
		allSources.map { it.locale }.filter { it.isNotBlank() }.distinct().sorted()
	}
	val filtered = remember(allSources, selectedLocale, newestOnly) {
		allSources
			.filter { selectedLocale == null || it.locale == selectedLocale }
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
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
		// Chips « Toutes les langues ▾ / Nouveaux / Marque-pages »
		Box(modifier = Modifier.fillMaxWidth()) {
			LazyRow(
				contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
				horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
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
						colors = FilterChipDefaults.filterChipColors(
							containerColor = MaterialTheme.colorScheme.surfaceContainer,
						),
					)
				}
			}
			// Menu des langues (capture : Toutes les langues, Langues diverses, Français, …)
			if (languageMenuOpen) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.surfaceContainerHigh),
				) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = 8.dp),
					) {
						LanguageMenuItem(FR_ALL_LANGUAGES) {
							selectedLocale = null
							languageMenuOpen = false
						}
						LanguageMenuItem(FR_MISC_LANGUAGES) {
							selectedLocale = ""
							languageMenuOpen = false
						}
						for (locale in availableLocales) {
							LanguageMenuItem(localeLabel(locale)) {
								selectedLocale = locale
								languageMenuOpen = false
							}
						}
					}
				}
			}
		}
		if (filtered.isEmpty()) {
			EmptyStateScreen(
				illustration = TotoroIllustration.FEED, // ic_empty_feed comme SourcesCatalogViewModel
				textPrimary = FR_TEXT_EMPTY_HOLDER_PRIMARY,
			)
		} else {
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(filtered, key = { it.name }) { source ->
					SourceCatalogRow(
						source = source,
						faviconUrl = domainOf(source),
						added = isAdded(source),
						onClick = { onSourceClick(source) },
						onToggle = { onSourceToggle(source) },
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

@Composable
private fun LanguageMenuItem(label: String, onClick: () -> Unit) {
	Text(
		text = label,
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
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
	faviconUrl: String?,
	added: Boolean,
	onClick: () -> Unit,
	onToggle: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(FAVICON_SIZE)
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.surfaceContainerHighest),
			contentAlignment = Alignment.Center,
		) {
			if (faviconUrl != null) {
				AsyncImage(
					model = faviconUrl,
					contentDescription = source.title,
					contentScale = ContentScale.Fit,
					modifier = Modifier
						.fillMaxSize()
						.padding(6.dp),
				)
			}
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
				imageVector = Icons.Filled.Add,
				contentDescription = if (added) "Retirer" else "Ajouter",
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

/** Libellé de langue dans sa propre langue (« Русский », « Français »…). */
private fun localeLabel(locale: String): String {
	if (locale.isBlank()) return FR_MISC_LANGUAGES
	val l = runCatching { Locale.forLanguageTag(locale) }.getOrNull() ?: return locale
	val display = l.getDisplayLanguage(l).ifBlank { locale }
	return display.replaceFirstChar { it.uppercase(l) }
}
