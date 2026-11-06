/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : remplace TrackerNotificationHelper + NotificationManagerCompat (Android) par un
 * flux d'événements `SharedFlow` ; la présentation visuelle est assurée par [TrackerTray]
 * (org.jetbrains.compose.ui Tray + bulle de notification) que l'orchestrateur branche lui-même.
 */

package org.koitharu.kotatsu.desktop.tracker

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter

/** Un manga suivi a de nouveaux chapitres (équivalent d'une notification Android). */
data class NewChaptersEvent(
	val manga: Manga,
	val newChapters: List<MangaChapter>,
) {

	val count: Int get() = newChapters.size
}

/**
 * Bus de notifications du tracker. Le [TrackerScheduler] y publie les événements ; l'UI
 * (ex. [TrackerTray]) les consomme. [TrackerNotifier] est agnostique de la présentation,
 * comme TrackerNotificationHelper l'était du canal Android.
 */
class TrackerNotifier {

	private val _events = MutableSharedFlow<NewChaptersEvent>(extraBufferCapacity = 16)

	val events: SharedFlow<NewChaptersEvent> = _events

	/** Publie un événement « nouveaux chapitres » (suspend si le buffer est plein). */
	suspend fun notify(event: NewChaptersEvent) {
		_events.emit(event)
	}
}
