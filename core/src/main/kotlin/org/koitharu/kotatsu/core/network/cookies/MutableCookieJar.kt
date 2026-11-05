/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : androidx.core.util.Predicate remplacé par java.util.function.Predicate.
 */
package org.koitharu.kotatsu.core.network.cookies

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.function.Predicate

interface MutableCookieJar : CookieJar {

	override fun loadForRequest(url: HttpUrl): List<Cookie>

	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>)

	fun removeCookies(url: HttpUrl, predicate: Predicate<Cookie>?)

	suspend fun clear(): Boolean
}
