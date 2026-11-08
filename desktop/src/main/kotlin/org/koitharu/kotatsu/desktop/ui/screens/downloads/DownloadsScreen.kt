/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Téléchargements » — reproduit DownloadsActivity (activity_downloads.xml) :
 * barre de titre + menu (opt_downloads : Pause, Resume, Cancel all, Remove completed),
 * liste de cartes item_download.xml (carte remplie, couverture 64dp/coins 8dp, titre
 * TitleSmall, statut + détails BodySmall, pourcentage « %1$s%% » à 1 décimale,
 * LinearProgressIndicator, liste de chapitres dépliable item_chapter_download.xml
 * hauteur max 240dp, boutons outline Pause/Resume/Retry/Skip/Skip all/Cancel alignés à
 * droite) et état vide (item_empty_state + ic_empty_common, « You don't have any downloads »).
 *
 * Les données viennent en paramètres (le service de téléchargement est porté ailleurs),
 * comme CatalogScreen reçoit `fetchList`.
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 */
package org.koitharu.kotatsu.desktop.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_CANCEL_ALL
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_PAUSE
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_REMOVE_COMPLETED
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_RESUME
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_RETRY
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_SKIP
import org.koitharu.kotatsu.desktop.ui.strings.FR_ACTION_SKIP_ALL
import org.koitharu.kotatsu.desktop.ui.strings.FR_CD_BACK
import org.koitharu.kotatsu.desktop.ui.strings.FR_CD_MORE_OPTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_EMPTY_PRIMARY
import org.koitharu.kotatsu.desktop.ui.strings.FR_DOWNLOADS_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_CANCELED
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_COMPLETE
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_DOWNLOADING
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_ERROR
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_PAUSED
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_QUEUED
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_STUCK
import org.koitharu.kotatsu.desktop.ui.strings.FR_STATUS_UNKNOWN
import org.koitharu.kotatsu.parsers.model.Manga
import java.time.Duration
import java.time.Instant
import java.util.UUID

// percent_string_pattern = « %1$s%% » avec 1 décimale (parsers.util.format(1))
private fun percentText(percent: Float): String = "%.1f%%".format(percent * 100f)

private val CARD_MARGIN = 8.dp // list_spacing_normal (padding du RecyclerView)
private val CARD_COVER_SIZE = 64.dp // item_download.xml
private val CARD_COVER_CORNER = 8.dp // ShapeAppearanceOverlay.Kotatsu.Cover.Medium
private val CARD_INNER_MARGIN = 12.dp
private val CARD_TITLE_MARGIN = 16.dp
private val CHAPTERS_MAX_HEIGHT = 240.dp // app:maxHeight du NestedRecyclerView
private val CHAPTER_BADGE_SIZE = 24.dp
private val ACTION_GAP = 8.dp // flow_horizontalGap

/** États miroir de WorkInfo.State + isPaused (DownloadItemModel d'origine). */
public enum class DownloadUiState {
	QUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED,
}

/** Ligne de chapitre dépliable (item_chapter_download.xml). */
public data class DownloadChapterInfo(
	public val number: Int,
	public val title: String,
	public val isCompleted: Boolean,
)

/**
 * Élément de téléchargement (substitut local de DownloadItemModel tant que le worker
 * de téléchargement n'est pas fusionné).
 */
public data class DownloadItem(
	public val id: UUID,
	public val manga: Manga?,
	public val state: DownloadUiState,
	public val isPaused: Boolean = false,
	public val isIndeterminate: Boolean = false,
	public val progress: Int = 0,
	public val max: Int = 0,
	public val error: String? = null,
	public val eta: Instant? = null,
	public val isStuck: Boolean = false,
	public val chaptersDownloaded: Int = 0,
	public val chapters: List<DownloadChapterInfo> = emptyList(),
	public val isExpanded: Boolean = false,
) {
	public val percent: Float
		get() = if (max > 0) progress / max.toFloat() else 0f

	// DownloadItemModel#canPause
	public val canPause: Boolean
		get() = state == DownloadUiState.RUNNING && !isPaused && error == null
}

@Composable
public fun DownloadsScreen(
	items: List<DownloadItem>,
	onItemClick: (DownloadItem) -> Unit,
	onPauseClick: (DownloadItem) -> Unit,
	onResumeClick: (DownloadItem) -> Unit,
	onCancelClick: (DownloadItem) -> Unit,
	modifier: Modifier = Modifier,
	onBackClick: (() -> Unit)? = null,
	onSkipClick: ((DownloadItem) -> Unit)? = null,
	onSkipAllClick: ((DownloadItem) -> Unit)? = null,
	onCancelAll: (() -> Unit)? = null,
	onRemoveCompleted: (() -> Unit)? = null,
	onPauseAll: (() -> Unit)? = null,
	onResumeAll: (() -> Unit)? = null,
) {
	Column(modifier = modifier.fillMaxSize()) {
		DownloadsTopBar(
			hasActiveWorks = items.any { it.state == DownloadUiState.RUNNING && !it.isPaused },
			hasPausedWorks = items.any { it.state == DownloadUiState.RUNNING && it.isPaused },
			hasCancellableWorks = items.any { it.state == DownloadUiState.QUEUED || it.state == DownloadUiState.RUNNING },
			onBackClick = onBackClick,
			onPauseAll = onPauseAll,
			onResumeAll = onResumeAll,
			onCancelAll = onCancelAll,
			onRemoveCompleted = onRemoveCompleted,
		)
		if (items.isEmpty()) {
			// emptyStateList() de DownloadsViewModel
			EmptyStateScreen(
				icon = Icons.Outlined.Download, // ic_empty_common
				textPrimary = FR_DOWNLOADS_EMPTY_PRIMARY,
				modifier = Modifier.fillMaxSize(),
			)
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(CARD_MARGIN),
				verticalArrangement = Arrangement.spacedBy(CARD_MARGIN),
			) {
				items(items, key = { it.id }) { item ->
					DownloadItemCard(
						item = item,
						onItemClick = onItemClick,
						onPauseClick = onPauseClick,
						onResumeClick = onResumeClick,
						onCancelClick = onCancelClick,
						onSkipClick = onSkipClick,
						onSkipAllClick = onSkipAllClick,
					)
				}
			}
		}
	}
}

