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

### Portées (P1 → P4)
- Liste et exploration des 1300+ sources manga (bibliothèque [kotatsu-parsers-redo](https://github.com/Kotatsu-Redo/kotatsu-parsers-redo) réutilisée telle quelle via JitPack)
- **Interface en français** (chaînes exactes de l'app Android, illustrations d'états vides d'origine)
- Écran Explorer avec sections « Pour vous / Nouveautés pour vous / Plus pour vous » (MangaDex et presets), onglet Source (boutons rapides, suggestions, grille de sources), catalogue des sources
- **Paramètres complets** : écran racine 4 sections, Paramètres du lecteur (Pages/Webtoon/Commandes/Écran/Avancé, images masquées), Apparence (cartes de palette des 9 schémas d'origine, Liste des mangas, Page du manga, Navigation), **Sources des mangas** (tri, vue grille, Gérer les sources avec recherche + activation/désactivation, Sources catalogue avec filtres et langues, miroir automatique, Gérer les liens), **Suggestions** (activation, inclusion des sources désactivées, exclusion de genres), **Nouveaux chapitres** (fréquence branchée au scheduler, catégories favorites, téléchargement auto, journal), « Principales sections de l'écran » réordonnables
- **Options de liste** (bottom sheet Historique/Favoris : mode Compact/Détails/Grille, taille de grille, tri, groupement) appliquées en direct
- Catalogue par source, recherche multi-sources avec filtres
- Détails d'un manga (couverture, auteur, tags, description, chapitres)
- Lecteur : mode paginé + mode webtoon, zones de tap, navigation clavier (adaptation desktop)
- Favoris, historique et signets persistants (base Room v28 identique à l'app Android)
- Téléchargements hors-ligne + lecture de CBZ locaux
- Notifications de nouveaux chapitres (icône tray + bulles Windows, vérification périodique paramétrable)
- Verrouillage par mot de passe (même hachage que l'app Android)
- Thème clair / sombre / AMOLED, 9 schémas de couleurs d'origine (Miku, Sakura, Totoro…)
- Créateur de préréglages de sources (langues, persistance Room)
- Installateur Windows `.exe` (JDK embarqué, raccourcis, désinstallation propre)

### Non portées (voir [Limitations](#limitations))
- Suivi AniList / MyAnimeList / Shikimori / Kitsu (scrobbling), Discord RPC
- Synchronisation multi-appareils, sauvegardes/restauration
- Verrouillage biométrique (le mot de passe est porté)
- Résolution Cloudflare intégrée (sources protégées)

## Installation

1. Téléchargez les fichiers de la release dans [Releases](../../releases).
2. **Simple** : lancez `Kotatsu-Desktop-<version>.exe` (assistant graphique, JDK embarqué,
   raccourcis Menu Démarrer + Bureau, choix du dossier).
3. **Installateur personnalisé** (recommandé pour gérer mises à jour et migrations) :
   placez `Kotatsu-Desktop-<version>.msi` à côté de `Installateur-Kotatsu-Desktop.cmd` +
   `installateur-kotatsu.ps1` puis double-cliquez le .cmd. Il gère :
   - **installation neuve** (assistant graphique) ;
   - **mise à jour** d'une 1.x existante (major-upgrade MSI, données conservées) ;
   - **migration depuis l'ancienne ligne produit 9.8.6** (proposition de désinstallation
     propre, données conservées) avant installation.
   - Variante silencieuse : `powershell -File installateur-kotatsu.ps1 -Silent`.
4. Configuration minimale : Windows 10/11 64 bits.

### Mises à jour

Les versions de la ligne **1.x** se mettent à jour directement les unes sur les autres :
lancez simplement l'installateur d'une version plus récente (major-upgrade MSI, données
conservées). Les données utilisateur vivent dans `%APPDATA%\Kotatsu` et survivent aux
mises à jour comme aux désinstallations.

> Depuis un installateur 9.8.6 (ancienne ligne produit) : la mise à jour directe est
> impossible (9.8.6 > 1.x pour MSI) — utilisez l'installateur personnalisé ci-dessus, qui
> propose la désinstallation propre de l'ancienne version en conservant vos données.

### Désinstallation

Deux façons, vos données utilisateur (`%APPDATA%\Kotatsu`) ne sont **jamais** supprimées
sans votre accord :

- **Applications & fonctionnalités** (Windows) : désinstallation standard, données conservées.
- **Désinstallateur personnalisé** (choix explicite) : `Desinstaller-Kotatsu-Desktop.cmd` +
  `desinstaller-kotatsu.ps1` (fournis dans les releases, ou présents dans le dossier
  d'installation `app\resources\uninstall\` si injectés) — il ferme l'app, désinstalle le
  produit puis demande : *« Conserver ces données pour une éventuelle réinstallation ?
  [O/n] »*. Répondez `n` pour tout supprimer.

## Compilation depuis les sources

Prérequis : JDK 17+ (le JDK utilisé doit fournir `jpackage`), WiX Toolset 3.14 (installateur `.exe`).

```bash
# Application runnable
./gradlew :desktop:run

# Image applicative (dossier avec Kotatsu.exe + JRE embarqué)
./gradlew :desktop:createReleaseDistributable

# Installateur .exe (nécessite WiX 3 sur le PATH)
./gradlew :desktop:packageReleaseExe
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

Contribution : lisez `docs/PORTING_AUDIT.md` (classification du code d'origine),
`docs/WORK_PLAN.md` (lots) et `docs/UI_PARITY_CHECKLIST.md` (parité UI).

## Licence

Ce projet est un travail dérivé de Kotatsu-Redo : il est distribué sous licence
**GNU GPL v3** — voir [LICENSE](LICENSE) (reprise à l'identique du dépôt d'origine).

Projet d'origine : [Kotatsu](https://github.com/KotatsuApp/Kotatsu) /
[Kotatsu-Redo](https://github.com/Kotatsu-Redo/Kotatsu-Redo) — tous les crédits aux auteurs
d'origine.

## Limitations

Voir `README` section ci-dessus et `docs/UI_DIVERGENCES.md` pour la liste complète et les
justifications. Les sources protégées par Cloudflare ne sont pas utilisables sur desktop
(pas de WebView JVM) : elles sont marquées comme telles au lieu d'être simulées.
