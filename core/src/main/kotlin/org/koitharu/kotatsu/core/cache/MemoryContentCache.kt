/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : plus de dépendance Application/ComponentCallbacks2 — le cache est instancié
 * explicitement, et le trimming mémoire se fait via trim(level) appelé par l'hôte desktop.
 */
package org.koitharu.kotatsu.core.cache

import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaSource
import java.util.concurrent.TimeUnit

class MemoryContentCache(isLowRam: Boolean = false) {

	private val detailsCache = ExpiringLruCache<SafeDeferred<Manga>>(if (isLowRam) 1 else 4, 5, TimeUnit.MINUTES)
	private val pagesCache =
		ExpiringLruCache<SafeDeferred<List<MangaPage>>>(if (isLowRam) 1 else 4, 10, TimeUnit.MINUTES)
	private val relatedMangaCache =
		ExpiringLruCache<SafeDeferred<List<Manga>>>(if (isLowRam) 1 else 3, 10, TimeUnit.MINUTES)

	suspend fun getDetails(source: MangaSource, url: String): Manga? {
		return detailsCache[Key(source, url)]?.awaitOrNull()
	}

	fun putDetails(source: MangaSource, url: String, details: SafeDeferred<Manga>) {
		detailsCache[Key(source, url)] = details
	}

	suspend fun getPages(source: MangaSource, url: String): List<MangaPage>? {
		return pagesCache[Key(source, url)]?.awaitOrNull()
	}

	fun putPages(source: MangaSource, url: String, pages: SafeDeferred<List<MangaPage>>) {
		pagesCache[Key(source, url)] = pages
	}

	suspend fun getRelatedManga(source: MangaSource, url: String): List<Manga>? {
		return relatedMangaCache[Key(source, url)]?.awaitOrNull()
	}

	fun putRelatedManga(source: MangaSource, url: String, related: SafeDeferred<List<Manga>>) {
		relatedMangaCache[Key(source, url)] = related
	}

	fun clear(source: MangaSource) {
		clearCache(detailsCache, source)
		clearCache(pagesCache, source)
		clearCache(relatedMangaCache, source)
	}

	/**
	 * Equivalent desktop de onTrimMemory(level) : 10+ (critique/complet) vide tout,
	 * 5..9 réduit à 1 entrée, sinon divise la taille par deux.
	 */
	fun trim(level: Int) {
		for (cache in listOf(detailsCache, pagesCache, relatedMangaCache)) {
			when {
				level >= 10 -> cache.clear()
				level >= 5 -> cache.trimToSize(1)
				else -> cache.trimToSize(cache.maxSize / 2)
			}
		}
	}

	private fun clearCache(cache: ExpiringLruCache<*>, source: MangaSource) {
		cache.removeAll(source)
	}

	data class Key(
		val source: MangaSource,
		val url: String,
	)
}
