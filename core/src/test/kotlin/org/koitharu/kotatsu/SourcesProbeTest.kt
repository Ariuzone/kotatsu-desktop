/*
 * Diagnostic : sonde N sources réelles, compte succès/échecs et types d'erreurs.
 */
package org.koitharu.kotatsu

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.koitharu.kotatsu.core.network.NetworkModule
import org.koitharu.kotatsu.core.parser.MangaLoaderContextImpl
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.parser.MirrorSwitcher
import org.koitharu.kotatsu.core.cache.MemoryContentCache
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.core.prefs.SourceSettingsStoreProvider
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import java.io.File
import java.nio.file.Files

class SourcesProbeTest {

	@Test
	fun probe20Sources() = runBlocking {
		val dir = Files.createTempDirectory("kotatsu-probe").toFile()
		val settingsStore = FileKeyValueStore(dir.resolve("settings.json").toPath())
		val appSettings = AppSettings(settingsStore)
		val network = NetworkModule(appSettings, File(dir, "cache"), FileKeyValueStore(dir.resolve("cookies.json").toPath()))
		val ctx = MangaLoaderContextImpl(
			httpClient = network.mangaHttpClient,
			cookieJar = network.cookieJar,
			sourceSettingsStoreProvider = SourceSettingsStoreProvider { s ->
				FileKeyValueStore(dir.resolve("sources/${s.name}.json").toPath())
			},
		)
		val factory = MangaRepository.Factory(ctx, MemoryContentCache(), MirrorSwitcher(appSettings, network.mangaHttpClient))
		val candidates = listOf(
			"MANGADEX", "MANGAFIRE", "MANGAGEKO", "MANGAKAKALOT", "MANGAPILL",
			"COMICK", "MANGAREADER", "MANGALIFE", "WEBTOONS", "MANGAFOX",
			"BATOTO", "ASURASCANS", "MANGAKATANA", "MANGAHERE", "MANGAOWL",
			"READMANGA", "REMANGA", "DESETTEI", "NATSUMANGA", "MANGA4LIFE",
		)
		var ok = 0
		for (name in candidates) {
			val source = MangaParserSource.entries.firstOrNull { it.name == name } ?: continue
			try {
				val list = factory.create(source).getList(0, null, null)
				println("PROBE $name: OK (${list.size} mangas)")
				ok++
			} catch (e: Exception) {
				println("PROBE $name: FAIL ${e.javaClass.simpleName}: ${e.message?.take(120)}")
			}
		}
		println("PROBE RESULT: $ok/${candidates.size} OK")
	}
}
