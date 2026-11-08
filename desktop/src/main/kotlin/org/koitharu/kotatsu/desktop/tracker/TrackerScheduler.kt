/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : remplace TrackWorker + WorkManager (PeriodicWorkRequest, contraintes réseau,
 * notification de premier plan) par une tâche périodique en coroutines :
 * — intervalle = 18 h / facteur de fréquence, borné à 2 h minimum (même formule que
 *   TrackWorker.Scheduler.schedule() avec runsPerFullCheck = 1 : desktop vérifie toutes les
 *   pistes à chaque passage, pas de BATCH_SIZE/46 par exécution) ;
 * — facteur de fréquence lu dans le KeyValueStore avec la clé d'origine `tracker_freq`
 *   (défaut 1, cf. AppSettings.trackerFrequencyFactor Android — clé absente d'AppSettings
 *   desktop qui n'a pas été modifiée par ce lot) ;
 * — pas de contraintes réseau (pas d'équivalent WorkManager) : l'hôte desktop décide ;
 * — parallélisme 6 comme l'original (Semaphore).
 */

package org.koitharu.kotatsu.desktop.tracker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.KeyValueStore
import org.koitharu.kotatsu.tracker.domain.CheckNewChaptersUseCase
import org.koitharu.kotatsu.tracker.domain.TrackingRepository
import org.koitharu.kotatsu.tracker.domain.model.MangaUpdates
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TrackerScheduler(
	private val settings: AppSettings,
	private val store: KeyValueStore,
	private val repository: TrackingRepository,
	private val checkNewChapters: CheckNewChaptersUseCase,
	private val notifier: TrackerNotifier,
	private val scope: CoroutineScope,
) {

	/** Facteur de fréquence (clé d'origine `tracker_freq`, défaut 1) : >1 = vérifier plus souvent. */
	val frequencyFactor: Float
		get() = store.getString(KEY_TRACKER_FREQUENCY, null)?.toFloatOrNull() ?: 1f

	private val _isRunning = MutableStateFlow(false)

	/** Équivalent de TrackWorker.Scheduler.observeIsRunning(). */
	val isRunning: StateFlow<Boolean> = _isRunning

	@Volatile
	private var job: Job? = null

	/** Démarre la boucle périodique (no-op si déjà démarrée). */
	fun start() {
		if (job?.isActive == true) return
		job = scope.launch {
			while (true) {
				runOnce()
				delay(intervalMillis())
			}
		}
	}

	/** Arrête la boucle périodique. */
	fun stop() {
		job?.cancel()
		job = null
		_isRunning.value = false
	}

	/** Un passage immédiat (équivalent de Scheduler.startNow()), sans modifier la boucle. */
	fun startNow() {
		scope.launch { runOnce() }
	}

	/**
	 * Un passage complet de vérification : pour chaque piste, getDetails → nouveaux
	 * chapitres → mise à jour de la TrackEntity + événement [NewChaptersEvent].
	 * Retourne les événements émis (utile pour les tests / l'UI « check now »).
	 */
	suspend fun runOnce(): List<NewChaptersEvent> {
		if (!settings.isTrackerEnabled) {
			return emptyList()
		}
		_isRunning.value = true
		try {
			val tracks = repository.getTracks(BATCH_SIZE)
			if (tracks.isEmpty()) {
				return emptyList()
			}
			val semaphore = Semaphore(MAX_PARALLELISM)
			val events = channelFlow {
				for (track in tracks) {
					launch {
						semaphore.withPermit {
							when (val updates = runCatching { checkNewChapters(track) }.getOrElse { e ->
								MangaUpdates.Failure(manga = track.manga, error = e)
							}) {
								is MangaUpdates.Failure -> {
									System.err.println(
										"[Tracker] \"${updates.manga.title}\" check failed: " +
											"${updates.error?.javaClass?.simpleName} ${updates.error?.message}",
									)
								}

								is MangaUpdates.Success -> {
									if (updates.isValid && updates.isNotEmpty()) {
										send(NewChaptersEvent(manga = updates.manga, newChapters = updates.newChapters))
									}
								}
							}
						}
					}
				}
			}.let { flow ->
				buildList {
					flow.collect { event ->
						add(event)
						if (settings.isTrackerNotificationsEnabled) {
							notifier.notify(event)
						}
					}
				}
			}
			return events
		} finally {
			_isRunning.value = false
		}
	}

	/** Même formule que l'original : 18 h de base / fréquence, borné à 2 h minimum.
	 * P4 : la préférence « Fréquence de vérification » (tracker_frequency : DEFAULT/HOURLY/
	 * EVERY6H/DAILY) prime quand elle est définie explicitement. */
	private fun intervalMillis(): Long {
		when (settings.trackerFrequency) {
			"HOURLY" -> return TimeUnit.HOURS.toMillis(1)
			"EVERY6H" -> return TimeUnit.HOURS.toMillis(6)
			"DAILY" -> return TimeUnit.HOURS.toMillis(24)
		}
		val factor = frequencyFactor.coerceAtLeast(0.1f)
		val hours = (BASE_INTERVAL_HOURS / factor).roundToInt().coerceAtLeast(MIN_INTERVAL_HOURS)
		return TimeUnit.HOURS.toMillis(hours.toLong())
	}

	companion object {

		/** Clé d'origine Android (AppSettings.KEY_TRACKER_FREQUENCY). */
		const val KEY_TRACKER_FREQUENCY = "tracker_freq"

		/** AppSettings.trackerFrequencyFactor Android : défaut 1. */
		const val DEFAULT_FREQUENCY = 1f

		const val BASE_INTERVAL_HOURS = 18f
		const val MIN_INTERVAL_HOURS = 2
		const val BATCH_SIZE = 46
		const val MAX_PARALLELISM = 6
	}
}
