/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — écran « Explorer »
 * (Discover), fidèle aux captures de référence : barre de recherche pill + menu ⋮ (Mettre à Jour,
 * Réafficher ce que j'ai écarté, Mode incognito, Paramètres), chips « ✓ Pour vous / Top / Mangas /
 * Manhwa », sections « Pour vous / Nouveautés pour vous / Plus pour vous » en rangées
 * horizontales de couvertures 2:3 avec titres et lien « Tout afficher »
 * (cf. fragment_explore.xml + item_explore_* de l'app d'origine).
 *
 * Les données sont injectées : [fetchSection] fournit les mangas d'une section pour le chip
 * actif (l'orchestrateur branche les sources, ex. MANGADEX).
 */
package org.koitharu.kotatsu.desktop.ui.screens.discover

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.desktop.ui.components.TopSearchBar
import org.koitharu.kotatsu.desktop.ui.components.TopSearchMenuAction
import org.koitharu.kotatsu.desktop.ui.strings.FR_FOR_YOU
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANHWA
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANGAS
import org.koitharu.kotatsu.desktop.ui.strings.FR_MORE_FOR_YOU
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_FINDS
import org.koitharu.kotatsu.desktop.ui.strings.FR_SHOW_ALL
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_EMPTY_HOLDER_PRIMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TOP
import org.koitharu.kotatsu.desktop.ui.strings.FR_UPDATE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SHOW_DISMISSED
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.parsers.model.Manga

private val SECTION_COVER_WIDTH = 128.dp
private val COVER_CORNER_RADIUS = 12.dp

/** Chips du haut (capture : « ✓ Pour vous / Top / Mangas / Manhwa »). */
public enum class DiscoverChip(public val label: String) {
	FOR_YOU(FR_FOR_YOU),
	TOP(FR_TOP),
	MANGAS(FR_MANGAS),
	MANHWA(FR_MANHWA),
}

/** Sections de l'écran Explorer (strings/for_you, new_finds, more_for_you). */
public enum class DiscoverSection(public val title: String) {
	FOR_YOU(FR_FOR_YOU),
	NEW_FINDS(FR_NEW_FINDS),
	MORE_FOR_YOU(FR_MORE_FOR_YOU),
}

/**
 * Écran « Explorer ». @param fetchSection mangas de [DiscoverSection] pour le [DiscoverChip]
 * actif (suspend, injecté — l'orchestrateur branchera les sources réelles).
 */
@Composable
public fun DiscoverScreen(
	fetchSection: suspend (section: DiscoverSection, chip: DiscoverChip) -> List<Manga>,
	onMangaClick: (Manga) -> Unit,
	onShowAllClick: (section: DiscoverSection) -> Unit,
	onSearchClick: () -> Unit,
	onUpdateClick: () -> Unit,
	onRestoreDismissedClick: () -> Unit,
	onIncognitoToggle: (Boolean) -> Unit,
	onSettingsClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var selectedChip by remember { mutableStateOf(DiscoverChip.FOR_YOU) }
	var incognito by remember { mutableStateOf(false) }

	Column(modifier = modifier.fillMaxSize()) {
		TopSearchBar(
			onSearchClick = onSearchClick,
			menuActions = listOf(
				TopSearchMenuAction(FR_UPDATE, onUpdateClick),
				TopSearchMenuAction(FR_SHOW_DISMISSED, onRestoreDismissedClick),
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
		// Chips « Pour vous / Top / Mangas / Manhwa »
		LazyRow(
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			items(DiscoverChip.entries, key = { it.name }) { chip ->
				FilterChip(
					selected = chip == selectedChip,
					onClick = { selectedChip = chip },
					label = { Text(chip.label) },
					leadingIcon = if (chip == selectedChip) {
						{
							Icon(
								imageVector = Icons.Filled.Check,
								contentDescription = null,
								modifier = Modifier.size(18.dp),
							)
						}
					} else {
						null
					},
					colors = FilterChipDefaults.filterChipColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
						selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
					),
					border = FilterChipDefaults.filterChipBorder(
						enabled = true,
						selected = chip == selectedChip,
						borderColor = MaterialTheme.colorScheme.outlineVariant,
					),
				)
			}
		}
		// Sections : « Pour vous », « Nouveautés pour vous », « Plus pour vous »
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			for (section in DiscoverSection.entries) {
				item(key = "header_${section.name}") {
					SectionHeader(
						title = section.title,
						onShowAllClick = { onShowAllClick(section) },
					)
				}
				item(key = "list_${section.name}") {
					SectionRow(
						section = section,
						chip = selectedChip,
						fetchSection = fetchSection,
						onMangaClick = onMangaClick,
					)
				}
			}
		}
	}
}

@Composable
private fun SectionHeader(title: String, onShowAllClick: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
		Text(
			text = FR_SHOW_ALL,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.clickable(onClick = onShowAllClick),
		)
	}
}

/** Rangée horizontale de couvertures 2:3 d'une section, avec chargement injecté. */
@Composable
private fun SectionRow(
	section: DiscoverSection,
	chip: DiscoverChip,
	fetchSection: suspend (DiscoverSection, DiscoverChip) -> List<Manga>,
	onMangaClick: (Manga) -> Unit,
) {
	var mangas by remember(section, chip) { mutableStateOf<List<Manga>>(emptyList()) }
	var isLoading by remember(section, chip) { mutableStateOf(true) }

	LaunchedEffect(section, chip) {
		isLoading = true
		mangas = try {
			fetchSection(section, chip)
		} catch (_: Exception) {
			emptyList() // les erreurs de section sont silencieuses comme côté Android
		} finally {
			isLoading = false
		}
	}

	if (isLoading) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(220.dp),
			contentAlignment = Alignment.Center,
		) {
			CircularProgressIndicator()
		}
	} else if (mangas.isEmpty()) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = FR_TEXT_EMPTY_HOLDER_PRIMARY,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	} else {
		LazyRow(
			contentPadding = PaddingValues(horizontal = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			itemsIndexed(mangas, key = { _, manga -> "${section.name}_${manga.url}" }) { _, manga ->
				DiscoverCover(
					manga = manga,
					onClick = { onMangaClick(manga) },
				)
			}
		}
	}
}

@Composable
private fun DiscoverCover(manga: Manga, onClick: () -> Unit) {
	Column(
		modifier = Modifier
			.width(SECTION_COVER_WIDTH)
			.clickable(onClick = onClick),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(2f / 3f)
				.clip(RoundedCornerShape(COVER_CORNER_RADIUS))
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
		Text(
			text = manga.title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(vertical = 4.dp),
		)
	}
}
