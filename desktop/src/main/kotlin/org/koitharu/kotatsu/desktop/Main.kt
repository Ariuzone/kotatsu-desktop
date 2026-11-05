/*
 * Portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo, GPLv3).
 * Ce fichier fait partie d'un travail dérivé — 2026-11-05.
 */
package org.koitharu.kotatsu.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koitharu.kotatsu.core.APP_VERSION_NAME

fun main() = application {
	Window(onCloseRequest = ::exitApplication, title = "Kotatsu $APP_VERSION_NAME") {
		MaterialTheme {
			Text("Kotatsu Desktop — squelette compilable")
		}
	}
}
