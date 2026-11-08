/*
 * Fichier créé — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P6 : source de
 * chaînes de l'écran « Comptes et synchronisation » (AccountsScreen). Conformité à la règle
 * client : « toute chaîne affichée vient d'une source de chaînes, jamais écrite en brut dans un
 * écran ». Provenance indiquée en commentaire : libellés de l'app Android (values-fr) et
 * captures de la vidéo de référence (half1-inventory.md, frames 41-50, t≈3:50-4:00) ; les
 * divergences propres au portage desktop sont signalées. Les constantes déjà présentes dans
 * Fr.kt (FR_OK) ne sont pas dupliquées : AccountsScreen les importe directement.
 */
package org.koitharu.kotatsu.desktop.ui.strings

// --- Écran « Comptes et synchronisation » (AccountsScreen, phase P6,
//     parité UI FR avec l'app Android, vidéo t≈3:50-4:00) ---
public const val FR_ACCOUNTS_TITLE: String = "Comptes et synchronisation" // titre de l'écran (vidéo)
public const val FR_TRACKERS_HEADER: String = "Services de suivi" // header de section (string/trackers)
public const val FR_DISABLED: String = "Désactivé" // sous-titre de chaque ligne (string/disabled)

// Noms des services de suivi (noms propres, EN d'origine, ligne 68 de l'inventaire)
public const val FR_SERVICE_ANILIST: String = "AniList"
public const val FR_SERVICE_KITSU: String = "Kitsu"
public const val FR_SERVICE_MAL: String = "MyAnimeList" // EN d'origine
public const val FR_SERVICE_SHIKIMORI: String = "Shikimori"

// Libellé EN volontairement non traduit (identique à l'app Android, inventaire ligne 68)
public const val FR_COMMUNITY_ROW: String = "Community : Comments, ratings and source reliability"

// Ligne Discord (libellé EN d'origine)
public const val FR_DISCORD_ROW: String = "Discord Rich Presence"

// Dialog d'information des lignes de suivi (divergence desktop : authentification web non portée)
public const val FR_TRACKERS_UNAVAILABLE: String =
	"La connexion aux services de suivi n'est pas encore disponible dans le portage desktop " +
		"(authentification web non portée). Elle sera rétablie dans une future version."

// Dialog d'information de la ligne Discord (divergence desktop : Discord RPC non porté)
public const val FR_DISCORD_UNAVAILABLE: String =
	"Discord Rich Presence n'est pas encore disponible dans le portage desktop (RPC non porté). " +
		"Il sera rétabli dans une future version."

// Note d'information en bas d'écran (divergence desktop : synchronisation multi-appareils)
public const val FR_SYNC_UNAVAILABLE_NOTE: String =
	"La synchronisation multi-appareils n'est pas disponible dans le portage desktop."
