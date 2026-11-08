/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P8-A :
 * dialog « Ajouter aux favoris » (vidéo t≈7:15-7:20) — vignette + titre du manga, cases à
 * cocher des catégories favorites (cloche 🔔 après le nom si la catégorie est notifiée,
 * champ `track` de FavouriteCategoryEntity), boutons « Gérer » et « Terminé ».
 * Composant PUR : aucune dépendance à la DB — l'appelant (bloc détails de Main.kt) lit
 * FavouritesDao.observeCategories(mangaId) / observeFavouriteCategories() et écrit via
 * LibraryRepository.addFavourite/removeFavourite (qui passent par FavouritesDao).
 * Note : « Gérer » est désactivé tant qu'il n'existe pas d'écran de gestion des catégories
 * (onManageClick = null) — l'écran favoris/categories est du périmètre P8-B.
 */
package org.koitharu.kotatsu.desktop.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.favourites.data.FavouriteCategoryEntity
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.desktop.ui.strings.FR_ADD_TO_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_DONE
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE

/** Dimensions de la vignette d'en-tête du dialog (parité capture t≈7:15). */
private val DIALOG_THUMBNAIL_SIZE = 48.dp
private val DIALOG_THUMBNAIL_CORNER = 8.dp

/**
 * Dialog « Ajouter aux favoris » — reproduction de la capture vidéo t≈7:15-7:20 :
 *  - en-tête : vignette de couverture + titre du manga ;
 *  - une case à cocher par catégorie favorite (triées par sort_key du DAO) ; l'icône cloche
 *    est affichée après le nom quand la catégorie est notifiée (`track` = true) ;
 *  - bouton « Gérer » (désactivé si [onManageClick] est null : pas d'écran de gestion
 *    disponible) et bouton « Terminé ».
 *
 * Composant pur : les cases émettent [onCheckedChange](categoryId, checked) EN DIRECT
 * (la persistance est faite par l'appelant via les méthodes DAO) ; [onConfirm] valide et
 * ferme, [onDismiss] ferme sans autre effet (les changements déjà émis restent appliqués,
 * comme le dialog d'origine).
 *
 * @param manga manga courant (vignette + titre).
 * @param categories catégories favorites existantes (FavouriteCategoriesDao.observeAll).
 * @param checkedIds ids des catégories contenant déjà le manga (FavouritesDao.observeCategories).
 */
@Composable
public fun FavoritesCategoriesDialog(
	manga: Manga,
	categories: List<FavouriteCategoryEntity>,
	checkedIds: Set<Long>,
	onCheckedChange: (categoryId: Long, checked: Boolean) -> Unit,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
	onManageClick: (() -> Unit)? = null,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = FR_ADD_TO_FAVOURITES) },
		text = {
			Column {
				// En-tête : vignette + titre (comme le dialog d'origine, capture t≈7:15)
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					Box(
						modifier = Modifier
							.size(DIALOG_THUMBNAIL_SIZE)
							.clip(RoundedCornerShape(DIALOG_THUMBNAIL_CORNER))
							.background(MaterialTheme.colorScheme.secondaryContainer),
					) {
						AsyncImage(
							model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
								.data(manga.coverUrl)
								.crossfade(true)
								.build(),
							contentDescription = manga.title,
							contentScale = ContentScale.Crop,
							modifier = Modifier.size(DIALOG_THUMBNAIL_SIZE),
						)
					}
					Text(
						text = manga.title,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 3,
						overflow = TextOverflow.Ellipsis,
					)
				}
				// Cases à cocher des catégories, cloche si la catégorie est notifiée (track)
				// (liste vide ⇒ aucune case : les catégories par défaut « Lire plus tard » /
				// « Hentai » sont créées par l'init de la bibliothèque, hors périmètre P8-A)
				for (category in categories) {
					val checked = checkedIds.contains(category.categoryId.toLong())
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onCheckedChange(category.categoryId.toLong(), !checked) },
						verticalAlignment = Alignment.CenterVertically,
					) {
						Checkbox(
							checked = checked,
							onCheckedChange = { onCheckedChange(category.categoryId.toLong(), it == true) },
						)
						Text(
							text = category.title,
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)
						if (category.track) {
							// 🔔 : catégorie suivie par les notifications nouveaux chapitres
							Icon(
								imageVector = Icons.Filled.Notifications,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.onSurfaceVariant,
								modifier = Modifier
									.padding(start = 4.dp)
									.size(16.dp),
							)
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(text = FR_DONE)
			}
		},
		dismissButton = {
			// « Gérer » : désactivé tant qu'il n'y a pas d'écran de gestion des catégories
			// (onManageClick = null, cf. en-tête de fichier)
			TextButton(
				onClick = { onManageClick?.invoke() },
				enabled = onManageClick != null,
			) {
				Text(text = FR_MANAGE)
			}
		},
	)
}
