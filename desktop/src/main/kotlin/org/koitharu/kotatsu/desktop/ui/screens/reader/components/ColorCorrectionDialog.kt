/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P7 vague 1 :
 * composant PUR « Correction des couleurs » du lecteur (vidéo de référence t≈8:55), sans
 * dépendance à ReaderScreen ni aux prefs (l'intégrateur P7-A branchera l'état en vague 2).
 * Aperçu avant/après côte à côte, réglages inverser / niveaux de gris / luminosité / contraste /
 * fond jaunâtre, et l'extension Modifier.colorCorrection réellement fonctionnelle (matrice de
 * couleurs RGBA 4x5 appliquée via Canvas.saveLayer(Paint(colorFilter)) — le filtre s'applique
 * au contenu dessiné par le composant auquel le modifier est attaché).
 */
package org.koitharu.kotatsu.desktop.ui.screens.reader.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.strings.FR_AFTER
import org.koitharu.kotatsu.desktop.ui.strings.FR_BEFORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_BRIGHTNESS
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLOR_CORRECTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLOR_PREVIEW_LABEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTRAST
import org.koitharu.kotatsu.desktop.ui.strings.FR_GRAYSCALE
import org.koitharu.kotatsu.desktop.ui.strings.FR_INVERT_COLORS
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_RESET
import org.koitharu.kotatsu.desktop.ui.strings.FR_YELLOWISH_BACKGROUND
import org.koitharu.kotatsu.desktop.ui.strings.frPercentage

/**
 * État de la correction des couleurs du lecteur (composant pur, l'intégrateur persiste s'il
 * veut la mémoriser — aucun stockage ici).
 *
 * @property invert inversion des couleurs (matrice négatif).
 * @property grayscale désaturation totale (matrice de saturation 0).
 * @property brightness multiplicateur de luminosité, 1f = neutre (plage utile 0.2..2.0).
 * @property contrast multiplicateur de contraste, 1f = neutre (plage utile 0.2..2.0).
 * @property yellowish fond jaunâtre : voile jaune semi-transparent posé sur le contenu.
 */
public data class ColorCorrectionState(
	public val invert: Boolean = false,
	public val grayscale: Boolean = false,
	public val brightness: Float = 1f,
	public val contrast: Float = 1f,
	public val yellowish: Boolean = false,
)

/** Voile jaune du « Fond jaunâtre » (overlay alpha faible, équivalent du filtre Android). */
private val YELLOWISH_OVERLAY: Color = Color(0xFFFFC44D).copy(alpha = 0.16f)

/** true si au moins un réglage modifie le rendu (évite d'attacher un modifier inutile). */
private fun ColorCorrectionState.hasEffect(): Boolean = invert || grayscale ||
	yellowish || brightness != 1f || contrast != 1f

/**
 * Dialog M3 « Correction des couleurs » : aperçu Avant/Après côte à côte (le bloc « Après »
 * porte réellement Modifier.colorCorrection(state)), switches Inverser / Niveaux de gris /
 * Fond jaunâtre, sliders Luminosité et Contraste (0.2..2.0, valeur affichée « N % »),
 * boutons « Réinitialiser » et « OK ».
 *
 * Contrat d'intégration (P7-A) : les changements sont émis EN DIRECT via [onChange] (même
 * pendant le drag des sliders) — l'intégrateur met à jour son état et applique
 * `Modifier.colorCorrection` au contenu du lecteur ; [onDismiss] ferme le dialog.
 *
 * @param state état courant affiché (source de vérité détenue par l'intégrateur).
 * @param onChange appelé à chaque modification de réglage avec le nouvel état complet.
 * @param onDismiss fermeture du dialog (clic hors fenêtre, « OK »).
 */
