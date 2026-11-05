/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation de PreferencesCookieJar (Android) : la persistance passe par [KeyValueStore] (JSON
 * sur disque) au lieu des SharedPreferences Android. Même logique de cache mémoire + encodage.
 */
package org.koitharu.kotatsu.core.network.cookies

import androidx.collection.ArrayMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.koitharu.kotatsu.core.prefs.KeyValueStore
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import java.util.function.Predicate

class FileCookieJar(
	private val store: KeyValueStore,
) : MutableCookieJar {

	private val cache = ArrayMap<String, CookieWrapper>()
	private var isLoaded = false

	override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(this) {
		loadPersistent()
		val expired = HashSet<String>()
		val result = ArrayList<Cookie>()
		for ((key, cookie) in cache) {
			if (cookie.isExpired()) {
				expired += key
			} else if (cookie.cookie.matches(url)) {
				result += cookie.cookie
			}
		}
		if (expired.isNotEmpty()) {
			for (key in expired) {
				cache.remove(key)
			}
			removePersistent(expired)
		}
		result
	}

	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		val wrapped = cookies.map { CookieWrapper(it) }
		synchronized(this) {
			store.edit(commit = true) {
				for (cookie in wrapped) {
					val key = cookie.key()
					cache[key] = cookie
					if (cookie.cookie.persistent) {
						putString(key, cookie.encode())
					}
				}
			}
		}
	}

	override fun removeCookies(url: HttpUrl, predicate: Predicate<Cookie>?) {
		synchronized(this) {
			loadPersistent()
			val toRemove = HashSet<String>()
			for ((key, cookie) in cache) {
				if (cookie.isExpired() || cookie.cookie.matches(url)) {
					if (predicate == null || predicate.test(cookie.cookie)) {
						toRemove += key
					}
				}
			}
			if (toRemove.isNotEmpty()) {
				for (key in toRemove) {
					cache.remove(key)
				}
				removePersistent(toRemove)
			}
		}
	}

	override suspend fun clear(): Boolean {
		synchronized(this) {
			cache.clear()
		}
		withContext(Dispatchers.IO) {
			store.edit(commit = true) {
				clear()
			}
		}
		return true
	}

	private fun loadPersistent() {
		if (!isLoaded) {
			val map = store.all
			for ((k, v) in map) {
				val cookie = try {
					CookieWrapper(v as String)
				} catch (e: Exception) {
					e.printStackTraceDebug()
					continue
				}
				cache[k] = cookie
			}
			isLoaded = true
		}
	}

	private fun removePersistent(keys: Collection<String>) {
		store.edit(commit = true) {
			for (key in keys) {
				remove(key)
			}
		}
	}
}
