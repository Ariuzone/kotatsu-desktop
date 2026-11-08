/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — chaînes de l'UI
 * desktop en français, reprises mot pour mot de app/src/main/res/values-fr/strings.xml de
 * l'app d'origine et des captures de référence. Chaque constante cite sa clé d'origine.
 * Modification : 2026-11-08 — phase P2 : chaînes de la section « Thème » de l'écran
 * Apparence (libellés exacts des captures frames 9-25 de la vidéo).
 * Modification : 2026-11-08 — phase P8-A : écran détails — méta enrichie (« Source », « État »,
 * « Chapitres », temps de lecture estimé, « Progression »), bouton « Lire ▾ » (menu
 * « Lire le chapitre 1 » / « Continuer · n »), dialog « Ajouter aux favoris »
 * (« Gérer » / « Terminé ») ; libellés des captures vidéo t≈6:07-6:45, 7:10-7:20, 9:25.
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

// --- Stockage local (string/text_local_holder_primary, text_local_holder_secondary, _import) ---
public const val FR_TEXT_LOCAL_HOLDER_PRIMARY: String = "Sauvegardez d'abord quelque chose"
public const val FR_TEXT_LOCAL_HOLDER_SECONDARY: String =
	"Enregistrez quelque chose à partir d'un catalogue en ligne ou importez-le à partir d'un fichier."
public const val FR_IMPORT: String = "Importer"

/** Barre d'infos du lecteur (string/reader_info_pattern) : « Ch. %1$d/%2$d Pg. %3$d/%4$d ». */
public fun frReaderInfo(chapter: Int, chaptersCount: Int, page: Int, pagesCount: Int): String =
	"Ch. $chapter/$chaptersCount Pg. $page/$pagesCount"

// --- Paramètres racine (strings des pref_*.xml de l'app d'origine + captures frames 7-8) ---
public const val FR_CANCEL: String = "Annuler" // android.R.string.cancel (values-fr)
public const val FR_OK: String = "OK" // ok
public const val FR_READING: String = "Lecture" // pref_sections_reading
public const val FR_FIND_MANGA: String = "Trouver des mangas" // pref_sections_find_manga
public const val FR_YOUR_DATA: String = "Vos données" // pref_sections_your_data
public const val FR_APPLICATION: String = "Application" // pref_sections_application
public const val FR_APPEARANCE: String = "Apparence" // appearance
public const val FR_APPEARANCE_SUMMARY: String = "Thème, Mode liste, Langue" // capture
public const val FR_READER_SETTINGS_SUMMARY: String =
	"Mode lecture, Mode mise à l'échelle, Changer de pages" // capture
public const val FR_NEW_CHAPTERS: String = "Nouveaux chapitres" // new_chapters
public const val FR_NEW_CHAPTERS_SUMMARY: String =
	"Fréquence de vérification, Rechercher les mises à jour" // capture
public const val FR_SUGGESTIONS_SUMMARY: String = "Activé" // capture
public const val FR_DOWNLOADS_STORAGE: String = "Téléchargements et stockage" // capture
public const val FR_DOWNLOADS_STORAGE_SUMMARY: String =
	"Dossier pour les téléchargements, Utilisation du stockage, Data removal" // capture
public const val FR_BACKUP_RESTORE: String = "Sauvegarde et restauration" // capture
// Chaîne non traduite dans l'app d'origine — reproduite telle quelle (parité)
public const val FR_BACKUP_RESTORE_SUMMARY: String = "Create or restore a backup, Sauvegardes"
public const val FR_NOTIFICATIONS: String = "Notifications" // notifications
public const val FR_NOTIFICATIONS_SUMMARY: String = "Nouveaux chapitres, Suggestions" // capture
public const val FR_PRIVACY_CONTENT: String = "Confidentialité et contenu" // capture
public const val FR_PRIVACY_SUMMARY: String =
	"Contenu pour adultes, Sécurité de l'application, Votre activité" // capture
