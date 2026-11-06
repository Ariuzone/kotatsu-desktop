/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Formatage de core/ui/model/DateTimeAgo côté desktop — reprend EXACTEMENT les chaînes et
 * pluriels de res/values/strings.xml + res/values/plurals.xml de l'app d'origine
 * (just_now, minutes_ago, hours_ago, today, yesterday, days_ago, months_ago, this_month,
 * long_ago, unknown).
 *
 * Porte aussi core/util/ext/Date.kt#calculateTimeAgo (logique identique : fenêtre de 3 minutes,
 * seuil de 6 jours, mode showMonths).
 */
package org.koitharu.kotatsu.desktop.ui.components

import org.koitharu.kotatsu.core.ui.model.DateTimeAgo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit


private const val JUST_NOW_WINDOW_MINUTES = 3L
private const val DAYS_WINDOW = 6L
private const val MONTHS_WINDOW = 6L

/**
 * Reproduit DateUtils.getRelativeTimeSpanString via le modèle DateTimeAgo d'origine.
 * `showMonths` = false comme pour les en-têtes d'historique (HistoryListViewModel.header).
 */
public fun calculateTimeAgo(instant: Instant, showMonths: Boolean = false): DateTimeAgo? {
	val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
	val now = LocalDate.now()
	val diffDays = localDate.until(now, ChronoUnit.DAYS)

	return when {
		diffDays < 0 -> null // dans le futur, probablement un bug, non supporté
		diffDays == 0L -> {
			if (instant.until(Instant.now(), ChronoUnit.MINUTES) < JUST_NOW_WINDOW_MINUTES) {
				DateTimeAgo.JustNow
			} else {
				DateTimeAgo.Today
			}
		}

		diffDays == 1L -> DateTimeAgo.Yesterday
		diffDays < DAYS_WINDOW -> DateTimeAgo.DaysAgo(diffDays.toInt())
		else -> {
			val diffMonths = localDate.until(now, ChronoUnit.MONTHS)
			if (showMonths && diffMonths <= MONTHS_WINDOW) {
				DateTimeAgo.MonthsAgo(diffMonths.toInt())
			} else {
				DateTimeAgo.Absolute(localDate)
			}
		}
	}
}

/**
 * Formatte un DateTimeAgo avec les libellés exacts de strings.xml / plurals.xml d'origine.
 * (Le modèle core ne porte pas de format(Context) côté desktop ; ce point est l'équivalent.)
 */
public fun DateTimeAgo.format(): String = when (this) {
	is DateTimeAgo.JustNow -> "Just now"
	is DateTimeAgo.MinutesAgo -> pluralize(minutes, "minute")
	is DateTimeAgo.HoursAgo -> pluralize(hours, "hour")
	is DateTimeAgo.Today -> "Today"
	is DateTimeAgo.Yesterday -> "Yesterday"
	is DateTimeAgo.DaysAgo -> pluralize(days, "day")
	is DateTimeAgo.MonthsAgo -> if (months == 0) "This month" else pluralize(months, "month")
	is DateTimeAgo.Absolute ->
		if (date == EPOCH_DATE) {
			"Unknown"
		} else {
			DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date)
		}

	is DateTimeAgo.LongAgo -> "Long ago"
}

// Équivalent desktop de getQuantityStringSafe + plurals d'origine :
// one = "%1$d minute ago", other = "%1$d minutes ago", etc.
private fun pluralize(count: Int, unit: String): String = buildString {
	append(count).append(' ').append(unit)
	if (count != 1) append('s')
	append(" ago")
}

private val EPOCH_DATE: LocalDate = LocalDate.of(1970, 1, 1)
