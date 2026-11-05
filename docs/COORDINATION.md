# Coordination des agents

## État des lots

| Lot | Agent | Statut | Branche | Dernier commit | Blocages | Prochaine action |
|---|---|---|---|---|---|---|
| Squelette build | principal | **fini** | main | « Squelette Gradle compilable » | — | — |
| L1 contrats | principal | en cours | main | — | — | modèles + prefs store |
| L2 core-données | sous-agent A | à faire | agent/core-data | — | — | — |
| L3 core-réseau/parser | sous-agent B | à faire | agent/core-net | — | — | — |
| L4a ui-shell | principal | à faire | main | — | — | — |
| L4b ui-écrans | sous-agent C | à faire | agent/ui-screens | — | — | — |
| L5 packaging/CI | sous-agent D | à faire | agent/packaging | — | — | — |
| L6 docs | principal | en cours | main | — | — | PORTING_AUDIT/WORK_PLAN écrits |

## Requêtes (destinataire : agent)

*(vide)*

## Notes d'environnement

- Limite : **1 sous-agent simultané** (« user concurrency limit exceeded » au-delà) → lots L2/L3/L4b/L5 séquencés.
- JDK : Android Studio JBR 25 (`C:/Program Files/Android/Android Studio/jbr`), `JAVA_HOME` à exporter pour chaque commande Gradle.
- Versions épinglées : Kotlin 2.2.10, CMP 1.9.0, Room 2.8.4 JVM, OkHttp 5.3.2, Coil 3.4.0, parsers JitPack 68f3fed395.
- `room-ktx` et `coil-gif` n'ont pas de variante JVM → retirés.