public const val FR_NETWORK: String = "Réseau" // network
public const val FR_NETWORK_SUMMARY: String = "Données mobiles, Proxy, DNS sur HTTPS" // capture
public const val FR_ACCOUNTS: String = "Comptes et synchronisation" // accounts (capture vidéo 3:50)
public const val FR_ACCOUNTS_SUMMARY: String = "Services de suivi, Community, Discord" // capture
public const val FR_ABOUT: String = "À propos" // about
public const val FR_ABOUT_VERSION: String = "Version 1.0" // version
public const val FR_COMING_SOON: String = "Cette section sera disponible dans une prochaine phase"

/** Sous-titre « Sources des mangas » : « %1$d de %2$d activé(s) » (capture frame 8). */
public fun frSourcesEnabled(enabled: Int, total: Int): String = "$enabled de $total activé(s)"

// --- Paramètres du lecteur : valeurs affichées (labels values-fr de l'app d'origine) ---
public const val FR_IMAGES_MASKED: String = "Images masquées" // masked_images
public const val FR_IMAGES_MASKED_SUMMARY: String =
	"Tout ce que vous avez effacé. Retirez une image pour la revoir" // capture
public const val FR_IMAGES_MASKED_EMPTY: String =
	"Rien de masqué pour l'instant. Frottez une image du pouce pendant la lecture pour la masquer"

/** « Mode par défaut » : Standard / De droite à gauche / Vertical / Webtoon (captures §2.1). */
public fun frReaderModeLabel(mode: org.koitharu.kotatsu.core.prefs.ReaderMode): String = when (mode) {
	org.koitharu.kotatsu.core.prefs.ReaderMode.STANDARD -> "Standard"
	org.koitharu.kotatsu.core.prefs.ReaderMode.REVERSED -> "De droite à gauche"
	org.koitharu.kotatsu.core.prefs.ReaderMode.VERTICAL -> "Vertical"
	org.koitharu.kotatsu.core.prefs.ReaderMode.WEBTOON -> "Webtoon"
}

/** « Mode mise à l'échelle » (values-fr : Ajuster au centre…). */
public fun frReaderScaleLabel(mode: org.koitharu.kotatsu.core.prefs.ScaleMode): String = when (mode) {
	org.koitharu.kotatsu.core.prefs.ScaleMode.FIT_CENTER -> "Ajuster au centre"
	org.koitharu.kotatsu.core.prefs.ScaleMode.FIT_WIDTH -> "Ajuster à la largeur"
	org.koitharu.kotatsu.core.prefs.ScaleMode.FIT_HEIGHT -> "Ajuster à la hauteur"
	org.koitharu.kotatsu.core.prefs.ScaleMode.CROP -> "Recadrer"
}

/** « Animation de page » (enum ReaderAnimation : NONE / DEFAULT / ADVANCED). */
public fun frReaderAnimationLabel(animation: org.koitharu.kotatsu.core.prefs.ReaderAnimation): String =
	when (animation) {
		org.koitharu.kotatsu.core.prefs.ReaderAnimation.NONE -> "Aucune"
		org.koitharu.kotatsu.core.prefs.ReaderAnimation.DEFAULT -> "Par défaut"
		org.koitharu.kotatsu.core.prefs.ReaderAnimation.ADVANCED -> "Avancée"
	}

/** « Arrière-plan » (enum ReaderBackground). */
public fun frReaderBackgroundLabel(background: org.koitharu.kotatsu.core.prefs.ReaderBackground): String =
	when (background) {
		org.koitharu.kotatsu.core.prefs.ReaderBackground.DEFAULT -> "Par défaut"
		org.koitharu.kotatsu.core.prefs.ReaderBackground.LIGHT -> "Clair"
		org.koitharu.kotatsu.core.prefs.ReaderBackground.DARK -> "Sombre"
		org.koitharu.kotatsu.core.prefs.ReaderBackground.WHITE -> "Blanc"
		org.koitharu.kotatsu.core.prefs.ReaderBackground.BLACK -> "Noir"
	}

