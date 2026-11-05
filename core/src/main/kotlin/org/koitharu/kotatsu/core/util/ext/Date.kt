/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : Resources.formatDurationShort (chaînes Android) retirée ; DateTimeAgo est ici un
 * modèle de données pur (le formatage texte sera défini côté UI desktop).
 */
package org.koitharu.kotatsu.core.util.ext

import org.koitharu.kotatsu.core.ui.model.DateTimeAgo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun calculateTimeAgo(instant: Instant, showMonths: Boolean = false): DateTimeAgo? {
	val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
	val now = LocalDate.now()
	val diffDays = localDate.until(now, ChronoUnit.DAYS)

	return when {
		diffDays < 0 -> null // in future, probably a bug, not supported
		diffDays == 0L -> {
			if (instant.until(Instant.now(), ChronoUnit.MINUTES) < 3) DateTimeAgo.JustNow
			else DateTimeAgo.Today
		}

		diffDays == 1L -> DateTimeAgo.Yesterday
		diffDays < 6 -> DateTimeAgo.DaysAgo(diffDays.toInt())
		else -> {
			val diffMonths = localDate.until(now, ChronoUnit.MONTHS)
			if (showMonths && diffMonths <= 6) {
				DateTimeAgo.MonthsAgo(diffMonths.toInt())
			} else {
				DateTimeAgo.Absolute(localDate)
			}
		}
	}
}

fun Long.toInstantOrNull() = if (this == 0L) null else Instant.ofEpochMilli(this)

fun LocalDate.toMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
