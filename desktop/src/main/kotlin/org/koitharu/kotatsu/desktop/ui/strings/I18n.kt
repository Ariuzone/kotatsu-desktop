/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — version 1.1.1 :
 * i18n de l'interface. Les chaînes FR_* sont des `var` (plus des const) et ce fichier
 * applique la langue choisie (Français / English) au démarrage et au changement :
 * la langue par défaut est celle du système Windows, repli anglais.
 * La traduction anglaise de certaines chaînes longues reste à affiner (documenté).
 */
package org.koitharu.kotatsu.desktop.ui.strings

/** Langues d'interface disponibles (français par défaut de ce portage, anglais). */
public enum class UiLang { FR, EN }

/** Traductions anglaises, indexées par nom de constante. */
private val EN_STRINGS: Map<String, String> = mapOf(
	"FR_SEARCH_MANGA" to "Search a manga",
	"FR_SEARCH_HINT" to "Enter the manga title, a genre or the source name",
	"FR_HISTORY" to "History",
	"FR_FAVOURITES" to "Favourites",
	"FR_EXPLORE" to "Explore",
	"FR_SOURCE" to "Source",
	"FR_FEED" to "Feed",
	"FR_TEXT_HISTORY_HOLDER_PRIMARY" to "What you read will be shown here",
	"FR_TEXT_HISTORY_HOLDER_SECONDARY" to "Find something to read in the « Explore » section",
	"FR_TEXT_EMPTY_HOLDER_PRIMARY" to "It's a bit empty here…",
	"FR_FAVOURITES_CATEGORY_EMPTY" to "Empty category",
	"FR_TEXT_FEED_HOLDER" to "New chapters of what you read are shown here",
	"FR_ALL_FAVOURITES" to "All favourites",
	"FR_READ_LATER" to "Read later",
	"FR_HENTAI" to "Hentai",
	"FR_CLEAR_HISTORY" to "Clear history",
	"FR_LIST_OPTIONS" to "List options",
	"FR_FAVOURITES_CATEGORIES" to "Favourite categories",
	"FR_INCOGNITO_MODE" to "Incognito mode",
	"FR_SETTINGS" to "Settings",
	"FR_UPDATE" to "Update",
	"FR_SHOW_DISMISSED" to "Show what I dismissed again",
	"FR_SOURCE_PRESETS" to "Source presets",
	"FR_MANAGE_SOURCES" to "Manage sources",
	"FR_FOR_YOU" to "For you",
	"FR_TOP" to "Top",
	"FR_MANGAS" to "Mangas",
	"FR_MANHWA" to "Manhwa",
	"FR_SHOW_ALL" to "Show all",
	"FR_NEW_FINDS" to "New for you",
	"FR_MORE_FOR_YOU" to "More for you",
	"FR_LOCAL_STORAGE" to "Local storage",
	"FR_BOOKMARKS" to "Bookmarks",
	"FR_RANDOM" to "Random",
	"FR_DOWNLOADS" to "Downloads",
	"FR_SUGGESTIONS" to "Suggestions",
	"FR_MORE" to "More",
	"FR_MANGA_SOURCES" to "Manga sources",
	"FR_CATALOG" to "Catalog",
	"FR_SOURCES_CATALOG" to "Sources catalog",
	"FR_ALL_LANGUAGES" to "All languages",
	"FR_NEWEST" to "Newest",
	"FR_MISC_LANGUAGES" to "Other languages",
	"FR_CONTENT_TYPE_MANGA" to "Mangas",
	"FR_CONTENT_TYPE_HENTAI" to "Hentais",
	"FR_CLEAR_SEARCH_HISTORY" to "Clear search history",
	"FR_SHOW_UPDATED" to "Show updated",
	"FR_CLEAR_FEED" to "Clear feed",
	"FR_READ" to "Read",
	"FR_CHAPTERS" to "Chapters",
	"FR_CHAPTERS_EMPTY" to "No chapters in this manga",
	"FR_DESCRIPTION" to "Description",
	"FR_EXPAND" to "Expand",
	"FR_COLLAPSE" to "Collapse",
	"FR_DOWNLOAD" to "Download",
	"FR_ADD_TO_FAVOURITES" to "Add to favourites",
	"FR_NSFW" to "18+",
	"FR_PREV_CHAPTER" to "Previous chapter",
	"FR_NEXT_CHAPTER" to "Next chapter",
	"FR_PREV_PAGE" to "Previous page",
	"FR_NEXT_PAGE" to "Next page",
	"FR_SAVE_PAGE" to "Save page",
	"FR_READER_SETTINGS" to "Reader settings",
	"FR_TEXT_LOCAL_HOLDER_PRIMARY" to "Save something first",
	"FR_IMPORT" to "Import",
	"FR_CANCEL" to "Cancel",
	"FR_OK" to "OK",
	"FR_READING" to "Reading",
	"FR_FIND_MANGA" to "Find mangas",
	"FR_YOUR_DATA" to "Your data",
	"FR_APPLICATION" to "Application",
	"FR_APPEARANCE" to "Appearance",
	"FR_APPEARANCE_SUMMARY" to "Theme, List mode, Language",
	"FR_NEW_CHAPTERS" to "New chapters",
	"FR_SUGGESTIONS_SUMMARY" to "Enabled",
	"FR_DOWNLOADS_STORAGE" to "Downloads and storage",
	"FR_BACKUP_RESTORE" to "Backup and restore",
	"FR_BACKUP_RESTORE_SUMMARY" to "Create or restore a backup, Backups",
	"FR_NOTIFICATIONS" to "Notifications",
	"FR_NOTIFICATIONS_SUMMARY" to "New chapters, Suggestions",
	"FR_PRIVACY_CONTENT" to "Privacy and content",
	"FR_NETWORK" to "Network",
	"FR_NETWORK_SUMMARY" to "Mobile data, Proxy, DNS over HTTPS",
	"FR_ACCOUNTS" to "Accounts and sync",
	"FR_ACCOUNTS_SUMMARY" to "Tracking services, Community, Discord",
	"FR_ABOUT" to "About",
	"FR_ABOUT_VERSION" to "Version 1.1.1",
	"FR_COMING_SOON" to "This section will be available in a next phase",
	"FR_IMAGES_MASKED" to "Hidden images",
	"FR_LIST_SECTION" to "Manga list",
	"FR_MANGA_PAGE_SECTION" to "Manga page",
	"FR_NAVIGATION_SECTION" to "Navigation",
	"FR_LIST_MODE" to "List mode",
	"FR_GRID_SIZE" to "Grid size",
	"FR_QUICK_FILTERS" to "Show quick filters",
	"FR_PROGRESS_INDICATORS" to "Show reading progress indicators",
	"FR_LIST_BADGES" to "Badges in lists",
	"FR_LIST_BADGES_SUMMARY" to "Saved mangas, Favourites",
	"FR_COLLAPSE_LONG_DESCRIPTION" to "Collapse long description",
	"FR_PAGES_THUMBNAILS" to "Show page thumbnails",
	"FR_PAGES_THUMBNAILS_SUMMARY" to "Enables the \"Pages\" tab on the details screen",
	"FR_DEFAULT_TAB" to "Default tab",
	"FR_DEFAULT_TAB_SUMMARY" to "Last used",
	"FR_READING_TIME" to "Show estimated reading time",
	"FR_READING_TIME_SUMMARY" to "The estimated time may be inaccurate",
	"FR_RELATED_MANGA" to "Related mangas",
	"FR_PIN_NAVIGATION" to "Pin navigation",
	"FR_FLOATING_CONTINUE" to "Show the floating Continue button",
	"FR_EXIT_CONFIRMATION" to "Exit confirmation",
	"FR_SEARCH_SUGGESTIONS" to "Search suggestions",
	"FR_RECENT_SHORTCUTS" to "Show recent manga shortcuts",
	"FR_MAIN_SECTIONS" to "Main screen sections",
	"FR_ADD" to "Add",
	"FR_LANGUAGE" to "Language",
	"FR_LANGUAGE_SUMMARY" to "Follow the system (English if unavailable)",
	"FR_ON_DEVICE" to "On device",
	"FR_UPDATES" to "Updated",
	"FR_THEME" to "Theme",
	"FR_COLOR_SCHEME" to "Color scheme",
	"FR_FOLLOW_SYSTEM" to "Follow the system",
	"FR_LIGHT" to "Light",
	"FR_DARK" to "Dark",
	"FR_AMOLED" to "Black",
	"FR_EXIT_DIALOG_TITLE" to "Quit Kotatsu?",
	"FR_EXIT_DIALOG_CONFIRM" to "Quit",
	"FR_AUTHOR" to "Author",
	"FR_STATE" to "Status",
	"FR_STATE_ONGOING" to "Ongoing",
	"FR_STATE_FINISHED" to "Completed",
	"FR_STATE_ABANDONED" to "Cancelled",
	"FR_STATE_PAUSED" to "On hiatus",
	"FR_STATE_UPCOMING" to "Upcoming",
	"FR_NO_CHAPTERS" to "No chapters",
	"FR_READ_FIRST_CHAPTER" to "Read chapter 1",
	"FR_CONTINUE" to "Continue",
	"FR_PROGRESS" to "Progress",
	"FR_DONE" to "Completed",
	"FR_SORT_ORDER" to "Sort order",
	"FR_SORT_ORDER_SCORE" to "Popular & reliable",
	"FR_SORT_ORDER_ALPHABETIC" to "Alphabetical",
	"FR_SHOW_GRID" to "Show as grid",
	"FR_ENABLE_ALL_SOURCES" to "Enable all manga sources",
	"FR_AUTO_MIRROR" to "Choose mirror automatically",
	"FR_MANAGE_LINKS" to "Manage links",
	"FR_SEARCH_SOURCES" to "Search",
	"FR_SOURCE_DISABLED" to "Disabled",
	"FR_DISABLE_EXPLICIT" to "Disable explicit content",
	"FR_DISABLE_ALL" to "Disable all",
	"FR_ENABLE_ALL" to "Enable all sources",
	"FR_CATEGORY_COMICS" to "Comics",
	"FR_CATEGORY_OTHER" to "Other",
	"FR_NO_SOURCES_FOUND" to "No sources found",
	"FR_EMPTY_SOURCES_CATALOG_TITLE" to "No manga sources",
	"FR_AVAILABLE_COUNT" to "Available: ",
	"FR_ACTION_REMOVE" to "Remove",
	"FR_BACK" to "Back",
	"FR_NEW_PRESET" to "New preset",
	"FR_SAVE" to "Save",
	"FR_BOOKMARKS_EMPTY_PRIMARY" to "No bookmarks",
	"FR_MANAGE" to "Manage",
	"FR_CONTINUE_READING_CD" to "Continue reading",
	"FR_UNKNOWN" to "Unknown",
	"FR_DOWNLOADS_TITLE" to "Downloads",
	"FR_STATUS_DOWNLOADING" to "Downloading…",
	"FR_STATUS_QUEUED" to "Queued",
	"FR_STATUS_PAUSED" to "Paused",
	"FR_STATUS_COMPLETE" to "Downloaded",
	"FR_STATUS_ERROR" to "An error occurred",
	"FR_STATUS_CANCELED" to "Canceled",
	"FR_STATUS_STUCK" to "Stuck",
	"FR_ACTION_PAUSE" to "Pause",
	"FR_ACTION_RESUME" to "Resume",
	"FR_ACTION_RETRY" to "Retry",
	"FR_ACTION_SKIP" to "Skip",
	"FR_ACTION_SKIP_ALL" to "Skip all",
	"FR_ACTION_CANCEL" to "Cancel",
	"FR_ACTION_CANCEL_ALL" to "Cancel all",
	"FR_ACTION_REMOVE_COMPLETED" to "Remove completed",
	"FR_DOWNLOADS_EMPTY_PRIMARY" to "You don't have any downloads",
	"FR_STATUS_UNKNOWN" to "Unknown",
	"FR_CD_BACK" to "Back",
	"FR_CD_MORE_OPTIONS" to "More options",
	"FR_PROTECT_TITLE" to "Kotatsu",
	"FR_PROTECT_SUBTITLE" to "Enter password",
	"FR_PROTECT_WRONG_PASSWORD" to "Wrong password",
	"FR_PROTECT_NEXT" to "Next",
	"FR_PROTECT_CANCEL" to "Cancel",
	"FR_CLOSE" to "Close",
	"FR_INCOGNITO" to "Incognito",
	"FR_ZOOM_OUT" to "Zoom -",
	"FR_ZOOM_IN" to "Zoom +",
	"FR_SORT_AND_FILTER" to "Sort and filter",
	"FR_GENRES" to "Genres",
	"FR_SORT_POPULARITY" to "Popular",
	"FR_SORT_RATING" to "Top rated",
	"FR_RETRY" to "Retry",
	"FR_APP_NAME" to "Kotatsu Desktop",
	"FR_COPIED" to "Copied to clipboard",
	"FR_LICENSE" to "License: GNU GPL v3",
	"FR_ORIGIN_PROJECT" to "Original project: Kotatsu / Kotatsu-Redo",
	"FR_LINK_KOTATSU_REDO" to "Kotatsu-Redo (GitHub)",
	"FR_LINK_KOTATSU" to "Kotatsu (GitHub)",
	"FR_SOURCES_PARSERS" to "Sources: kotatsu-parsers",
	"FR_BACKUP_BANNER" to "Create or restore a backup",
	"FR_CREATE_BACKUP" to "Create a backup",
	"FR_RESTORE_BACKUP" to "Restore a backup",
	"FR_CHOOSE_SAVE_TITLE" to "Save backup",
	"FR_CHOOSE_OPEN_TITLE" to "Choose a backup",
	"FR_ZIP_FILTER_DESCRIPTION" to "ZIP archives",
	"FR_RESTORE_WARNING_TITLE" to "Restore a backup",
	"FR_RESTORE_CONFIRM" to "Continue",
	"FR_BACKUP_CREATED_TITLE" to "Backup created",
	"FR_BACKUP_CREATED" to "Backup created:\n%s",
	"FR_BACKUP_FAILED" to "Backup failed: %s",
	"FR_RESTORE_INVALID" to "Invalid backup format",
	"FR_RESTORE_FAILED" to "Restore failed: %s",
	"FR_DB_REQUIRED" to "kotatsu.db",
	"FR_SETTINGS_FILE" to "settings.json",
	"FR_DOWNLOADS_DIR" to "Downloads folder",
	"FR_STORAGE_USAGE" to "Storage usage",
	"FR_DATA_REMOVAL" to "Data removal",
	"FR_DATA_REMOVAL_SUMMARY" to "Clear unnecessary data",
	"FR_BACKUPS" to "Backups",
	"FR_OPEN_DIR" to "Open folder",
	"FR_DIR_CHANGED_TITLE" to "Folder changed",
	"FR_OPEN_DIR_FAILED" to "Cannot open folder",
	"FR_SAME_DIR_MESSAGE" to "The selected folder is already the current one.",
	"FR_ELLIPSIS" to "…",
	"FR_DATA_REMOVAL_TITLE" to "Data removal",
	"FR_CLEAR_READ_HISTORY" to "Clear reading history",
	"FR_CLEAR_SUGGESTIONS" to "Clear suggestions",
	"FR_CLEAR_NETWORK_CACHE" to "Clear network cache",
	"FR_CLEAR_COOKIES" to "Clear cookies",
	"FR_CONFIRM_TITLE" to "Are you sure?",
	"FR_DONE_HISTORY" to "Reading history cleared",
	"FR_DONE_SUGGESTIONS" to "Suggestions cleared",
	"FR_DONE_NETWORK_CACHE" to "Network cache cleared",
	"FR_DONE_COOKIES" to "Cookies cleared",
	"FR_ERROR" to "An error occurred: %s",
	"FR_DELETE" to "Delete",
	"FR_TRACKER_SWITCH" to "Check for new chapters and notify",
	"FR_FREQUENCY" to "Check frequency",
	"FR_CHECK_UPDATES" to "Check for updates",
	"FR_DOWNLOAD_NEW" to "Download new chapters",
	"FR_SECTION_TROUBLESHOOTING" to "Troubleshooting",
	"FR_FEED_LOG" to "New chapters check log",
	"FR_BATTERY_OPTIMIZATION" to "Disable battery optimization",
	"FR_FREQUENCY_DEFAULT" to "Default",
	"FR_FREQUENCY_HOURLY" to "Hourly",
	"FR_FREQUENCY_EVERY6H" to "Every 6 hours",
	"FR_FREQUENCY_DAILY" to "Daily",
	"FR_DOWNLOAD_DISABLED" to "Never",
	"FR_DOWNLOAD_DOWNLOADED" to "Manga with downloaded chapters",
	"FR_DOWNLOAD_RECENTLY_READ" to "Recently read manga",
	"FR_FEED_LOG_EMPTY_TITLE" to "No check recorded yet",
	"FR_AT" to " at ",
	"FR_TIME_PATTERN" to "HH:mm",
	"FR_AGO_JUST_NOW" to "Just now",
	"FR_AGO_MINUTES" to "%d minutes ago",
	"FR_AGO_HOURS" to "%d hours ago",
	"FR_AGO_DAYS" to "%d days ago",
	"FR_SECTION_METERED" to "Mobile data",
	"FR_DOWNLOAD_METERED" to "Download on mobile data",
	"FR_SUGGESTIONS_WIFI_ONLY" to "Suggestions on Wi-Fi only",
	"FR_TRACKER_WIFI_ONLY" to "New chapters check on Wi-Fi only",
	"FR_SECTION_PROXY" to "Proxy",
	"FR_PROXY_TYPE" to "Proxy type",
	"FR_PROXY_ADDRESS" to "Address",
	"FR_PROXY_PORT" to "Port",
	"FR_PROXY_LOGIN" to "Username",
	"FR_PROXY_PASSWORD" to "Password",
	"FR_PROXY_EMPTY" to "—",
	"FR_PROXY_PASSWORD_MASK" to "••••••••",
	"FR_SECTION_DOH" to "DNS over HTTPS",
	"FR_DOH" to "DNS over HTTPS server",
	"FR_DOH_DISABLED" to "Disabled",
	"FR_PROXY_HOST_HINT" to "Host or IP address",
	"FR_PROXY_PORT_HINT" to "0 - 65535",
	"FR_PROXY_LOGIN_HINT" to "Username",
	"FR_PROXY_PASSWORD_HINT" to "Password",
	"FR_TRISTATE_ENABLED" to "Always",
	"FR_TRISTATE_ASK" to "Ask",
	"FR_TRISTATE_DISABLED" to "Never",
	"FR_PROXY_TYPE_DIRECT" to "Direct",
	"FR_PROXY_TYPE_HTTP" to "HTTP",
	"FR_PROXY_TYPE_SOCKS" to "SOCKS",
	"FR_DOH_GOOGLE" to "Google",
	"FR_DOH_CLOUDFLARE" to "Cloudflare",
	"FR_DOH_ADGUARD" to "AdGuard",
	"FR_DOH_ZERO_MS" to "0ms",
	"FR_TRACKER_NO_NSFW" to "Don't notify for adult mangas",
	"FR_TRACKER_FREQUENCY" to "New chapters — set the frequency",
	"FR_OPEN_FEED_SETTINGS" to "New chapters settings",
	"FR_SUGGESTIONS_NOTIFICATIONS" to "Notify about new suggestions",
	"FR_DATABASE" to "Database",
	"FR_NETWORK_CACHE" to "Network cache",
	"FR_TOTAL" to "Total",
	"FR_COMPUTING" to "Computing…",
	"FR_SUGGESTIONS_ENABLE" to "Enable suggestions",
	"FR_SUGGESTIONS_INCLUDE_DISABLED" to "Include disabled sources",
	"FR_SUGGESTIONS_EXCLUDE_GENRES" to "Exclude genres",
	"FR_SUGGESTIONS_GENRE_HINT" to "Genre name",
	"FR_READER_SECTION_PAGES" to "Pages",
	"FR_READER_DEFAULT_MODE" to "Default mode",
	"FR_READER_MODE_DETECT" to "Automatic reader mode detection",
	"FR_READER_SCALE" to "Scale mode",
	"FR_READER_ANIMATION" to "Page animation",
	"FR_READER_CROP_PAGES" to "Crop pages",
	"FR_READER_CROP_DISABLED" to "Disabled (not available on desktop)",
	"FR_READER_BACKGROUND" to "Background",
	"FR_READER_PAGES_NUMBERED" to "Numbered pages",
	"FR_READER_SECTION_WEBTOON" to "Webtoon",
	"FR_READER_WEBTOON_SEPARATORS" to "Separators in webtoon mode",
	"FR_READER_WEBTOON_ZOOM" to "Webtoon zoom",
	"FR_READER_SECTION_ADVANCED" to "Advanced",
	"FR_READER_PRELOAD" to "Preload pages",
	"FR_PRIVACY_SECTION_SECURITY" to "Application security",
	"FR_PRIVACY_SECTION_ACTIVITY" to "Your activity",
	"FR_ADULT_CONTENT" to "Adult content",
	"FR_ADULT_CONTENT_SUMMARY" to "Hide adult manga sources",
	"FR_SCREENSHOTS_POLICY" to "Screenshots policy",
	"FR_SCREENSHOTS_ALLOW" to "Allow",
	"FR_SCREENSHOTS_BLOCK_NSFW" to "Hide adult content",
	"FR_SCREENSHOTS_BLOCK_INCOGNITO" to "Hide content in incognito mode",
	"FR_SCREENSHOTS_BLOCK_ALL" to "Forbid",
	"FR_READING_STATS" to "Reading statistics",
	"FR_READING_STATS_SUMMARY" to "Collect statistics about your reading",
	"FR_INCOGNITO_TABS_HINT" to "Toggle from the ⋮ menu of each tab",
	"FR_INCOGNITO_NSFW" to "Incognito mode for adult content",
	"FR_PROTECT_APP" to "Protect the application",
	"FR_PROTECT_APP_ENABLED" to "Enabled",
	"FR_PROTECT_APP_DISABLED" to "Disabled",
	"FR_SHARE_SOURCE_RELIABILITY" to "Share source reliability",
	"FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE" to "Not available in the desktop port",
	"FR_SUGGESTIONS_18_PLUS" to "18+ suggestions",
	"FR_DISCORD_HIDE_NSFW" to "Hide NSFW content on Discord",
	"FR_COLOR_CORRECTION" to "Color correction",
	"FR_BEFORE" to "Before",
	"FR_AFTER" to "After",
	"FR_INVERT_COLORS" to "Invert colors",
	"FR_GRAYSCALE" to "Grayscale",
	"FR_BRIGHTNESS" to "Brightness",
	"FR_CONTRAST" to "Contrast",
	"FR_YELLOWISH_BACKGROUND" to "Yellowish background",
	"FR_RESET" to "Reset",
	"FR_COLOR_PREVIEW_LABEL" to "Sample page",
	"FR_AUTO_SCROLL" to "Auto-scroll",
	"FR_ENABLE" to "Enable",
	"FR_SPEED" to "Speed",
	"FR_AUTO_SCROLL_FAB_NOTE" to "Scrolling via the floating action button",
	"FR_AUTO_SCROLL_PLAY" to "Start auto-scrolling",
	"FR_AUTO_SCROLL_PAUSE" to "Pause auto-scrolling",
	"FR_ERASER_TUTORIAL_TITLE" to "Erase what you don't want to see",
	"FR_SKIP" to "Skip",
	"FR_ACCOUNTS_TITLE" to "Accounts and sync",
	"FR_TRACKERS_HEADER" to "Tracking services",
	"FR_DISABLED" to "Disabled",
	"FR_SERVICE_ANILIST" to "AniList",
	"FR_SERVICE_KITSU" to "Kitsu",
	"FR_SERVICE_MAL" to "MyAnimeList",
	"FR_SERVICE_SHIKIMORI" to "Shikimori",
	"FR_COMMUNITY_ROW" to "Community: Comments, ratings and source reliability",
	"FR_DISCORD_ROW" to "Discord Rich Presence",
)

