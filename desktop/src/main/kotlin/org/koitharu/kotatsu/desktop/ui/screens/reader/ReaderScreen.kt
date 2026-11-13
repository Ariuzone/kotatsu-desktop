/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — phase P1 :
 *  - modes du lecteur d'origine (Standard / De droite à gauche / Vertical / Webtoon, prefs ReaderMode)
 *  - navigation molette (adaptation desktop, UI_DIVERGENCES #5) et clavier avec focus explicite
 *  - zoom Ctrl+molette + double-clic (pager)
 *  - mise à l'échelle (ScaleMode), arrière-plan (ReaderBackground), pages numérotées
 *  - séparateurs webtoon (espaces verticaux), animation NONE = saut de page sans animation
 * Zones de tap équivalentes aux gestes Android (précédent / contrôles / suivant), orientation
 * inversée en mode RTL ; overlay de contrôles : panneau haut (retour, titres, compteur
 * « Ch. %1$d/%2$d Pg. %3$d/%4$d ») et panneau bas (chapitre précédent, curseur, chapitre suivant).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 * Modification : 2026-11-08 — phase P7 (vague 1) : menu ⋮ du lecteur (vidéo t≈7:25-9:20) :
 *  - « Sauvegarder la page » / « Ajouter un marque-page » (câblés par l'hôte, cf. Main.kt)
 *  - « Mode lecture » : 4 options radio (Standard / De droite à gauche / Vertical / Webtoon),
 *    appliquées immédiatement, la position de lecture est conservée lors du changement
 *  - « Pages doubles (paysage) — bêta » : 2 pages par vue du pager en fenêtre paysage
 *  - « Faire pivoter l'écran » : cycle 0° → 90° → 180° → 270° (rotation du contenu seul)
 *  - chapitre Précédent / Suivant, placeholders désactivés vague 2 (Défilement automatique,
 *    Correction des couleurs), Incognito / Zoom / Fermer conservés
 *  - compteur d'overlay complété au format vidéo : « Ch. X/Y Pg. X/Y Z % »
 * Modification : 2026-11-08 — phase P7 (vague 2) : intégration des composants purs de P7-B
 * (ui/screens/reader/components/, chaînes dans ui/strings/FrReader.kt) :
 *  - « Correction des couleurs » : ColorCorrectionDialog + Modifier.colorCorrection appliqué
 *    au conteneur des PAGES uniquement (les contrôles restent hors du filtre) ; état local
 *  - « Défilement automatique » : AutoScrollDialog + AutoScrollFab (BottomEnd, au-dessus des
 *    contrôles) ; webtoon = défilement continu en px (withFrameMillis + scrollBy) ; modes pager
 *    = saut de page périodique toutes les (4000 / vitesse) ms (le pager ne défile pas en continu)
 *  - « Gomme » (webtoon uniquement) : EraserOverlay superposé au contenu, tuto EraserTutorialDialog
 *    à la première ouverture webtoon de la session ; les rectangles normalisés (0..1) sont
 *    persistés par l'hôte (Main.kt) dans PageScanEntity.bands et recouverts d'un rect couleur fond
 *  - les contrôles sont SORTIS du Box pivoté (« Faire pivoter » ne tourne plus que les pages)
 * TODO(vague 3) : migrer les chaînes temporaires ci-dessous vers FrReader.kt (fichier propriété
 * de P7-B — migration réservée à l'agent principal).
 */
package org.koitharu.kotatsu.desktop.ui.screens.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.core.prefs.ReaderAnimation
import org.koitharu.kotatsu.core.prefs.ReaderBackground
import org.koitharu.kotatsu.core.prefs.ReaderMode
import org.koitharu.kotatsu.core.prefs.ScaleMode
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.AutoScrollDialog
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.AutoScrollFab
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.AutoScrollState
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.ColorCorrectionDialog
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.ColorCorrectionState
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.EraserOverlay
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.EraserTutorialDialog
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.autoScrollStep
import org.koitharu.kotatsu.desktop.ui.screens.reader.components.colorCorrection
import org.koitharu.kotatsu.desktop.ui.strings.FR_AUTO_SCROLL
import org.koitharu.kotatsu.desktop.ui.strings.FR_CLOSE
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLOR_CORRECTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEXT_CHAPTER
import org.koitharu.kotatsu.desktop.ui.strings.FR_PREV_CHAPTER
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_ADD_BOOKMARK
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_DOUBLE_PAGES
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_ERASER
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_MORE_OPTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_READING_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_REMOVE_BOOKMARK
import org.koitharu.kotatsu.desktop.ui.strings.FR_READER_ROTATE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SAVE_PAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_ZOOM_IN
import org.koitharu.kotatsu.desktop.ui.strings.FR_ZOOM_OUT
import org.koitharu.kotatsu.desktop.ui.strings.frReaderInfo
import org.koitharu.kotatsu.desktop.ui.strings.frReaderModeLabel

/** Multiplicateur de zoom par pas de Ctrl+molette / boutons. */
private const val ZOOM_STEP = 1.25f
private const val ZOOM_MAX = 6f

/** Intervalle de base (ms) entre deux sauts de page en défilement automatique pager (x1.0). */
private const val AUTO_SCROLL_PAGE_INTERVAL_MS = 4000.0

/**
 * Tuto gomme déjà affiché pendant cette session (état local, spec vague 2 #3 : à la première
 * ouverture d'un manga en mode WEBTOON seulement).
 */
private var eraserTutorialShownThisSession = false

@Composable
public fun ReaderContent(
	title: String,
	chapterTitle: String,
	pageUrls: List<String>,
	mode: ReaderMode,
	animation: ReaderAnimation,
	background: ReaderBackground,
	scaleMode: ScaleMode,
	isPagesNumbered: Boolean,
	isWebtoonSeparators: Boolean,
	isIncognito: Boolean,
	chapterIndex: Int,
	chaptersCount: Int,
	isBookmarked: Boolean,
	onModeChange: (ReaderMode) -> Unit,
	onSavePage: () -> Unit,
	onToggleBookmark: () -> Unit,
	onToggleIncognito: ((Boolean) -> Unit)?,
	onClose: () -> Unit,
	onPrevChapter: (() -> Unit)?,
	onNextChapter: (() -> Unit)?,
	onPageChange: ((page: Int) -> Unit)?,
	erasedRegions: Map<Int, List<Rect>> = emptyMap(),
	onErasePageRegion: (page: Int, region: Rect) -> Unit = { _, _ -> },
) {
	var controlsVisible by remember { mutableStateOf(true) }
	val scope = rememberCoroutineScope()
	val focusRequester = remember { FocusRequester() }
	var zoom by remember { mutableFloatStateOf(1f) }
	val bgColor = Color(background.argb)
	// Phase P7 : rotation de l'écran (état local du lecteur) et pages doubles (bêta,
	// état local sans persistance — cf. menu ⋮)
	var rotation by remember { mutableIntStateOf(0) }
	var isDoublePages by remember { mutableStateOf(false) }
	// Dernière page vue (index de page, pas de vue) : sert à conserver la position
	// lors d'un changement de mode ou de pages doubles
	var lastPage by remember { mutableIntStateOf(0) }
	// Raccourci clavier Alt+M : ouvre le menu ⋮ du lecteur (adaptation desktop)
	var menuTrigger by remember { mutableIntStateOf(0) }
	// Vague 2 : états des composants avancés (locaux au lecteur, sans persistance)
	var colorCorrection by remember { mutableStateOf(ColorCorrectionState()) }
	var autoScroll by remember { mutableStateOf(AutoScrollState()) }
	var showColorCorrectionDialog by remember { mutableStateOf(false) }
	var showAutoScrollDialog by remember { mutableStateOf(false) }
	var isEraseMode by remember { mutableStateOf(false) }
	var showEraserTutorial by remember { mutableStateOf(false) }

	val isWebtoon = mode == ReaderMode.WEBTOON

	BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
		// Pages doubles : pager horizontal uniquement (Standard / De droite à gauche),
		// et seulement en fenêtre paysage (comme l'intitulé du réglage d'origine)
		val isLandscape = maxWidth > maxHeight
		val isDoublePagesActive = isDoublePages && isLandscape && !isWebtoon &&
			(mode == ReaderMode.STANDARD || mode == ReaderMode.REVERSED)
		val pagesPerView = if (isDoublePagesActive) 2 else 1
		val pageCount = pageUrls.size
		val pagerPageCount = (pageCount + pagesPerView - 1) / pagesPerView

		val pagerState = rememberPagerState(pageCount = { pagerPageCount })
		val listState = rememberLazyListState()

		/** Page courante selon le mode (premier item visible en webtoon, première page de la vue sinon). */
		fun currentPage(): Int =
			if (isWebtoon) listState.firstVisibleItemIndex else pagerState.currentPage * pagesPerView

		suspend fun goToPage(target: Int) {
			if (target !in pageUrls.indices) return
			if (isWebtoon) {
				if (animation == ReaderAnimation.NONE) listState.scrollToItem(target)
				else listState.animateScrollToItem(target)
			} else {
				pagerState.snapOrAnimate(target / pagesPerView, animation)
			}
		}

		// Navigation clavier (flèches / PgUp-PgDn / Espace / Début-Fin / Échap)
		// et molette : page suivante/précédente ; Ctrl+molette : zoom (pager uniquement).
		val scrollLoop = if (isWebtoon) Modifier else Modifier.pointerInput(pageUrls.size) {
			awaitEachGesture {
				// boucle d'écoute des événements molette hors gestes de tap
				val event = awaitPointerEvent()
				if (event.type == PointerEventType.Scroll) {
					val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
					if (scrollDelta != 0f) {
						if (event.keyboardModifiers.isCtrlPressed) {
							zoom = (zoom * if (scrollDelta > 0f) 1f / ZOOM_STEP else ZOOM_STEP)
								.coerceIn(1f, ZOOM_MAX)
						} else {
							scope.launch { goToPage(currentPage() + if (scrollDelta > 0f) 1 else -1) }
						}
					}
				}
			}
		}

		LaunchedEffect(Unit) { focusRequester.requestFocus() }
		LaunchedEffect(pageUrls) {
			// nouveau chapitre : retour au début et remise du zoom
			pagerState.scrollToPage(0)
			listState.scrollToItem(0)
			zoom = 1f
			lastPage = 0
		}
		// Changement de mode / de pages doubles : conserve la page de lecture ;
		// le mode gomme est désactivé hors webtoon ; tuto gomme à la 1re ouverture webtoon
		LaunchedEffect(mode, pagesPerView) {
			if (pageCount > 0) {
				if (isWebtoon) {
					listState.scrollToItem(lastPage.coerceIn(0, pageCount - 1))
				} else {
					pagerState.scrollToPage((lastPage / pagesPerView).coerceIn(0, pagerPageCount - 1))
				}
			}
			if (mode != ReaderMode.WEBTOON) {
				isEraseMode = false
			} else if (!eraserTutorialShownThisSession) {
				showEraserTutorial = true
			}
		}
		// Progression : chaque page vue enregistre la progression (comme l'app d'origine)
		LaunchedEffect(pagerState.currentPage) {
			if (!isWebtoon) {
				lastPage = pagerState.currentPage * pagesPerView
				onPageChange?.invoke(lastPage)
			}
		}
		LaunchedEffect(listState.isScrollInProgress) {
			if (isWebtoon && !listState.isScrollInProgress) {
				lastPage = listState.firstVisibleItemIndex
				onPageChange?.invoke(lastPage)
			}
		}
		// Défilement automatique (vague 2) : webtoon = défilement continu en px
		// (withFrameMillis ~16 ms + scrollBy) ; les modes pager défileront en sauts de page
		if (isWebtoon) {
			LaunchedEffect(autoScroll.enabled, autoScroll.isRunning, autoScroll.speed) {
				while (autoScroll.enabled && autoScroll.isRunning) {
					withFrameMillis { } // un tick par frame (~16 ms)
					listState.scrollBy(autoScrollStep(autoScroll))
				}
			}
		}
		// Défilement automatique en modes pager : le pager ne défile pas en continu —
		// adaptation desktop : saut de page périodique toutes les (4000 / vitesse) ms.
		if (!isWebtoon) {
			LaunchedEffect(autoScroll.enabled, autoScroll.isRunning, autoScroll.speed, pagerState.currentPage) {
				if (autoScroll.enabled && autoScroll.isRunning) {
					delay((AUTO_SCROLL_PAGE_INTERVAL_MS / autoScroll.speed).toLong())
					goToPage(currentPage() + 1)
				}
			}
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(bgColor)
				.focusRequester(focusRequester)
				.focusable()
				.then(scrollLoop)
				.onKeyEvent { event ->
					if (event.type != KeyEventType.KeyUp) return@onKeyEvent false
					when (event.key) {
						Key.DirectionRight -> {
							val step = if (mode == ReaderMode.REVERSED) -1 else 1
							scope.launch { goToPage(currentPage() + step) }
							true
						}
						Key.DirectionLeft -> {
							val step = if (mode == ReaderMode.REVERSED) 1 else -1
							scope.launch { goToPage(currentPage() + step) }
							true
						}
						Key.DirectionDown, Key.PageDown, Key.Spacebar -> {
							scope.launch { goToPage(currentPage() + 1) }
							true
						}
						Key.DirectionUp, Key.PageUp -> {
							scope.launch { goToPage(currentPage() - 1) }
							true
						}
						Key.Home -> {
							scope.launch { goToPage(0) }
							true
						}
						Key.MoveEnd -> {
							scope.launch { goToPage(pageUrls.lastIndex) }
							true
						}
						Key.Escape -> {
							if (isEraseMode) {
								isEraseMode = false
							} else {
								onClose()
							}
							true
						}
						Key.M -> {
							if (event.isAltPressed) {
								menuTrigger++
								true
							} else {
								false
							}
						}
						else -> false
					}
				}
					.pointerInput(pageUrls.size, mode, isEraseMode, controlsVisible) {
						detectTapGestures(
							onTap = { position ->
								if (isEraseMode) return@detectTapGestures // le tap ne tourne pas la page en mode gomme
								// Correctif P7 : avec les contrôles visibles, les taps dans les bandes
								// haute (top bar) et basse (zoom/slider) ne tournent pas la page —
								// ils atteignent les boutons (⋮, retour) sans être avalés par les
								// zones de tap.
								val inTopBar = controlsVisible && position.y < 96f
								val inBottomBar = controlsVisible && position.y > size.height - 160f
								if (inTopBar || inBottomBar) return@detectTapGestures
								when {
								// Zones de tap : orientation inversée en mode RTL (comme l'app d'origine)
								(if (mode == ReaderMode.REVERSED) {
									position.x > 2f * size.width / 3f
								} else {
									position.x < size.width / 3f
								}) -> scope.launch { goToPage(currentPage() - 1) }
								(if (mode == ReaderMode.REVERSED) {
									position.x < size.width / 3f
								} else {
									position.x > 2f * size.width / 3f
								}) -> scope.launch { goToPage(currentPage() + 1) }
								else -> controlsVisible = !controlsVisible
							}
						},
						onDoubleTap = { if (!isEraseMode) zoom = if (zoom > 1f) 1f else 2.5f },
					)
				},
		) {
			// Contenu pivoté (« Faire pivoter l'écran ») : seules les pages tournent —
			// les contrôles sont posés APRÈS ce Box (hors rotation et hors filtre couleur).
			// Modifier.colorCorrection est attaché à CE conteneur (pages uniquement), pas
			// aux contrôles ni aux dialogs.
			Box(
				modifier = Modifier
					.fillMaxSize()
					.colorCorrection(colorCorrection)
					.graphicsLayer {
						rotationZ = rotation.toFloat()
						if (rotation == 90 || rotation == 270) {
							val fit = minOf(size.width / size.height, size.height / size.width)
							scaleX = fit
							scaleY = fit
						}
					},
			) {
				when (mode) {
					ReaderMode.STANDARD, ReaderMode.REVERSED, ReaderMode.VERTICAL -> {
						val pagerModifier: Modifier = Modifier
							.fillMaxSize()
							.graphicsLayer {
								scaleX = zoom
								scaleY = zoom
							}
						// Double vue (pages doubles, bêta) : en RTL la page de droite est lue
						// en premier, l'ordre des pages de la vue est donc inversé.
						val spreadOffsets = if (mode == ReaderMode.REVERSED) listOf(1, 0) else listOf(0, 1)
						if (mode == ReaderMode.VERTICAL) {
							VerticalPager(state = pagerState, modifier = pagerModifier) { group ->
								Row(modifier = Modifier.fillMaxSize()) {
									for (offset in 0 until pagesPerView) {
										ReaderPageImage(
											pageUrl = pageUrls.getOrNull(group * pagesPerView + offset),
											scaleMode = scaleMode,
											isPagesNumbered = isPagesNumbered,
											pageNumber = group * pagesPerView + offset + 1,
											pagesCount = pageCount,
											modifier = Modifier.weight(1f),
										)
									}
								}
							}
						} else {
							HorizontalPager(
								state = pagerState,
								modifier = pagerModifier,
								reverseLayout = mode == ReaderMode.REVERSED,
							) { group ->
								Row(modifier = Modifier.fillMaxSize()) {
									for (offset in spreadOffsets.take(pagesPerView)) {
										ReaderPageImage(
											pageUrl = pageUrls.getOrNull(group * pagesPerView + offset),
											scaleMode = scaleMode,
											isPagesNumbered = isPagesNumbered,
											pageNumber = group * pagesPerView + offset + 1,
											pagesCount = pageCount,
											modifier = Modifier.weight(1f),
										)
									}
								}
							}
						}
					}

					ReaderMode.WEBTOON -> {
						LazyColumn(
							state = listState,
							modifier = Modifier.fillMaxSize(),
							verticalArrangement = Arrangement.spacedBy(
								if (isWebtoonSeparators) 12.dp else 0.dp,
							),
						) {
							// clé par index : une même image peut apparaître sur deux pages
							itemsIndexed(pageUrls) { index, pageUrl ->
								ReaderPageImage(
									pageUrl = pageUrl,
									scaleMode = ScaleMode.FIT_WIDTH,
									fillHeight = false,
									isPagesNumbered = isPagesNumbered,
									pageNumber = index + 1,
									pagesCount = pageCount,
									// Masques de gomme : normalisés aux bounds de l'item (= l'image
									// en webtoon, qui remplit la largeur de l'item)
									erasedRegions = erasedRegions[index].orEmpty(),
									maskColor = bgColor,
								)
							}
						}
					}
				}
				// Overlay gomme (vague 2) : superposé au contenu webtoon ; normalisé à la
				// taille du viewport (= LazyColumn en webtoon). Hors webtoon : transparent.
				EraserOverlay(
					isEraseMode = isEraseMode && isWebtoon,
					onErase = { selection ->
						// conversion rect viewport (normalisé) → rects normalisés par page
						// visible (l'item webtoon remplit la largeur, x est donc identique)
						val info = listState.layoutInfo
						val viewportW = info.viewportSize.width.toFloat()
						val viewportH = info.viewportSize.height.toFloat()
						if (viewportW > 0f && viewportH > 0f) {
							val leftPx = selection.left * viewportW
							val rightPx = selection.right * viewportW
							val topPx = selection.top * viewportH
							val bottomPx = selection.bottom * viewportH
							for (item in info.visibleItemsInfo) {
								val itemTop = item.offset.toFloat()
								val itemBottom = (item.offset + item.size).toFloat()
								val top = maxOf(topPx, itemTop)
								val bottom = minOf(bottomPx, itemBottom)
								if (bottom - top <= 1f) continue
								onErasePageRegion(
									item.index,
									Rect(
										left = (leftPx / viewportW).coerceIn(0f, 1f),
										top = ((top - itemTop) / (itemBottom - itemTop)).coerceIn(0f, 1f),
										right = (rightPx / viewportW).coerceIn(0f, 1f),
										bottom = ((bottom - itemTop) / (itemBottom - itemTop)).coerceIn(0f, 1f),
									),
								)
							}
						}
					},
					modifier = Modifier.matchParentSize(),
				)
			}
			// Contrôles HORS du Box pivoté et HORS du filtre couleur (vague 2)
			ReaderControls(
				visible = controlsVisible,
				menuTrigger = menuTrigger,
				title = title,
				chapterTitle = chapterTitle,
				pageCounter = frReaderInfo(
					chapter = chapterIndex + 1,
					chaptersCount = chaptersCount,
					page = currentPage() + 1,
					pagesCount = pageCount,
				) + frReaderPercent(chapterIndex, chaptersCount, currentPage(), pageCount),
				currentPage = if (isWebtoon) listState.firstVisibleItemIndex else pagerState.currentPage,
				pageCount = if (isWebtoon) pageCount else pagerPageCount,
				zoom = if (isWebtoon) 1f else zoom,
				isIncognito = isIncognito,
				mode = mode,
				isBookmarked = isBookmarked,
				isDoublePages = isDoublePages,
				isEraseMode = isEraseMode,
				isEraseAvailable = isWebtoon,
				onPageSeek = if (isWebtoon) {
					null // le défilement webtoon est continu
				} else {
					{ page -> scope.launch { pagerState.snapOrAnimate(page, animation) } }
				},
				onZoom = if (isWebtoon) null else { factor -> zoom = (zoom * factor).coerceIn(1f, ZOOM_MAX) },
				onModeChange = onModeChange,
				onSavePage = onSavePage,
				onToggleBookmark = onToggleBookmark,
				onToggleDoublePages = {
					isDoublePages = !isDoublePages
					controlsVisible = !controlsVisible
				},
				onRotate = {
					rotation = (rotation + 90) % 360
					controlsVisible = !controlsVisible
				},
				onToggleEraseMode = { isEraseMode = !isEraseMode },
				onAutoScrollClick = { showAutoScrollDialog = true },
				onColorCorrectionClick = { showColorCorrectionDialog = true },
				onToggleIncognito = onToggleIncognito,
				onPrevChapter = onPrevChapter,
				onNextChapter = onNextChapter,
				onClose = onClose,
			)
			// Bouton de commande du défilement automatique (vague 2) : au-dessus des
			// contrôles (z-order : posé après), coin bas-droit, invisible si désactivé
			AutoScrollFab(
				state = autoScroll,
				onClick = { autoScroll = autoScroll.copy(isRunning = !autoScroll.isRunning) },
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(16.dp),
			)
		}

		// Dialogs des composants avancés (vague 2)
		if (showColorCorrectionDialog) {
			ColorCorrectionDialog(
				state = colorCorrection,
				onChange = { colorCorrection = it },
				onDismiss = { showColorCorrectionDialog = false },
			)
		}
		if (showAutoScrollDialog) {
			AutoScrollDialog(
				state = autoScroll,
				onChange = { autoScroll = it },
				onDismiss = { showAutoScrollDialog = false },
			)
		}
		if (showEraserTutorial) {
			EraserTutorialDialog(
				onDismiss = {
					showEraserTutorial = false
					eraserTutorialShownThisSession = true
				},
			)
		}
	}
}

// TODO(vague 3) : migrer vers FrReader.kt — pourcentage de lecture global du manga,
// format vidéo « Ch. 1/26 Pg. 2/28 0 % » (chapitre en cours ramené sur le total).
private fun frReaderPercent(chapterIndex: Int, chaptersCount: Int, page: Int, pagesCount: Int): String {
	if (chaptersCount <= 0 || pagesCount <= 0) return ""
	val percent = ((chapterIndex + (page + 1f) / pagesCount) / chaptersCount * 100f).toInt()
	return " $percent %"
}

/** Saut de page avec ou sans animation selon la préférence « Animation de page ». */
private suspend fun PagerState.snapOrAnimate(target: Int, animation: ReaderAnimation) {
	if (target !in 0 until pageCount) return
	if (animation == ReaderAnimation.NONE) scrollToPage(target) else animateScrollToPage(target)
}

/**
 * Contrôles du lecteur : panneau haut (activity_reader.xml : appbar_top avec retour + titres,
 * badge incognito) et panneau bas (layout_reader_actions.xml : chapitre précédent / curseur /
 * chapitre suivant). Phase P7 : menu ⋮ (Sauvegarder la page, marque-page, Mode lecture,
 * Pages doubles, Faire pivoter, chapitres, Défilement automatique / Correction des couleurs /
 * Gomme (vague 2), Incognito, Zoom, Fermer).
 */
@Composable
private fun ReaderControls(
	visible: Boolean,
	menuTrigger: Int = 0,
	title: String,
	chapterTitle: String,
	pageCounter: String,
	currentPage: Int,
	pageCount: Int,
	zoom: Float,
	isIncognito: Boolean,
	mode: ReaderMode,
	isBookmarked: Boolean,
	isDoublePages: Boolean,
	isEraseMode: Boolean,
	isEraseAvailable: Boolean,
	onPageSeek: ((Int) -> Unit)?,
	onZoom: ((Float) -> Unit)?,
	onModeChange: (ReaderMode) -> Unit,
	onSavePage: () -> Unit,
	onToggleBookmark: () -> Unit,
	onToggleDoublePages: (() -> Unit)?,
	onRotate: () -> Unit,
	onToggleEraseMode: () -> Unit,
	onAutoScrollClick: () -> Unit,
	onColorCorrectionClick: () -> Unit,
	onToggleIncognito: ((Boolean) -> Unit)?,
	onPrevChapter: (() -> Unit)?,
	onNextChapter: (() -> Unit)?,
	onClose: () -> Unit,
) {
	if (!visible) return
	var menuExpanded by remember { mutableStateOf(false) }
	// Raccourci clavier Alt+M (adaptation desktop) : ouvre le menu ⋮
	LaunchedEffect(menuTrigger) {
		if (menuTrigger > 0) menuExpanded = true
	}
	Column(modifier = Modifier.fillMaxSize()) {
		// Panneau haut
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(Color.Black.copy(alpha = 0.6f))
				.padding(horizontal = 8.dp, vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(onClick = onClose) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = FR_CLOSE,
					tint = Color.White,
				)
			}
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = chapterTitle,
					style = MaterialTheme.typography.titleMedium,
					color = Color.White,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = title,
					style = MaterialTheme.typography.bodySmall,
					color = Color.White.copy(alpha = 0.7f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
			if (isIncognito) {
				Text(
					text = FR_INCOGNITO,
					style = MaterialTheme.typography.labelLarge,
					color = Color.White.copy(alpha = 0.9f),
					modifier = Modifier.padding(end = 8.dp),
				)
			}
			Text(
				text = pageCounter,
				style = MaterialTheme.typography.labelLarge,
				color = Color.White,
				modifier = Modifier.padding(end = 8.dp),
			)
			// Menu ⋮ du lecteur (vidéo t≈7:25)
			Box {
				IconButton(onClick = { menuExpanded = true }) {
					Icon(
						imageVector = Icons.Filled.MoreVert,
						contentDescription = FR_READER_MORE_OPTIONS,
						tint = Color.White,
					)
				}
				DropdownMenu(
					expanded = menuExpanded,
					onDismissRequest = { menuExpanded = false },
				) {
					DropdownMenuItem(
						text = { Text(FR_SAVE_PAGE) },
						onClick = {
							menuExpanded = false
							onSavePage()
						},
					)
					DropdownMenuItem(
						text = {
							Text(if (isBookmarked) FR_READER_REMOVE_BOOKMARK else FR_READER_ADD_BOOKMARK)
						},
						leadingIcon = {
							Icon(
								imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
								contentDescription = null,
							)
						},
						onClick = {
							menuExpanded = false
							onToggleBookmark()
						},
					)
					HorizontalDivider()
					// « Mode lecture » : 4 options radio (ordre vidéo), appliquées immédiatement
					Text(
						text = FR_READER_READING_MODE,
						style = MaterialTheme.typography.labelLarge,
						modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
					)
					for (candidate in ReaderMode.entries) {
						DropdownMenuItem(
							text = { Text(frReaderModeLabel(candidate)) },
							leadingIcon = {
								RadioButton(selected = mode == candidate, onClick = null)
							},
							onClick = {
								menuExpanded = false
								onModeChange(candidate)
							},
						)
					}
					HorizontalDivider()
					if (onToggleDoublePages != null) {
						DropdownMenuItem(
							text = { Text(FR_READER_DOUBLE_PAGES) },
							leadingIcon = {
								Checkbox(checked = isDoublePages, onCheckedChange = null)
							},
							onClick = onToggleDoublePages,
						)
					}
					DropdownMenuItem(
						text = { Text(FR_READER_ROTATE) },
						onClick = onRotate,
					)
					HorizontalDivider()
					// Vague 2 : entrées des composants avancés (chaînes de FrReader.kt)
					DropdownMenuItem(
						text = { Text(FR_AUTO_SCROLL) },
						onClick = {
							menuExpanded = false
							onAutoScrollClick()
						},
					)
					DropdownMenuItem(
						text = { Text(FR_COLOR_CORRECTION) },
						onClick = {
							menuExpanded = false
							onColorCorrectionClick()
						},
					)
					// Gomme : webtoon uniquement (le masque est normalisé aux bounds de l'item,
					// correct seulement quand l'image remplit l'item, cf. rapport vague 2)
					DropdownMenuItem(
						text = { Text(FR_READER_ERASER) },
						enabled = isEraseAvailable,
						leadingIcon = {
							Checkbox(checked = isEraseMode, onCheckedChange = null)
						},
						onClick = {
							menuExpanded = false
							onToggleEraseMode()
						},
					)
					HorizontalDivider()
					DropdownMenuItem(
						text = { Text(FR_PREV_CHAPTER) },
						enabled = onPrevChapter != null,
						onClick = {
							menuExpanded = false
							onPrevChapter?.invoke()
						},
					)
					DropdownMenuItem(
						text = { Text(FR_NEXT_CHAPTER) },
						enabled = onNextChapter != null,
						onClick = {
							menuExpanded = false
							onNextChapter?.invoke()
						},
					)
					HorizontalDivider()
					if (onToggleIncognito != null) {
						DropdownMenuItem(
							text = { Text(FR_INCOGNITO) },
							leadingIcon = {
								Checkbox(checked = isIncognito, onCheckedChange = null)
							},
							onClick = {
								menuExpanded = false
								onToggleIncognito(!isIncognito)
							},
						)
					}
					if (onZoom != null) {
						DropdownMenuItem(
							text = { Text(FR_ZOOM_IN) },
							enabled = zoom < ZOOM_MAX,
							onClick = { onZoom(ZOOM_STEP) },
						)
						DropdownMenuItem(
							text = { Text(FR_ZOOM_OUT) },
							enabled = zoom > 1f,
							onClick = { onZoom(1f / ZOOM_STEP) },
						)
					}
					DropdownMenuItem(
						text = { Text(FR_CLOSE) },
						onClick = {
							menuExpanded = false
							onClose()
						},
					)
				}
			}
		}
		Spacer(modifier = Modifier.weight(1f))
		// Panneau bas (layout_reader_actions.xml)
		if (pageCount > 0) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(Color.Black.copy(alpha = 0.6f))
					.padding(horizontal = 8.dp, vertical = 4.dp),
			) {
				if (onZoom != null) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically,
					) {
						IconButton(onClick = { onZoom(1f / ZOOM_STEP) }) {
							Icon(Icons.Filled.Remove, contentDescription = FR_ZOOM_OUT, tint = Color.White)
						}
						IconButton(onClick = { onZoom(ZOOM_STEP) }) {
							Icon(Icons.Filled.Add, contentDescription = FR_ZOOM_IN, tint = Color.White)
						}
						Text(
							text = "${(zoom * 100).toInt()} %",
							style = MaterialTheme.typography.labelLarge,
							color = Color.White,
						)
					}
				}
				if (onPageSeek != null) {
					Slider(
						value = currentPage.toFloat(),
						onValueChange = { value -> onPageSeek(value.toInt().coerceIn(0, pageCount - 1)) },
						valueRange = 0f..(pageCount - 1).coerceAtLeast(1).toFloat(),
						colors = SliderDefaults.colors(
							thumbColor = Color.White,
							activeTrackColor = Color.White,
							inactiveTrackColor = Color.White.copy(alpha = 0.3f),
						),
					)
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					IconButton(onClick = { onPrevChapter?.invoke() }, enabled = onPrevChapter != null) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.Undo,
							contentDescription = FR_PREV_CHAPTER,
							tint = if (onPrevChapter != null) Color.White else Color.White.copy(alpha = 0.4f),
						)
					}
					Spacer(modifier = Modifier.weight(1f))
					IconButton(onClick = { onNextChapter?.invoke() }, enabled = onNextChapter != null) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.Redo,
							contentDescription = FR_NEXT_CHAPTER,
							tint = if (onNextChapter != null) Color.White else Color.White.copy(alpha = 0.4f),
						)
					}
				}
			}
		}
	}
}

