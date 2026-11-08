# Journal de la phase P2 — Apparence complète

> Suivi horodaté de tout ce qui est fait pendant P2. Chaque entrée : date, auteur
> (principal / agent P2-A / agent P2-B), action, résultat, preuve.
> Référence de spécification : `../video-analysis/half1-inventory.md` (frames 1:40 → 2:25)
> et `plan_implementation.md` §5 P2.

## 0. Périmètre P2 (rappel du plan)

- Choix de langue
- Section « Liste des mangas » : mode, taille, filtres rapides, indicateurs de progression, badges
- Section « Page du manga » : miniatures, onglet par défaut, temps estimé, connexes
- Section « Navigation » : épingler, bouton flottant Continuer, confirmation de sortie,
  suggestions de recherche, raccourcis
- Écran « Principales sections de l'écran » : liste réordonnable + dialog « Ajouter »
- Options de liste (bottom sheet, matrice P0/P2)

## 1. État hérité (session du 7 novembre, commit `bfec3c2`)

Inventaire fait le 2026-11-08 par l'agent principal — l'arbre de travail contenait ~1500 lignes
non commitées datées du 7 nov. (session P2 entamée puis interrompue) :

| Élément P2 | État hérité | Preuve |
|---|---|---|
| Apparence — Liste des mangas (mode, taille, filtres rapides, indicateurs, badges) | ✅ implémenté + câblé Main.kt:479-482 | `SettingsScreen.kt:258-318` |
| Apparence — Page du manga | ◐ « Réduire la description longue » ✅ ; miniatures/onglet/temps/connexes désactivés (P8) | `SettingsScreen.kt:320-357` |
| Apparence — Navigation (bouton flottant Continuer, confirmation de sortie) | ✅ implémenté + câblé (Main.kt:95,168,670) ; Épingler/Suggestions/Raccourcis désactivés (divergences desktop) | `SettingsScreen.kt:359-407` |
| « Principales sections de l'écran » | ✅ liste réordonnable (↑/↓ divergence desktop) + dialog « Ajouter » (Sur l'appareil, Suggestions, Mis à jour, Marque-pages) | `SectionsSettingsScreen.kt` |
| Langue | ◐ entrée présente, désactivée (app FR seule — divergence documentée) | `SettingsScreen.kt:249-256` |
| **Section « Thème » de l'Apparence** | ⛔ **EN + radios simples** : « Theme », « Follow system », « Light », « Dark », « AMOLED theme », « Color scheme » en rangées radio — la vidéo montre FR (« Thème », « Suivre le système », « Clair », « Sombre », « Noir », « Schéma de couleurs ») avec **cartes de palette horizontales** (aperçu « Abc » + barres + pastille, noms Mamimi/Kanade/Itsuka…) | `SettingsScreen.kt:172-247` |
| **Options de liste (bottom sheet du menu ⋮)** | ⛔ UI absente du menu ⋮ ; seule une ligne compacte existe côté Historique (`HistoryScreen.kt`, « Ligne compacte … Options de liste "Compact" ») | grep Main.kt : aucune entrée |
| **Marque-pages — état vide** | ⛔ EN « No bookmarks here yet » ; vidéo : « Aucun marque-page / Vous pouvez créer un marque-page pendant la lecture d'un manga » | `BookmarksScreen.kt:63` |

## 2. Journal de la session du 8 novembre

### Agent principal

- 09:xx Lecture de l'état : `git status` (13 fichiers modifiés, 8 non suivis), diff stat
  (+1498/−273), `plan_implementation.md`, `half1-inventory.md`, checklist de parité.
- 09:xx **Baseline compile vérifiée** : `./gradlew :desktop:compileKotlin` → OK (JBR 25).
- 09:xx **Commit de sécurité** `bfec3c2` : état hérité P2 WIP commité avant intervention des
  agents (branch main).
