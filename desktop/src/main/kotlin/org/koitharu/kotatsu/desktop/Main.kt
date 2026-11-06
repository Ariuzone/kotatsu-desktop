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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import org.koitharu.kotatsu.desktop.ui.screens.history.HistoryItem
import org.koitharu.kotatsu.desktop.ui.strings.FR_HISTORY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_EXPLORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCE
import org.koitharu.kotatsu.desktop.ui.strings.FR_FEED
import org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderContent
import org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderMode
import org.koitharu.kotatsu.desktop.tracker.TrackerTray
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.theme.KotatsuTheme
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.core.db.entity.toManga
import org.koitharu.kotatsu.core.db.entity.toMangaTag
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaParserSource

fun main() = application {
	val container = remember { AppContainer.getInstance() }
	val settings = remember { DesktopSettings.getInstance() }

	// Notifications nouveaux chapitres (icône tray + bulles Windows), cf. TrackWorker d'origine
	TrackerTray(notifier = container.trackerNotifier)
	LaunchedEffect(Unit) { container.trackerScheduler.start() }

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
		if (container.protectManager.isProtectionEnabled && !container.protectManager.isUnlocked) {
			org.koitharu.kotatsu.desktop.ui.screens.protect.ProtectScreen(
				manager = container.protectManager,
				onUnlocked = { /* recomposition : isUnlocked devient vrai */ },
				onCancel = ::exitApplication,
				modifier = Modifier.fillMaxSize(),
			)
		} else {
			Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
				MainScreen(container)
			}
		}
		}
	}
}

@Composable
private fun kotatsuIcon(): Painter = painterResource("kotatsu_256.png")

