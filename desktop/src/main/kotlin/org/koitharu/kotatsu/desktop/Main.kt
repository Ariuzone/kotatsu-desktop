/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Fenêtre principale — reproduit la mise en page « tablette/desktop » de l'app Android
 * (rail de navigation latéral + volet de contenu), cf. activity_main.xml + navigation_rail_fab.xml.
 * Navigation : Explore → catalogue d'une source → détails d'un manga → lecteur (fenêtre dédiée,
 * comme ReaderActivity est une activité dédiée).
 */
package org.koitharu.kotatsu.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koitharu.kotatsu.core.APP_VERSION_NAME
import org.koitharu.kotatsu.desktop.ui.nav.NavItem
import org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderContent
import org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderMode
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.theme.KotatsuTheme
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaParserSource

fun main() = application {
	val container = remember { AppContainer.getInstance() }
	val settings = remember { DesktopSettings.getInstance() }
	Window(
		onCloseRequest = ::exitApplication,
		title = "Kotatsu $APP_VERSION_NAME",
		icon = kotatsuIcon(),
	) {
		KotatsuTheme(
			scheme = settings.colorScheme.value,
			mode = settings.themeMode.value,
			amoled = settings.isAmoled.value,
		) {
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
			MainScreen(container)
		}
		}
	}
}

@Composable
private fun kotatsuIcon(): Painter = painterResource("kotatsu_256.png")

@Composable
private fun MainScreen(container: AppContainer) {
	var selected by rememberSaveable { mutableStateOf(NavItem.EXPLORE) }
	var selectedSource by remember { mutableStateOf<MangaParserSource?>(null) }
	var selectedManga by remember { mutableStateOf<Manga?>(null) }
	var showSettings by remember { mutableStateOf(false) }
	var showSearch by remember { mutableStateOf(false) }
	var searchResults by remember { mutableStateOf<List<Manga>>(emptyList()) }
	var readerRequest by remember { mutableStateOf<MangaChapter?>(null) }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.onKeyEvent { event ->
				// Ctrl+F : recherche multi-sources (équivalent du bouton recherche des
				// barres d'outils de l'app d'origine)
				if (event.type == androidx.compose.ui.input.key.KeyEventType.KeyUp &&
					event.key == androidx.compose.ui.input.key.Key.F &&
					event.isCtrlPressed
				) {
					showSearch = true
					true
				} else {
					false
				}
			},
	) {
		// Rail de navigation latéral (comme l'app d'origine en mode tablette/desktop)
		NavigationRail(modifier = Modifier.align(Alignment.CenterStart)) {
			for (item in NavItem.default) {
				NavigationRailItem(
					selected = selected == item && selectedManga == null && selectedSource == null && !showSettings,
					onClick = {
						selected = item
						selectedSource = null
						selectedManga = null
						showSettings = false
					},
					icon = {
						Icon(
							imageVector = if (selected == item) item.filledIcon else item.outlinedIcon,
							contentDescription = item.title,
						)
					},
					label = { Text(item.title) },
				)
			}
			// Paramètres : dans l'app d'origine ils sont dans le menu du drawer principal ;
			// sur desktop ils sont accessibles en bas du rail (divergence documentée).
			Spacer(modifier = Modifier.weight(1f))
			NavigationRailItem(
				selected = showSettings,
				onClick = { showSettings = true },
				icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
				label = { Text("Settings") },
			)
		}
		// Volet de contenu
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(start = 80.dp),
			color = MaterialTheme.colorScheme.surface,
		) {
			when {
				showSettings ->
					org.koitharu.kotatsu.desktop.ui.screens.settings.AppearanceSettingsScreen(
						settings = DesktopSettings.getInstance(),
					)

				showSearch -> org.koitharu.kotatsu.desktop.ui.screens.search.SearchScreen(
					results = searchResults,
					onQuery = { query ->
						container.searchAll(query).also { searchResults = it }
					},
					onMangaClick = {
						showSearch = false
						selectedManga = it
					},
					onBackClick = { showSearch = false },
				)

				selectedManga != null -> {
					val seed = selectedManga!!
					var details by remember(seed.url) { mutableStateOf<Manga?>(null) }
					var chapters by remember(seed.url) { mutableStateOf<List<MangaChapter>>(emptyList()) }
					var error by remember(seed.url) { mutableStateOf<String?>(null) }
					LaunchedEffect(seed.url) {
						try {
							val d = container.getDetails(seed)
							details = d
							chapters = d.chapters.orEmpty()
						} catch (e: Exception) {
							error = e.message ?: e.javaClass.simpleName
						}
					}
					org.koitharu.kotatsu.desktop.ui.screens.details.DetailsScreen(
						manga = details ?: seed,
						chapters = chapters.map { it.name },
						isFavourite = false,
						onBackClick = { selectedManga = null },
						onChapterClick = { index -> chapters.getOrNull(index)?.let { readerRequest = it } },
						onFavouriteClick = {},
						onDownloadClick = {},
						onReadClick = { chapters.firstOrNull()?.let { readerRequest = it } },
					)
					if (error != null) {
						LaunchedEffect(error) {
							// L'erreur est visible via l'écran détails (liste chapitres vide) ;
							// journalisée ici pour le débogage.
							println("Details error: $error")
						}
					}
				}

				selectedSource != null -> {
					val source = selectedSource!!
					org.koitharu.kotatsu.desktop.ui.screens.catalog.CatalogScreen(
						source = source,
						fetchList = { s, offset -> container.getList(s, offset) },
						onMangaClick = { selectedManga = it },
						onBackClick = { selectedSource = null },
					)
				}

					else -> when (selected) {
						NavItem.EXPLORE ->
							org.koitharu.kotatsu.desktop.ui.screens.explore.ExploreScreen(
								onSourceClick = { selectedSource = it },
								domainOf = { container.domainOf(it) },
							)
						else -> PlaceholderScreen(selected)
					}
			}
		}
	}
	// Fenêtre lecteur (comme ReaderActivity d'origine : une fenêtre dédiée)
	val chapter = readerRequest
	if (chapter != null) {
		var pageUrls by remember(chapter) { mutableStateOf<List<String>>(emptyList()) }
		LaunchedEffect(chapter) {
			val pages = container.getPages(chapter)
			pageUrls = pages.map { container.getPageUrl(it) }
		}
		ReaderContent(
			title = chapter.name,
			chapterTitle = chapter.name,
			pageUrls = pageUrls,
			mode = ReaderMode.PAGER,
			onClose = { readerRequest = null },
		)
	}
}

@Composable
private fun PlaceholderScreen(item: NavItem) {
	Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
		Text(
			text = item.title,
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)
	}
}
