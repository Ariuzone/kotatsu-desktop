/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : version sans Hilt — la Factory est construite explicitement. Les dépôts hors parsers
 * (local, test, externes) sont fournis via la carte `extraRepositories` ; les dépôts de parsers sont
 * créés paresseusement depuis MangaParserSource.entries. LocalMangaRepository (module local/,
 * lot L2) n'est pas encore disponible : fourni en option par l'hôte desktop.
 */
package org.koitharu.kotatsu.core.parser

import androidx.collection.ArrayMap
import org.koitharu.kotatsu.core.cache.MemoryContentCache
import org.koitharu.kotatsu.core.model.LocalMangaSource
import org.koitharu.kotatsu.core.model.TestMangaSource
import org.koitharu.kotatsu.core.model.UnknownMangaSource
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaListFilterCapabilities
import org.koitharu.kotatsu.parsers.model.MangaListFilterOptions
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.SortOrder
import java.lang.ref.WeakReference

interface MangaRepository {

	val source: MangaSource

	val sortOrders: Set<SortOrder>

	var defaultSortOrder: SortOrder

	val filterCapabilities: MangaListFilterCapabilities

	suspend fun getList(offset: Int, order: SortOrder?, filter: MangaListFilter?): List<Manga>

	suspend fun getDetails(manga: Manga): Manga

	suspend fun getPages(chapter: MangaChapter): List<MangaPage>

	suspend fun getPageUrl(page: MangaPage): String

	suspend fun getFilterOptions(): MangaListFilterOptions

	suspend fun getRelated(seed: Manga): List<Manga>

	suspend fun find(manga: Manga): Manga? {
		val list = getList(0, SortOrder.RELEVANCE, MangaListFilter(query = manga.title))
		return list.find { x -> x.id == manga.id }
	}

	/**
	 * Fabrique sans DI : construit (ou reçoit) la carte des dépôts. Les sources [MangaParserSource]
	 * sont résolues paresseusement depuis l'enum ; les autres sources via [extraRepositories].
	 */
	class Factory(
		private val loaderContext: MangaLoaderContext,
		private val contentCache: MemoryContentCache,
		private val mirrorSwitcher: MirrorSwitcher,
		private val localMangaRepository: MangaRepository? = null,
		private val extraRepositories: Map<MangaSource, MangaRepository> = emptyMap(),
	) {

		private val cache = ArrayMap<MangaSource, WeakReference<MangaRepository>>()

		fun create(source: MangaSource): MangaRepository {
			when (source) {
				LocalMangaSource -> localMangaRepository?.let { return it }
				UnknownMangaSource -> return EmptyMangaRepository(source)
				TestMangaSource -> return extraRepositories[source] ?: EmptyMangaRepository(source)
			}
			extraRepositories[source]?.let { return it }
			cache[source]?.get()?.let { return it }
			return synchronized(cache) {
				cache[source]?.get()?.let { return it }
				val repository = createRepository(source)
				if (repository != null) {
					cache[source] = WeakReference(repository)
					repository
				} else {
					EmptyMangaRepository(source)
				}
			}
		}

		private fun createRepository(source: MangaSource): MangaRepository? = when (source) {
			is MangaParserSource -> ParserMangaRepository(
				parser = loaderContext.newParserInstance(source),
				cache = contentCache,
				mirrorSwitcher = mirrorSwitcher,
			)

			else -> null
		}
	}
}
