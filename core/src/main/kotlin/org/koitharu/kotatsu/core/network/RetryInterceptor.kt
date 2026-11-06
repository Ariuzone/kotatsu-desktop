/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — intercepteur desktop.
 * Fichier nouveau : réessai court (backoff) pour les erreurs réseau transitoires, dans l'esprit du
 * RateLimitInterceptor d'origine (gestion gracieuse des erreurs côté serveur). Sélectif : seuls les
 * codes 502/503/504/522/524 et les IOException transitoires (timeout, connexion réinitialisée)
 * sont retentés — jamais les 4xx, les challenges CloudFlare, ni les domaines morts.
 */
package org.koitharu.kotatsu.core.network

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import org.koitharu.kotatsu.core.exceptions.DeadDomainException
import org.koitharu.kotatsu.parsers.exception.TooManyRequestExceptions
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.math.min

class RetryInterceptor(
	private val maxRetries: Int = DEFAULT_MAX_RETRIES,
) : Interceptor {

	init {
		require(maxRetries in 1..2) { "maxRetries must be 1 or 2, got $maxRetries" }
	}

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		var attempt = 0
		while (true) {
			var response: Response? = null
			try {
				response = chain.proceed(request)
				if (response.code !in RETRYABLE_CODES || attempt >= maxRetries) {
					return response
				}
				response.close()
			} catch (e: IOException) {
				if (attempt >= maxRetries || !e.isTransient()) {
					throw e
				}
			}
			attempt++
			sleepQuietly(BACKOFF_BASE_MS shl min(attempt - 1, 4))
		}
	}

	/** Échecs réseau qui valent un réessai : transitoires par nature. */
	private fun IOException.isTransient(): Boolean = when (this) {
		// Domaine mort ou problème de TLS : réessayer ne change rien.
		is UnknownHostException,
		is SSLException,
		is DeadDomainException,
		// Rate limit : le RateLimitInterceptor lève avec un Retry-After à honorer, pas un backoff aveugle.
		is TooManyRequestExceptions,
		-> false

		is SocketTimeoutException,
		is ConnectException,
		is NoRouteToHostException,
		is java.net.SocketException,
		is java.net.ProtocolException,
		-> true

		else -> cause?.let { (it as? IOException)?.isTransient() } ?: false
	}

	private fun sleepQuietly(millis: Long) {
		try {
			Thread.sleep(millis)
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
		}
	}

	companion object {

		const val DEFAULT_MAX_RETRIES = 1
		private const val BACKOFF_BASE_MS = 400L

		/** Codes serveur transitoires : mauvaise passerelle, surcharge, origin CloudFlare down. */
		private val RETRYABLE_CODES = intArrayOf(502, 503, 504, 522, 524)
	}
}