/** Valeurs françaises d'origine (généré depuis les chaînes du portage). */
private val FR_STRINGS: Map<String, String> = mapOf(
	"FR_SEARCH_MANGA" to "Rechercher un manga",
	"FR_SEARCH_HINT" to "Entrez le titre du manga, le genre ou le nom de la source",
	"FR_HISTORY" to "Historique",
	"FR_FAVOURITES" to "Favoris",
	"FR_EXPLORE" to "Explorer",
	"FR_SOURCE" to "Source",
	"FR_FEED" to "Flux",
	"FR_TEXT_HISTORY_HOLDER_PRIMARY" to "Ce que vous lisez sera affiché ici",
	"FR_TEXT_HISTORY_HOLDER_SECONDARY" to "Trouver quoi lire dans la section « Explorer »",
	"FR_TEXT_EMPTY_HOLDER_PRIMARY" to "C'est un peu vide ici…",
	"FR_FAVOURITES_CATEGORY_EMPTY" to "Catégorie vide",
	"FR_TEXT_FEED_HOLDER" to "Les nouveaux chapitres de ce que vous lisez sont affichés ici",
	"FR_ALL_FAVOURITES" to "Tous les favoris",
	"FR_READ_LATER" to "Lire plus tard",
	"FR_HENTAI" to "Hentai",
	"FR_CLEAR_HISTORY" to "Effacer l'historique",
	"FR_LIST_OPTIONS" to "Options de liste",
	"FR_FAVOURITES_CATEGORIES" to "Catégories favorites",
	"FR_INCOGNITO_MODE" to "Mode incognito",
	"FR_SETTINGS" to "Paramètres",
	"FR_UPDATE" to "Mettre à Jour",
	"FR_SHOW_DISMISSED" to "Réafficher ce que j'ai écarté",
	"FR_SOURCE_PRESETS" to "Préréglages de sources",
	"FR_MANAGE_SOURCES" to "Gérer les sources",
	"FR_FOR_YOU" to "Pour vous",
	"FR_TOP" to "Top",
	"FR_MANGAS" to "Mangas",
	"FR_MANHWA" to "Manhwa",
	"FR_SHOW_ALL" to "Tout afficher",
	"FR_NEW_FINDS" to "Nouveautés pour vous",
	"FR_MORE_FOR_YOU" to "Plus pour vous",
	"FR_LOCAL_STORAGE" to "Stockage local",
	"FR_BOOKMARKS" to "Marque-pages",
	"FR_RANDOM" to "Aléatoire",
	"FR_DOWNLOADS" to "Téléchargements",
	"FR_SUGGESTIONS" to "Suggestions",
	"FR_MORE" to "Plus",
	"FR_MANGA_SOURCES" to "Sources des mangas",
	"FR_CATALOG" to "Catalogue",
	"FR_SOURCES_CATALOG" to "Sources catalogue",
	"FR_ALL_LANGUAGES" to "Toutes les langues",
	"FR_NEWEST" to "Nouveaux",
	"FR_MISC_LANGUAGES" to "Langues diverses",
	"FR_CONTENT_TYPE_MANGA" to "Mangas",
	"FR_CONTENT_TYPE_HENTAI" to "Hentais",
	"FR_CLEAR_SEARCH_HISTORY" to "Effacer l'historique de recherche",
	"FR_SHOW_UPDATED" to "Afficher mis à jour",
	"FR_CLEAR_FEED" to "Effacer le flux",
	"FR_READ" to "Lire",
	"FR_CHAPTERS" to "Chapitres",
	"FR_CHAPTERS_EMPTY" to "Pas de chapitres dans ce manga",
	"FR_DESCRIPTION" to "Description",
	"FR_EXPAND" to "Développer",
	"FR_COLLAPSE" to "Réduire",
	"FR_DOWNLOAD" to "Télécharger",
	"FR_ADD_TO_FAVOURITES" to "Ajouter aux favoris",
	"FR_NSFW" to "18+",
	"FR_PREV_CHAPTER" to "Chapitre précèdent",
	"FR_NEXT_CHAPTER" to "Chapitre suivant",
	"FR_PREV_PAGE" to "Page précédente",
	"FR_NEXT_PAGE" to "Page suivante",
	"FR_SAVE_PAGE" to "Sauvegarder la page",
	"FR_READER_SETTINGS" to "Paramètres du lecteur",
	"FR_TEXT_LOCAL_HOLDER_PRIMARY" to "Sauvegardez d'abord quelque chose",
	"FR_IMPORT" to "Importer",
	"FR_CANCEL" to "Annuler",
	"FR_OK" to "OK",
	"FR_READING" to "Lecture",
	"FR_FIND_MANGA" to "Trouver des mangas",
	"FR_YOUR_DATA" to "Vos données",
	"FR_APPLICATION" to "Application",
	"FR_APPEARANCE" to "Apparence",
	"FR_APPEARANCE_SUMMARY" to "Thème, Mode liste, Langue",
	"FR_NEW_CHAPTERS" to "Nouveaux chapitres",
	"FR_SUGGESTIONS_SUMMARY" to "Activé",
	"FR_DOWNLOADS_STORAGE" to "Téléchargements et stockage",
	"FR_BACKUP_RESTORE" to "Sauvegarde et restauration",
	"FR_BACKUP_RESTORE_SUMMARY" to "Create or restore a backup, Sauvegardes",
	"FR_NOTIFICATIONS" to "Notifications",
	"FR_NOTIFICATIONS_SUMMARY" to "Nouveaux chapitres, Suggestions",
	"FR_PRIVACY_CONTENT" to "Confidentialité et contenu",
	"FR_NETWORK" to "Réseau",
	"FR_NETWORK_SUMMARY" to "Données mobiles, Proxy, DNS sur HTTPS",
	"FR_ACCOUNTS" to "Comptes et synchronisation",
	"FR_ACCOUNTS_SUMMARY" to "Services de suivi, Community, Discord",
	"FR_ABOUT" to "À propos",
	"FR_ABOUT_VERSION" to "Version 1.1.1",
	"FR_COMING_SOON" to "Cette section sera disponible dans une prochaine phase",
	"FR_IMAGES_MASKED" to "Images masquées",
	"FR_LIST_SECTION" to "Liste des mangas",
	"FR_MANGA_PAGE_SECTION" to "Page du manga",
	"FR_NAVIGATION_SECTION" to "Navigation",
	"FR_LIST_MODE" to "Mode liste",
	"FR_GRID_SIZE" to "Taille de la grille",
	"FR_QUICK_FILTERS" to "Afficher les filtres rapides",
	"FR_PROGRESS_INDICATORS" to "Afficher les indicateurs de progression de lecture",
	"FR_LIST_BADGES" to "Badges dans les listes",
	"FR_LIST_BADGES_SUMMARY" to "Mangas sauvegardés, Favoris",
	"FR_COLLAPSE_LONG_DESCRIPTION" to "Réduire la description longue",
	"FR_PAGES_THUMBNAILS" to "Afficher les miniatures des pages",
	"FR_PAGES_THUMBNAILS_SUMMARY" to "Active l'onglet \\\"Pages\\\" sur l'écran de détails",
	"FR_DEFAULT_TAB" to "Onglet par défaut",
	"FR_DEFAULT_TAB_SUMMARY" to "Dernière utilisation",
	"FR_READING_TIME" to "Afficher le temps de lecture estimé",
	"FR_READING_TIME_SUMMARY" to "Le temps estimé peut être inexact",
	"FR_RELATED_MANGA" to "Mangas connexes",
	"FR_PIN_NAVIGATION" to "Épingler l'interface de navigation",
	"FR_FLOATING_CONTINUE" to "Afficher le bouton flottant Continuer",
	"FR_EXIT_CONFIRMATION" to "Confirmation de sortie",
	"FR_SEARCH_SUGGESTIONS" to "Suggestions de recherche",
	"FR_RECENT_SHORTCUTS" to "Afficher les raccourcis des mangas récents",
	"FR_MAIN_SECTIONS" to "Principales sections de l'écran",
	"FR_ADD" to "Ajouter",
	"FR_LANGUAGE" to "Langue",
	"FR_LANGUAGE_SUMMARY" to "Suivre le système (français seul sur desktop)",
	"FR_ON_DEVICE" to "Sur l'appareil",
	"FR_UPDATES" to "Mis à jour",
	"FR_THEME" to "Thème",
	"FR_COLOR_SCHEME" to "Schéma de couleurs",
	"FR_FOLLOW_SYSTEM" to "Suivre le système",
	"FR_LIGHT" to "Clair",
	"FR_DARK" to "Sombre",
	"FR_AMOLED" to "Noir",
	"FR_EXIT_DIALOG_TITLE" to "Quitter Kotatsu ?",
	"FR_EXIT_DIALOG_CONFIRM" to "Quitter",
	"FR_AUTHOR" to "Auteur",
	"FR_STATE" to "État",
	"FR_STATE_ONGOING" to "En cours",
	"FR_STATE_FINISHED" to "Terminé",
	"FR_STATE_ABANDONED" to "Abandonné",
	"FR_STATE_PAUSED" to "En pause",
	"FR_STATE_UPCOMING" to "À venir",
	"FR_NO_CHAPTERS" to "Aucun chapitre",
	"FR_READ_FIRST_CHAPTER" to "Lire le chapitre 1",
	"FR_CONTINUE" to "Continuer",
	"FR_PROGRESS" to "Progression",
	"FR_DONE" to "Terminé",
	"FR_SORT_ORDER" to "Ordre de tri",
	"FR_SORT_ORDER_SCORE" to "Popular & reliable",
	"FR_SORT_ORDER_ALPHABETIC" to "Alphabétique",
	"FR_SHOW_GRID" to "Afficher en vue grille",
	"FR_ENABLE_ALL_SOURCES" to "Activer toutes les sources de mangas",
	"FR_AUTO_MIRROR" to "Choisir le miroir automatiquement",
	"FR_MANAGE_LINKS" to "Gérer les liens",
	"FR_SEARCH_SOURCES" to "Rechercher",
	"FR_SOURCE_DISABLED" to "Désactivée",
	"FR_DISABLE_EXPLICIT" to "Désactiver le contenu explicite",
	"FR_DISABLE_ALL" to "Tout désactiver",
	"FR_ENABLE_ALL" to "Activer toutes les sources",
	"FR_CATEGORY_COMICS" to "Comics",
	"FR_CATEGORY_OTHER" to "Autre",
	"FR_NO_SOURCES_FOUND" to "Aucune source trouvée",
	"FR_EMPTY_SOURCES_CATALOG_TITLE" to "Aucune source de mangas",
	"FR_AVAILABLE_COUNT" to "Disponible : ",
	"FR_ACTION_REMOVE" to "Retirer",
	"FR_BACK" to "Retour",
	"FR_NEW_PRESET" to "Nouveau préréglage",
	"FR_SAVE" to "Enregistrer",
	"FR_BOOKMARKS_EMPTY_PRIMARY" to "Aucun marque-page",
	"FR_MANAGE" to "Gérer",
	"FR_CONTINUE_READING_CD" to "Continuer la lecture",
	"FR_UNKNOWN" to "Inconnu",
	"FR_DOWNLOADS_TITLE" to "Downloads",
	"FR_STATUS_DOWNLOADING" to "Downloading…",
	"FR_STATUS_QUEUED" to "Queued",
	"FR_STATUS_PAUSED" to "Paused",
	"FR_STATUS_COMPLETE" to "Downloaded",
	"FR_STATUS_ERROR" to "An error occurred",
	"FR_STATUS_CANCELED" to "Canceled",
	"FR_STATUS_STUCK" to "Stuck",
	"FR_ACTION_PAUSE" to "Pause",
	"FR_ACTION_RESUME" to "Resume",
	"FR_ACTION_RETRY" to "Retry",
	"FR_ACTION_SKIP" to "Skip",
	"FR_ACTION_SKIP_ALL" to "Skip all",
	"FR_ACTION_CANCEL" to "Cancel",
	"FR_ACTION_CANCEL_ALL" to "Cancel all",
	"FR_ACTION_REMOVE_COMPLETED" to "Remove completed",
	"FR_DOWNLOADS_EMPTY_PRIMARY" to "You don't have any downloads",
	"FR_STATUS_UNKNOWN" to "Unknown",
	"FR_CD_BACK" to "Back",
	"FR_CD_MORE_OPTIONS" to "More options",
	"FR_PROTECT_TITLE" to "Kotatsu",
	"FR_PROTECT_SUBTITLE" to "Enter password",
	"FR_PROTECT_WRONG_PASSWORD" to "Wrong password",
	"FR_PROTECT_NEXT" to "Next",
	"FR_PROTECT_CANCEL" to "Cancel",
	"FR_CLOSE" to "Fermer",
	"FR_INCOGNITO" to "Incognito",
	"FR_ZOOM_OUT" to "Zoom -",
	"FR_ZOOM_IN" to "Zoom +",
	"FR_SORT_AND_FILTER" to "Trier et filtrer",
	"FR_GENRES" to "Genres",
	"FR_SORT_POPULARITY" to "Populaires",
	"FR_SORT_RATING" to "Mieux notés",
	"FR_RETRY" to "Réessayer",
	"FR_APP_NAME" to "Kotatsu Desktop",
	"FR_COPIED" to "Copié dans le presse-papiers",
	"FR_LICENSE" to "Licence : GNU GPL v3",
	"FR_ORIGIN_PROJECT" to "Projet d'origine : Kotatsu / Kotatsu-Redo",
	"FR_LINK_KOTATSU_REDO" to "Kotatsu-Redo (GitHub)",
	"FR_LINK_KOTATSU" to "Kotatsu (GitHub)",
	"FR_SOURCES_PARSERS" to "Sources : kotatsu-parsers",
	"FR_BACKUP_BANNER" to "Create or restore a backup",
	"FR_CREATE_BACKUP" to "Créer une sauvegarde",
	"FR_RESTORE_BACKUP" to "Restaurer une sauvegarde",
	"FR_CHOOSE_SAVE_TITLE" to "Enregistrer la sauvegarde",
	"FR_CHOOSE_OPEN_TITLE" to "Choisir une sauvegarde",
	"FR_ZIP_FILTER_DESCRIPTION" to "Archives ZIP",
	"FR_RESTORE_WARNING_TITLE" to "Restaurer une sauvegarde",
	"FR_RESTORE_CONFIRM" to "Continuer",
	"FR_BACKUP_CREATED_TITLE" to "Sauvegarde créée",
	"FR_BACKUP_CREATED" to "Sauvegarde créée :\\n%s",
	"FR_BACKUP_FAILED" to "Échec de la sauvegarde : %s",
	"FR_RESTORE_INVALID" to "Format de sauvegarde invalide",
	"FR_RESTORE_FAILED" to "Échec de la restauration : %s",
	"FR_DB_REQUIRED" to "kotatsu.db",
	"FR_SETTINGS_FILE" to "settings.json",
	"FR_DOWNLOADS_DIR" to "Dossier pour les téléchargements",
	"FR_STORAGE_USAGE" to "Utilisation du stockage",
	"FR_DATA_REMOVAL" to "Data removal",
	"FR_DATA_REMOVAL_SUMMARY" to "Effacer les données inutiles",
	"FR_BACKUPS" to "Sauvegardes",
	"FR_OPEN_DIR" to "Ouvrir le dossier",
	"FR_DIR_CHANGED_TITLE" to "Dossier modifié",
	"FR_OPEN_DIR_FAILED" to "Impossible d'ouvrir le dossier",
	"FR_SAME_DIR_MESSAGE" to "Le dossier sélectionné est déjà le dossier actuel.",
	"FR_ELLIPSIS" to "…",
	"FR_DATA_REMOVAL_TITLE" to "Effacement des données",
	"FR_CLEAR_READ_HISTORY" to "Effacer l'historique de lecture",
	"FR_CLEAR_SUGGESTIONS" to "Effacer les suggestions",
	"FR_CLEAR_NETWORK_CACHE" to "Vider le cache réseau",
	"FR_CLEAR_COOKIES" to "Effacer les cookies",
	"FR_CONFIRM_TITLE" to "Êtes-vous sûr ?",
	"FR_DONE_HISTORY" to "Historique de lecture effacé",
	"FR_DONE_SUGGESTIONS" to "Suggestions effacées",
	"FR_DONE_NETWORK_CACHE" to "Cache réseau vidé",
	"FR_DONE_COOKIES" to "Cookies effacés",
	"FR_ERROR" to "Une erreur est survenue : %s",
	"FR_DELETE" to "Effacer",
	"FR_TRACKER_SWITCH" to "Vérifier les nouveaux chapitres et les notifier",
	"FR_FREQUENCY" to "Fréquence de vérification",
	"FR_CHECK_UPDATES" to "Rechercher les mises à jour",
	"FR_DOWNLOAD_NEW" to "Télécharger de nouveaux chapitres",
	"FR_SECTION_TROUBLESHOOTING" to "Dépannage",
	"FR_FEED_LOG" to "Vérification du journal des nouveaux chapitres",
	"FR_BATTERY_OPTIMIZATION" to "Désactiver l'optimisation de la batterie",
	"FR_FREQUENCY_DEFAULT" to "Par défaut",
	"FR_FREQUENCY_HOURLY" to "Toutes les heures",
	"FR_FREQUENCY_EVERY6H" to "Toutes les 6 heures",
	"FR_FREQUENCY_DAILY" to "Quotidienne",
	"FR_DOWNLOAD_DISABLED" to "Jamais",
	"FR_DOWNLOAD_DOWNLOADED" to "Manga avec chapitres téléchargés",
	"FR_DOWNLOAD_RECENTLY_READ" to "Manga lu récemment",
	"FR_FEED_LOG_EMPTY_TITLE" to "Aucune vérification enregistrée",
	"FR_AT" to " à ",
	"FR_TIME_PATTERN" to "HH:mm",
	"FR_AGO_JUST_NOW" to "À l'instant",
	"FR_AGO_MINUTES" to "Il y a %d minutes",
	"FR_AGO_HOURS" to "Il y a %d heures",
	"FR_AGO_DAYS" to "Il y a %d jours",
	"FR_SECTION_METERED" to "Données mobiles",
	"FR_DOWNLOAD_METERED" to "Télécharger en données mobiles",
	"FR_SUGGESTIONS_WIFI_ONLY" to "Suggestions en Wi-Fi seulement",
	"FR_TRACKER_WIFI_ONLY" to "Vérification des nouveaux chapitres en Wi-Fi seulement",
	"FR_SECTION_PROXY" to "Proxy",
	"FR_PROXY_TYPE" to "Type de proxy",
	"FR_PROXY_ADDRESS" to "Adresse",
	"FR_PROXY_PORT" to "Port",
	"FR_PROXY_LOGIN" to "Identifiant",
	"FR_PROXY_PASSWORD" to "Mot de passe",
	"FR_PROXY_EMPTY" to "—",
	"FR_PROXY_PASSWORD_MASK" to "••••••••",
	"FR_SECTION_DOH" to "DNS sur HTTPS",
	"FR_DOH" to "Serveur DNS sur HTTPS",
	"FR_DOH_DISABLED" to "Désactivé",
	"FR_PROXY_HOST_HINT" to "Hôte ou adresse IP",
	"FR_PROXY_PORT_HINT" to "0 - 65535",
	"FR_PROXY_LOGIN_HINT" to "Identifiant",
	"FR_PROXY_PASSWORD_HINT" to "Mot de passe",
	"FR_TRISTATE_ENABLED" to "Toujours",
	"FR_TRISTATE_ASK" to "Demander",
	"FR_TRISTATE_DISABLED" to "Jamais",
	"FR_PROXY_TYPE_DIRECT" to "Direct",
	"FR_PROXY_TYPE_HTTP" to "HTTP",
	"FR_PROXY_TYPE_SOCKS" to "SOCKS",
	"FR_DOH_GOOGLE" to "Google",
	"FR_DOH_CLOUDFLARE" to "Cloudflare",
	"FR_DOH_ADGUARD" to "AdGuard",
	"FR_DOH_ZERO_MS" to "0ms",
	"FR_TRACKER_NO_NSFW" to "Ne pas notifier pour les mangas pour adultes",
	"FR_TRACKER_FREQUENCY" to "Nouveaux chapitres — régler la fréquence",
	"FR_OPEN_FEED_SETTINGS" to "Paramètres des nouveaux chapitres",
	"FR_SUGGESTIONS_NOTIFICATIONS" to "Notifier les nouvelles suggestions",
	"FR_DATABASE" to "Base de données",
	"FR_NETWORK_CACHE" to "Cache réseau",
	"FR_TOTAL" to "Total",
	"FR_COMPUTING" to "Calcul…",
	"FR_SUGGESTIONS_ENABLE" to "Activer les suggestions",
	"FR_SUGGESTIONS_INCLUDE_DISABLED" to "Inclure les sources désactivées",
	"FR_SUGGESTIONS_EXCLUDE_GENRES" to "Exclure des genres",
	"FR_SUGGESTIONS_GENRE_HINT" to "Nom du genre",
	"FR_READER_SECTION_PAGES" to "Pages",
	"FR_READER_DEFAULT_MODE" to "Mode par défaut",
	"FR_READER_MODE_DETECT" to "Mode de détection automatique du lecteur",
	"FR_READER_SCALE" to "Mode mise à l'échelle",
	"FR_READER_ANIMATION" to "Animation de page",
	"FR_READER_CROP_PAGES" to "Recadrer les pages",
	"FR_READER_CROP_DISABLED" to "Désactivé (non disponible sur desktop)",
	"FR_READER_BACKGROUND" to "Arrière-plan",
	"FR_READER_PAGES_NUMBERED" to "Pages numérotées",
	"FR_READER_SECTION_WEBTOON" to "Webtoon",
	"FR_READER_WEBTOON_SEPARATORS" to "Séparateurs en mode webtoon",
	"FR_READER_WEBTOON_ZOOM" to "Zoom Webtoon",
	"FR_READER_SECTION_ADVANCED" to "Avancé",
	"FR_READER_PRELOAD" to "Précharger les pages",
	"FR_PRIVACY_SECTION_SECURITY" to "Sécurité de l'application",
	"FR_PRIVACY_SECTION_ACTIVITY" to "Votre activité",
	"FR_ADULT_CONTENT" to "Contenu pour adultes",
	"FR_ADULT_CONTENT_SUMMARY" to "Masquer les sources de mangas pour adultes",
	"FR_SCREENSHOTS_POLICY" to "Politique relative aux captures d'écran",
	"FR_SCREENSHOTS_ALLOW" to "Autoriser",
	"FR_SCREENSHOTS_BLOCK_NSFW" to "Masquer le contenu pour adultes",
	"FR_SCREENSHOTS_BLOCK_INCOGNITO" to "Masquer le contenu en mode incognito",
	"FR_SCREENSHOTS_BLOCK_ALL" to "Interdire",
	"FR_READING_STATS" to "Statistiques de lecture",
	"FR_READING_STATS_SUMMARY" to "Collecter des statistiques sur ta lecture",
	"FR_INCOGNITO_TABS_HINT" to "Activer/désactiver depuis le menu ⋮ de chaque onglet",
	"FR_INCOGNITO_NSFW" to "Mode incognito pour le contenu pour adultes",
	"FR_PROTECT_APP" to "Protéger l'application",
	"FR_PROTECT_APP_ENABLED" to "Activé",
	"FR_PROTECT_APP_DISABLED" to "Désactivé",
	"FR_SHARE_SOURCE_RELIABILITY" to "Partager la fiabilité des sources",
	"FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE" to "Non disponible dans le portage desktop",
	"FR_SUGGESTIONS_18_PLUS" to "Suggestions 18+",
	"FR_DISCORD_HIDE_NSFW" to "Masquer le contenu NSFW sur Discord",
	"FR_COLOR_CORRECTION" to "Correction des couleurs",
	"FR_BEFORE" to "Avant",
	"FR_AFTER" to "Après",
	"FR_INVERT_COLORS" to "Inverser les couleurs",
	"FR_GRAYSCALE" to "Niveaux de gris",
	"FR_BRIGHTNESS" to "Luminosité",
	"FR_CONTRAST" to "Contraste",
	"FR_YELLOWISH_BACKGROUND" to "Fond jaunâtre",
	"FR_RESET" to "Réinitialiser",
	"FR_COLOR_PREVIEW_LABEL" to "Exemple de page",
	"FR_AUTO_SCROLL" to "Défilement automatique",
	"FR_ENABLE" to "Activer",
	"FR_SPEED" to "Vitesse",
	"FR_AUTO_SCROLL_FAB_NOTE" to "Défilement via le bouton de commande flottant",
	"FR_AUTO_SCROLL_PLAY" to "Lancer le défilement automatique",
	"FR_AUTO_SCROLL_PAUSE" to "Mettre en pause le défilement automatique",
	"FR_ERASER_TUTORIAL_TITLE" to "Effacez ce que vous ne voulez plus voir",
	"FR_SKIP" to "Passer",
	"FR_ACCOUNTS_TITLE" to "Comptes et synchronisation",
	"FR_TRACKERS_HEADER" to "Services de suivi",
	"FR_DISABLED" to "Désactivé",
	"FR_SERVICE_ANILIST" to "AniList",
	"FR_SERVICE_KITSU" to "Kitsu",
	"FR_SERVICE_MAL" to "MyAnimeList",
	"FR_SERVICE_SHIKIMORI" to "Shikimori",
	"FR_COMMUNITY_ROW" to "Community : Comments, ratings and source reliability",
	"FR_DISCORD_ROW" to "Discord Rich Presence",
)