@Composable
private fun ReaderPageImage(
	pageUrl: String?,
	scaleMode: ScaleMode,
	isPagesNumbered: Boolean,
	pageNumber: Int,
	pagesCount: Int,
	modifier: Modifier = Modifier.fillMaxSize(),
	fillHeight: Boolean = true,
	erasedRegions: List<Rect> = emptyList(),
	maskColor: Color = Color.Transparent,
) {
	if (pageUrl == null) return
	val contentScale = when {
		!fillHeight -> ContentScale.FillWidth // webtoon : largeur ajustée en continu
		else -> when (scaleMode) {
			ScaleMode.FIT_CENTER -> ContentScale.Fit
			ScaleMode.FIT_WIDTH -> ContentScale.FillWidth
			ScaleMode.FIT_HEIGHT -> ContentScale.FillHeight
			ScaleMode.CROP -> ContentScale.Crop
		}
	}
	Box(modifier = modifier, contentAlignment = Alignment.Center) {
		AsyncImage(
			model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
				.data(pageUrl)
				.build(),
			contentDescription = null,
			contentScale = contentScale,
			modifier = if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
		)
		// Masques de gomme (vague 2) : rects couleur fond dessinés par-dessus la page,
		// coordonnées normalisées (0..1) rapportées aux bounds de CE Box — exact quand
		// l'image remplit l'item (webtoon, largeur entière)
		if (erasedRegions.isNotEmpty()) {
			Canvas(modifier = Modifier.matchParentSize()) {
				for (region in erasedRegions) {
					drawRect(
						color = maskColor,
						topLeft = Offset(region.left * size.width, region.top * size.height),
						size = Size(region.width * size.width, region.height * size.height),
					)
				}
			}
		}
		if (isPagesNumbered) {
			Text(
				text = "$pageNumber / $pagesCount",
				style = MaterialTheme.typography.labelMedium,
				color = Color.White.copy(alpha = 0.85f),
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(8.dp)
					.background(Color.Black.copy(alpha = 0.5f))
					.padding(horizontal = 6.dp, vertical = 2.dp),
			)
		}
	}
}
