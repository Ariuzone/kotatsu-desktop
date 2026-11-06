# Références UI consultées

Source de vérité n°1 = code du dépôt d'origine (layouts, thèmes, styles) — voir `PORTING_AUDIT.md`.
Source de vérité n°2 = captures du dépôt `metadata/en-US/images/`.
Ce fichier liste les références externes consultées en cas de doute, avec la décision prise.

| Date | URL | Écran concerné | Décision |
|---|---|---|---|
| 2026-11-05 | https://github.com/Kotatsu-Redo/Kotatsu-Redo (code source) | tous | disposition, couleurs, dimensions issues du code (res/values/*, res/layout*) |
| 2026-11-05 | https://m3.material.io/components/navigation-rail/overview | shell principal (rail latéral) | comportement Material 3 standard, libellés = ceux de l'app d'origine (res/values/strings.xml) |
| 2026-11-05 | https://m3.material.io/styles/color/system/overview | thème clair/sombre/AMOLED | palette M3 reprise tel quel de `colors_kotatsu.xml` + `values-night` |

## Captures réelles fournies par le client (référence canonique — 2026-11-06)

12 captures de l'app Android réelle (thème sombre, **français**) : `ref-screenshots/` (hors dépôt).
Constats majeurs qui ont déclenché la refonte (lot agent/ui-fr) :
1. Interface **entièrement en français** (chaînes exactes, cf. values-fr).
2. Écran Explorer = **Discover** : chips « Pour vous / Top / Mangas / Manhwa » + sections
   « Pour vous / Nouveautés pour vous / Plus pour vous » (rangées horizontales de couvertures,
   liens « Tout afficher ») — PAS une liste de sources.
3. Onglet **Source** : boutons rapides (Stockage local, Marque-pages, Aléatoire, Téléchargements,
   Préréglages de sources), section Suggestions, grille « Sources des mangas » (favicons + 🔥).
4. **Sources catalogue** : liste (favicon, titre, « Mangas, <langue> », bouton +), chips langues.
5. Barre de recherche universelle « Rechercher un manga » + menus ⋮ par écran (Options de liste /
   Mode incognito / Paramètres ; Mettre à Jour / Réafficher ce que j'ai écarté sur Explorer).
6. États vides avec **illustrations Totoro** vectorielles d'origine.
7. Recherche : suggestions horizontales de mangas, historique de requêtes, suggestions de sources.
