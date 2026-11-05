/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Écran « Paramètres → Apparence » — reproduit les préférences d'apparence de l'app d'origine
 * (pref_appearance + AppearanceSettingsFragment) : thème (Follow system / Light / Dark),
 * AMOLED, schéma de couleurs (noms exacts : Totoro, Miku, Asuka, Mion, Rikka, Sakura, Mamimi,
 * Kanade, Itsuka). Les valeurs dynamiques (Monet/Expressive) tombent sur le schéma par défaut
 * sur desktop — voir docs/UI_DIVERGENCES.md.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.settings.DesktopSettings
import org.koitharu.kotatsu.desktop.ui.theme.AVAILABLE_SCHEMES
import org.koitharu.kotatsu.desktop.ui.theme.ThemeMode

private val SCHEME_TITLES = mapOf(
	"DEFAULT" to "Totoro",
	"ASUKA" to "Asuka",
	"MION" to "Mion",
	"RIKKA" to "Rikka",
	"SAKURA" to "Sakura",
	"MAMIMI" to "Mamimi",
	"KANADE" to "Kanade",
	"ITSUKA" to "Itsuka",
	"MIKU" to "Miku",
)

@Composable
public fun AppearanceSettingsScreen(
	settings: DesktopSettings,
	modifier: Modifier = Modifier,
) {
	LazyColumn(modifier = modifier.fillMaxSize()) {
		item {
			Text(
				text = "Appearance",
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(16.dp),
			)
		}
		item {
			Text(
				text = "Theme",
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)
		}
		items(listOf("SYSTEM", "LIGHT", "DARK")) { mode ->
			val selected = settings.themeMode.value == ThemeMode.valueOf(mode)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { settings.setThemeMode(ThemeMode.valueOf(mode)) }
					.padding(horizontal = 16.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				RadioButton(selected = selected, onClick = { settings.setThemeMode(ThemeMode.valueOf(mode)) })
				Text(
					text = when (mode) {
						"SYSTEM" -> "Follow system"
						"LIGHT" -> "Light"
						else -> "Dark"
					},
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}
		item {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = "AMOLED theme",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.weight(1f),
				)
				Switch(
					checked = settings.isAmoled.value,
					onCheckedChange = { settings.setAmoled(it) },
				)
			}
			HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
		}
		item {
			Text(
				text = "Color scheme",
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)
		}
		items(AVAILABLE_SCHEMES) { scheme ->
			val selected = settings.colorScheme.value == scheme
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { settings.setColorScheme(scheme) }
					.padding(horizontal = 16.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				RadioButton(selected = selected, onClick = { settings.setColorScheme(scheme) })
				Text(
					text = SCHEME_TITLES[scheme] ?: scheme,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}
	}
}
