/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : la vérification de glyphe via PaintCompat (android.graphics) est retirée ;
 * l'emoji drapeau est toujours renvoyé (le rendu est géré par la police côté desktop).
 */
package org.koitharu.kotatsu.core.util

import org.koitharu.kotatsu.parsers.util.ifNullOrEmpty
import java.util.Locale

object LocaleUtils {

	fun getEmojiFlag(locale: Locale): String? {
		val code = when (val c = locale.country.ifNullOrEmpty { locale.toLanguageTag() }.uppercase(Locale.ENGLISH)) {
			"EN" -> "GB"
			"JA" -> "JP"
			"VI" -> "VN"
			"ZH" -> "CN"
			"AR" -> "SA"
			else -> c
		}
		return countryCodeToEmojiFlag(code)
	}

	private fun countryCodeToEmojiFlag(countryCode: String): String {
		return countryCode.map { char ->
			Character.codePointAt("$char", 0) - 0x41 + 0x1F1E6
		}.map { codePoint ->
			Character.toChars(codePoint)
		}.joinToString(separator = "") { charArray ->
			String(charArray)
		}
	}
}
