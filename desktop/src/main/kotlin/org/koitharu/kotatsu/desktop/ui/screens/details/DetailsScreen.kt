/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Détails du manga » — reproduit activity_details.xml + fragment_chapters.xml de l'app
 * d'origine : couverture avec fond flouté, badge 18+ (textView_nsfw_18), titre TitleLarge,
 * sous-titre (auteur), chips de tags, description (tronquée + « Plus »/« Réduire »), liste des
 * chapitres (chaîne « Chapitres »), barre d'actions Lire / Favoris / Télécharger (chaînes
 * exactes de values-fr/strings.xml). Modification : 2026-11-08 — centralisation des chaînes
 * (règle client : aucune chaîne en brut dans les écrans).
 * Modification : 2026-11-08 — phase P8-A (parité vidéo t≈6:07-6:45, 7:10-7:20, 9:25) :
 *  - méta enrichie sous le titre/auteur : « Source : <titre du source> », « État : <état FR> »
 *    (manga.state), « Chapitres : <N> (temps estimé) » ou « Aucun chapitre », et
 *    « Progression : <p> % » avec barre quand une position d'historique existe ;
 *  - temps de lecture estimé : N chapitres × ESTIMATED_MINUTES_PER_CHAPTER (4 min), ou
 *    chapitres restants × 4 min après une position d'historique (formule documentée ;
 *    la vidéo révèle ~3,3 min/chapitre côté Android) ;
 *  - badge « 18+ » sur la couverture (déjà présent, ContentType.HENTAI — conservé) ;
 *  - section « Mangas connexes » + « Tout afficher » (masquée si la liste est vide :
 *    échec source ou aucun résultat — runCatching côté appelant) ;
 *  - bouton « Lire ▾ » en split button : action principale = « Lire » (chapitre 1) ou
 *    « Continuer · <n> » si l'historique a une position, menu ▾ = « Lire le chapitre 1 »
 *    et « Continuer · <n> » ;
 *  - puce ★ violette dans la barre d'actions quand le manga est en favoris (vidéo t≈6:07).
 */
package org.koitharu.kotatsu.desktop.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.desktop.ui.components.percentPattern
import org.koitharu.kotatsu.desktop.ui.strings.FR_ADD_TO_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_AUTHOR
import org.koitharu.kotatsu.desktop.ui.strings.FR_BACK
import org.koitharu.kotatsu.desktop.ui.strings.FR_CHAPTERS
import org.koitharu.kotatsu.desktop.ui.strings.FR_CHAPTERS_EMPTY
import org.koitharu.kotatsu.desktop.ui.strings.FR_COLLAPSE
import org.koitharu.kotatsu.desktop.ui.strings.FR_CONTINUE
import org.koitharu.kotatsu.desktop.ui.strings.FR_DESCRIPTION
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOAD
import org.koitharu.kotatsu.desktop.ui.strings.FR_MORE
import org.koitharu.kotatsu.desktop.ui.strings.FR_NSFW
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROGRESS
import org.koitharu.kotatsu.desktop.ui.strings.FR_READ
import org.koitharu.kotatsu.desktop.ui.strings.FR_READ_FIRST_CHAPTER
import org.koitharu.kotatsu.desktop.ui.strings.FR_RELATED_MANGA
import org.koitharu.kotatsu.desktop.ui.strings.FR_SHOW_ALL
import org.koitharu.kotatsu.desktop.ui.strings.frChapterXofY
import org.koitharu.kotatsu.desktop.ui.strings.frChaptersCount
import org.koitharu.kotatsu.desktop.ui.strings.frContinueChapter
import org.koitharu.kotatsu.desktop.ui.strings.frDuration
import org.koitharu.kotatsu.desktop.ui.strings.frStateLabel

private const val DETAILS_COVER_WIDTH_PERCENT = 0.3f // layout_constraintWidth_percent=0.3
private val DETAILS_COVER_CORNER = 12.dp
private val DETAILS_HEADER_HORIZONTAL_PADDING = 16.dp
private val DETAILS_HEADER_VERTICAL_PADDING = 12.dp
private val DETAILS_BADGE_MARGIN = 8.dp // card_indicator_offset
private const val DETAILS_TAGS_LIMIT = 6

