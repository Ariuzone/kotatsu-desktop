/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — sonde couvertures.
 * Vérification headless multi-sources : fetch liste (ordre défaut) -> coverUrl du 1er manga ->
 * GET via mangaHttpClient SANS tag (chemin Coil) -> statut 200 + Content-Type image/x.
 * Gating identique à SourcesProbeTest : RUN_NETWORK_PROBES=true (ou runNetworkProbes=true).
 */
package org.koitharu.kotatsu

import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.koitharu.kotatsu.core.cache.MemoryContentCache
import org.koitharu.kotatsu.core.network.NetworkModule
import org.koitharu.kotatsu.core.parser.MangaLoaderContextImpl
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.parser.MirrorSwitcher
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.core.prefs.SourceSettingsStoreProvider
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import java.io.File
import java.nio.file.Files

class CoverProbeTest {

	private val coverCandidates = listOf(
		"MANGADEX", "MANGAFIRE_EN", "COMICK_FUN", "WEBTOONS_EN", "TOONILY",
		"FLAMECOMICS", "MANGAKATANA", "MANGAHERE", "MANGAPLUSPARSER_EN", "KOMIIC",
	)

	@Test
	fun probeCovers() = runBlocking {
		assumeTrue(
			"Sonde couvertures ignorée : RUN_NETWORK_PROBES=true pour l'activer",
			System.getProperty("runNetworkProbes") == "true" || System.getenv("RUN_NETWORK_PROBES") == "true",
		)
		val dir = Files.createTempDirectory("kotatsu-covers").toFile()
		val appSettings = AppSettings(FileKeyValueStore(dir.resolve("settings.json").toPath()))
		val network = NetworkModule(appSettings, File(dir, "cache"), FileKeyValueStore(dir.resolve("cookies.json").toPath()))
		val ctx = MangaLoaderContextImpl(
			httpClient = network.mangaHttpClient,
			cookieJar = network.cookieJar,
			sourceSettingsStoreProvider = SourceSettingsStoreProvider { s ->
				FileKeyValueStore(dir.resolve("sources/${s.name}.json").toPath())
			},
		)
		val factory = MangaRepository.Factory(ctx, MemoryContentCache(), MirrorSwitcher(appSettings, network.mangaHttpClient))
		var ok = 0
		val failures = mutableListOf<String>()
		for (name in coverCandidates) {
			val source = MangaParserSource.entries.firstOrNull { it.name == name } ?: continue
			try {
				val list = factory.create(source).getList(0, null, null)
				val coverUrl = list.firstOrNull { it.coverUrl?.isNotEmpty() == true }?.coverUrl
				if (coverUrl.isNullOrEmpty()) {
					failures.add("$name: pas de coverUrl dans la liste (${list.size} mangas)")
					println("COVER $name: SKIP (pas de coverUrl)")
					continue
				}
				// Chemin Coil : requête sans tag MangaSource (l'UA vient de la résolution par domaine)
				network.mangaHttpClient.newCall(Request.Builder().url(coverUrl).build()).execute().use { r ->
					val contentType = r.header("content-type").orEmpty()
					val isImage = r.code == 200 && contentType.startsWith("image/")
					println("COVER $name: ${if (isImage) "OK" else "FAIL"} ${r.code} $contentType len=${r.body?.contentLength()} $coverUrl")
					if (isImage) ok++ else {
						failures.add("$name: ${r.code} $contentType")
						if (!contentType.startsWith("image/")) {
							println("  BODY: ${r.peekBody(200).string().replace('\n', ' ').take(200)}")
						}
					}
				}
			} catch (e: Exception) {
				failures.add("$name: ${e.javaClass.simpleName}: ${e.message?.take(100)}")
				println("COVER $name: FAIL ${e.javaClass.simpleName}: ${e.message?.take(120)}")
			}
		}
		println("COVER RESULT: $ok/${coverCandidates.size} covers OK ; échecs: $failures")
	}
}
