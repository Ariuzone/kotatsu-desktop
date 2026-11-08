/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P3 : écran
 * « Gérer les sources » (ManageSourcesFragment d'origine), fidèle aux captures de référence
 * (frames 30, 36-37 de la vidéo) : top bar avec recherche intégrée (loupe → champ de filtre)
 * et menu ⋮ (« Désactiver le contenu explicite », « Tout désactiver », « Activer toutes les
 * sources »), liste favicon + nom + « <Catégorie>, <Langue> » + menu ⋮ par ligne (entrée
 * « Désactivée ») + icône œil barré si la source est désactivée ; vue grille optionnelle.
 *
 * Accès au registry de sources : MangaParserSource.entries (parsers), comme SourcesCatalogScreen.
 * L'activation/désactivation ajoute/retire le nom (MangaSource.name) du source dans
 * AppSettings.hiddenSources (Set<String>), comme côté Android.
 * Divergences : MangaParserSource n'expose pas de flag NSFW — « contenu explicite » = sources
 * ContentType.HENTAI (cf. SourcesCatalogScreen) ; [domainOf] est injecté (optionnel) pour les
 * favicons, faute d'accès au MangaLoaderContext depuis l'écran (cf. AppContainer.domainOf).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.SourceFavicon
import org.koitharu.kotatsu.desktop.ui.strings.FR_CATEGORY_COMICS
import org.koitharu.kotatsu.desktop.ui.strings.FR_CATEGORY_OTHER
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTENT_TYPE_HENTAI
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTENT_TYPE_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISABLE_ALL
import org.koitharu.kotatsu.desktop.ui.strings.FR_DISABLE_EXPLICIT
import org.koitharu.kotatsu.desktop.ui.strings.FR_ENABLE_ALL
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_MISC_LANGUAGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_NO_SOURCES_FOUND
import org.koitharu.kotatsu.desktop.ui.strings.FR_SEARCH_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCE_DISABLED
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaParserSource

private val SOURCE_ROW_HEIGHT = 64.dp
private val SOURCE_FAVICON_SIZE = 44.dp
private val SOURCE_FAVICON_CORNER = 10.dp
private val GRID_FAVICON_SIZE = 64.dp

/**
 * Écran « Gérer les sources ». Liste toutes les sources du registry (parsers) ; un clic sur une
 * ligne (ou l'entrée « Désactivée » de son menu ⋮) bascule son activation en l'ajoutant/retirant
 * de [AppSettings.hiddenSources]. Le tri suit [AppSettings.sourcesSortOrder] (« SCORE » = ordre
 * du registry, « ALPHABETIC » = tri par nom) et la vue grille suit [AppSettings.isSourcesGrid].
 *
 * @param domainOf domaine d'une source (favicon), cf. AppContainer.domainOf ; null par défaut.
 */
@Composable
public fun ManageSourcesScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
	domainOf: (MangaParserSource) -> String? = { null },
) {
	// États locaux reflétés dans le store (FileKeyValueStore n'est pas observable)
	var query by remember { mutableStateOf("") }
	var searchActive by remember { mutableStateOf(false) }
	var hidden by remember { mutableStateOf(appSettings.hiddenSources) }
	var showGrid by remember { mutableStateOf(appSettings.isSourcesGrid) }
	var sourcesOrder by remember { mutableStateOf(appSettings.sourcesSortOrder) }
	var menuOpen by remember { mutableStateOf(false) }

	val allSources = remember { MangaParserSource.entries.filterNot { it.isBroken } }
	val nsfwNames = remember(allSources) {
		allSources.filter { it.contentType == ContentType.HENTAI }.map { it.name }.toSet()
	}
	val filtered = remember(allSources, query, sourcesOrder) {
		val base = if (sourcesOrder == "ALPHABETIC") {
			allSources.sortedBy { it.title.lowercase() }
		} else {
			allSources // « SCORE » : ordre du registry, comme l'app d'origine
		}
		if (query.isBlank()) {
			base
		} else {
			base.filter {
				it.name.contains(query, ignoreCase = true) ||
					it.title.contains(query, ignoreCase = true)
			}
		}
	}

	/** Ajoute/retire le nom du source dans hiddenSources (représentation : MangaSource.name). */
	fun toggleSource(source: MangaParserSource) {
		hidden = if (source.name in hidden) hidden - source.name else hidden + source.name
		appSettings.hiddenSources = hidden
	}

	Column(modifier = Modifier.fillMaxSize()) {
		// --- Top bar : ← / « Gérer les sources » / loupe / ⋮ (ou champ de recherche) ---
		if (searchActive) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(64.dp)
					.padding(horizontal = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				TextField(
					value = query,
					onValueChange = { query = it },
					placeholder = { Text(text = FR_SEARCH_SOURCES) },
					leadingIcon = {
						Icon(imageVector = Icons.Filled.Search, contentDescription = null)
					},
					trailingIcon = {
						IconButton(onClick = {
							query = ""
							searchActive = false
						}) {
							Icon(imageVector = Icons.Filled.Close, contentDescription = null)
						}
					},
					singleLine = true,
					colors = TextFieldDefaults.colors(
						focusedContainerColor = Color.Transparent,
						unfocusedContainerColor = Color.Transparent,
						focusedIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent,
					),
					modifier = Modifier.weight(1f),
				)
				Box {
					IconButton(onClick = { menuOpen = true }) {
						Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
					}
					ManageSourcesMenu(
						expanded = menuOpen,
						onDismiss = { menuOpen = false },
						explicitDisabled = nsfwNames.isNotEmpty() && nsfwNames.all { it in hidden },
						onDisableExplicit = {
							hidden = hidden + nsfwNames
							appSettings.hiddenSources = hidden
						},
						onEnableExplicit = {
							hidden = hidden - nsfwNames
							appSettings.hiddenSources = hidden
						},
						onDisableAll = {
							hidden = hidden + allSources.map { it.name }.toSet()
							appSettings.hiddenSources = hidden
						},
						onEnableAll = {
							hidden = emptySet()
							appSettings.hiddenSources = hidden
						},
					)
				}
			}
		} else {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(64.dp)
					.padding(horizontal = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				IconButton(onClick = onBackClick) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface,
					)
				}
				Text(
					text = FR_MANAGE_SOURCES,
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.weight(1f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				IconButton(onClick = { searchActive = true }) {
					Icon(
						imageVector = Icons.Filled.Search,
						contentDescription = FR_SEARCH_SOURCES,
						tint = MaterialTheme.colorScheme.onSurface,
					)
				}
				Box {
					IconButton(onClick = { menuOpen = true }) {
						Icon(
							imageVector = Icons.Filled.MoreVert,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurface,
						)
					}
					ManageSourcesMenu(
						expanded = menuOpen,
						onDismiss = { menuOpen = false },
						explicitDisabled = nsfwNames.isNotEmpty() && nsfwNames.all { it in hidden },
						onDisableExplicit = {
							hidden = hidden + nsfwNames
							appSettings.hiddenSources = hidden
						},
						onEnableExplicit = {
							hidden = hidden - nsfwNames
							appSettings.hiddenSources = hidden
						},
						onDisableAll = {
							hidden = hidden + allSources.map { it.name }.toSet()
							appSettings.hiddenSources = hidden
						},
						onEnableAll = {
							hidden = emptySet()
							appSettings.hiddenSources = hidden
						},
					)
				}
			}
		}
		// --- Contenu : liste (ou grille) des sources ---
		if (filtered.isEmpty()) {
			EmptyStateScreen(
				icon = Icons.Outlined.SearchOff,
				textPrimary = FR_NO_SOURCES_FOUND,
			)
		} else if (showGrid) {
			LazyVerticalGrid(
				columns = GridCells.Adaptive(96.dp),
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(16.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				items(filtered, key = { it.name }) { source ->
					SourceGridCard(
						source = source,
						domain = domainOf(source),
						enabled = source.name !in hidden,
						onClick = { toggleSource(source) },
					)
				}
			}
		} else {
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(filtered, key = { it.name }) { source ->
					ManageSourceRow(
						source = source,
						domain = domainOf(source),
						enabled = source.name !in hidden,
						onToggle = { toggleSource(source) },
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

/** Menu ⋮ global de la top bar (vidéo frame 36 : entrées avec case à cocher). */
@Composable
private fun ManageSourcesMenu(
	expanded: Boolean,
	onDismiss: () -> Unit,
	explicitDisabled: Boolean,
	onDisableExplicit: () -> Unit,
	onEnableExplicit: () -> Unit,
	onDisableAll: () -> Unit,
	onEnableAll: () -> Unit,
) {
	DropdownMenu(
		expanded = expanded,
		onDismissRequest = onDismiss,
		modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
	) {
		DropdownMenuItem(
			text = { Text(text = FR_DISABLE_EXPLICIT) },
			onClick = {
				onDismiss()
				if (explicitDisabled) onEnableExplicit() else onDisableExplicit()
			},
			trailingIcon = { Checkbox(checked = explicitDisabled, onCheckedChange = null) },
		)
		DropdownMenuItem(
			text = { Text(text = FR_DISABLE_ALL) },
			onClick = {
				onDismiss()
				onDisableAll()
			},
		)
		DropdownMenuItem(
			text = { Text(text = FR_ENABLE_ALL) },
			onClick = {
				onDismiss()
				onEnableAll()
			},
		)
	}
}

/** Ligne de source : favicon, nom, « <Catégorie>, <Langue> », œil barré si désactivée, menu ⋮. */
@Composable
private fun ManageSourceRow(
	source: MangaParserSource,
	domain: String?,
	enabled: Boolean,
	onToggle: () -> Unit,
) {
	var rowMenuOpen by remember { mutableStateOf(false) }
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(SOURCE_ROW_HEIGHT)
			.clickable(onClick = onToggle)
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(SOURCE_FAVICON_SIZE)
				.clip(RoundedCornerShape(SOURCE_FAVICON_CORNER))
				.background(MaterialTheme.colorScheme.surfaceContainerHighest),
			contentAlignment = Alignment.Center,
		) {
			SourceFavicon(
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
				.padding(horizontal = 16.dp),
		) {
			Text(
				text = source.title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = categoryLabel(source) + ", " + localeLabel(source.locale),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
		if (!enabled) {
			Icon(
				imageVector = Icons.Filled.VisibilityOff,
				contentDescription = FR_SOURCE_DISABLED,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(end = 4.dp),
			)
		}
		Box {
			IconButton(onClick = { rowMenuOpen = true }) {
				Icon(
					imageVector = Icons.Filled.MoreVert,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
			DropdownMenu(
				expanded = rowMenuOpen,
				onDismissRequest = { rowMenuOpen = false },
				modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
			) {
				DropdownMenuItem(
					text = { Text(text = FR_SOURCE_DISABLED) },
					onClick = {
						rowMenuOpen = false
						onToggle()
					},
					trailingIcon = { Checkbox(checked = !enabled, onCheckedChange = null) },
				)
			}
		}
	}
}

/** Carte de la vue grille (« Afficher en vue grille ») : favicon + nom, clic = bascule. */
@Composable
private fun SourceGridCard(
	source: MangaParserSource,
	domain: String?,
	enabled: Boolean,
	onClick: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.clickable(onClick = onClick)
			.padding(8.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box(contentAlignment = Alignment.TopEnd) {
			Box(
				modifier = Modifier
					.size(GRID_FAVICON_SIZE)
					.clip(RoundedCornerShape(16.dp))
					.border(
						width = 1.dp,
						color = MaterialTheme.colorScheme.outlineVariant,
						shape = RoundedCornerShape(16.dp),
					)
					.background(MaterialTheme.colorScheme.surfaceContainerHighest),
				contentAlignment = Alignment.Center,
			) {
				SourceFavicon(
					domain = domain,
					contentDescription = source.title,
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp),
				)
			}
			if (!enabled) {
				Icon(
					imageVector = Icons.Filled.VisibilityOff,
					contentDescription = FR_SOURCE_DISABLED,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(20.dp),
				)
			}
		}
		Text(
			text = source.title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 8.dp),
		)
	}
}

/** Catégorie du source (sous-titre) : Mangas/Hentais/Comics/Autre, d'après le ContentType. */
private fun categoryLabel(source: MangaParserSource): String = when (source.contentType) {
	ContentType.HENTAI -> FR_CONTENT_TYPE_HENTAI
	ContentType.COMICS -> FR_CATEGORY_COMICS
	ContentType.DOUJINSHI,
	ContentType.IMAGE_SET,
	ContentType.ARTIST_CG,
	ContentType.GAME_CG,
	ContentType.OTHER -> FR_CATEGORY_OTHER
	else -> FR_CONTENT_TYPE_MANGA
}

/** Libellé de langue dans sa propre langue (« Русский », « Français »…), cf. SourcesCatalogScreen. */
private fun localeLabel(locale: String): String {
	if (locale.isBlank()) return FR_MISC_LANGUAGES
	val l = runCatching { Locale.forLanguageTag(locale) }.getOrNull() ?: return locale
	val display = l.getDisplayLanguage(l).ifBlank { locale }
	return display.replaceFirstChar { it.uppercase(l) }
}
