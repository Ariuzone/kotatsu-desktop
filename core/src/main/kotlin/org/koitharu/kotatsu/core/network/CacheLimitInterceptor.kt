/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.network

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheLimitInterceptor : Interceptor {

	private val defaultMaxAge = TimeUnit.HOURS.toSeconds(1)
	private val defaultCacheControl = CacheControl.Builder()
		.maxAge(defaultMaxAge.toInt(), TimeUnit.SECONDS)
		.build()
		.toString()

	override fun intercept(chain: Interceptor.Chain): Response {
		val response = chain.proceed(chain.request())
		val responseCacheControl = CacheControl.parse(response.headers)
		if (responseCacheControl.noStore || responseCacheControl.maxAgeSeconds <= defaultMaxAge) {
			return response
		}
		return response.newBuilder()
			.header(CommonHeaders.CACHE_CONTROL, defaultCacheControl)
			.build()
	}
}