@Composable
private fun MainScreen(container: AppContainer) {
	// Onglet de démarrage surchargeable pour les vérifications automatisées :
	// -Dkotatsu.startTab=HISTORY|FAVORITES|EXPLORE|SOURCES|FEED
	var selected by rememberSaveable {
		mutableStateOf(
			runCatching { NavItem.valueOf(System.getProperty("kotatsu.startTab", "EXPLORE")) }
				.getOrDefault(NavItem.EXPLORE),
		)
	}
	var selectedSource by remember { mutableStateOf<MangaParserSource?>(null) }
	var selectedManga by remember { mutableStateOf<Manga?>(null) }
	var showSettings by remember { mutableStateOf(false) }
	var showSearch by remember { mutableStateOf(false) }
	var showDownloads by remember { mutableStateOf(false) }
	var showSourcesCatalog by remember { mutableStateOf(false) }
	var incognito by remember {
		mutableStateOf(container.appSettings.isIncognitoModeEnabled)
	}
	var suggestions by remember { mutableStateOf<List<Manga>>(emptyList()) }
	val sourcesList = remember {
		MangaParserSource.entries.filterNot { it.isBroken }.sortedBy { it.title }
	}
	LaunchedEffect(Unit) {
		// Suggestions de l'onglet Source : populaires MangaDex, comme le preset d'origine
		runCatching {
			val md = MangaParserSource.entries.first { it.name == "MANGADEX" }
			container.getList(md, 0, org.koitharu.kotatsu.parsers.model.SortOrder.POPULARITY).take(10)
		}.getOrDefault(emptyList()).let { suggestions = it }
	}
	var searchResults by remember { mutableStateOf<List<Manga>>(emptyList()) }
	var readerRequest by remember { mutableStateOf<Pair<Manga, MangaChapter>?>(null) }
	val scope = rememberCoroutineScope()

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
		// Rail de navigation latéral — mise en page « tablette/desktop » de l'app d'origine ;
		// onglets = navigation basse du téléphone (Historique, Favoris, Explorer, Source, Flux)
		NavigationRail(modifier = Modifier.align(Alignment.CenterStart)) {
			for (item in listOf(NavItem.HISTORY, NavItem.FAVORITES, NavItem.EXPLORE, NavItem.SOURCES, NavItem.FEED)) {
				NavigationRailItem(
					selected = selected == item && selectedManga == null && selectedSource == null &&
						!showSettings && !showDownloads && !showSourcesCatalog && !showSearch,
					onClick = {
						selected = item
						selectedSource = null
						selectedManga = null
						showSettings = false
						showDownloads = false
						showSourcesCatalog = false
						showSearch = false
					},
					icon = {
						Icon(
							imageVector = if (selected == item) item.filledIcon else item.outlinedIcon,
							contentDescription = item.title,
						)
					},
					label = {
						Text(
							when (item) {
								NavItem.HISTORY -> FR_HISTORY
								NavItem.FAVORITES -> FR_FAVOURITES
								NavItem.EXPLORE -> FR_EXPLORE
								NavItem.SOURCES -> FR_SOURCE
								else -> FR_FEED
							},
						)
					},
				)
			}
			// Paramètres : dans l'app d'origine ils sont dans le menu ⋮ et le drawer ;
			// sur desktop ils sont accessibles en bas du rail (divergence documentée).
			Spacer(modifier = Modifier.weight(1f))
			NavigationRailItem(
				selected = showDownloads,
				onClick = { showDownloads = true },
				icon = { Icon(Icons.Filled.Download, contentDescription = "Downloads") },
				label = { Text("Downloads") },
			)
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
				showSourcesCatalog ->
					org.koitharu.kotatsu.desktop.ui.screens.sources.SourcesCatalogScreen(
						domainOf = { container.domainOf(it) },
						onSourceClick = {
							showSourcesCatalog = false
							selectedSource = it
						},
						onSourceToggle = { /* activation/désactivation : P3 */ },
						isAdded = { true },
						onBackClick = { showSourcesCatalog = false },
						onSearchClick = { showSearch = true },
					)

				showDownloads -> {
					// Téléchargements réels (DownloadManager.observeState)
					val states by container.downloadManager.observeState().collectAsState()
					org.koitharu.kotatsu.desktop.ui.screens.downloads.DownloadsScreen(
						items = states.map { s ->
							org.koitharu.kotatsu.desktop.ui.screens.downloads.DownloadItem(
								id = java.util.UUID.nameUUIDFromBytes(byteArrayOf((s.manga.id and 0xff).toByte())),
								manga = s.manga,
								state = when {
									s.isStopped && s.error != null ->
										org.koitharu.kotatsu.desktop.ui.screens.downloads.DownloadUiState.FAILED
									s.isStopped && s.error == null ->
										org.koitharu.kotatsu.desktop.ui.screens.downloads.DownloadUiState.SUCCEEDED
									s.isStopped ->
										org.koitharu.kotatsu.desktop.ui.screens.downloads.DownloadUiState.CANCELLED
									else ->
										org.koitharu.kotatsu.desktop.ui.screens.downloads.DownloadUiState.RUNNING
								},
								isPaused = s.isPaused,
								isIndeterminate = s.isIndeterminate,
								progress = s.progress,
								max = s.max,
								error = s.errorMessage ?: s.error?.message,
								eta = if (s.eta > 0) java.time.Instant.ofEpochMilli(s.timestamp + s.eta) else null,
								isStuck = s.isStuck,
								chaptersDownloaded = s.downloadedChapters,
							)
						},
						onItemClick = { item ->
							item.manga?.let { selectedManga = it }
							showDownloads = false
						},
						onPauseClick = { item -> item.manga?.let { container.downloadManager.pause(it.id) } },
						onResumeClick = { item -> item.manga?.let { container.downloadManager.resume(it.id) } },
						onCancelClick = { item -> item.manga?.let { container.downloadManager.cancel(it.id) } },
						onBackClick = { showDownloads = false },
					)
				}

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
					suggestions = suggestions,
					sources = sourcesList.take(8),
					domainOf = { container.domainOf(it) },
					onSourceClick = {
						showSearch = false
						selectedSource = it
					},
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
						onChapterClick = { index ->
							chapters.getOrNull(index)?.let { readerRequest = (details ?: seed) to it }
						},
						onFavouriteClick = {},
						onDownloadClick = {
							container.downloadManager.enqueue(manga = details ?: seed)
						},
						onReadClick = { chapters.firstOrNull()?.let { readerRequest = (details ?: seed) to it } },
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
						NavItem.HISTORY -> {
							// Historique réel depuis la DB Room (HistoryDao)
							var historyItems by remember {
								mutableStateOf<List<org.koitharu.kotatsu.desktop.ui.screens.history.HistoryItem>>(emptyList())
							}
							LaunchedEffect(Unit) {
								val library = container.library
								historyItems = library.getHistory().mapNotNull { row ->
									val (manga, history) = library.run { row.toDisplay() }
									org.koitharu.kotatsu.desktop.ui.screens.history.HistoryItem(
										manga = manga,
										chapter = null, // chapitre résolu au clic via getDetails
										percent = history.percent,
										updatedAt = java.time.Instant.ofEpochMilli(history.updatedAt),
									)
								}
							}
							org.koitharu.kotatsu.desktop.ui.screens.history.HistoryScreen(
								historyItems = historyItems,
								onMangaClick = { selectedManga = it },
								onContinueClick = { item ->
									// « Continue reading » : retrouve le chapitre courant via la source
									scope.launch {
										val details = runCatching { container.getDetails(item.manga) }.getOrNull()
										val chapter = details?.chapters?.firstOrNull()
										if (chapter != null) readerRequest = item.manga to chapter
									}
								},
							)
						}

						NavItem.FAVORITES -> {
							// Favoris réels depuis la DB Room (FavouritesDao + catégories)
							var categories by remember {
								mutableStateOf<List<org.koitharu.kotatsu.desktop.ui.screens.favourites.FavouriteCategory>>(emptyList())
							}
							LaunchedEffect(Unit) {
								val library = container.library
								val all = library.getFavourites()
								val cats = library.getFavouriteCategories()
								categories = buildList {
									add(
										org.koitharu.kotatsu.desktop.ui.screens.favourites.FavouriteCategory(
											id = -1L,
											title = null, // « All favorites » comme FavouritesTabConfigurationStrategy
											count = all.size,
										),
									)
									for (c in cats) {
										add(
											org.koitharu.kotatsu.desktop.ui.screens.favourites.FavouriteCategory(
												id = c.categoryId.toLong(),
												title = c.title,
												count = all.count { f -> f.favourite.categoryId == c.categoryId.toLong() },
											),
										)
									}
								}
							}
							org.koitharu.kotatsu.desktop.ui.screens.favourites.FavouritesScreen(
								categories = categories,
								fetchManga = { category ->
									container.library.getFavourites()
										.filter { category.id == -1L || it.favourite.categoryId.toLong() == category.id }
										.map { it.manga.toManga(emptySet(), chapters = null) }
								},
								onMangaClick = { selectedManga = it },
							)
						}

						NavItem.BOOKMARKS -> {
							// Signets réels depuis la DB Room (BookmarksDao.observe())
							var bookmarksMap by remember {
								mutableStateOf<Map<org.koitharu.kotatsu.core.db.entity.MangaWithTags, List<org.koitharu.kotatsu.desktop.ui.screens.bookmarks.BookmarkUi>>>(emptyMap())
							}
							LaunchedEffect(Unit) {
								container.database.getBookmarksDao().observe().collect { map ->
									bookmarksMap = map.mapValues { (_, marks) ->
										marks.map { b ->
											org.koitharu.kotatsu.desktop.ui.screens.bookmarks.BookmarkUi(
												pageId = b.pageId,
												chapterId = b.chapterId,
												page = b.page,
												imageUrl = b.imageUrl,
												percent = b.percent,
											)
										}
									}
								}
							}
							org.koitharu.kotatsu.desktop.ui.screens.bookmarks.BookmarksScreen(
								bookmarksByManga = bookmarksMap,
								onBookmarkClick = { mangaWithTags, bookmark ->
									// ouvre le lecteur sur la page du signet
									scope.launch {
										val mangaEntity = mangaWithTags.manga
										val manga = mangaEntity.toManga(
											mangaWithTags.tags.map { it.toMangaTag() }.toSet(),
											chapters = null,
										)
										val details = runCatching { container.getDetails(manga) }.getOrNull()
										val chapter = details?.chapters?.firstOrNull { it.id == bookmark.chapterId }
										if (chapter != null) readerRequest = details to chapter
									}
								},
								onBookmarkDelete = { bookmark ->
									scope.launch { container.database.getBookmarksDao().delete(bookmark.pageId) }
								},
							)
						}

						NavItem.EXPLORE ->
							org.koitharu.kotatsu.desktop.ui.screens.discover.DiscoverScreen(
								fetchSection = { section, _ ->
									// Sections Discover depuis MangaDex (preset principal de l'app
									// d'origine) : popularité / récent / suite
									val source = MangaParserSource.entries.first { it.name == "MANGADEX" }
									val order = when (section) {
										org.koitharu.kotatsu.desktop.ui.screens.discover.DiscoverSection.FOR_YOU ->
											org.koitharu.kotatsu.parsers.model.SortOrder.POPULARITY
										org.koitharu.kotatsu.desktop.ui.screens.discover.DiscoverSection.NEW_FINDS ->
											org.koitharu.kotatsu.parsers.model.SortOrder.UPDATED
										else ->
											org.koitharu.kotatsu.parsers.model.SortOrder.POPULARITY
									}
									container.getList(source, 0, order).take(20)
								},
								onMangaClick = { selectedManga = it },
								onShowAllClick = { /* ouvre le catalogue MD complet : à venir */ },
								onSearchClick = { showSearch = true },
								onUpdateClick = { container.trackerScheduler.startNow() },
								onRestoreDismissedClick = {},
								onIncognitoToggle = { incognito = it },
								onSettingsClick = { showSettings = true },
							)

						NavItem.SOURCES ->
							org.koitharu.kotatsu.desktop.ui.screens.sources.SourcePresetsScreen(
								suggestions = suggestions,
								sources = sourcesList,
								domainOf = { container.domainOf(it) },
								hasNewChapters = { false },
								onSuggestionClick = { selectedManga = it },
								onSourceClick = { selectedSource = it },
								onCatalogClick = { showSourcesCatalog = true },
								onMoreSuggestionsClick = {},
								onQuickAction = { action ->
									when (action) {
										org.koitharu.kotatsu.desktop.ui.screens.sources.SourceQuickAction.LOCAL_STORAGE -> selected = NavItem.LOCAL
										org.koitharu.kotatsu.desktop.ui.screens.sources.SourceQuickAction.BOOKMARKS -> selected = NavItem.BOOKMARKS
										org.koitharu.kotatsu.desktop.ui.screens.sources.SourceQuickAction.DOWNLOADS -> showDownloads = true
										org.koitharu.kotatsu.desktop.ui.screens.sources.SourceQuickAction.RANDOM -> scope.launch {
											val source = MangaParserSource.entries.filterNot { it.isBroken }.random()
											val list = runCatching { container.getList(source, 0) }.getOrDefault(emptyList())
											list.randomOrNull()?.let { selectedManga = it }
										}
										else -> showSourcesCatalog = true
									}
								},
								onSearchClick = { showSearch = true },
								onIncognitoToggle = { incognito = it },
								onSettingsClick = { showSettings = true },
								onManageSourcesClick = { showSourcesCatalog = true },
							)

						NavItem.FEED ->
							org.koitharu.kotatsu.desktop.ui.screens.feed.FeedScreen(
								entries = emptyList(),
								onEntryClick = { selectedManga = it.manga },
								onUpdateClick = { container.trackerScheduler.startNow() },
								onClearFeedClick = {},
								onShowUpdatedToggle = {},
								onSearchClick = { showSearch = true },
								onSettingsClick = { showSettings = true },
								onIncognitoToggle = { incognito = it },
							)

						else -> PlaceholderScreen(selected)
					}
			}
		}
	}
	// Fenêtre lecteur (comme ReaderActivity d'origine : une fenêtre dédiée) ;
	// l'ouverture enregistre la progression dans l'historique (HistoryDao, comme l'app d'origine)
	val request = readerRequest
	if (request != null) {
		val (readerManga, chapter) = request
		var pageUrls by remember(request) { mutableStateOf<List<String>>(emptyList()) }
		LaunchedEffect(request) {
			// Mode incognito (comme l'app d'origine) : aucune progression enregistrée
			if (!incognito) {
				container.library.saveProgress(readerManga, chapter, page = 0)
			}
			val pages = container.getPages(chapter)
			pageUrls = pages.map { container.getPageUrl(it) }
		}
		ReaderContent(
			title = readerManga.title,
			chapterTitle = chapter.title ?: chapter.name,
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
