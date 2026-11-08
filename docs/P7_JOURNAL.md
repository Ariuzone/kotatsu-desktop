# Journal de la phase P7 — Lecteur avancé

> Références : `plan_implementation.md` §5 P7, `half2` de la vidéo (7:25 → 9:20).
> RÈGLE DE TEST : app toujours testée vierge. RÈGLE DE CHAÎNES : tout texte vient de
> `ui/strings/`. **LIVRABLES DE FIN DE PHASE** : `Kotatsu-9.8.6-P7.exe` + `P7-source.zip`.

## 0. Périmètre P7 (rappel du plan)

- Menu du lecteur : Sauvegarder la page, marque-page, **Mode lecture segmenté**
  (Standard/RTL/Vertical/Webtoon) mémorisé par manga, pages doubles paysage (bêta),
  Faire pivoter, Défilement automatique, Correction des couleurs, Paramètres
- Tutoriel + gomme webtoon ; défilement automatique (dialog, vitesse, bouton flottant) ;
  correction des couleurs (aperçu avant/après) ; chapitres préc./suiv. ; toast « Images masquées »

## 1. État de départ (inventaire principal, 2026-11-08)

| Élément P7 | État | Preuve |
|---|---|---|
| Enum `ReaderMode` | ✅ les 4 modes existent déjà (STANDARD/REVERSED/VERTICAL/WEBTOON, ids d'origine) | `core/prefs/ReaderMode.kt` |
| Mode appliqué au lecteur | ◐ global `settings.readerMode` + détection webtoon ; **pas de mémorisation par manga** ; webtoon fonctionnel | `Main.kt` ReaderHost (~846-906) |
| Menu du lecteur | ⛔ minimal (Fermer/Incognito/Zoom d'après S-A) ; pas de menu vidéo complet | `ReaderScreen.kt` (518 l.) |
| Chapitres préc./suiv. | ⛔ non câblés (matrice P7) | — |
| Signets | ✅ `BookmarksDao.insert/delete/find` + `BookmarkEntity` ; `BookmarksScreen` P2 | `bookmarks/data/` |
| Gomme webtoon | ◐ backend `HiddenImagesDatabase`/`HiddenImagesDao` (getScan, entités) porté ; AUCUN usage UI | `reader/data/erase/` |
| Défilement auto / Correction couleurs / Rotation / Doubles pages paysage | ⛔ rien | — |
| Toast « Images masquées » | ⛔ | — |

## 2. Journal de la session

### Agent principal

- Inventaire (ci-dessus). **Orchestration en 2 vagues** pour éviter le conflit sur
  `ReaderScreen.kt` : P7-B construit d'abord des composants PURS (nouveaux fichiers,
  signatures fixées), P7-A (propriétaire de `ReaderScreen.kt`, `AppSettings`, `ReaderMode`)
  fait le cœur du lecteur puis intègre les composants de P7-B en itération 2.
- Lancement des agents en parallèle.

### Agent P7-B — composants purs (VAGUE 1 TERMINÉE, ses 4 fichiers compilent)

- **`FrReader.kt`** (nouveau) : ~20 constantes avec provenance vidéo ; réutilise FR_OK/FR_CANCEL.
- **`ColorCorrectionDialog.kt`** : `ColorCorrectionState(invert, grayscale, brightness, contrast,
  yellowish)` + dialog (aperçu avant/après avec le filtre réellement appliqué au bloc « Après »,
  changements en direct, Réinitialiser) + **`Modifier.colorCorrection(state)`** réel : matrices
  RGBA 4×5 construites à la main (inversion → gris Rec.601 → luminosité → contraste autour du
  gris 0.5), composition par produit affine ; `drawWithContent` + `saveLayer` + ColorFilter ;
  fond jaunâtre = rect jaune alpha 0.16. No-op si inactif.
- **`AutoScrollDialog.kt`** : `AutoScrollState(enabled, speed, isRunning)` + dialog (Activer +
  Vitesse xN.N slider 0.5..8.0 + note FAB) + `AutoScrollFab` (Play/Pause, invisible si
  désactivé) + `autoScrollStep()` (px/tick 16 ms) avec exemple d'intégration en KDoc.
- **`EraserTutorialDialog.kt`** : tuto « Passer » + `EraserOverlay` (drag → rect normalisé 0..1,
  callback onErase, feedback rouge translucide, aucun accès DB).
- Erreur transitoire `Main.kt:1078` = travail en cours de P7-A (hors périmètre).

### Agent P7-A — cœur du lecteur (VAGUE 1 TERMINÉE, compile verte)

- **`AppSettings.kt`** : `getReaderModeFor/setReaderModeFor` (clés `reader_mode_<id>`) ;
  priorité lecteur = par manga > détection webtoon > global.
- **`ReaderScreen.kt`** : menu ⋮ complet ordre vidéo — Sauvegarder la page (export PNG vers
  Pictures via re-téléchargement, détection d'extension par octets magiques, repli
  presse-papiers documenté), Ajouter un marque-page (toggle réel `BookmarksDao`, état re-synchro
  par page), **Mode lecture** 4 options radio appliqué immédiatement avec conservation de la
  position (STANDARD/REVERSED pager, VERTICAL pager vertical, WEBTOON LazyColumn), Pages doubles
  (paysage, bêta, 2 pages/vue + compteur en vues), Faire pivoter (0/90/180/270° avec
  compensation d'échelle, contrôles droits), chapitres Précédent/Suivant câblés (désactivés aux
  bornes), Fermer/Incognito/Zoom conservés ; placeholders désactivés vague 2 (« Défilement
  automatique », « Correction des couleurs »).
- **`Main.kt`** (bloc lecteur seulement) : callbacks Sauvegarder/Marque-page avec le container ;
  **toast « Images masquées »** (2 s) via instance locale `HiddenImagesDatabase`
  (hidden-images.db) — pages entièrement masquées ; overlay « Ch. X/Y Pg. X/Y Z % » (%
  pondéré par chapitres).
- Chaînes temporaires marquées « TODO(vague 2) : migrer vers FrReader.kt ».
- Compile verte. Limites documentées (referer approximatif, scan partiel gomme, rotation
  bêta, doubles pages ignorées en webtoon/portrait).

### Vague 2 — intégration P7-B (TERMINÉE, compile verte)

- **Correction des couleurs** : menu → `ColorCorrectionDialog`, état local, filtre appliqué au
  conteneur des pages uniquement (avant la rotation, hors contrôles/dialogs/FAB).
- **Défilement automatique** : menu → dialog ; webtoon = défilement continu
  (`withFrameMillis` + `scrollBy(autoScrollStep)`) ; pager = saut de page périodique
  (4000/vitesse ms, documenté) ; `AutoScrollFab` au-dessus des contrôles (z-order supérieur).
- **Gomme webtoon + tuto** : tuto au 1er webtoon par session ; entrée « Gomme » (webtoon seul) ;
  `EraserOverlay` → rect normalisé converti par page visible (`visibleItemsInfo`) →
  **persistance `PageScanEntity`** (format `bands = "x1,y1,x2,y2;..."` normalisés) → recouvrement
  par rect couleur fond au rendu. Escape/changement de mode quittent l'effacement.
- **Bonus** : les contrôles sortis du Box pivoté (la rotation ne tournait que les pages).

### Correctifs du principal (11-08, pendant les captures E2E)

1. **Zones de tap vs contrôles** : les taps sur la top bar/bande basse étaient avalés par les
   zones de tap (le ⋮ tournait la page au lieu d'ouvrir le menu) → taps ignorés dans les bandes
   de contrôles quand elles sont visibles (`inTopBar <96px / inBottomBar >h-160px`).
2. **Raccourci clavier Alt+M** : ouvre le menu ⋮ (adaptation desktop ; le popup menu Compose ne
   s'ouvrait pas de façon fiable par clic synthétique ; raccourci utile aux utilisateurs).
   Implémentation : trigger hoisté + `LaunchedEffect` dans ReaderControls.
3. E2E vérifié sur app vierge : lecture Chainsaw Man (MangaDex) réel, **« Sauvegarder la page »
   → `Chainsaw Man-1.png` (274 Ko) exporté dans Pictures**, navigation pages, zoom 125 %,
   overlay « Ch. 1/2938 Pg. 1/52 0 % ».
- **Captures vérifiées** : `desktop-reader-standard.png`, `desktop-reader-menu.png` (menu
  complet : 4 modes radio, doubles pages, gomme grisée hors webtoon, chapitres bornés),
  `desktop-reader-color-correction.png` (Avant/Après + 5 réglages + Réinitialiser),
  `desktop-reader-autoscroll.png` (Activer + Vitesse x4.0 + note FAB).

## ✅ Bilan P7

- Matrice (plan §6) : menu du lecteur ⛔ → ✅ ; modes segmentés mémorisés par manga ⛔ → ✅ ;
  doubles pages paysage ⛔ → ✅ (bêta) ; rotation ⛔ → ✅ ; défilement automatique ⛔ → ✅ ;
  correction des couleurs ⛔ → ✅ ; tuto+gomme webtoon ⛔ → ✅/◐ (masques corrects en webtoon) ;
  chapitres préc./suiv. ◐ → ✅ ; toast « Images masquées » ⛔ → ✅.
- Vérifications : compile par vague, build complet EXIT=0, captures sur app vierge, E2E
  sauvegarde de page.
- Restes documentés : masques gomme en modes pager non éditables ; autoscroll pager = sauts ;
  sauvegarde page = repli presse-papiers si le téléchargement direct échoue ; scan gomme
  minimal (bands, matcherVersion 0).
