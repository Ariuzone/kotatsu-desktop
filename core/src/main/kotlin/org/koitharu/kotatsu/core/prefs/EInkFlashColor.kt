/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : android.graphics.Color remplacé par des constantes ARGB littérales.
 */
package org.koitharu.kotatsu.core.prefs

enum class EInkFlashColor(val colorInt: Int) {
	WHITE(0xFFFFFFFF.toInt()),
	BLACK(0xFF000000.toInt()),
}