@Composable
public fun ColorCorrectionDialog(
	state: ColorCorrectionState,
	onChange: (ColorCorrectionState) -> Unit,
	onDismiss: () -> Unit,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = FR_COLOR_CORRECTION) },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
				// Aperçu avant/après côte à côte
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					ColorPreviewBlock(
						label = FR_BEFORE,
						caption = FR_BEFORE,
						modifier = Modifier.weight(1f),
					)
					ColorPreviewBlock(
						label = FR_COLOR_PREVIEW_LABEL,
						caption = FR_AFTER,
						modifier = Modifier
							.weight(1f)
							.colorCorrection(state),
					)
				}
				Spacer(modifier = Modifier.height(8.dp))
				ToggleRow(
					label = FR_INVERT_COLORS,
					checked = state.invert,
					onCheckedChange = { onChange(state.copy(invert = it)) },
				)
				ToggleRow(
					label = FR_GRAYSCALE,
					checked = state.grayscale,
					onCheckedChange = { onChange(state.copy(grayscale = it)) },
				)
				SliderRow(
					label = FR_BRIGHTNESS,
					value = state.brightness,
					valueText = frPercentage(state.brightness),
					onValueChange = { onChange(state.copy(brightness = it)) },
				)
				SliderRow(
					label = FR_CONTRAST,
					value = state.contrast,
					valueText = frPercentage(state.contrast),
					onValueChange = { onChange(state.copy(contrast = it)) },
				)
				ToggleRow(
					label = FR_YELLOWISH_BACKGROUND,
					checked = state.yellowish,
					onCheckedChange = { onChange(state.copy(yellowish = it)) },
				)
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(text = FR_OK)
			}
		},
		dismissButton = {
			TextButton(onClick = { onChange(ColorCorrectionState()) }) {
				Text(text = FR_RESET)
			}
		},
	)
}

/** Bloc factice coloré servant de page d'exemple aux deux aperçus (aucune resource image). */
@Composable
private fun ColorPreviewBlock(
	label: String,
	caption: String,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f),
			contentAlignment = Alignment.Center,
		) {
			Canvas(modifier = Modifier.fillMaxSize()) {
				drawRoundRect(
					brush = Brush.linearGradient(
						listOf(
							Color(0xFFB0BEC5),
							Color(0xFF78909C),
							Color(0xFF546E7A),
						),
					),
					cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
				)
			}
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = Color.White,
				modifier = Modifier
					.background(Color.Black.copy(alpha = 0.35f))
					.padding(horizontal = 6.dp, vertical = 2.dp),
			)
		}
		Text(
			text = caption,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(top = 4.dp),
		)
	}
}

/**
 * Extension RÉELLE appliquant la correction des couleurs au contenu du composant auquel le
 * modifier est attaché (typiquement la zone de pages du lecteur, ou chaque AsyncImage).
 *
 * Implémentation : matrice RGBA 4x5 construite par [buildColorMatrix], portée par un
 * `Paint(colorFilter)` et appliquée au contenu via `Canvas.saveLayer(bounds, paint)` — le
 * filtre est posé sur la couche quand elle est composée (restore). Le « Fond jaunâtre » est
 * un rect jaune alpha faible dessiné PAR-DESSUS le contenu (drawRect après drawContent).
 * Sans réglage actif, le modifier n'est pas attaché (no-op).
 *
 * Exemple d'intégration (P7-A) :
 * ```
 * Box(modifier = Modifier.fillMaxSize().colorCorrection(colorCorrectionState)) { ...pages... }
 * ```
 */
public fun Modifier.colorCorrection(state: ColorCorrectionState): Modifier {
	if (!state.hasEffect()) return this
	val matrixActive = state.invert || state.grayscale || state.brightness != 1f || state.contrast != 1f
	val paint = Paint().apply {
		if (matrixActive) {
			colorFilter = ColorFilter.colorMatrix(ColorMatrix(buildColorMatrix(state)))
		}
	}
	return drawWithContent {
		if (matrixActive) {
			drawIntoCanvas { canvas ->
				canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
				drawContent()
				canvas.restore()
			}
		} else {
			drawContent()
		}
		if (state.yellowish) {
			drawRect(color = YELLOWISH_OVERLAY)
		}
	}
}

