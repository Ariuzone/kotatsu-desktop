# Coordination des agents

## État des lots

| Lot | Agent | Statut | Branche | Dernier commit | Blocages | Prochaine action |
|---|---|---|---|---|---|---|
| L2 core-données (Room JVM) | sous-agent B | **fini, fusionné** — 97 fichiers, 17 entités, 16 DAO, 28 migrations, schéma v28 exporté, 3/3 tests | agent/core-data | fusion | — | — |
| L3 core-réseau/parser/prefs | sous-agent A | **fini, fusionné** — test réseau réel PASSED (DYNASTYSCANS) | agent/core-net | d1f2e47 | — | — |
| L4a ui-shell + thème | principal | **fini** | main | 266cf2b | — | — |
| L4b ui-écrans (Explore/Catalogue/Détails/Lecteur/Recherche/Paramètres) | principal | **fini** — E2E réseau réel PASSED (catalogue>détails>chapitres>pages>URL après fix org.json lenient) | main | 04819e8 | — | — |
| L4c écrans History/Favorites/Downloads | sous-agent C | **fini, fusionné** + câblé à la DB réelle | agent/ui-screens | fusion 04819e8 | — | écran Downloads à brancher au lot P2 |
| P2 téléchargements + CBZ local | sous-agent D puis principal (quota épuisé, travail repris) | **fini, fusionné** — 12/12 tests core verts, câblé (rail + enqueue depuis Détails + dépôt local dans la fabrique) | agent/downloads | fusion dc0faf7 | — | — |
| P3 tracker + verrouillage | sous-agent E | **fini, fusionné** — 14/14 tests verts, câblé (Tray + ProtectScreen) | agent/tracker | fusion | — | — |
| L5 packaging/CI | principal | **fini** — installateur final Kotatsu-9.8.6.exe (94 Mo, JDK embarqué, WiX 3 portable) ; fix ProGuard keep Room/sqlite vérifié par lancement de l'app packagée (DB v28 créée) ; install locale bloquée par UAC → CI | main | — | — | — |
| Signets (UI) | principal | **fini, branché** | main | — | — | — |
| L6 docs | principal | en cours | main | — | — | rapport final |

## Requêtes (destinataire : agent)

*(vide)*

## Phase P2 — Apparence complète (2026-11-08, **TERMINÉE**)

Journal détaillé : `docs/P2_JOURNAL.md` (état hérité, itérations des agents, captures, bilan).
Spécification : `../video-analysis/half1-inventory.md` (frames 1:40 → 2:25). Baseline `bfec3c2`.

