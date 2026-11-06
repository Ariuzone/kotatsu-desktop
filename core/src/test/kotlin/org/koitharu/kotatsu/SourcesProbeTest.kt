/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — sonde réseau étendue.
 * Instrumentation de travail : sonde 47 sources réelles, mesure le taux de succès global et
 * imprime le type d'erreur par source. Ce test n'échoue jamais : il documente l'état réel du
 * réseau desktop.
 *
 * Mode CI : la sonde réseau est DÉSACTIVÉE par défaut (les builds CI n'ont pas de réseau fiable
 * et ce test dure plusieurs minutes). Elle ne s'exécute que si :
 *   - la propriété système `runNetworkProbes=true` (ex. systemProp.runNetworkProbes=true dans
 *     gradle.properties, car `./gradlew -D...` ne l'endosse pas au JVM de test), OU
 *   - la variable d'environnement RUN_NETWORK_PROBES=true (héritée par le JVM de test).
 * Sinon le test est ignoré (Assume) — CI verte hors ligne.
 * Filtrage optionnel : -Dprobe.sources=MANGADEX,MANGAFIRE_EN (systemProp) ou PROBE_SOURCES env.
 */
package org.koitharu.kotatsu

import kotlinx.coroutines.runBlocking
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
import org.koitharu.kotatsu.parsers.model.SortOrder
import java.io.File
import java.nio.file.Files

class SourcesProbeTest {

	/** Sources candidates : noms d'enum connus du paquet parsers, majuscules. */
	private val candidates = listOf(
		"MANGADEX", "MANGAFIRE_EN", "COMICK_FUN", "WEBTOONS_EN", "TOONILY",
		"FLAMECOMICS", "MANGAPILL", "ASURASCANS", "MANGAKATANA", "MANGAHERE",
		"MANGAOWL", "REMANGA", "MANGAGEKO", "MANGAKAKALOT", "MANGA4LIFE",
		"MANGAGO", "MANGABUDDY", "MANGAFREAK", "ZINMANGA", "MANGACLASH",
		"AQUAMANGA", "MANGATOWN", "MANGANELO_COM", "READMANGA_RU", "MANGALIB",
		"DESUME", "MANGAPARK", "MANGAREADERTO", "MANGAKAKALOTTV", "MANGAGOJO",
		"ZENMANGA", "KOMIIC", "BAOZIMH", "MANGAPLUSPARSER_EN", "DYNASTYSCANS",
		"NHENTAI",
		// Deuxième vague d'instrumentation (v2)
		"MANHUAPLUS", "MANGASUSHI", "COMIX", "MANGABUFF", "ACOMICS",
		"SENKURO", "COMX", "YAKSHACOMICS", "PLUMACOMICS", "DRAGONTRANSLATIONORG",
		"LINEWEBTOONS_EN",
	)

	/**
	 * Validation hors ligne (exécutée en CI) : les noms de la sonde existent bien dans
	 * MangaParserSource.entries — protège la sonde contre les renommages du paquet parsers.
	 */
	@Test
	fun probeCandidatesExistInParsersEnum() {
		val unknown = candidates.filterNot { name -> MangaParserSource.entries.any { it.name == name } }
		assumeTrue(unknown.joinToString(), unknown.isEmpty())
	}

	@Test
	fun probeSources() = runBlocking {
		assumeTrue(
			"Sonde réseau ignorée : -DrunNetworkProbes=true (ou RUN_NETWORK_PROBES=true) pour l'activer",
			isNetworkProbeEnabled(),
		)
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
		val filter = (System.getProperty("probe.sources") ?: System.getenv("PROBE_SOURCES"))
			?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
		val selected = candidates.filter { name ->
			val exists = MangaParserSource.entries.any { it.name == name }
			if (!exists) println("PROBE $name: SKIP (inconnu du paquet parsers)")
			(filter == null || name in filter) && exists
		}
		var ok = 0
		val errors = mutableMapOf<String, String>()
		for (name in selected) {
			val source = MangaParserSource.entries.first { it.name == name }
			// Ordre par défaut + ordre explicite POPULARITY (l'écran Discover de l'app desktop
			// utilise des tris explicites : un ordre peut échouer là où le défaut passe).
			for (order in listOf(null, SortOrder.POPULARITY)) {
				val label = if (order == null) name else "$name/POPULARITY"
				try {
					val list = factory.create(source).getList(0, order, null)
					println("PROBE $label: OK (${list.size} mangas)")
					if (order == null) ok++
				} catch (e: Exception) {
					val detail = e.message?.take(120) ?: "<no message>"
					println("PROBE $label: FAIL ${e.javaClass.simpleName}: $detail")
					if (order == null) errors[name] = e.javaClass.simpleName
				}
			}
		}
		val summary = errors.entries.groupingBy { it.value }.eachCount()
		println("PROBE RESULT: $ok/${selected.size} OK ; erreurs: $summary")
		println("PROBE RATE: ${ok * 100 / selected.size}%")
	}

	private fun isNetworkProbeEnabled(): Boolean =
		System.getProperty("runNetworkProbes") == "true" ||
			System.getenv("RUN_NETWORK_PROBES") == "true"
}
