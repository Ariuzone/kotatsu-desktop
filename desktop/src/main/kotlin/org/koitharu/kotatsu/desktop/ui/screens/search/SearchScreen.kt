/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — refonte fidèle à la
 * capture de référence (activity_search.xml d'origine) : barre du haut (←, hint
 * « Entrez le titre du manga, le genre ou le nom de la source », micro, ⋮), rangée horizontale de
 * suggestions (couvertures + titres), historique de requêtes (icône horloge) et suggestions de
 * sources (favicon + titre + « Mangas, <langue> »). Saisie = résultats en grille (MangaGrid).
 */
package org.koitharu.kotatsu.desktop.ui.screens.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import java.util.Locale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koitharu.kotatsu.desktop.ui.components.MangaGrid
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_HINT
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaParserSource

private val SUGGESTION_WIDTH = 120.dp // largeur des couvertures de suggestions (capture)
private val SUGGESTION_CORNER = 12.dp

/**
 * Recherche multi-sources. Saisie débounce 500 ms comme l'app d'origine ; [onQuery] déclenche
 * la recherche et les résultats arrivent via [results]. En état initial : suggestions
 * ([suggestions]), historique de requêtes ([history]) et suggestions de sources ([sources]).
 */
@OptIn(FlowPreview::class)
@Composable
public fun SearchScreen(
	results: List<Manga>,
	onQuery: suspend (String) -> Unit,
	onMangaClick: (Manga) -> Unit,
	onBackClick: () -> Unit,
	modifier: Modifier = Modifier,
	suggestions: List<Manga> = emptyList(),
	history: List<String> = emptyList(),
	sources: List<MangaParserSource> = emptyList(),
	domainOf: (MangaParserSource) -> String? = { null },
	onHistoryClick: (String) -> Unit = {},
	onSourceClick: (MangaParserSource) -> Unit = {},
) {
	var query by remember { mutableStateOf(TextFieldValue("")) }
	val queryFlow = remember { MutableStateFlow("") }
	val focusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
		queryFlow
			.debounce(500)
			.distinctUntilChanged()
			.collect { value -> onQuery(value) }
	}

	Column(modifier = modifier.fillMaxSize()) {
		// Barre du haut : ←  hint/micro  ⋮ (capture)
		Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
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
				BasicTextField(
					value = query,
					onValueChange = {
						query = it
						queryFlow.value = it.text
					},
					textStyle = TextStyle(
						color = MaterialTheme.colorScheme.onSurface,
						fontSize = MaterialTheme.typography.bodyLarge.fontSize,
					),
					cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
					singleLine = true,
					modifier = Modifier
						.weight(1f)
						.focusRequester(focusRequester),
					decorationBox = { inner ->
						Box {
							if (query.text.isEmpty()) {
								Text(
									text = FR_SEARCH_HINT,
									style = MaterialTheme.typography.bodyLarge,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis,
								)
							}
							inner()
						}
					},
				)
				IconButton(onClick = {}) {
					Icon(Icons.Filled.Mic, contentDescription = "Recherche vocale")
				}
				IconButton(onClick = {}) {
					Icon(Icons.Filled.MoreVert, contentDescription = null)
				}
			}
		}
		if (query.text.isNotEmpty() && results.isNotEmpty()) {
			// Résultats en grille (comme l'app d'origine, fragment_search)
			MangaGrid(
				mangaList = results,
				onMangaClick = onMangaClick,
			)
		} else {
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				// Suggestions de mangas : rangée horizontale de couvertures
				if (suggestions.isNotEmpty()) {
					item {
						LazyRow(
							contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
							horizontalArrangement = Arrangement.spacedBy(12.dp),
						) {
							itemsIndexed(suggestions) { _, manga ->
								SearchSuggestionCover(
									manga = manga,
									onClick = { onMangaClick(manga) },
								)
							}
						}
					}
				}
				// Historique de requêtes (icône horloge + flèche de ré-application)
				item {
					HistoryRow(
						history = history,
						onHistoryClick = onHistoryClick,
					)
				}
				// Suggestions de sources (favicon + titre + « Mangas, Français »)
				item {
					SourceSuggestions(
						sources = sources,
						domainOf = domainOf,
						onSourceClick = onSourceClick,
					)
				}
			}
		}
	}
}

/** Couverture de suggestion (capture : image 2:3 + titre sur fond pill sombre). */
@Composable
private fun SearchSuggestionCover(manga: Manga, onClick: () -> Unit) {
	Column(
		modifier = Modifier
			.width(SUGGESTION_WIDTH)
			.clickable(onClick = onClick),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(2f / 3f)
				.clip(RoundedCornerShape(SUGGESTION_CORNER))
				.background(MaterialTheme.colorScheme.surfaceContainer),
			contentAlignment = Alignment.Center,
		) {
			AsyncImage(
				model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
					.data(manga.coverUrl)
					.crossfade(true)
					.build(),
				contentDescription = manga.title,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize(),
			)
		}
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 6.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.surfaceContainerHighest)
				.padding(horizontal = 8.dp, vertical = 6.dp),
		) {
			Text(
				text = manga.title,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

/** Historique de requêtes : horloge + requête + séparateur + flèche « réappliquer ». */
@Composable
private fun HistoryRow(history: List<String>, onHistoryClick: (String) -> Unit) {
	for (entry in history) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onHistoryClick(entry) }
				.padding(horizontal = 16.dp, vertical = 14.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				imageVector = Icons.Filled.History,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
			)
			Text(
				text = entry,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier
					.weight(1f)
					.padding(start = 16.dp),
			)
			// Séparateur vertical + flèche de ré-application (capture)
			Box(
				modifier = Modifier
					.padding(horizontal = 12.dp)
					.width(1.dp)
					.height(24.dp)
					.background(MaterialTheme.colorScheme.outlineVariant),
			)
			Icon(
				imageVector = Icons.Filled.NorthWest,
				contentDescription = "Rechercher à nouveau",
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
		HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
	}
}

/** Suggestions de sources : favicon + titre + « Mangas, Français ». */
@Composable
private fun SourceSuggestions(
	sources: List<MangaParserSource>,
	domainOf: (MangaParserSource) -> String?,
	onSourceClick: (MangaParserSource) -> Unit,
) {
	for (source in sources) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onSourceClick(source) }
				.padding(horizontal = 16.dp, vertical = 10.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(MaterialTheme.colorScheme.surfaceContainerHighest),
				contentAlignment = Alignment.Center,
			) {
				// Chaîne de secours : favicon du site → service Google → icône générique
				org.koitharu.kotatsu.desktop.ui.components.SourceFavicon(
					domain = domainOf(source),
					contentDescription = source.title,
					modifier = Modifier
						.fillMaxSize()
						.padding(6.dp),
				)
			}
			Column(modifier = Modifier.padding(start = 16.dp)) {
				Text(
					text = source.title,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = sourceContentType(source) + ", " + localeDisplay(source.locale),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

private fun sourceContentType(source: MangaParserSource): String = when (source.contentType) {
	ContentType.HENTAI -> "Hentais"
	else -> "Mangas"
}

private fun localeDisplay(locale: String): String {
	if (locale.isBlank()) return "Langues diverses"
	val l = runCatching { Locale.forLanguageTag(locale) }.getOrNull() ?: return locale
	return l.getDisplayLanguage(l).ifBlank { locale }
}
