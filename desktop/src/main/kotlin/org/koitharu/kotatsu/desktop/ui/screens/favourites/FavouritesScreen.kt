/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose
 * Desktop ; 2026-11-08 — phase P2 : l'entrée « Options de liste » du menu ⋮ ouvre la
 * ListOptionsSheet (callback onListOptionsClick, feuille hébergée par Main) ; l'écran consomme
 * désormais listMode + gridSize (3 modes de liste, comme l'historique — bottom sheet frame ~0:30).
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 * Modification : 2026-11-08 — phase P8 : puce ★ violette sur les couvertures de la grille
 * (overlay coin supérieur droit, vidéo t≈7:20 favoris — tous les mangas de cet écran sont des
 * favoris, la puce est donc affichée sur chaque couverture de la grille, comme le badge favori
 * de HistoryGridCard). Vérification : les onglets catégories filtrent déjà réellement — la page
 * active recharge `fetchManga(selectedCategory)` et Main filtre par categoryId côté données.
 *
 * Écran « Favoris » — reproduit FavouritesContainerFragment (fragment_favourites_container.xml) :
 * barre de recherche pill + menu ⋮ (fidèle aux captures ; menu = Options de liste, Catégories
 * favorites, Mode incognito, Paramètres), rangée d'onglets défilants (TabLayout
 * tabMode=scrollable, tabGravity=start) + page active = grille de mangas (fragment_list.xml
 * mode grille). Titre d'onglet = catégorie, « Tous les favoris » (all_favourites) si null,
 * comme FavouritesTabConfigurationStrategy. État vide : item_empty_state + Totoro
 * ic_empty_favourites, « C'est un peu vide ici… » / « Catégorie vide » (values-fr).
 *
 * Les données viennent en paramètres (la couche DB Room est portée ailleurs), comme
 * CatalogScreen reçoit `fetchList`.
 */
package org.koitharu.kotatsu.desktop.ui.screens.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koitharu.kotatsu.core.prefs.ListMode
import org.koitharu.kotatsu.desktop.ui.components.EmptyStateScreen
import org.koitharu.kotatsu.desktop.ui.components.MangaListCard
import org.koitharu.kotatsu.desktop.ui.components.TotoroIllustration
import org.koitharu.kotatsu.desktop.ui.components.TopSearchBar
import org.koitharu.kotatsu.desktop.ui.components.TopSearchMenuAction
import org.koitharu.kotatsu.desktop.ui.strings.FR_ALL_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES_CATEGORIES
import org.koitharu.kotatsu.desktop.ui.strings.FR_FAVOURITES_CATEGORY_EMPTY
import org.koitharu.kotatsu.desktop.ui.strings.FR_INCOGNITO_MODE
import org.koitharu.kotatsu.desktop.ui.strings.FR_LIST_OPTIONS
import org.koitharu.kotatsu.desktop.ui.strings.FR_MANAGE
import org.koitharu.kotatsu.desktop.ui.strings.FR_SETTINGS
import org.koitharu.kotatsu.desktop.ui.strings.FR_TEXT_EMPTY_HOLDER_PRIMARY
import org.koitharu.kotatsu.parsers.model.Manga

// strings.xml d'origine (values-fr)
public var FAVOURITES_TITLE: String = FR_FAVOURITES // string favourites
public var ALL_FAVOURITES: String = FR_ALL_FAVOURITES // string all_favourites

private val GRID_CONTENT_PADDING = 2.dp // grid card margin
private val GRID_SPACING = 6.dp // espacement de grille (comme CatalogScreen)
private val GRID_BASE_CELL_WIDTH = 110.dp // taille 100 % de la bottom sheet (comme l'historique)
private val DETAILED_COVER_HEIGHT = 118.dp // item_manga_list_details.xml
private val DETAILED_COVER_CORNER = 8.dp // ShapeAppearanceOverlay.Kotatsu.Cover.Medium
private val ROW_START_MARGIN = 16.dp
private val ROW_END_MARGIN = 12.dp
private val TEXT_SPACING = 4.dp

/**
 * Catégorie de favoris (substitut local de FavouriteCategoryEntity tant que la couche DB
 * n'est pas fusionnée — cf. FavouriteTabModel d'origine : id + titre).
 */
public data class FavouriteCategory(
	public val id: Long,
	public val title: String?,
	public val count: Int,
)

