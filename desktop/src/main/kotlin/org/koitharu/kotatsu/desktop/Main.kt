/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose
 * Desktop ; 2026-11-08 — phase P2 : entrées « Options de liste » des menus ⋮ Historique/Favoris
 * ouvrant la ListOptionsSheet (état partagé ListOptionsState, appliqué en direct à AppSettings),
 * tri de l'historique (« Dernier lu » / « Par nom ») et réglages liste/grille appliqués aux Favoris.
 *
 * Fenêtre principale — reproduit la mise en page « tablette/desktop » de l'app Android
 * (rail de navigation latéral + volet de contenu), cf. activity_main.xml + navigation_rail_fab.xml.
 * Navigation : Explore → catalogue d'une source → détails d'un manga → lecteur (fenêtre dédiée,
 * comme ReaderActivity est une activité dédiée).
 * Modification : 2026-11-08 — phase P7 (vague 1) : bloc lecteur (ReaderHost) — mémorisation du
 * mode de lecture par manga (priorité : mode par manga > détection webtoon > mode global),
 * marque-pages (toggle via BookmarksDao.find/insert/delete), « Sauvegarder la page » (export
 * PNG/JPG vers %USERPROFILE%/Pictures, repli presse-papiers de l'URL), toggle incognito du menu,
 * toast temporaire « Images masquées » à l'ouverture (hidden_pages) et toasts de retour.
 * Modification : 2026-11-08 — phase P7 (vague 2) : persistance des zones gomme (webtoon) —
 * les rectangles normalisés 0..1 par page sont encodés dans PageScanEntity.bands (table
 * page_scans de HiddenImagesDatabase, champs x1,y1,x2,y2 séparés par « ; » ; matcherVersion 0),
 * chargés à l'ouverture du chapitre et remontés à ReaderContent (masque = rect couleur fond
 * dessiné par-dessus la page).
 * Modification : 2026-11-08 — phase P8-A (bloc écran détails uniquement) : câblage du bloc
 * détails — catégories de favoris (FavouritesDao.observeCategories + écriture via
 * LibraryRepository.addFavourite/removeFavourite → dialog FavoritesCategoriesDialog),
 * position d'historique (HistoryDao.find → « Continuer · n », « Chapitre n sur N »,
 * « Progression ») et mangas connexes (recherche par 1er genre, « Tout afficher » ouvrant
 * le catalogue filtré).
 */
package org.koitharu.kotatsu.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
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
import org.koitharu.kotatsu.desktop.ui.components.ListOptionsSheet
import org.koitharu.kotatsu.desktop.ui.components.ListOptionsState
import org.koitharu.kotatsu.desktop.ui.nav.NavItem
import org.koitharu.kotatsu.desktop.ui.screens.history.HistoryItem
import org.koitharu.kotatsu.desktop.ui.strings.FR_HISTORY
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_EXPLORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCE
import org.koitharu.kotatsu.desktop.ui.strings.FR_FEED
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANGA_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_SUGGESTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEW_CHAPTERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_STORAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACKUP_RESTORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_NOTIFICATIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_NETWORK
import org.koitharu.kotatsu.desktop.ui.strings.FR_PRIVACY_CONTENT
import org.koitharu.kotatsu.desktop.ui.strings.FR_ABOUT
import org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderContent
import org.koitharu.kotatsu.desktop.tracker.TrackerTray
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.theme.KotatsuTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koitharu.kotatsu.bookmarks.data.BookmarkEntity
import org.koitharu.kotatsu.core.db.entity.toManga
import org.koitharu.kotatsu.core.db.entity.toMangaTag
import org.koitharu.kotatsu.core.prefs.ReaderMode
import org.koitharu.kotatsu.reader.data.erase.PageScanEntity
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaPage
import java.io.File
import org.koitharu.kotatsu.parsers.model.MangaParserSource

fun main(args: Array<String>) {
	// Mode maintenance : désinstallateur GUI (-Dkotatsu.mode=uninstaller, lanceur « Uninstall
	// Kotatsu Desktop » créé au premier lancement, cf. setupMaintenanceLauncher).
	val mode = System.getProperty("kotatsu.mode")
	if (mode == "uninstaller" || "--mode uninstaller" in args) {
		org.koitharu.kotatsu.desktop.ui.maintenance.mainUninstaller()
		return
	}
	if (mode == "installer" || "--mode installer" in args) {
		org.koitharu.kotatsu.desktop.ui.maintenance.mainInstaller()
		return
	}
	// App principale ; au premier lancement d'une installation jpackage, crée le lanceur
	// « Uninstall » et oriente la clé de désinstallation HKCU vers son GUI.
	application {
	val container = remember { AppContainer.getInstance() }
	val settings = remember { DesktopSettings.getInstance() }

	// Notifications nouveaux chapitres (icône tray + bulles Windows), cf. TrackWorker d'origine
	TrackerTray(notifier = container.trackerNotifier)
	LaunchedEffect(Unit) { container.trackerScheduler.start() }

	// 1.1.1 : lanceur « Uninstall » + clé de désinstallation HKCU orientée vers le GUI
	// (installation par utilisateur, sans UAC — fil d'arrière-plan, premier lancement)
	LaunchedEffect(Unit) {
		Thread { setupMaintenanceLauncher() }.start()
	}

	// « Confirmation de sortie » (P2) : dialog avant fermeture si activé
	var pendingExit by remember { mutableStateOf(false) }
	Window(
		onCloseRequest = {
			if (container.appSettings.isExitConfirmationEnabled) {
				pendingExit = true
			} else {
				exitApplication()
			}
		},
		title = "Kotatsu Desktop $APP_VERSION_NAME",
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
		if (pendingExit) {
			ExitConfirmationDialog(
				onDismiss = { pendingExit = false },
				onConfirm = { exitApplication() },
			)
		}
		}
	}
	}
}

