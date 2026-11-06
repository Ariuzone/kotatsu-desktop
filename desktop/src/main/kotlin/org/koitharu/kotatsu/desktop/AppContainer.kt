/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Racine de composition (remplace la DI Hilt de l'app d'origine, cf. core/AppModule.kt) :
 * câble préférences → réseau → contexte de parsers → fabrique de dépôts.
 * Les données vivent dans %APPDATA%/Kotatsu (équiv. du stockage privé Android).
 */
package org.koitharu.kotatsu.desktop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koitharu.kotatsu.local.domain.MangaLock
import org.koitharu.kotatsu.core.cache.MemoryContentCache
import org.koitharu.kotatsu.core.network.NetworkModule
import org.koitharu.kotatsu.core.parser.MangaLoaderContextImpl
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.parser.MirrorSwitcher
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.core.prefs.SourceSettingsStoreProvider
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.SortOrder
import java.io.File
import java.nio.file.Path

public class AppContainer private constructor(appDir: File) {

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	public val appSettings: AppSettings

	public val network: NetworkModule

	private val loaderContext: MangaLoaderContextImpl

	private val contentCache = MemoryContentCache()

	/** Base Room (schéma v28 identique à l'app Android), dans %APPDATA%/Kotatsu. */
	public val database: org.koitharu.kotatsu.core.db.MangaDatabase by lazy {
		org.koitharu.kotatsu.core.db.createMangaDatabase(File(appDir, "kotatsu.db"))
	}

	/** Favoris + historique (DAO Room portés). */
	public val library: org.koitharu.kotatsu.desktop.library.LibraryRepository by lazy {
		org.koitharu.kotatsu.desktop.library.LibraryRepository(database)
	}

	public val repositoryFactory: MangaRepository.Factory

	/** Verrouillage par mot de passe ( ProtectManager, hash MD5 comme l'app d'origine). */
	public val protectManager: org.koitharu.kotatsu.desktop.ui.screens.protect.ProtectManager by lazy {
		org.koitharu.kotatsu.desktop.ui.screens.protect.ProtectManager(settingsStore)
	}

	/** Suivi des nouveaux chapitres : notifier + tâche périodique. */
	public val trackerNotifier: org.koitharu.kotatsu.desktop.tracker.TrackerNotifier by lazy {
		org.koitharu.kotatsu.desktop.tracker.TrackerNotifier()
	}

	public val trackerScheduler: org.koitharu.kotatsu.desktop.tracker.TrackerScheduler by lazy {
		org.koitharu.kotatsu.desktop.tracker.TrackerScheduler(
			settings = appSettings,
			store = settingsStore,
			repository = org.koitharu.kotatsu.tracker.domain.TrackingRepository(database),
			checkNewChapters = org.koitharu.kotatsu.tracker.domain.CheckNewChaptersUseCase(
				repository = org.koitharu.kotatsu.tracker.domain.TrackingRepository(database),
				detailsLoader = org.koitharu.kotatsu.tracker.domain.MangaDetailsLoader.of(repositoryFactory),
			),
			notifier = trackerNotifier,
			scope = scope,
		)
	}

	private lateinit var settingsStore: org.koitharu.kotatsu.core.prefs.KeyValueStore

	/** Téléchargements hors-ligne + lecture locale (CBZ/dossiers), cf. lot P2. */
	public val downloadManager: org.koitharu.kotatsu.download.domain.DownloadManager by lazy {
		val localStorageChanges = MutableSharedFlow<org.koitharu.kotatsu.local.domain.model.LocalManga?>(
			extraBufferCapacity = 16,
		)
		val lock = MangaLock()
		val storageManager = org.koitharu.kotatsu.local.data.LocalStorageManager(
			defaultDir = File(appDir, "local"),
		)
		lateinit var localRepository: org.koitharu.kotatsu.local.data.LocalMangaRepository
		val localMangaIndex = org.koitharu.kotatsu.local.data.index.LocalMangaIndex(
			db = database,
			localMangaRepositoryProvider = { localRepository },
			prefsFile = File(appDir, "local-prefs.json"),
		)
		localRepository = org.koitharu.kotatsu.local.data.LocalMangaRepository(
			storageManager = storageManager,
			localMangaIndex = localMangaIndex,
			localStorageChanges = localStorageChanges,
			settings = appSettings,
			lock = lock,
		)
		localMangaRepository = localRepository
		org.koitharu.kotatsu.download.domain.DownloadManager(
			scope = scope,
			okHttp = network.mangaHttpClient,
			localMangaRepository = localRepository,
			mangaRepositoryFactory = repositoryFactory,
			localMangaIndex = localMangaIndex,
			settings = appSettings,
			lock = lock,
			localStorageChanges = localStorageChanges,
		)
	}

	/** Dépôt local (CBZ/dossiers), branché dans la fabrique de dépôts. */
	private lateinit var localMangaRepository: org.koitharu.kotatsu.local.data.LocalMangaRepository

	/** Dépôt local (CBZ/dossiers), null tant que le gestionnaire de téléchargements n'est pas initialisé. */
	public fun localRepository(): org.koitharu.kotatsu.local.data.LocalMangaRepository? =
		if (::localMangaRepository.isInitialized) localMangaRepository else null

