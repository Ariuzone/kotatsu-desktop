/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop ;
 * 2026-11-08 — phase P2 : préférence isHistoryGroupingEnabled (« Groupe » de la bottom sheet
 * Options de liste, clé history_grouping) ;
 * 2026-11-08 — phase P3 : isSourcesGrid (« Afficher en vue grille », clé sources_grid) et
 * isAutoMirrorEnabled (« Choisir le miroir automatiquement », clé auto_mirror) de pref_sources.
 * Adaptation : façade desktop sur KeyValueStore (à la place de SharedPreferences) portant les
 * préférences P1. Les mêmes noms de clés KEY_* que l'original sont conservés quand ils existent.
 * Les préférences liées à l'UI Android (navigation, notifications, backups DocumentFile, WebView,
 * ressources R.*, ListSortOrder/SourcesSortOrder des modules list/explore non encore portés)
 * sont omises ; historySortOrder/allFavoritesSortOrder utilisent SortOrder (parsers) avec les
 * clés d'origine ; sourcesSortOrder est exposé en String brut (défaut "SCORE").
 */
package org.koitharu.kotatsu.core.prefs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import org.koitharu.kotatsu.core.network.DoHProvider
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.nullIfEmpty
import java.net.Proxy
import java.util.EnumSet

class AppSettings(
	private val store: KeyValueStore,
) {

	var listMode: ListMode
		get() = store.getEnumValue(KEY_LIST_MODE, ListMode.GRID)
		set(value) = store.edit { putString(KEY_LIST_MODE, value.name) }

	/** Thème (mode clair/sombre) : valeur brute d'origine — 1 = clair, 2 = sombre, sinon système. */
	var theme: Int?
		get() = store.getString(KEY_THEME, null)?.toIntOrNull()
		set(value) = store.edit { putString(KEY_THEME, value?.toString()) }

	var colorScheme: ColorScheme
		get() = store.getEnumValue(KEY_COLOR_THEME, ColorScheme.default)
		set(value) = store.edit { putString(KEY_COLOR_THEME, value.name) }

	val isAmoledTheme: Boolean
		get() = store.getBoolean(KEY_THEME_AMOLED, false)

	var isAmoledThemeEnabled: Boolean
		get() = isAmoledTheme
		set(value) = store.edit { putBoolean(KEY_THEME_AMOLED, value) }

	var gridSize: Int
		get() = store.getInt(KEY_GRID_SIZE, 100)
		set(value) = store.edit { putInt(KEY_GRID_SIZE, value) }

	var gridSizePages: Int
		get() = store.getInt(KEY_GRID_SIZE_PAGES, 100)
		set(value) = store.edit { putInt(KEY_GRID_SIZE_PAGES, value) }

	var isNsfwDisabled: Boolean
		get() = store.getBoolean(KEY_DISABLE_NSFW, false)
		set(value) = store.edit { putBoolean(KEY_DISABLE_NSFW, value) }

	/** Sources masquées de l'explore (noms de MangaSource). */
	var hiddenSources: Set<String>
		get() = store.getStringSet(KEY_HIDDEN_SOURCES, emptySet()).orEmpty()
		set(value) = store.edit { putStringSet(KEY_HIDDEN_SOURCES, value) }

	/** Ordre de tri des sources explore : nom brut (défaut « SCORE », cf. SourcesSortOrder Android). */
	var sourcesSortOrder: String
		get() = store.getString(KEY_SOURCES_ORDER, "SCORE") ?: "SCORE"
		set(value) = store.edit { putString(KEY_SOURCES_ORDER, value) }

	/** « Afficher en vue grille » de l'écran « Gérer les sources » (pref_sources d'origine). */
	var isSourcesGrid: Boolean
		get() = store.getBoolean(KEY_SOURCES_GRID, false)
		set(value) = store.edit { putBoolean(KEY_SOURCES_GRID, value) }

	/**
	 * « Choisir le miroir automatiquement » (pref_sources d'origine). Note : le changement
	 * automatique de domaine (miroirs) n'est pas encore branché côté desktop — la préférence
	 * est persistée mais sans effet pour l'instant.
	 */
	var isAutoMirrorEnabled: Boolean
		get() = store.getBoolean(KEY_AUTO_MIRROR, false)
		set(value) = store.edit { putBoolean(KEY_AUTO_MIRROR, value) }

	var readerMode: ReaderMode
		get() = store.getEnumValue(KEY_READER_MODE, ReaderMode.STANDARD)
		set(value) = store.edit { putString(KEY_READER_MODE, value.name) }

	var isReaderModeDetect: Boolean
		get() = store.getBoolean(KEY_READER_MODE_DETECT, true)
		set(value) = store.edit { putBoolean(KEY_READER_MODE_DETECT, value) }

	var readerAnimation: ReaderAnimation
		get() = store.getEnumValue(KEY_READER_ANIMATION, ReaderAnimation.DEFAULT)
		set(value) = store.edit { putString(KEY_READER_ANIMATION, value.name) }

	var isDoublePagesEnabled: Boolean
		get() = store.getBoolean(KEY_READER_DOUBLE_PAGES, false)
		set(value) = store.edit { putBoolean(KEY_READER_DOUBLE_PAGES, value) }

	var readerBackground: ReaderBackground
		get() = store.getEnumValue(KEY_READER_BACKGROUND, ReaderBackground.DEFAULT)
		set(value) = store.edit { putString(KEY_READER_BACKGROUND, value.name) }

	/** « Mode mise à l'échelle » du lecteur (pref_reader d'origine). */
	var readerScaleMode: ScaleMode
		get() = store.getEnumValue(KEY_READER_SCALE, ScaleMode.FIT_CENTER)
		set(value) = store.edit { putString(KEY_READER_SCALE, value.name) }

	/** « Pages numérotées » : afficher le numéro de page dans le coin inférieur. */
	var isPagesNumberingEnabled: Boolean
		get() = store.getBoolean(KEY_PAGES_NUMBERING, false)
		set(value) = store.edit { putBoolean(KEY_PAGES_NUMBERING, value) }

	/** « Séparateurs en mode webtoon » : espaces verticaux entre les pages. */
	var isWebtoonSeparatorsEnabled: Boolean
		get() = store.getBoolean(KEY_WEBTOON_SEPARATORS, false)
		set(value) = store.edit { putBoolean(KEY_WEBTOON_SEPARATORS, value) }

	// --- Phase P2 : apparence ---

	/** « Afficher les filtres rapides » dans les listes. */
	var isQuickFilterEnabled: Boolean
		get() = store.getBoolean(KEY_LIST_FILTERS, true)
		set(value) = store.edit { putBoolean(KEY_LIST_FILTERS, value) }

	/** « Badges dans les listes » (mangas sauvegardés / favoris). */
	var isListBadgesEnabled: Boolean
		get() = store.getBoolean(KEY_LIST_BADGES, true)
		set(value) = store.edit { putBoolean(KEY_LIST_BADGES, value) }

	/** « Réduire la description longue » sur l'écran de détails. */
	var isLongDescriptionCollapsed: Boolean
		get() = store.getBoolean(KEY_COLLAPSE_DESCRIPTIONS, true)
		set(value) = store.edit { putBoolean(KEY_COLLAPSE_DESCRIPTIONS, value) }

	/** « Confirmation de sortie » : dialog avant fermeture de la fenêtre. */
	var isExitConfirmationEnabled: Boolean
		get() = store.getBoolean(KEY_EXIT_CONFIRMATION, false)
		set(value) = store.edit { putBoolean(KEY_EXIT_CONFIRMATION, value) }

	/** « Afficher le bouton flottant Continuer » (hors incognito et historique non vide). */
	var isFloatingContinueEnabled: Boolean
		get() = store.getBoolean(KEY_NAV_CONTINUE_FAB, true)
		set(value) = store.edit { putBoolean(KEY_NAV_CONTINUE_FAB, value) }

	/** Ordre du rail (Principales sections de l'écran), noms NavItem séparés par des virgules. */
	var mainSections: List<String>
		get() = store.getString(KEY_MAIN_SECTIONS_ORDER, DEFAULT_MAIN_SECTIONS)
			?.split(",")?.mapNotNull { it.trim().takeIf { n -> n.isNotEmpty() } }
			?: DEFAULT_MAIN_SECTIONS.split(",")
		set(value) = store.edit { putString(KEY_MAIN_SECTIONS_ORDER, value.joinToString(",")) }

	var readerControls: Set<ReaderControl>
		get() = store.getStringSet(KEY_READER_CONTROLS, null)
			?.mapNotNullTo(EnumSet.noneOf(ReaderControl::class.java)) { x ->
				try {
					ReaderControl.valueOf(x)
				} catch (_: IllegalArgumentException) {
					null
				}
			}
			?: ReaderControl.DEFAULT
		set(value) = store.edit { putStringSet(KEY_READER_CONTROLS, value.mapTo(HashSet()) { it.name }) }

	var screenshotsPolicy: ScreenshotsPolicy
		get() = store.getEnumValue(KEY_SCREENSHOTS_POLICY, ScreenshotsPolicy.ALLOW)
		set(value) = store.edit { putString(KEY_SCREENSHOTS_POLICY, value.name) }

	var progressIndicators: ProgressIndicatorMode
		get() = store.getEnumValue(KEY_PROGRESS_INDICATORS, ProgressIndicatorMode.PERCENT_READ)
		set(value) = store.edit { putString(KEY_PROGRESS_INDICATORS, value.name) }

	/** Politique réseau pour le préchargement des pages (desktop : isMetered choisi par l'hôte). */
	var pagesPreloadPolicy: NetworkPolicy
		get() = NetworkPolicy.from(store.getString(KEY_PAGES_PRELOAD, null), NetworkPolicy.NON_METERED)
		set(value) = store.edit { putString(KEY_PAGES_PRELOAD, value.name) }

	/** Politique réseau pour le préchargement du contenu (suggestions, détails). */
	val contentPrefetchPolicy: NetworkPolicy
		get() = NetworkPolicy.from(store.getString(KEY_PREFETCH_CONTENT, null), NetworkPolicy.NEVER)

	var isIncognitoModeEnabled: Boolean
		get() = store.getBoolean(KEY_INCOGNITO_MODE, false)
		set(value) = store.edit { putBoolean(KEY_INCOGNITO_MODE, value) }

	var incognitoModeForNsfw: TriStateOption
		get() = store.getEnumValue(KEY_INCOGNITO_NSFW, TriStateOption.ASK)
		set(value) = store.edit { putString(KEY_INCOGNITO_NSFW, value.name) }

	fun isIncognitoModeEnabled(isNsfw: Boolean): Boolean {
		return isIncognitoModeEnabled || (isNsfw && incognitoModeForNsfw == TriStateOption.ENABLED)
	}

	var isTrackerEnabled: Boolean
		get() = store.getBoolean(KEY_TRACKER_ENABLED, true)
		set(value) = store.edit { putBoolean(KEY_TRACKER_ENABLED, value) }

	var isTrackerWiFiOnly: Boolean
		get() = store.getBoolean(KEY_TRACKER_WIFI_ONLY, false)
		set(value) = store.edit { putBoolean(KEY_TRACKER_WIFI_ONLY, value) }

	val trackerDownloadStrategy: TrackerDownloadStrategy
		get() = store.getEnumValue(KEY_TRACKER_DOWNLOAD, TrackerDownloadStrategy.DISABLED)

	var isSuggestionsEnabled: Boolean
		get() = store.getBoolean(KEY_SUGGESTIONS, true)
		set(value) = store.edit { putBoolean(KEY_SUGGESTIONS, value) }

	var isSuggestionsWiFiOnly: Boolean
		get() = store.getBoolean(KEY_SUGGESTIONS_WIFI_ONLY, false)
		set(value) = store.edit { putBoolean(KEY_SUGGESTIONS_WIFI_ONLY, value) }

	var isSuggestionsExcludeNsfw: Boolean
		get() = store.getBoolean(KEY_SUGGESTIONS_EXCLUDE_NSFW, false)
		set(value) = store.edit { putBoolean(KEY_SUGGESTIONS_EXCLUDE_NSFW, value) }

	/** Inclure les sources désactivées dans les suggestions (P4). */
	var isSuggestionsIncludeDisabled: Boolean
		get() = store.getBoolean(KEY_SUGGESTIONS_INCLUDE_DISABLED, false)
		set(value) = store.edit { putBoolean(KEY_SUGGESTIONS_INCLUDE_DISABLED, value) }

	/** Genres exclus des suggestions (P4) — clé desktop dédiée, style du module list non porté. */
	var excludedSuggestionGenres: Set<String>
		get() = store.getStringSet(KEY_SUGGESTIONS_EXCLUDED_GENRES, emptySet()).orEmpty()
		set(value) = store.edit { putStringSet(KEY_SUGGESTIONS_EXCLUDED_GENRES, value) }

	/** Fréquence de vérification des nouveaux chapitres (P4) — « DEFAULT » = Par défaut. */
	var trackerFrequency: String
		get() = store.getString(KEY_TRACKER_FREQUENCY, "DEFAULT") ?: "DEFAULT"
		set(value) = store.edit { putString(KEY_TRACKER_FREQUENCY, value) }

	/** Catégories favorites suivies pour les nouveaux chapitres (P4) — vide = toutes. */
	var trackerFavouriteCategories: Set<String>
		get() = store.getStringSet(KEY_TRACKER_FAV_CATEGORIES, emptySet()).orEmpty()
		set(value) = store.edit { putStringSet(KEY_TRACKER_FAV_CATEGORIES, value) }

	var allowDownloadOnMeteredNetwork: TriStateOption
		get() = store.getEnumValue(KEY_DOWNLOADS_METERED_NETWORK, TriStateOption.ASK)
		set(value) = store.edit { putString(KEY_DOWNLOADS_METERED_NETWORK, value.name) }

	val preferredDownloadFormat: DownloadFormat
		get() = store.getEnumValue(KEY_DOWNLOADS_FORMAT, DownloadFormat.AUTOMATIC)

	var dnsOverHttps: DoHProvider
		get() = store.getEnumValue(KEY_DOH, DoHProvider.NONE)
		set(value) = store.edit { putString(KEY_DOH, value.name) }

	var isSSLBypassEnabled: Boolean
		get() = store.getBoolean(KEY_SSL_BYPASS, false)
		set(value) = store.edit { putBoolean(KEY_SSL_BYPASS, value) }

	var isMirrorSwitchingEnabled: Boolean
		get() = store.getBoolean(KEY_MIRROR_SWITCHING, false)
		set(value) = store.edit { putBoolean(KEY_MIRROR_SWITCHING, value) }

	var proxyType: Proxy.Type
		get() {
			val raw = store.getString(KEY_PROXY_TYPE, null) ?: return Proxy.Type.DIRECT
			return enumValues<Proxy.Type>().find { it.name == raw } ?: Proxy.Type.DIRECT
		}
		set(value) = store.edit { putString(KEY_PROXY_TYPE, value.name) }

	var proxyAddress: String?
		get() = store.getString(KEY_PROXY_ADDRESS, null)
		set(value) = store.edit { putString(KEY_PROXY_ADDRESS, value) }

	var proxyPort: Int
		get() = store.getString(KEY_PROXY_PORT, null)?.toIntOrNull() ?: 0
		set(value) = store.edit { putString(KEY_PROXY_PORT, value.toString()) }

	var proxyLogin: String?
		get() = store.getString(KEY_PROXY_LOGIN, null)?.nullIfEmpty()
		set(value) = store.edit { putString(KEY_PROXY_LOGIN, value) }

	var proxyPassword: String?
		get() = store.getString(KEY_PROXY_PASSWORD, null)?.nullIfEmpty()
		set(value) = store.edit { putString(KEY_PROXY_PASSWORD, value) }

	val imagesProxy: Int
		get() = store.getString(KEY_IMAGES_PROXY, null)?.toIntOrNull() ?: -1

	var localListOrder: SortOrder
		get() = store.getEnumValue(KEY_LOCAL_LIST_ORDER, SortOrder.NEWEST)
		set(value) = store.edit { putString(KEY_LOCAL_LIST_ORDER, value.name) }

	/** Clé d'origine (ListSortOrder Android) ; trié avec SortOrder tant que le module list n'est pas porté. */
	var historySortOrder: SortOrder
		get() = store.getEnumValue(KEY_HISTORY_ORDER, SortOrder.NEWEST)
		set(value) = store.edit { putString(KEY_HISTORY_ORDER, value.name) }

	/** Clé d'origine (ListSortOrder Android) ; trié avec SortOrder tant que le module list n'est pas porté. */
	var allFavoritesSortOrder: SortOrder
		get() = store.getEnumValue(KEY_FAVORITES_ORDER, SortOrder.NEWEST)
		set(value) = store.edit { putString(KEY_FAVORITES_ORDER, value.name) }

	/**
	 * « Groupe » (bottom sheet « Options de liste », P2) : regrouper l'historique par date de
	 * dernière lecture. Aucune clé d'origine retrouvée côté Android (géré par le module list
	 * non porté) — clé desktop dédiée, même style que les autres.
	 */
	var isHistoryGroupingEnabled: Boolean
		get() = store.getBoolean(KEY_HISTORY_GROUPING, true)
		set(value) = store.edit { putBoolean(KEY_HISTORY_GROUPING, value) }

	val isRelatedMangaEnabled: Boolean
		get() = store.getBoolean(KEY_RELATED_MANGA, true)

	val isUnstableUpdatesAllowed: Boolean
		get() = store.getBoolean(KEY_UPDATES_UNSTABLE, false)

	val isReadingTimeEstimationEnabled: Boolean
		get() = store.getBoolean(KEY_READING_TIME, true)

	var isStatsEnabled: Boolean
		get() = store.getBoolean(KEY_STATS_ENABLED, false)
		set(value) = store.edit { putBoolean(KEY_STATS_ENABLED, value) }

	val searchSuggestionTypes: Set<SearchSuggestionType>
		get() = store.getStringSet(KEY_SEARCH_SUGGESTION_TYPES, null)?.let { stringSet ->
			stringSet.mapNotNullTo(EnumSet.noneOf(SearchSuggestionType::class.java)) { x ->
				try {
					SearchSuggestionType.valueOf(x)
				} catch (_: IllegalArgumentException) {
					null
				}
			}
		} ?: EnumSet.allOf(SearchSuggestionType::class.java)

	val isAutoLocalChaptersCleanupEnabled: Boolean
		get() = store.getBoolean(KEY_CHAPTERS_CLEAR_AUTO, false)

	/** Nouveaux chapitres : auto-téléchargement du tracker (préférences « new chapters »). */
	var trackerDownload: TrackerDownloadStrategy
		get() = trackerDownloadStrategy
		set(value) = store.edit { putString(KEY_TRACKER_DOWNLOAD, value.name) }

	var isTrackerNotificationsEnabled: Boolean
		get() = store.getBoolean(KEY_TRACKER_NOTIFICATIONS, true)
		set(value) = store.edit { putBoolean(KEY_TRACKER_NOTIFICATIONS, value) }

	var isTrackerNoNsfw: Boolean
		get() = store.getBoolean(KEY_TRACKER_NO_NSFW, false)
		set(value) = store.edit { putBoolean(KEY_TRACKER_NO_NSFW, value) }

	fun observe(vararg keys: String): Flow<String?> = store.observeChanges()
		.filter { key -> key == null || key in keys }
		.onStart { emit(null) }

	fun observeChanges(): Flow<String?> = store.observeChanges()

	fun getAllValues(): Map<String, Any?> = store.all

	fun upsertAll(m: Map<String, Any?>) = store.edit {
		clear()
		for ((key, value) in m) {
			when (value) {
				null -> Unit
				is String -> putString(key, value)
				is Int -> putInt(key, value)
				is Long -> putLong(key, value)
				is Float -> putFloat(key, value)
				is Double -> putFloat(key, value.toFloat())
				is Boolean -> putBoolean(key, value)
				is Set<*> -> putStringSet(key, value.mapNotNull { it as? String }.toSet())
				is Collection<*> -> putStringSet(key, value.mapNotNull { it as? String }.toSet())
			}
		}
	}

	companion object {

		/** Ordre par défaut du rail (les 5 onglets visibles de l'app d'origine). */
		public const val DEFAULT_MAIN_SECTIONS: String = "HISTORY,FAVORITES,EXPLORE,SOURCES,FEED"


		const val KEY_HIDDEN_SOURCES = "hidden_sources"

		// Noms de clés identiques à l'app Android d'origine
		const val KEY_LIST_MODE = "list_mode_2"
		const val KEY_THEME = "theme"
		const val KEY_COLOR_THEME = "color_theme"
		const val KEY_THEME_AMOLED = "amoled_theme"
		const val KEY_GRID_SIZE = "grid_size"
		const val KEY_GRID_SIZE_PAGES = "grid_size_pages"
		const val KEY_DISABLE_NSFW = "no_nsfw"
		const val KEY_READER_DOUBLE_PAGES = "reader_double_pages"
		const val KEY_READER_ANIMATION = "reader_animation2"
		const val KEY_READER_CONTROLS = "reader_controls"
		const val KEY_READER_MODE = "reader_mode"
		const val KEY_READER_MODE_DETECT = "reader_mode_detect"
		const val KEY_READER_BACKGROUND = "reader_background"
		const val KEY_READER_SCALE = "reader_scale_mode"
		const val KEY_PAGES_NUMBERING = "reader_pages_numbering"
		const val KEY_WEBTOON_SEPARATORS = "reader_webtoon_separators"
		const val KEY_TRACKER_ENABLED = "tracker_enabled"
		const val KEY_TRACKER_WIFI_ONLY = "tracker_wifi"
		const val KEY_TRACKER_DOWNLOAD = "tracker_download"
		const val KEY_TRACKER_NOTIFICATIONS = "tracker_notifications"
		const val KEY_TRACKER_NO_NSFW = "tracker_no_nsfw"
	const val KEY_SUGGESTIONS = "suggestions"
	const val KEY_SUGGESTIONS_WIFI_ONLY = "suggestions_wifi"
	const val KEY_SUGGESTIONS_EXCLUDED_GENRES = "suggestions_excluded_genres"
	const val KEY_TRACKER_FREQUENCY = "tracker_frequency"
	const val KEY_TRACKER_FAV_CATEGORIES = "tracker_fav_categories"
	const val KEY_SUGGESTIONS_INCLUDE_DISABLED = "suggestions_include_disabled"
		const val KEY_SUGGESTIONS_EXCLUDE_NSFW = "suggestions_exclude_nsfw"
		const val KEY_DOWNLOADS_METERED_NETWORK = "downloads_metered_network"
		const val KEY_DOWNLOADS_FORMAT = "downloads_format"
		const val KEY_DOH = "doh"
		const val KEY_INCOGNITO_MODE = "incognito"
		const val KEY_INCOGNITO_NSFW = "incognito_nsfw"
		const val KEY_PROGRESS_INDICATORS = "progress_indicators"
		const val KEY_LIST_FILTERS = "list_filters"
		const val KEY_LIST_BADGES = "list_badges"
		const val KEY_COLLAPSE_DESCRIPTIONS = "collapse_descriptions"
		const val KEY_EXIT_CONFIRMATION = "exit_confirmation"
		const val KEY_NAV_CONTINUE_FAB = "nav_continue_fab"
		const val KEY_MAIN_SECTIONS_ORDER = "main_sections_order"
		const val KEY_PAGES_PRELOAD = "pages_preload"
		const val KEY_PREFETCH_CONTENT = "prefetch_content"
		const val KEY_SCREENSHOTS_POLICY = "screenshots_policy"
		const val KEY_MIRROR_SWITCHING = "mirror_switching"
		const val KEY_PROXY_TYPE = "proxy_type_2"
		const val KEY_PROXY_ADDRESS = "proxy_address"
		const val KEY_PROXY_PORT = "proxy_port"
		const val KEY_PROXY_LOGIN = "proxy_login"
		const val KEY_PROXY_PASSWORD = "proxy_password"
		const val KEY_IMAGES_PROXY = "images_proxy_2"
		const val KEY_LOCAL_LIST_ORDER = "local_order"
		const val KEY_HISTORY_ORDER = "history_order"
		const val KEY_HISTORY_GROUPING = "history_grouping"
		const val KEY_FAVORITES_ORDER = "fav_order"
		const val KEY_SOURCES_ORDER = "sources_sort_order"
		const val KEY_SOURCES_GRID = "sources_grid"
		const val KEY_AUTO_MIRROR = "auto_mirror"
		const val KEY_RELATED_MANGA = "related_manga"
		const val KEY_UPDATES_UNSTABLE = "updates_unstable"
		const val KEY_SSL_BYPASS = "ssl_bypass"
		const val KEY_SEARCH_SUGGESTION_TYPES = "search_suggest_types"
		const val KEY_READING_TIME = "reading_time"
		const val KEY_STATS_ENABLED = "stats_on"
		const val KEY_CHAPTERS_CLEAR_AUTO = "chapters_clear_auto"
	}
}