/**
 * Construit la matrice 4x5 combinée, ordre d'application documenté :
 * 1) inversion (négatif), 2) désaturation (niveaux de gris, pondérations Rec.601
 * 0.213/0.715/0.072, mêmes que ColorMatrix.setToSaturation(0f) de Compose qui n'expose pas
 * de setToInvert), 3) luminosité (échelle RGB), 4) contraste (échelle RGB + décalage
 * (1-c)/2 autour du gris moyen 0.5).
 *
 * Convention matricielle (identique à android.graphics.ColorMatrix) : ligne = sortie,
 * colonnes = contributions R,G,B,A + offset ; composition = multiplication de matrices 4x5
 * (chaque op s'applique au résultat du précédent : M_total = op_n * … * op_1).
 */
private fun buildColorMatrix(state: ColorCorrectionState): FloatArray {
	var m = floatArrayOf(
		1f, 0f, 0f, 0f, 0f,
		0f, 1f, 0f, 0f, 0f,
		0f, 0f, 1f, 0f, 0f,
		0f, 0f, 0f, 1f, 0f,
	)
	if (state.invert) {
		m = multiply(
			post = floatArrayOf(
				-1f, 0f, 0f, 0f, 1f,
				0f, -1f, 0f, 0f, 1f,
				0f, 0f, -1f, 0f, 1f,
				0f, 0f, 0f, 1f, 0f,
			),
			pre = m,
		)
	}
	if (state.grayscale) {
		m = multiply(
			post = floatArrayOf(
				0.213f, 0.715f, 0.072f, 0f, 0f,
				0.213f, 0.715f, 0.072f, 0f, 0f,
				0.213f, 0.715f, 0.072f, 0f, 0f,
				0f, 0f, 0f, 1f, 0f,
			),
			pre = m,
		)
	}
	if (state.brightness != 1f) {
		val b = state.brightness
		m = multiply(
			post = floatArrayOf(
				b, 0f, 0f, 0f, 0f,
				0f, b, 0f, 0f, 0f,
				0f, 0f, b, 0f, 0f,
				0f, 0f, 0f, 1f, 0f,
			),
			pre = m,
		)
	}
	if (state.contrast != 1f) {
		val c = state.contrast
		val offset = (1f - c) * 0.5f
		m = multiply(
			post = floatArrayOf(
				c, 0f, 0f, 0f, offset,
				0f, c, 0f, 0f, offset,
				0f, 0f, c, 0f, offset,
				0f, 0f, 0f, 1f, 0f,
			),
			pre = m,
		)
	}
	return m
}

/**
 * Produit de deux matrices 4x5 avec l'offset (5e colonne) traité en affine :
 * `result = post * pre` (le contenu subit d'abord [pre] puis [post]).
 */
private fun multiply(post: FloatArray, pre: FloatArray): FloatArray = FloatArray(20) { index ->
	val row = index / 5
	val col = index % 5
	var sum = 0f
	for (k in 0 until 4) sum += post[row * 5 + k] * pre[k * 5 + col]
	if (col == 4) sum += post[row * 5 + 4]
	sum
}

/** Ligne switch réutilisable du dialog. */
@Composable
private fun ToggleRow(
	label: String,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f),
		)
		Switch(checked = checked, onCheckedChange = onCheckedChange)
	}
}

/** Ligne slider réutilisable (libellé + valeur « N % ») du dialog. */
@Composable
private fun SliderRow(
	label: String,
	value: Float,
	valueText: String,
	onValueChange: (Float) -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f),
		)
		Text(
			text = valueText,
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
	Slider(
		value = value,
		onValueChange = onValueChange,
		valueRange = 0.2f..2.0f,
	)
}
