/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Indicateur circulaire de progression de lecture — reproduit history/ui/util/ReadingProgressView
 * + ReadingProgressDrawable avec le style ProgressDrawable d'origine :
 *   fillColor = colorPrimary, strokeColor = colorSurfaceVariant, strokeWidth = 4dp,
 *   textColor = colorOnPrimary, textSize = 9sp (autoFit), taille = card_indicator_size (32dp),
 *   marge = card_indicator_offset (8dp). Texte par défaut = percent_string_pattern (« %1$s%% »).
 */
package org.koitharu.kotatsu.desktop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public val CARD_INDICATOR_SIZE: Dp = 32.dp
public val CARD_INDICATOR_OFFSET: Dp = 8.dp
private const val FULL_ARC_DEGREES = 360f
private const val START_ARC_DEGREES = -90f

/**
 * Pastille de progression (0f..1f) avec texte centré, fidèle au ProgressDrawable d'origine.
 * [text] : par ex. « 27% » (PERCENT_READ) ; laisser vide pour la pastille seule.
 */
@Composable
public fun ReadingProgressIndicator(
	percent: Float,
	modifier: Modifier = Modifier,
	size: Dp = CARD_INDICATOR_SIZE,
	text: String = percentPattern(percent),
) {
	val fillColor = MaterialTheme.colorScheme.primary
	val trackColor = MaterialTheme.colorScheme.surfaceVariant
	val textColor = MaterialTheme.colorScheme.onPrimary
	val stroke = 4.dp
	val coercedPercent = percent.coerceIn(0f, 1f)

	Box(
		modifier = modifier.size(size),
		contentAlignment = Alignment.Center,
	) {
		Canvas(modifier = Modifier.size(size)) {
			val strokePx = stroke.toPx()
			val inset = strokePx / 2f
			val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
			val topLeft = Offset(inset, inset)
			drawArc(
				color = trackColor,
				startAngle = 0f,
				sweepAngle = FULL_ARC_DEGREES,
				useCenter = false,
				topLeft = topLeft,
				size = arcSize,
				style = Stroke(width = strokePx, cap = StrokeCap.Butt),
			)
			if (coercedPercent > 0f) {
				drawArc(
					color = fillColor,
					startAngle = START_ARC_DEGREES,
					sweepAngle = coercedPercent * FULL_ARC_DEGREES,
					useCenter = false,
					topLeft = topLeft,
					size = arcSize,
					style = Stroke(width = strokePx, cap = StrokeCap.Butt),
				)
			}
		}
		if (text.isNotEmpty()) {
			Text(
				text = text,
				color = textColor,
				fontSize = 9.sp,
				textAlign = TextAlign.Center,
				maxLines = 1,
			)
		}
	}
}

/** percent_string_pattern d'origine : « %1$s%% », affiché sans décimales comme ReadingProgressDrawable. */
public fun percentPattern(percent: Float): String = "${(percent * 100).toInt()}%"
