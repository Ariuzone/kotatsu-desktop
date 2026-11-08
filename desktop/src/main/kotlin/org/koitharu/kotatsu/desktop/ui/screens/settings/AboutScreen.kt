/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « À propos » (parité UI FR avec l'app Android) : logo discret + nom + version (copie au
 * presse-papiers via java.awt.datatransfer), licence GNU GPL v3, projets d'origine Kotatsu /
 * Kotatsu-Redo avec liens ouverts dans le navigateur via java.awt.Desktop.browse (protégé par
 * runCatching si Desktop n'est pas supporté) et mention des sources kotatsu-parsers.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import java.awt.Desktop
import java.net.URI

// Chaînes FR de l'écran « À propos »
private const val FR_TITLE = "À propos"
private const val FR_APP_NAME = "Kotatsu Desktop"
private const val FR_VERSION = "Version 9.8.6"
private const val FR_COPIED = "Copié dans le presse-papiers"
private const val FR_LICENSE = "Licence : GNU GPL v3"
private const val FR_ORIGIN_PROJECT = "Projet d'origine : Kotatsu / Kotatsu-Redo"
private const val FR_LINK_KOTATSU_REDO = "Kotatsu-Redo (GitHub)"
private const val FR_LINK_KOTATSU = "Kotatsu (GitHub)"
private const val FR_SOURCES_PARSERS = "Sources : kotatsu-parsers"

private const val URL_KOTATSU_REDO = "https://github.com/Kotatsu-Redo/Kotatsu-Redo"
private const val URL_KOTATSU = "https://github.com/KotatsuApp/Kotatsu"

private val LOGO_CORNER_RADIUS = 16.dp
private val LOGO_SIZE = 48.dp
private val SCREEN_HORIZONTAL_PADDING = 16.dp
private val LINK_ICON_SIZE = 20.dp

/** Ouvre [url] dans le navigateur système ; no-op silencieux si Desktop n'est pas supporté. */
private fun openInBrowser(url: String) {
	runCatching {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(URI(url))
		}
	}
}

/** Copie [text] dans le presse-papiers système (AWT) ; no-op silencieux en cas d'échec. */
private fun copyToClipboard(text: String): Boolean = runCatching {
	val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
	clipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
}.isSuccess

/**
 * Écran « À propos » (Paramètres > À propos) : identité de l'application, version copiable,
 * licence, projets d'origine et liens cliquables.
 */
@Composable
public fun AboutScreen(
	onBackClick: () -> Unit,
) {
	var versionCopied by remember { mutableStateOf(false) }
	val clipboardManager = LocalClipboardManager.current

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_TITLE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = SCREEN_HORIZONTAL_PADDING),
		) {
			// 1. Logo discret + nom + version (clic = copie de la version)
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 24.dp),
			) {
				Surface(
					shape = RoundedCornerShape(LOGO_CORNER_RADIUS),
					color = MaterialTheme.colorScheme.primaryContainer,
				) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.Redo,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onPrimaryContainer,
						modifier = Modifier
							.padding(12.dp)
							.size(LOGO_SIZE),
					)
				}
				Text(
					text = FR_APP_NAME,
					style = MaterialTheme.typography.headlineSmall,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(top = 12.dp),
				)
				Text(
					text = if (versionCopied) FR_COPIED else FR_VERSION,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.padding(top = 4.dp)
						.clickable {
							// Presse-papiers Compose en priorité ; repli AWT si nécessaire
							val copied = runCatching {
								clipboardManager.setText(AnnotatedString(FR_VERSION))
								true
							}.getOrDefault(false) || copyToClipboard(FR_VERSION)
							versionCopied = copied
						},
				)
			}

			HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

			// 2. Licence et projets d'origine
			AboutLine(icon = Icons.Outlined.Info, text = FR_LICENSE)
			AboutLine(icon = Icons.AutoMirrored.Filled.Redo, text = FR_ORIGIN_PROJECT)

			// 3. Liens cliquables (navigateur système, protégés par runCatching)
			AboutLink(text = FR_LINK_KOTATSU_REDO, url = URL_KOTATSU_REDO)
			AboutLink(text = FR_LINK_KOTATSU, url = URL_KOTATSU)

			HorizontalDivider(
				color = MaterialTheme.colorScheme.outlineVariant,
				modifier = Modifier.padding(top = 8.dp),
			)

			// 4. Remerciements : parsers
			AboutLine(icon = Icons.Outlined.Info, text = FR_SOURCES_PARSERS)

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

/** Ligne d'information simple (icône + texte). */
@Composable
private fun AboutLine(
	icon: ImageVector,
	text: String,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(LINK_ICON_SIZE),
		)
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(start = 16.dp),
		)
	}
}

/** Lien cliquable ouvrant [url] dans le navigateur système. */
@Composable
private fun AboutLink(
	text: String,
	url: String,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { openInBrowser(url) }
			.padding(vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			imageVector = Icons.Outlined.Info,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(LINK_ICON_SIZE),
		)
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.padding(start = 16.dp),
		)
	}
}
