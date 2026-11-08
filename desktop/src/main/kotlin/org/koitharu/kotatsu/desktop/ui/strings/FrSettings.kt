/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — source de chaînes
 * des écrans de réglages desktop (phases P1/P4/P5 : lecteur, nouveaux chapitres, notifications,
 * réseau, stockage, suggestions, sauvegarde, données, à propos). Conformité à la règle client :
 * « toute chaîne affichée vient d'une source de chaînes, jamais écrite en brut dans un écran ».
 * Toutes les chaînes privées des écrans de ui/screens/settings/ sont centralisées ici en
 * public const val FR_*, avec leur provenance (libellé d'origine de l'app Android / frame
 * vidéo) ; les littéraux anglais volontairement non traduits sont signalés. Les constantes déjà
 * présentes dans Fr.kt (FR_OK, FR_CANCEL, FR_ABOUT, FR_NEW_CHAPTERS, FR_SUGGESTIONS…) ne sont
 * pas dupliquées : les écrans les utilisent directement.
 */
package org.koitharu.kotatsu.desktop.ui.strings

// --- Écran « À propos » (AboutScreen, phase P5, parité UI FR avec l'app Android) ---
public const val FR_APP_NAME: String = "Kotatsu Desktop" // nom de l'app desktop (capture)
public const val FR_COPIED: String = "Copié dans le presse-papiers" // version copiée (comportement desktop)
public const val FR_LICENSE: String = "Licence : GNU GPL v3" // licence (écran À propos)
public const val FR_ORIGIN_PROJECT: String = "Projet d'origine : Kotatsu / Kotatsu-Redo" // écran À propos
public const val FR_LINK_KOTATSU_REDO: String = "Kotatsu-Redo (GitHub)" // lien cliquable (écran À propos)
public const val FR_LINK_KOTATSU: String = "Kotatsu (GitHub)" // lien cliquable (écran À propos)
public const val FR_SOURCES_PARSERS: String = "Sources : kotatsu-parsers" // remerciements parsers

// --- Écran « Sauvegarde et restauration » (BackupRestoreScreen, phase P5,
//     équivalent desktop du BackupFragment d'origine) ---
// Libellé EN non traduit dans l'original (bannière de la vidéo) — partagé avec l'écran
// « Téléchargements et stockage » (section du même nom).
public const val FR_BACKUP_BANNER: String = "Create or restore a backup"
public const val FR_CREATE_BACKUP: String = "Créer une sauvegarde" // backup_create
public const val FR_CREATE_BACKUP_SUMMARY: String =
	"Enregistrer la base de données et les paramètres dans un fichier ZIP" // libellé vidéo
public const val FR_RESTORE_BACKUP: String = "Restaurer une sauvegarde" // backup_restore
public const val FR_RESTORE_BACKUP_SUMMARY: String =
	"Restaurer la base de données et les paramètres depuis un fichier ZIP" // libellé vidéo
public const val FR_CHOOSE_SAVE_TITLE: String = "Enregistrer la sauvegarde" // dialog JFileChooser (desktop)
public const val FR_CHOOSE_OPEN_TITLE: String = "Choisir une sauvegarde" // dialog JFileChooser (desktop)
public const val FR_ZIP_FILTER_DESCRIPTION: String = "Archives ZIP" // filtre JFileChooser (desktop)
public const val FR_RESTORE_WARNING_TITLE: String = "Restaurer une sauvegarde" // dialog d'avertissement
public const val FR_RESTORE_WARNING: String =
	"L'application sera fermée pour restaurer la sauvegarde. Continuer ?" // divergence desktop
public const val FR_RESTORE_CONFIRM: String = "Continuer" // bouton du dialog d'avertissement
public const val FR_BACKUP_CREATED_TITLE: String = "Sauvegarde créée" // dialog de succès (desktop)
public const val FR_BACKUP_CREATED: String = "Sauvegarde créée :\n%s" // dialog de succès (desktop)
public const val FR_BACKUP_FAILED: String = "Échec de la sauvegarde : %s" // dialog d'erreur (desktop)
public const val FR_RESTORE_INVALID: String = "Format de sauvegarde invalide" // zip sans kotatsu.db
public const val FR_RESTORE_FAILED: String = "Échec de la restauration : %s" // dialog d'erreur (desktop)
public const val FR_CLOSE_APP_NOTE: String =
	"Il est recommandé de fermer l'application avant de créer ou de restaurer une sauvegarde " +
		"pour garantir la cohérence de la base de données. La restauration ferme l'application " +
		"automatiquement : relancez-la ensuite." // note desktop (cohérence WAL)
