/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Créé : 2026-11-08 — phase P2 : rangée
 * horizontale de cartes de palette de la préférence « Schéma de couleurs » de l'écran
 * Apparence (frames 1:40-2:25 de la vidéo de référence). Chaque carte dessine un aperçu du
 * schéma (texte « Abc » + petites barres + pastille ronde) avec les VRAIES couleurs de la
 * palette correspondante (voir desktop/ui/theme/Palettes.kt), le nom du schéma étant affiché
 * sous la carte. La carte sélectionnée porte une bordure primaire de 2 dp.
 */
package org.koitharu.kotatsu.desktop.ui.components.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.theme.paletteFor

/** Clé → nom affiché des schémas de couleurs (noms propres, non traduits). */
public val COLOR_SCHEME_TITLES: Map<String, String> = mapOf(
	"DEFAULT" to "Totoro",
	"ASUKA" to "Asuka",
	"MION" to "Mion",
	"RIKKA" to "Rikka",
	"SAKURA" to "Sakura",
	"MAMIMI" to "Mamimi",
	"KANADE" to "Kanade",
	"ITSUKA" to "Itsuka",
	"MIKU" to "Miku",
)

/** Ordre d'affichage des schémas, identique à la liste de l'écran Apparence d'origine. */
private val DISPLAYED_SCHEMES: List<String> = listOf(
	"DEFAULT",
	"ASUKA",
	"MION",
	"RIKKA",
	"SAKURA",
	"MAMIMI",
	"KANADE",
	"ITSUKA",
	"MIKU",
)

private val CARD_WIDTH = 76.dp
private val CARD_PREVIEW_HEIGHT = 56.dp
private val CARD_CORNER = 12.dp

/**
 * Rangée horizontale scrollable des cartes de palette (« Schéma de couleurs », écran
 * Apparence). [dark] sélectionne les variantes claire/sombre de chaque palette pour
 * l'aperçu, [selected] est la clé du schéma actif.
 */
@Composable
public fun ColorSchemeCardsRow(
	selected: String,
	dark: Boolean,
	onSelect: (String) -> Unit,
	modifier: Modifier = Modifier,
	schemes: List<String> = DISPLAYED_SCHEMES,
) {
	LazyRow(
		modifier = modifier.fillMaxWidth(),
		contentPadding = PaddingValues(horizontal = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		items(schemes, key = { it }) { scheme ->
			ColorSchemeCard(
				scheme = scheme,
				title = COLOR_SCHEME_TITLES[scheme] ?: scheme,
				dark = dark,
				selected = scheme == selected,
				onClick = { onSelect(scheme) },
			)
		}
	}
}

/**
 * Carte de palette individuelle : aperçu aux couleurs du schéma (fond surface, « Abc » en
 * onSurface, barres primary/secondary/tertiary, pastille ronde primary) + nom du schéma
 * en dessous.
 */
@Composable
public fun ColorSchemeCard(
	scheme: String,
	title: String,
	dark: Boolean,
	selected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val palette = paletteFor(scheme, dark)
	val surface = palette.getValue("surface")
	val onSurface = palette.getValue("onSurface")
	val primary = palette.getValue("primary")
	val secondary = palette.getValue("secondary")
	val tertiary = palette.getValue("tertiary")
	Column(
		modifier = modifier.width(CARD_WIDTH),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box(
			modifier = Modifier
				.width(CARD_WIDTH)
				.height(CARD_PREVIEW_HEIGHT)
				.clip(RoundedCornerShape(CARD_CORNER))
				.background(surface)
				.border(
					width = if (selected) 2.dp else 1.dp,
					color = if (selected) primary else MaterialTheme.colorScheme.outlineVariant,
					shape = RoundedCornerShape(CARD_CORNER),
				)
				.clickable(onClick = onClick)
				.padding(8.dp),
		) {
			Column(modifier = Modifier.fillMaxSize()) {
				Text(
					text = "Abc",
					style = MaterialTheme.typography.labelLarge,
					color = onSurface,
				)
				Spacer(modifier = Modifier.weight(1f))
				Row(verticalAlignment = Alignment.CenterVertically) {
					PreviewBar(color = primary)
					Spacer(modifier = Modifier.width(3.dp))
					PreviewBar(color = secondary)
					Spacer(modifier = Modifier.width(3.dp))
					PreviewBar(color = tertiary)
					Spacer(modifier = Modifier.weight(1f))
					Box(
						modifier = Modifier
							.size(12.dp)
							.clip(CircleShape)
							.background(primary),
					)
				}
			}
		}
		Text(
			text = title,
			style = MaterialTheme.typography.labelMedium,
			color = if (selected) primary else MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
			modifier = Modifier.padding(top = 4.dp),
		)
	}
}

/** Petite barre arrondie de l'aperçu (14 x 5 dp). */
@Composable
private fun PreviewBar(color: Color) {
	Box(
		modifier = Modifier
			.size(width = 14.dp, height = 5.dp)
			.clip(RoundedCornerShape(2.5.dp))
			.background(color),
	)
}