/**
 * 1.1.1 : pour une installation jpackage PAR UTILISATEUR, crée le lanceur dédié
 * « Uninstall Kotatsu Desktop.exe » (copie de l'exécutable principal avec
 * -Dkotatsu.mode=uninstaller dans son .cfg) et oriente la clé de désinstallation
 * HKCU vers ce lanceur — ainsi Paramètres Windows > Applications > Kotatsu Desktop >
 * Désinstaller ouvre l'interface graphique de désinstallation (choix de conserver
 * ou non les données utilisateur). Sans effet en développement.
 */
private fun setupMaintenanceLauncher() {
	val appDir = System.getProperty("jpackage.app-dir") ?: return
	try {
		val mainExe = File(appDir, "Kotatsu Desktop.exe")
		val mainCfg = File(appDir, "Kotatsu Desktop.cfg")
		if (!mainExe.isFile || !mainCfg.isFile) return
		val uninstallExe = File(appDir, "Uninstall Kotatsu Desktop.exe")
		val uninstallCfg = File(appDir, "Uninstall Kotatsu Desktop.cfg")
		if (!uninstallExe.isFile) {
			mainExe.copyTo(uninstallExe, overwrite = false)
		}
		if (!uninstallCfg.isFile) {
			val cfg = mainCfg.readText(Charsets.UTF_8)
			val patched = if (cfg.contains("kotatsu.mode")) {
				cfg
			} else if (cfg.contains("[JavaOptions]")) {
				cfg.replace("[JavaOptions]", "[JavaOptions]\njava-options=-Dkotatsu.mode=uninstaller")
			} else {
				cfg + "\n[JavaOptions]\njava-options=-Dkotatsu.mode=uninstaller\n"
			}
			uninstallCfg.writeText(patched, Charsets.UTF_8)
		}
		// Orienter UninstallString (HKCU — installation par utilisateur, pas d'élévation)
		val query = ProcessBuilder(
			"reg", "query", "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
			"/s", "/f", "Kotatsu Desktop", "/d",
		).start()
		val output = query.inputStream.readBytes().toString(Charsets.UTF_8)
		query.waitFor()
		val key = output.lineSequence()
			.map { it.trim() }
			.firstOrNull { it.startsWith("HKEY_") } ?: return
		if (output.contains(uninstallExe.path)) return // déjà orienté
		ProcessBuilder(
			"reg", "add", key, "/v", "UninstallString", "/t", "REG_SZ",
			"/d", "\"" + uninstallExe.path + "\"", "/f",
		).start().waitFor()
	} catch (t: Throwable) {
		// jamais bloquant : l'entrée Windows standard reste fonctionnelle
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
	var settingsRoute by remember {
		mutableStateOf(org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT)
	}
	var showSearch by remember { mutableStateOf(false) }
	var showDownloads by remember { mutableStateOf(false) }
	var showSourcesCatalog by remember { mutableStateOf(false) }
	var showManageSources by remember { mutableStateOf(false) } // P3 : Gérer les sources
	var showManageLinks by remember { mutableStateOf(false) } // P3 : Gérer les liens
	var showFeedLog by remember { mutableStateOf(false) } // P4 : journal des nouveaux chapitres
	// Options de liste (P2) : bottom sheet ouverte depuis le menu ⋮ Historique/Favoris ;
	// état partagé pour que les écrans se recomposent dès qu'une valeur change.
	var showListOptions by remember { mutableStateOf(false) }
	val listOptions = remember { ListOptionsState(container.appSettings) }
	var incognito by remember {
		mutableStateOf(container.appSettings.isIncognitoModeEnabled)
	}
	var suggestions by remember { mutableStateOf<List<Manga>>(emptyList()) }
	var favouriteUrls by remember { mutableStateOf(emptySet<String>()) }
	LaunchedEffect(incognito) {
		if (container.appSettings.isListBadgesEnabled) {
			favouriteUrls = runCatching {
				container.library.getFavourites().map { it.manga.url }.toSet()
			}.getOrDefault(emptySet())
		}
	}
	// Bouton flottant « Continuer » (P2) : premier item d'historique non incognito
	var continueItem by remember { mutableStateOf<org.koitharu.kotatsu.desktop.ui.screens.history.HistoryItem?>(null) }
	LaunchedEffect(incognito) {
		if (!incognito && container.appSettings.isFloatingContinueEnabled) {
			runCatching {
				container.library.getHistory(limit = 1).firstOrNull()?.let { row ->
					container.library.run { row.toDisplay() }.let { (manga, history) ->
						continueItem = org.koitharu.kotatsu.desktop.ui.screens.history.HistoryItem(
							manga = manga,
							chapter = null,
							percent = history.percent,
							updatedAt = java.time.Instant.ofEpochMilli(history.updatedAt),
						)
					}
				}
			}
		} else {
			continueItem = null
		}
	}
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
	var readerRequest by remember { mutableStateOf<ReaderRequest?>(null) }
	/** Ouvre le lecteur sur un chapitre donné (index résolu dans la liste fournie). */
	fun openReader(manga: Manga, chapters: List<MangaChapter>, chapter: MangaChapter?) {
		if (chapter == null) return
		val index = chapters.indexOfFirst { it.id == chapter.id }
		if (index >= 0) readerRequest = ReaderRequest(manga, index, chapters)
	}
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
		// onglets = « Principales sections de l'écran » (ordre réordonnable, P2) ;
		// relecture à chaque retour des Paramètres (settingsRoute change)
		val railItems = remember(settingsRoute) {
			container.appSettings.mainSections.mapNotNull { name ->
				runCatching { NavItem.valueOf(name) }.getOrNull()
			}.ifEmpty { listOf(NavItem.HISTORY, NavItem.FAVORITES, NavItem.EXPLORE, NavItem.SOURCES, NavItem.FEED) }
		}
		NavigationRail(modifier = Modifier.align(Alignment.CenterStart)) {
			for (item in railItems) {
				NavigationRailItem(
				selected = selected == item && selectedManga == null && selectedSource == null &&
						!showSettings && !showDownloads && !showSourcesCatalog && !showSearch &&
						!showManageSources && !showManageLinks && !showFeedLog,
				onClick = {
					selected = item
					selectedSource = null
					selectedManga = null
					showSettings = false
					showDownloads = false
					showSourcesCatalog = false
					showManageSources = false
					showManageLinks = false
					showFeedLog = false
					showSearch = false
				},
					icon = {
						Icon(
							imageVector = if (selected == item) item.filledIcon else item.outlinedIcon,
							contentDescription = item.title,
						)
					},
					label = {
						Text(org.koitharu.kotatsu.desktop.ui.strings.frNavItemLabel(item))
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
				onClick = {
					showSettings = true
					settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT
				},
				icon = { Icon(Icons.Filled.Settings, contentDescription = FR_SETTINGS) },
				label = { Text(FR_SETTINGS) },
			)
		}
		// Volet de contenu
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(start = 80.dp),
			color = MaterialTheme.colorScheme.surface,
		) {
			Box {
			when {
				showFeedLog ->
					// P4 : journal des nouveaux chapitres (backend de log pas encore alimenté —
					// état vide explicatif en attendant)
					org.koitharu.kotatsu.desktop.ui.screens.settings.FeedLogScreen(
						entries = emptyList(),
						onBackClick = { showFeedLog = false },
					)

				showManageSources ->
					// P3 : « Gérer les sources » — toggle → hiddenSources (AppSettings)
					org.koitharu.kotatsu.desktop.ui.screens.sources.ManageSourcesScreen(
						appSettings = container.appSettings,
						onBackClick = { showManageSources = false },
						domainOf = container::domainOf,
					)

				showManageLinks ->
					// P3 : « Gérer les liens » (adaptation desktop, lecture seule)
					org.koitharu.kotatsu.desktop.ui.screens.sources.ManageLinksScreen(
						onBackClick = { showManageLinks = false },
					)

				showSourcesCatalog ->
					org.koitharu.kotatsu.desktop.ui.screens.sources.SourcesCatalogScreen(
						domainOf = { container.domainOf(it) },
						onSourceClick = {
							showSourcesCatalog = false
							selectedSource = it
						},
						onSourceToggle = { /* P3 : ignoré quand appSettings est fourni (toggle direct) */ },
						isAdded = { true },
						appSettings = container.appSettings,
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

				showSettings -> {
					// Paramètres : écran racine (P0) puis sous-destinations ; Apparence est la
					// seule destination pleinement implémentée pour l'instant.
					val route = settingsRoute
					when (route) {
						org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT ->
							org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRootScreen(
								enabledSources = sourcesList.size,
								totalSources = MangaParserSource.entries.size,
								onNavigate = { settingsRoute = it },
								onBackClick = { showSettings = false },
								onSearchClick = { showSearch = true },
							)
						org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.APPEARANCE ->
							org.koitharu.kotatsu.desktop.ui.screens.settings.AppearanceSettingsScreen(
								settings = DesktopSettings.getInstance(),
								appSettings = container.appSettings,
								onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
								onNavigate = { settingsRoute = it },
							)
						org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.MAIN_SECTIONS ->
							org.koitharu.kotatsu.desktop.ui.screens.settings.SectionsSettingsScreen(
								appSettings = container.appSettings,
								onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
							)
						org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.READING ->
							org.koitharu.kotatsu.desktop.ui.screens.settings.ReaderSettingsScreen(
								settings = container.appSettings,
								onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
								onNavigate = { settingsRoute = it },
							)
						org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.MASKED_IMAGES ->
							org.koitharu.kotatsu.desktop.ui.screens.settings.MaskedImagesScreen(
								onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.READING },
							)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.SUGGESTIONS ->
						// P4 : écran « Suggestions » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.SuggestionsSettingsScreen(
							appSettings = container.appSettings,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.FEED_SETTINGS -> {
						// P4 : écran « Nouveaux chapitres » (remplace le stub) ; catégories
						// favorites réelles depuis la DB, « Rechercher les mises à jour » → scheduler
						var feedCats by remember { mutableStateOf<List<String>>(emptyList()) }
						LaunchedEffect(Unit) {
							runCatching {
								container.database.getFavouriteCategoriesDao().findAll().map { it.title }
							}.getOrDefault(emptyList()).let { feedCats = it }
						}
						org.koitharu.kotatsu.desktop.ui.screens.settings.FeedSettingsScreen(
							appSettings = container.appSettings,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
							favouriteCategories = feedCats,
							onUpdateNow = { scope.launch { runCatching { container.trackerScheduler.runOnce() } } },
							onOpenLog = { showFeedLog = true },
						)
					}
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.SOURCES ->
						// P3 : écran racine « Sources des mangas » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.sources.SourcesSettingsScreen(
							appSettings = container.appSettings,
							enabledCount = sourcesList.size,
							totalCount = MangaParserSource.entries.size,
							availableCount = MangaParserSource.entries.filterNot { it.isBroken }.size,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
						onOpenManageSources = { showManageSources = true },
						onOpenCatalog = { showSourcesCatalog = true },
						onOpenLinks = { showManageLinks = true },
					)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA ->
						// P5 : « Téléchargements et stockage » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.DownloadsStorageSettingsScreen(
							appDir = container.appDir,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
							onOpenUsage = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA_USAGE },
							onOpenDataRemoval = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA_REMOVAL },
							onOpenBackup = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.BACKUP },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA_USAGE ->
						org.koitharu.kotatsu.desktop.ui.screens.settings.StorageUsageScreen(
							appDir = container.appDir,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA_REMOVAL ->
						org.koitharu.kotatsu.desktop.ui.screens.settings.DataRemovalScreen(
							appSettings = container.appSettings,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.BACKUP ->
						// P5 : « Sauvegarde et restauration » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.BackupRestoreScreen(
							appDir = container.appDir,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.NOTIFICATIONS ->
						// P5 : « Notifications » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.NotificationsSettingsScreen(
							appSettings = container.appSettings,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
							onOpenFeedSettings = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.FEED_SETTINGS },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.NETWORK ->
						// P5 : « Réseau » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.NetworkSettingsScreen(
							appSettings = container.appSettings,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.PRIVACY ->
						// P6 : « Confidentialité et contenu » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.PrivacySettingsScreen(
							appSettings = container.appSettings,
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ACCOUNTS ->
						// P6 : « Comptes et synchronisation » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.AccountsScreen(
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
						)
					org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ABOUT ->
						// P5 : « À propos » (remplace le stub)
						org.koitharu.kotatsu.desktop.ui.screens.settings.AboutScreen(
							onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
						)
						else -> {
							val title = when (route) {
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.READING -> FR_READER_SETTINGS
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.SOURCES -> FR_MANGA_SOURCES
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.SUGGESTIONS -> FR_SUGGESTIONS
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.FEED_SETTINGS -> FR_NEW_CHAPTERS
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.DATA -> FR_DOWNLOADS_STORAGE
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.BACKUP -> FR_BACKUP_RESTORE
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.NOTIFICATIONS -> FR_NOTIFICATIONS
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.NETWORK -> FR_NETWORK
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.PRIVACY -> FR_PRIVACY_CONTENT
								org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ABOUT -> FR_ABOUT
								else -> FR_SETTINGS
							}
							org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsStubScreen(
								title = title,
								onBackClick = { settingsRoute = org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsRoute.ROOT },
							)
						}
					}
				}

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

					// --- Phase P8-A : favoris, historique (position de lecture) et connexes ---
					// DAO favoris : lecture des catégories du manga (observeCategories) ; écriture
					// via LibraryRepository.addFavourite/removeFavourite (qui passent par les
					// méthodes DAO FavouritesDao.upsert/delete et garantissent le stockage du
					// manga avant insertion — contrainte FK).
					val favouritesDao = remember { container.database.getFavouritesDao() }
					val historyDao = remember { container.database.getHistoryDao() }
					val allCategories by container.library.observeFavouriteCategories()
						.collectAsState(initial = emptyList())
					val mangaCategories by remember(seed.id) {
						favouritesDao.observeCategories(seed.id)
					}.collectAsState(initial = emptyList())
					val isFavourite = mangaCategories.isNotEmpty()
					var showFavouritesDialog by remember(seed.url) { mutableStateOf(false) }

					// Position d'historique du manga (chapterId → index dans la liste chargée).
					// Réévaluée à la fermeture du lecteur (readerRequest repasse à null) pour
					// afficher « Continuer · n » / « Chapitre n sur N » / « Progression » (vidéo 9:25).
					var historyEntry by remember(seed.url) {
						mutableStateOf<org.koitharu.kotatsu.history.data.HistoryEntity?>(null)
					}
					LaunchedEffect(seed.url, readerRequest) {
						historyEntry = runCatching { historyDao.find(seed.id) }.getOrNull()
					}
					val continueChapterIndex = historyEntry?.let { h ->
						chapters.indexOfFirst { it.id == h.chapterId }.takeIf { it >= 0 }
					}

					// Mangas connexes (P8-A) : recherche dans la source par le premier genre du
					// manga (container.getList(query = genre)). runCatching : en cas d'échec
					// réseau ou de liste vide, la section « Mangas connexes » est masquée.
					val base = details ?: seed
					var relatedManga by remember(seed.url) { mutableStateOf<List<Manga>>(emptyList()) }
					var relatedTag by remember(seed.url) { mutableStateOf<String?>(null) }
					LaunchedEffect(base.id, base.tags) {
						val tag = base.tags.firstOrNull()?.title ?: return@LaunchedEffect
						relatedTag = tag
						relatedManga = runCatching {
							container.getList(base.source, 0, query = tag)
								.filterNot { it.url == base.url }
								.take(12)
						}.getOrDefault(emptyList())
					}
					// « Tout afficher » (P8-A) : ouvre le catalogue de la source filtré par le
					// genre (CatalogScreen réutilisé avec un fetchList à requête) — rendu plus bas.
					var showRelatedCatalog by remember(seed.url) { mutableStateOf(false) }

					val relatedSource = base.source as? MangaParserSource
					if (showRelatedCatalog && relatedTag != null && relatedSource != null) {
						org.koitharu.kotatsu.desktop.ui.screens.catalog.CatalogScreen(
							source = relatedSource,
							fetchList = { s, offset ->
								container.getList(s, offset, query = relatedTag)
							},
							onMangaClick = { selectedManga = it },
							onBackClick = { showRelatedCatalog = false },
						)
					} else {
						org.koitharu.kotatsu.desktop.ui.screens.details.DetailsScreen(
							manga = details ?: seed,
							chapters = chapters.map { it.name },
							isFavourite = isFavourite,
							collapseLongDescription = container.appSettings.isLongDescriptionCollapsed,
							continueChapterIndex = continueChapterIndex,
							historyPercent = historyEntry?.percent,
							relatedManga = relatedManga,
							onSeeRelated = if (relatedManga.isNotEmpty()) {
								{ showRelatedCatalog = true }
							} else {
								null
							},
							onRelatedClick = { selectedManga = it },
							onBackClick = { selectedManga = null },
							onChapterClick = { index ->
								openReader(details ?: seed, chapters, chapters.getOrNull(index))
							},
							onFavouriteClick = { showFavouritesDialog = true },
							onDownloadClick = {
								container.downloadManager.enqueue(manga = details ?: seed)
							},
							onReadClick = {
								// CTA principal : « Continuer · n » si position d'historique,
								// sinon chapitre 1 (vidéo t≈9:25)
								val target = continueChapterIndex?.let { chapters.getOrNull(it) }
									?: chapters.firstOrNull()
								openReader(details ?: seed, chapters, target)
							},
							onReadFirstChapter = {
								openReader(details ?: seed, chapters, chapters.firstOrNull())
							},
						)
						if (showFavouritesDialog) {
							// Dialog « Ajouter aux favoris » (P8-A) : cases des catégories,
							// cloche si la catégorie est notifiée, « Gérer » désactivé (aucun
							// écran de gestion des catégories — périmètre P8-B).
							org.koitharu.kotatsu.desktop.ui.screens.details.FavoritesCategoriesDialog(
								manga = details ?: seed,
								categories = allCategories,
								checkedIds = mangaCategories.map { it.categoryId.toLong() }.toSet(),
								onCheckedChange = { categoryId, checked ->
									scope.launch {
										runCatching {
											if (checked) {
												container.library.addFavourite(details ?: seed, categoryId)
											} else {
												container.library.removeFavourite(details ?: seed, categoryId)
											}
										}
									}
								},
								onConfirm = { showFavouritesDialog = false },
								onDismiss = { showFavouritesDialog = false },
							)
						}
					}
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
						// P8-B : genres officiels du source pour les chips de filtres
						fetchFilterOptions = { s ->
							runCatching {
								container.repositoryFactory.create(s) as? org.koitharu.kotatsu.core.parser.ParserMangaRepository
							}.getOrNull()?.getFilterOptions()
						},
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
							// « Ordre de tri » de la bottom sheet Options de liste (P2) :
							// « Dernier lu » = ordre du DAO (date de dernière lecture décroissante),
							// « Par nom » = tri alphabétique insensible à la casse.
							val sortedHistory = remember(historyItems, listOptions.sortOrder) {
								if (listOptions.sortOrder == org.koitharu.kotatsu.parsers.model.SortOrder.ALPHABETICAL) {
									historyItems.sortedBy { it.manga.title.lowercase() }
								} else {
									historyItems
								}
							}
							org.koitharu.kotatsu.desktop.ui.screens.history.HistoryScreen(
								historyItems = sortedHistory,
								listMode = listOptions.listMode,
								gridSize = listOptions.gridSize,
								progressMode = container.appSettings.progressIndicators,
								isBadgesEnabled = container.appSettings.isListBadgesEnabled,
								// « Groupe » : les en-têtes de dates n'ont de sens qu'en tri « Dernier lu »
								// (tri alphabétique ⇒ liste plate, comme le comportement d'origine).
								isGroupingEnabled = listOptions.isGroupingEnabled &&
									listOptions.sortOrder != org.koitharu.kotatsu.parsers.model.SortOrder.ALPHABETICAL,
								favouriteUrls = favouriteUrls,
								onListOptionsClick = { showListOptions = true },
								onMangaClick = { selectedManga = it },
								onContinueClick = { item ->
									// « Continue reading » : retrouve le chapitre courant via la source
									scope.launch {
										val details = runCatching { container.getDetails(item.manga) }.getOrNull()
										val chapter = details?.chapters?.firstOrNull()
										if (details != null) openReader(item.manga, details.chapters.orEmpty(), chapter)
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
								listMode = listOptions.listMode,
								gridSize = listOptions.gridSize,
								onMangaClick = { selectedManga = it },
								onListOptionsClick = { showListOptions = true },
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
										if (details != null) openReader(manga, details.chapters.orEmpty(), chapter)
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
								presetsDao = container.database.getSourcePresetsDao(),
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
								onManageSourcesClick = { showManageSources = true },
							)

						NavItem.LOCAL -> {
							// CBZ/dossiers locaux (LocalMangaRepository, lot P2)
							var localItems by remember { mutableStateOf<List<Manga>>(emptyList()) }
							LaunchedEffect(Unit) {
								runCatching {
									container.downloadManager // initialise le dépôt local
									val local = container.localRepository() ?: return@runCatching
									// chaque émission du flux = un manga local découvert
									local.getRawListAsFlow().toList().map { it.manga }
								}
							}
							org.koitharu.kotatsu.desktop.ui.screens.local.LocalScreen(
								items = localItems,
								onMangaClick = { selectedManga = it },
								onImportClick = {},
								onSearchClick = { showSearch = true },
								onSettingsClick = { showSettings = true },
								onIncognitoToggle = { incognito = it },
							)
						}

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
			// Bouton flottant « Continuer » : onglet principal, hors overlays/incognito
			val mainTabShown = selectedManga == null && selectedSource == null &&
				!showSettings && !showDownloads && !showSourcesCatalog && !showSearch
			if (mainTabShown && !incognito && continueItem != null &&
				container.appSettings.isFloatingContinueEnabled
			) {
				androidx.compose.material3.ExtendedFloatingActionButton(
					onClick = {
						val item = continueItem ?: return@ExtendedFloatingActionButton
						scope.launch {
							val details = runCatching { container.getDetails(item.manga) }.getOrNull()
							val chapter = details?.chapters?.firstOrNull()
							if (details != null) openReader(item.manga, details.chapters.orEmpty(), chapter)
						}
					},
					icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
					text = { Text("Continuer la lecture") },
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(16.dp),
				)
			}
			}
		}
	}
	// Bottom sheet « Options de liste » (P2) : ouverte depuis le menu ⋮ Historique/Favoris
	if (showListOptions) {
		ListOptionsSheet(
			state = listOptions,
			onDismiss = { showListOptions = false },
		)
	}
	// Fenêtre lecteur (comme ReaderActivity d'origine : une fenêtre dédiée)
	val request = readerRequest
	if (request != null) {
		ReaderWindow(
			container = container,
			request = request,
			isIncognito = incognito,
			onToggleIncognito = { incognito = it },
			onClose = { readerRequest = null },
			onNavigate = { readerRequest = it },
		)
	}
}

