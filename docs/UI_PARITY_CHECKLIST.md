# Checklist de parité UI

Pour chaque écran : statut (identique / écart mineur / écart majeur / à faire), preuve (capture
comparée rangée dans `docs/ui-parity/`), référence utilisée.

| Écran | Référence (capture/fichier d'origine) | Statut | Preuve |
|---|---|---|---|
| Shell principal (rail de navigation) | `activity_main.xml` + tenInchScreenshots/1.png | **écart mineur** : structure identique (rail icônes+libellés à gauche, volet contenu) ; libellés exacts (« Explore », « History », « Favorites »…) ; à faire : barre de recherche en haut du volet | `ui-parity/desktop-explore.png` |
| Explorateur (sources par langue) | `fragment_explore.xml`, `item_explore_source_grid.xml` | **écart mineur** : grille de cartes sources groupées par langue conforme ; favicons réels chargés depuis les sites (fallback icône quand le site n'en expose pas — l'app Android utilise sa propre base de favicons) | `ui-parity/desktop-explore-favicons.png` |
| Liste de mangas (grille) | `fragment_list.xml`, `item_manga_grid.xml` | **implémenté** (MangaGrid : marge 2dp/padding 6dp, coins 12dp, titre 2 lignes) ; écart mineur : badges/anneaux de progression non encore portés | — |
| Liste de mangas (liste) | `item_manga_list.xml` | **implémenté** (MangaList : couverture 40dp coins 4dp, titre+sous-titre) | — |
| Recherche multi-sources | `activity_search.xml` | implémenté (hint exact, débounce 500 ms) ; écart mineur : suggestions de recherche non portées | — |
| Détails du manga | `activity_details.xml` | implémenté (couverture + fond flouté, titre, auteur, tags, description repliable, chapitres, barre Read/Favorite/Download) ; écart mineur : chips favori/communauté/scrobbling non portés | — |
| Lecteur (pager) | `fragment_reader_pager.xml` | implémenté (pager + zones de tap + clavier + overlay contrôles) ; écart mineur : menu de config lecteur non porté | — |
| Lecteur (webtoon) | `fragment_reader_webtoon.xml` | implémenté (défilement vertical continu) | — |
| Favoris (catégories + liste) | `fragment_favourites_container.xml` | en cours (lot sous-agent) | — |
| Historique | `fragment_list_simple.xml` + tenInchScreenshots/1.png | en cours (lot sous-agent) ; référence analysée : chips de filtres + groupement par date + anneaux de progression à reproduire | — |
| Téléchargements | `activity_downloads.xml` | en cours (lot sous-agent) | — |
| Paramètres (apparence) | pref_appearance, AppearanceSettingsFragment | implémenté (thème Follow system/Light/Dark, AMOLED, 9 schémas d'accent avec noms exacts) ; écart mineur : disposition « liste de préférences » simplifiée | — |
| Thème clair / sombre / AMOLED | `colors_kotatsu.xml`, `values-night/*`, overlay Amoled | **implémenté** : palettes générées à l'identique (10 schémas clair/sombre + AMOLED #000000) | Palettes.kt |
