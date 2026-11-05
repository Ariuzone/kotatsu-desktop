/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Test de fumée : instancie la couche prefs/réseau/parser desktop et fait un appel réseau réel
 * (page 1) sur une source fiable (DynastyScans). Ignoré si le réseau n'est pas fiable (CI).
 */
package org.koitharu.kotatsu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koitharu.kotatsu.core.cache.MemoryContentCache
import org.koitharu.kotatsu.core.model.MangaSource
import org.koitharu.kotatsu.core.network.NetworkModule
import org.koitharu.kotatsu.core.parser.MangaLoaderContextImpl
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.parser.MirrorSwitcher
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.core.prefs.SourceSettingsStoreProvider
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class SourcesSmokeTest {

	private fun buildFactory(): Pair<NetworkModule, MangaRepository.Factory> {
		val dir = Files.createTempDirectory("kotatsu-desktop-smoke")
		val settingsStore = FileKeyValueStore(dir.resolve("settings.json"))
		val cookiesStore = FileKeyValueStore(dir.resolve("cookies.json"))
		val sourcesDir = dir.resolve("sources")
		val settings = AppSettings(settingsStore)

		val networkModule = NetworkModule(
			settings = settings,
			cacheDir = dir.resolve("http-cache").toFile(),
			cookieStore = cookiesStore,
		)
		val loaderContext = MangaLoaderContextImpl(
			httpClient = networkModule.mangaHttpClient,
			cookieJar = networkModule.cookieJar,
			sourceSettingsStoreProvider = SourceSettingsStoreProvider { source ->
				FileKeyValueStore(sourcesDir.resolve("${source.name}.json"))
			},
		)
		val mirrorSwitcher = MirrorSwitcher(settings, networkModule.mangaHttpClient)
		val factory = MangaRepository.Factory(
			loaderContext = loaderContext,
			contentCache = MemoryContentCache(),
			mirrorSwitcher = mirrorSwitcher,
		)
		return networkModule to factory
	}

	@Test
	fun sourcesEnumContainsDynastyScans() {
		val source = MangaParserSource.entries.firstOrNull { it.name == "DYNASTYSCANS" }
		assertEquals("DYNASTYSCANS", source?.name)
	}

	@Test
	fun dynastyscansFetchesFirstPage() {
		val source = MangaParserSource.entries.firstOrNull { it.name == "DYNASTYSCANS" }
			?: throw IllegalStateException("Source DYNASTYSCANS introuvable dans MangaParserSource")

		val (_, factory) = buildFactory()
		val repository = factory.create(source)

		// Appel réseau réel : première page de la liste
		val list = runBlocking(Dispatchers.IO) {
			withTimeout(TimeUnit.SECONDS.toMillis(60)) {
				repository.getList(offset = 0, order = null, filter = null)
			}
		}
		assertTrue("La liste renvoyée est vide", list.isNotEmpty())
		assertTrue(list.all { it.source.name == "DYNASTYSCANS" })
	}
}
