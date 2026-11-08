/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — énumération du mode
 * de mise à l'échelle des pages (« Mode mise à l'échelle » de pref_reader, values-fr : Ajuster
 * au centre / Largeur ajustée / Hauteur ajustée / Recadrer).
 */
package org.koitharu.kotatsu.core.prefs

enum class ScaleMode {
	FIT_CENTER,
	FIT_WIDTH,
	FIT_HEIGHT,
	CROP,
	;
}
