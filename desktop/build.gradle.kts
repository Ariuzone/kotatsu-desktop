// Module `desktop` — application Compose Multiplatform Desktop (Windows).
// Reconstruit l'UI de Kotatsu-Redo à l'identique en Compose Material 3.
// Fichier modifié (projet nouveau, dérivé de Kotatsu-Redo) — 2026-11-05.

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val composeVersion: String = libs.versions.compose.get()

// Répertoire de build dédié (l'ancien desktop/build est verrouillé par un process tiers)
layout.buildDirectory.set(layout.projectDirectory.dir("build-desktop"))

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	implementation(project(":core"))
	implementation(compose.desktop.currentOs)
	implementation("org.jetbrains.compose.material3:material3:$composeVersion")
	implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
	implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
	implementation("org.jetbrains.compose.ui:ui:$composeVersion")
	implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
	implementation(libs.kotlinx.coroutines.swing)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.okhttp)
	implementation(libs.coil.compose)
	implementation(libs.coil.network.okhttp)

	testImplementation(kotlin("test"))
}

compose.desktop {
	application {
		mainClass = "org.koitharu.kotatsu.desktop.MainKt"

		buildTypes {
			release.proguard {
				// Désactivé : ProGuard casse des méthodes inline d'okio en release
				// (VerifyError buffer$hash dans l'app packagée, cf. docs) — l'obfuscation
				// n'apporte rien pour une app desktop avec JDK embarqué.
				isEnabled = false
				configurationFiles.from(project.file("compose-desktop.pro"))
			}
		}

		// Métadonnées propres au portage desktop
		group = "org.koitharu.kotatsu"
		version = "1.1.1"

		nativeDistributions {
			targetFormats(TargetFormat.Exe, TargetFormat.Msi)
			packageName = "Kotatsu Desktop"
			// Les scripts personnalisés (installateur / désinstallateur) sont injectés dans
			// <installation>\app\resources par les tâches Gradle injectHelpers* (bas de fichier)
			// — mécanisme appResourcesRootDir écarté (copie non fiable selon les variantes).
			packageVersion = "1.1.1"
			description = "Lecteur de manga — portage Windows de Kotatsu-Redo"
			vendor = "Portage non officiel de Kotatsu-Redo (GPLv3)"
			// UUID de la ligne produit « Kotatsu Desktop 1.x » (nouveau en 1.1 : l'ancien UUID servait
			// aux installateurs 9.8.6 — MSI refusait 1.0/1.1 comme « version inférieure » à 9.8.6
			// déjà installée, bloquant toute mise à jour). Conserver ce UUID pour toutes les
			// futures versions 1.x ; les mises à jour = major-upgrade MSI (augmenter packageVersion).
			windows {
				exePackageVersion = "1.1.1"
				msiPackageVersion = "1.1.1"
				iconFile.set(project.file("src/main/resources/kotatsu.ico"))
				menu = true
				shortcut = true
				dirChooser = true
				// Installation PAR UTILISATEUR : pas d UAC a l installation/desinstallation,
				// cle de desinstallation dans HKCU (modifiable par l app sans elevation).
				perUserInstall = true
				upgradeUuid = "e17ceb03-287c-4d25-ab56-748855fef9d3"
			}

			// JDK embarqué : l'utilisateur n'a pas besoin d'installer Java
			includeAllModules = true
		}
	}
}