/** « Précharger les pages » (enum NetworkPolicy : Toujours / Uniquement en Wi-Fi / Jamais). */
public fun frNetworkPolicyLabel(policy: org.koitharu.kotatsu.core.prefs.NetworkPolicy): String =
	when (policy) {
		org.koitharu.kotatsu.core.prefs.NetworkPolicy.ALWAYS -> "Toujours"
		org.koitharu.kotatsu.core.prefs.NetworkPolicy.NON_METERED -> "Uniquement en Wi-Fi"
		org.koitharu.kotatsu.core.prefs.NetworkPolicy.NEVER -> "Jamais"
	}

// --- Phase P2 : Apparence (libellés exacts des captures frames 21-25) ---
public const val FR_LIST_SECTION: String = "Liste des mangas"
public const val FR_MANGA_PAGE_SECTION: String = "Page du manga"
public const val FR_NAVIGATION_SECTION: String = "Navigation"
public const val FR_LIST_MODE: String = "Mode liste"
public const val FR_GRID_SIZE: String = "Taille de la grille"
public const val FR_QUICK_FILTERS: String = "Afficher les filtres rapides"
public const val FR_QUICK_FILTERS_SUMMARY: String =
	"Fournit la capacité de filtrer les listes de manga par certains paramètres"
public const val FR_PROGRESS_INDICATORS: String = "Afficher les indicateurs de progression de lecture"
public const val FR_LIST_BADGES: String = "Badges dans les listes"
public const val FR_LIST_BADGES_SUMMARY: String = "Mangas sauvegardés, Favoris"
public const val FR_COLLAPSE_LONG_DESCRIPTION: String = "Réduire la description longue"
public const val FR_PAGES_THUMBNAILS: String = "Afficher les miniatures des pages"
public const val FR_PAGES_THUMBNAILS_SUMMARY: String = "Active l'onglet \"Pages\" sur l'écran de détails"
public const val FR_DEFAULT_TAB: String = "Onglet par défaut"
public const val FR_DEFAULT_TAB_SUMMARY: String = "Dernière utilisation"
public const val FR_READING_TIME: String = "Afficher le temps de lecture estimé"
public const val FR_READING_TIME_SUMMARY: String = "Le temps estimé peut être inexact"
public const val FR_RELATED_MANGA: String = "Mangas connexes"
public const val FR_RELATED_MANGA_SUMMARY: String =
	"Afficher une liste de mangas connexes. Dans certains cas, elle peut être inexacte ou manquante"
public const val FR_PIN_NAVIGATION: String = "Épingler l'interface de navigation"
public const val FR_PIN_NAVIGATION_SUMMARY: String =
	"Ne pas masquer la barre de navigation et la vue de recherche lors du défilement"
public const val FR_FLOATING_CONTINUE: String = "Afficher le bouton flottant Continuer"
public const val FR_FLOATING_CONTINUE_SUMMARY: String =
	"Permet de continuer la lecture en un clic. Ce bouton n'apparaîtra pas en mode navigation privée ou lorsque l'historique est vide"
public const val FR_EXIT_CONFIRMATION: String = "Confirmation de sortie"
public const val FR_SEARCH_SUGGESTIONS: String = "Suggestions de recherche"
public const val FR_SEARCH_SUGGESTIONS_SUMMARY: String =
	"Sources des mangas, Genres, Requêtes récentes, Auteurs, Requêtes suggérées, Sources récentes, Mangas"
public const val FR_RECENT_SHORTCUTS: String = "Afficher les raccourcis des mangas récents"
public const val FR_MAIN_SECTIONS: String = "Principales sections de l'écran"
public const val FR_ADD: String = "Ajouter"
public const val FR_LANGUAGE: String = "Langue"
public const val FR_LANGUAGE_SUMMARY: String = "Suivre le système (français seul sur desktop)"
// Dialog « Ajouter » de « Principales sections de l'écran » (capture ~2:07) :
// « Sur l'appareil, Suggestions, Mis à jour, Marque-pages »
public const val FR_ON_DEVICE: String = "Sur l'appareil" // libellé du dialog sections (Nav LOCAL)
public const val FR_UPDATES: String = "Mis à jour" // updated (dialog sections, écran P4)
// Adaptations desktop — libellés divergents, documentés dans docs/UI_DIVERGENCES.md
public const val FR_EXIT_CONFIRMATION_SUMMARY: String =
	"Demander confirmation avant de fermer la fenêtre (desktop)" // origine : « Appuyez deux fois sur la touche Retour pour quitter l'appli »
