/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P4 : écran
 * « Vérification du journal des nouveaux chapitres » (Paramètres > « Nouveaux chapitres » >
 * Dépannage), reproduit de la vidéo (video-analysis/half1-inventory.md, frames t≈3:20) :
 * liste de journal avec, par entrée, vignette + source + horodatage relatif « Il y a … à HH:mm »
 * + « N nouveaux chapitres » ou le message d'erreur ; état vide explicatif.
 * NOTE : le backend du journal n'existe pas encore côté desktop (aucune persistance des
 * passages de TrackerScheduler) — les [FeedLogEntry] sont injectés par le appelant ;
 * l'intégrateur principal branchera les vraies données (capture des événements de
 * TrackerScheduler.runOnce) plus tard. Les chaînes FR sont des private const val locales.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateView
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar

// --- Chaînes FR (parité app Android en français, libellés vidéo) ---
private const val FR_SCREEN_TITLE = "Vérification du journal des nouveaux chapitres"
private const val FR_EMPTY_TITLE = "Aucune vérification enregistrée"
private const val FR_EMPTY_TEXT =
	"Le journal de la vérification en arrière-plan des nouveaux chapitres est vide. " +
		"Les vérifications effectuées apparaîtront ici."
private const val FR_NEW_CHAPTERS_ONE = "1 nouveau chapitre"
private const val FR_NEW_CHAPTERS_MANY = "%d nouveaux chapitres"
private const val FR_AT = " à "
private const val FR_TIME_PATTERN = "HH:mm"

/** Vignette de couverture de remplacement (l'original affiche la couverture du manga). */
private val LOG_THUMBNAIL_SIZE = 48.dp
private val LOG_THUMBNAIL_CORNER = 6.dp
private val LOG_ROW_HORIZONTAL_PADDING = 16.dp
private val LOG_ROW_VERTICAL_PADDING = 8.dp
private val LOG_TEXT_SPACING = 2.dp

/**
 * Une entrée du journal de vérification des nouveaux chapitres.
 *
 * @param timestamp horodatage du passage (epoch millis).
 * @param source nom du manga / de la source vérifiée (donnée d'affichage libre ;
 * l'écran l'affiche comme titre de ligne).
 * @param foundChapters nombre de nouveaux chapitres trouvés (0 si aucun / en erreur).
 * @param error message d'erreur si la vérification a échoué (null sinon).
 */
public data class FeedLogEntry(
	val timestamp: Long,
	val source: String,
	val foundChapters: Int,
	val error: String? = null,
)

/**
 * Écran « Vérification du journal des nouveaux chapitres » (section Dépannage).
 * Affiche la liste chronologique des [entries] passées par l'appelant, ou un état vide
 * explicatif. NOTE : le backend du journal n'existe pas encore côté desktop (aucune
 * persistance des passages de TrackerScheduler) — l'intégrateur principal branchera
 * les vraies données plus tard.
 */
@Composable
public fun FeedLogScreen(
	entries: List<FeedLogEntry>,
	onBackClick: () -> Unit,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_SCREEN_TITLE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		if (entries.isEmpty()) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				EmptyStateView(
					icon = Icons.Outlined.History,
					textPrimary = FR_EMPTY_TITLE,
					textSecondary = FR_EMPTY_TEXT,
				)
			}
		} else {
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(entries) { entry ->
					FeedLogRow(entry = entry)
				}
			}
		}
	}
}

/** Ligne de journal : vignette + source + horodatage « Il y a … à HH:mm » + résultat. */
@Composable
private fun FeedLogRow(entry: FeedLogEntry) {
	val instant = Instant.ofEpochMilli(entry.timestamp)
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				horizontal = LOG_ROW_HORIZONTAL_PADDING,
				vertical = LOG_ROW_VERTICAL_PADDING,
			),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Vignette (couverture indisponible sans référence manga : initiale de la source)
		Surface(
			color = MaterialTheme.colorScheme.surfaceContainerHighest,
			modifier = Modifier
				.size(LOG_THUMBNAIL_SIZE)
				.clip(RoundedCornerShape(LOG_THUMBNAIL_CORNER)),
		) {
			Box(contentAlignment = Alignment.Center) {
				Text(
					text = entry.source.take(1).uppercase(),
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = LOG_ROW_HORIZONTAL_PADDING),
		) {
			Text(
				text = entry.source,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = formatLogTimestamp(instant),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(top = LOG_TEXT_SPACING),
			)
			Text(
				text = entry.error ?: frNewChaptersCount(entry.foundChapters),
				style = MaterialTheme.typography.bodyMedium,
				color = if (entry.error != null) {
					MaterialTheme.colorScheme.error
				} else {
					MaterialTheme.colorScheme.onSurfaceVariant
				},
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(top = LOG_TEXT_SPACING),
			)
		}
	}
}

/** « N nouveau(x) chapitre(s) » (pluriel d'origine). */
private fun frNewChaptersCount(count: Int): String = when (count) {
	1 -> FR_NEW_CHAPTERS_ONE
	else -> FR_NEW_CHAPTERS_MANY.format(count)
}

/** Horodatage relatif « Il y a 3 heures à 16:24 » (format de la vidéo). */
private fun formatLogTimestamp(instant: Instant): String {
	val zoned = instant.atZone(ZoneId.systemDefault())
	return buildString {
		append(frRelativeTime(zoned))
		append(FR_AT)
		append(DateTimeFormatter.ofPattern(FR_TIME_PATTERN).format(zoned))
	}
}

/**
 * Temps relatif FR minimal (le DateTimeAgo.format() commun affiche des libellés EN —
 * cette variante FR privée garde la parité française de la vidéo).
 */
private fun frRelativeTime(dateTime: java.time.ZonedDateTime): String {
	val minutes = java.time.Duration.between(dateTime, java.time.ZonedDateTime.now()).toMinutes()
	val absolute = kotlin.math.abs(minutes)
	return when {
		absolute < 1 -> FR_AGO_JUST_NOW
		absolute < 60 -> FR_AGO_MINUTES.format(absolute)
		absolute < 24 * 60 -> FR_AGO_HOURS.format(absolute / 60)
		absolute < 7 * 24 * 60 -> FR_AGO_DAYS.format(absolute / (24 * 60))
		else -> java.time.format.DateTimeFormatter
			.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
			.format(dateTime.toLocalDate())
	}
}

private const val FR_AGO_JUST_NOW = "À l'instant"
private const val FR_AGO_MINUTES = "Il y a %d minutes"
private const val FR_AGO_HOURS = "Il y a %d heures"
private const val FR_AGO_DAYS = "Il y a %d jours"
