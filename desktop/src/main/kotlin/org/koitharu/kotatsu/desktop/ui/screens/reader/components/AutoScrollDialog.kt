/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P7 vague 1 :
 * composants PURS « Défilement automatique » du lecteur (vidéo de référence t≈8:35-9:19), sans
 * dépendance à ReaderScreen ni aux prefs (l'intégrateur P7-A branchera l'état en vague 2).
 * Dialog M3 (switch Activer + slider Vitesse au format vidéo « Vitesse xN.N » + note),
 * bouton de commande flottant Play/Pause et helper autoScrollStep pour la boucle de défilement.
 */
package org.koitharu.kotatsu.desktop.ui.screens.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.strings.FR_AUTO_SCROLL
import org.koitharu.kotatsu.desktop.ui.strings.FR_AUTO_SCROLL_FAB_NOTE
import org.koitharu.kotatsu.desktop.ui.strings.FR_AUTO_SCROLL_PAUSE
import org.koitharu.kotatsu.desktop.ui.strings.FR_AUTO_SCROLL_PLAY
import org.koitharu.kotatsu.desktop.ui.strings.FR_ENABLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_OK
import org.koitharu.kotatsu.desktop.ui.strings.FR_SPEED
import org.koitharu.kotatsu.desktop.ui.strings.frAutoScrollSpeed

/**
 * État du défilement automatique du lecteur (composant pur, l'intégrateur persiste s'il veut
 * le mémoriser — aucun stockage ici).
 *
 * @property enabled réglage utilisateur « Activer » : contrôle l'AFFICHAGE du bouton flottant.
 * @property speed vitesse de défilement en unités « xN.N » (plage utile 0.5..8.0).
 * @property isRunning true si le défilement est en cours (Play ↔ Pause du bouton flottant).
 */
public data class AutoScrollState(
	public val enabled: Boolean = false,
	public val speed: Float = 4f,
	public val isRunning: Boolean = false,
)

/**
 * Dialog M3 « Défilement automatique » : switch « Activer », slider « Vitesse » (0.5..8.0)
 * avec libellé au format vidéo « Vitesse xN.N », note « Défilement via le bouton de commande
 * flottant », bouton « OK ».
 *
 * Contrat d'intégration (P7-A) : les changements sont émis EN DIRECT via [onChange] ;
 * quand [AutoScrollState.enabled] passe à true, l'intégrateur affiche [AutoScrollFab]
 * dans le coin bas-droit du lecteur.
 *
 * @param state état courant affiché (source de vérité détenue par l'intégrateur).
 * @param onChange appelé à chaque modification avec le nouvel état complet.
 * @param onDismiss fermeture du dialog (clic hors fenêtre, « OK »).
 */
@Composable
public fun AutoScrollDialog(
	state: AutoScrollState,
	onChange: (AutoScrollState) -> Unit,
	onDismiss: () -> Unit,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = FR_AUTO_SCROLL) },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = FR_ENABLE,
						style = MaterialTheme.typography.bodyLarge,
						modifier = Modifier.weight(1f),
					)
					Switch(
						checked = state.enabled,
						onCheckedChange = { checked ->
							// désactiver stoppe aussi la boucle en cours
							onChange(state.copy(enabled = checked, isRunning = state.isRunning && checked))
						},
					)
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = FR_SPEED,
						style = MaterialTheme.typography.bodyLarge,
						modifier = Modifier.weight(1f),
					)
					Text(
						text = frAutoScrollSpeed(state.speed),
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
				Slider(
					value = state.speed,
					onValueChange = { value ->
						onChange(state.copy(speed = value.coerceIn(MIN_SPEED, MAX_SPEED)))
					},
					valueRange = MIN_SPEED..MAX_SPEED,
				)
				Text(
					text = FR_AUTO_SCROLL_FAB_NOTE,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(text = FR_OK)
			}
		},
	)
}

/** Plage du slider « Vitesse » (unités « xN.N » de la vidéo). */
private const val MIN_SPEED = 0.5f
private const val MAX_SPEED = 8.0f

/**
 * Bouton de commande flottant du défilement automatique : icône Play quand arrêté, Pause quand
 * en cours. VISIBLE SEULEMENT quand [AutoScrollState.enabled] (aucun rendu sinon).
 *
 * Contrat d'intégration (P7-A) : à placer dans la Box racine du lecteur avec
 * `Modifier.align(Alignment.BottomEnd).padding(16.dp)` ; [onClick] bascule isRunning
 * (l'intégrateur gère la boucle de défilement via [autoScrollStep]).
 *
 * @param state état courant (enabled contrôle la visibilité, isRunning contrôle l'icône).
 * @param onClick appelé au clic sur le bouton.
 */
@Composable
public fun AutoScrollFab(
	state: AutoScrollState,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	if (!state.enabled) return
	val icon: ImageVector = if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow
	FloatingActionButton(onClick = onClick, modifier = modifier) {
		Icon(
			imageVector = icon,
			contentDescription = if (state.isRunning) FR_AUTO_SCROLL_PAUSE else FR_AUTO_SCROLL_PLAY,
		)
	}
}

/**
 * Décalage en PIXELS à appliquer pour un tick d'horloge de 16 ms (~60 FPS) :
 * `speed * 1f` px par tick, soit environ `speed * 62.5` px/s (x4.0 ≈ 250 px/s,
 * x8.0 ≈ 500 px/s, x0.5 ≈ 31 px/s). Retourne 0f si désactivé ou non démarré.
 *
 * Contrat d'intégration (P7-A) — exemple avec un webtoon (LazyListState) :
 * ```
 * LaunchedEffect(state.enabled, state.isRunning, state.speed) {
 *     while (state.enabled && state.isRunning) {
 *         withFrameMillis { }                       // un tick par frame (~16 ms)
 *         listState.scrollBy(autoScrollStep(state)) // défilement continu en px
 *     }
 * }
 * ```
 * ou pour un pager vertical avec [androidx.compose.foundation.pager.PagerState], convertir
 * le décalage en sauts de page (cumuler `autoScrollStep` et appeler
 * `pagerState.currentPage`/`snapToPage` quand un seuil est dépassé). Le helper est suspend
 * pour rester compatible avec une boucle de coroutine (withFrameMillis).
 *
 * @param state état courant du défilement automatique.
 * @return décalage en px pour ce tick, 0f si le défilement ne doit pas avancer.
 */
public suspend fun autoScrollStep(state: AutoScrollState): Float {
	if (!state.enabled || !state.isRunning) return 0f
	return state.speed * PX_PER_SPEED_UNIT_PER_TICK
}

/** Décalage en px par unité de vitesse et par tick de 16 ms (voir [autoScrollStep]). */
private const val PX_PER_SPEED_UNIT_PER_TICK = 1f
