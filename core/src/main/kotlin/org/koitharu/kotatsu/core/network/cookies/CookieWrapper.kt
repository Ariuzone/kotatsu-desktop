/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : android.util.Base64 remplacé par java.util.Base64.
 */
package org.koitharu.kotatsu.core.network.cookies

import okhttp3.Cookie
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64

data class CookieWrapper(
	val cookie: Cookie,
) {

	constructor(encodedString: String) : this(
		ObjectInputStream(ByteArrayInputStream(Base64.getDecoder().decode(encodedString))).use {
			val name = it.readUTF()
			val value = it.readUTF()
			val expiresAt = it.readLong()
			val domain = it.readUTF()
			val path = it.readUTF()
			val secure = it.readBoolean()
			val httpOnly = it.readBoolean()
			val persistent = it.readBoolean()
			val hostOnly = it.readBoolean()
			Cookie.Builder().also { c ->
				c.name(name)
				c.value(value)
				if (persistent) {
					c.expiresAt(expiresAt)
				}
				if (hostOnly) {
					c.hostOnlyDomain(domain)
				} else {
					c.domain(domain)
				}
				c.path(path)
				if (secure) {
					c.secure()
				}
				if (httpOnly) {
					c.httpOnly()
				}
			}.build()
		},
	)

	fun encode(): String {
		val output = ByteArrayOutputStream()
		ObjectOutputStream(output).use {
			it.writeUTF(cookie.name)
			it.writeUTF(cookie.value)
			it.writeLong(cookie.expiresAt)
			it.writeUTF(cookie.domain)
			it.writeUTF(cookie.path)
			it.writeBoolean(cookie.secure)
			it.writeBoolean(cookie.httpOnly)
			it.writeBoolean(cookie.persistent)
			it.writeBoolean(cookie.hostOnly)
		}
		return Base64.getEncoder().encodeToString(output.toByteArray())
	}

	fun isExpired() = cookie.expiresAt < System.currentTimeMillis()

	fun key(): String {
		return (if (cookie.secure) "https" else "http") + "://" + cookie.domain + cookie.path + "|" + cookie.name
	}
}
