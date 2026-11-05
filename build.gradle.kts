// Portage Windows de Kotatsu-Redo — build racine.
// Fichier modifié (projet nouveau, dérivé de Kotatsu-Redo) — 2026-11-05.

plugins {
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.multiplatform) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.compose) apply false
	alias(libs.plugins.compose.compiler) apply false
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.room) apply false
}