/** P8-A : estimation de temps de lecture = 4 minutes par chapitre (formule documentée). */
private const val ESTIMATED_MINUTES_PER_CHAPTER = 4

/** P8-A : largeur d'une cellule « Mangas connexes » (grid_width d'origine, 120dp). */
private val RELATED_CELL_WIDTH = 120.dp

/**
 * P8-A : couleur de la puce ★ favoris — violet de la capture vidéo (t≈6:07), hors palette
 * M3 (parité visuelle directe, comme le chip « ★ – » de l'app d'origine).
 */
private val FAVOURITE_CHIP_PURPLE = Color(0xFF5E4B6E)

/** Badge 18+ si la source est Hentai (comme DetailsScreen d'origine, isNsfw). */
private fun isNsfw(manga: Manga): Boolean =
	(manga.source as? MangaParserSource)?.contentType == ContentType.HENTAI

@Composable
public fun DetailsScreen(
	manga: Manga?,
	chapters: List<String>,
	isFavourite: Boolean,
	onBackClick: () -> Unit,
	onChapterClick: (Int) -> Unit,
	onFavouriteClick: () -> Unit,
	onDownloadClick: () -> Unit,
	onReadClick: () -> Unit,
	modifier: Modifier = Modifier,
	collapseLongDescription: Boolean = true, // P2 : « Réduire la description longue »
	// P8-A : index (0-based) du chapitre courant d'après l'historique, null si jamais lu
	continueChapterIndex: Int? = null,
	// P8-A : progression de lecture 0f..1f d'après l'historique, null si jamais lu
	historyPercent: Float? = null,
	// P8-A : mangas connexes (même genre) ; liste vide ⇒ section masquée
	relatedManga: List<Manga> = emptyList(),
	// P8-A : « Tout afficher » — null ⇒ libellé désactivé (pas de catalogue filtré disponible)
	onSeeRelated: (() -> Unit)? = null,
	// P8-A : clic sur un manga connexe
	onRelatedClick: (Manga) -> Unit = {},
	// P8-A : entrée de menu « Lire le chapitre 1 » du bouton Lire ▾
	onReadFirstChapter: () -> Unit = onReadClick,
) {
	Column(modifier = modifier.fillMaxSize()) {
		LazyColumn(modifier = Modifier.weight(1f)) {
			item {
				if (manga != null) {
					DetailsHeader(
						manga = manga,
						chaptersCount = chapters.size,
						continueChapterIndex = continueChapterIndex,
						historyPercent = historyPercent,
						onBackClick = onBackClick,
					)
				}
			}
			item {
				DescriptionSection(
					description = manga?.description.orEmpty(),
					initialExpanded = !collapseLongDescription,
				)
			}
			item {
				// chips_tags : sous la description comme activity_details.xml
				TagsSection(tags = manga?.tags.orEmpty())
			}
			item {
				// P8-A : « Mangas connexes » + « Tout afficher » (vidéo t≈6:35) ;
				// section masquée quand la liste est vide (échec source / aucun résultat)
				RelatedMangaSection(
					related = relatedManga,
					onSeeRelated = onSeeRelated,
					onRelatedClick = onRelatedClick,
				)
			}
			item {
				Text(
					text = FR_CHAPTERS,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(
						horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
						vertical = 8.dp,
					),
				)
			}
			if (chapters.isEmpty()) {
				// getEmptyState de ChaptersListViewModel (string chapters_empty)
				item {
					Text(
						text = FR_CHAPTERS_EMPTY,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(horizontal = DETAILS_HEADER_HORIZONTAL_PADDING),
					)
				}
			}
			// clé par index : les titres de chapitres peuvent être dupliqués
			// (même nom dans deux branches/volumes) → clé unique obligatoire
			itemsIndexed(chapters) { index, chapterTitle ->
				ChapterRow(title = chapterTitle, onClick = { onChapterClick(index) })
				HorizontalDivider(
					color = MaterialTheme.colorScheme.outlineVariant,
					thickness = 0.5.dp,
				)
			}
		}
		DetailsActionsBar(
			isFavourite = isFavourite,
			continueChapterNumber = continueChapterIndex?.plus(1),
			onReadClick = onReadClick,
			onReadFirstChapter = onReadFirstChapter,
			onFavouriteClick = onFavouriteClick,
			onDownloadClick = onDownloadClick,
		)
	}
}

