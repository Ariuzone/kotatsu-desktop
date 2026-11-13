# Journal de la phase P9 — Parité finale

> Dernière phase du plan. Objectifs : zéro chaîne FR-only en mode EN, captures comparées
> complètes, documentation finale cohérente.
> RÈGLE DE TEST : app toujours testée vierge. RÈGLE DE CONTENU : jamais de source explicite.
> **LIVRABLES** : `Kotatsu-Desktop-1.1.1.exe` reconstruit + `source.zip` final → `../releases/`.

## 0. Périmètre P9 (rappel du plan + restes connus)

- Captures comparées écran par écran ; checklist finale
- Chaînes EN « volontaires » : décision assumée = corrigées plutôt que reproduites
  (les coquilles vidéo « adutes », « Certaines appareils » etc. ne sont PAS reproduites)
- Restes identifiés en 1.1.1 :
  1. **Sous-titres inline FR** résiduels dans des écrans (ex. AMOLED « Utilise moins
     d'énergie pour les écrans AMOLED »)
  2. **Fonctions de libellés FR-only** : frListModeLabel, frSortOrderLabel, frStateLabel,
     frProgressModeLabel, frNavItemLabel, frReaderModeLabel, frDuration, frChaptersCount,
     frContinueChapter, frChapterXofY, frReaderInfo, frReaderPercent, frAvailableCount,
     frItemsCount, frBookmarkPage, frSortOrderLabel (18 valeurs)…
  3. **Chaînes temporaires FR_READER_*** dans ReaderScreen.kt (8) et Main.kt (4, toasts)
     marquées « TODO vague »
  4. Feeds/libellés d'états téléchargements EN « volontaires » → désormais via i18n

## 1. Journal de la session (2026-11-13)

### Agent principal

- Inventaire des restes (ci-dessus), découpage 2 agents :
  - **P9-A i18n final** : chasse des inline + branchement des fonctions de libellés sur
    l'i18n (FR/EN), migration des temporaires vers FrReader.kt/FrSettings.kt.
  - **P9-B documentation finale** : README, checklist (statuts finaux vs captures),
    WORK_PLAN (plan bouclé), coherence des journaux, rapport final de portage.
- Lancement des agents en parallèle.

### Agent P9-B — documentation finale (TERMINÉ)

- **Nouveau `PORTING_REPORT.md`** : rapport final de portage (architecture, porté/non porté,
  divergences #1-#28, chiffres clés vérifiés : 363 chaînes i18n, 34/47 sources = 72 %,
  build EXIT=0, livrable ~149 Mo).
- **README** : P0→P9 complet, bilingue FR/EN, ~1385 sources, installation 1.1.1 réécrite
  (un seul .exe, GUIs inclus, per-user, major-upgrade MSI), « Non portées » corrigé.
- **WORK_PLAN.md** : encadré « PLAN TERMINÉ (2026-11-13) » + récapitulatif P0→P9.
- **UI_PARITY_CHECKLIST.md** : statut final + correction « Version 1.1.1 » + preuves
  `desktop-lang-dialog.png` / `desktop-ui-english.png` ; **PORTING_AUDIT.md** : note finale.
- **Incohérences corrigées** : README décrivant encore le flux 1.1 (scripts séparés) ;
  divergence #11 (langue) contredite par la 1.1.1 → historisée ; « Page du manga »
  annonçait un portage P8 resté désactivé → reformulé ; « sauvegardes non portées »
  clarifié (ZIP desktop existe, format Android non).

### Agent P9-A — i18n final (TERMINÉ, compile verte)

- **12 chaînes inline FR traitées** : SearchScreen (recherche vocale, rechercher à nouveau,
  langues diverses branchées aux constantes), SectionsSettingsScreen (Monter/Descendre/Retirer,
  message sections), SettingsScreen (sélecteur de langue bilingue), ListOptionsSheet (Dernier
  lu/Par nom/Groupe → FR_SORT_ORDER etc.).
- **17 fonctions de libellés branchées sur l'i18n** (signatures inchangées) : frNewChapters,
  frSourcesEnabled, frReaderModeLabel, frReaderScaleLabel, frReaderAnimationLabel,
  frReaderBackgroundLabel, frNetworkPolicyLabel, frListModeLabel, frProgressModeLabel,
  frChaptersCount, frChapterXofY (Fr.kt) ; frItemsCount, frSortOrderLabel (18 SortOrder
  traduits) (FrSources.kt) ; frPercentage, frAutoScrollSpeed (FrReader.kt) ; frFileSize
  (o/Ko/Mo/Go → B/KB/MB/GB) (FrSettings.kt). Identiques FR/EN vérifiés et documentés :
  frDuration, frReaderInfo, frBookmarkPage.
- **11 temporaires TODO migrées** vers FrReader.kt (FR_READER_* du lecteur + 4 toasts Main.kt)
  via le mécanisme I18n (public var + EN + ALL_STRING_NAMES + setStringByName) ; marqueurs
  supprimés. Bouton flottant « Continuer la lecture » branché.
- **Infrastructure** : `currentUiLang()` + helper `t(fr, en)` dans I18n.kt ; pattern local par
  écran (rafraîchissement à recomposition, documenté).
- Compile verte. Restes assumés : MaintenanceWindows (mécanisme propre déjà bilingue),
  commentaires/logs, DateTimeAgoFormat EN-only même en FR (problème inverse, hors périmètre).

### Agent P9-A — complément multi-lignes (TERMINÉ, compile verte)

- **Cause racine des résidus FR en EN trouvée** : ~53 déclarations `public var` MULTI-LIGNES
  (valeur sur la ligne suivante) échappaient à l'extraction initiale → 50 nouvelles entrées +
  2 correctifs de complétude (FR_QUICK_FILTERS_SUMMARY et FR_AMOLED_SUMMARY avaient l'entrée
  EN mais ni valeur FR, ni branche setStringByName, ni nom — ne basculaient jamais).
- **I18n.kt audit de cohérence complet** : 426 noms / 426 EN / 426 FR / 426 branches — zéro
  divergence. Répartition : Fr.kt 13, FrSources.kt 7, FrSettings.kt 26, FrReader.kt 1,
  FrAccounts.kt 3 (+ 2 correctifs).
- Fonctions multi-lignes t()-bilingues vérifiées (frSourcesEnabled, frReader*Label,
  frNetworkPolicyLabel, frProgressModeLabel, frChapterXofY, frPercentage, frItemsCount) ;
  frReaderInfo identique FR/EN (documenté).
- Compile verte.

*(les entrées suivantes sont ajoutées au fil de l'eau)*
