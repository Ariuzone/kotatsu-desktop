# Coordination des agents

## État des lots

| Lot | Agent | Statut | Branche | Dernier commit | Blocages | Prochaine action |
|---|---|---|---|---|---|---|
| L2 core-données (Room JVM) | sous-agent B | **fini, fusionné** — 97 fichiers, 17 entités, 16 DAO, 28 migrations, schéma v28 exporté, 3/3 tests | agent/core-data | fusion | — | — |
| L3 core-réseau/parser/prefs | sous-agent A | **fini, fusionné** — test réseau réel PASSED (DYNASTYSCANS) | agent/core-net | d1f2e47 | — | — |
| L4a ui-shell + thème | principal | **fini** | main | 266cf2b | — | — |
| L4b ui-écrans (Explore/Catalogue/Détails/Lecteur/Recherche/Paramètres) | principal | **fini** — E2E réseau réel PASSED (catalogue>détails>chapitres>pages>URL après fix org.json lenient) | main | 04819e8 | — | — |
| L4c écrans History/Favorites/Downloads | sous-agent C | **fini, fusionné** + câblé à la DB réelle | agent/ui-screens | fusion 04819e8 | — | écran Downloads à brancher au lot P2 |
| P2 téléchargements + CBZ local | sous-agent D | en cours | agent/downloads (worktree) | — | — | rapport puis fusion |
| P3 tracker + verrouillage | sous-agent E | **fini, fusionné** — 14/14 tests verts, câblé (Tray + ProtectScreen) | agent/tracker | fusion | — | — |
| L5 packaging/CI | principal | **fini** (app-image + Kotatsu-9.8.6.exe ; install locale bloquée par UAC → CI) | main | 1a2e796 | — | rebuild final après fusions |
| Signets (UI) | principal | **fini** | main | — | — | branché sur BookmarksDao |
| L6 docs | principal | en cours | main | — | — | rapport final |

## Requêtes (destinataire : agent)

*(vide)*

## Notes d'environnement

- Limite : **2 sous-agents simultanés** (passée de 1 à 2 en cours de projet) (« user concurrency limit exceeded » au-delà) → lots L2/L3/L4b/L5 séquencés.
- JDK : Android Studio JBR 25 (`C:/Program Files/Android/Android Studio/jbr`), `JAVA_HOME` à exporter pour chaque commande Gradle.
- Versions épinglées : Kotlin 2.2.10, CMP 1.9.0, Room 2.8.4 JVM, OkHttp 5.3.2, Coil 3.4.0, parsers JitPack 68f3fed395.
- `room-ktx` et `coil-gif` n'ont pas de variante JVM → retirés.
