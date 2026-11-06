/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : fusion desktop de `main/ui/protect/AppProtectHelper.kt` (état verrouillé/déverrouillé
 * à l'ouverture), `ProtectViewModel.kt` (tryUnlock + délai anti-bruteforce PASSWORD_COMPARE_DELAY,
 * équivalent de l'« AntiPSWDrug » demandé — l'original n'a pas de classe de ce nom : la protection
 * anti-force-brute y est exactement ce délai d'1 s avant de signaler un mot de passe erroné) et de
 * la partie stockage de `settings/protect/ProtectSetupViewModel.kt`.
 * Hachage : l'original stocke `password.md5()` (parsers util) — MD5 disponible tel quel sur JVM,
 * aucune API Android n'étant requise, l'algorithme d'origine est conservé (pas de bcrypt/SHA-256+sel
 * à introduire) et les préférences restent compatibles avec l'app Android (clés identiques).
 * NB : AppSettings desktop (core/prefs, hors périmètre) n'expose pas appPassword ; le KeyValueStore
 * est donc utilisé directement, avec les noms de clés d'origine.
 */

package org.koitharu.kotatsu.desktop.ui.screens.protect

import kotlinx.coroutines.delay
import org.koitharu.kotatsu.core.prefs.KeyValueStore
import org.koitharu.kotatsu.parsers.util.isNumeric
import org.koitharu.kotatsu.parsers.util.md5

class ProtectManager(private val store: KeyValueStore) {

	/** L'app exige-t-elle un mot de passe à l'ouverture ? (cf. `isProtectApp` / appPassword) */
	val isProtectionEnabled: Boolean
		get() = !store.getString(KEY_APP_PASSWORD, null).isNullOrEmpty()

	val isNumericPassword: Boolean
		get() = store.getBoolean(KEY_APP_PASSWORD_NUMERIC, false)

	/** État courant du verrou (cf. AppProtectHelper.isUnlocked, initialisé selon la protection). */
	var isUnlocked: Boolean = !isProtectionEnabled
		private set

	/** Déverrouille (cf. AppProtectHelper.unlock()). */
	fun unlock() {
		isUnlocked = true
	}

	/** Reverrouille (cf. AppProtectHelper.restoreLock()) — au démarrage ou à la demande. */
	fun lock() {
		isUnlocked = !isProtectionEnabled
	}

	/** Vérifie un mot de passe sans délai (usage interne/tests). */
	fun verify(password: String): Boolean {
		val expected = store.getString(KEY_APP_PASSWORD, null) ?: return false
		return hash(password) == expected
	}

	/**
	 * Tentative de déverrouillage avec garde anti-force-brute (cf. ProtectViewModel.tryUnlock) :
	 * en cas d'échec, attend [PASSWORD_COMPARE_DELAY] ms avant de répondre, pour ralentir les
	 * tentatives répétées.
	 */
	suspend fun tryUnlock(password: String): Boolean {
		if (verify(password)) {
			unlock()
			return true
		}
		delay(PASSWORD_COMPARE_DELAY)
		return false
	}

	/** Définit le mot de passe (cf. ProtectSetupViewModel.onNextClick : hash md5 + flag numérique). */
	fun setPassword(password: String) {
		store.edit {
			putString(KEY_APP_PASSWORD, hash(password))
			putBoolean(KEY_APP_PASSWORD_NUMERIC, password.isNumeric())
		}
	}

	/** Supprime la protection (cf. « reset_password »). */
	fun clearPassword() {
		store.edit {
			putString(KEY_APP_PASSWORD, null)
			putBoolean(KEY_APP_PASSWORD_NUMERIC, false)
		}
	}

	companion object {

		/** Clés d'origine Android (AppSettings.KEY_APP_PASSWORD / KEY_APP_PASSWORD_NUMERIC). */
		const val KEY_APP_PASSWORD = "app_password"
		const val KEY_APP_PASSWORD_NUMERIC = "app_password_num"
		const val KEY_PROTECT_APP = "protect_app"

		/** ProtectViewModel.PASSWORD_COMPARE_DELAY. */
		const val PASSWORD_COMPARE_DELAY = 1_000L

		/** Hachage d'origine : MD5 (parsers util), identique à l'app Android. */
		fun hash(password: String): String = password.md5()
	}
}