@Composable
private fun DetailsHeader(
	manga: Manga,
	chaptersCount: Int,
	continueChapterIndex: Int?,
	historyPercent: Float?,
	onBackClick: () -> Unit,
) {
	Box(modifier = Modifier.fillMaxWidth()) {
		// Fond : couverture floutée (imageView_cover_bg d'origine)
		AsyncImage(
			model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
				.data(manga.coverUrl)
				.crossfade(true)
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.matchParentSize()
				.blur(24.dp),
		)
		Box(
			modifier = Modifier
				.matchParentSize()
				.background(
					Brush.verticalGradient(
						listOf(
							MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
							MaterialTheme.colorScheme.surface,
						),
					),
				),
		)
		Column(modifier = Modifier.fillMaxWidth()) {
			IconButton(onClick = onBackClick) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = FR_BACK,
					tint = MaterialTheme.colorScheme.onSurface,
				)
			}
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
						vertical = DETAILS_HEADER_VERTICAL_PADDING,
					),
			) {
				// Couverture : 30 % de la largeur, ratio 13:18 (activity_details.xml)
				Box(
					modifier = Modifier
						.fillMaxWidth(DETAILS_COVER_WIDTH_PERCENT)
						.aspectRatio(13f / 18f)
						.clip(RoundedCornerShape(DETAILS_COVER_CORNER))
						.background(MaterialTheme.colorScheme.secondaryContainer),
					contentAlignment = Alignment.Center,
				) {
					AsyncImage(
						model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
							.data(manga.coverUrl)
							.crossfade(true)
							.build(),
						contentDescription = manga.title,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize(),
					)
					// Badge 18+ (textView_nsfw_18) : superposé en bas à droite de la couverture
					if (isNsfw(manga)) {
						Text(
							text = FR_NSFW,
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onErrorContainer,
							modifier = Modifier
								.align(Alignment.BottomEnd)
								.padding(DETAILS_BADGE_MARGIN)
								.clip(RoundedCornerShape(4.dp))
								.background(MaterialTheme.colorScheme.errorContainer)
								.padding(horizontal = 4.dp, vertical = 2.dp),
						)
					}
				}
				Column(
					modifier = Modifier
						.weight(1f)
						.padding(start = 16.dp, end = 16.dp),
				) {
					Text(
						text = manga.title,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 3,
						overflow = TextOverflow.Ellipsis,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = manga.author.orEmpty(),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 3,
						overflow = TextOverflow.Ellipsis,
					)
					// P8-A : méta enrichie (vidéo t≈6:07) — table « Source / État / Chapitres /
					// Progression » sous le titre et l'auteur, comme activity_details.xml
					DetailsMetaBlock(
						manga = manga,
						chaptersCount = chaptersCount,
						continueChapterIndex = continueChapterIndex,
						historyPercent = historyPercent,
					)
				}
			}
		}
	}
}

/** P8-A : table méta « Source / État / Chapitres / Progression » (vidéo t≈6:07 et t≈9:25). */
@Composable
private fun DetailsMetaBlock(
	manga: Manga,
	chaptersCount: Int,
	continueChapterIndex: Int?,
	historyPercent: Float?,
) {
	Spacer(modifier = Modifier.height(8.dp))
	// « Source : <titre du source> » — nom affiché de la source d'origine
	MetaRow(
		label = org.koitharu.kotatsu.desktop.ui.strings.FR_SOURCE,
		value = (manga.source as? MangaParserSource)?.title ?: manga.source.name,
	)
	// « Auteur : » — valeur déjà affichée sous le titre ; ligne dédiée seulement si distincte
	val author = manga.author
	if (!author.isNullOrBlank() && author != manga.title) {
		MetaRow(label = FR_AUTHOR, value = author)
	}
	// « État : <En cours/Terminé/…> » (manga.state, libellés values-fr) — masqué si inconnu
	frStateLabel(manga.state)?.let { stateLabel ->
		MetaRow(label = org.koitharu.kotatsu.desktop.ui.strings.FR_STATE, value = stateLabel)
	}
	// « Chapitres : » — « Aucun chapitre », « <N> chapitres (1 h 26 m) » ou
	// « Chapitre <n> sur <N> (1 h 22 m) » après lecture (vidéo t≈7:10 puis t≈9:25)
	val chaptersValue = when {
		chaptersCount == 0 -> org.koitharu.kotatsu.desktop.ui.strings.FR_NO_CHAPTERS
		continueChapterIndex != null -> {
			// Temps restant estimé : chapitres restants (à partir du chapitre courant) × 4 min
			val remainingMinutes = (chaptersCount - continueChapterIndex) * ESTIMATED_MINUTES_PER_CHAPTER
			frChapterXofY(
				current = continueChapterIndex + 1,
				total = chaptersCount,
			) + " (${frDuration(remainingMinutes)})"
		}
		else -> {
			val totalMinutes = chaptersCount * ESTIMATED_MINUTES_PER_CHAPTER
			frChaptersCount(chaptersCount) + " (${frDuration(totalMinutes)})"
		}
	}
	MetaRow(
		label = FR_CHAPTERS,
		value = chaptersValue,
	)
	// « Progression : [barre] 4 % » (vidéo t≈9:25) — seulement si l'historique a une position
	if (historyPercent != null && chaptersCount > 0) {
		MetaRowWithProgress(percent = historyPercent)
	}
}

