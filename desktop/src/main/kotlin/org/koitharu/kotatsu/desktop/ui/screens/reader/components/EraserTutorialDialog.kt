/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P7 vague 1 :
 * composants PURS du tuto / mode gomme du lecteur (vidéo de référence t≈8:05-8:25 : « Effacez
 * ce que vous ne voulez plus voir », bouton « Passer »), sans dépendance à ReaderScreen ni au
 * stockage des zones effacées (l'intégrateur P7-A branchera l'état et la persistance en vague 2).
 * EraserOverlay gère le glisser-souris (adaptation desktop du geste tactile de l'app Android)
 * et remonte des rectangles NORMALISÉS (0..1) indépendants de la taille du lecteur.
 */
package org.koitharu.kotatsu.desktop.ui.screens.reader.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.strings.FR_ERASER_TUTORIAL_TEXT
import org.koitharu.kotatsu.desktop.ui.strings.FR_ERASER_TUTORIAL_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SKIP

/**
 * Dialog/tuto M3 « Effacez ce que vous ne voulez plus voir » (vidéo t≈8:05-8:25) : titre,
 * texte explicatif du geste (glisser la souris sur desktop au lieu du balayage tactile),
 * bouton « Passer » (l'app d'origine montre « Passer » ; pas de « Réessayer »).
 *
 * Contrat d'intégration (P7-A) : à afficher une seule fois (ex. au premier passage en mode
 * gomme, mémorisé côté intégrateur) ; [onDismiss] ferme le tuto sans changer d'état.
 */
@Composable
public fun EraserTutorialDialog(onDismiss: () -> Unit) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = FR_ERASER_TUTORIAL_TITLE) },
		text = {
			Text(
				text = FR_ERASER_TUTORIAL_TEXT,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(text = FR_SKIP)
			}
		},
	)
}

/**
 * Overlay TRANSPARENT de mode gomme à superposer AU-DESSUS du contenu du lecteur (dans la
 * même Box, même taille que la zone de pages). Quand [isEraseMode] est true :
 *  - le glisser-souris affiche un rect de sélection (fond rouge translucide + bordure pointillée)
 *    pendant le drag ;
 *  - au RELÂCHEMENT, [onErase] reçoit le rectangle en coordonnées NORMALISÉES : chaque
 *    coordonnée est une fraction de la taille de l'overlay, `0.0f..1.0f` (left/top/right/bottom,
 *    top-left = origine). L'intégrateur multiplie par les dimensions réelles de la page pour
 *    retrouver les pixels (ex. `left * pageWidth`), ce qui reste correct si la page est
 *    redimensionnée ou zoomée.
 *
 * Aucun accès disque/DB ici : la persistance des zones effacées appartient à l'intégrateur.
 * Quand [isEraseMode] est false, l'overlay est purement transparent et ignore les gestes.
 *
 * Contrat d'intégration (P7-A) :
 * ```
 * Box {
 *     ...pages...
 *     EraserOverlay(
 *         isEraseMode = isEraseMode,
 *         onErase = { rect -> /* rect normalisé 0..1 → masque persisté */ },
 *         modifier = Modifier.matchParentSize(),
 *     )
 * }
 * ```
 *
 * @param isEraseMode true si le mode gomme est actif (geste capté, feedback visible).
 * @param onErase appelé au relâchement avec la sélection normalisée 0..1.
 * @param modifier modifier de l'overlay (typiquement `Modifier.matchParentSize()`).
 */
@Composable
public fun EraserOverlay(
	isEraseMode: Boolean,
	onErase: (Rect) -> Unit,
	modifier: Modifier = Modifier,
) {
	var dragStart by remember { mutableStateOf<Offset?>(null) }
	var dragCurrent by remember { mutableStateOf<Offset?>(null) }

	Box(
		modifier = modifier.pointerInput(isEraseMode) {
			if (!isEraseMode) return@pointerInput
			detectDragGestures(
				onDragStart = { offset ->
					dragStart = offset
					dragCurrent = offset
				},
				onDrag = { change, _ ->
					dragCurrent = change.position
				},
				onDragEnd = {
					val start = dragStart
					val current = dragCurrent
					if (start != null && current != null && size.width > 0 && size.height > 0) {
						// sélection en pixels → fraction de la taille de l'overlay (0..1)
						onErase(
							Rect(
								left = (minOf(start.x, current.x) / size.width).coerceIn(0f, 1f),
								top = (minOf(start.y, current.y) / size.height).coerceIn(0f, 1f),
								right = (maxOf(start.x, current.x) / size.width).coerceIn(0f, 1f),
								bottom = (maxOf(start.y, current.y) / size.height).coerceIn(0f, 1f),
							),
						)
					}
					dragStart = null
					dragCurrent = null
				},
				onDragCancel = {
					dragStart = null
					dragCurrent = null
				},
			)
		},
	) {
		val start = dragStart
		val current = dragCurrent
		if (isEraseMode && start != null && current != null) {
			val selection = Rect(
				left = minOf(start.x, current.x),
				top = minOf(start.y, current.y),
				right = maxOf(start.x, current.x),
				bottom = maxOf(start.y, current.y),
			)
			Canvas(modifier = Modifier.fillMaxSize()) {
				drawRect(
					color = ERASE_FILL,
					topLeft = selection.topLeft,
					size = Size(selection.width, selection.height),
				)
				drawRect(
					color = ERASE_BORDER,
					topLeft = selection.topLeft,
					size = Size(selection.width, selection.height),
					style = Stroke(
						width = 2.dp.toPx(),
						pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
					),
				)
			}
		}
	}
}

/** Remplissage translucide de la sélection en mode gomme (rouge, feedback visuel). */
private val ERASE_FILL: Color = Color.Red.copy(alpha = 0.25f)

/** Bordure pointillée de la sélection en mode gomme. */
private val ERASE_BORDER: Color = Color.Red