/** App bar de DownloadsActivity : titre + actions opt_downmarks (opt_downloads.xml). */
@Composable
private fun DownloadsTopBar(
	hasActiveWorks: Boolean,
	hasPausedWorks: Boolean,
	hasCancellableWorks: Boolean,
	onBackClick: (() -> Unit)?,
	onPauseAll: (() -> Unit)?,
	onResumeAll: (() -> Unit)?,
	onCancelAll: (() -> Unit)?,
	onRemoveCompleted: (() -> Unit)?,
) {
	Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp, vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			if (onBackClick != null) {
				IconButton(onClick = onBackClick) {
					Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = FR_CD_BACK)
				}
			}
			Text(
				text = FR_DOWNLOADS_TITLE,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.weight(1f),
			)
			// action_pause / action_resume : visibles selon l'état (DownloadsMenuProvider#onPrepareMenu)
			if (hasActiveWorks && onPauseAll != null) {
				IconButton(onClick = onPauseAll) {
					Icon(Icons.Filled.Pause, contentDescription = FR_ACTION_PAUSE)
				}
			}
			if (hasPausedWorks && onResumeAll != null) {
				IconButton(onClick = onResumeAll) {
					Icon(Icons.Filled.PlayArrow, contentDescription = FR_ACTION_RESUME)
				}
			}
			var menuOpen by remember { mutableStateOf(false) }
			Box {
				IconButton(onClick = { menuOpen = true }) {
					Icon(Icons.Filled.MoreVert, contentDescription = FR_CD_MORE_OPTIONS)
				}
				DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
					if (hasCancellableWorks && onCancelAll != null) {
						DropdownMenuItem(
							text = { Text(FR_ACTION_CANCEL_ALL) },
							onClick = {
								menuOpen = false
								onCancelAll()
							},
						)
					}
					if (onRemoveCompleted != null) {
						DropdownMenuItem(
							text = { Text(FR_ACTION_REMOVE_COMPLETED) },
							onClick = {
								menuOpen = false
								onRemoveCompleted()
							},
						)
					}
				}
			}
		}
	}
}