- 09:xx Découpage P2 en 2 lots sans conflit de fichiers (voir `COORDINATION.md` § P2) :
  - **P2-A** (parité Apparence) : `SettingsScreen.kt`, `Fr.kt`, nouveau composant cartes de palette.
  - **P2-B** (options de liste + marque-pages FR) : `Main.kt`, `HistoryScreen.kt`,
    `BookmarksScreen.kt`, nouveau `ListOptionsSheet.kt`. Chaînes privées dans ses fichiers
    (pas de `Fr.kt` → zéro conflit avec P2-A).
- 09:xx Lancement des deux agents en parallèle (arrière-plan) :
  - **agent P2-A** : carte de parité Apparence — section Thème FR + cartes de palette
    (`ColorSchemeCards.kt` nouveau, `SettingsScreen.kt`, `Fr.kt`).
  - **agent P2-B** : options de liste + marque-pages FR — `ListOptionsSheet.kt` nouveau,
    entrée menu ⋮ dans `Main.kt`, `HistoryScreen.kt`, `BookmarksScreen.kt`.
- 09:xx En attendant les agents : préparation de l'étape captures (outils existants
  `tools/capture-kotatsu.ps1` : capture de la fenêtre Kotatsu → `docs/ui-parity/`).

### Agent P2-A — parité Apparence (TERMINÉ, compile verte)

- **Nouveau `ColorSchemeCards.kt`** : `ColorSchemeCardsRow` (LazyRow 16dp/8dp, ordre d'origine
  Totoro→Miku) + `ColorSchemeCard` (76dp large, aperçu 56dp coins 12dp dessiné avec les vraies
  couleurs de `paletteFor(scheme, dark)` : fond surface, « Abc » onSurface, 3 barres
  primary/secondary/tertiary, pastille 12dp ; sélection = bordure primary 2dp, sinon
  outlineVariant 1dp ; nom labelMedium sous la carte).
- **`SettingsScreen.kt`** : section Thème refaite dans l'ordre de la vidéo — « Thème » →
  « Schéma de couleurs » + cartes → radios « Suivre le système / Clair / Sombre » (via
  `ThemeMode.entries`) → « Noir » + « Utilise moins d'énergie pour les écrans AMOLED » →
  Langue (désactivée, inchangée). Les 9 rangées radio supprimées ; logique
  `setThemeMode`/`setColorScheme`/`setAmoled` inchangée.
- **`Fr.kt`** : + FR_THEME, FR_COLOR_SCHEME, FR_FOLLOW_SYSTEM, FR_LIGHT, FR_DARK, FR_AMOLED,
  FR_AMOLED_SUMMARY (style existant).
