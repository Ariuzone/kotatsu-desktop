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

	testImplementation(kotlin("test"))
}

compose.desktop {
	application {
		mainClass = "org.koitharu.kotatsu.desktop.MainKt"

		buildTypes {
			release.proguard {
				configurationFiles.from(project.file("compose-desktop.pro"))
			}
		}

		// Métadonnées dérivées de l'app d'origine (versionName 9.8.6)
		group = "org.koitharu.kotatsu"
		version = "9.8.6"

		nativeDistributions {
			targetFormats(TargetFormat.Exe, TargetFormat.Msi)
			packageName = "Kotatsu"
			description = "Lecteur de manga — portage Windows de Kotatsu-Redo"
			vendor = "Portage non officiel de Kotatsu-Redo (GPLv3)"
			// UUID stable pour les mises à niveau (installation/désinstallation propres)
			// NOTE : à régénérer une seule fois puis conserver.
			windows {
				iconFile.set(project.file("src/main/resources/kotatsu.ico"))
				menu = true
				shortcut = true
				dirChooser = true
				upgradeUuid = "7f9a4c1e-2d5b-4e8f-9c3a-6b1d8e2f4a7c"
			}

			// JDK embarqué : l'utilisateur n'a pas besoin d'installer Java
			includeAllModules = true
		}
	}
}