// Noms de fichiers attendus dans l'archive (non affichés tels quels, conservés pour parité) :
public const val FR_DB_REQUIRED: String = "kotatsu.db" // base Room d'origine
public const val FR_SETTINGS_FILE: String = "settings.json" // KeyValueStore du portage desktop

// --- Écran « Téléchargements et stockage » (DownloadsStorageSettingsScreen, phase P5) ---
public const val FR_DOWNLOADS_DIR: String = "Dossier pour les téléchargements" // downloads_dir
public const val FR_STORAGE_USAGE: String = "Utilisation du stockage" // storage_usage (aussi titre
// de l'écran « Utilisation du stockage », StorageUsageScreen)
// Libellé EN non traduit dans l'original (libellé de la vidéo) :
public const val FR_DATA_REMOVAL: String = "Data removal"
public const val FR_DATA_REMOVAL_SUMMARY: String = "Effacer les données inutiles" // libellé vidéo
public const val FR_BACKUPS: String = "Sauvegardes" // ligne de section sauvegarde (vidéo)
public const val FR_OPEN_DIR: String = "Ouvrir le dossier" // entrée desktop (équivalent Android)
public const val FR_DIR_CHANGED_TITLE: String = "Dossier modifié" // dialog desktop (divergence)
public const val FR_DIR_CHANGED_MESSAGE: String =
	"Le dossier sélectionné a été noté. Dans ce portage desktop, les téléchargements existants " +
		"ne sont pas déplacés automatiquement : déplacez-les manuellement vers le nouveau dossier " +
		"pour les retrouver dans la bibliothèque locale." // divergence desktop
public const val FR_OPEN_DIR_FAILED: String = "Impossible d'ouvrir le dossier" // message de secours desktop
public const val FR_SAME_DIR_MESSAGE: String = "Le dossier sélectionné est déjà le dossier actuel." // dialog desktop
public const val FR_ELLIPSIS: String = "…" // taille en cours de calcul (placeholder)

// --- Écran « Effacement des données » (DataRemovalScreen, phase P5) ---
public const val FR_DATA_REMOVAL_TITLE: String = "Effacement des données" // data_removal (vidéo)
public const val FR_CLEAR_READ_HISTORY: String = "Effacer l'historique de lecture" // clear_history (values-fr)
public const val FR_CLEAR_SUGGESTIONS: String = "Effacer les suggestions" // clear_suggestions
public const val FR_CLEAR_NETWORK_CACHE: String = "Vider le cache réseau" // clear_network_cache
public const val FR_CLEAR_COOKIES: String = "Effacer les cookies" // clear_cookies
public const val FR_CONFIRM_TITLE: String = "Êtes-vous sûr ?" // dialog de confirmation (vidéo)
public const val FR_CONFIRM_HISTORY: String =
	"Êtes-vous sûr de vouloir effacer tout l'historique de lecture ? Cette action est irréversible."
public const val FR_CONFIRM_SUGGESTIONS: String =
	"Êtes-vous sûr de vouloir effacer toutes les suggestions ? Cette action est irréversible."
public const val FR_CONFIRM_NETWORK_CACHE: String =
	"Êtes-vous sûr de vouloir vider le cache réseau ? Les pages déjà chargées seront retéléchargées."
public const val FR_CONFIRM_COOKIES: String =
	"Êtes-vous sûr de vouloir effacer les cookies ? Vous serez déconnecté des sources."
public const val FR_DONE_HISTORY: String = "Historique de lecture effacé" // « snackbar » desktop
public const val FR_DONE_SUGGESTIONS: String = "Suggestions effacées" // « snackbar » desktop
public const val FR_DONE_NETWORK_CACHE: String = "Cache réseau vidé" // « snackbar » desktop
public const val FR_DONE_COOKIES: String = "Cookies effacés" // « snackbar » desktop
public const val FR_ERROR: String = "Une erreur est survenue : %s" // message d'erreur (desktop)
public const val FR_DELETE: String = "Effacer" // bouton de confirmation (delete, values-fr)

