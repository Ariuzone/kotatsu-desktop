/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : android.net.Uri remplacé par okhttp3.HttpUrl ; MangaDataRepository (module core/db,
 * lot L2) est découpé en une interface fonctionnelle [MangaLookup] injectée par l'hôte desktop ;
 * BuildConfig.APP_LINK_HOST remplacé par une constante locale.
 */
package org.koitharu.kotatsu.core.parser

import coil3.request.CachePolicy
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.koitharu.kotatsu.core.model.MangaSource
import org.koitharu.kotatsu.core.model.UnknownMangaSource
import org.koitharu.kotatsu.core.model.isNsfw
import org.koitharu.kotatsu.core.util.ext.isHttpUrl
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.exception.NotFoundException
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaSource as ParsersMangaSource
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.parsers.util.almostEquals
import org.koitharu.kotatsu.parsers.util.ifNullOrEmpty
import org.koitharu.kotatsu.parsers.util.levenshteinDistance
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable

interface MangaLookup {

	/** Recherche en base locale : manga par son identifiant, ou par son URL publique. */
	suspend fun findById(mangaId: Long): Manga?

	suspend fun findByPublicUrl(publicUrl: String): Manga?
}

class MangaLinkResolver(
	private val repositoryFactory: MangaRepository.Factory,
	private val dataLookup: MangaLookup,
	private val context: MangaLoaderContext,
) {

	suspend fun resolve(uri: HttpUrl): Manga {
		return if (uri.scheme == "kotatsu" || uri.host == "kotatsu.app" || uri.host == APP_LINK_HOST) {
			resolveAppLink(uri)
		} else {
			resolveExternalLink(uri.toString())
		} ?: throw NotFoundException("Cannot resolve link", uri.toString())
	}

	suspend fun resolve(uri: String): Manga {
		val httpUrl = uri.toHttpUrlOrNull()
		return if (httpUrl != null) {
			resolve(httpUrl)
		} else {
			resolveExternalLink(uri)
		} ?: throw NotFoundException("Cannot resolve link", uri)
	}

	private suspend fun resolveAppLink(uri: HttpUrl): Manga? {
		// "manga" is the host in kotatsu://manga?..., but the only path segment in
		// https://kotatsu.app/manga?... and in the internal kotatsu:/manga?id=... short links.
		require(uri.host == "manga" || uri.pathSegments.singleOrNull() == "manga") { "Invalid url" }
		uri.queryParameter("id")?.let { mangaId ->
			// short url
			return dataLookup.findById(mangaId.toLong())
		}
		val sourceName = requireNotNull(uri.queryParameter("source")) { "Source is not specified" }
		val source = MangaSource(sourceName)
		require(source != UnknownMangaSource) { "Manga source $sourceName is not supported" }
		val repo = repositoryFactory.create(source)
		return repo.findExact(
			url = uri.queryParameter("url"),
			title = uri.queryParameter("name"),
		)
	}

	private suspend fun resolveExternalLink(uri: String): Manga? {
		dataLookup.findByPublicUrl(uri)?.let {
			return it
		}
		return context.newLinkResolver(uri).getManga()
	}

	private suspend fun MangaRepository.findExact(url: String?, title: String?): Manga? {
		if (!title.isNullOrEmpty()) {
			val list = getList(0, null, MangaListFilter(query = title))
			if (url != null) {
				list.find { it.url == url }?.let {
					return it
				}
			}
			list.minByOrNull { it.title.levenshteinDistance(title) }
				?.takeIf { it.title.almostEquals(title, 0.2f) }
				?.let { return it }
		}
		// Fetching the details page for the exact url already yields the manga the link points at.
		val seed = getDetailsNoCache(
			getSeedManga(source, url ?: return null, title),
		)
		return runCatchingCancellable {
			val seedTitle = seed.title.ifEmpty {
				seed.altTitle
			}.ifNullOrEmpty {
				seed.author
			} ?: return@runCatchingCancellable null
			val seedList = getList(0, null, MangaListFilter(query = seedTitle))
			seedList.firstOrNull { x -> x.url == url }
		}.getOrNull()
			?: seed.publicUrl.takeIf { it.isNotEmpty() }?.let { dataLookup.findByPublicUrl(it) }
			?: seed
	}

	/**
	 * The manga at [url] on [source], from its own details page. The result stays out of the
	 * in-memory details cache (same rationale as the Android original).
	 */
	suspend fun resolveExact(source: ParsersMangaSource, url: String, title: String?): Manga {
		val manga = repositoryFactory.create(source).getDetailsNoCache(getSeedManga(source, url, title))
		return manga.publicUrl.takeIf { it.isNotEmpty() }?.let { dataLookup.findByPublicUrl(it) } ?: manga
	}

	/** The manga at [url] on [source] if it can be had without a request, with what it takes to show it. */
	suspend fun findKnown(source: ParsersMangaSource, url: String, title: String?): Manga? {
		val seed = getSeedManga(source, url, title)
		(repositoryFactory.create(source) as? CachingMangaRepository)?.peekDetails(seed)?.let { return it }
		return dataLookup.findById(seed.id)?.takeUnless { it.coverUrl.isNullOrEmpty() }
	}

	/** A manga to show from what the server knows of it. Everything else comes from the site when it is opened. */
	fun fromServer(source: ParsersMangaSource, url: String, title: String?, coverUrl: String, genres: Collection<String>): Manga =
		getSeedManga(source, url, title).copy(
			coverUrl = coverUrl,
			tags = genres.mapTo(LinkedHashSet()) { genre ->
				MangaTag(title = genre.replaceFirstChar(Char::uppercase), key = genre, source = source)
			},
		)

	private suspend fun MangaRepository.getDetailsNoCache(manga: Manga): Manga = if (this is CachingMangaRepository) {
		getDetails(manga, CachePolicy.READ_ONLY)
	} else {
		getDetails(manga)
	}

	private fun getSeedManga(source: ParsersMangaSource, url: String, title: String?) = Manga(
		id = run {
			var h = 1125899906842597L
			source.name.forEach { c ->
				h = 31 * h + c.code
			}
			url.forEach { c ->
				h = 31 * h + c.code
			}
			h
		},
		title = title.orEmpty(),
		altTitle = null,
		url = url,
		publicUrl = "",
		rating = 0.0f,
		isNsfw = source.isNsfw(),
		coverUrl = "",
		tags = emptySet(),
		state = null,
		author = null,
		largeCoverUrl = null,
		description = null,
		chapters = null,
		source = source,
	)

	companion object {

		const val APP_LINK_HOST = "kotatsu.app"

		fun isValidLink(str: String): Boolean {
			return str.isHttpUrl() || str.startsWith("kotatsu://", ignoreCase = true)
		}
	}
}