/** Ligne « Libellé :  valeur » de la méta enrichie (parité capture t≈6:07). */
@Composable
private fun MetaRow(label: String, value: String) {
	Row(modifier = Modifier.padding(vertical = 2.dp)) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.width(110.dp),
		)
		Text(
			text = value,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
		)
	}
}

/** Ligne « Progression : [barre] 4 % » (parité capture t≈9:25). */
@Composable
private fun MetaRowWithProgress(percent: Float) {
	Row(
		modifier = Modifier.padding(vertical = 2.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = FR_PROGRESS,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.width(110.dp),
		)
		LinearProgressIndicator(
			progress = { percent.coerceIn(0f, 1f) },
			modifier = Modifier
				.weight(1f)
				.padding(end = 8.dp),
		)
		Text(
			// « 4 % » — même motif que les indicateurs de listes (percentPattern)
			text = percentPattern(percent),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}

/**
 * P8-A : section « Mangas connexes » + « Tout afficher » (vidéo t≈6:35) — rangée horizontale
 * de cartes grille 120dp. Masquée si [related] est vide. « Tout afficher » est désactivé
 * (gris) quand [onSeeRelated] est null (pas de catalogue filtré à ouvrir).
 */
@Composable
private fun RelatedMangaSection(
	related: List<Manga>,
	onSeeRelated: (() -> Unit)?,
	onRelatedClick: (Manga) -> Unit,
) {
	if (related.isEmpty()) return
	Column(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
					vertical = 8.dp,
				),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = FR_RELATED_MANGA,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Text(
				text = FR_SHOW_ALL,
				style = MaterialTheme.typography.labelLarge,
				color = if (onSeeRelated != null) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // désactivé (M3)
				},
				modifier = Modifier.clickable(enabled = onSeeRelated != null) {
					onSeeRelated?.invoke()
				},
			)
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState())
				.padding(horizontal = DETAILS_HEADER_HORIZONTAL_PADDING),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			for (item in related) {
				Box(modifier = Modifier.width(RELATED_CELL_WIDTH)) {
					org.koitharu.kotatsu.desktop.ui.components.MangaGridCard(
						manga = item,
						onClick = { onRelatedClick(item) },
					)
				}
			}
		}
	}
}

/** Chips des tags (chips_tags d'origine, sous la description). */
@Composable
private fun TagsSection(tags: Set<org.koitharu.kotatsu.parsers.model.MangaTag>) {
	if (tags.isEmpty()) return
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = DETAILS_HEADER_HORIZONTAL_PADDING),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		for (tag in tags.take(DETAILS_TAGS_LIMIT)) {
			AssistChip(
				onClick = {},
				label = {
					Text(
						text = tag.title,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				},
			)
		}
	}
}

@Composable
private fun DescriptionSection(description: String, initialExpanded: Boolean = false) {
	if (description.isBlank()) return
	var expanded by remember { mutableStateOf(initialExpanded) }
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
				vertical = 8.dp,
			),
	) {
		Text(
			text = FR_DESCRIPTION,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)
		Text(
			text = description,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = if (expanded) Int.MAX_VALUE else 4,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 4.dp),
		)
		Text(
			text = if (expanded) FR_COLLAPSE else FR_MORE,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier
				.padding(top = 4.dp)
				.clickable { expanded = !expanded },
		)
	}
}

