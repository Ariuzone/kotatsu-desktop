/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase 1.1.1 :
 * interfaces de maintenance GUI incluses dans l'exécutable :
 *   - Désinstallateur : lancé depuis Paramètres Windows > Applications > Kotatsu Desktop >
 *     Désinstaller (la clé UninstallString de HKCU pointe vers ce lanceur, cf. Main.kt),
 *     avec choix de conserver ou supprimer les données utilisateur.
 *   - Multilingue : suit la préférence « Langue de l'application » (défaut = langue du
 *     système ; repli anglais si indisponible).
 * Lancement : propriété système -Dkotatsu.mode=uninstaller (le lanceur « Uninstall » est
 * une copie de l'exécutable principal avec cette option dans son .cfg — cf. Main.kt).
 */
package org.koitharu.kotatsu.desktop.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koitharu.kotatsu.core.prefs.AppSettings
import java.io.File

/** Langues d'interface disponibles (SYSTEM = langue de Windows, repli EN). */
public enum class MaintenanceLang { SYSTEM, EN, FR }

/** Résout la langue effective : préférence utilisateur, sinon langue système, sinon EN. */
public fun resolveLang(preference: String): MaintenanceLang {
	when (preference) {
		"en" -> return MaintenanceLang.EN
		"fr" -> return MaintenanceLang.FR
	}
	val sys = System.getProperty("user.language")?.lowercase() ?: return MaintenanceLang.EN
	return if (sys.startsWith("fr")) MaintenanceLang.FR else MaintenanceLang.EN
}

private fun readAppLanguage(): String {
	val appData = File(System.getenv("APPDATA"), "Kotatsu")
	return AppSettings(
		org.koitharu.kotatsu.core.prefs.FileKeyValueStore(
			appData.resolve("settings.json").toPath(),
		),
	).appLanguage
}

private class Strings(lang: MaintenanceLang) {
	val fr = lang == MaintenanceLang.FR
	val title = if (fr) "Désinstaller Kotatsu Desktop" else "Uninstall Kotatsu Desktop"
	val installed = if (fr) "Kotatsu Desktop est installé sur cet ordinateur." else "Kotatsu Desktop is installed on this computer."
	val notInstalled = if (fr) "Kotatsu Desktop n'a pas été trouvé sur cet ordinateur." else "Kotatsu Desktop was not found on this computer."
	val language = if (fr) "Langue" else "Language"
	val keepData = if (fr) "Conserver mes données (favoris, historique, téléchargements, préférences) pour une éventuelle réinstallation" else "Keep my data (favourites, history, downloads, preferences) for a possible reinstallation"
	val deleteData = if (fr) "Supprimer toutes mes données" else "Delete all my data"
	val dataPath = if (fr) "Emplacement des données :" else "Data location:"
	val uninstall = if (fr) "Désinstaller" else "Uninstall"
	val cancel = if (fr) "Annuler" else "Cancel"
	val working = if (fr) "Désinstallation en cours…" else "Uninstalling…"
	val done = if (fr) "Désinstallation terminée." else "Uninstall complete."
	val doneKeep = if (fr) "Vos données ont été conservées dans :" else "Your data was kept in:"
	val failed = if (fr) "La désinstallation a échoué (code %d)." else "Uninstall failed (code %d)."
	val close = if (fr) "Fermer" else "Close"
	val warnRunning = if (fr) "L'application est en cours d'exécution et va être fermée." else "The application is running and will be closed."
	val installDone = if (fr) "Installation terminée avec succès. Vos données sont conservées." else "Install completed successfully. Your data is preserved."
	val installFailed = if (fr) "L'installation a échoué (code %d)." else "Install failed (code %d)."
}

/** Point d'entrée du mode maintenance : fenêtre de désinstallation GUI. */
public fun mainUninstaller() {
	val appData = File(System.getenv("APPDATA"), "Kotatsu")
	val settings = AppSettings(
		org.koitharu.kotatsu.core.prefs.FileKeyValueStore(
			appData.resolve("settings.json").toPath(),
		),
	)
	application {
		var lang by remember {
			mutableStateOf(resolveLang(settings.appLanguage))
		}
		val strings = remember(lang) { Strings(lang) }
		Window(
			onCloseRequest = ::exitApplication,
			title = strings.title,
			state = rememberWindowState(width = 560.dp, height = 480.dp),
		) {
			MaintenanceTheme(content = {
				UninstallerScreen(
					strings = strings,
					onLangChange = {
						lang = it
						settings.appLanguage = when (it) {
							MaintenanceLang.FR -> "fr"
							MaintenanceLang.EN -> "en"
							MaintenanceLang.SYSTEM -> "system"
						}
					},
				)
			})
		}
	}
}

/** Point d'entrée du mode maintenance : fenêtre d'installation / mise à jour GUI. */
public fun mainInstaller(msiPath: String? = null) {
	val appData = File(System.getenv("APPDATA"), "Kotatsu")
	val settings = AppSettings(
		org.koitharu.kotatsu.core.prefs.FileKeyValueStore(
			appData.resolve("settings.json").toPath(),
		),
	)
	application {
		var lang by remember {
			mutableStateOf(resolveLang(settings.appLanguage))
		}
		val strings = remember(lang) { Strings(lang) }
		Window(
			onCloseRequest = ::exitApplication,
			title = if (strings.fr) "Installer Kotatsu Desktop" else "Install Kotatsu Desktop",
			state = rememberWindowState(width = 560.dp, height = 520.dp),
		) {
			MaintenanceTheme(content = {
				InstallerScreen(
					strings = strings,
					msiPath = msiPath,
					onLangChange = {
						lang = it
						settings.appLanguage = when (it) {
							MaintenanceLang.FR -> "fr"
							MaintenanceLang.EN -> "en"
							MaintenanceLang.SYSTEM -> "system"
						}
					},
				)
			})
		}
	}
}

@Composable
private fun MaintenanceTheme(content: @Composable () -> Unit) {
	MaterialTheme(content = content)
}

@Composable
private fun UninstallerScreen(strings: Strings, onLangChange: (MaintenanceLang) -> Unit) {
	var keepData by remember { mutableStateOf(true) }
	var status by remember { mutableStateOf("idle") }
	var exitCode by remember { mutableStateOf<Int?>(null) }
	var langMenu by remember { mutableStateOf(false) }
	val appDir = File(System.getenv("APPDATA"), "Kotatsu")

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		// Sélecteur de langue
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(strings.language, style = MaterialTheme.typography.labelLarge)
			Spacer(Modifier.width(12.dp))
			TextButton(onClick = { langMenu = true }) {
				Text(
					when (resolveLang(readAppLanguage())) {
						MaintenanceLang.FR -> "Français"
						else -> "English"
					},
				)
			}
			if (langMenu) {
				AlertDialog(
					onDismissRequest = { langMenu = false },
					title = { Text(strings.language) },
					text = {
						Column {
							MaintenanceLang.entries.forEach { l ->
								Row(
									modifier = Modifier.fillMaxWidth(),
									verticalAlignment = Alignment.CenterVertically,
								) {
									RadioButton(
										selected = l == resolveLang(readAppLanguage()),
										onClick = {
											onLangChange(l)
											langMenu = false
										},
									)
									Text(if (l == MaintenanceLang.FR) "Français" else "English")
								}
							}
						}
					},
					confirmButton = {
						TextButton(onClick = { langMenu = false }) { Text(strings.close) }
					},
				)
			}
		}

		Text(strings.installed, style = MaterialTheme.typography.bodyLarge)
		Text(
			text = strings.warnRunning,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)

		Text(strings.dataPath + " " + appDir.path, style = MaterialTheme.typography.bodySmall)

		// Choix données : conserver (défaut) / supprimer
		Row(verticalAlignment = Alignment.CenterVertically) {
			RadioButton(selected = keepData, onClick = { keepData = true })
			Text(strings.keepData, style = MaterialTheme.typography.bodyMedium)
		}
		Row(verticalAlignment = Alignment.CenterVertically) {
			RadioButton(selected = !keepData, onClick = { keepData = false })
			Text(strings.deleteData, style = MaterialTheme.typography.bodyMedium)
		}

		Spacer(Modifier.height(4.dp))
		when (status) {
			"idle" -> {
				Button(
					onClick = { status = "working" },
					modifier = Modifier.fillMaxWidth(),
				) { Text(strings.uninstall) }
				TextButton(onClick = { exitProcessMaintenance() }, modifier = Modifier.fillMaxWidth()) {
					Text(strings.cancel)
				}
			}
			"working" -> {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
				Text(strings.working)
			}
			else -> {
				val code = exitCode
				if (code == 0 || code == 3010) {
					Text(strings.done, color = MaterialTheme.colorScheme.primary)
					if (keepData) {
						Text(strings.doneKeep + " " + appDir.path)
					}
				} else {
					Text(strings.failed.format(code ?: -1), color = MaterialTheme.colorScheme.error)
				}
				TextButton(onClick = { exitProcessMaintenance() }, modifier = Modifier.fillMaxWidth()) {
					Text(strings.close)
				}
			}
		}
	}

	// Exécution de la désinstallation hors UI
	if (status == "working") {
		Thread {
			val code = runWindowsUninstaller()
			if ((code == 0 || code == 3010) && !keepData) {
				appDir.deleteRecursively()
				File(System.getenv("APPDATA"), "KotatsuRedo").deleteRecursively()
			}
			java.awt.EventQueue.invokeLater {
				exitCode = code
				status = "done"
			}
		}.start()
	}
}

