/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Destinations du rail de navigation, identiques à core/prefs/NavItem.kt de l'app d'origine
 * (libellés = strings.xml d'origine ; icônes Material équivalentes aux drawables d'origine).
 */
package org.koitharu.kotatsu.desktop.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector

public enum class NavItem(
	public val title: String,
	public val filledIcon: ImageVector,
	public val outlinedIcon: ImageVector,
) {
	HISTORY("History", Icons.Filled.History, Icons.Outlined.History),
	FAVORITES("Favorites", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
	LOCAL("On device", Icons.Filled.Storage, Icons.Outlined.Storage),
	EXPLORE("Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
	SOURCES("Source", Icons.Filled.Source, Icons.Outlined.Source),
	FEED("Feed", Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
	BOOKMARKS("Bookmarks", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
	;

	public companion object {

		/** Destinations visibles par défaut sur le rail (P1 ; configurable dans les paramètres ensuite). */
		public val default: List<NavItem> = listOf(EXPLORE, HISTORY, FAVORITES, BOOKMARKS, LOCAL)
	}
}