// --- Écran « Nouveaux chapitres » (FeedSettingsScreen, phase P4, frames t≈3:05-3:20) ---
public const val FR_TRACKER_SWITCH: String = "Vérifier les nouveaux chapitres et les notifier" // tracker_enabled
public const val FR_FREQUENCY: String = "Fréquence de vérification" // tracker_frequency
public const val FR_CHECK_UPDATES: String = "Rechercher les mises à jour" // check_for_updates
public const val FR_DOWNLOAD_NEW: String = "Télécharger de nouveaux chapitres" // tracker_download
public const val FR_SECTION_TROUBLESHOOTING: String = "Dépannage" // troubleshooting (headers)
public const val FR_FEED_LOG: String = "Vérification du journal des nouveaux chapitres" // feed_log
// (aussi titre de l'écran « Vérification du journal des nouveaux chapitres », FeedLogScreen)
public const val FR_FEED_LOG_SUBTITLE: String =
	"Informations de débogage sur la vérification en arrière-plan des nouveaux chapitres"
public const val FR_BATTERY_OPTIMIZATION: String = "Désactiver l'optimisation de la batterie" // battery_optimization
public const val FR_BATTERY_OPTIMIZATION_SUBTITLE: String =
	"Aide à la vérification des mises à jour des arrière-plans…" // libellé vidéo (entrée grisée)
public const val FR_FREQUENCY_DEFAULT: String = "Par défaut" // dialog radio fréquence (choix desktop)
public const val FR_FREQUENCY_HOURLY: String = "Toutes les heures" // dialog radio fréquence
public const val FR_FREQUENCY_EVERY6H: String = "Toutes les 6 heures" // dialog radio fréquence
public const val FR_FREQUENCY_DAILY: String = "Quotidienne" // dialog radio fréquence
public const val FR_DOWNLOAD_DISABLED: String = "Jamais" // TrackerDownloadStrategy.DISABLED
public const val FR_DOWNLOAD_DOWNLOADED: String = "Manga avec chapitres téléchargés" // .DOWNLOADED
public const val FR_DOWNLOAD_RECENTLY_READ: String = "Manga lu récemment" // .RECENTLY_READ
public const val FR_CATEGORIES_EMPTY: String =
	"Aucune catégorie favorite. Ajoutez des mangas à vos favoris pour " +
		"organiser la vérification des nouveaux chapitres par catégorie." // état vide du dialog

// --- Écran « Vérification du journal des nouveaux chapitres » (FeedLogScreen, phase P4,
//     frames t≈3:20 ; libellés de la vidéo) ---
public const val FR_FEED_LOG_EMPTY_TITLE: String = "Aucune vérification enregistrée"
public const val FR_FEED_LOG_EMPTY_TEXT: String =
	"Le journal de la vérification en arrière-plan des nouveaux chapitres est vide. " +
		"Les vérifications effectuées apparaîtront ici."
public const val FR_AT: String = " à " // horodatage « Il y a 3 heures à 16:24 »
public const val FR_TIME_PATTERN: String = "HH:mm" // motif DateTimeFormatter (non affiché tel quel)
public const val FR_AGO_JUST_NOW: String = "À l'instant" // temps relatif (< 1 min)
public const val FR_AGO_MINUTES: String = "Il y a %d minutes" // temps relatif (< 1 h)
public const val FR_AGO_HOURS: String = "Il y a %d heures" // temps relatif (< 24 h)
public const val FR_AGO_DAYS: String = "Il y a %d jours" // temps relatif (< 7 j)

