/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.desktop.tracker

import java.io.File
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.createMangaDatabase
import org.koitharu.kotatsu.core.db.entity.MangaEntity
import org.koitharu.kotatsu.core.model.TestMangaSource
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.tracker.data.TrackEntity
import org.koitharu.kotatsu.tracker.domain.CheckNewChaptersUseCase
import org.koitharu.kotatsu.tracker.domain.MangaDetailsLoader
import org.koitharu.kotatsu.tracker.domain.TrackingRepository

/**
 * Test d'intégration du [TrackerScheduler] avec un dépôt de détails factice :
 * 3 chapitres au premier passage (baseline), 4 au second → 1 événement notifié.
 */
class TrackerSchedulerTest {

	private class FakeDetailsLoader : MangaDetailsLoader {

		var chapters: List<MangaChapter> = emptyList()

		override suspend fun getDetails(manga: Manga): Manga = manga.copy(chapters = chapters)
	}

	private lateinit var db: org.koitharu.kotatsu.core.db.MangaDatabase
	private lateinit var loader: FakeDetailsLoader
	private lateinit var notifier: TrackerNotifier
	private lateinit var scheduler: TrackerScheduler
	private lateinit var repository: TrackingRepository
	private lateinit var scope: CoroutineScope

	@BeforeTest
	fun setUp() {
		// NB : Room.inMemoryDatabaseBuilder nécessiterait androidx.sqlite.bundled en testImpl
		// (build files hors périmètre) — createMangaDatabase(File) de core embarque le driver.
		db = createMangaDatabase(File(Files.createTempDirectory("kotatsu-tracker-test").toFile(), "test.db"))
		val dir = Files.createTempDirectory("kotatsu-tracker-test")
		val store = FileKeyValueStore(dir.resolve("settings.json"))
		val settings = AppSettings(store)
		repository = TrackingRepository(db)
		loader = FakeDetailsLoader()
		notifier = TrackerNotifier()
		scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
		scheduler = TrackerScheduler(
			settings = settings,
			store = store,
			repository = repository,
			checkNewChapters = CheckNewChaptersUseCase(repository, loader),
			notifier = notifier,
			scope = scope,
		)
	}

	private fun mangaOf(id: Long, title: String): Manga = Manga(
		id = id,
		title = title,
		altTitles = emptySet(),
		url = "https://example.org/manga/$id",
		publicUrl = "https://example.org/manga/$id",
		rating = 0f,
		contentRating = null,
		coverUrl = "https://example.org/cover.jpg",
		tags = emptySet(),
		state = null,
		authors = emptySet(),
		largeCoverUrl = null,
		description = null,
		chapters = null,
		source = TestMangaSource,
	)

	private fun chapter(id: Long, number: Float) = MangaChapter(
		id = id,
		title = "Chapter $number",
		number = number,
		volume = 1,
		url = "https://example.org/ch/$id",
		scanlator = null,
		uploadDate = number.toLong() * 1_000_000L,
		branch = null,
		source = TestMangaSource,
	)