| Lot | Agent | Périmètre exclusif | Résultat |
|---|---|---|---|
| **P2-A parité Apparence** | agent P2-A | `SettingsScreen.kt`, `Fr.kt`, `ColorSchemeCards.kt` (nouveau), `SectionsSettingsScreen.kt`, `UI_DIVERGENCES.md` | ✅ Cartes de palette (vraies couleurs), Thème 100 % FR, dialog « Ajouter » aux 4 options exactes, revue libellé par libellé, 5 divergences documentées (#10-#14) |
| **P2-B options de liste** | agent P2-B | `ListOptionsSheet.kt` (nouveau), `Main.kt`, `HistoryScreen.kt`, `FavouritesScreen.kt`, `BookmarksScreen.kt`, `AppSettings.kt` | ✅ Bottom sheet complète (segments/slider/tri/groupe) branchée Historique **et** Favoris, tri appliqué, groupement persisté (clé `history_grouping`), marque-pages FR |
| **Captures** | agent (échec filtre) puis principal | — | ✅ 6 captures sur **app vierge** (règle client), vérifiées visuellement |

Vérifications finales : `:desktop:build :core:build` EXIT=0 · checklist P2 à jour ·
E2E « Ajouter Marque-pages au rail » validé. Restes hors périmètre documentés (tri favoris P8,
miniatures/temps/connexes P8).

## Phase P3 — Sources (2026-11-08, en cours)

Journal détaillé : `docs/P3_JOURNAL.md`. Spécification : `../video-analysis/half1-inventory.md`
(lignes 46-64 + moitié 2 t≈9:35-9:45). Main.kt **réservé au principal** (routage en intégration) ;
chaînes en constantes privées dans les fichiers de chaque agent (Fr.kt interdit aux deux).

| Lot | Agent | Périmètre exclusif | Critères de fin |
|---|---|---|---|
| **P3-A racine Sources + Gérer les sources** | agent P3-A | nouveaux `ui/screens/sources/SourcesSettingsScreen.kt` + `ManageSourcesScreen.kt`, `core/prefs/AppSettings.kt` (si nouvelle clé) | Écran racine « Sources des mangas » avec menu ⋮ exact (Ordre de tri, Afficher en vue grille, Gérer les sources, Sources catalogue — Disponible : N, Activer toutes les sources de mangas, Choisir le miroir automatiquement, Gérer les liens) ; « Gérer les sources » : liste icône+nom+« Catégorie, Langue »+menu ⋮ par ligne+œil barré, recherche, menu global (« Désactiver le contenu explicite », « Tout désactiver »), toggle → `hiddenSources`, tri `sourcesOrder` ; compile OK |
| **P3-B catalogue + installables + préréglages + liens** | agent P3-B | `SourcesCatalogScreen.kt`, `SourcePresetsScreen.kt`, nouveau `ManageLinksScreen.kt` (+ dialog créateur de préréglages) | Catalogue : chips conformes vidéo (« Toutes les langues ▾ / Nouveaux / Mangas / Hentais / Comics / Autre / Manhwa »), menu langues plein écran, état vide FR exact, « Disponible : N », bouton **+** fonctionnel (toggle `hiddenSources`) ; créateur de préréglages (« Nouveau préréglage », chips de langues, « Enregistrer » désactivé tant que vide) ; « Gérer les liens » (adaptation desktop documentée) ; compile OK |

## Phase P4 — Suggestions & nouveaux chapitres (2026-11-08, en cours)

Journal : `docs/P4_JOURNAL.md`. Spécification : `half1-inventory.md` lignes 62-64. Préférences
P4 **déjà ajoutées** par le principal (compile verte) : `excludedSuggestionGenres`,
`isSuggestionsIncludeDisabled`, `trackerFrequency`, `trackerFavouriteCategories` →
**`AppSettings.kt` INTERDIT aux deux agents**. `Main.kt` et `Fr.kt` interdits (principal).
Chaînes en `private const` locaux.

| Lot | Agent | Périmètre exclusif | Critères de fin |
|---|---|---|---|
| **P4-A Suggestions** | agent P4-A | nouveau `ui/screens/settings/SuggestionsSettingsScreen.kt` | Écran « Suggestions » : bandeau interrupteur « Activer les suggestions », « Inclure les sources désactivées », « Exclure des genres » (+ dialog saisie, Annuler/OK), bannière note locale exacte (« Toutes les données sont analysées localement… ») ; compile OK |
| **P4-B Nouveaux chapitres** | agent P4-B | nouveau `ui/screens/settings/FeedSettingsScreen.kt` (+ sous-écran journal si besoin) | Écran « Nouveaux chapitres » : « Vérifier les nouveaux chapitres et les notifier » (KEY_TRACKER_ENABLED), « Fréquence de vérification » (trackerFrequency), « Rechercher les mises à jour » (action → TrackerScheduler si appelable), « Catégories favorites — N de M activé(s) » (dialog multi-choix, trackerFavouriteCategories), « Télécharger de nouveaux chapitres » (TrackerDownloadStrategy), « Dépannage » + journal de vérification, « Désactiver l'optimisation de la batterie » divergente desktop ; compile OK |

## Phase P5 — Vos données & Application (2026-11-08, en cours)

Journal : `docs/P5_JOURNAL.md`. `Main.kt`/`Fr.kt`/`AppSettings.kt` INTERDITS aux agents
(toutes les clés nécessaires existent déjà). Chaînes en `private const` locaux.

| Lot | Agent | Périmètre exclusif | Critères de fin |
|---|---|---|---|
| **P5-A Vos données** | agent P5-A | nouveaux `ui/screens/settings/DownloadsStorageSettingsScreen.kt`, `StorageUsageScreen.kt`, `DataRemovalScreen.kt`, `BackupRestoreScreen.kt` | Écran « Téléchargements et stockage » : « Dossier pour les téléchargements » (chemin réel + ouvrir), « Utilisation du stockage » (tailles réelles DB/téléchargements/cache), « Data removal » (actions réelles : historique, cache, suggestions) ; « Sauvegarde et restauration » : « Create or restore a backup » (EN conservé, libellé vidéo) + « Sauvegardes » → créer/restaurer un zip (kotatsu.db + settings.json) via JFileChooser ; compile OK |
| **P5-B Application** | agent P5-B | nouveaux `ui/screens/settings/NotificationsSettingsScreen.kt`, `NetworkSettingsScreen.kt`, `AboutScreen.kt` | « Notifications » : tracker notifications + no NSFW ; « Réseau » : Données mobiles (metered TriState, Wi-Fi only suggestions/tracker), « Proxy » (type/adresse/port/login), « DNS sur HTTPS » (DoHProvider) ; « À propos » : Version 9.8.6, licence, liens ; compile OK |

## Notes d'environnement

- **RÈGLE DE TEST (consigne client, 2026-11-08) : avant CHAQUE test/lancement de l'app, effacer
  les données utilisateur** — fermer l'app puis supprimer `%APPDATA%\Kotatsu` (kotatsu.db*,
  settings.json, cookies.json, local/, okhttp-cache/) — et relancer : on teste TOUJOURS une app
  vierge, comme une première ouverture. S'applique à tous les agents (captures, E2E, manuel).
