/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : version sans Hilt de NetworkModule — classe explicite exposant `baseHttpClient`
 * (qualifier @BaseHttpClient) et `mangaHttpClient` (qualifier @MangaHttpClient) en simples vals.
 * Le cache HTTP est créé ici si [cacheDir] est fourni ; le CookieJar persistant est créé
 * automatiquement à partir du [cookieStore] (JSON desktop) si non fourni.
 * Les fournisseurs pour CommonHeadersInterceptor (fabrique de dépôts, User-Agent par défaut)
 * sont injectables — câblés par l'hôte desktop une fois le parser prêt.
 */
package org.koitharu.kotatsu.core.network

import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.koitharu.kotatsu.core.network.cookies.FileCookieJar
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import org.koitharu.kotatsu.core.network.proxy.ProxyProvider
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.KeyValueStore
import org.koitharu.kotatsu.core.util.ext.assertNotInMainThread
import org.koitharu.kotatsu.parsers.network.UserAgents
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkModule(
	private val settings: AppSettings,
	cacheDir: File?,
	cookieStore: KeyValueStore,
	cookieJar: MutableCookieJar? = null,
	private val cloudFlareBypassResolver: CloudFlareBypassResolver? = CloudFlareBypassStub,
	private val repositoryFactoryProvider: (() -> org.koitharu.kotatsu.core.parser.MangaRepository.Factory)? = null,
	defaultUserAgentProvider: () -> String = { UserAgents.FIREFOX_MOBILE },
) {

	val proxyProvider: ProxyProvider = ProxyProvider(settings)

	val cookieJar: MutableCookieJar = cookieJar ?: FileCookieJar(cookieStore)

	val cache: Cache? = cacheDir?.let { dir ->
		dir.mkdirs()
		Cache(dir, CACHE_SIZE_BYTES)
	}

	private val commonHeadersInterceptor = CommonHeadersInterceptor(
		repositoryFactoryProvider,
		defaultUserAgentProvider,
	)

	/** Qualifier desktop pour l'ancien `@BaseHttpClient`. */
	val baseHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
		assertNotInMainThread()
		connectTimeout(20, TimeUnit.SECONDS)
		readTimeout(60, TimeUnit.SECONDS)
		writeTimeout(20, TimeUnit.SECONDS)
		cookieJar(cookieJar as CookieJar)
		proxySelector(proxyProvider.selector)
		proxyAuthenticator(proxyProvider.authenticator)
		dns(DoHManager(cache, settings))
		if (settings.isSSLBypassEnabled) {
			disableCertificateVerification()
		}
		if (cache != null) {
			cache(cache)
		}
		addInterceptor(CloudFlareInterceptor(cloudFlareBypassResolver))
		addInterceptor(RateLimitInterceptor())
	}.build()

	/** Qualifier desktop pour l'ancien `@MangaHttpClient`. */
	val mangaHttpClient: OkHttpClient = baseHttpClient.newBuilder().apply {
		addNetworkInterceptor(CacheLimitInterceptor())
		addInterceptor(commonHeadersInterceptor)
	}.build()

	companion object {

		const val CACHE_SIZE_BYTES = 20L * 1024 * 1024
	}
}