// --- Écran « Réseau » (NetworkSettingsScreen, phase P5, parité UI FR avec l'app Android) ---
public const val FR_SECTION_METERED: String = "Données mobiles" // pref_header_network (metered)
public const val FR_DOWNLOAD_METERED: String = "Télécharger en données mobiles" // download_metered
public const val FR_SUGGESTIONS_WIFI_ONLY: String = "Suggestions en Wi-Fi seulement" // suggestions_wifi_only
public const val FR_TRACKER_WIFI_ONLY: String = "Vérification des nouveaux chapitres en Wi-Fi seulement" // tracker_wifi_only
public const val FR_SECTION_PROXY: String = "Proxy" // proxy
public const val FR_PROXY_TYPE: String = "Type de proxy" // proxy_type
public const val FR_PROXY_ADDRESS: String = "Adresse" // proxy_address
public const val FR_PROXY_PORT: String = "Port" // proxy_port
public const val FR_PROXY_LOGIN: String = "Identifiant" // proxy_login
public const val FR_PROXY_PASSWORD: String = "Mot de passe" // proxy_password
public const val FR_PROXY_EMPTY: String = "—" // valeur vide affichée (desktop)
public const val FR_PROXY_PASSWORD_MASK: String = "••••••••" // mot de passe masqué (desktop)
public const val FR_SECTION_DOH: String = "DNS sur HTTPS" // dns_over_https
public const val FR_DOH: String = "Serveur DNS sur HTTPS" // dns_over_https (ligne)
public const val FR_DOH_DISABLED: String = "Désactivé" // DoHProvider.NONE
public const val FR_RESTART_NOTE: String =
	"Ces réglages s'appliquent après redémarrage de l'application." // note desktop
public const val FR_PROXY_HOST_HINT: String = "Hôte ou adresse IP" // champ de saisie (desktop)
public const val FR_PROXY_PORT_HINT: String = "0 - 65535" // champ de saisie (desktop)
public const val FR_PROXY_LOGIN_HINT: String = "Identifiant" // champ de saisie (desktop)
public const val FR_PROXY_PASSWORD_HINT: String = "Mot de passe" // champ de saisie (desktop)
// Libellés FR du TriStateOption (« Télécharger en données mobiles ») :
// « Wi-Fi seulement » n'existe pas dans ce tri-état, ASK = « Demander ».
public const val FR_TRISTATE_ENABLED: String = "Toujours"
public const val FR_TRISTATE_ASK: String = "Demander"
public const val FR_TRISTATE_DISABLED: String = "Jamais"
// Types de proxy (java.net.Proxy.Type) — non traduits dans l'original :
public const val FR_PROXY_TYPE_DIRECT: String = "Direct"
public const val FR_PROXY_TYPE_HTTP: String = "HTTP"
public const val FR_PROXY_TYPE_SOCKS: String = "SOCKS"
// Serveurs DNS sur HTTPS — noms de marque, non traduits dans l'original :
public const val FR_DOH_GOOGLE: String = "Google"
public const val FR_DOH_CLOUDFLARE: String = "Cloudflare"
public const val FR_DOH_ADGUARD: String = "AdGuard"
public const val FR_DOH_ZERO_MS: String = "0ms"

// --- Écran « Notifications » (NotificationsSettingsScreen, phase P5) ---
public const val FR_TRACKER_NOTIFICATIONS: String =
	"Afficher une notification quand de nouveaux chapitres sont détectés" // tracker_notifications
public const val FR_TRACKER_NO_NSFW: String = "Ne pas notifier pour les mangas pour adultes" // tracker_nsfw
public const val FR_TRACKER_FREQUENCY: String = "Nouveaux chapitres — régler la fréquence" // libellé vidéo
public const val FR_TRACKER_FREQUENCY_SUMMARY: String =
	"Choisir la fréquence de vérification des nouveaux chapitres" // sous-titre vidéo
public const val FR_OPEN_FEED_SETTINGS: String = "Paramètres des nouveaux chapitres" // renvoi (desktop)
public const val FR_SUGGESTIONS_NOTIFICATIONS: String = "Notifier les nouvelles suggestions" // suggestions_notifications
public const val FR_SUGGESTIONS_NOTIFICATIONS_UNAVAILABLE: String =
	"Aucun réglage de notification des suggestions n'existe dans le portage desktop actuel" // entrée grisée

// --- Écran « Utilisation du stockage » (StorageUsageScreen, phase P5) ---
public const val FR_DATABASE: String = "Base de données" // storage_database (vidéo)
// FR_DOWNLOADS ("Téléchargements") : voir Fr.kt (déjà défini).
public const val FR_NETWORK_CACHE: String = "Cache réseau" // storage_cache (vidéo)
public const val FR_TOTAL: String = "Total" // storage_total (vidéo)
public const val FR_COMPUTING: String = "Calcul…" // calcul en cours (desktop)

