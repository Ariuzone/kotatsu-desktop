/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Recherche multi-sources » — reproduit activity_search.xml de l'app d'origine :
 * barre de recherche en haut (hint exact « Enter manga title, genre or source name »),
 * résultats en grille (MangaGrid).
 */
package org.koitharu.kotatsu.desktop.ui.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.koitharu.kotatsu.desktop.ui.components.MangaGrid
import org.koitharu.kotatsu.parsers.model.Manga

private const val SEARCH_HINT = "Enter manga title, genre or source name"

/**
 * Recherche : déclenche [onQuery] (débounce 500 ms, cf. l'app d'origine) ; les résultats
 * arrivent via [results] et s'affichent dans la grille standard.
 */
@OptIn(FlowPreview::class)
@Composable
public fun SearchScreen(
	results: List<Manga>,
	onQuery: suspend (String) -> Unit,
	onMangaClick: (Manga) -> Unit,
	onBackClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var query by remember { mutableStateOf("") }
	val queryFlow = remember { MutableStateFlow("") }
	val scope = rememberCoroutineScope()
	val focusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
		queryFlow
			.debounce(500)
			.distinctUntilChanged()
			.collect { value -> onQuery(value) }
	}

	Column(modifier = modifier.fillMaxSize()) {
		Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
			OutlinedTextField(
				value = query,
				onValueChange = {
					query = it
					queryFlow.value = it
				},
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 8.dp, vertical = 8.dp)
					.focusRequester(focusRequester),
				placeholder = { Text(SEARCH_HINT, maxLines = 1) },
				leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
				trailingIcon = {
					if (query.isNotEmpty()) {
						IconButton(onClick = {
							query = ""
							queryFlow.value = ""
						}) {
							Icon(Icons.Filled.Close, contentDescription = "Clear")
						}
					}
				},
				singleLine = true,
			)
		}
		IconButton(onClick = onBackClick) {
			Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
		}
		MangaGrid(
			mangaList = results,
			onMangaClick = onMangaClick,
		)
	}
}