/**
 * Conteneur à onglets des favoris.
 * @param categories catégories favorites (l'app d'origine les observe depuis la DB)
 * @param fetchManga charge les mangas de la catégorie sélectionnée (suspend, injecté)
 * @param listMode mode d'affichage de la page active (bottom sheet « Options de liste ») :
 * grille adaptative, liste compacte ou liste détaillée — comme l'historique
 * @param gridSize taille de la grille en % (50..200, 100 % = 110dp, comme l'historique)
 * @param onManageClick ouvre la gestion des catégories (router.openFavoriteCategories)
 */
@Composable
public fun FavouritesScreen(
	categories: List<FavouriteCategory>,
	fetchManga: suspend (FavouriteCategory) -> List<Manga>,
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	listMode: ListMode = ListMode.GRID,
	gridSize: Int = 100,
	onListOptionsClick: (() -> Unit)? = null,
	onManageClick: (() -> Unit)? = null,
	onSearchClick: () -> Unit = {},
	onSettingsClick: () -> Unit = {},
	onIncognitoToggle: (Boolean) -> Unit = {},
) {
	if (categories.isEmpty()) {
		// stub_empty de FavouritesContainerFragment (ItemEmptyStateBinding, Totoro favoris)
		EmptyStateScreen(
			illustration = TotoroIllustration.FAVOURITES,
			textPrimary = FR_TEXT_EMPTY_HOLDER_PRIMARY,
			textSecondary = FR_FAVOURITES_CATEGORY_EMPTY,
			actionText = if (onManageClick != null) FR_MANAGE else null,
			onActionClick = onManageClick,
			modifier = modifier.fillMaxSize(),
		)
		return
	}
	var selectedId by remember(categories) { mutableStateOf(categories.first().id) }
	var mangas by remember { mutableStateOf<List<Manga>>(emptyList()) }
	var isLoading by remember { mutableStateOf(false) }
	var incognito by remember { mutableStateOf(false) }
	val selectedCategory = categories.firstOrNull { it.id == selectedId } ?: categories.first()

	Column(modifier = modifier.fillMaxSize()) {
		// Barre de recherche pill + menu ⋮ (fidèle aux captures ; menu = menu_favourites)
		TopSearchBar(
			onSearchClick = onSearchClick,
			menuActions = listOf(
				TopSearchMenuAction(label = FR_LIST_OPTIONS, onClick = { onListOptionsClick?.invoke() }),
				TopSearchMenuAction(
					label = FR_FAVOURITES_CATEGORIES,
					onClick = { onManageClick?.invoke() },
				),
				TopSearchMenuAction(
					label = FR_INCOGNITO_MODE,
					onClick = {
						incognito = !incognito
						onIncognitoToggle(incognito)
					},
					checkable = true,
					checked = incognito,
				),
				TopSearchMenuAction(FR_SETTINGS, onSettingsClick),
			),
		)
		Surface(color = MaterialTheme.colorScheme.surface) {
			// TabLayout : tabMode=scrollable, tabGravity=start
			PrimaryScrollableTabRow(
				selectedTabIndex = categories.indexOfFirst { it.id == selectedCategory.id }.coerceAtLeast(0),
				edgePadding = 8.dp,
				containerColor = MaterialTheme.colorScheme.surface,
			) {
				for (category in categories) {
					Tab(
						selected = category.id == selectedCategory.id,
						onClick = { selectedId = category.id },
						text = {
							Text(
								text = category.title ?: ALL_FAVOURITES,
								style = MaterialTheme.typography.bodyLarge, // ~16sp comme la capture
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
							)
						},
					)
				}
			}
		}
		Box(modifier = Modifier.fillMaxSize()) {
			if (isLoading) {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					CircularProgressIndicator()
				}
			} else if (mangas.isEmpty()) {
				// stub_empty de FavouritesListFragment : catégorie vide (Totoro favoris, values-fr)
				EmptyStateScreen(
					illustration = TotoroIllustration.FAVOURITES,
					textPrimary = FR_TEXT_EMPTY_HOLDER_PRIMARY,
					textSecondary = FR_FAVOURITES_CATEGORY_EMPTY,
				)
			} else {
				when (listMode) {
						ListMode.GRID -> {
							// « Taille de la grille » : 100 % = 110dp, même base que l'historique
							val cellWidth = (GRID_BASE_CELL_WIDTH * gridSize.coerceIn(50, 200) / 100f)
							LazyVerticalGrid(
								columns = GridCells.Adaptive(cellWidth),
								modifier = Modifier.fillMaxSize(),
								contentPadding = PaddingValues(GRID_CONTENT_PADDING),
								horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
								verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
							) {
								itemsIndexed(mangas) { _, manga ->
									// P8 : puce ★ violette (tous les mangas de l'écran sont des favoris)
									FavouritesGridCard(manga = manga, onClick = { onMangaClick(manga) })
								}
							}
						}
					ListMode.LIST -> {
						// « Compact » : lignes item_manga_list.xml (couverture 40dp, titre + auteur)
						LazyColumn(
							modifier = Modifier.fillMaxSize(),
							contentPadding = PaddingValues(GRID_CONTENT_PADDING),
							verticalArrangement = Arrangement.spacedBy(2.dp),
						) {
							items(mangas, key = { it.url }) { manga ->
								MangaListCard(manga = manga, onClick = { onMangaClick(manga) })
							}
						}
					}
					ListMode.DETAILED_LIST -> {
						// « Détails » : lignes item_manga_list_details.xml (couverture 118dp, comme
						// la ligne détaillée de l'historique)
						LazyColumn(
							modifier = Modifier.fillMaxSize(),
							contentPadding = PaddingValues(GRID_CONTENT_PADDING),
							verticalArrangement = Arrangement.spacedBy(2.dp),
						) {
							items(mangas, key = { it.url }) { manga ->
								FavouritesDetailedCard(manga = manga, onClick = { onMangaClick(manga) })
							}
						}
					}
				}
			}
			// Chargement de la page de la catégorie active (comme le ViewPager2 + pagerAdapter)
			LaunchedEffect(selectedCategory) {
				isLoading = true
				mangas = try {
					fetchManga(selectedCategory)
				} catch (_: Exception) {
					emptyList() // les erreurs de liste sont silencieuses comme côté Android (ErrorState)
				} finally {
					isLoading = false
				}
			}
		}
	}
}