/**
 * Carte de téléchargement, fidèle à item_download.xml (MaterialCardView filled style,
 * paddingBottom 12dp) avec la logique d'affichage de downloadItemAD.
 */
@Composable
private fun DownloadItemCard(
	item: DownloadItem,
	onItemClick: (DownloadItem) -> Unit,
	onPauseClick: (DownloadItem) -> Unit,
	onResumeClick: (DownloadItem) -> Unit,
	onCancelClick: (DownloadItem) -> Unit,
	onSkipClick: ((DownloadItem) -> Unit)?,
	onSkipAllClick: ((DownloadItem) -> Unit)?,
) {
	Card(
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest, // materialCardViewFilledStyle
		),
		shape = MaterialTheme.shapes.medium,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onItemClick(item) },
	) {
		Column(modifier = Modifier.padding(bottom = CARD_INNER_MARGIN)) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = CARD_INNER_MARGIN),
				verticalAlignment = Alignment.Top,
			) {
				Box(
					modifier = Modifier
						.padding(start = CARD_INNER_MARGIN)
						.size(CARD_COVER_SIZE)
						.clip(RoundedCornerShape(CARD_COVER_CORNER))
						.background(MaterialTheme.colorScheme.surfaceContainer),
					contentAlignment = Alignment.Center,
				) {
					AsyncImage(
						model = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
							.data(item.manga?.coverUrl)
							.crossfade(true)
							.build(),
						contentDescription = item.manga?.title,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize(),
					)
				}
				Column(
					modifier = Modifier
						.weight(1f)
						.padding(start = CARD_TITLE_MARGIN, end = CARD_INNER_MARGIN),
				) {
					Text(
						text = item.manga?.title ?: FR_STATUS_UNKNOWN,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					Text(
						text = statusText(item),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.padding(top = 4.dp),
					)
					Text(
						text = detailsText(item),
						style = MaterialTheme.typography.bodySmall,
						color = if (item.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
						fontWeight = if (item.error != null) FontWeight.Bold else FontWeight.Normal,
						maxLines = 4,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.padding(top = 4.dp),
					)
				}
				// button_expand : présent seulement si la liste de chapitres est disponible
				if (item.chapters.isNotEmpty()) {
					IconButton(onClick = { onItemClick(item) }) {
						Icon(
							imageVector = if (item.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
							contentDescription = if (item.isExpanded) "Collapse" else "Expand", // strings collapse/expand
							tint = MaterialTheme.colorScheme.primary, // ?colorControlActivated
						)
					}
				}
			}
			// progressBar : masqué pour QUEUED/SUCCEEDED/FAILED/CANCELLED (downloadItemAD)
			when (item.state) {
				DownloadUiState.RUNNING -> {
					LinearProgressIndicator(
						progress = { item.percent },
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = CARD_INNER_MARGIN)
							.padding(top = CARD_INNER_MARGIN),
						color = MaterialTheme.colorScheme.primary,
						trackColor = MaterialTheme.colorScheme.background, // ?android:colorBackground
					)
					if (!item.isIndeterminate) {
						Text(
							text = percentText(item.percent),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.align(Alignment.End)
								.padding(horizontal = CARD_INNER_MARGIN, vertical = 4.dp),
						)
					}
				}

				else -> Unit
			}
			// recyclerView_chapters : liste dépliable (maxHeight 240dp, paddingVertical 8dp)
			if (item.isExpanded && item.chapters.isNotEmpty()) {
				Box(
					modifier = Modifier
						.padding(horizontal = CARD_INNER_MARGIN)
						.padding(top = CARD_INNER_MARGIN)
						.heightIn(max = CHAPTERS_MAX_HEIGHT)
						.clip(MaterialTheme.shapes.small)
						.background(MaterialTheme.colorScheme.surface),
				) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = 8.dp),
					) {
						for (chapter in item.chapters) {
							ChapterRow(chapter = chapter)
						}
					}
				}
			}
			// flow_buttons : boutons outline alignés à droite (visibility par état, cf. downloadItemAD)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = CARD_INNER_MARGIN)
					.padding(top = ACTION_GAP / 2),
				horizontalArrangement = Arrangement.spacedBy(ACTION_GAP, Alignment.End),
			) {
				when (item.state) {
					DownloadUiState.QUEUED -> {
						OutlinedButtonText(FR_ACTION_CANCEL) { onCancelClick(item) }
					}

					DownloadUiState.RUNNING -> {
						if (item.canPause) {
							OutlinedButtonText(FR_ACTION_PAUSE) { onPauseClick(item) }
						}
						if (item.isPaused) {
							OutlinedButtonText(if (item.error == null) FR_ACTION_RESUME else FR_ACTION_RETRY) {
								onResumeClick(item)
							}
							if (item.error != null) {
								OutlinedButtonText(FR_ACTION_SKIP) { onSkipClick?.invoke(item) }
								OutlinedButtonText(FR_ACTION_SKIP_ALL) { onSkipAllClick?.invoke(item) }
							}
						}
						OutlinedButtonText(FR_ACTION_CANCEL) { onCancelClick(item) }
					}

					else -> Unit // SUCCEEDED / FAILED / CANCELLED : aucun bouton (comme l'original)
				}
			}
		}
	}
}

