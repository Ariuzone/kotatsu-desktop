# Journal de la phase P5 — Vos données & Application

> Références : `plan_implementation.md` §5 P5, `half1-inventory.md` (lignes 13, 49).
> RÈGLE DE TEST : app toujours testée vierge (wipe `%APPDATA%\Kotatsu` avant lancement).
> **LIVRABLES DE FIN DE PHASE (consigne client)** : code source prêt GitHub + installateur `.exe`
> → `../releases/` (P5-source.zip / Kotatsu-9.8.6-P5.exe).

## 0. Périmètre P5 (rappel du plan)

- Téléchargements et stockage : dossier, utilisation, data removal
- Sauvegarde et restauration : créer/restaurer
- Réseau : Données mobiles, Proxy, DNS sur HTTPS (backends `DoHManager` + clés `KEY_PROXY_*`/`KEY_DOH` déjà en core)
- Notifications : écran de réglage
- À propos : écran version

## 1. État de départ (inventaire principal, 2026-11-08)

| Élément P5 | État | Preuve |
|---|---|---|
| « Téléchargements et stockage » | ◐ écran téléchargements ✅ (rail) ; dossier/utilisation/data removal ⛔ | `Main.kt` showDownloads |
| « Sauvegarde et restauration » | ⛔ stub ; seul vestige `BadBackupFormatException` ; libellé vidéo EN « Create or restore a backup » à conserver | `core/exceptions/` |
| « Réseau » | ⛔ stub UI ; backend complet : `KEY_DOH` (DoHProvider), `KEY_PROXY_TYPE/ADDRESS/PORT/LOGIN/PASSWORD`, `KEY_IMAGES_PROXY`, `allowDownloadOnMeteredNetwork` (TriStateOption), `isSuggestionsWiFiOnly`, `KEY_TRACKER_WIFI_ONLY` ; `DoHManager` + `NetworkModule` câblés | `AppSettings.kt:244-274,400,414` |
| « Notifications » | ◐ tray ✅ ; écran réglage ⛔ ; clés `KEY_TRACKER_NOTIFICATIONS`, `KEY_TRACKER_NO_NSFW` existent | `AppSettings.kt:311-314` |
| « À propos » | ⛔ titre de fenêtre seulement (« Version 9.8.6 » déjà en sous-titre du racine) | `Main.kt` |
| Dossier téléchargements | `File(appDir, "local")` fixe (LocalMangaRepository), cache `File(appDir, "okhttp-cache")` | `AppContainer.kt:92,147` |

## 2. Journal de la session

### Agent principal

- Inventaire (tableau ci-dessus). Découpage 2 lots (COORDINATION.md § P5) — `Main.kt`/`Fr.kt`
  interdits aux agents, chaînes privées locales. Sauvegarde/restauration : implémentation desktop
  pragmatique (zip kotatsu.db + settings.json), divergence documentée.
- Lancement des agents P5-A (Vos données) et P5-B (Application) en parallèle.

### Agent P5-A — Vos données (TERMINÉ, compile verte)

- **`DownloadsStorageSettingsScreen.kt`** : dossier téléchargements (chemin réel + JFileChooser +
  divergence « déplacement manuel »), « Ouvrir le dossier » (java.awt.Desktop), « Utilisation du
  stockage » (taille réelle calculée en IO), « Data removal », bannière EN exacte « Create or
  restore a backup » + « Sauvegardes ».
- **`StorageUsageScreen.kt`** : Base de données / Téléchargements / Cache réseau / Total — tailles
  réelles + barres de progression.
- **`DataRemovalScreen.kt`** : 4 actions TOUTES câblées réellement (confirmations + snackbar
  desktop 4 s) : historique (`HistoryDao.clear()`), suggestions (`SuggestionDao.deleteAll()`),
  cache réseau (okhttp-cache/), cookies (cookies.json).
- **`BackupRestoreScreen.kt`** : sauvegarde ZIP réelle (kotatsu.db + WAL/SHM + settings.json,
  entrées à la racine) ; restauration avec validation (kotatsu.db obligatoire) + protection
  zip-slip + fermeture de l'app (`exitProcess(0)`).
