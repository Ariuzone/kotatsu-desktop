/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Fenêtre principale — reproduit la mise en page « tablette/desktop » de l'app Android
 * (rail de navigation latéral + volet de contenu), cf. activity_main.xml + navigation_rail_fab.xml.
 * Les écrans (explorateur, historique, favoris…) sont branchés par le lot L4b.
 */
package org.koitharu.kotatsu.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koitharu.kotatsu.core.APP_VERSION_NAME
import org.koitharu.kotatsu.desktop.ui.nav.NavItem
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.theme.KotatsuTheme

fun main() = application {
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
				MainScreen()
			}
		}
	}
}

@Composable
private fun kotatsuIcon(): Painter = painterResource("kotatsu_256.png")

@Composable
private fun MainScreen() {
	var selected by rememberSaveable { mutableStateOf(NavItem.EXPLORE) }
	// Pile de navigation : écran source actuel + manga sélectionné (écran détails)
	var selectedManga by remember { mutableStateOf<org.koitharu.kotatsu.parsers.model.Manga?>(null) }
	var readerRequest by remember {
		mutableStateOf<Pair<org.koitharu.kotatsu.parsers.model.Manga, String>?>(null)
	}
	Box(modifier = Modifier.fillMaxSize()) {
		// Rail de navigation latéral (comme l'app d'origine en mode tablette/desktop)
		NavigationRail(modifier = Modifier.align(Alignment.CenterStart)) {
			for (item in NavItem.default) {
				NavigationRailItem(
					selected = selected == item && selectedManga == null,
					onClick = {
						selected = item
						selectedManga = null
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
		}
		// Volet de contenu
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(start = 80.dp),
			color = MaterialTheme.colorScheme.surface,
		) {
			val manga = selectedManga
			if (manga != null) {
				org.koitharu.kotatsu.desktop.ui.screens.details.DetailsScreen(
					manga = manga,
					chapters = emptyList(), // TODO: brancher les chapitres réels (fusion L3)
					isFavourite = false,
					onBackClick = { selectedManga = null },
					onChapterClick = { readerRequest = manga to "Chapter" },
					onFavouriteClick = {},
					onDownloadClick = {},
					onReadClick = { readerRequest = manga to "Chapter" },
				)
			} else {
				when (selected) {
					NavItem.EXPLORE ->
						org.koitharu.kotatsu.desktop.ui.screens.explore.ExploreScreen(
							onSourceClick = { source ->
								// TODO: catalogue de la source (fusion L3) — placeholder détail
							},
						)
					else -> PlaceholderScreen(selected)
				}
			}
		}
	}
	// Fenêtre lecteur (comme ReaderActivity d'origine : une fenêtre dédiée)
	val request = readerRequest
	if (request != null) {
		org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderContent(
			title = request.first.title,
			chapterTitle = request.second,
			pageUrls = emptyList(), // TODO: pages réelles (fusion L3)
			mode = org.koitharu.kotatsu.desktop.ui.screens.reader.ReaderMode.PAGER,
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