public const val FR_RECENT_SHORTCUTS_SUMMARY: String =
	"Geste long-press de l'icône Android, sans équivalent sur desktop" // origine : « Rendre les mangas récents disponibles en appuyant longuement sur l'icône de l'application »
// Section « Thème » de l'écran Apparence (captures frames 9-25)
public const val FR_THEME: String = "Thème" // pref_sections_theme
public const val FR_COLOR_SCHEME: String = "Schéma de couleurs" // color_scheme (values-fr)
public const val FR_FOLLOW_SYSTEM: String = "Suivre le système" // follow_system (values-fr)
public const val FR_LIGHT: String = "Clair" // theme_light (values-fr)
public const val FR_DARK: String = "Sombre" // theme_dark (values-fr)
public const val FR_AMOLED: String = "Noir" // dark_amoled (capture)
public const val FR_AMOLED_SUMMARY: String =
	"Utilise moins d'énergie pour les écrans AMOLED" // dark_amoled_summary (values-fr)
// Confirmation de sortie (dialog desktop, adaptation de « Appuyez deux fois sur la touche Retour »)
public const val FR_EXIT_DIALOG_TITLE: String = "Quitter Kotatsu ?"
public const val FR_EXIT_DIALOG_CONFIRM: String = "Quitter"

/** « Mode liste » : Compact / Détails / Grille (bottom sheet Options de liste, frame 0:30). */
public fun frListModeLabel(mode: org.koitharu.kotatsu.core.prefs.ListMode): String = when (mode) {
	org.koitharu.kotatsu.core.prefs.ListMode.LIST -> "Compact"
	org.koitharu.kotatsu.core.prefs.ListMode.DETAILED_LIST -> "Détails"
	org.koitharu.kotatsu.core.prefs.ListMode.GRID -> "Grille"
}

/** « Afficher les indicateurs de progression de lecture » (dialog radio, frame 1:45). */
public fun frProgressModeLabel(mode: org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode): String =
	when (mode) {
		org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode.NONE -> "Désactivé"
		org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode.PERCENT_READ -> "Pourcentage lu"
		org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode.PERCENT_LEFT -> "Pourcentage restant"
		org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode.CHAPTERS_READ -> "Chapitres lus"
		org.koitharu.kotatsu.core.prefs.ProgressIndicatorMode.CHAPTERS_LEFT -> "Chapitres restants"
	}

/** Libellés FR du rail (Principales sections de l'écran + rail principal). */
public fun frNavItemLabel(item: org.koitharu.kotatsu.desktop.ui.nav.NavItem): String = when (item) {
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.HISTORY -> FR_HISTORY
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.FAVORITES -> FR_FAVOURITES
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.EXPLORE -> FR_EXPLORE
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.SOURCES -> FR_SOURCE
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.FEED -> FR_FEED
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.LOCAL -> FR_LOCAL_STORAGE
	org.koitharu.kotatsu.desktop.ui.nav.NavItem.BOOKMARKS -> FR_BOOKMARKS
}

// --- Phase P8-A : écran détails (vidéo t≈6:07-6:45 fiche, t≈7:10-7:20 dialog favoris, t≈9:25 fiche après lecture) ---

/** « Auteur : » (string/author, values-fr). */
public const val FR_AUTHOR: String = "Auteur"

/** « État : » (string/state, values-fr ; vidéo t≈6:07 « État : En cours »). */
public const val FR_STATE: String = "État"

