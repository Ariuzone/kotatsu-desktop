/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koitharu.kotatsu.core.db.entity.MangaEntity
import org.koitharu.kotatsu.favourites.data.FavouriteCategoryEntity
import org.koitharu.kotatsu.favourites.data.FavouriteEntity
import org.koitharu.kotatsu.history.data.HistoryEntity
import java.io.File
import java.nio.file.Files

class MangaDatabaseTest {

	@Test
	fun `manga - history - favourites round trip`() = kotlinx.coroutines.runBlocking {
		val db = Room
			.inMemoryDatabaseBuilder(::MangaDatabase_Impl)
			.setDriver(BundledSQLiteDriver())
			.build()
		try {
			val manga = MangaEntity(
				id = 42L,
				title = "Test Manga",
				altTitles = "Alt A\nAlt B",
				url = "https://example.org/manga/42",
				publicUrl = "https://example.org/manga/42",
				rating = 0.5f,
				isNsfw = false,
				contentRating = null,
				coverUrl = "https://example.org/cover.jpg",
				largeCoverUrl = null,
				state = "ONGOING",
				authors = "Author One\nAuthor Two",
				source = "MANGADEX",
			)
			db.getMangaDao().upsert(manga)

			// Histoire
			val history = HistoryEntity(
				mangaId = manga.id,
				createdAt = 1000L,
				updatedAt = 2000L,
				chapterId = 7L,
				page = 3,
				scroll = 0.25f,
				percent = 0.5f,
				deletedAt = 0L,
				chaptersCount = 12,
			)
			assertTrue(db.getHistoryDao().upsert(history))
			val historyRead = db.getHistoryDao().find(manga.id)
			assertEquals(history, historyRead)

			// Favoris (la FK exige une catégorie)
			val category = FavouriteCategoryEntity(
				categoryId = 0, // auto-généré
				createdAt = 1000L,
				sortKey = 1,
				title = "Read later",
				order = "NEWEST",
				track = false,
				isVisibleInLibrary = true,
				deletedAt = 0L,
			)
			val categoryId = db.getFavouriteCategoriesDao().insert(category)
			db.getFavouritesDao().insert(
				FavouriteEntity(
					mangaId = manga.id,
					categoryId = categoryId,
					sortKey = 1,
					isPinned = false,
					createdAt = 1500L,
					deletedAt = 0L,
				),
			)
			val favouritesRead = db.getFavouritesDao().findAllRaw(manga.id)
			assertEquals(1, favouritesRead.size)
			assertEquals(categoryId, favouritesRead.single().categoryId)
			assertTrue(favouritesRead.single().isPinned.not())

			// Relecture du manga via MangaDao (MangaWithTags)
			val withTags = db.getMangaDao().find(manga.id)
			assertEquals(manga, withTags?.manga)
			assertTrue(db.getMangaDao().contains(manga.id))

			// Bookmarks
			val bookmark = org.koitharu.kotatsu.bookmarks.data.BookmarkEntity(
				mangaId = manga.id,
				pageId = 99L,
				chapterId = 7L,
				page = 5,
				scroll = 0,
				imageUrl = "https://example.org/page5.jpg",
				createdAt = 3000L,
				percent = 0.4f,
			)
			db.getBookmarksDao().insert(bookmark)
			assertEquals(bookmark, db.getBookmarksDao().find(99L))
		} finally {
			db.close()
		}
	}

	@Test
	fun `migrations couvrent toutes les versions de 1 a 28`() {
		val covered = sortedSetOf<Int>()
		for (migration in getDatabaseMigrations()) {
			covered.add(migration.startVersion)
			covered.add(migration.endVersion)
		}
		for (version in 1 until DATABASE_VERSION) {
			assertTrue("Pas de migration atteignant la version $version", version in covered)
		}
	}

	@Test
	fun `creation sur fichier et relecture`() {
		kotlinx.coroutines.runBlocking {
		val dir = Files.createTempDirectory("kotatsu-db-test").toFile()
		val dbFile = File(dir, "kotatsu-db")
		val db = createMangaDatabase(dbFile)
		try {
			val manga = MangaEntity(
				id = 1L,
				title = "File Manga",
				altTitles = null,
				url = "u",
				publicUrl = "pu",
				rating = -1f,
				isNsfw = true,
				contentRating = "ADULT",
				coverUrl = "",
				largeCoverUrl = null,
				state = null,
				authors = null,
				source = "MANGADEX",
			)
			db.getMangaDao().upsert(manga)
			assertEquals(manga, db.getMangaDao().find(1L)?.manga)
			} finally {
				db.close()
			}
			dir.deleteRecursively()
		}
	}
}