/** Statut par état, exactement comme downloadItemAD (WorkInfo.State → string res). */
private fun statusText(item: DownloadItem): String = when (item.state) {
	DownloadUiState.QUEUED -> FR_STATUS_QUEUED
	DownloadUiState.RUNNING -> if (item.isPaused) FR_STATUS_PAUSED else FR_STATUS_DOWNLOADING
	DownloadUiState.SUCCEEDED -> FR_STATUS_COMPLETE
	DownloadUiState.FAILED -> FR_STATUS_ERROR
	DownloadUiState.CANCELLED -> FR_STATUS_CANCELED
}

/** textView_details par état, comme downloadItemAD : ETA / Stuck / erreur / « N chapters ». */
private fun detailsText(item: DownloadItem): String = when (item.state) {
	DownloadUiState.RUNNING -> when {
		item.isPaused -> item.error.orEmpty()
		item.isStuck -> FR_STATUS_STUCK
		item.eta != null -> etaText(item.eta)
		else -> ""
	}

	DownloadUiState.SUCCEEDED ->
		if (item.chaptersDownloaded > 0) {
			pluralChapters(item.chaptersDownloaded)
		} else {
			""
		}

	DownloadUiState.FAILED -> item.error.orEmpty()
	else -> ""
}

// DateUtils.getRelativeTimeSpanString(eta) — temps futur : « in X minutes » / « in X hours »
private fun etaText(eta: Instant): String {
	val seconds = Duration.between(Instant.now(), eta).seconds
	return when {
		seconds >= 3600 -> "in ${seconds / 3600} hour${if (seconds / 3600 == 1L) "" else "s"}"
		seconds >= 60 -> "in ${seconds / 60} minute${if (seconds / 60 == 1L) "" else "s"}"
		else -> "in ${seconds} second${if (seconds == 1L) "" else "s"}"
	}
}

// plurals.xml « chapters » : one = "%1$d chapter", other = "%1$d chapters"
private fun pluralChapters(count: Int): String = "$count chapter${if (count == 1) "" else "s"}"

/** Ligne de chapitre, fidèle à item_chapter_download.xml : badge numéro + titre + icône check. */
@Composable
private fun ChapterRow(chapter: DownloadChapterInfo) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.padding(start = CARD_TITLE_MARGIN)
				.size(CHAPTER_BADGE_SIZE)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.primary),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = chapter.number.toString(),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onPrimary,
			)
		}
		Text(
			text = chapter.title,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = CARD_TITLE_MARGIN),
		)
		if (chapter.isCompleted) {
			Icon(
				imageVector = Icons.Filled.Check,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant, // ?colorControlNormal
				modifier = Modifier.padding(end = CARD_TITLE_MARGIN),
			)
		} else {
			Box(modifier = Modifier.width(CARD_TITLE_MARGIN))
		}
	}
}

@Composable
private fun OutlinedButtonText(text: String, onClick: () -> Unit) {
	OutlinedButton(onClick = onClick) {
		Text(text)
	}
}