/** Demande d'ouverture du lecteur : manga, index du chapitre courant, liste des chapitres. */
private data class ReaderRequest(
	val manga: Manga,
	val chapterIndex: Int,
	val chapters: List<MangaChapter>,
)

// TODO(vague 2) : chaînes temporaires — migrer vers FrReader.kt (créé par l'agent P7-B)
private const val FR_READER_BOOKMARK_ADDED = "Marque-page ajouté"
private const val FR_READER_BOOKMARK_REMOVED = "Marque-page retiré"
private const val FR_READER_PAGE_SAVED = "Page enregistrée"
private const val FR_READER_PAGE_URL_COPIED = "Enregistrement impossible : URL de la page copiée"

// --- Gomme webtoon (vague 2) : encodage des rectangles normalisés dans PageScanEntity.bands ---
// Format « x1,y1,x2,y2;x1,y1,x2,y2 » (coordonnées 0..1, page-scans de HiddenImagesDatabase,
// matcherVersion 0 : jamais produit par le matcher d'origine). Choix d'entité documenté :
// PageScanEntity est la table « régions par page » existante (HiddenPageEntity ne sait que
// marquer une page entière, HiddenImageEntity n'a pas de paire yStart/yEnd).

/** Encode des rectangles normalisés pour PageScanEntity.bands. */
private fun encodeErasedRegions(regions: List<Rect>): String =
	regions.joinToString(";") { "${it.left},${it.top},${it.right},${it.bottom}" }