/** Applique la langue à toutes les chaînes de l'interface (FR = valeurs d'origine). */
public fun applyUiLanguage(lang: UiLang) {
	val target = if (lang == UiLang.EN) EN_STRINGS else FR_STRINGS
	ALL_STRING_NAMES.forEach { name ->
		val value = target[name] ?: return@forEach
		setStringByName(name, value)
	}
}

/** Liste de toutes les chaînes (pour la restauration FR via FR_SNAPSHOT dynamique). */
private fun setStringByName(name: String, value: String) {
	when (name) {
		"FR_SEARCH_MANGA" -> FR_SEARCH_MANGA = value
		"FR_SEARCH_HINT" -> FR_SEARCH_HINT = value
		"FR_HISTORY" -> FR_HISTORY = value
		"FR_FAVOURITES" -> FR_FAVOURITES = value
		"FR_EXPLORE" -> FR_EXPLORE = value
		"FR_SOURCE" -> FR_SOURCE = value
		"FR_FEED" -> FR_FEED = value
		"FR_TEXT_HISTORY_HOLDER_PRIMARY" -> FR_TEXT_HISTORY_HOLDER_PRIMARY = value
		"FR_TEXT_HISTORY_HOLDER_SECONDARY" -> FR_TEXT_HISTORY_HOLDER_SECONDARY = value
		"FR_TEXT_EMPTY_HOLDER_PRIMARY" -> FR_TEXT_EMPTY_HOLDER_PRIMARY = value
		"FR_FAVOURITES_CATEGORY_EMPTY" -> FR_FAVOURITES_CATEGORY_EMPTY = value
		"FR_TEXT_FEED_HOLDER" -> FR_TEXT_FEED_HOLDER = value
		"FR_ALL_FAVOURITES" -> FR_ALL_FAVOURITES = value
		"FR_READ_LATER" -> FR_READ_LATER = value
		"FR_HENTAI" -> FR_HENTAI = value
		"FR_CLEAR_HISTORY" -> FR_CLEAR_HISTORY = value
		"FR_LIST_OPTIONS" -> FR_LIST_OPTIONS = value
		"FR_FAVOURITES_CATEGORIES" -> FR_FAVOURITES_CATEGORIES = value
		"FR_INCOGNITO_MODE" -> FR_INCOGNITO_MODE = value
		"FR_SETTINGS" -> FR_SETTINGS = value
		"FR_UPDATE" -> FR_UPDATE = value
		"FR_SHOW_DISMISSED" -> FR_SHOW_DISMISSED = value
		"FR_SOURCE_PRESETS" -> FR_SOURCE_PRESETS = value
		"FR_MANAGE_SOURCES" -> FR_MANAGE_SOURCES = value
		"FR_FOR_YOU" -> FR_FOR_YOU = value
		"FR_TOP" -> FR_TOP = value
		"FR_MANGAS" -> FR_MANGAS = value
		"FR_MANHWA" -> FR_MANHWA = value
		"FR_SHOW_ALL" -> FR_SHOW_ALL = value
		"FR_NEW_FINDS" -> FR_NEW_FINDS = value
		"FR_MORE_FOR_YOU" -> FR_MORE_FOR_YOU = value
		"FR_LOCAL_STORAGE" -> FR_LOCAL_STORAGE = value
		"FR_BOOKMARKS" -> FR_BOOKMARKS = value
		"FR_RANDOM" -> FR_RANDOM = value
		"FR_DOWNLOADS" -> FR_DOWNLOADS = value
		"FR_SUGGESTIONS" -> FR_SUGGESTIONS = value
		"FR_MORE" -> FR_MORE = value
		"FR_MANGA_SOURCES" -> FR_MANGA_SOURCES = value
		"FR_CATALOG" -> FR_CATALOG = value
		"FR_SOURCES_CATALOG" -> FR_SOURCES_CATALOG = value
		"FR_ALL_LANGUAGES" -> FR_ALL_LANGUAGES = value
		"FR_NEWEST" -> FR_NEWEST = value
		"FR_MISC_LANGUAGES" -> FR_MISC_LANGUAGES = value
		"FR_CONTENT_TYPE_MANGA" -> FR_CONTENT_TYPE_MANGA = value
		"FR_CONTENT_TYPE_HENTAI" -> FR_CONTENT_TYPE_HENTAI = value
		"FR_CLEAR_SEARCH_HISTORY" -> FR_CLEAR_SEARCH_HISTORY = value
		"FR_SHOW_UPDATED" -> FR_SHOW_UPDATED = value
		"FR_CLEAR_FEED" -> FR_CLEAR_FEED = value
		"FR_READ" -> FR_READ = value
		"FR_CHAPTERS" -> FR_CHAPTERS = value
		"FR_CHAPTERS_EMPTY" -> FR_CHAPTERS_EMPTY = value
		"FR_DESCRIPTION" -> FR_DESCRIPTION = value
		"FR_EXPAND" -> FR_EXPAND = value
		"FR_COLLAPSE" -> FR_COLLAPSE = value
		"FR_DOWNLOAD" -> FR_DOWNLOAD = value
		"FR_ADD_TO_FAVOURITES" -> FR_ADD_TO_FAVOURITES = value
		"FR_NSFW" -> FR_NSFW = value
		"FR_PREV_CHAPTER" -> FR_PREV_CHAPTER = value
		"FR_NEXT_CHAPTER" -> FR_NEXT_CHAPTER = value
		"FR_PREV_PAGE" -> FR_PREV_PAGE = value
		"FR_NEXT_PAGE" -> FR_NEXT_PAGE = value
		"FR_SAVE_PAGE" -> FR_SAVE_PAGE = value
		"FR_READER_SETTINGS" -> FR_READER_SETTINGS = value
		"FR_TEXT_LOCAL_HOLDER_PRIMARY" -> FR_TEXT_LOCAL_HOLDER_PRIMARY = value
		"FR_IMPORT" -> FR_IMPORT = value
		"FR_CANCEL" -> FR_CANCEL = value
		"FR_OK" -> FR_OK = value
		"FR_READING" -> FR_READING = value
		"FR_FIND_MANGA" -> FR_FIND_MANGA = value
		"FR_YOUR_DATA" -> FR_YOUR_DATA = value
		"FR_APPLICATION" -> FR_APPLICATION = value
		"FR_APPEARANCE" -> FR_APPEARANCE = value
		"FR_APPEARANCE_SUMMARY" -> FR_APPEARANCE_SUMMARY = value
		"FR_NEW_CHAPTERS" -> FR_NEW_CHAPTERS = value
		"FR_SUGGESTIONS_SUMMARY" -> FR_SUGGESTIONS_SUMMARY = value
		"FR_DOWNLOADS_STORAGE" -> FR_DOWNLOADS_STORAGE = value
		"FR_BACKUP_RESTORE" -> FR_BACKUP_RESTORE = value
		"FR_BACKUP_RESTORE_SUMMARY" -> FR_BACKUP_RESTORE_SUMMARY = value
		"FR_NOTIFICATIONS" -> FR_NOTIFICATIONS = value
		"FR_NOTIFICATIONS_SUMMARY" -> FR_NOTIFICATIONS_SUMMARY = value
		"FR_PRIVACY_CONTENT" -> FR_PRIVACY_CONTENT = value
		"FR_NETWORK" -> FR_NETWORK = value
		"FR_NETWORK_SUMMARY" -> FR_NETWORK_SUMMARY = value
		"FR_ACCOUNTS" -> FR_ACCOUNTS = value
		"FR_ACCOUNTS_SUMMARY" -> FR_ACCOUNTS_SUMMARY = value
		"FR_ABOUT" -> FR_ABOUT = value
		"FR_ABOUT_VERSION" -> FR_ABOUT_VERSION = value
		"FR_COMING_SOON" -> FR_COMING_SOON = value
		"FR_IMAGES_MASKED" -> FR_IMAGES_MASKED = value
		"FR_LIST_SECTION" -> FR_LIST_SECTION = value
		"FR_MANGA_PAGE_SECTION" -> FR_MANGA_PAGE_SECTION = value
		"FR_NAVIGATION_SECTION" -> FR_NAVIGATION_SECTION = value
		"FR_LIST_MODE" -> FR_LIST_MODE = value
		"FR_GRID_SIZE" -> FR_GRID_SIZE = value
		"FR_QUICK_FILTERS" -> FR_QUICK_FILTERS = value
		"FR_PROGRESS_INDICATORS" -> FR_PROGRESS_INDICATORS = value
		"FR_LIST_BADGES" -> FR_LIST_BADGES = value
		"FR_LIST_BADGES_SUMMARY" -> FR_LIST_BADGES_SUMMARY = value
		"FR_COLLAPSE_LONG_DESCRIPTION" -> FR_COLLAPSE_LONG_DESCRIPTION = value
		"FR_PAGES_THUMBNAILS" -> FR_PAGES_THUMBNAILS = value
		"FR_PAGES_THUMBNAILS_SUMMARY" -> FR_PAGES_THUMBNAILS_SUMMARY = value
		"FR_DEFAULT_TAB" -> FR_DEFAULT_TAB = value
		"FR_DEFAULT_TAB_SUMMARY" -> FR_DEFAULT_TAB_SUMMARY = value
		"FR_READING_TIME" -> FR_READING_TIME = value
		"FR_READING_TIME_SUMMARY" -> FR_READING_TIME_SUMMARY = value
		"FR_RELATED_MANGA" -> FR_RELATED_MANGA = value
		"FR_PIN_NAVIGATION" -> FR_PIN_NAVIGATION = value
		"FR_FLOATING_CONTINUE" -> FR_FLOATING_CONTINUE = value
		"FR_EXIT_CONFIRMATION" -> FR_EXIT_CONFIRMATION = value
		"FR_SEARCH_SUGGESTIONS" -> FR_SEARCH_SUGGESTIONS = value
		"FR_RECENT_SHORTCUTS" -> FR_RECENT_SHORTCUTS = value
		"FR_MAIN_SECTIONS" -> FR_MAIN_SECTIONS = value
		"FR_ADD" -> FR_ADD = value
		"FR_LANGUAGE" -> FR_LANGUAGE = value
		"FR_LANGUAGE_SUMMARY" -> FR_LANGUAGE_SUMMARY = value
		"FR_ON_DEVICE" -> FR_ON_DEVICE = value
		"FR_UPDATES" -> FR_UPDATES = value
		"FR_THEME" -> FR_THEME = value
		"FR_COLOR_SCHEME" -> FR_COLOR_SCHEME = value
		"FR_FOLLOW_SYSTEM" -> FR_FOLLOW_SYSTEM = value
		"FR_LIGHT" -> FR_LIGHT = value
		"FR_DARK" -> FR_DARK = value
		"FR_AMOLED" -> FR_AMOLED = value
		"FR_EXIT_DIALOG_TITLE" -> FR_EXIT_DIALOG_TITLE = value
		"FR_EXIT_DIALOG_CONFIRM" -> FR_EXIT_DIALOG_CONFIRM = value
		"FR_AUTHOR" -> FR_AUTHOR = value
		"FR_STATE" -> FR_STATE = value
		"FR_STATE_ONGOING" -> FR_STATE_ONGOING = value
		"FR_STATE_FINISHED" -> FR_STATE_FINISHED = value
		"FR_STATE_ABANDONED" -> FR_STATE_ABANDONED = value
		"FR_STATE_PAUSED" -> FR_STATE_PAUSED = value
		"FR_STATE_UPCOMING" -> FR_STATE_UPCOMING = value
		"FR_NO_CHAPTERS" -> FR_NO_CHAPTERS = value
		"FR_READ_FIRST_CHAPTER" -> FR_READ_FIRST_CHAPTER = value
		"FR_CONTINUE" -> FR_CONTINUE = value
		"FR_PROGRESS" -> FR_PROGRESS = value
		"FR_DONE" -> FR_DONE = value
		"FR_SORT_ORDER" -> FR_SORT_ORDER = value
		"FR_SORT_ORDER_SCORE" -> FR_SORT_ORDER_SCORE = value
		"FR_SORT_ORDER_ALPHABETIC" -> FR_SORT_ORDER_ALPHABETIC = value
		"FR_SHOW_GRID" -> FR_SHOW_GRID = value
		"FR_ENABLE_ALL_SOURCES" -> FR_ENABLE_ALL_SOURCES = value
		"FR_AUTO_MIRROR" -> FR_AUTO_MIRROR = value
		"FR_MANAGE_LINKS" -> FR_MANAGE_LINKS = value
		"FR_SEARCH_SOURCES" -> FR_SEARCH_SOURCES = value
		"FR_SOURCE_DISABLED" -> FR_SOURCE_DISABLED = value
		"FR_DISABLE_EXPLICIT" -> FR_DISABLE_EXPLICIT = value
		"FR_DISABLE_ALL" -> FR_DISABLE_ALL = value
		"FR_ENABLE_ALL" -> FR_ENABLE_ALL = value
		"FR_CATEGORY_COMICS" -> FR_CATEGORY_COMICS = value
		"FR_CATEGORY_OTHER" -> FR_CATEGORY_OTHER = value
		"FR_NO_SOURCES_FOUND" -> FR_NO_SOURCES_FOUND = value
		"FR_EMPTY_SOURCES_CATALOG_TITLE" -> FR_EMPTY_SOURCES_CATALOG_TITLE = value
		"FR_AVAILABLE_COUNT" -> FR_AVAILABLE_COUNT = value
		"FR_ACTION_REMOVE" -> FR_ACTION_REMOVE = value
		"FR_BACK" -> FR_BACK = value
		"FR_NEW_PRESET" -> FR_NEW_PRESET = value
		"FR_SAVE" -> FR_SAVE = value
		"FR_BOOKMARKS_EMPTY_PRIMARY" -> FR_BOOKMARKS_EMPTY_PRIMARY = value
		"FR_MANAGE" -> FR_MANAGE = value
		"FR_CONTINUE_READING_CD" -> FR_CONTINUE_READING_CD = value
		"FR_UNKNOWN" -> FR_UNKNOWN = value
		"FR_DOWNLOADS_TITLE" -> FR_DOWNLOADS_TITLE = value
		"FR_STATUS_DOWNLOADING" -> FR_STATUS_DOWNLOADING = value
		"FR_STATUS_QUEUED" -> FR_STATUS_QUEUED = value
		"FR_STATUS_PAUSED" -> FR_STATUS_PAUSED = value
		"FR_STATUS_COMPLETE" -> FR_STATUS_COMPLETE = value
		"FR_STATUS_ERROR" -> FR_STATUS_ERROR = value
		"FR_STATUS_CANCELED" -> FR_STATUS_CANCELED = value
		"FR_STATUS_STUCK" -> FR_STATUS_STUCK = value
		"FR_ACTION_PAUSE" -> FR_ACTION_PAUSE = value
		"FR_ACTION_RESUME" -> FR_ACTION_RESUME = value
		"FR_ACTION_RETRY" -> FR_ACTION_RETRY = value
		"FR_ACTION_SKIP" -> FR_ACTION_SKIP = value
		"FR_ACTION_SKIP_ALL" -> FR_ACTION_SKIP_ALL = value
		"FR_ACTION_CANCEL" -> FR_ACTION_CANCEL = value
		"FR_ACTION_CANCEL_ALL" -> FR_ACTION_CANCEL_ALL = value
		"FR_ACTION_REMOVE_COMPLETED" -> FR_ACTION_REMOVE_COMPLETED = value
		"FR_DOWNLOADS_EMPTY_PRIMARY" -> FR_DOWNLOADS_EMPTY_PRIMARY = value
		"FR_STATUS_UNKNOWN" -> FR_STATUS_UNKNOWN = value
		"FR_CD_BACK" -> FR_CD_BACK = value
		"FR_CD_MORE_OPTIONS" -> FR_CD_MORE_OPTIONS = value
		"FR_PROTECT_TITLE" -> FR_PROTECT_TITLE = value
		"FR_PROTECT_SUBTITLE" -> FR_PROTECT_SUBTITLE = value
		"FR_PROTECT_WRONG_PASSWORD" -> FR_PROTECT_WRONG_PASSWORD = value
		"FR_PROTECT_NEXT" -> FR_PROTECT_NEXT = value
		"FR_PROTECT_CANCEL" -> FR_PROTECT_CANCEL = value
		"FR_CLOSE" -> FR_CLOSE = value
		"FR_INCOGNITO" -> FR_INCOGNITO = value
		"FR_ZOOM_OUT" -> FR_ZOOM_OUT = value
		"FR_ZOOM_IN" -> FR_ZOOM_IN = value
		"FR_SORT_AND_FILTER" -> FR_SORT_AND_FILTER = value
		"FR_GENRES" -> FR_GENRES = value
		"FR_SORT_POPULARITY" -> FR_SORT_POPULARITY = value
		"FR_SORT_RATING" -> FR_SORT_RATING = value
		"FR_RETRY" -> FR_RETRY = value
		"FR_APP_NAME" -> FR_APP_NAME = value
		"FR_COPIED" -> FR_COPIED = value
		"FR_LICENSE" -> FR_LICENSE = value
		"FR_ORIGIN_PROJECT" -> FR_ORIGIN_PROJECT = value
		"FR_LINK_KOTATSU_REDO" -> FR_LINK_KOTATSU_REDO = value
		"FR_LINK_KOTATSU" -> FR_LINK_KOTATSU = value
		"FR_SOURCES_PARSERS" -> FR_SOURCES_PARSERS = value
		"FR_BACKUP_BANNER" -> FR_BACKUP_BANNER = value
		"FR_CREATE_BACKUP" -> FR_CREATE_BACKUP = value
		"FR_RESTORE_BACKUP" -> FR_RESTORE_BACKUP = value
		"FR_CHOOSE_SAVE_TITLE" -> FR_CHOOSE_SAVE_TITLE = value
		"FR_CHOOSE_OPEN_TITLE" -> FR_CHOOSE_OPEN_TITLE = value
		"FR_ZIP_FILTER_DESCRIPTION" -> FR_ZIP_FILTER_DESCRIPTION = value
		"FR_RESTORE_WARNING_TITLE" -> FR_RESTORE_WARNING_TITLE = value
		"FR_RESTORE_CONFIRM" -> FR_RESTORE_CONFIRM = value
		"FR_BACKUP_CREATED_TITLE" -> FR_BACKUP_CREATED_TITLE = value
		"FR_BACKUP_CREATED" -> FR_BACKUP_CREATED = value
		"FR_BACKUP_FAILED" -> FR_BACKUP_FAILED = value
		"FR_RESTORE_INVALID" -> FR_RESTORE_INVALID = value
		"FR_RESTORE_FAILED" -> FR_RESTORE_FAILED = value
		"FR_DB_REQUIRED" -> FR_DB_REQUIRED = value
		"FR_SETTINGS_FILE" -> FR_SETTINGS_FILE = value
		"FR_DOWNLOADS_DIR" -> FR_DOWNLOADS_DIR = value
		"FR_STORAGE_USAGE" -> FR_STORAGE_USAGE = value
		"FR_DATA_REMOVAL" -> FR_DATA_REMOVAL = value
		"FR_DATA_REMOVAL_SUMMARY" -> FR_DATA_REMOVAL_SUMMARY = value
		"FR_BACKUPS" -> FR_BACKUPS = value
		"FR_OPEN_DIR" -> FR_OPEN_DIR = value
		"FR_DIR_CHANGED_TITLE" -> FR_DIR_CHANGED_TITLE = value
		"FR_OPEN_DIR_FAILED" -> FR_OPEN_DIR_FAILED = value
		"FR_SAME_DIR_MESSAGE" -> FR_SAME_DIR_MESSAGE = value
		"FR_ELLIPSIS" -> FR_ELLIPSIS = value
		"FR_DATA_REMOVAL_TITLE" -> FR_DATA_REMOVAL_TITLE = value
		"FR_CLEAR_READ_HISTORY" -> FR_CLEAR_READ_HISTORY = value
		"FR_CLEAR_SUGGESTIONS" -> FR_CLEAR_SUGGESTIONS = value
		"FR_CLEAR_NETWORK_CACHE" -> FR_CLEAR_NETWORK_CACHE = value
		"FR_CLEAR_COOKIES" -> FR_CLEAR_COOKIES = value
		"FR_CONFIRM_TITLE" -> FR_CONFIRM_TITLE = value
		"FR_DONE_HISTORY" -> FR_DONE_HISTORY = value
		"FR_DONE_SUGGESTIONS" -> FR_DONE_SUGGESTIONS = value
		"FR_DONE_NETWORK_CACHE" -> FR_DONE_NETWORK_CACHE = value
		"FR_DONE_COOKIES" -> FR_DONE_COOKIES = value
		"FR_ERROR" -> FR_ERROR = value
		"FR_DELETE" -> FR_DELETE = value
		"FR_TRACKER_SWITCH" -> FR_TRACKER_SWITCH = value
		"FR_FREQUENCY" -> FR_FREQUENCY = value
		"FR_CHECK_UPDATES" -> FR_CHECK_UPDATES = value
		"FR_DOWNLOAD_NEW" -> FR_DOWNLOAD_NEW = value
		"FR_SECTION_TROUBLESHOOTING" -> FR_SECTION_TROUBLESHOOTING = value
		"FR_FEED_LOG" -> FR_FEED_LOG = value
		"FR_BATTERY_OPTIMIZATION" -> FR_BATTERY_OPTIMIZATION = value
		"FR_FREQUENCY_DEFAULT" -> FR_FREQUENCY_DEFAULT = value
		"FR_FREQUENCY_HOURLY" -> FR_FREQUENCY_HOURLY = value
		"FR_FREQUENCY_EVERY6H" -> FR_FREQUENCY_EVERY6H = value
		"FR_FREQUENCY_DAILY" -> FR_FREQUENCY_DAILY = value
		"FR_DOWNLOAD_DISABLED" -> FR_DOWNLOAD_DISABLED = value
		"FR_DOWNLOAD_DOWNLOADED" -> FR_DOWNLOAD_DOWNLOADED = value
		"FR_DOWNLOAD_RECENTLY_READ" -> FR_DOWNLOAD_RECENTLY_READ = value
		"FR_FEED_LOG_EMPTY_TITLE" -> FR_FEED_LOG_EMPTY_TITLE = value
		"FR_AT" -> FR_AT = value
		"FR_TIME_PATTERN" -> FR_TIME_PATTERN = value
		"FR_AGO_JUST_NOW" -> FR_AGO_JUST_NOW = value
		"FR_AGO_MINUTES" -> FR_AGO_MINUTES = value
		"FR_AGO_HOURS" -> FR_AGO_HOURS = value
		"FR_AGO_DAYS" -> FR_AGO_DAYS = value
		"FR_SECTION_METERED" -> FR_SECTION_METERED = value
		"FR_DOWNLOAD_METERED" -> FR_DOWNLOAD_METERED = value
		"FR_SUGGESTIONS_WIFI_ONLY" -> FR_SUGGESTIONS_WIFI_ONLY = value
		"FR_TRACKER_WIFI_ONLY" -> FR_TRACKER_WIFI_ONLY = value
		"FR_SECTION_PROXY" -> FR_SECTION_PROXY = value
		"FR_PROXY_TYPE" -> FR_PROXY_TYPE = value
		"FR_PROXY_ADDRESS" -> FR_PROXY_ADDRESS = value
		"FR_PROXY_PORT" -> FR_PROXY_PORT = value
		"FR_PROXY_LOGIN" -> FR_PROXY_LOGIN = value
		"FR_PROXY_PASSWORD" -> FR_PROXY_PASSWORD = value
		"FR_PROXY_EMPTY" -> FR_PROXY_EMPTY = value
		"FR_PROXY_PASSWORD_MASK" -> FR_PROXY_PASSWORD_MASK = value
		"FR_SECTION_DOH" -> FR_SECTION_DOH = value
		"FR_DOH" -> FR_DOH = value
		"FR_DOH_DISABLED" -> FR_DOH_DISABLED = value
		"FR_PROXY_HOST_HINT" -> FR_PROXY_HOST_HINT = value
		"FR_PROXY_PORT_HINT" -> FR_PROXY_PORT_HINT = value
		"FR_PROXY_LOGIN_HINT" -> FR_PROXY_LOGIN_HINT = value
		"FR_PROXY_PASSWORD_HINT" -> FR_PROXY_PASSWORD_HINT = value
		"FR_TRISTATE_ENABLED" -> FR_TRISTATE_ENABLED = value
		"FR_TRISTATE_ASK" -> FR_TRISTATE_ASK = value
		"FR_TRISTATE_DISABLED" -> FR_TRISTATE_DISABLED = value
		"FR_PROXY_TYPE_DIRECT" -> FR_PROXY_TYPE_DIRECT = value
		"FR_PROXY_TYPE_HTTP" -> FR_PROXY_TYPE_HTTP = value
		"FR_PROXY_TYPE_SOCKS" -> FR_PROXY_TYPE_SOCKS = value
		"FR_DOH_GOOGLE" -> FR_DOH_GOOGLE = value
		"FR_DOH_CLOUDFLARE" -> FR_DOH_CLOUDFLARE = value
		"FR_DOH_ADGUARD" -> FR_DOH_ADGUARD = value
		"FR_DOH_ZERO_MS" -> FR_DOH_ZERO_MS = value
		"FR_TRACKER_NO_NSFW" -> FR_TRACKER_NO_NSFW = value
		"FR_TRACKER_FREQUENCY" -> FR_TRACKER_FREQUENCY = value
		"FR_OPEN_FEED_SETTINGS" -> FR_OPEN_FEED_SETTINGS = value
		"FR_SUGGESTIONS_NOTIFICATIONS" -> FR_SUGGESTIONS_NOTIFICATIONS = value
		"FR_DATABASE" -> FR_DATABASE = value
		"FR_NETWORK_CACHE" -> FR_NETWORK_CACHE = value
		"FR_TOTAL" -> FR_TOTAL = value
		"FR_COMPUTING" -> FR_COMPUTING = value
		"FR_SUGGESTIONS_ENABLE" -> FR_SUGGESTIONS_ENABLE = value
		"FR_SUGGESTIONS_INCLUDE_DISABLED" -> FR_SUGGESTIONS_INCLUDE_DISABLED = value
		"FR_SUGGESTIONS_EXCLUDE_GENRES" -> FR_SUGGESTIONS_EXCLUDE_GENRES = value
		"FR_SUGGESTIONS_GENRE_HINT" -> FR_SUGGESTIONS_GENRE_HINT = value
		"FR_READER_SECTION_PAGES" -> FR_READER_SECTION_PAGES = value
		"FR_READER_DEFAULT_MODE" -> FR_READER_DEFAULT_MODE = value
		"FR_READER_MODE_DETECT" -> FR_READER_MODE_DETECT = value
		"FR_READER_SCALE" -> FR_READER_SCALE = value
		"FR_READER_ANIMATION" -> FR_READER_ANIMATION = value
		"FR_READER_CROP_PAGES" -> FR_READER_CROP_PAGES = value
		"FR_READER_CROP_DISABLED" -> FR_READER_CROP_DISABLED = value
		"FR_READER_BACKGROUND" -> FR_READER_BACKGROUND = value
		"FR_READER_PAGES_NUMBERED" -> FR_READER_PAGES_NUMBERED = value
		"FR_READER_SECTION_WEBTOON" -> FR_READER_SECTION_WEBTOON = value
		"FR_READER_WEBTOON_SEPARATORS" -> FR_READER_WEBTOON_SEPARATORS = value
		"FR_READER_WEBTOON_ZOOM" -> FR_READER_WEBTOON_ZOOM = value
		"FR_READER_SECTION_ADVANCED" -> FR_READER_SECTION_ADVANCED = value
		"FR_READER_PRELOAD" -> FR_READER_PRELOAD = value
		"FR_PRIVACY_SECTION_SECURITY" -> FR_PRIVACY_SECTION_SECURITY = value
		"FR_PRIVACY_SECTION_ACTIVITY" -> FR_PRIVACY_SECTION_ACTIVITY = value
		"FR_ADULT_CONTENT" -> FR_ADULT_CONTENT = value
		"FR_ADULT_CONTENT_SUMMARY" -> FR_ADULT_CONTENT_SUMMARY = value
		"FR_SCREENSHOTS_POLICY" -> FR_SCREENSHOTS_POLICY = value
		"FR_SCREENSHOTS_ALLOW" -> FR_SCREENSHOTS_ALLOW = value
		"FR_SCREENSHOTS_BLOCK_NSFW" -> FR_SCREENSHOTS_BLOCK_NSFW = value
		"FR_SCREENSHOTS_BLOCK_INCOGNITO" -> FR_SCREENSHOTS_BLOCK_INCOGNITO = value
		"FR_SCREENSHOTS_BLOCK_ALL" -> FR_SCREENSHOTS_BLOCK_ALL = value
		"FR_READING_STATS" -> FR_READING_STATS = value
		"FR_READING_STATS_SUMMARY" -> FR_READING_STATS_SUMMARY = value
		"FR_INCOGNITO_TABS_HINT" -> FR_INCOGNITO_TABS_HINT = value
		"FR_INCOGNITO_NSFW" -> FR_INCOGNITO_NSFW = value
		"FR_PROTECT_APP" -> FR_PROTECT_APP = value
		"FR_PROTECT_APP_ENABLED" -> FR_PROTECT_APP_ENABLED = value
		"FR_PROTECT_APP_DISABLED" -> FR_PROTECT_APP_DISABLED = value
		"FR_SHARE_SOURCE_RELIABILITY" -> FR_SHARE_SOURCE_RELIABILITY = value
		"FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE" -> FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE = value
		"FR_SUGGESTIONS_18_PLUS" -> FR_SUGGESTIONS_18_PLUS = value
		"FR_DISCORD_HIDE_NSFW" -> FR_DISCORD_HIDE_NSFW = value
		"FR_COLOR_CORRECTION" -> FR_COLOR_CORRECTION = value
		"FR_BEFORE" -> FR_BEFORE = value
		"FR_AFTER" -> FR_AFTER = value
		"FR_INVERT_COLORS" -> FR_INVERT_COLORS = value
		"FR_GRAYSCALE" -> FR_GRAYSCALE = value
		"FR_BRIGHTNESS" -> FR_BRIGHTNESS = value
		"FR_CONTRAST" -> FR_CONTRAST = value
		"FR_YELLOWISH_BACKGROUND" -> FR_YELLOWISH_BACKGROUND = value
		"FR_RESET" -> FR_RESET = value
		"FR_COLOR_PREVIEW_LABEL" -> FR_COLOR_PREVIEW_LABEL = value
		"FR_AUTO_SCROLL" -> FR_AUTO_SCROLL = value
		"FR_ENABLE" -> FR_ENABLE = value
		"FR_SPEED" -> FR_SPEED = value
		"FR_AUTO_SCROLL_FAB_NOTE" -> FR_AUTO_SCROLL_FAB_NOTE = value
		"FR_AUTO_SCROLL_PLAY" -> FR_AUTO_SCROLL_PLAY = value
		"FR_AUTO_SCROLL_PAUSE" -> FR_AUTO_SCROLL_PAUSE = value
		"FR_ERASER_TUTORIAL_TITLE" -> FR_ERASER_TUTORIAL_TITLE = value
		"FR_SKIP" -> FR_SKIP = value
		"FR_ACCOUNTS_TITLE" -> FR_ACCOUNTS_TITLE = value
		"FR_TRACKERS_HEADER" -> FR_TRACKERS_HEADER = value
		"FR_DISABLED" -> FR_DISABLED = value
		"FR_SERVICE_ANILIST" -> FR_SERVICE_ANILIST = value
		"FR_SERVICE_KITSU" -> FR_SERVICE_KITSU = value
		"FR_SERVICE_MAL" -> FR_SERVICE_MAL = value
		"FR_SERVICE_SHIKIMORI" -> FR_SERVICE_SHIKIMORI = value
		"FR_COMMUNITY_ROW" -> FR_COMMUNITY_ROW = value
		"FR_DISCORD_ROW" -> FR_DISCORD_ROW = value
	}
}

