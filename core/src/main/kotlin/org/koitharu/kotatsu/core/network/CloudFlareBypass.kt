/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Fichier nouveau : résolveur CloudFlare injectable pour le desktop. L'original délègue au
 * WebView Android (browser/cloudflare, module webview) qui n'existe pas ici : le stub ne résout
 * rien et laisse l'exception remonter vers l'UI (comme l'app Android sans résolution).
 */
package org.koitharu.kotatsu.core.network

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import org.koitharu.kotatsu.core.exceptions.CloudFlareBlockedException
import org.koitharu.kotatsu.core.exceptions.CloudFlareException
import org.koitharu.kotatsu.core.exceptions.CloudFlareProtectedException
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper

/**
 * Résolveur optionnel d'une protection CloudFlare. Sur Android, le WebView résout le challenge ;
 * sur desktop un vrai résolveur (navigateur externe, moteur embarqué) pourra l'implémenter plus tard.
 */
interface CloudFlareBypassResolver {

	/**
	 * Tente de résoudre la protection. Renvoie true si la protection est levée
	 * (cookies `cf_clearance` etc. écrits dans le CookieJar) et que la requête peut être réessayée.
	 */
	suspend fun resolve(url: String, source: MangaSource?, exception: CloudFlareException): Boolean
}

/** Stub sans WebView : ne résout jamais, l'exception CloudFlare est laissée remonter. */
object CloudFlareBypassStub : CloudFlareBypassResolver {

	override suspend fun resolve(url: String, source: MangaSource?, exception: CloudFlareException): Boolean = false
}

class CloudFlareInterceptor(
	private val bypassResolver: CloudFlareBypassResolver? = CloudFlareBypassStub,
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		var response = chain.proceed(request)
		when (CloudFlareHelper.checkResponseForProtection(response)) {
			CloudFlareHelper.PROTECTION_BLOCKED -> throw response.closeThrowing(
				CloudFlareBlockedException(
					url = request.url.toString(),
					source = request.tag(MangaSource::class.java),
				),
			)

			CloudFlareHelper.PROTECTION_CAPTCHA -> {
				val exception = CloudFlareProtectedException(
					url = request.url.toString(),
					source = request.tag(MangaSource::class.java),
					headers = request.headers,
				)
				if (bypassResolver != null && response.code in 400..499) {
					// Le résolveur (optionnel) peut tenter de lever la protection avant l'échec final.
					// Il ne doit pas bloquer la réponse : on referme et on laisse l'exception par défaut.
					response.close()
					throw exception
				}
				throw response.closeThrowing(exception)
			}

			else -> return response
		}
	}

	private fun Response.closeThrowing(error: IOException): Nothing {
		try {
			close()
		} catch (e: Exception) {
			error.addSuppressed(e)
		}
		throw error
	}
}
