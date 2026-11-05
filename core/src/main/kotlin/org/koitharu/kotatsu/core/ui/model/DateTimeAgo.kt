/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : modèle de données pur — les méthodes format(Context) avec ressources Android sont
 * retirées (le formatage texte sera défini côté UI desktop) ; toString conserve les ids stables.
 */
package org.koitharu.kotatsu.core.ui.model

import java.time.LocalDate

sealed class DateTimeAgo {

	data object JustNow : DateTimeAgo() {
		override fun toString() = "just_now"
	}

	data class MinutesAgo(val minutes: Int) : DateTimeAgo() {
		override fun toString() = "minutes_ago_$minutes"
	}

	data class HoursAgo(val hours: Int) : DateTimeAgo() {
		override fun toString() = "hours_ago_$hours"
	}

	data object Today : DateTimeAgo() {
		override fun toString() = "today"
	}

	data object Yesterday : DateTimeAgo() {
		override fun toString() = "yesterday"
	}

	data class DaysAgo(val days: Int) : DateTimeAgo() {
		override fun toString() = "days_ago_$days"
	}

	data class MonthsAgo(val months: Int) : DateTimeAgo() {
		override fun toString() = "months_ago_$months"
	}

	data class Absolute(val date: LocalDate) : DateTimeAgo() {
		override fun toString() = "abs_${date.toEpochDay()}"
	}

	data object LongAgo : DateTimeAgo() {
		override fun toString() = "long_ago"
	}
}