/** Applique la langue selon la préférence (system/en/fr) : system → FR si français, sinon EN. */
public fun applyUiLanguagePreference(preference: String): UiLang {
	val lang = when (preference) {
		"en" -> UiLang.EN
		"fr" -> UiLang.FR
		else -> if (System.getProperty("user.language")?.lowercase()?.startsWith("fr") == true) UiLang.FR else UiLang.EN
	}
	applyUiLanguage(lang)
	return lang
}

/** Liste de toutes les constantes (utilisée par l'outil de génération). */
private val ALL_STRING_NAMES: List<String> = listOf(
	"FR_SEARCH_MANGA", "FR_SEARCH_HINT", "FR_HISTORY", "FR_FAVOURITES", "FR_EXPLORE",
	"FR_SOURCE", "FR_FEED", "FR_TEXT_HISTORY_HOLDER_PRIMARY", "FR_TEXT_HISTORY_HOLDER_SECONDARY",
	"FR_TEXT_EMPTY_HOLDER_PRIMARY", "FR_FAVOURITES_CATEGORY_EMPTY", "FR_TEXT_FEED_HOLDER",
	"FR_ALL_FAVOURITES", "FR_READ_LATER", "FR_HENTAI", "FR_CLEAR_HISTORY", "FR_LIST_OPTIONS",
	"FR_FAVOURITES_CATEGORIES", "FR_INCOGNITO_MODE", "FR_SETTINGS", "FR_UPDATE",
	"FR_SHOW_DISMISSED", "FR_SOURCE_PRESETS", "FR_MANAGE_SOURCES", "FR_FOR_YOU", "FR_TOP",
	"FR_MANGAS", "FR_MANHWA", "FR_SHOW_ALL", "FR_NEW_FINDS", "FR_MORE_FOR_YOU",
	"FR_LOCAL_STORAGE", "FR_BOOKMARKS", "FR_RANDOM", "FR_DOWNLOADS", "FR_SUGGESTIONS",
	"FR_MORE", "FR_MANGA_SOURCES", "FR_CATALOG", "FR_SOURCES_CATALOG", "FR_ALL_LANGUAGES",
	"FR_NEWEST", "FR_MISC_LANGUAGES", "FR_CONTENT_TYPE_MANGA", "FR_CONTENT_TYPE_HENTAI",
	"FR_CLEAR_SEARCH_HISTORY", "FR_SHOW_UPDATED", "FR_CLEAR_FEED", "FR_READ", "FR_CHAPTERS",
	"FR_CHAPTERS_EMPTY", "FR_DESCRIPTION", "FR_EXPAND", "FR_COLLAPSE", "FR_DOWNLOAD",
	"FR_ADD_TO_FAVOURITES", "FR_NSFW", "FR_PREV_CHAPTER", "FR_NEXT_CHAPTER", "FR_PREV_PAGE",
	"FR_NEXT_PAGE", "FR_SAVE_PAGE", "FR_READER_SETTINGS", "FR_TEXT_LOCAL_HOLDER_PRIMARY",
	"FR_IMPORT", "FR_CANCEL", "FR_OK", "FR_READING", "FR_FIND_MANGA", "FR_YOUR_DATA",
	"FR_APPLICATION", "FR_APPEARANCE", "FR_APPEARANCE_SUMMARY", "FR_NEW_CHAPTERS",
	"FR_SUGGESTIONS_SUMMARY", "FR_DOWNLOADS_STORAGE", "FR_BACKUP_RESTORE",
	"FR_BACKUP_RESTORE_SUMMARY", "FR_NOTIFICATIONS", "FR_NOTIFICATIONS_SUMMARY",
	"FR_PRIVACY_CONTENT", "FR_NETWORK", "FR_NETWORK_SUMMARY", "FR_ACCOUNTS",
	"FR_ACCOUNTS_SUMMARY", "FR_ABOUT", "FR_ABOUT_VERSION", "FR_COMING_SOON",
	"FR_IMAGES_MASKED", "FR_LIST_SECTION", "FR_MANGA_PAGE_SECTION", "FR_NAVIGATION_SECTION",
	"FR_LIST_MODE", "FR_GRID_SIZE", "FR_QUICK_FILTERS", "FR_PROGRESS_INDICATORS",
	"FR_LIST_BADGES", "FR_LIST_BADGES_SUMMARY", "FR_COLLAPSE_LONG_DESCRIPTION",
	"FR_PAGES_THUMBNAILS", "FR_PAGES_THUMBNAILS_SUMMARY", "FR_DEFAULT_TAB",
	"FR_DEFAULT_TAB_SUMMARY", "FR_READING_TIME", "FR_READING_TIME_SUMMARY",
	"FR_RELATED_MANGA", "FR_PIN_NAVIGATION", "FR_FLOATING_CONTINUE", "FR_EXIT_CONFIRMATION",
	"FR_SEARCH_SUGGESTIONS", "FR_RECENT_SHORTCUTS", "FR_MAIN_SECTIONS", "FR_ADD",
	"FR_LANGUAGE", "FR_LANGUAGE_SUMMARY", "FR_ON_DEVICE", "FR_UPDATES", "FR_THEME",
	"FR_COLOR_SCHEME", "FR_FOLLOW_SYSTEM", "FR_LIGHT", "FR_DARK", "FR_AMOLED",
	"FR_EXIT_DIALOG_TITLE", "FR_EXIT_DIALOG_CONFIRM", "FR_AUTHOR", "FR_STATE",
	"FR_STATE_ONGOING", "FR_STATE_FINISHED", "FR_STATE_ABANDONED", "FR_STATE_PAUSED",
	"FR_STATE_UPCOMING", "FR_NO_CHAPTERS", "FR_READ_FIRST_CHAPTER", "FR_CONTINUE",
	"FR_PROGRESS", "FR_DONE", "FR_SORT_ORDER", "FR_SORT_ORDER_SCORE",
	"FR_SORT_ORDER_ALPHABETIC", "FR_SHOW_GRID", "FR_ENABLE_ALL_SOURCES", "FR_AUTO_MIRROR",
	"FR_MANAGE_LINKS", "FR_SEARCH_SOURCES", "FR_SOURCE_DISABLED", "FR_DISABLE_EXPLICIT",
	"FR_DISABLE_ALL", "FR_ENABLE_ALL", "FR_CATEGORY_COMICS", "FR_CATEGORY_OTHER",
	"FR_NO_SOURCES_FOUND", "FR_EMPTY_SOURCES_CATALOG_TITLE", "FR_AVAILABLE_COUNT",
	"FR_ACTION_REMOVE", "FR_BACK", "FR_NEW_PRESET", "FR_SAVE", "FR_BOOKMARKS_EMPTY_PRIMARY",
	"FR_MANAGE", "FR_CONTINUE_READING_CD", "FR_UNKNOWN", "FR_DOWNLOADS_TITLE",
	"FR_STATUS_DOWNLOADING", "FR_STATUS_QUEUED", "FR_STATUS_PAUSED", "FR_STATUS_COMPLETE",
	"FR_STATUS_ERROR", "FR_STATUS_CANCELED", "FR_STATUS_STUCK", "FR_ACTION_PAUSE",
	"FR_ACTION_RESUME", "FR_ACTION_RETRY", "FR_ACTION_SKIP", "FR_ACTION_SKIP_ALL",
	"FR_ACTION_CANCEL", "FR_ACTION_CANCEL_ALL", "FR_ACTION_REMOVE_COMPLETED",
	"FR_DOWNLOADS_EMPTY_PRIMARY", "FR_STATUS_UNKNOWN", "FR_CD_BACK", "FR_CD_MORE_OPTIONS",
	"FR_PROTECT_TITLE", "FR_PROTECT_SUBTITLE", "FR_PROTECT_WRONG_PASSWORD", "FR_PROTECT_NEXT",
	"FR_PROTECT_CANCEL", "FR_CLOSE", "FR_INCOGNITO", "FR_ZOOM_OUT", "FR_ZOOM_IN",
	"FR_SORT_AND_FILTER", "FR_GENRES", "FR_SORT_POPULARITY", "FR_SORT_RATING", "FR_RETRY",
	"FR_APP_NAME", "FR_COPIED", "FR_LICENSE", "FR_ORIGIN_PROJECT", "FR_LINK_KOTATSU_REDO",
	"FR_LINK_KOTATSU", "FR_SOURCES_PARSERS", "FR_BACKUP_BANNER", "FR_CREATE_BACKUP",
	"FR_RESTORE_BACKUP", "FR_CHOOSE_SAVE_TITLE", "FR_CHOOSE_OPEN_TITLE",
	"FR_ZIP_FILTER_DESCRIPTION", "FR_RESTORE_WARNING_TITLE", "FR_RESTORE_CONFIRM",
	"FR_BACKUP_CREATED_TITLE", "FR_BACKUP_CREATED", "FR_BACKUP_FAILED", "FR_RESTORE_INVALID",
	"FR_RESTORE_FAILED", "FR_DB_REQUIRED", "FR_SETTINGS_FILE", "FR_DOWNLOADS_DIR",
	"FR_STORAGE_USAGE", "FR_DATA_REMOVAL", "FR_DATA_REMOVAL_SUMMARY", "FR_BACKUPS",
	"FR_OPEN_DIR", "FR_DIR_CHANGED_TITLE", "FR_OPEN_DIR_FAILED", "FR_SAME_DIR_MESSAGE",
	"FR_ELLIPSIS", "FR_DATA_REMOVAL_TITLE", "FR_CLEAR_READ_HISTORY", "FR_CLEAR_SUGGESTIONS",
	"FR_CLEAR_NETWORK_CACHE", "FR_CLEAR_COOKIES", "FR_CONFIRM_TITLE", "FR_DONE_HISTORY",
	"FR_DONE_SUGGESTIONS", "FR_DONE_NETWORK_CACHE", "FR_DONE_COOKIES", "FR_ERROR",
	"FR_DELETE", "FR_TRACKER_SWITCH", "FR_FREQUENCY", "FR_CHECK_UPDATES", "FR_DOWNLOAD_NEW",
	"FR_SECTION_TROUBLESHOOTING", "FR_FEED_LOG", "FR_BATTERY_OPTIMIZATION",
	"FR_FREQUENCY_DEFAULT", "FR_FREQUENCY_HOURLY", "FR_FREQUENCY_EVERY6H",
	"FR_FREQUENCY_DAILY", "FR_DOWNLOAD_DISABLED", "FR_DOWNLOAD_DOWNLOADED",
	"FR_DOWNLOAD_RECENTLY_READ", "FR_FEED_LOG_EMPTY_TITLE", "FR_AT", "FR_TIME_PATTERN",
	"FR_AGO_JUST_NOW", "FR_AGO_MINUTES", "FR_AGO_HOURS", "FR_AGO_DAYS", "FR_SECTION_METERED",
	"FR_DOWNLOAD_METERED", "FR_SUGGESTIONS_WIFI_ONLY", "FR_TRACKER_WIFI_ONLY",
	"FR_SECTION_PROXY", "FR_PROXY_TYPE", "FR_PROXY_ADDRESS", "FR_PROXY_PORT",
	"FR_PROXY_LOGIN", "FR_PROXY_PASSWORD", "FR_PROXY_EMPTY", "FR_PROXY_PASSWORD_MASK",
	"FR_SECTION_DOH", "FR_DOH", "FR_DOH_DISABLED", "FR_PROXY_HOST_HINT",
	"FR_PROXY_PORT_HINT", "FR_PROXY_LOGIN_HINT", "FR_PROXY_PASSWORD_HINT",
	"FR_TRISTATE_ENABLED", "FR_TRISTATE_ASK", "FR_TRISTATE_DISABLED", "FR_PROXY_TYPE_DIRECT",
	"FR_PROXY_TYPE_HTTP", "FR_PROXY_TYPE_SOCKS", "FR_DOH_GOOGLE", "FR_DOH_CLOUDFLARE",
	"FR_DOH_ADGUARD", "FR_DOH_ZERO_MS", "FR_TRACKER_NO_NSFW", "FR_TRACKER_FREQUENCY",
	"FR_OPEN_FEED_SETTINGS", "FR_SUGGESTIONS_NOTIFICATIONS", "FR_DATABASE",
	"FR_NETWORK_CACHE", "FR_TOTAL", "FR_COMPUTING", "FR_SUGGESTIONS_ENABLE",
	"FR_SUGGESTIONS_INCLUDE_DISABLED", "FR_SUGGESTIONS_EXCLUDE_GENRES",
	"FR_SUGGESTIONS_GENRE_HINT", "FR_READER_SECTION_PAGES", "FR_READER_DEFAULT_MODE",
	"FR_READER_MODE_DETECT", "FR_READER_SCALE", "FR_READER_ANIMATION", "FR_READER_CROP_PAGES",
	"FR_READER_CROP_DISABLED", "FR_READER_BACKGROUND", "FR_READER_PAGES_NUMBERED",
	"FR_READER_SECTION_WEBTOON", "FR_READER_WEBTOON_SEPARATORS", "FR_READER_WEBTOON_ZOOM",
	"FR_READER_SECTION_ADVANCED", "FR_READER_PRELOAD", "FR_PRIVACY_SECTION_SECURITY",
	"FR_PRIVACY_SECTION_ACTIVITY", "FR_ADULT_CONTENT", "FR_ADULT_CONTENT_SUMMARY",
	"FR_SCREENSHOTS_POLICY", "FR_SCREENSHOTS_ALLOW", "FR_SCREENSHOTS_BLOCK_NSFW",
	"FR_SCREENSHOTS_BLOCK_INCOGNITO", "FR_SCREENSHOTS_BLOCK_ALL", "FR_READING_STATS",
	"FR_READING_STATS_SUMMARY", "FR_INCOGNITO_TABS_HINT", "FR_INCOGNITO_NSFW",
	"FR_PROTECT_APP", "FR_PROTECT_APP_ENABLED", "FR_PROTECT_APP_DISABLED",
	"FR_SHARE_SOURCE_RELIABILITY", "FR_SHARE_SOURCE_RELIABILITY_UNAVAILABLE",
	"FR_SUGGESTIONS_18_PLUS", "FR_DISCORD_HIDE_NSFW", "FR_COLOR_CORRECTION", "FR_BEFORE",
	"FR_AFTER", "FR_INVERT_COLORS", "FR_GRAYSCALE", "FR_BRIGHTNESS", "FR_CONTRAST",
	"FR_YELLOWISH_BACKGROUND", "FR_RESET", "FR_COLOR_PREVIEW_LABEL", "FR_AUTO_SCROLL",
	"FR_ENABLE", "FR_SPEED", "FR_AUTO_SCROLL_FAB_NOTE", "FR_AUTO_SCROLL_PLAY",
	"FR_AUTO_SCROLL_PAUSE", "FR_ERASER_TUTORIAL_TITLE", "FR_SKIP", "FR_ACCOUNTS_TITLE",
	"FR_TRACKERS_HEADER", "FR_DISABLED", "FR_SERVICE_ANILIST", "FR_SERVICE_KITSU",
	"FR_SERVICE_MAL", "FR_SERVICE_SHIKIMORI", "FR_COMMUNITY_ROW", "FR_DISCORD_ROW",
)
