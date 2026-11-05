# Audit de portage — Kotatsu-Redo (Android) → Windows Desktop

Date : 2026-11-05. Source auditée : https://github.com/Kotatsu-Redo/Kotatsu-Redo, branche `devel`
(commit du 2026-11-05), module `app/`, 1152 fichiers Kotlin, `versionName 9.8.6`.

## 0. Architecture d'origine

- Gradle single-module Android (`app/`), Views/ Fragments XML + un peu de Compose (nav flottante).
- Room v28 (`core/db/MangaDatabase.kt`) — entités/DAO répartis entre `core/db` et les features
  (`favourites/data`, `history/data`, `bookmarks/data`, `local`, `scrobbling`, `stats`, `suggestions`, `tracker`, `explore`).
- Hilt (DI décentralisée, ~10 modules), SharedPreferences (`core/prefs/AppSettings.kt`, 1010 lignes),
  OkHttp 5, Coil 3 (déjà multiplateforme), WorkManager, WebView (Cloudflare), Sentry/ACRA.
- UI : 45+ activités, 64 fragments, 171 layouts, navigation par fragments.

## 1. Classification du code source

Légende : **(a)** pur Kotlin/JVM réutilisable tel quel · **(b)** dépend d'Android mais remplaçable ·
**(c)** UI Android à reconstruire en Compose.

| Paquet | Fichiers | Classe | Détail |
|---|---|---|---|
| `core/cache` | 4 | (a) | LRU + caches mémoire expirants |
| `core/db` | 47 | (b) | Room : entités/DAO 100 % `androidx.room`/`androidx.sqlite` → **portage direct en Room KMP cible JVM** (Room 2.8.4) ; 28 migrations SQL pures ; `MangaQueryBuilder` = SQL pur |
| `core/exceptions` | 24 | (a) sauf `resolve/` | hiérarchie d'exceptions pure ; `resolve/` = UI (Snackbar/Toast) → Compose |
| `core/fs`, `core/io`, `core/zip` | 3 | (a) | java.io / okio |
| `core/github` | 3 | (a) | Retrofit + kotlinx.serialization (maj app) |
| `core/image` | 9 | (b) | Coil 3 KMP : décodeurs AVIF/JXL, CBZ fetcher — ajustements mineurs JVM ; `CoilImageView` (c) |
| `core/model` | 16 | (a) / (c) parcelable | `Manga`, `MangaHistory`, `MangaSource`, `FavouriteCategory`, tris — purs ; `parcelable/` abandonné |
| `core/nav` | 5 | (c) | intents Android → navigation Compose |
| `core/network` | 26 | (a/b) | intercepteurs purs (rate limit, gzip, headers, DoH) ; cookies → jar fichier ; WebView (c) → stub |
| `core/os` | 7 | (c) | shortcuts/SAF/ConnectivityManager ; interface `NetworkState` conservée |
| `core/parser` | 15 | (a/b) | **cœur** : `MangaRepository`, factory, `CachingMangaRepository`, `MangaLoaderContextImpl` (retirer Context/Base64/WebView) |
| `core/prefs` | 19 | (b) | 17 enums purs ; `AppSettings` → façade sur store clé-valeur fichier ; widgets (c) |
| `core/ui` | 98 | (c) | 100 % Android → réécrit en Compose |
| `core/util` | 67 | mixte | la moitié `ext/` est pure (`Collections`, `Date`, `Flow`, `File`, `IO`, `String`, `Throwable`) ; ACRA/Sentry (b) → remplacé |
| features UI (`details`, `reader`, `favourites`, `history`, `discover`, `explore`, `search`, `filter`, `list`, `settings`, `download`, `main`, `bookmarks`, …) | ~600 | (c) majoritaire | ViewModels souvent portables avec ajustements (pas de AndroidViewModel) ; data/domain portables |

### Bilan quantitatif
- ~60-70 % de `core` hors `core/ui` est portable.
- 5 couplages durs identifiés : **Room** (→ Room KMP/JVM, mêmes schéma+SQL), **SharedPreferences**
  (→ store KV fichier + façade `AppSettings` portée), **WebView/Cloudflare** (→ stub `evaluateJs`),
  **Coil décodeurs** (→ Coil 3 JVM), **core/ui** (→ Compose).

## 2. Bibliothèque de parsers (`kotatsu-parsers-redo`)

- Kotlin/JVM **pur** (aucune dépendance Android ; `androidx.collection` est JVM-ok), 1474 fichiers, 1200+ sources.
- API : `MangaLoaderContext` (hôte fournit `OkHttpClient`, `CookieJar`, `evaluateJs`, config par source),
  `MangaParserSource` (enum généré par KSP), `MangaParser`/`PagedMangaParser` par site, modèles `Manga/MangaChapter/MangaPage/MangaTag/...`.
- **Consommation : artefact JitPack `com.github.clquwu:kotatsu-parsers-redo:68f3fed395`** — identique à
  l'app Android. Résolution vérifiée : OK.

## 3. Inventaire des écrans (source de vérité UI)

