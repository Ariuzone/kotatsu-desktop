/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — classification desktop.
 * Fichier nouveau : classe les échecs réseau en exceptions typées pour l'UI desktop. Un
 * UnknownHostException (après fallback DoH de DoHManager) signifie que le domaine de la source
 * n'existe plus : il est re-levé comme DeadDomainException afin que l'UI distingue un domaine
 * mort d'un échec temporaire et n'affiche pas « réessayer » pour une source définitivement perdue.
 */
package org.koitharu.kotatsu.core.network

import okhttp3.Interceptor
import okhttp3.Response
import org.koitharu.kotatsu.core.exceptions.DeadDomainException
import java.net.UnknownHostException

class ErrorClassificationInterceptor : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		try {
			return chain.proceed(chain.request())
		} catch (e: UnknownHostException) {
			val hostname = chain.request().url.host.ifEmpty { e.message.orEmpty() }
			throw DeadDomainException(
				hostname = hostname,
				message = "Domain is unreachable or no longer exists: $hostname",
				cause = e,
			)
		}
	}
}
