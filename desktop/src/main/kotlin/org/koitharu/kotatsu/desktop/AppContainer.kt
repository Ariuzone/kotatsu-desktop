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

	public val repositoryFactory: MangaRepository.Factory

	init {
		appDir.mkdirs()
		val settingsStore = FileKeyValueStore(appDir.resolve("settings.json").toPath())
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

	public companion object {

		@Volatile
		private var instance: AppContainer? = null

		public fun getInstance(): AppContainer = instance ?: synchronized(this) {
			instance ?: AppContainer(defaultAppDir()).also { instance = it }
		}

		private fun defaultAppDir(): File {
			val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
			return File(appData, "Kotatsu")
		}
	}
}
