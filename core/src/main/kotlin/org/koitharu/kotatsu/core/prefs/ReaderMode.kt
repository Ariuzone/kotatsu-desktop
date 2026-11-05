/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.prefs

enum class ReaderMode(val id: Int) {

	STANDARD(1),
	REVERSED(3),
	VERTICAL(4),
	WEBTOON(2),
	;

	companion object {

		fun valueOf(id: Int) = entries.firstOrNull { it.id == id }
	}
}
