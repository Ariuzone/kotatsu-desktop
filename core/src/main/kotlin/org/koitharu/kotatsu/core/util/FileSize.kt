/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : le formatage par ressources Android (Context/R.string) est remplacé par un
 * formatage avec des unités passées en paramètre (défaut B/KB/MB).
 */
package org.koitharu.kotatsu.core.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

enum class FileSize(private val multiplier: Int) {

	BYTES(1), KILOBYTES(1024), MEGABYTES(1024 * 1024);

	fun convert(amount: Long, target: FileSize): Long = amount * multiplier / target.multiplier

	fun format(amount: Long, units: List<String> = DEFAULT_UNITS): String {
		val bytes = amount * multiplier
		if (bytes <= 0) {
			return "0 ${units.first()}"
		}
		val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
		return buildString {
			append(
				DecimalFormat("#,##0.#").format(
					bytes / 1024.0.pow(digitGroups.toDouble()),
				),
			)
			val unit = units.getOrNull(digitGroups)
			if (unit != null) {
				append(' ')
				append(unit)
			}
		}
	}

	companion object {

		val DEFAULT_UNITS = listOf("B", "KB", "MB")
	}
}