/** Trouve le code produit « Kotatsu Desktop » (HKCU, installation par utilisateur) et lance msiexec /x. */
private fun runWindowsUninstaller(): Int {
	// L'application tourne encore ? fermeture (ce processus-ci est le lanceur dédié)
	try {
		val pb = ProcessBuilder("taskkill", "/F", "/IM", "Kotatsu Desktop.exe")
		pb.start().waitFor()
	} catch (_: Exception) { }

	// Recherche du code produit dans HKCU (installation par utilisateur, pas d'UAC requis)
	val query = ProcessBuilder("reg", "query", "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall", "/s", "/f", "Kotatsu Desktop", "/d")
		.start()
	val output = query.inputStream.readBytes().toString(Charsets.UTF_8)
	query.waitFor()
	val key = output.lineSequence()
		.map { it.trim() }
		.firstOrNull { it.startsWith("HKEY_") }
		?: return 1602 // produit introuvable

	val uninstall = ProcessBuilder("msiexec.exe", "/x", key.substringAfterLast("\\"), "/qn", "/norestart")
		.start()
	return uninstall.waitFor()
}

private fun exitProcessMaintenance() {
	// Ferme la fenêtre de maintenance (exitApplication fait le reste via Compose)
 kotlin.system.exitProcess(0)
}