	init {
		appDir.mkdirs()
		val settingsStore = FileKeyValueStore(appDir.resolve("settings.json").toPath())
		this.settingsStore = settingsStore
		// ImageLoader Coil global : passe par le client réseau de l'app (headers communs)
		// + referer par source, fidèle au MangaSourceHeaderInterceptor d'origine.
		val imageLoader = coil3.ImageLoader.Builder(coil3.PlatformContext.INSTANCE)
			.components {
				add(
					coil3.network.okhttp.OkHttpNetworkFetcherFactory(
						callFactory = { imageClient() },
					),
				)
			}
			.build()
		coil3.SingletonImageLoader.setSafe { imageLoader }
		val sourcesDir: Path = appDir.resolve("sources").toPath()
		appSettings = AppSettings(settingsStore)
		network = NetworkModule(
			settings = appSettings,
			cacheDir = File(appDir, "okhttp-cache"),
			cookieStore = FileKeyValueStore(appDir.resolve("cookies.json").toPath()),
		)
		loaderContext = MangaLoaderContextImpl(
			httpClient = network.mangaHttpClient,
			cookieJar = network.cookieJar,
			sourceSettingsStoreProvider = SourceSettingsStoreProvider { source ->
				FileKeyValueStore(sourcesDir.resolve("${source.name}.json"))
			},
		)
		val mirrorSwitcher = MirrorSwitcher(appSettings, network.mangaHttpClient)
		repositoryFactory = MangaRepository.Factory(
			loaderContext = loaderContext,
			contentCache = contentCache,
			mirrorSwitcher = mirrorSwitcher,
			localMangaRepository = null, // branché après création du dépôt local (lazy, cf. downloadManager)
		)
	}

	/** Liste paged d'une source (comme MangaListViewModel de l'app d'origine). */
	public suspend fun getList(
		source: MangaSource,
		offset: Int = 0,
		order: SortOrder? = null,
		query: String? = null,
	): List<Manga> = repositoryFactory.create(source).getList(
		offset = offset,
		order = order,
		filter = query?.takeIf { it.isNotBlank() }
			?.let { org.koitharu.kotatsu.parsers.model.MangaListFilter(query = it) },
	)

	public suspend fun getDetails(manga: Manga): Manga = repositoryFactory.create(manga.source).getDetails(manga)

	public suspend fun getPages(chapter: MangaChapter): List<MangaPage> =
		repositoryFactory.create(chapter.source).getPages(chapter)

	public suspend fun getPageUrl(page: MangaPage): String =
		repositoryFactory.create(page.source).getPageUrl(page)

	/**
	 * Recherche multi-sources, comme SearchActivity de l'app d'origine : requête envoyée
	 * en parallèle aux sources activées (ici : les sources non « broken », triées par nom),
	 * résultats concaténés.
	 */
	public suspend fun searchAll(query: String, maxSources: Int = 8): List<Manga> {
		if (query.isBlank()) return emptyList()
		val sources = MangaParserSource.entries
			.filterNot { it.isBroken }
			.sortedBy { it.title }
			.take(maxSources)
		val filter = org.koitharu.kotatsu.parsers.model.MangaListFilter(query = query)
		return sources.map { source ->
			scope.async {
				runCatching { repositoryFactory.create(source).getList(0, SortOrder.RELEVANCE, filter) }
					.getOrDefault(emptyList())
			}
		}.awaitAll().flatten()
	}

	// Cache domaine -> referer (MangaSourceHeaderInterceptor d'origine : images chargées
	// avec Referer = base du site source, sinon certaines renvoient 403).
	@Volatile
	private var referers: Map<String, String>? = null

	private fun refererFor(host: String): String? {
		val map = referers ?: MangaParserSource.entries
			.mapNotNull { s ->
				domainOf(s)?.let { d -> d.lowercase() to "https://$d/" }
			}
			.toMap()
			.also { referers = it }
		return map.entries.firstOrNull { host.endsWith(it.key.removePrefix("www.")) }?.value
	}

	/** Client HTTP dédié aux images : headers communs + referer par source. */
	private fun imageClient(): okhttp3.OkHttpClient = network.mangaHttpClient.newBuilder()
		.addInterceptor { chain ->
			val request = chain.request()
			val referer = refererFor(request.url.host)
			val enriched = if (referer != null && request.header("Referer") == null) {
				request.newBuilder().header("Referer", referer).build()
			} else {
				request
			}
			chain.proceed(enriched)
		}
		.build()

	/** Domaine d'une source parser (pour les favicons), null si indisponible. */
	public fun domainOf(source: MangaSource): String? = runCatching {
		(source as? MangaParserSource)?.let { loaderContext.newParserInstance(it).domain }
	}.getOrNull()

	public companion object {

		@Volatile
		private var instance: AppContainer? = null

		public fun getInstance(): AppContainer = instance ?: synchronized(this) {
			instance ?: AppContainer(defaultAppDir()).also { instance = it }
		}

		/** Pour les tests : conteneur isolé dans un dossier temporaire. */
		public fun createForTest(dir: File): AppContainer = AppContainer(dir)

		private fun defaultAppDir(): File {
			val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
			return File(appData, "Kotatsu")
		}
	}
}
