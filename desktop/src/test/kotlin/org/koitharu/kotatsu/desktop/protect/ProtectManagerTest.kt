/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.desktop.protect

import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.koitharu.kotatsu.core.prefs.FileKeyValueStore
import org.koitharu.kotatsu.desktop.ui.screens.protect.ProtectManager

class ProtectManagerTest {

	private lateinit var manager: ProtectManager

	@BeforeTest
	fun setUp() {
		val dir = Files.createTempDirectory("kotatsu-protect-test")
		manager = ProtectManager(
			FileKeyValueStore(dir.resolve("settings.json")),
		)
	}

	@Test
	fun `hash - md5 d'origine, déterministe et distinct par mot de passe`() {
		val expected = java.security.MessageDigest.getInstance("MD5")
			.digest("1234".toByteArray())
			.joinToString("") { "%02x".format(it) }
		assertEquals(expected, ProtectManager.hash("1234"))
		assertEquals(ProtectManager.hash("abc"), ProtectManager.hash("abc"))
		assertFalse(ProtectManager.hash("abc") == ProtectManager.hash("abd"))
	}

	@Test
	fun `protection - inactive par défaut, activée par setPassword, retirée par clearPassword`() {
		assertFalse(manager.isProtectionEnabled)
		assertTrue(manager.isUnlocked) // pas de protection → déverrouillé (AppProtectHelper)

		manager.setPassword("secret42")
		assertTrue(manager.isProtectionEnabled)
		assertTrue(manager.isNumericPassword.not())
		manager.lock()
		assertFalse(manager.isUnlocked) // verrouillé à l'ouverture (isProtectApp)
		assertTrue(manager.verify("secret42"))
		assertFalse(manager.verify("wrong"))

		manager.clearPassword()
		assertFalse(manager.isProtectionEnabled)
		manager.lock()
		assertTrue(manager.isUnlocked)
	}

	@Test
	fun `tryUnlock - succès déverrouille, échec applique le délai anti-bruteforce`() = runBlocking {
		manager.setPassword("sesame")
		manager.lock() // l'état de verrou est établi à la construction (cf. AppProtectHelper)

		val start = System.currentTimeMillis()
		val failed = manager.tryUnlock("bad")
		val elapsed = System.currentTimeMillis() - start
		assertFalse(failed)
		assertFalse(manager.isUnlocked)
		// Délai anti-force-brute (PASSWORD_COMPARE_DELAY = 1 s, cf. ProtectViewModel)
		assertTrue(elapsed >= ProtectManager.PASSWORD_COMPARE_DELAY - 50)

		val unlocked = manager.tryUnlock("sesame")
		assertTrue(unlocked)
		assertTrue(manager.isUnlocked)
	}
}