/** Métriques de la carte grille (identiques à MangaGridCard, cf. item_manga_grid.xml). */
private val GRID_CARD_MARGIN = 2.dp
private val GRID_CARD_PADDING = 6.dp
private val GRID_COVER_CORNER = 12.dp
private val GRID_TITLE_PADDING_VERTICAL = 4.dp
private val STAR_BADGE_OFFSET = 6.dp
private val STAR_BADGE_SIZE = 18.dp

/**
 * Carte grille des favoris (P8) : identique à MangaGridCard (marges 2/6dp, couverture coins
 * 12dp, titre TitleSmall 2 lignes) + puce ★ violette en overlay du coin supérieur droit de la
 * couverture (vidéo t≈7:20) — tous les mangas de cet écran sont des favoris. Dupliquée localement
 * car MangaGridCard (composant partagé) n'expose pas d'overlay et n'est pas dans le périmètre.
 */
@Composable
private fun FavouritesGridCard(
	manga: Manga,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.padding(GRID_CARD_MARGIN)
			.clickable(onClick = onClick)
			.padding(GRID_CARD_PADDING),
	) {
		Box {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(2f / 3f)
					.clip(RoundedCornerShape(GRID_COVER_CORNER))
					.background(MaterialTheme.colorScheme.surfaceContainer),
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
			}
			// Puce ★ violette (couleur primaire du thème, comme le badge favori de l'historique)
			Icon(
				imageVector = Icons.Filled.Star,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(STAR_BADGE_OFFSET)
					.size(STAR_BADGE_SIZE),
			)
		}
		Text(
			text = manga.title,
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = GRID_TITLE_PADDING_VERTICAL),
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			textAlign = TextAlign.Start,
		)
	}
}

/**
 * Ligne détaillée « Détails » (mode DETAILED_LIST de la bottom sheet Options de liste), fidèle à
 * item_manga_list_details.xml comme la ligne détaillée de l'historique : couverture 118dp
 * (ratio 2:3, coins 8dp, fond colorSurfaceContainer), titre BodyLarge 3 lignes, sous-titre
 * auteur (BodyMedium).
 */
@Composable
private fun FavouritesDetailedCard(
	manga: Manga,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.height(DETAILED_COVER_HEIGHT)
				.aspectRatio(2f / 3f)
				.clip(RoundedCornerShape(DETAILED_COVER_CORNER))
				.background(MaterialTheme.colorScheme.surfaceContainer),
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
		}
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = ROW_START_MARGIN, end = ROW_END_MARGIN),
		) {
			Text(
				text = manga.title,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 3,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = manga.author.orEmpty(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(top = TEXT_SPACING),
			)
		}
	}
}
