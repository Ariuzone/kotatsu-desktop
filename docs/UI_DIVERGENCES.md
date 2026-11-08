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
| 9 | Écran Téléchargements accessible par une icône du rail (l'app Android l'ouvre depuis les menus des listes) | pas de drawer sur desktop ; accès direct équivalent | implémenté |
| 7 | Notifications de nouveaux chapitres via **icône tray Windows + bulles** (au lieu des canaux de notification Android) | adaptation OS ; même fréquence de vérification que TrackWorker | implémenté |
| 8 | Écran de verrouillage en plein écran à l'ouverture de la fenêtre (au lieu d'une activité Android) | équivalent direct de ProtectActivity ; même hachage MD5 et mêmes chaînes | implémenté |
| 6 | Recherche accessible par **Ctrl+F** (au lieu du bouton des barres d'outils Android) ; paramètres en bas du rail (au lieu du menu du drawer) | adaptation desktop indispensable : pas de drawer ni de barre d'outils tactile | implémenté |
| 5 | Les gestes du lecteur (swipe, double-tap) sont doublés par la souris (clic molette, molette) et le clavier (flèches) sans changer l'apparence | adaptation desktop indispensable (exigence §4 du cahier des charges) | prévu |
| 10 | (2026-11-08) « Confirmation de sortie » : sous-titre desktop « Demander confirmation avant de fermer la fenêtre (desktop) » au lieu de « Appuyez deux fois sur la touche Retour pour quitter l'appli » | pas de touche Retour système sur desktop ; le dialog « Quitter Kotatsu ? » de fermeture de fenêtre remplace le double-appui | implémenté |
| 11 | (2026-11-08) « Langue » : entrée désactivée, sous-titre « Suivre le système (français seul sur desktop) » | l'app desktop est livrée en français seul ; pas de sélecteur de langue | implémenté |
| 12 | (2026-11-08) « Épingler l'interface de navigation » : entrée désactivée | rien ne se masque au défilement sur desktop (pas de barre tactile à masquer) | implémenté |
| 13 | (2026-11-08) « Afficher les raccourcis des mangas récents » : entrée désactivée, sous-titre « Geste long-press de l'icône Android, sans équivalent sur desktop » | pas de long-press d'icône de raccourci sur Windows | implémenté |
| 14 | (2026-11-08) « Principales sections de l'écran » : réordonnancement par boutons flèche ↑/↓ au lieu de la grille de glissement Android ; dialog « Ajouter » avec « Suggestions » et « Mis à jour » affichées mais désactivées | pas de geste de glissement à la souris ; les écrans Suggestions/Mis à jour sont portés en P4 | implémenté |
| 15 | (2026-11-08) « Gérer les liens » : écran lecture seule (textes exacts + note desktop) au lieu de la gestion d'intents Android | pas de deep-links d'applications externes sur Windows ; ouverture directe d'un lien dans l'app | implémenté |
| 16 | (2026-11-08) Menu de langues du catalogue : « Langues diverses » en fin de liste (la vidéo le montre au milieu) ; langues natives triées alphabétiquement | ordre simplifié et déterministe à partir des locales du registry | implémenté |
| 17 | (2026-11-08) « Choisir le miroir automatiquement » : switch persisté (`auto_mirror`) mais backend de miroirs non branché | `ParserMangaRepository.getAvailableMirrors()` non exposé côté desktop ; UI prête pour le branchement | prévu |
| 18 | (2026-11-08) « Désactiver l'optimisation de la batterie » : entrée affichée mais désactivée (grisée) | optimisation batterie Android sans équivalent Windows ; l'app n'a pas d'arrière-plan restreint | implémenté |
| 19 | (2026-11-08) Fréquences de vérification : « Par défaut / Toutes les heures / Toutes les 6 heures / Quotidienne » (l'Android n'expose pas ses options dans la vidéo) ; journal de vérification avec état vide tant qu'aucun backend de log | valeurs desktop choisies et branchées réellement au scheduler (intervalMillis) | implémenté |
| 20 | (2026-11-08) Sauvegarde/restauration : format ZIP desktop (kotatsu.db + WAL/SHM + settings.json) au lieu du format de sauvegarde Android ; restauration = extraction puis relance manuelle de l'app | backend de sauvegarde Android non porté ; format simple et vérifiable (kotatsu.db obligatoire, protection zip-slip) | implémenté |
| 21 | (2026-11-08) Dossier de téléchargements : sélection via JFileChooser non appliquée (message « déplacement manuel ») ; chemins Windows réels affichés | le stockage local est reconstruit au démarrage depuis %APPDATA%\Kotatsu ; pas de reconfiguration à chaud | implémenté |
| 22 | (2026-11-08) Réseau : note « Ces réglages s'appliquent après redémarrage de l'application » (proxy/DoH reconstruits au démarrage) ; « Notifier les nouvelles suggestions » désactivé (aucune clé) | NetworkModule reconstruit au lancement | implémenté |