### Activités (45, hors debug/widget)
main (MainActivity, ProtectActivity), reader (ReaderActivity, ColorFilterConfig), details (Details,
DetailsByLink, RelatedManga), search (MangaListActivity, SearchActivity), favourites (Favourites,
FavouriteCategories, CategoryEdit), history, downloads, explore (Sources, SourcePresetList/Edit,
Suggestions), settings (SettingsActivity, AppUpdate, OverrideConfig, ProtectSetup, ReaderTapGridConfig,
SourceAuth, SourcesCatalog, MangaDirectories), scrobbling (ScrobblerConfig, DiscordAuth, KitsuAuth),
sync (SyncAuth, SyncAuthReset), tracker (Updates, TrackerDebug), alternatives (Alternatives,
SourceReplacement, AlternativeCovers), bookmarks (AllBookmarks), browser (BrowserActivity, CloudFlare),
image (ImageActivity, PageImagePick), stats, CrashReport.

### Fragments principaux (64)
- Feed : `FeedFragment`, `UpdatesFragment`, `SuggestionsFragment`, `DiscoverFragment`, `ExploreFragment`.
- Listes : `MangaListFragment` (+ modes grille/liste/détails), `HistoryListFragment`, `FavouritesContainerFragment`, `FavouritesListFragment`, `LocalListFragment`, `RelatedListFragment`, `RemoteListFragment`.
- Détails : `ChaptersFragment`, `PagesFragment`, `PreviewFragment`, `AllBookmarksFragment`, `BookmarksFragment`.
- Lecteur : `WebtoonReaderFragment`, `PagerReaderFragment`, `DoubleReaderFragment`, `ReversedReaderFragment`, `ReversedDoubleReaderFragment`, `VerticalReaderFragment` (base `BaseReaderFragment`/`BasePagerReaderFragment`), `PagePickerFragment`, `HiddenImagesFragment`.
- Recherche : `SearchActivity` (multi-sources) avec `FilterHeaderFragment`, `FilterSheetFragment`, suggestions (`fragment_search_suggestion_*`).
- Paramètres : Root, Appearance, Reader, Downloads, Network, Notifications, Sync, Backup(s), Sources/Catalog/Auth, Privacy, Community, DataCleanup, EInk, Suggestions, Tracker, About (+ `SettingsSearchFragment`).
- Dialogues : download, favorite (choix catégories), backup/restore/import, error details, manga unavailable, directory select, progress, changelog, etc.

### Ressources
- Thème : `Theme.Material3.DayNight.NoActionBar` avec palette M3 complète générée par Material Theme Builder
  (`colors_kotatsu.xml`, clair+sombre via `values-night`), 9 schémas d'accent (`themes_colored.xml` :
  Asuka, Itsuka, Kanade, Mamimi, Miku, Mion, Rikka, Sakura, Totoro), overlay AMOLED
  (`ThemeOverlay.Kotatsu.Amoled`), couleurs dynamiques Material You, appliqués dans `BaseActivity`
  (résolution : `settings.colorScheme.styleResId` + AMOLED overlay).
- 171 layouts, ~35 items lists, 1176 chaînes i18n, `dimens.xml`, polices `res/font`.

### Captures de référence
`metadata/en-US/images/phoneScreenshots/` (1-6) et `tenInchScreenshots/` (1-2).

## 4. Décisions d'architecture pour le portage

| Android | Desktop (choix retenu) | Justification |
|---|---|---|
| Room (Android) | **Room KMP cible JVM** + `sqlite-bundled-jvm` | mêmes entités/DAO/SQL/migrations → fidélité maximale, vérifié compilable |
| SharedPreferences | `KeyValueStore` (JSON fichier) + port de `AppSettings` | pas de dépendance Android, mêmes clés/defauts |
| Hilt | injection manuelle (arbre de dépendances explicite) | DI simple, projet desktop mono-process |
| Coil 3 Android | Coil 3 JVM (`coil-core` + `coil-network-okhttp`) | déjà KMP |
| WorkManager | coroutines (`CoroutineScope` applicatif) | pas de tâches longues critiques hors téléchargements/tracker |
| WebView (Cloudflare) | **stub** — sources protégées non supportées en P1, documenté | pas de WebView JVM ; Cloudflare demanderait un navigateur intégré (P3+) |
| Biometric | verrouillage par mot de passe (comme l'app en fallback) | P3 |
| Notifications | notifications Windows via tray (Compose desktop `Tray`) | P3 |
| Views/RecyclerView | **Compose Multiplatform Material 3** avec les mêmes couleurs/dimensions | exigence de fidélité |

## 5. Modules livrés

- `:core` — Kotlin/JVM : modèles, réseau, prefs, Room JVM, repositories, parsers (JitPack).
- `:desktop` — Compose Multiplatform Desktop : UI, navigation, packaging (jpackage → `.exe`).

## 6. Ce qui ne sera pas porté (documenté, non simulé)

- Résolution Cloudflare intégrée (WebView) — sources protégées seulement.
- Widgets d'écran d'accueil Android (appwidget shelf/recent).
- Sentry/ACRA (crash reporting tiers) — remplacé par journal local.
- Plugin system externe (`core/parser/external`, ContentProvider).
- Partage d'assets Android (SAF), raccourcis launcher.
