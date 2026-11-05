/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : la partie WebView (ProxyController androidx.webkit) est retirée — sur desktop le
 * proxy JVM d'OkHttp (selector + authenticator) couvre tout ; plus de DI Hilt.
 */
package org.koitharu.kotatsu.core.network.proxy

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okio.IOException
import org.koitharu.kotatsu.core.exceptions.ProxyConfigException
import org.koitharu.kotatsu.core.network.CommonHeaders
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.net.Authenticator as JavaAuthenticator

class ProxyProvider(
	private val settings: AppSettings,
) {

	private var cachedProxy: Proxy? = null

	val selector = object : ProxySelector() {
		override fun select(uri: URI?): List<Proxy> {
			return listOf(getProxy())
		}

		override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
			ioe?.printStackTraceDebug()
		}
	}

	val authenticator = ProxyAuthenticator()

	private fun isProxyEnabled() = settings.proxyType != Proxy.Type.DIRECT

	private fun getProxy(): Proxy {
		val type = settings.proxyType
		val address = settings.proxyAddress
		val port = settings.proxyPort
		if (type == Proxy.Type.DIRECT) {
			return Proxy.NO_PROXY
		}
		if (address.isNullOrEmpty() || port < 0 || port > 0xFFFF) {
			throw ProxyConfigException()
		}
		cachedProxy?.let {
			val addr = it.address() as? InetSocketAddress
			if (addr != null && it.type() == type && addr.port == port && addr.hostString == address) {
				return it
			}
		}
		val proxy = Proxy(type, InetSocketAddress(address, port))
		cachedProxy = proxy
		return proxy
	}

	inner class ProxyAuthenticator : Authenticator, JavaAuthenticator() {

		override fun authenticate(route: Route?, response: Response): Request? {
			if (!isProxyEnabled()) {
				return null
			}
			if (response.request.header(CommonHeaders.PROXY_AUTHORIZATION) != null) {
				return null
			}
			val login = settings.proxyLogin ?: return null
			val password = settings.proxyPassword ?: return null
			val credential = Credentials.basic(login, password)
			return response.request.newBuilder()
				.header(CommonHeaders.PROXY_AUTHORIZATION, credential)
				.build()
		}

		public override fun getPasswordAuthentication(): PasswordAuthentication? {
			if (!isProxyEnabled()) {
				return null
			}
			val login = settings.proxyLogin ?: return null
			val password = settings.proxyPassword ?: return null
			return PasswordAuthentication(login, password.toCharArray())
		}
	}
}
