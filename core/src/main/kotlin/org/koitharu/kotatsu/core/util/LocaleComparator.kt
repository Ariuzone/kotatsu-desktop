/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : LocaleListCompat (androidx) remplacé par la locale JVM par défaut.
 */
package org.koitharu.kotatsu.core.util

import java.util.Locale

class LocaleComparator : Comparator<Locale> {

	private val deviceLocales: List<String>

	init {
		deviceLocales = buildList {
			add("")
			val set = HashSet<String>()
			set.add("")
			val lang = Locale.getDefault().language
			if (set.add(lang)) {
				add(lang)
			}
		}
	}

	override fun compare(a: Locale, b: Locale): Int {
		val indexA = deviceLocales.indexOf(a.language)
		val indexB = deviceLocales.indexOf(b.language)
		return when {
			indexA < 0 && indexB < 0 -> compareValues(a.language, b.language)
			indexA < 0 -> 1
			indexB < 0 -> -1
			else -> compareValues(indexA, indexB)
		}
	}
}
