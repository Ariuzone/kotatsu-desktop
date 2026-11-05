/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : LocaleListCompat (androidx) remplacé par la locale JVM par défaut.
 */
package org.koitharu.kotatsu.core.util

import org.koitharu.kotatsu.core.util.ext.indexOfContains
import java.util.Locale

class LocaleStringComparator : Comparator<String?> {

	private val deviceLocales: List<String?>

	init {
		deviceLocales = buildList {
			add(null)
			val set = HashSet<String?>()
			set.add(null)
			val locale = Locale.getDefault()
			val lang = locale.getDisplayLanguage(locale)
			if (set.add(lang)) {
				add(lang)
			}
		}
	}

	override fun compare(a: String?, b: String?): Int {
		val indexA = deviceLocales.indexOfContains(a, true)
		val indexB = deviceLocales.indexOfContains(b, true)
		return when {
			indexA < 0 && indexB < 0 -> compareValues(a, b)
			indexA < 0 -> 1
			indexB < 0 -> -1
			else -> compareValues(indexA, indexB)
		}
	}
}
