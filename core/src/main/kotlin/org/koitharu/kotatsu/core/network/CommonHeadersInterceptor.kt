/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — fallback sans fabrique.
 * Adaptation : plus de Hilt — la fabrique de dépôts et le User-Agent par défaut sont fournis par
 * des lambdas injectables ; BuildConfig.DEBUG retiré ; Lazy de dagger remplacé par un appel paresseux.
 * Nouveau (desktop) : quand la fabrique de dépôts n'est pas câblée (hébergé desktop qui n'injecte
 * pas repositoryFactoryProvider), les en-têtes par source (User-Agent du parser, Referer, en-têtes
 * personnalisés) sont résolus via MangaLoaderContextImpl.globalInstance — fidèle au comportement
 * Android où l'intercepteur retrouve toujours le parser de la source.
 */
package org.koitharu.kotatsu.core.network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.koitharu.kotatsu.core.model.MangaSource
import org.koitharu.kotatsu.core.parser.MangaLoaderContextImpl
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.parser.ParserMangaRepository
import org.koitharu.kotatsu.core.prefs.SourceSettings
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.util.mergeWith
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import java.net.IDN

class CommonHeadersInterceptor(
	private val repositoryFactoryProvider: (() -> MangaRepository.Factory)?,
	private val defaultUserAgentProvider: () -> String,
) : Interceptor {

	override fun intercept(chain: Chain): Response {
		val request = chain.request()
		val source = request.tag(MangaSource::class.java)
			?: request.headers[CommonHeaders.MANGA_SOURCE]?.let { MangaSource(it) }
		val sourceInfo = resolveSourceInfo(source)
		val headersBuilder = request.headers.newBuilder()
			.removeAll(CommonHeaders.MANGA_SOURCE)
		sourceInfo?.requestHeaders?.let {
			headersBuilder.mergeWith(it, replaceExisting = false)
		}
		// Overrides the parser's agent, deliberately: a cf_clearance cookie is only honoured for the
		// exact User-Agent it was issued to, so once a challenge has been solved the request must
		// present that same identity or it stays blocked forever. See SourceSettings.cloudFlareUserAgent.
		sourceInfo?.cloudFlareUserAgent?.let {
			headersBuilder.trySet(CommonHeaders.USER_AGENT, it)
		}
		if (headersBuilder[CommonHeaders.USER_AGENT] == null) {
			headersBuilder[CommonHeaders.USER_AGENT] = defaultUserAgentProvider()
		}
		if (headersBuilder[CommonHeaders.REFERER] == null && sourceInfo?.domain != null) {
			val idn = IDN.toASCII(sourceInfo.domain)
			headersBuilder.trySet(CommonHeaders.REFERER, "https://$idn/")
		}
		val newRequest = request.newBuilder().headers(headersBuilder.build()).build()
		return sourceInfo?.interceptSafe(ProxyChain(chain, newRequest)) ?: chain.proceed(newRequest)
	}

	private fun resolveSourceInfo(source: MangaSource?): SourceInfo? {
		if (source !is MangaParserSource) return null
		val repository = repositoryFactoryProvider?.invoke()?.create(source) as? ParserMangaRepository
		if (repository != null) {
			return SourceInfo(
				requestHeaders = repository.getRequestHeaders(),
				cloudFlareUserAgent = repository.getConfig().cloudFlareUserAgent,
				domain = repository.domain,
				delegate = repository,
			)
		}
		// Fallback desktop : fabrique de dépôts non câblée — parser brut via le contexte global.
		val context = MangaLoaderContextImpl.globalInstance ?: return null
		val parser = context.parserFor(source)
		return SourceInfo(
			requestHeaders = parser.getRequestHeaders(),
			cloudFlareUserAgent = (parser.config as? SourceSettings)?.cloudFlareUserAgent,
			domain = parser.domain,
			delegate = parser,
		)
	}

	private class SourceInfo(
		val requestHeaders: Headers?,
		val cloudFlareUserAgent: String?,
		val domain: String?,
		private val delegate: Interceptor,
	) {

		fun interceptSafe(chain: Chain): Response = delegate.interceptSafe(chain)
	}

	private fun Headers.Builder.trySet(name: String, value: String) = try {
		set(name, value)
	} catch (e: IllegalArgumentException) {
		e.printStackTraceDebug()
	}

	private class ProxyChain(
		private val delegate: Chain,
		private val request: Request,
	) : Chain by delegate {

		override fun request(): Request = request
	}
}

/** only IOException can be safely thrown from an Interceptor */
private fun Interceptor.interceptSafe(chain: Chain): Response = runCatchingCancellable {
	intercept(chain)
}.getOrElse { e ->
	if (e is IOException || e is Error) {
		throw e
	} else {
		throw IOException("Error in interceptor: ${e.message}", e)
	}
}
