/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : remplace NotificationManagerCompat (Android) par le Tray Compose Desktop
 * (androidx.compose.ui.window.Tray, org.jetbrains.compose.ui) avec bulles de notification.
 * À câbler dans la racine de composition `application { }` (Main.kt) :
 *   TrackerTray(trackerNotifier)
 */

package org.koitharu.kotatsu.desktop.tracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState

/**
 * Icône système du tracker : affiche une bulle de notification Windows pour chaque
 * [NewChaptersEvent] émis par [TrackerNotifier.events].
 * Optionnel : menu contextuel du tray (ex. item « Ouvrir Kotatsu », « Quitter »).
 */
@Composable
fun ApplicationScope.TrackerTray(
	notifier: TrackerNotifier,
	tooltip: String = "Kotatsu",
	menu: @Composable MenuScope.() -> Unit = {},
) {
	val trayState = rememberTrayState()
	LaunchedEffect(notifier) {
		notifier.events.collect { event ->
			trayState.sendNotification(
				Notification(
					title = event.manga.title,
					message = when (event.count) {
						1 -> event.newChapters.single().name
						else -> "${event.count} new chapters"
					},
					type = Notification.Type.Info,
				),
			)
		}
	}
	// NB : pas de garde isTraySupported (API instable selon les versions de Compose) ;
	// le Tray est supporté sur Windows (cible exclusive de ce portage).
	Tray(
		icon = painterResource("kotatsu_256.png"),
		state = trayState,
		tooltip = tooltip,
		menu = menu,
	)
}
