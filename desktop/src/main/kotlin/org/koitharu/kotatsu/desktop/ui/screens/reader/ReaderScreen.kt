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
 */
package org.koitharu.kotatsu.desktop.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.core.prefs.ReaderAnimation
import org.koitharu.kotatsu.core.prefs.ReaderBackground
import org.koitharu.kotatsu.core.prefs.ReaderMode
import org.koitharu.kotatsu.core.prefs.ScaleMode
import org.koitharu.kotatsu.desktop.ui.strings.FR_NEXT_CHAPTER
import org.koitharu.kotatsu.desktop.ui.strings.FR_PREV_CHAPTER
import org.koitharu.kotatsu.desktop.ui.strings.frReaderInfo

/** Multiplicateur de zoom par pas de Ctrl+molette / boutons. */
private const val ZOOM_STEP = 1.25f
private const val ZOOM_MAX = 6f

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
	onClose: () -> Unit,
	onPrevChapter: (() -> Unit)?,
	onNextChapter: (() -> Unit)?,
	onPageChange: ((page: Int) -> Unit)?,
) {
	var controlsVisible by remember { mutableStateOf(true) }
	val scope = rememberCoroutineScope()
	val focusRequester = remember { FocusRequester() }
	var zoom by remember { mutableFloatStateOf(1f) }
	val bgColor = Color(background.argb)

	val isWebtoon = mode == ReaderMode.WEBTOON
	val pagerState = rememberPagerState(pageCount = { pageUrls.size })
	val listState = rememberLazyListState()

	/** Page courante selon le mode (premier item visible en webtoon). */
	fun currentPage(): Int =
		if (isWebtoon) listState.firstVisibleItemIndex else pagerState.currentPage

	suspend fun goToPage(target: Int) {
		if (target !in pageUrls.indices) return
		if (isWebtoon) {
			if (animation == ReaderAnimation.NONE) listState.scrollToItem(target)
			else listState.animateScrollToItem(target)
		} else {
			pagerState.snapOrAnimate(target, animation)
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
	}
	// Progression : chaque page vue enregistre la progression (comme l'app d'origine)
	LaunchedEffect(pagerState.currentPage) {
		if (!isWebtoon) onPageChange?.invoke(pagerState.currentPage)
	}
	LaunchedEffect(listState.isScrollInProgress) {
		if (isWebtoon && !listState.isScrollInProgress) {
			onPageChange?.invoke(listState.firstVisibleItemIndex)
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
						onClose()
						true
					}
					else -> false
				}
			}
			.pointerInput(pageUrls.size, mode) {
				detectTapGestures(
					onTap = { position ->
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
					onDoubleTap = { zoom = if (zoom > 1f) 1f else 2.5f },
				)
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
				if (mode == ReaderMode.VERTICAL) {
					VerticalPager(state = pagerState, modifier = pagerModifier) { page ->
						ReaderPageImage(
							pageUrl = pageUrls.getOrNull(page),
							scaleMode = scaleMode,
							isPagesNumbered = isPagesNumbered,
							pageNumber = page + 1,
							pagesCount = pageUrls.size,
						)
					}
				} else {
					HorizontalPager(
						state = pagerState,
						modifier = pagerModifier,
						reverseLayout = mode == ReaderMode.REVERSED,
					) { page ->
						ReaderPageImage(
							pageUrl = pageUrls.getOrNull(page),
							scaleMode = scaleMode,
							isPagesNumbered = isPagesNumbered,
							pageNumber = page + 1,
							pagesCount = pageUrls.size,
						)
					}
				}
				ReaderControls(
					visible = controlsVisible,
					title = title,
					chapterTitle = chapterTitle,
					pageCounter = frReaderInfo(
						chapter = chapterIndex + 1,
						chaptersCount = chaptersCount,
						page = pagerState.currentPage + 1,
						pagesCount = pageUrls.size,
					),
					currentPage = pagerState.currentPage,
					pageCount = pageUrls.size,
					zoom = zoom,
					isIncognito = isIncognito,
					onPageSeek = { page -> scope.launch { pagerState.snapOrAnimate(page, animation) } },
					onZoom = { factor -> zoom = (zoom * factor).coerceIn(1f, ZOOM_MAX) },
					onPrevChapter = onPrevChapter,
					onNextChapter = onNextChapter,
					onClose = onClose,
				)
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
							pagesCount = pageUrls.size,
						)
					}
				}
				ReaderControls(
					visible = controlsVisible,
					title = title,
					chapterTitle = chapterTitle,
					pageCounter = frReaderInfo(
						chapter = chapterIndex + 1,
						chaptersCount = chaptersCount,
						page = listState.firstVisibleItemIndex + 1,
						pagesCount = pageUrls.size,
					),
					currentPage = listState.firstVisibleItemIndex,
					pageCount = pageUrls.size,
					zoom = 1f,
					isIncognito = isIncognito,
					onPageSeek = null, // le défilement webtoon est continu
					onZoom = null,
					onPrevChapter = onPrevChapter,
					onNextChapter = onNextChapter,
					onClose = onClose,
				)
			}
		}
	}
}

/** Saut de page avec ou sans animation selon la préférence « Animation de page ». */
private suspend fun PagerState.snapOrAnimate(target: Int, animation: ReaderAnimation) {
	if (target !in 0 until pageCount) return
	if (animation == ReaderAnimation.NONE) scrollToPage(target) else animateScrollToPage(target)
}

/**
 * Contrôles du lecteur : panneau haut (activity_reader.xml : appbar_top avec retour + titres,
 * badge incognito) et panneau bas (layout_reader_actions.xml : chapitre précédent / curseur /
 * chapitre suivant).
 */
@Composable
private fun ReaderControls(
	visible: Boolean,
	title: String,
	chapterTitle: String,
	pageCounter: String,
	currentPage: Int,
	pageCount: Int,
	zoom: Float,
	isIncognito: Boolean,
	onPageSeek: ((Int) -> Unit)?,
	onZoom: ((Float) -> Unit)?,
	onPrevChapter: (() -> Unit)?,
	onNextChapter: (() -> Unit)?,
	onClose: () -> Unit,
) {
	if (!visible) return
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
					contentDescription = "Fermer",
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
					text = "Incognito",
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
							Icon(Icons.Filled.Remove, contentDescription = "Zoom -", tint = Color.White)
						}
						IconButton(onClick = { onZoom(ZOOM_STEP) }) {
							Icon(Icons.Filled.Add, contentDescription = "Zoom +", tint = Color.White)
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
	fillHeight: Boolean = true,
	isPagesNumbered: Boolean,
	pageNumber: Int,
	pagesCount: Int,
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
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		AsyncImage(
			model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
				.data(pageUrl)
				.build(),
			contentDescription = null,
			contentScale = contentScale,
			modifier = if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
		)
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
