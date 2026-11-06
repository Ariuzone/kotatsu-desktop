/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — contexte global.
 * Adaptation : pas d'Android Context — le User-Agent est un UA desktop constant ; les stores de
 * SourceSettings viennent d'un SourceSettingsStoreProvider injecté ; base64 en java.util ;
 * evaluateJs/WebView : stubs (null / listes vides) ; redrawImageResponse/createBitmap : non
 * supportés sur JVM (UnsupportedOperationException).
 * Nouveau (desktop) : l'instance s'enregistre dans [globalInstance] et expose [parserFor] —
 * permet à CommonHeadersInterceptor (core/network) de retrouver les en-têtes par source
 * (User-Agent du parser, Referer) même quand la fabrique de dépôts n'est pas câblée, faute de DI.
 */
package org.koitharu.kotatsu.core.parser

import okhttp3.OkHttpClient
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.koitharu.kotatsu.core.exceptions.CloudFlareProtectedException
import org.koitharu.kotatsu.core.exceptions.InteractiveActionRequiredException
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import org.koitharu.kotatsu.core.prefs.SourceSettings
import org.koitharu.kotatsu.core.prefs.SourceSettingsStoreProvider
import org.koitharu.kotatsu.core.util.ext.toMimeType
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaParser
import org.koitharu.kotatsu.parsers.bitmap.Bitmap
import org.koitharu.kotatsu.parsers.config.MangaSourceConfig
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.network.UserAgents
import org.koitharu.kotatsu.parsers.util.map
import org.koitharu.kotatsu.parsers.webview.InterceptedRequest
import java.util.Base64
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class MangaLoaderContextImpl(
	override val httpClient: OkHttpClient,
	override val cookieJar: MutableCookieJar,
	private val sourceSettingsStoreProvider: SourceSettingsStoreProvider,
) : MangaLoaderContext() {

	init {
		// Fallback desktop : sans DI, le réseau doit pouvoir retrouver le contexte courant.
		globalInstance = this
	}

	override suspend fun evaluateJs(baseUrl: String, script: String, timeout: Long): String? = null

	@Deprecated("Provide a base url")
	override suspend fun evaluateJs(script: String): String? = null

	override fun getDefaultUserAgent(): String = UserAgents.FIREFOX_MOBILE

	override fun getConfig(source: MangaSource): MangaSourceConfig {
		return SourceSettings(sourceSettingsStoreProvider(source), source)
	}

	override fun encodeBase64(data: ByteArray): String {
		return Base64.getEncoder().encodeToString(data)
	}

	override fun decodeBase64(data: String): ByteArray {
		return Base64.getDecoder().decode(data)
	}

	override fun getPreferredLocales(): List<Locale> {
		return listOf(Locale.getDefault())
	}

	override fun requestBrowserAction(
		parser: MangaParser,
		url: String,
	): Nothing {
		if (parser.source == MangaParserSource.COMIX) {
			throw CloudFlareProtectedException(url, parser.source, parser.getRequestHeaders())
		}
		throw InteractiveActionRequiredException(parser.source, url)
	}

	override fun redrawImageResponse(response: Response, redraw: (image: Bitmap) -> Bitmap): Response {
		// Le décodage/redessin d'image (Android Bitmap) n'est pas porté côté JVM pur.
		throw UnsupportedOperationException("Image redrawing is not supported on desktop")
	}

	override fun createBitmap(width: Int, height: Int): Bitmap {
		throw UnsupportedOperationException("Bitmap creation is not supported on desktop")
	}

	override suspend fun interceptWebViewRequests(
		url: String,
		interceptorScript: String,
		timeout: Long,
	): List<InterceptedRequest> = emptyList()

	override suspend fun captureWebViewUrls(
		pageUrl: String,
		urlPattern: Regex,
		timeout: Long,
	): List<String> = emptyList()

	/** Parser brut pour une source (mis en cache) — fallback réseau sans fabrique de dépôts. */
	fun parserFor(source: MangaParserSource): MangaParser =
		cachedParsers.getOrPut(source.name) { newParserInstance(source) }

	/**
	 * Source dont le domaine correspond au host donné (images sans tag, ex. Coil :
	 * uploads.mangadex.org -> MANGADEX). Index domaine -> source construit paresseusement
	 * à partir des domaines prédéfinis des parsers. Sous-domaines acceptés
	 * (uploads.mangadex.org correspond à mangadex.org).
	 * Les CDN d'images hébergés hors du domaine de la source passent par [CDN_HOST_ALIASES].
	 */
	fun sourceForDomain(host: String): MangaParserSource? {
		val normalized = host.lowercase(Locale.ROOT).removePrefix("www.")
		return domainIndex[normalized]
			?: domainIndex.entries.firstOrNull { normalized.endsWith(".${it.key}") }?.value
			?: cdnAliases[normalized]
			?: cdnAliases.entries.firstOrNull { normalized.endsWith(".${it.key}") }?.value
	}

	/**
	 * CDN d'images hébergés sur un domaine tiers, non déductible du domaine de la source.
	 * Sans entrée ici, ces covers partent avec l'User-Agent par défaut et sans Referer,
	 * et certains CDN répondent 403 (Naver exige le Referer webtoons.com ; CloudFlare
	 * bloque fmcdn.mangahere.com et cdn1.comicknew.pictures sans l'identité du parser).
	 */
	private val cdnAliases: Map<String, MangaParserSource> by lazy {
		val aliases = mapOf(
			// Covers Comick, derrière CloudFlare (UA parser + Referer comick.live requis)
			"comicknew.pictures" to "COMICK_FUN",
			// fmcdn.mangahere.com : héberge les covers sous .com alors que la source vit en .cc
			"mangahere.com" to "MANGAHERE",
			// CDN Naver des covers Webtoons (403 sans Referer https://webtoons.com/)
			"pstatic.net" to "WEBTOONS_EN",
		)
		aliases.mapValues { (_, name) -> MangaParserSource.entries.firstOrNull { it.name == name } }
			.filterValues { it != null }
			.mapValues { it.value!! }
	}

	private val domainIndex: Map<String, MangaParserSource> by lazy {
		// ~1400 parsers à instancier une seule fois (~3 s séquentiel) : parallélisé pour
		// contenir le coût du premier chargement d'image (lookup ensuite quasi gratuit).
		runBlocking {
			val index = java.util.concurrent.ConcurrentHashMap<String, MangaParserSource>()
			MangaParserSource.entries
				.chunked(64)
				.map { chunk ->
					async(Dispatchers.Default) {
						for (source in chunk) {
							runCatching {
								for (domain in parserFor(source).configKeyDomain.presetValues) {
									val key = domain.lowercase(Locale.ROOT).removePrefix("www.")
									index.putIfAbsent(key, source)
								}
							}
						}
					}
				}
				.awaitAll()
			index
		}
	}

	private val cachedParsers = ConcurrentHashMap<String, MangaParser>()

	companion object {

		/** Dernière instance créée — équivalent desktop du singleton Hilt Android. */
		@Volatile
		var globalInstance: MangaLoaderContextImpl? = null
			private set
	}
}
