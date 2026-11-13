# Kotatsu Desktop

![Licence GPLv3](https://img.shields.io/badge/licence-GPLv3-blue.svg)
![Plateforme](https://img.shields.io/badge/plateforme-Windows%2010%2F11-blue.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-purple.svg)

**Kotatsu Desktop** est un portage Windows **non officiel** de
[Kotatsu-Redo](https://github.com/Kotatsu-Redo/Kotatsu-Redo), lecteur de manga Android.
L'objectif du projet est de réutiliser au maximum le code d'origine (parsers, réseau, base de
données, préférences) et de reconstruire l'interface **à l'identique** en Compose Multiplatform
Desktop (Material 3).

> ⚠️ **Avertissement (repris du projet d'origine)** : Kotatsu est un navigateur de contenus
> tiers. Aucun contenu n'est hébergé par l'application ; elle affiche ce que publient les sites
> de mangas configurés dans ses sources.

## Fonctionnalités

### Portées (P0 → P9, version 1.1.1)
- Liste et exploration des ~1385 sources manga (bibliothèque [kotatsu-parsers-redo](https://github.com/Kotatsu-Redo/kotatsu-parsers-redo) réutilisée telle quelle via JitPack)
- **Interface bilingue FR/EN** : chaînes exactes de l'app Android, langue du système Windows par défaut (repli anglais), sélecteur Langue fonctionnel dans Apparence (Système / English / Français), changement appliqué immédiatement
- Écran Explorer avec sections « Pour vous / Nouveautés pour vous / Plus pour vous » (MangaDex et presets), onglet Source (boutons rapides, suggestions, grille de sources), catalogue des sources
- **Paramètres complets** : écran racine 4 sections, Paramètres du lecteur (Pages/Webtoon/Commandes/Écran/Avancé, images masquées), Apparence (cartes de palette des 9 schémas d'origine, Liste des mangas, Page du manga, Navigation, Langue), **Sources des mangas** (tri, vue grille, Gérer les sources avec recherche + activation/désactivation, Sources catalogue avec filtres et langues, miroir automatique, Gérer les liens), **Suggestions** (activation, inclusion des sources désactivées, exclusion de genres), **Nouveaux chapitres** (fréquence branchée au scheduler, catégories favorites, téléchargement auto, journal), **Vos données** (Téléchargements et stockage, Sauvegarde et restauration ZIP, Data removal), **Réseau** (proxy, DNS sur HTTPS), **Comptes et synchronisation**, **Confidentialité et contenu**, Notifications, À propos, « Principales sections de l'écran » réordonnables
- **Options de liste** (bottom sheet Historique/Favoris : mode Compact/Détails/Grille, taille de grille, tri, groupement) appliquées en direct
- Catalogue par source, recherche multi-sources avec filtres
- Détails d'un manga enrichis (méta Source/Auteur/État/Chapitres + temps estimé, badge 18+, mangas connexes, menu « Lire ▾ » avec Continuer, dialog catégories de favoris, puce ★ sur les couvertures favorites), filtres du catalogue par source (tri + genres)
- Lecteur : mode paginé + mode webtoon, zones de tap, navigation clavier (adaptation desktop) ; **lecteur avancé** : mode de lecture mémorisé par manga (Standard/Droite à gauche/Vertical/Webtoon), pages doubles paysage (bêta), rotation, défilement automatique (vitesse réglable, continu en webtoon), correction des couleurs (filtre réel appliqué aux pages), gomme webtoon avec didacticiel, sauvegarde de page en PNG, marque-page pendant la lecture, chapitres précédent/suivant, raccourci Alt+M
- Favoris, historique et signets persistants (base Room v28 identique à l'app Android)
- Téléchargements hors-ligne + lecture de CBZ locaux
- Notifications de nouveaux chapitres (icône tray + bulles Windows, vérification périodique paramétrable)
- Verrouillage par mot de passe (même hachage que l'app Android)
- Thème clair / sombre / AMOLED, 9 schémas de couleurs d'origine (Miku, Sakura, Totoro…)
- Créateur de préréglages de sources (langues, persistance Room)
- Un seul `.exe` : installateur graphique + application + désinstallateur graphique (JDK embarqué), installation par utilisateur (sans UAC), mises à jour major-upgrade MSI ligne produit 1.x

### Non portées (voir [Limitations](#limitations))
- Suivi AniList / MyAnimeList / Shikimori / Kitsu (scrobbling OAuth), Discord RPC
- Synchronisation multi-appareils
- Sauvegardes au format Android (un format ZIP propre au desktop est disponible, divergence documentée)
- Filtrage NSFW des sources (la clé « contenu pour adultes » est persistée, sans consommateur desktop)
- Verrouillage biométrique (le mot de passe est porté)
- Résolution Cloudflare intégrée (sources protégées)

## Installation

1. Téléchargez `Kotatsu-Desktop-<version>.exe` dans [Releases](../../releases) — un seul
   fichier, l'installateur graphique et l'application sont inclus dedans.
2. Double-cliquez le `.exe` : assistant graphique, JDK embarqué (Java non requis),
   raccourcis Menu Démarrer + Bureau, choix du dossier.
3. L'installation est **par utilisateur** (clé de désinstallation dans HKCU) : aucune
   élévation de privilèges n'est demandée (pas d'UAC).
4. Configuration minimale : Windows 10/11 64 bits.

### Mises à jour

Les versions de la ligne **1.x** se mettent à jour directement les unes sur les autres :
lancez simplement l'installateur d'une version plus récente (major-upgrade MSI sur la ligne
produit `e17ceb03-287c-4d25-ab56-748855fef9d3`, données conservées). Les données
utilisateur vivent dans `%APPDATA%\Kotatsu` et survivent aux mises à jour comme aux
désinstallations.

> Depuis un installateur 9.8.6 (ancienne ligne produit) : la mise à jour directe est
> impossible (9.8.6 > 1.x pour MSI) — désinstallez l'ancienne version (vos données
> `%APPDATA%\Kotatsu` sont conservées) puis installez la 1.x.

### Désinstallation

**Paramètres Windows → Applications → Kotatsu Desktop → Désinstaller** : Windows ouvre le
désinstallateur graphique **inclus dans l'application** (au premier lancement, un lanceur
« Uninstall Kotatsu Desktop » est créé dans le dossier d'installation et la clé de
désinstallation HKCU pointe vers lui). Il demande :
*« Conserver ces données pour une éventuelle réinstallation ? »* — vos données utilisateur
(`%APPDATA%\Kotatsu`) ne sont **jamais** supprimées sans votre accord explicite.

## Compilation depuis les sources

Prérequis : JDK 17+ (le JDK utilisé doit fournir `jpackage`), WiX Toolset 3.14 (installateur `.exe`).

```bash
# Application runnable
./gradlew :desktop:run

# Image applicative (dossier avec Kotatsu.exe + JRE embarqué)
./gradlew :desktop:createReleaseDistributable

# Installateur .exe (nécessite WiX 3 sur le PATH)
./gradlew :desktop:packageReleaseExe

# Installateur .msi (mise à jour major-upgrade ligne produit 1.x)
./gradlew :desktop:packageReleaseMsi
```

Le workflow GitHub Actions `.github/workflows/windows-release.yml` construit et publie
l'installateur sur chaque tag `v*`.

## Architecture

```
core/      Logique métier JVM (portée de l'app Android) :
           modèles, réseau OkHttp (intercepteurs, DoH, cookies), préférences,
           base Room (cible JVM, schéma v28 identique), repositories, cache.
desktop/   Compose Multiplatform Desktop : UI Material 3 reproduisant l'app d'origine,
           navigation, fenêtre principale + lecteur, packaging jpackage.
```

Contribution : lisez `docs/PORTING_REPORT.md` (rapport final de portage),
`docs/PORTING_AUDIT.md` (classification du code d'origine), `docs/WORK_PLAN.md` (lots) et
`docs/UI_PARITY_CHECKLIST.md` (parité UI).

## Licence

Ce projet est un travail dérivé de Kotatsu-Redo : il est distribué sous licence
**GNU GPL v3** — voir [LICENSE](LICENSE) (reprise à l'identique du dépôt d'origine).

Projet d'origine : [Kotatsu](https://github.com/KotatsuApp/Kotatsu) /
[Kotatsu-Redo](https://github.com/Kotatsu-Redo/Kotatsu-Redo) — tous les crédits aux auteurs
d'origine.

## Limitations

Voir la section [Fonctionnalités](#fonctionnalités) → Non portées ci-dessus et
`docs/UI_DIVERGENCES.md` (#1 à #28) pour la liste complète et les justifications. Les sources
protégées par Cloudflare ne sont pas utilisables sur desktop (pas de WebView JVM) : elles sont
marquées comme telles au lieu d'être simulées.
