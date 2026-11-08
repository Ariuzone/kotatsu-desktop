/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — source de chaînes des
 * écrans « sources » (Sources des mangas, Gérer les sources, Sources catalogue, Préréglages,
 * Gérer les liens, Nouveau préréglage) et des écrans principaux (Signets, Favoris, Historique,
 * Téléchargements, Protection, Lecteur, Détails), conformité règle client : toute chaîne affichée
 * vient d'une source de chaînes, jamais écrite en brut dans un écran. Chaque constante cite sa
 * provenance (écran d'origine, capture/vidéo de référence ou clé strings.xml).
 */
package org.koitharu.kotatsu.desktop.ui.strings

// --- Écran « Sources des mangas » (SourcesSettingsScreen, pref_sources, frames 29-30/39/45) ---
public const val FR_SORT_ORDER: String = "Ordre de tri" // dialog « Ordre de tri »
// Option du dialog de tri — non traduite dans l'original (vidéo : « Popular & reliable »)
public const val FR_SORT_ORDER_SCORE: String = "Popular & reliable"
public const val FR_SORT_ORDER_ALPHABETIC: String = "Alphabétique" // option alphabetical
public const val FR_SHOW_GRID: String = "Afficher en vue grille" // pref_sources d'origine
public const val FR_ENABLE_ALL_SOURCES: String = "Activer toutes les sources de mangas"
public const val FR_ENABLE_ALL_SOURCES_SUMMARY: String =
	"Toutes les sources de mangas disponibles seront activées de façon permanente" // vidéo frame 45
public const val FR_AUTO_MIRROR: String = "Choisir le miroir automatiquement"
public const val FR_AUTO_MIRROR_SUMMARY: String =
	"Changement automatique de domaine pour les sources de mangas en cas d'erreur " +
		"si des miroirs sont disponibles" // vidéo frame 45
public const val FR_MANAGE_LINKS: String = "Gérer les liens"
public const val FR_MANAGE_LINKS_SUMMARY: String =
	"Gérer les liens vers des mangas à partir d'applications externes (par exemple, un navigateur web)"
// vidéo frames 39/45, texte tronqué dans la capture

/** Sous-titre « N éléments » de « Gérer les sources » (vidéo : « 361 éléments »). */
public fun frItemsCount(count: Int): String =
	if (count == 1) "1 élément" else "$count éléments"

/** Sous-titre « Disponible : N » de « Sources catalogue » (vidéo : « Disponible : 596 »). */
public fun frAvailableCount(count: Int): String = FR_AVAILABLE_COUNT + count

// --- Écran « Gérer les sources » (ManageSourcesScreen, frames 30, 36-37) ---
public const val FR_SEARCH_SOURCES: String = "Rechercher" // placeholder du champ (frame 37)
public const val FR_SOURCE_DISABLED: String = "Désactivée" // menu ⋮ par ligne (case cochée)
public const val FR_DISABLE_EXPLICIT: String = "Désactiver le contenu explicite" // menu ⋮ global
public const val FR_DISABLE_ALL: String = "Tout désactiver" // menu ⋮ global
public const val FR_ENABLE_ALL: String = "Activer toutes les sources" // menu ⋮ global
public const val FR_CATEGORY_COMICS: String = "Comics" // sous-titre de ligne (catégorie)
public const val FR_CATEGORY_OTHER: String = "Autre" // sous-titre de ligne (catégorie)
public const val FR_NO_SOURCES_FOUND: String = "Aucune source trouvée" // état vide de recherche

// --- Écran « Sources catalogue » (SourcesCatalogScreen, phase P3) ---
public const val FR_EMPTY_SOURCES_CATALOG_TITLE: String = "Aucune source de mangas"
public const val FR_EMPTY_SOURCES_CATALOG_TEXT: String =
	"Il n'y a pas de sources disponibles dans cette section, ou tout cela aurait pu être ajouté. " +
		"Restez à l'écoute" // état vide FR exact de la vidéo
public const val FR_AVAILABLE_COUNT: String = "Disponible : " // compteur du catalogue
public const val FR_ACTION_REMOVE: String = "Retirer" // contentDescription du bouton ✓ (installée)
public const val FR_BACK: String = "Retour" // contentDescription de la flèche ← (top bars)

// --- Écran « Source » / préréglages (SourcePresetsScreen + PresetCreatorDialog, vidéo t≈5:30) ---
public const val FR_NEW_PRESET: String = "Nouveau préréglage" // action du menu ⋮ + titre du dialog
public const val FR_SAVE: String = "Enregistrer" // bouton du créateur de préréglages

// --- Écran « Gérer les liens » (ManageLinksScreen, adaptation desktop minimale) ---
public const val FR_MANAGE_LINKS_EXPLANATION: String =
	"Gérer les liens vers des mangas à partir d'applications externes (par exemple, un navigateur " +
		"web). Il se peut que vous deviez également l'activer manuellement dans les paramètres " +
		"système de l'application." // texte explicatif de la vidéo
public const val FR_MANAGE_LINKS_DESKTOP_NOTE: String =
	"Sur la version de bureau, les liens profonds (deep links) d'applications externes ne sont " +
		"pas pris en charge : ouvrez directement un lien de manga dans Kotatsu-Redo. Les domaines " +
		"miroir, s'il en existe, seront listés ici en lecture seule." // note spécifique desktop

// --- Écran « Signets » (BookmarksScreen, values-fr text_bookmark_holder_*) ---
public const val FR_BOOKMARKS_EMPTY_PRIMARY: String = "Aucun marque-page"
public const val FR_BOOKMARKS_EMPTY_SECONDARY: String =
	"Vous pouvez créer un marque-page pendant la lecture d'un manga"

/** Libellé « Page N » sous chaque signet (N = index de page + 1). */
public fun frBookmarkPage(page: Int): String = "Page ${page + 1}"

// --- Écran « Favoris » (FavouritesScreen, string manage) ---
public const val FR_MANAGE: String = "Gérer" // action « Gérer » des catégories

// --- Écran « Historique » (HistoryScreen) ---
public const val FR_CONTINUE_READING_CD: String = "Continuer la lecture" // action « lire la suite »
public const val FR_UNKNOWN: String = "Inconnu" // string unknown (values-fr)

// --- Écran « Téléchargements » (DownloadsScreen) — chaînes non traduites dans l'original ---
public const val FR_DOWNLOADS_TITLE: String = "Downloads" // string downloads (non traduit)
public const val FR_STATUS_DOWNLOADING: String = "Downloading…" // manga_downloading_ (non traduit)
public const val FR_STATUS_QUEUED: String = "Queued" // queued (non traduit)
public const val FR_STATUS_PAUSED: String = "Paused" // paused (non traduit)
public const val FR_STATUS_COMPLETE: String = "Downloaded" // download_complete (non traduit)
public const val FR_STATUS_ERROR: String = "An error occurred" // error_occurred (non traduit)
public const val FR_STATUS_CANCELED: String = "Canceled" // canceled (non traduit)
public const val FR_STATUS_STUCK: String = "Stuck" // stuck (non traduit)
public const val FR_ACTION_PAUSE: String = "Pause" // pause (non traduit)
public const val FR_ACTION_RESUME: String = "Resume" // resume (non traduit)
public const val FR_ACTION_RETRY: String = "Retry" // retry (non traduit)
public const val FR_ACTION_SKIP: String = "Skip" // skip (non traduit)
public const val FR_ACTION_SKIP_ALL: String = "Skip all" // skip_all (non traduit)
public const val FR_ACTION_CANCEL: String = "Cancel" // android.R.string.cancel (non traduit)
public const val FR_ACTION_CANCEL_ALL: String = "Cancel all" // cancel_all (non traduit)
public const val FR_ACTION_REMOVE_COMPLETED: String = "Remove completed" // remove_completed (non traduit)
public const val FR_DOWNLOADS_EMPTY_PRIMARY: String = "You don't have any downloads" // text_downloads_list_holder (non traduit)
public const val FR_STATUS_UNKNOWN: String = "Unknown" // unknown (non traduit)
public const val FR_CD_BACK: String = "Back" // contentDescription ← (non traduit)
public const val FR_CD_MORE_OPTIONS: String = "More options" // contentDescription ⋮ (non traduit)

// --- Écran de protection (ProtectScreen, res/values/strings.xml d'origine, non traduits) ---
public const val FR_PROTECT_TITLE: String = "Kotatsu" // app_name
public const val FR_PROTECT_SUBTITLE: String = "Enter password" // enter_password (non traduit)
public const val FR_PROTECT_WRONG_PASSWORD: String = "Wrong password" // wrong_password (non traduit)
public const val FR_PROTECT_NEXT: String = "Next" // next (non traduit)
public const val FR_PROTECT_CANCEL: String = "Cancel" // android.R.string.cancel (non traduit)

// --- Lecteur (ReaderScreen, overlay incognito + zoom) ---
public const val FR_CLOSE: String = "Fermer" // contentDescription ← du panneau haut
public const val FR_INCOGNITO: String = "Incognito" // badge du panneau haut (mode incognito)
public const val FR_ZOOM_OUT: String = "Zoom -" // contentDescription bouton zoom arrière
public const val FR_ZOOM_IN: String = "Zoom +" // contentDescription bouton zoom avant