@Composable
private fun InstallerScreen(
	strings: Strings,
	msiPath: String?,
	onLangChange: (MaintenanceLang) -> Unit,
) {
	var status by remember { mutableStateOf("idle") }
	var exitCode by remember { mutableStateOf<Int?>(null) }
	var langMenu by remember { mutableStateOf(false) }
	val appDir = File(System.getenv("APPDATA"), "Kotatsu")

	// Données d'une installation précédente (laissées par l'option « conserver » du
	// désinstallateur) : détectées automatiquement et reprises par l'installation —
	// l'app lit ce dossier à chaque lancement, aucune copie n'est nécessaire.
	val existingData = appDir.isDirectory &&
		(File(appDir, "kotatsu.db").isFile || File(appDir, "settings.json").isFile)

	// Package MSI : à côté de l'exécutable (scénario setup), sinon dossier Téléchargements
	val msi: File? = remember {
		msiPath?.let(::File)?.takeIf { it.isFile }
			?: File(System.getProperty("jpackage.app-dir") ?: "", "Kotatsu Desktop.msi")
				.takeIf { it.isFile }
			?: sequenceOf(
					File(System.getProperty("user.home"), "Downloads"),
					File(System.getProperty("user.dir")),
					File(System.getProperty("user.dir")).parentFile,
				).asSequence()
				.flatMap { dir ->
					dir?.listFiles { f -> f.name.startsWith("Kotatsu Desktop") && f.name.endsWith(".msi") }
						?.asSequence() ?: emptySequence()
				}
				.maxByOrNull { it.lastModified() }
	}

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		// Sélecteur de langue
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(strings.language, style = MaterialTheme.typography.labelLarge)
			Spacer(Modifier.width(12.dp))
			TextButton(onClick = { langMenu = true }) {
				Text(
					when (resolveLang(readAppLanguage())) {
						MaintenanceLang.FR -> "Français"
						else -> "English"
					},
				)
			}
			if (langMenu) {
				AlertDialog(
					onDismissRequest = { langMenu = false },
					title = { Text(strings.language) },
					text = {
						Column {
							MaintenanceLang.entries.forEach { l ->
								Row(
									modifier = Modifier.fillMaxWidth(),
									verticalAlignment = Alignment.CenterVertically,
								) {
									RadioButton(
										selected = l == resolveLang(readAppLanguage()),
										onClick = {
											onLangChange(l)
											langMenu = false
										},
									)
									Text(if (l == MaintenanceLang.FR) "Français" else "English")
								}
							}
						}
					},
					confirmButton = {
						TextButton(onClick = { langMenu = false }) { Text(strings.close) }
					},
				)
			}
		}

		Text(
			if (strings.fr) "Kotatsu Desktop — installation / mise à jour"
			else "Kotatsu Desktop — install / update",
			style = MaterialTheme.typography.titleLarge,
		)

		// Détection des données conservées
		if (existingData) {
			Text(
				if (strings.fr)
					"Des données d'une installation précédente ont été détectées — elles seront automatiquement reprises (favoris, historique, téléchargements, préférences)."
				else
					"Data from a previous installation was detected — it will be picked up automatically (favourites, history, downloads, preferences).",
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.bodyMedium,
			)
		}

		if (msi == null) {
			Text(
				if (strings.fr)
					"Aucun package « Kotatsu Desktop-<version>.msi » trouvé. Placez le .msi à côté de ce programme ou dans votre dossier Téléchargements."
				else
					"No 'Kotatsu Desktop-<version>.msi' package found. Place the .msi next to this program or in your Downloads folder.",
				color = MaterialTheme.colorScheme.error,
				style = MaterialTheme.typography.bodyMedium,
			)
		} else {
			Text(
				(if (strings.fr) "Package : " else "Package: ") + (msi?.path ?: ""),
				style = MaterialTheme.typography.bodySmall,
			)
		}

		Spacer(Modifier.height(4.dp))
		when (status) {
			"idle" -> {
				Button(
					onClick = { status = "working" },
					modifier = Modifier.fillMaxWidth(),
					enabled = msi != null,
				) {
					Text(
						if (existingData) {
							if (strings.fr) "Installer / Mettre à jour (données reprises)"
							else "Install / Update (data picked up)"
						} else {
							if (strings.fr) "Installer" else "Install"
						},
					)
				}
				TextButton(onClick = { exitProcessMaintenance() }, modifier = Modifier.fillMaxWidth()) {
					Text(strings.cancel)
				}
			}
			"working" -> {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
				Text(if (strings.fr) "Installation en cours…" else "Installing…")
			}
			else -> {
				val code = exitCode
				if (code == 0 || code == 3010) {
					Text(strings.installDone, color = MaterialTheme.colorScheme.primary)
				} else {
					Text(strings.installFailed.format(code ?: -1), color = MaterialTheme.colorScheme.error)
				}
				TextButton(onClick = { exitProcessMaintenance() }, modifier = Modifier.fillMaxWidth()) {
					Text(strings.close)
				}
			}
		}
	}

	if (status == "working") {
		val msiFile = msi
		Thread {
			if (msiFile == null) {
				java.awt.EventQueue.invokeLater {
					exitCode = 1602
					status = "done"
				}
			} else {
				// L'app ne doit pas tourner pendant l'installation
				try {
					ProcessBuilder("taskkill", "/F", "/IM", "Kotatsu Desktop.exe").start().waitFor()
				} catch (_: Exception) { }
				// Installation par utilisateur : pas d'élévation requise ; major-upgrade MSI
				// si une 1.x est déjà installée (données conservées automatiquement).
				val proc = ProcessBuilder("msiexec.exe", "/i", msiFile.path, "/qn", "/norestart").start()
				val code = proc.waitFor()
				java.awt.EventQueue.invokeLater {
					exitCode = code
					status = "done"
				}
			}
		}.start()
	}
}