- **RÈGLE DE CHAÎNES (consigne client, 2026-11-08) : toute description/chaîne affichée vient de
  la source de chaînes (`desktop/ui/strings/`), jamais écrite en brut dans un écran.** Les
  `private const val` locaux des phases P3-P5 sont bannis : ils migrent vers des fichiers
  `public const val FR_*` dans `ui/strings/` (Fr.kt existant, FrSources.kt pour les écrans
  sources, FrSettings.kt pour les écrans de réglages). Même convention pour les
  `contentDescription` non nuls.
- Limite : **2 sous-agents simultanés** (passée de 1 à 2 en cours de projet) (« user concurrency limit exceeded » au-delà) → lots L2/L3/L4b/L5 séquencés.
- JDK : Android Studio JBR 25 (`C:/Program Files/Android/Android Studio/jbr`), `JAVA_HOME` à exporter pour chaque commande Gradle.
- Versions épinglées : Kotlin 2.2.10, CMP 1.9.0, Room 2.8.4 JVM, OkHttp 5.3.2, Coil 3.4.0, parsers JitPack 68f3fed395.
- `room-ktx` et `coil-gif` n'ont pas de variante JVM → retirés.

## Refonte fidélité (2026-11-06, après retours client)

Captures réelles fournies par le client → 2 agents relancés en continu :
- **agent/ui-fr** : refonte complète des écrans (français exact, Totoro vectoriel, Discover
  à sections, onglet Source, catalogue, recherche, Flux, LOCAL en cours) — lot 1+2 fusionnés
  et intégrés dans Main.kt (rail 5 onglets FR). Backlog de fiabilité en cours.
- **agent/net-rel** : sonde 36 sources — baseline 23/36 puis 25/36 (MANGADEX fixé via headers) ;
  RetryInterceptor + DeadDomainException commités ; en cours : ordres de tri explicites
  (MangaDex POPULARITY/UPDATED renvoient du HTML — cas transmis par l'orchestrateur) + SSL.
- Orchestrateur : rail 5 onglets, mode incognito, ImageLoader Coil (Referer par source),
  onglet de démarrage pour tests non intrusifs, restauration des routes DB après refonte.

## Fusion net-rel + Discover alimenté (2026-11-06 soir)

- **Cause racine du « sources ne fonctionnent pas »** (agent net-rel) : le client desktop
  n'envoyait jamais l'UA par parser (`Kotatsu/6.8…`) — `repositoryFactoryProvider` n'était pas
  câblé dans NetworkModule, donc CommonHeadersInterceptor tombait sur un UA navigateur,
  et les API (MangaDex…) répondaient par des challenges HTML. Fix : `globalInstance` +
  `parserFor(source)`, en-têtes résolus par source comme sur Android.
- Taux de succès : 63 % → **72 %** (34/47) ; RetryInterceptor ; DeadDomainException
  (domaines morts masquables) ; certificats Let's Encrypt YE embarqués (MANGAPARK OK).
- Discover vérifié visuellement : sections « Pour vous / Nouveautés pour vous » alimentées
  en titres réels (docs/ui-parity/desktop-discover-data.png). Couvertures : diagnostic en cours
  (agent net-rel).
- Écran « On device » branché (CBZ locaux via LocalMangaRepository).
