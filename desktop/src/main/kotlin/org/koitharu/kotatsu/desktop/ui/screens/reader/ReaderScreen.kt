/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Lecteur — reproduit fragment_reader_pager.xml / fragment_reader_webtoon.xml + ReaderActivity :
 * mode paginé (HorizontalPager) ou webtoon (défilement vertical continu), zones de tap
 * (précédent / contrôles / suivant) équivalentes aux gestes de l'app d'origine, navigables aussi
 * au clavier (flèches / PageUp-PageDown = adaptation desktop indispensable, cf. UI_DIVERGENCES #5),
 * overlay de contrôles (titre du chapitre, compteur de pages).
 */
package org.koitharu.kotatsu.desktop.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxHeight

public enum class ReaderMode { PAGER, WEBTOON }

@Composable
public fun ReaderContent(
	title: String,
	chapterTitle: String,
	pageUrls: List<String>,
	mode: ReaderMode,
	onClose: () -> Unit,
) {
	var controlsVisible by remember { mutableStateOf(true) }
	val scope = rememberCoroutineScope()

	when (mode) {
		ReaderMode.PAGER -> {
			val pagerState = rememberPagerState(pageCount = { pageUrls.size })
			LaunchedEffect(pagerState.currentPage) { controlsVisible = true }
			Box(
				modifier = Modifier
					.fillMaxSize()
					.onKeyEvent { event ->
						when (event.key) {
							Key.DirectionRight -> {
								scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
								true
							}
							Key.DirectionLeft -> {
								scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
								true
							}
							else -> false
						}
					}
					.pointerInput(Unit) {
						detectTapGestures(
							onTap = { position ->
								when {
									position.x < size.width / 3f -> scope.launch {
										pagerState.animateScrollToPage(pagerState.currentPage - 1)
									}
									position.x > 2f * size.width / 3f -> scope.launch {
										pagerState.animateScrollToPage(pagerState.currentPage + 1)
									}
									else -> controlsVisible = !controlsVisible
								}
							},
						)
					},
			) {
				HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
					ReaderPageImage(pageUrl = pageUrls.getOrNull(page))
				}
				ReaderControls(
					visible = controlsVisible,
					title = title,
					chapterTitle = chapterTitle,
					pageCounter = "${pagerState.currentPage + 1} / ${pageUrls.size}",
					onClose = onClose,
				)
			}
		}

		ReaderMode.WEBTOON -> {
			var counterText by remember { mutableStateOf("1 / ${pageUrls.size}") }
			Box(
				modifier = Modifier
					.fillMaxSize()
					.pointerInput(Unit) {
						detectTapGestures(onTap = { controlsVisible = !controlsVisible })
					},
			) {
				LazyColumn(modifier = Modifier.fillMaxSize()) {
					items(pageUrls, key = { it }) { pageUrl ->
						ReaderPageImage(pageUrl = pageUrl, fillHeight = false)
					}
				}
				ReaderControls(
					visible = controlsVisible,
					title = title,
					chapterTitle = chapterTitle,
					pageCounter = counterText,
					onClose = onClose,
				)
			}
		}
	}
}

@Composable
private fun ReaderPageImage(pageUrl: String?, fillHeight: Boolean = true) {
	if (pageUrl == null) return
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		AsyncImage(
			model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
				.data(pageUrl)
				.build(),
			contentDescription = null,
			contentScale = if (fillHeight) ContentScale.Fit else ContentScale.FillWidth,
			modifier = if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
		)
	}
}

@Composable
private fun ReaderControls(
	visible: Boolean,
	title: String,
	chapterTitle: String,
	pageCounter: String,
	onClose: () -> Unit,
) {
	if (!visible) return
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color.Black.copy(alpha = 0.6f))
			.padding(horizontal = 8.dp, vertical = 4.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(onClick = onClose) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = "Close",
					tint = Color.White,
				)
			}
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					color = Color.White,
					maxLines = 1,
				)
				Text(
					text = chapterTitle,
					style = MaterialTheme.typography.bodySmall,
					color = Color.White.copy(alpha = 0.7f),
					maxLines = 1,
				)
			}
			Text(
				text = pageCounter,
				style = MaterialTheme.typography.labelLarge,
				color = Color.White,
				modifier = Modifier.padding(end = 8.dp),
			)
		}
	}
}