/** Décode PageScanEntity.bands en rectangles normalisés (entrées invalides ignorées). */
private fun decodeErasedRegions(bands: String): List<Rect> = bands.split(";").mapNotNull { part ->
	val v = part.split(',')
	if (v.size != 4) return@mapNotNull null
	runCatching {
		Rect(
			left = v[0].trim().toFloat(),
			top = v[1].trim().toFloat(),
			right = v[2].trim().toFloat(),
			bottom = v[3].trim().toFloat(),
		)
	}.getOrNull()
}

@Composable
private fun ReaderWindow(
	container: AppContainer,
	request: ReaderRequest,
	isIncognito: Boolean,
	onToggleIncognito: (Boolean) -> Unit,
	onClose: () -> Unit,
	onNavigate: (ReaderRequest) -> Unit,
) {
	val windowState = androidx.compose.ui.window.rememberWindowState(
		width = 1100.dp,
		height = 850.dp,
	)
	val scope = rememberCoroutineScope()
	val settings = container.appSettings
	val chapter = request.chapters[request.chapterIndex]
	Window(
		onCloseRequest = onClose,
		title = "Kotatsu",
		icon = kotatsuIcon(),
		state = windowState,
	) {
		var mangaPages by remember(request) { mutableStateOf<List<MangaPage>>(emptyList()) }
		var pageUrls by remember(request) { mutableStateOf<List<String>>(emptyList()) }
		var pagesError by remember(request) { mutableStateOf<String?>(null) }
		// Toast temporaire (2 s) : « Images masquées » à l'ouverture + retours du menu (P7)
		var readerNotice by remember(request) { mutableStateOf<String?>(null) }
		LaunchedEffect(readerNotice) {
			if (readerNotice != null) {
				delay(2000)
				readerNotice = null
			}
		}
		// Toast « Images masquées » (vidéo t≈7:25) : la base du mode gomme (HiddenImagesDatabase)
		// n'est pas câblée dans AppContainer ; ouverture locale unique ici. Choix minimal (P7) :
		// une seule requête getHiddenPages() (pages entièrement masquées) au lieu d'un
		// getScan(pageId) par page. TODO(vague 2) : brancher via le composant gomme de P7-B.
		val hiddenImagesDb = remember {
			runCatching {
				org.koitharu.kotatsu.reader.data.erase.createHiddenImagesDatabase(
					java.io.File(container.appDir, "hidden-images.db"),
				)
			}.getOrNull()
		}
		DisposableEffect(hiddenImagesDb) {
			onDispose { runCatching { hiddenImagesDb?.close() } }
		}
		LaunchedEffect(hiddenImagesDb, pageUrls) {
			val db = hiddenImagesDb ?: return@LaunchedEffect
			if (pageUrls.isEmpty()) return@LaunchedEffect
			val hiddenPageIds = runCatching {
				withContext(Dispatchers.IO) { db.getDao().getHiddenPages() }
			}.getOrDefault(emptyList())
			if (mangaPages.any { it.id in hiddenPageIds }) {
				readerNotice = org.koitharu.kotatsu.desktop.ui.strings.FR_IMAGES_MASKED
			}
		}
		LaunchedEffect(request) {
			// Mode incognito (comme l'app d'origine) : aucune progression enregistrée
			if (!isIncognito) {
				runCatching { container.library.saveProgress(request.manga, chapter, page = 0) }
			}
			runCatching {
				val pages = container.getPages(chapter)
				mangaPages = pages
				pageUrls = pages.map { container.getPageUrl(it) }
			}.onFailure {
				pagesError = it.message ?: it.javaClass.simpleName
			}
		}
		// Page courante (index 0-based, tenue à jour par le lecteur) pour marque-page / sauvegarde
		var currentPage by remember(request) { mutableIntStateOf(0) }
		// Marque-page de la page courante (menu « Ajouter un marque-page » : toggle)
		var isCurrentPageBookmarked by remember(request) { mutableStateOf(false) }
		LaunchedEffect(currentPage, mangaPages) {
			if (currentPage in mangaPages.indices) {
				isCurrentPageBookmarked = runCatching {
					container.database.getBookmarksDao().find(mangaPages[currentPage].id) != null
				}.getOrDefault(false)
			}
		}
		// Zones gomme (vague 2) : index de page → rectangles normalisés 0..1, chargés depuis
		// PageScanEntity.bands (HiddenImagesDatabase locale, cf. vague 1)
		var erasedRegions by remember(request) { mutableStateOf<Map<Int, List<Rect>>>(emptyMap()) }
		LaunchedEffect(hiddenImagesDb, mangaPages) {
			val db = hiddenImagesDb ?: return@LaunchedEffect
			if (mangaPages.isEmpty()) return@LaunchedEffect
			val map = mutableMapOf<Int, List<Rect>>()
			mangaPages.forEachIndexed { index, page ->
				val scan = runCatching {
					withContext(Dispatchers.IO) { db.getDao().getScan(page.id) }
				}.getOrNull()
				val regions = scan?.let { decodeErasedRegions(it.bands) }.orEmpty()
				if (regions.isNotEmpty()) map[index] = regions
			}
			erasedRegions = map
		}
		// Mode effectif (phase P7) : mémorisé par manga > détection webtoon par tags > global
		var modeOverride by remember(request) { mutableStateOf(settings.getReaderModeFor(request.manga.id)) }
		val detectedWebtoon = remember(request) {
			settings.isReaderModeDetect && request.manga.tags.any {
				it.title.equals("Webtoon", ignoreCase = true) ||
					it.title.equals("Long Strip", ignoreCase = true)
			}
		}
		val effectiveMode = modeOverride
			?: (if (detectedWebtoon) ReaderMode.WEBTOON else settings.readerMode)
		ReaderContent(
			title = request.manga.title,
			chapterTitle = chapter.name,
			pageUrls = pageUrls,
			mode = effectiveMode,
			animation = settings.readerAnimation,
			background = settings.readerBackground,
			scaleMode = settings.readerScaleMode,
			isPagesNumbered = settings.isPagesNumberingEnabled,
			isWebtoonSeparators = settings.isWebtoonSeparatorsEnabled,
			isIncognito = isIncognito,
			chapterIndex = request.chapterIndex,
			chaptersCount = request.chapters.size,
			isBookmarked = isCurrentPageBookmarked,
			onModeChange = { mode ->
				// applique immédiatement + mémorise par manga (clé reader_mode_<id>)
				modeOverride = mode
				settings.setReaderModeFor(request.manga.id, mode)
			},
			onSavePage = {
				// « Sauvegarder la page » (P7) : octets de l'image → %USERPROFILE%/Pictures ;
				// si le téléchargement échoue (referer/cookie exigé par la source), repli
				// presse-papiers de l'URL de la page — limite documentée au rapport.
				scope.launch {
					val page = mangaPages.getOrNull(currentPage) ?: return@launch
					val url = runCatching { container.getPageUrl(page) }.getOrNull() ?: page.url
					val saved = runCatching {
						val request2 = okhttp3.Request.Builder()
							.url(url)
							.header("Referer", "https://" + java.net.URI(url).host + "/")
							.build()
						container.network.mangaHttpClient.newCall(request2).execute().use { response ->
							check(response.isSuccessful) { "HTTP ${response.code}" }
							val bytes = response.body.bytes()
							val ext = when {
								bytes.size > 4 && bytes[0] == 0x89.toByte() && bytes[1] == 'P'.code.toByte() -> "png"
								bytes.size > 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "jpg"
								bytes.size > 3 && bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() -> "gif"
								bytes.size > 12 && bytes[0] == 'R'.code.toByte() && bytes[8] == 'W'.code.toByte() -> "webp"
								else -> "jpg"
							}
							val pictures = java.io.File(System.getProperty("user.home"), "Pictures").apply { mkdirs() }
							val safeTitle = request.manga.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
							val base = "$safeTitle-${currentPage + 1}"
							var file = java.io.File(pictures, "$base.$ext")
							var counter = 1
							while (file.exists()) file = java.io.File(pictures, "$base (${counter++}).$ext")
							file.writeBytes(bytes)
						}
						true
					}.getOrElse { false }
					if (saved) {
						readerNotice = FR_READER_PAGE_SAVED
					} else {
						// limite : les octets ne sont pas récupérables directement depuis le
						// cache Coil depuis l'écran, on copie l'URL de la page
						runCatching {
							java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(
								java.awt.datatransfer.StringSelection(url),
								null,
							)
						}
						readerNotice = FR_READER_PAGE_URL_COPIED
					}
				}
			},
			onToggleBookmark = {
				// « Ajouter un marque-page » (P7) : insert ou delete selon l'état existant
				scope.launch {
					val page = mangaPages.getOrNull(currentPage) ?: return@launch
					val dao = runCatching { container.database.getBookmarksDao() }.getOrNull() ?: return@launch
					val existing = runCatching { dao.find(page.id) }.getOrNull()
					if (existing != null) {
						runCatching { dao.delete(existing) }
						isCurrentPageBookmarked = false
						readerNotice = FR_READER_BOOKMARK_REMOVED
					} else {
						val inserted = runCatching {
							dao.insert(
								BookmarkEntity(
									mangaId = request.manga.id,
									pageId = page.id,
									chapterId = chapter.id,
									page = currentPage,
									scroll = 0,
									imageUrl = page.preview ?: "",
									createdAt = System.currentTimeMillis(),
									percent = if (pageUrls.isEmpty()) {
										0f
									} else {
										(currentPage + 1f) / pageUrls.size * 100f
									},
								),
							)
						}.isSuccess
						isCurrentPageBookmarked = inserted
						readerNotice = if (inserted) FR_READER_BOOKMARK_ADDED else FR_READER_BOOKMARK_REMOVED
					}
				}
			},
			onToggleIncognito = onToggleIncognito,
			onClose = onClose,
			onPrevChapter = if (request.chapterIndex > 0) {
				{ onNavigate(request.copy(chapterIndex = request.chapterIndex - 1)) }
			} else null,
			onNextChapter = if (request.chapterIndex < request.chapters.lastIndex) {
				{ onNavigate(request.copy(chapterIndex = request.chapterIndex + 1)) }
			} else null,
			onPageChange = { page ->
				currentPage = page
				if (!isIncognito) {
					scope.launch {
						runCatching { container.library.saveProgress(request.manga, chapter, page = page) }
					}
				}
			},
			erasedRegions = erasedRegions,
			onErasePageRegion = { pageIndex, region ->
				// Gomme webtoon (vague 2) : persiste la région (PageScanEntity.bands, REPLACE
				// sur page_id) et met à jour le masque rendu par-dessus la page
				scope.launch {
					val page = mangaPages.getOrNull(pageIndex) ?: return@launch
					val db = hiddenImagesDb ?: return@launch
					val saved = runCatching {
						withContext(Dispatchers.IO) {
							val existing = db.getDao().getScan(page.id)
							val regions = existing?.let { decodeErasedRegions(it.bands) }.orEmpty() + region
							db.getDao().upsertScan(
								PageScanEntity(
									pageId = page.id,
									newestRuleId = 0L,
									ruleIds = "",
									isHidden = false,
									sourceWidth = 0,
									sourceHeight = 0,
									bands = encodeErasedRegions(regions),
									patches = "",
									scannedAt = System.currentTimeMillis(),
									matcherVersion = 0,
								),
							)
						}
					}.isSuccess
					if (saved) {
						erasedRegions = erasedRegions +
							(pageIndex to (erasedRegions[pageIndex].orEmpty() + region))
					}
				}
			},
		)
		// Toast temporaire en haut de la fenêtre lecteur (équivalent Toast Android, 2 s)
		readerNotice?.let { notice ->
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
				Text(
					text = notice,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.padding(top = 16.dp)
						.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
						.padding(horizontal = 16.dp, vertical = 8.dp),
				)
			}
		}
		if (pagesError != null) {
			LaunchedEffect(pagesError) {
				println("Reader pages error: $pagesError")
			}
		}
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


/** Dialog « Quitter Kotatsu ? » (P2 : Confirmation de sortie, adaptation desktop du double-back). */
@Composable
private fun ExitConfirmationDialog(
	onDismiss: () -> Unit,
	onConfirm: () -> Unit,
) {
	androidx.compose.material3.AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = org.koitharu.kotatsu.desktop.ui.strings.FR_EXIT_DIALOG_TITLE) },
		text = {},
		confirmButton = {
			androidx.compose.material3.TextButton(onClick = onConfirm) {
				Text(text = org.koitharu.kotatsu.desktop.ui.strings.FR_EXIT_DIALOG_CONFIRM)
			}
		},
		dismissButton = {
			androidx.compose.material3.TextButton(onClick = onDismiss) {
				Text(text = org.koitharu.kotatsu.desktop.ui.strings.FR_CANCEL)
			}
		},
	)
}
