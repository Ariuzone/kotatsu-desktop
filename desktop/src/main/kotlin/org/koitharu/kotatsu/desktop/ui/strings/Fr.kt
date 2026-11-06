/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — chaînes de l'UI
 * desktop en français, reprises mot pour mot de app/src/main/res/values-fr/strings.xml de
 * l'app d'origine et des captures de référence. Chaque constante cite sa clé d'origine.
 */
package org.koitharu.kotatsu.desktop.ui.strings

/** Barre de recherche des écrans principaux (string/search_manga). */
public const val FR_SEARCH_MANGA: String = "Rechercher un manga"

/** Recherche avancée (string/search_hint). */
public const val FR_SEARCH_HINT: String = "Entrez le titre du manga, le genre ou le nom de la source"

// --- Navigation basse (string/history, favourites, explore, sources, feed) ---
public const val FR_HISTORY: String = "Historique"
public const val FR_FAVOURITES: String = "Favoris"
public const val FR_EXPLORE: String = "Explorer"
public const val FR_SOURCE: String = "Source"
public const val FR_FEED: String = "Flux"

// --- États vides (string/text_history_holder_*, text_empty_holder_primary,
//     favourites_category_empty, text_feed_holder) ---
public const val FR_TEXT_HISTORY_HOLDER_PRIMARY: String = "Ce que vous lisez sera affiché ici"
public const val FR_TEXT_HISTORY_HOLDER_SECONDARY: String = "Trouver quoi lire dans la section « Explorer »"
public const val FR_TEXT_EMPTY_HOLDER_PRIMARY: String = "C'est un peu vide ici…"
public const val FR_FAVOURITES_CATEGORY_EMPTY: String = "Catégorie vide"
public const val FR_TEXT_FEED_HOLDER: String = "Les nouveaux chapitres de ce que vous lisez sont affichés ici"

// --- Favoris (string/all_favourites, read_later) ---
public const val FR_ALL_FAVOURITES: String = "Tous les favoris"
public const val FR_READ_LATER: String = "Lire plus tard"
public const val FR_HENTAI: String = "Hentai" // catégorie de l'utilisateur (capture)

// --- Menus ⋮ (string/clear_history, list_options, favourites_categories, incognito_mode,
//     settings, update, show_dismissed, source_presets, manage_sources) ---
public const val FR_CLEAR_HISTORY: String = "Effacer l'historique"
public const val FR_LIST_OPTIONS: String = "Options de liste"
public const val FR_FAVOURITES_CATEGORIES: String = "Catégories favorites"
public const val FR_INCOGNITO_MODE: String = "Mode incognito"
public const val FR_SETTINGS: String = "Paramètres"
public const val FR_UPDATE: String = "Mettre à Jour"
public const val FR_SHOW_DISMISSED: String = "Réafficher ce que j'ai écarté"
public const val FR_SOURCE_PRESETS: String = "Préréglages de sources"
public const val FR_MANAGE_SOURCES: String = "Gérer les sources"

// --- Explorer / Discover (string/for_you, top_charts, show_all, new_finds, more_for_you) ---
public const val FR_FOR_YOU: String = "Pour vous"
public const val FR_TOP: String = "Top"
public const val FR_MANGAS: String = "Mangas" // chip (capture)
public const val FR_MANHWA: String = "Manhwa" // chip (capture)
public const val FR_SHOW_ALL: String = "Tout afficher"
public const val FR_NEW_FINDS: String = "Nouveautés pour vous"
public const val FR_MORE_FOR_YOU: String = "Plus pour vous"

// --- Onglet Source (string/local_storage, bookmarks, random, downloads, suggestions, more,
//     catalog, manga_sources voir capture) ---
public const val FR_LOCAL_STORAGE: String = "Stockage local"
public const val FR_BOOKMARKS: String = "Marque-pages"
public const val FR_RANDOM: String = "Aléatoire"
public const val FR_DOWNLOADS: String = "Téléchargements"
public const val FR_SUGGESTIONS: String = "Suggestions"
public const val FR_MORE: String = "Plus"
public const val FR_MANGA_SOURCES: String = "Sources des mangas"
public const val FR_CATALOG: String = "Catalogue"

// --- Sources catalogue (string/sources_catalog, all_languages, content_type_manga,
//     content_type_hentai ; « Nouveaux » et « Marque-pages » d'après capture) ---
public const val FR_SOURCES_CATALOG: String = "Sources catalogue"
public const val FR_ALL_LANGUAGES: String = "Toutes les langues"
public const val FR_NEWEST: String = "Nouveaux"
public const val FR_MISC_LANGUAGES: String = "Langues diverses"
public const val FR_CONTENT_TYPE_MANGA: String = "Mangas"
public const val FR_CONTENT_TYPE_HENTAI: String = "Hentais"

// --- Recherche (string/clear_search_history, search_history_cleared) ---
public const val FR_CLEAR_SEARCH_HISTORY: String = "Effacer l'historique de recherche"

// --- Flux (string/update, show_updated, clear_feed ; plurals new_chapters de values-fr/plurals.xml) ---
public const val FR_SHOW_UPDATED: String = "Afficher mis à jour"
public const val FR_CLEAR_FEED: String = "Effacer le flux"

/** Pluriel « nouveau(x) chapitre(s) » (plurals.xml : new_chapters, values-fr). */
public fun frNewChapters(count: Int): String = when (count) {
	1 -> "1 nouveau chapitre"
	else -> "$count nouveaux chapitres"
}

// --- Détails du manga (string/read, chapters, chapters_empty, description, expand, collapse,
//     download, add_to_favourites, nsfw) ---
public const val FR_READ: String = "Lire"
public const val FR_CHAPTERS: String = "Chapitres"
public const val FR_CHAPTERS_EMPTY: String = "Pas de chapitres dans ce manga"
public const val FR_DESCRIPTION: String = "Description"
public const val FR_EXPAND: String = "Développer"
public const val FR_COLLAPSE: String = "Réduire"
public const val FR_DOWNLOAD: String = "Télécharger"
public const val FR_ADD_TO_FAVOURITES: String = "Ajouter aux favoris"
public const val FR_NSFW: String = "18+"

// --- Lecteur (string/prev_chapter, next_chapter, prev_page, next_page, save_page,
//     reader_settings, reader_info_pattern) ---
public const val FR_PREV_CHAPTER: String = "Chapitre précèdent"
public const val FR_NEXT_CHAPTER: String = "Chapitre suivant"
public const val FR_PREV_PAGE: String = "Page précédente"
public const val FR_NEXT_PAGE: String = "Page suivante"
public const val FR_SAVE_PAGE: String = "Sauvegarder la page"
public const val FR_READER_SETTINGS: String = "Paramètres du lecteur"

/** Barre d'infos du lecteur (string/reader_info_pattern) : « Ch. %1$d/%2$d Pg. %3$d/%4$d ». */
public fun frReaderInfo(chapter: Int, chaptersCount: Int, page: Int, pagesCount: Int): String =
	"Ch. $chapter/$chaptersCount Pg. $page/$pagesCount"
