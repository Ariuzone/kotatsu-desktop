/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.list.domain.ReadingProgress`
 * (sans le data class dépendant de core/prefs ProgressIndicatorMode). À REMPLACER par la
 * version complète d'origine à la fusion. Les DAO n'utilisent que les constantes/validations.
 */
package org.koitharu.kotatsu.list.domain

class ReadingProgress private constructor() {

	companion object {

		const val PROGRESS_NONE = -1f
		const val PROGRESS_COMPLETED = 1f
		private const val PROGRESS_COMPLETED_THRESHOLD = 0.99999f

		fun isValid(percent: Float) = percent in 0f..1f

		fun isCompleted(percent: Float) = percent >= PROGRESS_COMPLETED_THRESHOLD

		fun percentToString(percent: Float): String = if (isValid(percent)) {
			if (isCompleted(percent)) "100" else (percent * 100f).toInt().toString()
		} else {
			"0"
		}
	}
}
