# Coordination des agents

## État des lots

| Lot | Agent | Statut | Branche | Dernier commit | Blocages | Prochaine action |
|---|---|---|---|---|---|---|
| L2 core-données (Room JVM) | sous-agent B | **fini, fusionné** — 97 fichiers, 17 entités, 16 DAO, 28 migrations, schéma v28 exporté, 3/3 tests | agent/core-data | fusion | — | — |
| L3 core-réseau/parser/prefs | sous-agent A | **fini, fusionné** — test réseau réel PASSED (DYNASTYSCANS) | agent/core-net | d1f2e47 | — | — |
| L4a ui-shell + thème | principal | **fini** | main | 266cf2b | — | — |
| L4b ui-écrans (Explore/Catalogue/Détails/Lecteur/Recherche/Paramètres) | principal | **fini** — E2E réseau réel PASSED (catalogue>détails>chapitres>pages>URL après fix org.json lenient) | main | 04819e8 | — | — |
| L4c écrans History/Favorites/Downloads | sous-agent C | **fini, fusionné** + câblé à la DB réelle | agent/ui-screens | fusion 04819e8 | — | écran Downloads à brancher au lot P2 |
| P2 téléchargements + CBZ local | sous-agent D puis principal (quota épuisé, travail repris) | **fini, fusionné** — 12/12 tests core verts, câblé (rail + enqueue depuis Détails + dépôt local dans la fabrique) | agent/downloads | fusion dc0faf7 | — | — |
| P3 tracker + verrouillage | sous-agent E | **fini, fusionné** — 14/14 tests verts, câblé (Tray + ProtectScreen) | agent/tracker | fusion | — | — |
| L5 packaging/CI | principal | **fini** — installateur final Kotatsu-9.8.6.exe (94 Mo, JDK embarqué, WiX 3 portable) ; fix ProGuard keep Room/sqlite vérifié par lancement de l'app packagée (DB v28 créée) ; install locale bloquée par UAC → CI | main | — | — | — |
| Signets (UI) | principal | **fini, branché** | main | — | — | — |
| L6 docs | principal | en cours | main | — | — | rapport final |

## Requêtes (destinataire : agent)

*(vide)*

## Notes d'environnement

- Limite : **2 sous-agents simultanés** (passée de 1 à 2 en cours de projet) (« user concurrency limit exceeded » au-delà) → lots L2/L3/L4b/L5 séquencés.
- JDK : Android Studio JBR 25 (`C:/Program Files/Android/Android Studio/jbr`), `JAVA_HOME` à exporter pour chaque commande Gradle.
- Versions épinglées : Kotlin 2.2.10, CMP 1.9.0, Room 2.8.4 JVM, OkHttp 5.3.2, Coil 3.4.0, parsers JitPack 68f3fed395.
- `room-ktx` et `coil-gif` n'ont pas de variante JVM → retirés.

## Refonte fidélité (2026-11-06, après retours client)

Captures réelles fournies par le client → 2 agents relancés en continu :
- **agent/ui-fr** : refonte complète des écrans (français exact, Totoro vectoriel, Discover
  à sections, onglet Source, catalogue, recherche, Flux, LOCAL en cours) — lot 1+2 fusionnés
  et intégrés dans Main.kt (rail 5 onglets FR). Backlog de fiabilité en cours.
- **agent/net-rel** : sonde 36 sources — baseline 23/36 puis 25/36 (MANGADEX fixé via headers) ;
  RetryInterceptor + DeadDomainException commités ; en cours : ordres de tri explicites
  (MangaDex POPULARITY/UPDATED renvoient du HTML — cas transmis par l'orchestrateur) + SSL.
- Orchestrateur : rail 5 onglets, mode incognito, ImageLoader Coil (Referer par source),
  onglet de démarrage pour tests non intrusifs, restauration des routes DB après refonte.

## Fusion net-rel + Discover alimenté (2026-11-06 soir)

- **Cause racine du « sources ne fonctionnent pas »** (agent net-rel) : le client desktop
  n'envoyait jamais l'UA par parser (`Kotatsu/6.8…`) — `repositoryFactoryProvider` n'était pas
  câblé dans NetworkModule, donc CommonHeadersInterceptor tombait sur un UA navigateur,
  et les API (MangaDex…) répondaient par des challenges HTML. Fix : `globalInstance` +
  `parserFor(source)`, en-têtes résolus par source comme sur Android.
- Taux de succès : 63 % → **72 %** (34/47) ; RetryInterceptor ; DeadDomainException
  (domaines morts masquables) ; certificats Let's Encrypt YE embarqués (MANGAPARK OK).
- Discover vérifié visuellement : sections « Pour vous / Nouveautés pour vous » alimentées
  en titres réels (docs/ui-parity/desktop-discover-data.png). Couvertures : diagnostic en cours
  (agent net-rel).
- Écran « On device » branché (CBZ locaux via LocalMangaRepository).
