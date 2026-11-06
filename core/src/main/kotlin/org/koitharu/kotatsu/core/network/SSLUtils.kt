/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — trust store étendu.
 * Adaptation : disableCertificateVerification est porté (JVM pur). installExtraCertificates est
 * réintroduit en version desktop, fidèle à l'esprit Android (l'app d'origine étend le trust aux
 * .pem embarqués) : on complète le trust store JVM (cacerts du JBR, en retard sur les racines
 * récentes — ex. ISRG Root YE de Let's Encrypt, utilisée par mangapark.io) avec :
 * 1) les certificats racine embarqués dans les ressources (certs/, indexés par certs/index.txt) ;
 * 2) le magasin système Windows-ROOT via SunMSCAPI quand il existe (JDK standard sous Windows ;
 *    no-op propre sur les JBR qui l'omettent, ou hors Windows).
 * Si rien ne peut être ajouté, le client garde le trust JVM par défaut (return false, sans erreur).
 */
package org.koitharu.kotatsu.core.network

import okhttp3.OkHttpClient
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.disableCertificateVerification() = also { builder ->
	runCatching {
		val trustAllCerts = object : X509TrustManager {
			override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit

			override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

			override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
		}
		val sslContext = SSLContext.getInstance("SSL")
		sslContext.init(null, arrayOf(trustAllCerts), SecureRandom())
		val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
		builder.sslSocketFactory(sslSocketFactory, trustAllCerts)
		builder.hostnameVerifier { _, _ -> true }
	}.onFailure {
		it.printStackTraceDebug()
	}
}

/**
 * Étend la vérification TLS au-delà du trust store JVM : racines embarquées (certs/) et
 * magasin système quand disponible. Renvoie true si l'installation a eu lieu. Fidèle à l'app
 * Android, qui installe des certificats supplémentaires quand le bypass SSL est désactivé.
 */
fun OkHttpClient.Builder.installExtraCertificates(): Boolean = runCatching {
	val delegates = buildList {
		add(createDefaultTrustManager())
		createBundledTrustManager()?.let { add(it) }
		createPlatformTrustManager()?.let { add(it) }
	}
	if (delegates.size <= 1) {
		// Rien à ajouter (pas de ressources, pas de magasin système) : trust JVM par défaut.
		return@runCatching false
	}
	val composite = CompositeTrustManager(delegates)
	val sslContext = SSLContext.getInstance("TLS")
	sslContext.init(null, arrayOf(composite), SecureRandom())
	sslSocketFactory(sslContext.socketFactory, composite)
	true
}.onFailure {
	it.printStackTraceDebug()
}.getOrDefault(false)

/** Trust manager construit sur les racines .pem embarquées dans les ressources (certs/index.txt). */
private fun createBundledTrustManager(): X509TrustManager? {
	val classLoader = Thread.currentThread().contextClassLoader
		?: CompositeTrustManager::class.java.classLoader
		?: return null
	val indexUrl = classLoader.getResource("$CERTS_RESOURCE_DIR/index.txt") ?: return null
	return runCatching {
		val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
		val certificateFactory = CertificateFactory.getInstance("X.509")
		var count = 0
		indexUrl.openStream().bufferedReader().useLines { lines ->
			for (name in lines) {
				val trimmed = name.trim()
				if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
				val certUrl = classLoader.getResource("$CERTS_RESOURCE_DIR/$trimmed") ?: continue
				val cert = certUrl.openStream().use { certificateFactory.generateCertificate(it) } as X509Certificate
				keyStore.setCertificateEntry("extra-$count", cert)
				count++
			}
		}
		if (count == 0) return null
		val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
		factory.init(keyStore)
		factory.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
	}.onFailure {
		it.printStackTraceDebug()
	}.getOrNull()
}

/**
 * Trust manager du magasin système : « Windows-ROOT » (SunMSCAPI) sur Windows, null ailleurs.
 * Sur macOS/Linux le trust JVM reste seul (comportement par défaut).
 */
private fun createPlatformTrustManager(): X509TrustManager? {
	val keyStoreName = when {
		isWindows() -> "Windows-ROOT"
		else -> return null
	}
	val keyStore = runCatching {
		KeyStore.getInstance(keyStoreName).apply { load(null, null) }
	}.getOrElse { e ->
		e.printStackTraceDebug()
		return null
	}
	val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
	factory.init(keyStore)
	return factory.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
}

private fun createDefaultTrustManager(): X509TrustManager {
	val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
	factory.init(null as KeyStore?)
	return factory.trustManagers.filterIsInstance<X509TrustManager>().first()
}

/**
 * Accepte un certificat s'il est validé par l'un des trust managers délégués :
 * le JVM (cacerts) d'abord, sinon le magasin système. Si les deux échouent, l'erreur
 * d'origine (JVM) est propagée pour ne pas masquer un vrai problème de TLS.
 */
private class CompositeTrustManager(
	private val delegates: List<X509TrustManager>,
) : X509TrustManager {

	constructor(vararg delegates: X509TrustManager) : this(delegates.toList())

	init {
		require(delegates.isNotEmpty()) { "At least one trust manager is required" }
	}

	override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
		delegates.first().checkClientTrusted(chain, authType)
	}

	override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
		var firstError: CertificateException? = null
		for (delegate in delegates) {
			try {
				delegate.checkServerTrusted(chain, authType)
				return
			} catch (e: CertificateException) {
				if (firstError == null) firstError = e
			}
		}
		throw firstError ?: CertificateException("No trust manager available")
	}

	override fun getAcceptedIssuers(): Array<X509Certificate> =
		delegates.flatMap { it.acceptedIssuers.toList() }.toTypedArray()
}

private fun isWindows(): Boolean =
	System.getProperty("os.name", "").lowercase(java.util.Locale.ROOT).contains("windows")

private const val CERTS_RESOURCE_DIR = "certs"

