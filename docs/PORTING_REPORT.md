# Rapport final de portage — Kotatsu-Redo (Android) → Windows Desktop

Date : 2026-11-13. Version livrée : **1.1.1** (`Kotatsu-Desktop-1.1.1.exe`).
Plan exécuté intégralement (P0 → P9) : voir `WORK_PLAN.md` (récapitulatif), journaux
`P*_JOURNAL.md` par phase, audit initial dans `PORTING_AUDIT.md`.

## 1. Architecture

| Module | Contenu |
|---|---|
| `:core` | Kotlin/JVM : modèles, préférences (`AppSettings` porté sur store clé-valeur JSON), réseau OkHttp (intercepteurs, DoH, cookies fichier), base **Room v28 cible JVM** (entités/DAO/28 migrations SQL identiques à l'app Android, `sqlite-bundled-jvm`), repositories, téléchargements, tracker (coroutines), injection manuelle (remplace Hilt) |
| `:desktop` | Compose Multiplatform Desktop (Material 3) : UI reproduisant l'app d'origine, navigation, fenêtre principale + lecteur, fenêtres de maintenance (installateur/désinstallateur), packaging jpackage (WiX 3) |
| parsers | **Bibliothèque d'origine réutilisée telle quelle** : artefact JitPack `com.github.clquwu:kotatsu-parsers-redo:68f3fed395`, identique à l'app Android (aucune dépendance Android dedans) |

Versions épinglées : Kotlin 2.2.10, Compose Multiplatform 1.9.0, Room 2.8.4 JVM, OkHttp 5.3.2,
Coil 3.4.0. JDK d'exécution embarqué dans l'app packagée (jpackage).

## 2. Ce qui est porté

- **UI complète** en Material 3 : rail de navigation, Explorer (sections « Pour vous /
  Nouveautés pour vous / Plus pour vous », onglet Source, catalogue), recherche multi-sources
  avec filtres, détails du manga enrichis, favoris (catégories, dialog, puce ★), historique
  (groupement par date), signets, téléchargements, verrouillage par mot de passe, thème
  clair/sombre/AMOLED avec les 9 schémas d'accent d'origine.
- **Paramètres complets** : racine 4 sections, Lecteur, Apparence (langue incluse), Sources
  des mangas (gestion, catalogue, préréglages, liens), Suggestions, Nouveaux chapitres,
  Vos données (stockage, sauvegarde/restauration ZIP, data removal), Notifications, Réseau
  (proxy, DoH), Comptes et synchronisation, Confidentialité et contenu, À propos.
- **Lecteur avancé** : mode de lecture mémorisé par manga (4 modes), pages doubles paysage
  (bêta), rotation, défilement automatique, correction des couleurs (filtre réel),
  gomme webtoon avec didacticiel, sauvegarde de page PNG, marque-page, chapitres
  précédent/suivant, raccourci Alt+M.
- **Données** : favoris, historique, signets et préférences persistés dans la même base
  Room v28 que l'app Android ; téléchargements hors-ligne ; lecture de CBZ locaux.
- **Arrière-plan** : vérification des nouveaux chapitres branchée au scheduler (fréquence
  paramétrable, catégories favorites, téléchargement auto), notifications via icône tray +
  bulles Windows.
- **i18n FR/EN** : 363 chaînes traduites, langue du système Windows par défaut (repli EN),
  sélecteur fonctionnel dans Apparence, appliqué immédiatement (UI et fenêtres de maintenance).
- **Installation** : un seul `.exe` (installateur graphique + application + désinstallateur
  graphique, JDK embarqué) ; installation par utilisateur (`perUserInstall`, clé HKCU,
  pas d'UAC) ; désinstallation depuis Paramètres Windows ouvrant le GUI (choix de conserver
  ou supprimer `%APPDATA%\Kotatsu`) ; mises à jour par major-upgrade MSI sur la ligne produit
  1.x (upgradeUuid `e17ceb03-287c-4d25-ab56-748855fef9d3`).

## 3. Ce qui n'est pas porté (documenté, non simulé)

- Suivi AniList / MyAnimeList / Shikimori / Kitsu (scrobbling OAuth — pas de WebView desktop ;
  les écrans Comptes reproduisent l'état « Désactivé »).
- Discord RPC (riche présence).
- Synchronisation multi-appareils.
- Résolution Cloudflare intégrée (pas de WebView JVM) — sources protégées marquées comme
  telles.
- Filtrage NSFW des sources (clé « contenu pour adultes » persistée, sans consommateur
  desktop).
- Sauvegardes au format Android (un format ZIP desktop est disponible à la place,
  divergence #20).
- Widgets d'écran d'accueil, Sentry/ACRA, plugin system externe, deep-links (hérités des
  contraintes OS, cf. `PORTING_AUDIT.md` §6).

## 4. Divergences d'adaptation

28 divergences documentées (#1 à #28) avec justification et statut dans
[`UI_DIVERGENCES.md`](UI_DIVERGENCES.md) : titre de fenêtre Windows, actualisation par
bouton/F5 (pas de pull-to-refresh), contrôles souris/clavier du lecteur, entrées désactivées
sans équivalent desktop (langue d'origine, épingler, raccourcis, batterie), écrans adaptés
(liens, sauvegarde ZIP, réseau, export de page), et champs du modèle non exposés (temps de
lecture estimé, dates de tri). Le détail écran par écran est dans
[`UI_PARITY_CHECKLIST.md`](UI_PARITY_CHECKLIST.md) (statut + capture pour chaque écran).

## 5. Chiffres clés

| Indicateur | Valeur |
|---|---|
| Chaînes i18n traduites (FR/EN) | 363 |
| Divergences d'adaptation documentées | 28 |
| Sources manga disponibles | ~1385 (bibliothèque d'origine) |
| Sources vérifiées réellement fonctionnelles | 34/47 (72 %) — sonde réseau, le reste étant surtout des domaines morts |
| Migrations Room portées | 28 (schéma v28 identique) |
| Build final | `./gradlew :core:build :desktop:build` EXIT=0 |
| Livrable | `Kotatsu-Desktop-1.1.1.exe` (~149 Mo, JDK embarqué) |
| Données utilisateur | `%APPDATA%\Kotatsu` (base Room v28, settings.json, cookies, téléchargements) |