- **Divergences notées** : dossier téléchargements non déplaçable à chaud ; restauration = extrait
  puis relance manuelle ; snackbar maison ; chemins Windows réels.
- Compile verte (3 itérations). Non commité.

### Agent P5-B — Application (TERMINÉ puis itération 2 en cours)

- **`NotificationsSettingsScreen.kt`**, **`NetworkSettingsScreen.kt`**, **`AboutScreen.kt`** créés
  (compile verte). Enums relevées : TriStateOption (ENABLED/ASK/DISABLED → « Toujours / Demander /
  Jamais »), DoHProvider (NONE/GOOGLE/CLOUDFLARE/ADGUARD/ZERO_MS), Proxy.Type (DIRECT/HTTP/SOCKS).
- À propos : version 9.8.6 copiable, licence GPLv3, liens GitHub d'origine cliquables.
- **Découverte** : plusieurs préférences étaient en `val` (notifications tracker, no-NSFW, Wi-Fi
  only ×2, proxy ×5) → écrans en lecture seule au 1er passage.
- **Itération 2 (principal + agent)** : setters ajoutés dans AppSettings par le principal
  (compile verte), agent P5-B relancé pour câbler switches Wi-Fi only, notifications, no-NSFW et
  les dialogs proxy (type/adresse/port/identifiant/mot de passe).

### Intégration du principal (11-08)

- **`AppSettings.kt`** (principal) : setters ajoutés pour `isTrackerWiFiOnly`,
  `isSuggestionsWiFiOnly`, `isTrackerNotificationsEnabled`, `isTrackerNoNsfw`, `proxyType`,
  `proxyAddress`, `proxyPort`, `proxyLogin`, `proxyPassword` (étaient `val`) → itération 2 de
  P5-B : tous les switches câblés + dialogs proxy (type radio, adresse/port/identifiant saisis,
  port validé 0..65535, mot de passe masqué).
- **`Main.kt`** : routes DATA → `DownloadsStorageSettingsScreen` (avec `container.appDir`,
  exposé via nouvelle propriété publique `AppContainer.appDir`), DATA_USAGE →
  `StorageUsageScreen`, DATA_REMOVAL → `DataRemovalScreen`, BACKUP → `BackupRestoreScreen`,
  NOTIFICATIONS → `NotificationsSettingsScreen` (renvoi vers FEED_SETTINGS), NETWORK →
  `NetworkSettingsScreen`, ABOUT → `AboutScreen` — toutes les 7 remplacent les stubs.
- **Build complet** `:desktop:build :core:build` → **EXIT=0**.

### Captures de parité (principal, 11-08, app vierge)

- Règle client appliquée (wipe + relance + vérification fraîcheur + fenêtre 1300×1100).
- **7 captures vérifiées** dans `docs/ui-parity/` : `desktop-downloads-storage.png` (chemin réel,
  « Utilisation du stockage — 613 Ko » calculé, bannière EN exacte), `desktop-storage-usage.png`
  (tailles réelles + barres : DB 198 Ko, cache 416 Ko), `desktop-data-removal.png` (4 actions),
  `desktop-backup-restore.png` (« Create or restore a backup », créer/restaurer ZIP, note),
  `desktop-notifications.png` (switches câblés), `desktop-network.png` (données mobiles «
  Demander », Wi-Fi only ×2, proxy complet, DoH), `desktop-about.png` (Version 9.8.6, licence,
  liens).
- App fermée après captures.

## ✅ Bilan P5

- Matrice (plan §6) : Téléchargements et stockage ◐ → ✅ ; Sauvegarde et restauration ⛔ → ✅
  (ZIP réel, divergent du format Android — documenté) ; Notifications ◐ → ✅ ; Réseau ⛔ → ✅ ;
  À propos ⛔ → ✅.
- Vérifications : compile par lot, build complet EXIT=0, 7 captures sur app vierge.
- Restes documentés : dossier téléchargements non déplaçable à chaud ; restauration = extrait +
  relance manuelle ; journal P4 et log scheduler à alimenter.
