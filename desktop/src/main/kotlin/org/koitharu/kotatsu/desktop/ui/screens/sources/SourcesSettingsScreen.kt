/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P3 : écran
 * racine « Sources des mangas » (SourcesSettingsFragment d'origine, pref_sources), fidèle aux
 * captures de référence (frames 29-30 et 39 de la vidéo) : top bar (←, titre, loupe), lignes
 * Ordre de tri / Afficher en vue grille / Gérer les sources / Sources catalogue / Activer toutes
 * les sources de mangas / Choisir le miroir automatiquement / Gérer les liens.
 *
 * Les réglages sont écrits dans AppSettings (mêmes clés KEY_* que l'original) ; le store desktop
 * n'est pas observable, chaque ligne garde donc son état local (cf. ReaderSettingsScreen).
 * « Choisir le miroir automatiquement » est persisté mais sans effet : le changement automatique
 * de domaine (miroirs) sera branché plus tard (backend non porté).
 */
package org.koitharu.kotatsu.desktop.ui.screens.sources

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANGA_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE_SOURCES
import org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCES_CATALOG

// --- Chaînes FR de cet écran (non présentes dans ui/strings/Fr.kt) ---

/** Titre du dialog « Ordre de tri ». */
private const val FR_SORT_ORDER = "Ordre de tri"

/** Option du dialog de tri — l'app d'origine laisse « Popular & reliable » non traduit (vidéo). */
private const val FR_SORT_ORDER_SCORE = "Popular & reliable"

/** Option du dialog de tri (alphabetical). */
private const val FR_SORT_ORDER_ALPHABETIC = "Alphabétique"

/** Ligne « Afficher en vue grille » (pref_sources d'origine). */
private const val FR_SHOW_GRID = "Afficher en vue grille"

/** Ligne « Activer toutes les sources de mangas ». */
private const val FR_ENABLE_ALL_SOURCES = "Activer toutes les sources de mangas"

/** Sous-titre de la ligne précédente (vidéo frame 45). */
private const val FR_ENABLE_ALL_SOURCES_SUMMARY =
	"Toutes les sources de mangas disponibles seront activées de façon permanente"

/** Ligne « Choisir le miroir automatiquement ». */
private const val FR_AUTO_MIRROR = "Choisir le miroir automatiquement"

/** Sous-titre de la ligne précédente (vidéo frame 45). */
private const val FR_AUTO_MIRROR_SUMMARY =
	"Changement automatique de domaine pour les sources de mangas en cas d'erreur " +
		"si des miroirs sont disponibles"

/** Ligne « Gérer les liens ». */
private const val FR_MANAGE_LINKS = "Gérer les liens"

/** Sous-titre de la ligne précédente (vidéo frames 39/45, texte tronqué dans la capture). */
private const val FR_MANAGE_LINKS_SUMMARY =
	"Gérer les liens vers des mangas à partir d'applications externes (par exemple, un navigateur web)"

/** Sous-titre « N éléments » de « Gérer les sources » (vidéo : « 361 éléments »). */
private fun frItemsCount(count: Int): String =
	if (count == 1) "1 élément" else "$count éléments"

/** Sous-titre « Disponible : N » de « Sources catalogue » (vidéo : « Disponible : 596 »). */
private fun frAvailableCount(count: Int): String = "Disponible : $count"

/** Libellé courant de l'ordre de tri (sous-titre de « Ordre de tri »). */
private fun sourcesOrderLabel(order: String): String =
	if (order == "ALPHABETIC") FR_SORT_ORDER_ALPHABETIC else FR_SORT_ORDER_SCORE

/**
 * Écran racine « Sources des mangas » (Paramètres → Sources des mangas). Reproduit
 * SourcesSettingsFragment / pref_sources de l'app d'origine.
 *
 * @param enabledCount nombre de sources activées (réservé : la vidéo n'affiche pas ce chiffre
 * sur cet écran, il alimente l'écran racine Paramètres) ;
 * @param totalCount nombre de sources ajoutées (sous-titre « N éléments ») ;
 * @param availableCount nombre de sources disponibles au catalogue (sous-titre « Disponible : N »).
 */
@Composable
public fun SourcesSettingsScreen(
	appSettings: AppSettings,
	enabledCount: Int,
	totalCount: Int,
	availableCount: Int,
	onBackClick: () -> Unit,
	onOpenManageSources: () -> Unit,
	onOpenCatalog: () -> Unit,
	onOpenLinks: () -> Unit,
) {
	// États locaux reflétés dans le store (FileKeyValueStore n'est pas observable)
	var sourcesOrder by remember { mutableStateOf(appSettings.sourcesSortOrder) }
	var showGrid by remember { mutableStateOf(appSettings.isSourcesGrid) }
	var autoMirror by remember { mutableStateOf(appSettings.isAutoMirrorEnabled) }
	var orderDialogOpen by remember { mutableStateOf(false) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_MANGA_SOURCES,
			onBackClick = onBackClick,
			// La loupe ouvre la recherche globale de l'app (non ciblée sur cet écran, comme
			// les autres écrans Paramètres) : câblée par le principal via le routage global.
			onSearchClick = {},
		)
		LazyColumn(modifier = Modifier.fillMaxSize()) {
			item {
				PreferenceRow(
					icon = Icons.AutoMirrored.Filled.Sort,
					title = FR_SORT_ORDER,
					subtitle = sourcesOrderLabel(sourcesOrder),
					onClick = { orderDialogOpen = true },
				)
			}
			item {
				PreferenceSwitchRow(
					icon = Icons.Filled.GridView,
					title = FR_SHOW_GRID,
					checked = showGrid,
					onCheckedChange = {
						showGrid = it
						appSettings.isSourcesGrid = it
					},
				)
			}
			item {
				PreferenceRow(
					icon = Icons.Filled.Source,
					title = FR_MANAGE_SOURCES,
					subtitle = frItemsCount(totalCount),
					onClick = onOpenManageSources,
				)
			}
			item {
				PreferenceRow(
					icon = Icons.Filled.TravelExplore,
					title = FR_SOURCES_CATALOG,
					subtitle = frAvailableCount(availableCount),
					onClick = onOpenCatalog,
				)
			}
			item {
				PreferenceRow(
					icon = Icons.Filled.DoneAll,
					title = FR_ENABLE_ALL_SOURCES,
					subtitle = FR_ENABLE_ALL_SOURCES_SUMMARY,
					onClick = { appSettings.hiddenSources = emptySet() },
				)
			}
			item {
				PreferenceSwitchRow(
					icon = Icons.Filled.Dns,
					title = FR_AUTO_MIRROR,
					subtitle = FR_AUTO_MIRROR_SUMMARY,
					checked = autoMirror,
					onCheckedChange = {
						autoMirror = it
						appSettings.isAutoMirrorEnabled = it
					},
				)
			}
			item {
				PreferenceRow(
					icon = Icons.Filled.Link,
					title = FR_MANAGE_LINKS,
					subtitle = FR_MANAGE_LINKS_SUMMARY,
					onClick = onOpenLinks,
				)
			}
		}
	}
	if (orderDialogOpen) {
		RadioDialog(
			title = FR_SORT_ORDER,
			options = listOf(FR_SORT_ORDER_SCORE, FR_SORT_ORDER_ALPHABETIC),
			selectedIndex = if (sourcesOrder == "ALPHABETIC") 1 else 0,
			onDismiss = { orderDialogOpen = false },
			onSelect = { index ->
				sourcesOrder = if (index == 1) "ALPHABETIC" else "SCORE"
				appSettings.sourcesSortOrder = sourcesOrder
			},
		)
	}
}
