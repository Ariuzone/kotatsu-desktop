/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Thème Material 3 de l'app desktop. Les valeurs de couleur sont celles de l'app d'origine
 * (voir Palettes.kt, généré depuis colors_kotatsu.xml / colors_themed.xml / colors.xml).
 * Logique de résolution : identique à BaseActivity.kt d'origine (schéma d'accent + mode
 * clair/sombre + overlay AMOLED).
 */
package org.koitharu.kotatsu.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Fond AMOLED de l'app d'origine (colors.xml : background_amoled = #000000). */
private val AMOLED_BACKGROUND = Color(0xFF000000)

/** Fallbacks pour les tokens absents des ressources d'origine (M3 standards). */
private val FALLBACK_OUTLINE_VARIANT_LIGHT = Color(0xFFC4C6D0)
private val FALLBACK_OUTLINE_VARIANT_DARK = Color(0xFF44474E)
private val DEFAULT_SCRIM = Color(0xFF000000)

public fun kotatsuColorScheme(scheme: String, dark: Boolean, amoled: Boolean = false): ColorScheme {
	val p = paletteFor(scheme, dark)
	return if (dark) {
		darkColorScheme(
			primary = p.getValue("primary"),
			onPrimary = p.getValue("onPrimary"),
			primaryContainer = p.getValue("primaryContainer"),
			onPrimaryContainer = p.getValue("onPrimaryContainer"),
			inversePrimary = p.getValue("inversePrimary"),
			secondary = p.getValue("secondary"),
			onSecondary = p.getValue("onSecondary"),
			secondaryContainer = p.getValue("secondaryContainer"),
			onSecondaryContainer = p.getValue("onSecondaryContainer"),
			tertiary = p.getValue("tertiary"),
			onTertiary = p.getValue("onTertiary"),
			tertiaryContainer = p.getValue("tertiaryContainer"),
			onTertiaryContainer = p.getValue("onTertiaryContainer"),
			background = if (amoled) AMOLED_BACKGROUND else p.getValue("background"),
			onBackground = p.getValue("onBackground"),
			surface = if (amoled) AMOLED_BACKGROUND else p.getValue("surface"),
			onSurface = p.getValue("onSurface"),
			surfaceVariant = p.getValue("surfaceVariant"),
			onSurfaceVariant = p.getValue("onSurfaceVariant"),
			outline = p.getValue("outline"),
			outlineVariant = p.getOrDefault("outlineVariant", FALLBACK_OUTLINE_VARIANT_DARK),
			inverseSurface = p.getValue("inverseSurface"),
			inverseOnSurface = p.getValue("inverseOnSurface"),
			surfaceDim = p.getValue("surfaceDim"),
			surfaceBright = p.getValue("surfaceBright"),
			surfaceContainerLowest = if (amoled) AMOLED_BACKGROUND else p.getValue("surfaceContainerLowest"),
			surfaceContainerLow = p.getValue("surfaceContainerLow"),
			surfaceContainer = p.getValue("surfaceContainer"),
			surfaceContainerHigh = p.getValue("surfaceContainerHigh"),
			surfaceContainerHighest = p.getValue("surfaceContainerHighest"),
			error = p.getValue("error"),
			onError = p.getValue("onError"),
			errorContainer = p.getValue("errorContainer"),
			onErrorContainer = p.getValue("onErrorContainer"),
			surfaceTint = p.getOrDefault("surfaceTint", p.getValue("primary")),
			scrim = DEFAULT_SCRIM,
		)
	} else {
		lightColorScheme(
			primary = p.getValue("primary"),
			onPrimary = p.getValue("onPrimary"),
			primaryContainer = p.getValue("primaryContainer"),
			onPrimaryContainer = p.getValue("onPrimaryContainer"),
			inversePrimary = p.getValue("inversePrimary"),
			secondary = p.getValue("secondary"),
			onSecondary = p.getValue("onSecondary"),
			secondaryContainer = p.getValue("secondaryContainer"),
			onSecondaryContainer = p.getValue("onSecondaryContainer"),
			tertiary = p.getValue("tertiary"),
			onTertiary = p.getValue("onTertiary"),
			tertiaryContainer = p.getValue("tertiaryContainer"),
			onTertiaryContainer = p.getValue("onTertiaryContainer"),
			background = p.getValue("background"),
			onBackground = p.getValue("onBackground"),
			surface = p.getValue("surface"),
			onSurface = p.getValue("onSurface"),
			surfaceVariant = p.getValue("surfaceVariant"),
			onSurfaceVariant = p.getValue("onSurfaceVariant"),
			outline = p.getValue("outline"),
			outlineVariant = p.getOrDefault("outlineVariant", FALLBACK_OUTLINE_VARIANT_LIGHT),
			inverseSurface = p.getValue("inverseSurface"),
			inverseOnSurface = p.getValue("inverseOnSurface"),
			surfaceDim = p.getValue("surfaceDim"),
			surfaceBright = p.getValue("surfaceBright"),
			surfaceContainerLowest = p.getValue("surfaceContainerLowest"),
			surfaceContainerLow = p.getValue("surfaceContainerLow"),
			surfaceContainer = p.getValue("surfaceContainer"),
			surfaceContainerHigh = p.getValue("surfaceContainerHigh"),
			surfaceContainerHighest = p.getValue("surfaceContainerHighest"),
			error = p.getValue("error"),
			onError = p.getValue("onError"),
			errorContainer = p.getValue("errorContainer"),
			onErrorContainer = p.getValue("onErrorContainer"),
			surfaceTint = p.getOrDefault("surfaceTint", p.getValue("primary")),
			scrim = DEFAULT_SCRIM,
		)
	}
}

/**
 * Mode de thème, identique aux choix de l'app d'origine (paramètres d'apparence) :
 * système / clair / sombre, avec option AMOLED en sombre.
 */
public enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
public fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
	ThemeMode.SYSTEM -> isSystemInDarkTheme()
	ThemeMode.LIGHT -> false
	ThemeMode.DARK -> true
}

@Composable
public fun KotatsuTheme(
	scheme: String,
	mode: ThemeMode,
	amoled: Boolean,
	content: @Composable () -> Unit,
) {
	val dark = isDarkTheme(mode)
	MaterialTheme(
		colorScheme = kotatsuColorScheme(scheme, dark, amoled && dark),
		content = content,
	)
}
