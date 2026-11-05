// Portage Windows de Kotatsu-Redo — fichier de réglages Gradle.
// Fichier modifié (projet nouveau, dérivé de Kotatsu-Redo) — 2026-11-05.

pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google() // artefacts androidx (Room KMP/JVM, sqlite-bundled, collection)
		mavenCentral()
		maven("https://jitpack.io") // kotatsu-parsers-redo
	}
}

rootProject.name = "Kotatsu-Desktop"

include(":core")
include(":desktop")
