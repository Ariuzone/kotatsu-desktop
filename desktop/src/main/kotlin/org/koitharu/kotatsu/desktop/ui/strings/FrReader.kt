/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P7 vague 1 :
 * source de chaînes EXCLUSIVE des composants avancés du lecteur (composants purs de
 * ui/screens/reader/components/ : ColorCorrectionDialog, AutoScrollDialog, EraserTutorialDialog).
 * Conformité à la règle client :
 * « toute chaîne affichée vient d'une source de chaînes, jamais écrite en brut dans un écran ».
 * Provenance des chaînes : captures vidéo de référence (lecteur avancé — correction des couleurs
 * t≈8:55 ; défilement automatique t≈8:35-9:19 ; tuto gomme t≈8:05-8:25) et adaptation desktop
 * signalée le cas échéant (geste tactile → glisser-souris). Les constantes déjà présentes dans
 * Fr.kt (FR_OK, FR_CANCEL…) ne sont pas dupliquées : les composants les utilisent directement.
 * SEUL ce fichier est propriétaire des chaînes du lecteur avancé (règle du portage).
 */
package org.koitharu.kotatsu.desktop.ui.strings

// --- Dialog « Correction des couleurs » (vidéo t≈8:55, bottom sheet Android « Correction
//     des couleurs » ; adaptation desktop : dialog M3 avec aperçu avant/après) ---
public var FR_COLOR_CORRECTION: String = "Correction des couleurs"
public var FR_BEFORE: String = "Avant" // aperçu gauche
public var FR_AFTER: String = "Après" // aperçu droite
public var FR_INVERT_COLORS: String = "Inverser les couleurs"
public var FR_GRAYSCALE: String = "Niveaux de gris"
public var FR_BRIGHTNESS: String = "Luminosité"
public var FR_CONTRAST: String = "Contraste"
public var FR_YELLOWISH_BACKGROUND: String = "Fond jaunâtre"
public var FR_RESET: String = "Réinitialiser"

/** Étiquette du contenu factice des deux aperçus (desktop, sans resource image dispo). */
public var FR_COLOR_PREVIEW_LABEL: String = "Exemple de page"

/** Valeur d'un slider en pourcentage : « N % » (format vidéo, ex. « 120 % »). */
public fun frPercentage(value: Float): String = "${(value * 100).toInt()} %"

// --- Dialog « Défilement automatique » (vidéo t≈8:35-9:19, adaptation desktop : dialog M3
//     + bouton de commande flottant Play/Pause) ---
public var FR_AUTO_SCROLL: String = "Défilement automatique"
public var FR_ENABLE: String = "Activer"
public var FR_SPEED: String = "Vitesse"
public var FR_AUTO_SCROLL_FAB_NOTE: String = "Défilement via le bouton de commande flottant"

/** Libellé du slider de vitesse au format vidéo : « Vitesse xN.N » (ex. « Vitesse x4.0 »). */
public fun frAutoScrollSpeed(speed: Float): String = "Vitesse x%.1f".format(java.util.Locale.US, speed)

/** Description d'accessibilité du bouton flottant « lancer ». */
public var FR_AUTO_SCROLL_PLAY: String = "Lancer le défilement automatique"

/** Description d'accessibilité du bouton flottant « pause ». */
public var FR_AUTO_SCROLL_PAUSE: String = "Mettre en pause le défilement automatique"

// --- Dialog/tuto de la gomme (vidéo t≈8:05-8:25, écran plein « Effacez ce que vous ne voulez
//     plus voir » ; adaptation desktop : glisser la souris au lieu du geste tactile) ---
public var FR_ERASER_TUTORIAL_TITLE: String = "Effacez ce que vous ne voulez plus voir"
public var FR_ERASER_TUTORIAL_TEXT: String =
	"Balayez avec le doigt les zones à effacer ; sur desktop, faites glisser la souris sur la " +
		"zone à masquer pendant la lecture. La zone sélectionnée sera recouverte dès que vous " +
		"relâchez le bouton."
public var FR_SKIP: String = "Passer"
