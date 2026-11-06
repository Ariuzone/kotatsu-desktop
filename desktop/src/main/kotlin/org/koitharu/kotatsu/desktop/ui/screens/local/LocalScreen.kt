/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — onglet « Stockage
 * local » (LocalListFragment d'origine, local/ui) : grille de mangas locaux (CBZ/CBR importés),
 * état vide Totoro (ic_empty_local, « Sauvegardez d'abord quelque chose » / « Enregistrez
 * quelque chose à partir d'un catalogue en ligne ou importez-le à partir d'un fichier. »),
 * action « Importer » (string/_import).
 *
 * Les données sont injectées : [items] (l'orchestrateur branchera LocalMangaRepository).
 */
package org.koitharu.kotatsu.desktop.ui.screens.local

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.MangaGrid
import org.koitharu.kotatsu.desktop.ui.components.TotoroIllustration
import org.koitharu.kotatsu.desktop.ui.components.TopSearchBar
import org.koitharu.kotatsu.desktop.ui.components.TopSearchMenuAction
import org.koitharu.kotatsu.desktop.ui.strings.FR_IMPORT
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_LOCAL_HOLDER_PRIMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_LOCAL_HOLDER_SECONDARY
import org.koitharu.kotatsu.parsers.model.Manga

/**
 * Onglet « Stockage local ». @param items mangas locaux détectés (CBZ/CBR, injecté).
 * @param onImportClick ouvre le sélecteur de fichiers (comme l'action « Importer » d'origine).
 */
@Composable
public fun LocalScreen(
	items: List<Manga>,
	onMangaClick: (Manga) -> Unit,
	onImportClick: () -> Unit,
	onSearchClick: () -> Unit,
	onSettingsClick: () -> Unit,
	onIncognitoToggle: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
) {
	var incognito by remember { mutableStateOf(false) }

	Column(modifier = modifier.fillMaxSize()) {
		// Barre de recherche pill + menu ⋮ (cohérent avec les autres écrans principaux)
		TopSearchBar(
			onSearchClick = onSearchClick,
			menuActions = listOf(
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
		if (items.isEmpty()) {
			// createEmptyState de LocalListViewModel (Totoro ic_empty_local + action _import)
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				EmptyStateScreen(
					illustration = TotoroIllustration.LOCAL,
					textPrimary = FR_TEXT_LOCAL_HOLDER_PRIMARY,
					textSecondary = FR_TEXT_LOCAL_HOLDER_SECONDARY,
					actionText = FR_IMPORT,
					onActionClick = onImportClick,
				)
			}
		} else {
			MangaGrid(
				mangaList = items,
				onMangaClick = onMangaClick,
			)
		}
	}
}