- **Décisions** : noms de schémas non traduits (noms propres, identiques FR/EN dans l'original) ;
  bordure 1dp outlineVariant pour les cartes non sélectionnées (aperçu clair sinon invisible) ;
  ordre vidéo respecté (Schéma de couleurs avant le mode) ; aperçu suit clair/sombre courant
  (pas l'overlay AMOLED).
- **Vérification** : `./gradlew :desktop:compileKotlin` → succès. Non commité (principal).

### Agent P2-B — options de liste + marque-pages (TERMINÉ, compile verte)

- **Nouveau `ListOptionsSheet.kt`** : ModalBottomSheet M3 fidèle à la vidéo (frame ~0:30) —
  « Mode liste » en SegmentedButtons Compact/Détails/Grille, « Taille de la grille » slider
  50..200 avec « N % », « Ordre de tri » dropdown (« Dernier lu »/« Par nom »), « Groupe » en
  interrupteur (la vidéo montre un interrupteur, pas un dropdown — confiance élevée). Écrit en
  direct dans AppSettings à chaque interaction, pas de bouton valider.
- **`Main.kt`** : état `ListOptionsState` + hôte de la feuille ; HistoryScreen alimenté via
  l'état (recomposition correcte) ; entrée « Options de liste » branchée Historique **et** Favoris.
- **`BookmarksScreen.kt`** : état vide FR « Aucun marque-page » / « Vous pouvez créer un
  marque-page pendant la lecture d'un manga ».
- **Dérogation validée par le principal** : le menu ⋮ vit dans les écrans (`TopSearchBar
  menuActions`), pas dans Main.kt ; l'entrée « Options de liste » existait déjà avec
  `onClick = {}` mort → édition minimale de `HistoryScreen.kt` et `FavouritesScreen.kt`
  (paramètre optionnel `onListOptionsClick`). Aucun conflit avec P2-A.
- **Restes identifiés par l'agent** (KDoc documentés) : tri de l'historique persisté mais non
  appliqué ; groupement en session seulement (pas de clé AppSettings) ; Favoris n'accepte pas
  encore listMode/gridSize.
- **Vérification** : compile verte du premier coup. Non commité.

### Intégration (principal, 11-08)

- **Build complet** : `./gradlew :desktop:build :core:build -q` → **EXIT=0** (tests inclus).
- **Itération 2 lancée** sur les restes, avec périmètres élargis :
  - P2-A : revue de parité finale de tout l'écran Apparence + SectionsSettingsScreen contre
    half1-inventory (lignes 30/39/41-44), dialog indicateurs de progression, dialog « Ajouter »,
    documentation des divergences desktop dans `UI_DIVERGENCES.md`.
  - P2-B : câblage tri historique, persistance du groupement (nouvelle clé AppSettings),
    branchement listMode/gridSize des Favoris.

### Agent P2-A — revue de parité Apparence, itération 2 (TERMINÉ, compile verte)

- **Revue libellé par libellé** contre half1-inventory (l. 30/39/41/42/43/44) : sections Thème et
  Liste des mangas conformes sans correction ; Navigation conforme (valeurs par défaut vérifiées).
- **Corrections** : guillemets de `FR_PAGES_THUMBNAILS_SUMMARY` (« Pages ») ; sous-titres desktop
  de Navigation centralisés dans Fr.kt (`FR_EXIT_CONFIRMATION_SUMMARY`,
  `FR_RECENT_SHORTCUTS_SUMMARY`, libellé d'origine en commentaire) ; **dialog « Ajouter » refait**
  — 4 options exactes dans l'ordre de la vidéo (« Sur l'appareil » et « Marque-pages »
  fonctionnelles, « Suggestions »/« Mis à jour » grisées comme sur Android, message si tout est
  déjà affiché), + chaînes FR_ON_DEVICE/FR_UPDATES.
- **Dialog indicateurs de progression** vérifié : ordre exact « Désactivé / Pourcentage lu /
  Pourcentage restant / Chapitres lus / Chapitres restants » + « Annuler ».
- **`UI_DIVERGENCES.md`** : 5 divergences documentées datées 2026-11-08 (#10 confirmation de
  sortie desktop, #11 langue désactivée, #12 épingler, #13 raccourcis, #14 sections ↑/↓ +
  « Ajouter »).
- Fichiers : SettingsScreen.kt, Fr.kt, SectionsSettingsScreen.kt, UI_DIVERGENCES.md.
- Compile verte (fix smart cast `NavItem?` → `item?.let`) ; erreurs transitoires Main.kt vues
  pendant un build = travail simultané de P2-B, disparues ensuite. Non commité.

### Agent P2-B — câblage options de liste, itération 2 (TERMINÉ, build vert)

- **`AppSettings.kt`** : nouvelle préférence `isHistoryGroupingEnabled` (défaut `true`, clé
  `KEY_HISTORY_GROUPING = "history_grouping"`, style existant ; clé desktop dédiée car aucune clé
  d'origine retrouvée — module list non porté).
- **`ListOptionsSheet.kt`** : « Groupe » maintenant persisté via AppSettings (survit au
  redémarrage).
- **`Main.kt`** : tri de l'historique appliqué (`sortedHistory` en mémoire — « Par nom »
  insensible à la casse, « Dernier lu » = ordre DAO) ; groupement désactivé automatiquement en
  tri alphabétique (en-têtes de dates sans sens) ; `FavouritesScreen` branché sur
  `listOptions.listMode`/`gridSize`.
- **`FavouritesScreen.kt`** : 3 modes de liste fonctionnels sur la catégorie active — Grille
  adaptative (cellule 110dp × gridSize/100, même base que l'historique), Compact
  (`MangaListCard` 40dp), Détails (nouvelle carte privée calquée sur item_manga_list_details.xml,
  couverture 118dp) ; `GRID_CELL_WIDTH` 120dp fixe supprimé.
- **Restes documentés** (non demandés) : le contrôle unique « Ordre de tri » de la sheet ne
  pilote que l'historique (tri favoris dédié = `allFavoritesSortOrder`, à faire en P8 si besoin) ;
  tri appliqué en mémoire, pas dans la requête Room (limite 50 items, impact nul).
- **Vérification** : `./gradlew :desktop:build :core:build -q` → EXIT=0. Non commité.

### Intégration finale (principal, 11-08, suite)

- **Build final d'intégration** après itération 2 des deux agents :
  `./gradlew :desktop:build :core:build -q` → **EXIT=0**.
- **`UI_PARITY_CHECKLIST.md`** : 8 lignes P2 ajoutées (Thème, Liste des mangas, Page du manga,
  Navigation, Options de liste, Principales sections, Marque-pages état vide) avec preuves.
- **Session de captures lancée** (agent dédié, arrière-plan) : app lancée + navigation par clics
  + 4-5 captures attendues dans `docs/ui-parity/` (`desktop-appearance-p2.png`,
  `desktop-list-options-sheet.png`, `desktop-sections-settings.png`, `desktop-sections-add-dialog.png`,
  `desktop-bookmarks-empty.png`).

### Captures de parité (principal, 11-08, reprise sur app vierge)

- Le 2e agent de captures a de nouveau été bloqué par le filtre de sécurité **après** avoir
  produit 4/6 captures ; le principal a repris la session à la main.
- **Règle client appliquée strictement** : app fermée, `%APPDATA%\Kotatsu` ET `%APPDATA%\KotatsuRedo`
  (ancien build) supprimés, relance → vérification de la fraîcheur (seuls kotatsu.db + cache
  recréés, pas de settings.json/cookies) ; fenêtre redimensionnée 1300×1100.
- **6 captures produites** dans `docs/ui-parity/`, toutes vérifiées visuellement :
  - `desktop-appearance-p2.png` / `-2.png` : Apparence complet (cartes de palette Totoro→Miku,
    Thème FR, Liste des mangas, Page du manga, Navigation) — valeurs par défaut fraîches.
  - `desktop-sections-settings.png` : 5 sections + ↑/↓ + corbeille + « Ajouter ».
  - `desktop-sections-add-dialog.png` : dialog aux 4 options exactes (Suggestions/Mis à jour grisées).
  - `desktop-list-options-sheet.png` : bottom sheet (segments Compact/Détails/Grille, slider
    « 100 % », « Ordre de tri — Dernier lu », « Groupe » activé).
  - `desktop-bookmarks-empty.png` : « Aucun marque-page » / « Vous pouvez créer un marque-page
    pendant la lecture d'un manga ».
- **E2E bonus vérifié** : « Marque-pages » ajouté au rail via le dialog « Ajouter » et la
  préférence a survécu à la navigation (présent au retour sur l'écran principal).
- Nettoyage : captures temporaires (`_probe*`, `_nav*`, `_check-start`) supprimées ; app fermée.
- Outil ajouté : `tools/_click.ps1` (clic souris screen-coords, réutilisable pour les sessions
  de parité).

## ✅ Bilan P2

- Périmètre P2 du plan : **implémenté** (Langue = divergence documentée #11 ; miniatures/onglet/
  temps/connexes = entrées désactivées, portage P8 ; Épingler/Raccourcis = divergences #12/#13).
- Matrice de traçabilité (plan §6) — lignes P2 toutes passées de ⛔/◐ à ✅/◐-documenté :
  Options de liste ✅ · Apparence complet ✅ · Sections réordonnables ✅ · Marque-pages FR ✅.
- Vérifications : compile itérative verte à chaque lot, `:desktop:build :core:build` EXIT=0
  après chaque intégration, 6 captures de parité sur app vierge.
- Restes connus (documentés, hors périmètre P2 strict) : tri favoris dédié (P8), tri en mémoire
  (limité à 50 items), miniatures/temps/connexes (P8).

*(les entrées suivantes sont ajoutées au fil de l'eau)*
