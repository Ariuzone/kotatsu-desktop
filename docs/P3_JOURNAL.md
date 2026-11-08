# Journal de la phase P3 — Sources

> Suivi horodaté de tout ce qui est fait pendant P3. Références : `plan_implementation.md` §5 P3,
> `../video-analysis/half1-inventory.md` (lignes 46-64 + moitié 2, 9:35-9:45).
> RÈGLE DE TEST : app testée toujours vierge (wipe `%APPDATA%\Kotatsu` avant lancement —
> consigne client 2026-11-08, voir COORDINATION.md).

## 0. Périmètre P3 (rappel du plan)

- Écran « Gérer les sources » complet : tri, vue grille, recherche, menu
  « Désactiver le contenu explicite » / « Tout désactiver »
- Écran racine « Sources des mangas » avec menu ⋮ (Ordre de tri, Afficher en vue grille,
  Gérer les sources, Sources catalogue, Activer toutes les sources, Choisir le miroir
  automatiquement, Gérer les liens)
- « Sources catalogue » : chips de filtres + menu de langues + état vide + bouton **+**
  installable fonctionnel
- Créateur de préréglages
- Miroir automatique (backend à vérifier)

## 1. État de départ (inventaire principal, 2026-11-08)

| Élément P3 | État | Preuve |
|---|---|---|
| Entrée « Sources des mangas » (Paramètres racine) | ⛔ stub (`SettingsStubScreen`) | `Main.kt:384` — SettingsRoute.SOURCES → stub |
| « Gérer les sources » | ⛔ absent ; action `FR_MANAGE_SOURCES` existe dans le menu de l'onglet Source (`SourcePresetsScreen.kt:125`) mais destination non câblée | grep ManageSources : néant |
| Sources catalogue | ◐ écran existant (top bar + chips + liste + état vide) ; chips actuelles « Toutes les langues ▾ / Nouveaux / Marque-pages » ≠ vidéo ; bouton + = param `onSourceToggle` (à vérifier côté Main) ; « Disponible : N » absent | `SourcesCatalogScreen.kt` (328 l.) |
| Backend activation/désactivation | ✅ `AppSettings.hiddenSources` (KEY_HIDDEN_SOURCES) + `sourcesOrder` (KEY_SOURCES_ORDER, "SCORE") | `AppSettings.kt:61-68,331,384` |
| Créateur de préréglages | ⛔ absent (menu préréglages existe dans l'onglet Source) | grep Dialog : néant |
| Miroir automatique | ⛔ UI absente ; backend à vérifier | — |
| Gérer les liens | ⛔ absent | — |
| Onglet Source (rail) | ✅ existant (`SourcePresetsScreen`, 439 l.) — améliorations P3 : créateur, câblages | — |

## 2. Journal de la session du 8 novembre (après-midi)

### Agent principal

- Inventaire de l'état de départ (tableau ci-dessus).
- Découpage en 2 lots à propriété de fichiers exclusive (COORDINATION.md § P3) ;
  **Main.kt réservé au principal** (intégration/routage après les agents) ; chaînes en
  constantes privées dans les fichiers de chaque agent (pas de Fr.kt partagé).
- Lancement des agents P3-A et P3-B en parallèle (arrière-plan).

### Agent P3-A — racine Sources + Gérer les sources (TERMINÉ, compile verte)

- **Nouveau `SourcesSettingsScreen.kt`** : écran racine « Sources des mangas » (top bar
  `SettingsTopBar`, lignes `PreferenceRow`/`PreferenceSwitchRow`) — Ordre de tri (dialog radio,
  `sourcesSortOrder`), Afficher en vue grille (nouvelle clé `sources_grid`), Gérer les sources
  (« N éléments »), Sources catalogue (« Disponible : N »), Activer toutes les sources de mangas
  (vide `hiddenSources`), Choisir le miroir automatiquement (nouvelle clé `auto_mirror`, backend
  à brancher plus tard — KDoc), Gérer les liens.
- **Nouveau `ManageSourcesScreen.kt`** : liste complète du registry (icône + nom +
  « Catégorie, Langue » + menu ⋮ par ligne avec case « Désactivée » + œil barré), recherche
  intégrée avec filtre direct, menu ⋮ global (« Désactiver le contenu explicite » =
  contentType HENTAI, « Tout désactiver », « Activer toutes les sources »), vue grille
  adaptative optionnelle, tri SCORE/ALPHABETIC. Toggle → `hiddenSources` (noms d'enum
  `MangaSource.name`).
- **`AppSettings.kt`** : + `KEY_SOURCES_GRID`/`isSourcesGrid` (défaut false),
  `KEY_AUTO_MIRROR`/`isAutoMirrorEnabled` (défaut false).
- **Décisions/divergences** : propriété réelle `sourcesSortOrder` (pas la spec `sourcesOrder`) ;
  pas de flag NSFW → contenu explicite = `ContentType.HENTAI` ; param optionnel
  `domainOf: (MangaParserSource) -> String? = { null }` ajouté en fin de signature (favicons —
  le principal passera `container::domainOf`) ; « de façon permanente » (singulier, conforme
  vidéo) ; « Popular & reliable » laissé EN comme l'original ; chaînes en `private const` locaux.
- **Compile** : `:desktop:compileKotlin` → BUILD SUCCESSFUL. Non commité.

### Agent P3-B — catalogue + préréglages + liens (TERMINÉ, compile verte)

- **`SourcesCatalogScreen.kt`** réécrit : chips « Toutes les langues ▾ / Nouveaux / Mangas /
  Hentais / Comics / Autre / Manhwa », menu de langues plein écran (langues natives triées +
  « Langues diverses » en fin), « Disponible : N » sous les chips, état vide FR exact, bouton
  **+** fonctionnel (nouveau param optionnel `appSettings` → toggle direct `hiddenSources`,
  visuel ✓/+ ; `onSourceToggle`/`isAdded` ignorés quand fourni).
- **`PresetCreatorDialog.kt`** (nouveau) : chips ~36 langues natives, « Enregistrer » désactivé
  tant que rien n'est choisi, « Annuler » + ✕. Câblé dans `SourcePresetsScreen.kt` via entrée
  « Nouveau préréglage » ; persistance Room **existante** réutilisée
  (`SourcePresetsDao`/`SourcePresetEntity` — nouveau param optionnel `presetsDao`).
- **`ManageLinksScreen.kt`** (nouveau) : adaptation desktop lecture seule (textes FR exacts,
  pas d'intents sur bureau — divergence à documenter).
- **Divergences notées** : ordre du menu langues (diverses en fin vs milieu) ; chip « English ▾ »
  non ajoutée (frame 9:35 vérifiée : une seule chip de langue) ; « Manhwa » = ContentType.MANHWA
  uniquement ; « Autre » = tout le reste.
- **Compile** : EXIT=0. Non commité.

### Intégration du principal (11-08)

- **`Main.kt`** : + `showManageSources`/`showManageLinks` (états, désélection rail) ;
  route `SettingsRoute.SOURCES` → `SourcesSettingsScreen` (remplace le stub, compteurs
  enabled/total/available) ; branches `ManageSourcesScreen` (avec `container::domainOf`) et
  `ManageLinksScreen` ; catalogue appelé avec `appSettings = container.appSettings` (bouton +
  direct) ; `SourcePresetsScreen` avec `presetsDao = container.database.getSourcePresetsDao()` ;
  `onManageSourcesClick` → Gérer les sources (renvoyait au catalogue).
- **Build complet** : `./gradlew :desktop:build :core:build -q` → **EXIT=0**.

### Captures de parité (principal, 11-08, app vierge)

- Règle client appliquée : app fermée (fenêtre tuée), `%APPDATA%\Kotatsu` + `KotatsuRedo`
  supprimés, relance → vérification fraîcheur (kotatsu.db + cache seulement).
- **7 captures vérifiées visuellement** dans `docs/ui-parity/` :
  - `desktop-sources-settings.png` : racine « Sources des mangas » — Ordre de tri (« Popular &
    reliable » EN non traduit comme l'original), Vue grille, Gérer les sources « 1385 éléments »,
    Sources catalogue « Disponible : 955 », Activer toutes les sources, Miroir automatique,
    Gérer les liens.
  - `desktop-manage-sources.png` : liste favicons + « Catégorie, Langue » + menu ⋮ par ligne.
  - `desktop-manage-sources-menu.png` : « Désactiver le contenu explicite » (case) / « Tout
    désactiver » / « Activer toutes les sources ».
  - `desktop-sources-catalog.png` : chips exactes + « Disponible : 955 » + ✓ d'installation.
  - `desktop-sources-catalog-langs.png` : menu langues plein écran (langues natives triées).
  - `desktop-manage-links.png` : adaptation desktop lecture seule, textes exacts.
  - `desktop-preset-creator.png` : « Nouveau préréglage », chips ~36 langues natives,
    « Enregistrer » désactivée, « Annuler ».
- App fermée après captures ; capture temporaire supprimée.

## ✅ Bilan P3

- Matrice (plan §6) : Sources des mangas (tri/gestion/catalogue/miroir switch/liens) ⛔/◐ → ✅ ;
  Créateur de préréglages ⛔ → ✅ ; Catalogue installable ◐ → ✅ (bouton + fonctionnel).
- Vérifications : compile verte par lot, build complet EXIT=0 après intégration, 7 captures sur
  app vierge.
- Restes documentés : backend miroir non branché (clé `auto_mirror` prête, UI ✅) ;
  ManageLinksScreen lecture seule (pas de deep-links desktop) ; ordre du menu langues (diverses
  en fin) ; « Popular & reliable » EN non traduit comme l'original.
