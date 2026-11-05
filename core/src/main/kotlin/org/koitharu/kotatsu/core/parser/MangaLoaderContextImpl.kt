/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : pas d'Android Context — le User-Agent est un UA desktop constant ; les stores de
 * SourceSettings viennent d'un SourceSettingsStoreProvider injecté ; base64 en java.util ;
 * evaluateJs/WebView : stubs (null / listes vides) ; redrawImageResponse/createBitmap : non
 * supportés sur JVM (UnsupportedOperationException).
 */
package org.koitharu.kotatsu.core.parser

import okhttp3.OkHttpClient
import okhttp3.Response
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

class MangaLoaderContextImpl(
	override val httpClient: OkHttpClient,
	override val cookieJar: MutableCookieJar,
	private val sourceSettingsStoreProvider: SourceSettingsStoreProvider,
) : MangaLoaderContext() {

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
}
