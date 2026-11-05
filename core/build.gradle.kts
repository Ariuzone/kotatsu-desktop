// Module `core` — logique métier partagée, portée depuis l'app Android Kotatsu-Redo.
// Kotlin/JVM pur : modèles, réseau, préférences, base Room (cible JVM), repositories.
// Fichier modifié (projet nouveau, dérivé de Kotatsu-Redo) — 2026-11-05.

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ksp)
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.collections.immutable)

	// Parsers de sources manga (JVM pur, consommé tel quel comme l'app Android)
	api(libs.parsers)

	// Réseau
	implementation(libs.okhttp)
	implementation(libs.okhttp.dnsoverhttps)
	implementation(libs.okio)

	// Base de données : Room avec cible JVM (mêmes entités/DAO que l'app Android)
	// NB : room-ktx est Android-only ; le support coroutines/Flow est inclus dans room-runtime KMP.
	api(libs.androidx.room.runtime)
	ksp(libs.androidx.room.compiler)
	implementation(libs.androidx.sqlite.bundled)
	implementation(libs.androidx.collection)

	// Images (Coil 3 est multiplateforme)
	api(libs.coil.core)
	implementation(libs.coil.network.okhttp)
	implementation(libs.coil.svg)

	testImplementation(libs.junit)
	testImplementation(libs.kotlinx.coroutines.test)
}

ksp {
	arg("room.schemaLocation", "$projectDir/schemas")
	arg("room.generateKotlin", "true")
}
