# Journal de la phase P8 — Découverte & écriture

> Références : `plan_implementation.md` §5 P8, vidéo moitié 2 (6:00-7:20, 9:25).
> RÈGLE DE TEST : app toujours testée vierge. RÈGLE DE CHAÎNES : tout texte vient de
> `ui/strings/` (P8-A ajoute dans Fr.kt ; P8-B dans FrSources.kt — propriétaires exclusifs).
> **LIVRABLES DE FIN DE PHASE** : `Kotatsu-9.8.6-P8.exe` + `P8-source.zip` → `../releases/`.

## 0. Périmètre P8 (rappel du plan)

- Filtres d'une source (tri, chips genres, bottom sheet avec recherche)
- Dialog catégories des favoris (cases + « Gérer »/« Terminé », puce ★)
- Fiche manga enrichie (méta Source/Auteur/État, compteur chapitres + temps estimé,
  « Mangas connexes », dropdown favoris, CTA « Continuer · n ▾ »)
- Badges 🔥 fonctionnels

## 1. État de départ (inventaire principal, 2026-11-08)

| Élément P8 | État | Preuve |
|---|---|---|
| Fiche manga | ◐ couverture/tags/description/chapitres + barre Lire/Favoris/Télécharger ; méta Source/État/temps/connexes ⛔ ; dropdown Lire ⛔ | `DetailsScreen.kt` (376 l.) |
| Filtres d'une source | ⛔ CatalogScreen sans filtre (grep vide) | `CatalogScreen.kt` |
| Favoris | ◐ onglets lecture seule ; puce ★ ⛔ ; `observeCategories(mangaId)` existe | `FavouritesScreen.kt`, `FavouritesDao.kt:168` |
| Modèle | ✅ `manga.author`, état via parsers, contentType NSFW ; catégories DB complètes (add/remove à vérifier dans les DAO) | — |
| Badges 🔥 | ⛔ (`hasNewChapters = { false }`) | `Main.kt` |

## 2. Journal de la session

### Agent principal

- Inventaire (ci-dessus), découpage 2 lots (COORDINATION.md § P8) — P8-A : fiche enrichie +
  dialog catégories (neuf) + Fr.kt ; P8-B : filtres catalogue + puce ★ Favoris + FrSources.kt.
- Lancement des agents en parallèle.

### Agent P8-B — filtres catalogue + favoris (TERMINÉ, compile verte)

- **`CatalogScreen.kt`** réécrit : entonnoir + chip « Populaires ▾ » → bottom sheet « Trier et
  filtrer » (radios SortOrder, chips genres multiselect avec case) appliquée à chaque
  interaction ; tri + genres en mémoire (rawItems séparé, pagination préservée) ; divergences
  documentées (POPULARITY ≈ rating, UPDATED = ordre source). **Découverte** :
  `ParserMangaRepository.getFilterOptions()` expose les genres officiels du source — nouveau
  param optionnel `fetchFilterOptions` prêt à câbler (chips dérivées des tags chargés en
  attendant).
- **`FavouritesScreen.kt`** : puce ★ violette TopEnd des couvertures (carte locale dupliquée) ;
  onglets catégories vérifiés déjà fonctionnels (filtrage réel existant).
- Chaînes : `FrSources.kt` +9 (frSortOrderLabel exhaustif sur 18 valeurs, réutilise
  FR_UPDATES/FR_NEWEST). Compile EXIT=0.
- **Points d'intégration Main restants** (principal) : câbler `fetchFilterOptions` via
  `ParserMangaRepository.getFilterOptions()` ; tri côté source possible via `getList(order)`.

### Agents P8-A et P8-B (TERMINÉS, compiles vertes) + intégration du principal

- **P8-A** (voir rapport) : méta Source/Auteur/État/Chapitres+temps (195 h pour 2940 chapitres),
  badge 18+, « Mangas connexes / Tout afficher » réels (recherche genre MangaDex), « Lire ▾ »
  dropdown (« Lire le chapitre 1 » / « Continuer · n »), dialog « Ajouter aux favoris » (case
  catégorie + 🔔, « Gérer » désactivé, « Terminé ») avec persistance `container.library`,
  « Chapitre n sur N » + Progression % + puce ★ violette après ajout. Chaînes : Fr.kt +12/5 fn.
- **P8-B** (voir rapport) : catalogue réécrit (entonnoir + chip tri → bottom sheet Tri/Genres,
  appliqués en mémoire, pagination préservée), puce ★ Favoris, onglets déjà fonctionnels ;
  `fetchFilterOptions` câblé par le principal via `ParserMangaRepository.getFilterOptions()`
  (genres officiels des sources).
- **Build complet** `:desktop:build :core:build` → **EXIT=0**.

### Captures de parité (principal, 11-08, app vierge) — RÈGLE DE CONTENU appliquée

- **Règle client enregistrée** : interdiction d'ouvrir des contenus explicites (sources
  NSFW/Hentai) pendant les tests — filtre de sécurité du fournisseur. La 1ère tuile du catalogue
  source (tri SCORE) pointant sur une source 18+, la capture correspondante a été **supprimée
  sans lecture** et remplacée par un parcours MangaDex uniquement.
- **Captures vérifiées** : `desktop-details-enriched.png` (méta complète + connexes réels +
  Lire ▾), `desktop-details-read-dropdown.png` (« Lire le chapitre 1 »), `desktop-details-favorites-
  dialog.png` (catégorie réelle « Read later 🔔 », Gérer/Terminé), `desktop-favourites-star.png`
  (favori ajouté, onglets « Tous les favoris / Read later »).
- **E2E vérifié** : ajout favori via dialog → manga présent dans Favoris → puce ★ violette dans
  la barre d'actions de la fiche.
- Reste à capturer plus tard : bottom sheet filtres du catalogue (nécessite d'atteindre un
  catalogue de source sûre sans risquer les tuiles 18+ du tri SCORE).

## ✅ Bilan P8

- Matrice (plan §6) : Fiche manga enrichie ◐ → ✅ ; favoris (dialog catégories, puce ★) ⛔ → ✅ ;
  filtres d'une source ⛔ → ✅ (UI + tri/genres mémoire ; tri côté source possible via
  getList(order) — branchement optionnel restant).
- Vérifications : compile par lot, build complet EXIT=0, 4 captures sur app vierge, E2E favoris.
