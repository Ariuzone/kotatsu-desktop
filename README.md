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

### Portées (P1/P2)
- Liste et exploration des 1200+ sources manga (bibliothèque [kotatsu-parsers-redo](https://github.com/Kotatsu-Redo/kotatsu-parsers-redo) réutilisée telle quelle via JitPack)
- Catalogue par source, recherche multi-sources avec filtres
- Détails d'un manga (couverture, auteur, tags, description, chapitres)
- Lecteur : mode paginé + mode webtoon, zones de tap, navigation clavier (adaptation desktop)
- Favoris, historique persistants (base Room v28 identique à l'app Android)
- Thème clair / sombre / AMOLED, 9 schémas de couleurs d'origine (Miku, Sakura, Totoro…)
- Installateur Windows `.exe` (JDK embarqué, raccourcis, désinstallation propre)

### Non portées (voir [Limitations](#limitations))
- Suivi AniList / MyAnimeList / Shikimori / Kitsu, Discord RPC (P3)
- Synchronisation, notifications de nouveaux chapitres (P3)
- Verrouillage biométrique (P3 — mot de passe visé)
- Résolution Cloudflare intégrée (sources protégées)

## Installation

1. Téléchargez `Kotatsu-<version>.exe` dans les [Releases](../../releases).
2. Lancez l'installateur : raccourcis Menu Démarrer + Bureau, choix du dossier, JDK embarqué
   (aucun Java à installer).
3. Configuration minimale : Windows 10/11 64 bits.

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
