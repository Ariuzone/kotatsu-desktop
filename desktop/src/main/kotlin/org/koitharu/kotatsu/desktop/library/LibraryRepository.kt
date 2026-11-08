/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation Compose Desktop.
 *
 * Façade bibliothèque (favoris + historique) — remplace les use cases Android
 * (GetHistoryUseCase, GetFavouritesUseCase, SaveHistoryUseCase…) par un accès direct aux DAO
 * Room portés (mêmes tables et même schéma que l'app Android).
 */
package org.koitharu.kotatsu.desktop.library

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.entity.toEntities
import org.koitharu.kotatsu.core.db.entity.toEntity
import org.koitharu.kotatsu.core.db.entity.toManga
import org.koitharu.kotatsu.core.db.entity.toMangaTags
import org.koitharu.kotatsu.favourites.data.FavouriteCategoryEntity
import org.koitharu.kotatsu.favourites.data.FavouriteEntity
import org.koitharu.kotatsu.favourites.data.FavouriteManga
import org.koitharu.kotatsu.history.data.HistoryEntity
import org.koitharu.kotatsu.history.data.HistoryWithManga
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter

public class LibraryRepository(
	private val db: MangaDatabase,
) {

	private val historyDao = db.getHistoryDao()
	private val favouritesDao = db.getFavouritesDao()
	private val categoriesDao = db.getFavouriteCategoriesDao()
	private val mangaDao = db.getMangaDao()
	private val tagsDao = db.getTagsDao()

	// ---- Historique ----

	public suspend fun getHistory(offset: Int = 0, limit: Int = 50): List<HistoryWithManga> =
		historyDao.findAll(offset, limit)

	public fun observeHistory(limit: Int = 50): Flow<List<HistoryWithManga>> =
		historyDao.observeAll(limit)

	/**
	 * Persiste le manga dans la table « manga » (et ses tags) avant tout écriture qui le
	 * référence par clé étrangère (history, favourites) — équivalent desktop de ce que fait
	 * SaveHistoryUseCase/AddToFavouritesUseCase côté Android. Sans cela, Room renvoie
	 * « FOREIGN KEY constraint failed » (code 787) pour tout manga jamais vu dans la bibliothèque.
	 */
	private suspend fun ensureMangaStored(manga: Manga) {
		val tagEntities = manga.tags.toEntities()
		if (tagEntities.isNotEmpty()) {
			// Les entités tags doivent exister dans la table « tags » avant leurs relations
			// (manga_tags a une FK vers tags) — sinon « FOREIGN KEY constraint failed ».
			tagsDao.upsert(tagEntities)
		}
		mangaDao.upsert(manga.toEntity(), tagEntities)
	}

	public suspend fun saveProgress(
		manga: Manga,
		chapter: MangaChapter,
		page: Int,
		scroll: Float = 0f,
		percent: Float = 0f,
	) {
		ensureMangaStored(manga)
		val existing = historyDao.find(manga.id)
		val now = System.currentTimeMillis()
		historyDao.upsert(
			HistoryEntity(
				mangaId = manga.id,
				createdAt = existing?.createdAt ?: now,
				updatedAt = now,
				chapterId = chapter.id,
				page = page,
				scroll = scroll,
				percent = percent,
				deletedAt = 0L,
				chaptersCount = manga.chapters?.size ?: 0,
			),
		)
	}

	// ---- Favoris ----

	public suspend fun getFavouriteCategories(): List<FavouriteCategoryEntity> =
		categoriesDao.findAll()

	public fun observeFavouriteCategories(): Flow<List<FavouriteCategoryEntity>> =
		categoriesDao.observeAll()

	public suspend fun getFavourites(): List<FavouriteManga> = favouritesDao.findAll()


	public suspend fun addFavourite(manga: Manga, categoryId: Long) {
		ensureMangaStored(manga)
		favouritesDao.upsert(
			FavouriteEntity(
				mangaId = manga.id,
				categoryId = categoryId,
				sortKey = 0,
				isPinned = false,
				createdAt = System.currentTimeMillis(),
				deletedAt = 0L,
			),
		)
	}

	public suspend fun removeFavourite(manga: Manga, categoryId: Long) {
		favouritesDao.delete(manga.id, categoryId)
	}

	/** Convertit une entrée d'historique en manga affichable + progression. */
	public fun HistoryWithManga.toDisplay(): Pair<Manga, HistoryEntity> =
		Pair(manga.toManga(tags.toMangaTags(), chapters = null), history)
}
