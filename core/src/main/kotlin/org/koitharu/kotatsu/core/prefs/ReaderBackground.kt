/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : resolve(Context) (drawables/thèmes Android) retiré — la résolution graphique est
 * définie côté UI desktop ; la sémantique clair/sombre est conservée via isLight().
 */
package org.koitharu.kotatsu.core.prefs

enum class ReaderBackground {

	DEFAULT, LIGHT, DARK, WHITE, BLACK;

	/** Couleur de fond en ARGB pour le lecteur desktop (DEFAULT -> gris sombre neutre). */
	val argb: Int
		get() = when (this) {
			DEFAULT -> 0xFF101010.toInt()
			LIGHT -> 0xFFF2F2F2.toInt()
			DARK -> 0xFF202020.toInt()
			WHITE -> 0xFFFFFFFF.toInt()
			BLACK -> 0xFF000000.toInt()
		}

	fun isLight(isNightMode: Boolean): Boolean = when (this) {
		DEFAULT -> !isNightMode
		LIGHT, WHITE -> true
		DARK, BLACK -> false
	}
}
