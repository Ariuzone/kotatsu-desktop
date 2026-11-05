# Divergences UI / comportement Windows ↔ Android

Toute divergence nécessaire à l'adaptation desktop est listée ici avec sa justification.
Principe : conserver l'apparence et la logique de l'app Android ; n'ajouter que les adaptations
indispensables (souris, clavier, fenêtre, tray).

| # | Divergence | Justification | Statut |
|---|---|---|---|
| 1 | Barre de titre Windows native (au lieu de la status bar Android) | contrainte OS ; les couleurs de la fenêtre suivent le thème | prévu |
| 2 | Pas de pull-to-refresh dans les listes : actualisation par bouton/F5 | interaction souris ; l'app desktop reste fidèle sinon | prévu |
| 3 | Résolution Cloudflare par WebView indisponible → sources protégées marquées « indisponible sur desktop » | pas de WebView JVM ; navigateur externe possible plus tard (P3+) | documenté |
| 4 | Widgets d'écran d'accueil Android (shelf/recent) non portés | concept inexistant sur Windows | documenté |
| 5 | Les gestes du lecteur (swipe, double-tap) sont doublés par la souris (clic molette, molette) et le clavier (flèches) sans changer l'apparence | adaptation desktop indispensable (exigence §4 du cahier des charges) | prévu |
