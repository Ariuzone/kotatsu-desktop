# Journal de la phase P6 — Comptes & synchronisation / Confidentialité

> Références : `plan_implementation.md` §5 P6, `half1-inventory.md` (lignes 68-69).
> RÈGLE DE TEST : app toujours testée vierge. RÈGLE DE CHAÎNES : tout texte vient de
> `ui/strings/` (nouveau `FrAccounts.kt` pour P6-A ; `FrSettings.kt` pour P6-B).
> **LIVRABLES DE FIN DE PHASE** : `Kotatsu-9.8.6-P6.exe` + `P6-source.zip` → `../releases/`.

## 0. Périmètre P6 (rappel du plan)

- Écran « Comptes et synchronisation » : services de suivi (AniList/Kitsu/MAL/Shikimori —
  DAO seul en core), Community, Discord
- Écran « Confidentialité et contenu » : contenu pour adultes, politique des captures,
  statistiques de lecture, partage de fiabilité ; incognito + verrouillage déjà câblés

## 1. État de départ (inventaire principal, 2026-11-08)

| Élément P6 | État | Preuve |
|---|---|---|
| « Comptes et synchronisation » | ⛔ absent du racine et des routes ; backend = `ScrobblingDao`/`ScrobblingEntity` SEUL (aucun service OAuth porté) | `core/scrobbling/common/data/` |
| « Confidentialité et contenu » | ⛔ stub (route PRIVACY) ; backends : `KEY_DISABLE_NSFW` (no_nsfw, var), `KEY_SCREENSHOTS_POLICY` (ScreenshotsPolicy, var), `KEY_STATS_ENABLED` (var, défaut false), `KEY_INCOGNITO_NSFW` (TriStateOption, var) ; `ProtectManager` + `ProtectScreen` déjà câblés | `AppSettings.kt:59,168,189,316` |
| Statistiques de lecture | backend `core/stats/data` + clé — jamais affiché | — |
| Discord / Community | ⛔ rien (Android : Discord RPC + module Community) | — |

**Décision d'architecture** (principal) : les services de suivi (OAuth/WebView) ne sont pas
portables cette phase → l'écran reproduit FIDÈLEMENT l'état montré dans la vidéo
(AniList/Kitsu/MAL/Shikimori « Désactivé ») avec dialog de connexion reporté (divergence
documentée). Community/Discord idem.

## 2. Journal de la session

### Agent principal

- Inventaire (ci-dessus), découpage 2 lots (COORDINATION.md § P6), lancement des agents
  P6-A (Comptes) et P6-B (Confidentialité) en parallèle.

### Agent P6-A — Comptes et synchronisation (TERMINÉ, compile verte)

- **Nouveau `AccountsScreen.kt`** : « Services de suivi » (AniList, Kitsu, MyAnimeList,
  Shikimori — sous-titre « Désactivé » exact de la vidéo) → dialog d'information M3 (connexion
  reportée, auth web non portée) ; « Community : Comments, ratings and source reliability » (EN
  d'origine conservé) ; « Discord Rich Presence » ; note bas d'écran sur la sync multi-appareils.
- **Nouveau `FrAccounts.kt`** : 13 constantes avec provenance ; réutilise FR_OK de Fr.kt.
- Icônes Material approximatives (documentées) ; `Icons.Filled.Discord` inexistant → Forum.
- Compile verte. Non commité.

### Agent P6-B — Confidentialité (TERMINÉ puis itération 2 en cours)

- **Nouveau `PrivacySettingsScreen.kt`** (sections conformes à la frame 78) : « Contenu pour
  adultes » (switch `isNsfwDisabled` câblé), « Politique relative aux captures d'écran » (dialog
  radio 4 valeurs de l'enum : Autoriser / Masquer le contenu pour adultes / Masquer le contenu
  en mode incognito / Interdire), incognito (info) + « Toujours / Demander / Jamais » (dialog
  TriStateOption câblé), header « Sécurité de l'application » (Protéger l'application — état lu
  via la clé du store), header « Votre activité » (Statistiques de lecture, Partager la
  fiabilité des sources désactivée).
- **Découverte** : la clé `no_nsfw` n'est lue par aucun consommateur desktop (persistée mais
  sans effet de filtrage encore) — documenté.
- Chaînes ajoutées à `FrSettings.kt` (unique propriétaire). Compile verte au 1er essai.
- **Itération 2 (principal + agent)** : setters `isStatsEnabled` + `isSuggestionsExcludeNsfw`
  ajoutés par le principal ; agent relancé pour câbler le switch stats et ajouter les 2 entrées
  manquantes de la frame 78 (Suggestions 18+ câblé, Discord NSFW grisée).

### Intégration du principal (11-08)

- **`AppSettings.kt`** : setters ajoutés pour `isStatsEnabled` et `isSuggestionsExcludeNsfw`
  (étaient `val`) → itération 2 de P6-B : switch stats câblé + 2 entrées frame 78 ajoutées
  (« Suggestions 18+ » câblé — sens ON = exclure, documenté ; « Masquer le contenu NSFW sur
  Discord » grisée, Discord non porté).
- **`Main.kt`** : routes ACCOUNTS → `AccountsScreen`, PRIVACY → `PrivacySettingsScreen`
  (remplacent les stubs).
- **`SettingsRootScreen.kt`** : route ACCOUNTS + ligne « Comptes et synchronisation — Services de
  suivi, Community, Discord » (section Application, entre Notifications et Confidentialité) ;
  chaînes FR_ACCOUNTS/FR_ACCOUNTS_SUMMARY ajoutées à Fr.kt (règle chaînes à la source).
- **Build complet** `:desktop:build :core:build` → **EXIT=0**.

### Captures de parité (principal, 11-08, app vierge)

- Règle client appliquée (wipe + relance + vérification fraîcheur).
- **2 captures vérifiées** : `desktop-accounts.png` (4 trackers « Désactivé » + Community EN +
  Discord + note sync) et `desktop-privacy.png` (tout l'écran : NSFW, incognito ×2, Suggestions
  18+, Discord NSFW grisée, Sécurité de l'application, Votre activité) ; la racine avec la
  nouvelle ligne vérifiée en route.
- App fermée après captures.

## ✅ Bilan P6

- Matrice (plan §6) : Comptes et synchronisation ⛔ → ✅/◐ (écran complet, services reproduits
  « Désactivé » comme la vidéo — connexion OAuth reportée, divergence) ; Confidentialité ⛔ → ✅.
- Vérifications : compile par lot, build complet EXIT=0, 2 captures sur app vierge.
- Restes documentés : connexion des trackers (OAuth/WebView), Discord RPC, filtrage NSFW des
  sources (clé persistée, sans consommateur desktop), partage de fiabilité.