	@Test
	fun `runOnce - 3 chapitres puis 4 chapitres - 1 événement`() = runBlocking {
		// Manga suivi (piste créée directement dans le DAO tracks — cf. updateTracks)
		db.getMangaDao().upsert(
			MangaEntity(
				id = 42L,
				title = "Tracked Manga",
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
				source = "MANGADEX", // source parser réelle (mappage EntityMapping)
			),
		)
		db.getTracksDao().upsert(TrackEntity.create(42L))
		// Un manga suivi vient de l'historique/favoris (cf. updateTracks) — nécessaire pour que
		// la requête findAll (clause d'activité récente, comme l'original) le retourne.
		db.getHistoryDao().upsert(
			org.koitharu.kotatsu.history.data.HistoryEntity(
				mangaId = 42L,
				createdAt = System.currentTimeMillis(),
				updatedAt = System.currentTimeMillis(),
				chapterId = 3L,
				page = 0,
				scroll = 0f,
				percent = 0.5f,
				deletedAt = 0L,
				chaptersCount = 3,
			),
		)

		// Passage 1 : 3 chapitres → baseline silencieuse
		loader.chapters = listOf(chapter(1L, 1f), chapter(2L, 2f), chapter(3L, 3f))
		val events1 = scheduler.runOnce()
		assertEquals(0, events1.size)
		assertEquals(3L, repository.getTrackOrNull(mangaOf(42L, "Tracked Manga"))!!.lastChapterId)

		// Passage 2 : 4 chapitres → 1 événement
		val received = mutableListOf<NewChaptersEvent>()
		val collector = scope.launch { notifier.events.collect { received.add(it) } }
		withTimeout(5_000) {
			// S'assurer que le collecteur est abonné avant l'émission (SharedFlow, sans replay)
			while (notifier.subscriberCount.value == 0) {
				kotlinx.coroutines.delay(10)
			}
		}
		loader.chapters = listOf(chapter(1L, 1f), chapter(2L, 2f), chapter(3L, 3f), chapter(4L, 4f))
		val events2 = scheduler.runOnce()
		assertEquals(1, events2.size, "runOnce doit détecter le nouveau chapitre")

		withTimeout(5_000) {
			while (received.size < 1) {
				kotlinx.coroutines.delay(10)
			}
		}
		collector.cancel()

		assertEquals(1, received.size)
		assertEquals(1, received.size)
		assertEquals(42L, received.single().manga.id)
		assertEquals(4L, received.single().newChapters.single().id)
		assertEquals(1, repository.getNewChaptersCount(42L))

		// Passage 3 : rien de nouveau → plus d'événement
		assertEquals(0, scheduler.runOnce().size)

		scope.cancel()
		db.close()
	}

	@Test
	fun `runOnce - tracker désactivé - aucun passage`() = runBlocking {
		val dir = Files.createTempDirectory("kotatsu-tracker-test-off")
		val store = FileKeyValueStore(dir.resolve("settings.json"))
		val settings = AppSettings(store).apply { isTrackerEnabled = false }
		val offScheduler = TrackerScheduler(
			settings = settings,
			store = store,
			repository = repository,
			checkNewChapters = CheckNewChaptersUseCase(repository, loader),
			notifier = notifier,
			scope = scope,
		)
		assertTrue(offScheduler.runOnce().isEmpty())
		assertTrue(repository.getTracks(10).isEmpty())
	}

	@Test
	fun `frequency - clé d'origine tracker_freq, intervalle 18h-fréquence borné à 2h`() {
		val dir = Files.createTempDirectory("kotatsu-tracker-test-freq")
		val store = FileKeyValueStore(dir.resolve("settings.json"))
		val settings = AppSettings(store)
		val scheduler = TrackerScheduler(
			settings = settings,
			store = store,
			repository = repository,
			checkNewChapters = CheckNewChaptersUseCase(repository, loader),
			notifier = notifier,
			scope = scope,
		)
		assertEquals(TrackerScheduler.DEFAULT_FREQUENCY, scheduler.frequencyFactor)
		store.edit { putString(TrackerScheduler.KEY_TRACKER_FREQUENCY, "2") }
		assertEquals(2f, scheduler.frequencyFactor)
		// 18 / 2 = 9 h
		val hours9 = 9L * 60 * 60 * 1000
		assertEquals(hours9, intervalOf(scheduler))
		// fréquence très élevée → borné à 2 h minimum
		store.edit { putString(TrackerScheduler.KEY_TRACKER_FREQUENCY, "100") }
		assertEquals(2L * 60 * 60 * 1000, intervalOf(scheduler))
	}

	private fun intervalOf(scheduler: TrackerScheduler): Long {
		val method = TrackerScheduler::class.java.getDeclaredMethod("intervalMillis")
		method.isAccessible = true
		return method.invoke(scheduler) as Long
	}
}