/** « État : » — libellés des états du manga (manga.state ; state_* de values-fr). */
public const val FR_STATE_ONGOING: String = "En cours" // state_ongoing (values-fr)
public const val FR_STATE_FINISHED: String = "Terminé" // state_finished (values-fr)
public const val FR_STATE_ABANDONED: String = "Abandonné" // state_abandoned (values-fr)
public const val FR_STATE_PAUSED: String = "En pause" // state_paused (values-fr)
public const val FR_STATE_UPCOMING: String = "À venir" // state_upcoming (values-fr)

/** Libellé FR d'un état de manga ; null (ligne masquée) si état inconnu/non traduit. */
public fun frStateLabel(state: org.koitharu.kotatsu.parsers.model.MangaState?): String? =
	when (state) {
		org.koitharu.kotatsu.parsers.model.MangaState.ONGOING -> FR_STATE_ONGOING
		org.koitharu.kotatsu.parsers.model.MangaState.FINISHED -> FR_STATE_FINISHED
		org.koitharu.kotatsu.parsers.model.MangaState.ABANDONED -> FR_STATE_ABANDONED
		org.koitharu.kotatsu.parsers.model.MangaState.PAUSED -> FR_STATE_PAUSED
		org.koitharu.kotatsu.parsers.model.MangaState.UPCOMING -> FR_STATE_UPCOMING
		null -> null
		// RESTRICTED : pas de libellé values-fr relevé → ligne « État » masquée (parité prudente)
		else -> null
	}

/** « Aucun chapitre » (string/no_chapters, values-fr ; vidéo t≈6:07 « Chapitres : Aucun chapitre »). */
public const val FR_NO_CHAPTERS: String = "Aucun chapitre"

/** « 26 chapitres » (vidéo t≈7:10 « Chapitres : 26 chapitres (1 h 26 m) ») ; singulier « 1 chapitre ». */
public fun frChaptersCount(count: Int): String = when (count) {
	1 -> "1 chapitre"
	else -> "$count chapitres"
}

/**
 * Durée au format vidéo « (1 h 26 m) » / « (26 m) » (captures t≈7:10, 9:25).
 * Formule d'estimation P8-A : 4 minutes par chapitre (constante ESTIMATED_MINUTES_PER_CHAPTER
 * de DetailsScreen), ou temps restant = chapitres restants × 4 min quand une position
 * d'historique existe. Divergence documentée : la vidéo affiche ~3,3 min/chapitre.
 */
public fun frDuration(minutes: Int): String {
	val m = minutes.coerceAtLeast(0)
	return if (m >= 60) {
		val h = m / 60
		val rem = m % 60
		if (rem == 0) "$h h" else "$h h $rem m"
	} else {
		"$m m"
	}
}

/** « Lire le chapitre 1 » (menu ▾ du bouton Lire, vidéo t≈7:15 ; pas de clé values-fr relevée). */
public const val FR_READ_FIRST_CHAPTER: String = "Lire le chapitre 1"

/** « Continuer » (string/_continue, values-fr) — CTA du bouton Lire après lecture. */
public const val FR_CONTINUE: String = "Continuer"

/** « Continuer · 2 » (vidéo t≈9:25 « Continuer · 2 ▾ »). */
public fun frContinueChapter(chapterNumber: Int): String = "$FR_CONTINUE · $chapterNumber"

/** « Chapitre 2 sur 26 » (string/chapter_d_of_d, values-fr ; vidéo t≈9:25). */
public fun frChapterXofY(current: Int, total: Int): String = "Chapitre $current sur $total"

/** « Progression : » (string/progress, values-fr ; vidéo t≈9:25 ligne « Progression : 4 % »). */
public const val FR_PROGRESS: String = "Progression"

/** « Terminé » (string/done, values-fr ; bouton de validation du dialog favoris t≈7:15). */
public const val FR_DONE: String = "Terminé"

// « Gérer » : déjà déclaré dans FrSources.kt (string/manage, values-fr) — réutilisé par
// le dialog « Ajouter aux favoris » (P8-A) sans nouvelle déclaration.
