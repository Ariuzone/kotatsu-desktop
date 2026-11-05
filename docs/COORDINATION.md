# Coordination des agents

## État des lots

| Lot | Agent | Statut | Branche | Dernier commit | Blocages | Prochaine action |
|---|---|---|---|---|---|---|
| Squelette build | principal | **fini** | main | « Squelette Gradle compilable » | — | — |
| L1 contrats | principal | intégré au squelette | main | — | — | — |
| L2 core-données (Room JVM) | sous-agent B | en cours | agent/core-data (worktree kotatsu-desktop-core-data) | — | — | — |
| L3 core-réseau/parser/prefs | sous-agent A | en cours (5 commits : prefs, network, parser, github) | agent/core-net (worktree kotatsu-desktop-core-net) | 019d359 | — | fin de lot : test réseau |
| L4a ui-shell + thème | principal | **fini** | main | 266cf2b | — | — |
| L4b ui-écrans | principal | en cours | main | 24bec18+ | — | Explore/Détails/Lecteur/Recherche/Paramètres écrits ; données réelles à brancher après fusion L2/L3 |
| L5 packaging/CI | principal | **fini** (app-image vérifié + .exe généré ; install local bloqué par UAC → CI) | main | 1a2e796 | UAC pour install silencieuse locale | — |
| L6 docs | principal | en cours | main | — | — | README initial écrit |

## Requêtes (destinataire : agent)

*(vide)*

## Notes d'environnement

- Limite : **2 sous-agents simultanés** (passée de 1 à 2 en cours de projet) (« user concurrency limit exceeded » au-delà) → lots L2/L3/L4b/L5 séquencés.
- JDK : Android Studio JBR 25 (`C:/Program Files/Android/Android Studio/jbr`), `JAVA_HOME` à exporter pour chaque commande Gradle.
- Versions épinglées : Kotlin 2.2.10, CMP 1.9.0, Room 2.8.4 JVM, OkHttp 5.3.2, Coil 3.4.0, parsers JitPack 68f3fed395.
- `room-ktx` et `coil-gif` n'ont pas de variante JVM → retirés.
