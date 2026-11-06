/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.tracker

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koitharu.kotatsu.core.db.MangaDatabase_Impl
import org.koitharu.kotatsu.core.db.entity.MangaEntity
import org.koitharu.kotatsu.core.model.TestMangaSource
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.tracker.data.TrackEntity
import org.koitharu.kotatsu.tracker.domain.CheckNewChaptersUseCase
import org.koitharu.kotatsu.tracker.domain.MangaDetailsLoader
import org.koitharu.kotatsu.tracker.domain.TrackingRepository
import org.koitharu.kotatsu.tracker.domain.model.MangaTracking
import org.koitharu.kotatsu.tracker.domain.model.MangaUpdates

/**
 * Détecteur de nouveaux chapitres testé avec un dépôt de détails factice :
 * 3 chapitres au premier contrôle (baseline silencieuse), 4 au second → 1 événement.
 */
class CheckNewChaptersUseCaseTest {

	private class FakeDetailsLoader : MangaDetailsLoader {

		var chapters: List<MangaChapter> = emptyList()

		override suspend fun getDetails(manga: Manga): Manga = manga.copy(chapters = chapters)
	}

	private fun MangaEntity.toManga(): Manga = Manga(
		id = id,
		title = title,
		altTitles = emptySet(),
		url = url,
		publicUrl = publicUrl,
		rating = rating,
		contentRating = null,
		coverUrl = coverUrl,
		tags = emptySet(),
		state = null,
		authors = emptySet(),
		largeCoverUrl = largeCoverUrl,
		description = null,
		chapters = null,
		source = TestMangaSource,
	)

	private fun chapter(id: Long, number: Float, uploadDate: Long = number.toLong() * 1_000_000L) = MangaChapter(
		id = id,
		title = "Chapter $number",
		number = number,
		volume = 1,
		url = "https://example.org/ch/$id",
		scanlator = null,
		uploadDate = uploadDate,
		branch = null,
		source = TestMangaSource,
	)

	@Test
	fun `new chapter detection - 3 chapters then 4 chapters - 1 event`() = runBlocking {
		val db = Room
			.inMemoryDatabaseBuilder(::MangaDatabase_Impl)
			.setDriver(BundledSQLiteDriver())
			.build()
		try {
			db.getMangaDao().upsert(
				MangaEntity(
					id = 42L,
					title = "Test Manga",
					altTitles = null,
					url = "https://example.org/manga/42",
					publicUrl = "https://example.org/manga/42",
					rating = 0f,
					isNsfw = false,
					contentRating = null,
					coverUrl = "https://example.org/cover.jpg",
					largeCoverUrl = null,
					state = "ONGOING",
					authors = null,
					source = "TEST",
				),
			)
			db.getTracksDao().upsert(TrackEntity.create(42L))
			val repository = TrackingRepository(db)
			val loader = FakeDetailsLoader()
			val useCase = CheckNewChaptersUseCase(repository, loader)
			val manga = db.getMangaDao().find(42L)!!.manga.toManga()

			// Contrôle 1 : 3 chapitres, piste vide → baseline silencieuse (pas d'événement)
			loader.chapters = listOf(chapter(1L, 1f), chapter(2L, 2f), chapter(3L, 3f))
			val first = useCase.invoke(
				MangaTracking(manga = manga, lastChapterId = 0L, lastCheck = null, lastChapterDate = null, newChapters = 0),
			)
			assertTrue(first is MangaUpdates.Success)
			assertFalse((first as MangaUpdates.Success).isNotEmpty())
			val track = repository.getTrackOrNull(manga)!!
			assertEquals(3L, track.lastChapterId)
			assertEquals(0, track.newChapters)

			// Contrôle 2 : 4 chapitres → 1 nouveau chapitre détecté
			loader.chapters = listOf(chapter(1L, 1f), chapter(2L, 2f), chapter(3L, 3f), chapter(4L, 4f))
			val second = useCase.invoke(repository.getTrackOrNull(manga)!!)
			assertTrue(second is MangaUpdates.Success)
			assertEquals(1, (second as MangaUpdates.Success).newChapters.size)
			assertEquals(4L, second.newChapters.single().id)
			assertEquals(1, repository.getNewChaptersCount(42L))

			// Contrôle 3 : rien de nouveau entre-temps → plus d'événement
			val third = useCase.invoke(repository.getTrackOrNull(manga)!!)
			assertTrue(third is MangaUpdates.Success)
			assertFalse((third as MangaUpdates.Success).isNotEmpty())
			assertEquals(1, repository.getNewChaptersCount(42L))
		} finally {
			db.close()
		}
	}

	@Test
	fun `date fallback - when chapter ids churn, uploadDate anchor finds the new chapter`() = runBlocking {
		val db = Room
			.inMemoryDatabaseBuilder(::MangaDatabase_Impl)
			.setDriver(BundledSQLiteDriver())
			.build()
		try {
			db.getMangaDao().upsert(
				MangaEntity(
					id = 7L,
					title = "Rotating Ids",
					altTitles = null,
					url = "https://example.org/manga/7",
					publicUrl = "https://example.org/manga/7",
					rating = 0f,
					isNsfw = false,
					contentRating = null,
					coverUrl = "https://example.org/cover.jpg",
					largeCoverUrl = null,
					state = "ONGOING",
					authors = null,
					source = "TEST",
				),
			)
			db.getTracksDao().upsert(
				TrackEntity(
					mangaId = 7L,
					lastChapterId = 999_999L, // id absent de la liste fraîche (churn de la source)
					newChapters = 0,
					lastCheckTime = 0L,
					lastChapterDate = 3_000_000L, // date d'upload du chapitre 3
					lastResult = TrackEntity.RESULT_NO_UPDATE,
					lastError = null,
				),
			)
			val repository = TrackingRepository(db)
			val loader = FakeDetailsLoader().apply {
				// La source a changé d'ids (urls réécrites) mais les dates d'upload sont stables
				chapters = listOf(
					chapter(101L, 1f),
					chapter(102L, 2f),
					chapter(103L, 3f),
					chapter(104L, 4f), // uploadDate = 4_000_000 > baseline (3_000_000)
				)
			}
			val useCase = CheckNewChaptersUseCase(repository, loader)
			val manga = db.getMangaDao().find(7L)!!.manga.toManga()
			val updates = useCase.invoke(repository.getTrackOrNull(manga)!!) as MangaUpdates.Success
			assertEquals(1, updates.newChapters.size)
			assertEquals(104L, updates.newChapters.single().id)
		} finally {
			db.close()
		}
	}
}