@Composable
private fun ChapterRow(title: String, onClick: () -> Unit) {
	Text(
		text = title,
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(
				horizontal = DETAILS_HEADER_HORIZONTAL_PADDING,
				vertical = 12.dp,
			),
	)
}

/**
 * P8-A : barre d'actions — bouton « Lire ▾ » en split button (vidéo t≈7:10 « Lire | ▾ »,
 * t≈9:25 « Continuer · 2 | ▾ ») : action principale = reprendre au chapitre courant (ou
 * chapitre 1), menu ▾ = « Lire le chapitre 1 » + « Continuer · <n> ». La puce ★ violette
 * n'apparaît que si le manga est en favoris (vidéo t≈6:07).
 */
@Composable
private fun DetailsActionsBar(
	isFavourite: Boolean,
	continueChapterNumber: Int?,
	onReadClick: () -> Unit,
	onReadFirstChapter: () -> Unit,
	onFavouriteClick: () -> Unit,
	onDownloadClick: () -> Unit,
) {
	Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			// Split button « Lire | ▾ » (comme le sheet des chapitres de l'app d'origine)
			Row(
				modifier = Modifier.weight(1f),
				horizontalArrangement = Arrangement.spacedBy(1.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Button(
					onClick = onReadClick,
					modifier = Modifier.weight(1f),
					shape = RoundedCornerShape(
						topStart = 20.dp,
						bottomStart = 20.dp,
						topEnd = 4.dp,
						bottomEnd = 4.dp,
					),
				) {
					Text(
						text = if (continueChapterNumber != null) {
							frContinueChapter(continueChapterNumber)
						} else {
							FR_READ
						},
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
				// Partie flèche du split button : ouvre le menu « Lire le chapitre 1 » /
				// « Continuer · <n> » (vidéo t≈7:15, t≈9:25)
				ReadMenuButton(
					continueChapterNumber = continueChapterNumber,
					onReadFirstChapter = onReadFirstChapter,
					onContinue = onReadClick,
				)
			}
			// Puce ★ violette (parité capture t≈6:07) : le manga est en favoris
			if (isFavourite) {
				Surface(
					shape = RoundedCornerShape(50),
					color = FAVOURITE_CHIP_PURPLE,
					modifier = Modifier.size(40.dp),
					contentColor = Color.White,
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Filled.Star,
							contentDescription = FR_ADD_TO_FAVOURITES,
							modifier = Modifier.size(20.dp),
						)
					}
				}
			}
			IconButton(onClick = onFavouriteClick) {
				Icon(
					imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
					contentDescription = FR_ADD_TO_FAVOURITES,
					tint = MaterialTheme.colorScheme.primary,
				)
			}
			IconButton(onClick = onDownloadClick) {
				Icon(
					imageVector = Icons.Filled.Download,
					contentDescription = FR_DOWNLOAD,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
	}
}

/** P8-A : partie ▾ du split button « Lire » avec menu (Lire le chapitre 1 / Continuer · n). */
@Composable
private fun ReadMenuButton(
	continueChapterNumber: Int?,
	onReadFirstChapter: () -> Unit,
	onContinue: () -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	Box {
		Button(
			onClick = { expanded = true },
			shape = RoundedCornerShape(
				topStart = 4.dp,
				bottomStart = 4.dp,
				topEnd = 20.dp,
				bottomEnd = 20.dp,
			),
			contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
			modifier = Modifier.size(width = 40.dp, height = 40.dp),
		) {
			Icon(
				imageVector = Icons.Filled.ArrowDropDown,
				contentDescription = FR_READ,
			)
		}
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
		) {
			DropdownMenuItem(
				text = { Text(text = FR_READ_FIRST_CHAPTER) },
				onClick = {
					expanded = false
					onReadFirstChapter()
				},
			)
			if (continueChapterNumber != null) {
				DropdownMenuItem(
					text = { Text(text = frContinueChapter(continueChapterNumber)) },
					onClick = {
						expanded = false
						onContinue()
					},
				)
			}
		}
	}
}
