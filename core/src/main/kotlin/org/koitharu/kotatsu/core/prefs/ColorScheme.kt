/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : @StyleRes/R et Material DynamicColors sont remplacés par un identifiant de thème
 * textuel (themeId, ex. "ThemeOverlay.Kotatsu.Totoro") ; les couleurs Compose réelles seront
 * définies côté desktop. default = DEFAULT (pas de couleur dynamique JVM), MONET/EXPRESSIVE
 * restent disponibles dans getAvailableList() pour compatibilité.
 */
package org.koitharu.kotatsu.core.prefs

import org.koitharu.kotatsu.parsers.util.find

enum class ColorScheme(
	val themeId: String,
) {

	DEFAULT("ThemeOverlay.Kotatsu.Totoro"),
	MONET("ThemeOverlay.Kotatsu.Monet"),
	EXPRESSIVE("ThemeOverlay.Kotatsu.Expressive"),
	MIKU("ThemeOverlay.Kotatsu.Miku"),
	RENA("ThemeOverlay.Kotatsu.Asuka"),
	FROG("ThemeOverlay.Kotatsu.Mion"),
	BLUEBERRY("ThemeOverlay.Kotatsu.Rikka"),
	SAKURA("ThemeOverlay.Kotatsu.Sakura"),
	MAMIMI("ThemeOverlay.Kotatsu.Mamimi"),
	KANADE("ThemeOverlay.Kotatsu.Kanade"),
	ITSUKA("ThemeOverlay.Kotatsu.Itsuka"),
	;

	companion object {

		val default: ColorScheme
			get() = DEFAULT

		fun getAvailableList(): List<ColorScheme> = ColorScheme.entries.toList()

		fun safeValueOf(name: String): ColorScheme? {
			return ColorScheme.entries.find(name)
		}
	}
}
