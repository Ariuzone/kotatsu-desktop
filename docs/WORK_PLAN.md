# Plan de travail (lots)

Découpage en lots avec propriétaire exclusif des fichiers, dépendances et critères de fin.
Mode d'exécution : sous-agents en parallèle quand l'environnement le permet (limite de concurrence
observée : 1 sous-agent simultané → lots séquencés, l'agent principal travaille en parallèle sur
l'intégration). Squelette : `:core` (Kotlin/JVM) + `:desktop` (Compose M3), build validé.

## Graphe de dépendances

```
L1 contrats (modèles, prefs, DI, thème) ──> L2 core-données ──> L4 ui-écrans ──> L6 packaging
                                        └──> L3 core-réseau/parser ─┘
L5 docs/CI (indépendant, dès L1)
```

## Lots

| Lot | Propriétaire | Périmètre exclusif | Dépend de | Critères de fin |
|---|---|---|---|---|
| **L1 contrats** | principal | `core/src/main/kotlin/org/koitharu/kotatsu/core/model/`, `core/prefs/*` (store + AppSettings), `settings.gradle.kts`, `libs.versions.toml` | — | compile ; modèles = copies fidèles de l'app |
| **L2 core-données** | sous-agent A | `core/**/db/**`, `favourites/data/**`, `history/data/**`, `bookmarks/data/**` (Room JVM) | L1 | `./gradlew :core:build` OK ; DAO compilés ; schéma exporté |
| **L3 core-réseau/parser** | sous-agent B | `core/network/**`, `core/parser/**`, `core/cache/**`, `core/util/**` | L1 | build OK ; `MangaLoaderContext` impl complet ; fetch d'une liste depuis une source testée en test unitaire |
| **L4a ui-shell** | sous-agent C | `desktop/**/ui/theme/**`, `Main.kt`, navigation, fenêtre | L1 | fenêtre se lance, rail latéral, thème clair/sombre = palette d'origine |
| **L4b ui-écrans** | sous-agent C (suite) | écrans Explore/Recherche/Détails/Lecteur/Favoris/Historique | L2+L3+L4a | chaque écran compile + parité avec capture de référence |
| **L5 packaging/CI** | sous-agent D | `.github/workflows/windows-release.yml`, icône `.ico`, config jpackage | — | workflow committed ; `.exe` si Windows disponible |
| **L6 docs** | principal | `README.md`, `docs/*` | tous | complet, honnête |

Fichiers partagés (modifications uniquement par l'agent principal) : `settings.gradle.kts`,
`gradle/libs.versions.toml`, `gradle.properties`, `README.md`, `LICENSE`.

## Ordre d'exécution (contraint par la limite de concurrence = 1 sous-agent à la fois)

1. **L1** (principal, fait en même temps que le squelette).
2. **L3** en sous-agent (chemin critique : données réelles) pendant que le principal porte L2.
3. **L2** en sous-agent pendant que le principal écrit L4a (thème + shell).
4. **L4b** écrans, par paliers P1 (Explorateur → Recherche → Détails → Lecteur → Favoris → Historique).
5. P2 (téléchargements/CBZ/signets/paramètres), puis P3 (suivi/sync/notifications/verrouillage).
6. **L5** packaging (jpackage local si possible, sinon CI) + **L6** docs finales.

## Backlog de secours (si un agent finit tôt / est bloqué)

- Parité UI d'un écran déjà implémenté vs captures du dépôt.
- Tests unitaires : builders SQL, intercepteurs réseau, mappings entités.
- Traduction des chaînes manquantes (fr) depuis `res/values-fr`.
- Documentation `docs/UI_REFERENCES.md`, `docs/UI_DIVERGENCES.md`.

## Règles Git

- Branche par lot : `agent/<lot>` ; commits atomiques ; le principal fusionne dans `main` et fait
  compiler l'ensemble après chaque fusion (`./gradlew build`).
- Toute demande de modification de fichier partagé passe par `docs/COORDINATION.md` (section Requêtes).