// --- Écran « Suggestions » (SuggestionsSettingsScreen, phase P4, t≈2:40-3:05) ---
public const val FR_SUGGESTIONS_ENABLE: String = "Activer les suggestions" // suggestions_enable
public const val FR_SUGGESTIONS_INCLUDE_DISABLED: String = "Inclure les sources désactivées" // suggestions_include_disabled
public const val FR_SUGGESTIONS_INCLUDE_DISABLED_SUMMARY: String =
	"Afficher les suggestions de toutes les sources de mangas, y compris celles désactivées"
public const val FR_SUGGESTIONS_EXCLUDE_GENRES: String = "Exclure des genres" // suggestions_excluded_genres
public const val FR_SUGGESTIONS_EXCLUDE_GENRES_SUMMARY: String =
	"Spécifiez les genres que vous ne voulez pas voir apparaître dans les suggestions"
public const val FR_SUGGESTIONS_LOCAL_NOTE: String =
	"Toutes les données sont analysées localement sur cet appareil et ne sont jamais envoyées ailleurs."
public const val FR_SUGGESTIONS_GENRE_HINT: String = "Nom du genre" // champ de saisie (dialog desktop)

// --- Écran « Paramètres du lecteur » (ReaderSettingsScreen, phase P1, pref_reader +
//     ReaderSettingsFragment, captures frames 8-19) ---
public const val FR_READER_SECTION_PAGES: String = "Pages" // section d'en-tête (captures)
public const val FR_READER_DEFAULT_MODE: String = "Mode par défaut" // default_mode
public const val FR_READER_MODE_DETECT: String = "Mode de détection automatique du lecteur" // reader_mode_detect
public const val FR_READER_MODE_DETECT_SUMMARY: String =
	"Détecter automatiquement si un manga est un webtoon" // libellé d'origine
public const val FR_READER_SCALE: String = "Mode mise à l'échelle" // scale_mode
public const val FR_READER_ANIMATION: String = "Animation de page" // animation
public const val FR_READER_CROP_PAGES: String = "Recadrer les pages" // crop_pages
public const val FR_READER_CROP_DISABLED: String = "Désactivé (non disponible sur desktop)" // divergence
public const val FR_READER_BACKGROUND: String = "Arrière-plan" // reader_background
public const val FR_READER_PAGES_NUMBERED: String = "Pages numérotées" // pages_numbering
public const val FR_READER_PAGES_NUMBERED_SUMMARY: String =
	"Afficher les numéros de page dans le coin inférieur" // libellé d'origine
// Non traduit dans l'original (nom de mode) :
public const val FR_READER_SECTION_WEBTOON: String = "Webtoon" // section d'en-tête (captures)
public const val FR_READER_WEBTOON_SEPARATORS: String = "Séparateurs en mode webtoon" // webtoon_separators
public const val FR_READER_WEBTOON_SEPARATORS_SUMMARY: String =
	"Afficher des espaces verticaux entre les pages en mode webtoon" // libellé d'origine
public const val FR_READER_WEBTOON_ZOOM: String = "Zoom Webtoon" // adaptation desktop
public const val FR_READER_WEBTOON_ZOOM_SUMMARY: String =
	"Ctrl+molette ou double-clic dans le lecteur (desktop)" // divergence desktop
public const val FR_READER_SECTION_ADVANCED: String = "Avancé" // section d'en-tête (captures)
public const val FR_READER_PRELOAD: String = "Précharger les pages" // preload_pages

/** Taille lisible en Ko/Mo/Go (unités affichées de l'original, libellés vidéo). */
public fun frFileSize(bytes: Long): String = when {
	bytes >= 1_000_000_000L -> String.format("%.1f Go", bytes / 1_000_000_000.0)
	bytes >= 1_000_000L -> String.format("%.1f Mo", bytes / 1_000_000.0)
	bytes >= 1_000L -> String.format("%.0f Ko", bytes / 1_000.0)
	else -> String.format("%d o", bytes)
}
