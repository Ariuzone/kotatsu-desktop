/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-08 — phase P5 : écran
 * « Réseau » (parité UI FR avec l'app Android) : section « Données mobiles » (téléchargement en
 * données mobiles via TriStateOption — Toujours / Demander / Jamais —, suggestions et
 * vérification des nouveaux chapitres en Wi-Fi seulement), section « Proxy » (type en dialog
 * radio Direct / HTTP / SOCKS, adresse, port validé numérique, identifiant, mot de passe masqué)
 * et section « DNS sur HTTPS » (DoHProvider). Préférences via AppSettings (allowDownloadOnMeteredNetwork,
 * isSuggestionsWiFiOnly, isTrackerWiFiOnly, proxyType/Address/Port/Login/Password, dnsOverHttps),
 * toutes mutables. Le réseau est reconstruit au démarrage : note indiquant que les réglages
 * s'appliquent après redémarrage.
 */
package org.koitharu.kotatsu.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.core.network.DoHProvider
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.TriStateOption
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceDivider
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceSwitchRow
import org.koitharu.kotatsu.desktop.ui.components.preferences.PreferenceCategoryHeader
import org.koitharu.kotatsu.desktop.ui.components.preferences.RadioDialog
import org.koitharu.kotatsu.desktop.ui.screens.settings.root.SettingsTopBar
import java.net.Proxy

// Chaînes FR de l'écran « Réseau »
private const val FR_TITLE = "Réseau"
private const val FR_SECTION_METERED = "Données mobiles"
private const val FR_DOWNLOAD_METERED = "Télécharger en données mobiles"
private const val FR_SUGGESTIONS_WIFI_ONLY = "Suggestions en Wi-Fi seulement"
private const val FR_TRACKER_WIFI_ONLY = "Vérification des nouveaux chapitres en Wi-Fi seulement"
private const val FR_SECTION_PROXY = "Proxy"
private const val FR_PROXY_TYPE = "Type de proxy"
private const val FR_PROXY_ADDRESS = "Adresse"
private const val FR_PROXY_PORT = "Port"
private const val FR_PROXY_LOGIN = "Identifiant"
private const val FR_PROXY_PASSWORD = "Mot de passe"
private const val FR_PROXY_EMPTY = "—"
private const val FR_PROXY_PASSWORD_MASK = "••••••••"
private const val FR_SECTION_DOH = "DNS sur HTTPS"
private const val FR_DOH = "Serveur DNS sur HTTPS"
private const val FR_DOH_DISABLED = "Désactivé"
private const val FR_RESTART_NOTE =
	"Ces réglages s'appliquent après redémarrage de l'application."
private const val FR_OK = "OK"
private const val FR_CANCEL = "Annuler"
private const val FR_PROXY_HOST_HINT = "Hôte ou adresse IP"
private const val FR_PROXY_PORT_HINT = "0 - 65535"
private const val FR_PROXY_LOGIN_HINT = "Identifiant"
private const val FR_PROXY_PASSWORD_HINT = "Mot de passe"

// Libellés FR du TriStateOption (Télécharger en données mobiles). L'enum d'origine est
// ENABLED / ASK / DISABLED : « Wi-Fi seulement » n'existe pas dans ce tri-état, ASK = « Demander ».
private const val FR_TRISTATE_ENABLED = "Toujours"
private const val FR_TRISTATE_ASK = "Demander"
private const val FR_TRISTATE_DISABLED = "Jamais"

private val NOTE_ICON_SIZE = 20.dp
private val NOTE_HORIZONTAL_PADDING = 16.dp
private val NOTE_VERTICAL_PADDING = 12.dp

/** Libellé FR d'une valeur de [TriStateOption] (dialog « Télécharger en données mobiles »). */
private fun frTriStateLabel(option: TriStateOption): String = when (option) {
	TriStateOption.ENABLED -> FR_TRISTATE_ENABLED
	TriStateOption.ASK -> FR_TRISTATE_ASK
	TriStateOption.DISABLED -> FR_TRISTATE_DISABLED
}

/** Libellé FR d'un type de proxy (java.net.Proxy.Type : DIRECT / HTTP / SOCKS). */
private fun frProxyTypeLabel(type: Proxy.Type): String = when (type) {
	Proxy.Type.DIRECT -> "Direct"
	Proxy.Type.HTTP -> "HTTP"
	Proxy.Type.SOCKS -> "SOCKS"
}

/** Libellé FR d'un serveur DNS sur HTTPS (enum DoHProvider, ordre d'origine conservé). */
private fun frDoHLabel(provider: DoHProvider): String = when (provider) {
	DoHProvider.NONE -> FR_DOH_DISABLED
	DoHProvider.GOOGLE -> "Google"
	DoHProvider.CLOUDFLARE -> "Cloudflare"
	DoHProvider.ADGUARD -> "AdGuard"
	DoHProvider.ZERO_MS -> "0ms"
}

/** Port valide : vide (= aucun, 0) ou entier entre 0 et 65535. */
private fun isValidPort(input: String): Boolean {
	val trimmed = input.trim()
	if (trimmed.isEmpty()) return true
	return trimmed.toIntOrNull()?.let { it in 0..65535 } ?: false
}

/**
 * Écran « Réseau » (Paramètres > Réseau) : données mobiles, proxy et DNS sur HTTPS.
 * Tous les réglages sont modifiables (écriture immédiate via AppSettings) ; le réseau
 * n'est reconstruit qu'au redémarrage de l'application (note en bas d'écran).
 */
@Composable
public fun NetworkSettingsScreen(
	appSettings: AppSettings,
	onBackClick: () -> Unit,
) {
	// États locaux miroir des préférences (AppSettings n'est pas observable) ; écriture immédiate
	var downloadMetered by remember { mutableStateOf(appSettings.allowDownloadOnMeteredNetwork) }
	var suggestionsWiFiOnly by remember { mutableStateOf(appSettings.isSuggestionsWiFiOnly) }
	var trackerWiFiOnly by remember { mutableStateOf(appSettings.isTrackerWiFiOnly) }
	var proxyType by remember { mutableStateOf(appSettings.proxyType) }
	var proxyAddress by remember { mutableStateOf(appSettings.proxyAddress) }
	var proxyPort by remember { mutableStateOf(appSettings.proxyPort) }
	var proxyLogin by remember { mutableStateOf(appSettings.proxyLogin) }
	var proxyPassword by remember { mutableStateOf(appSettings.proxyPassword) }
	var dnsOverHttps by remember { mutableStateOf(appSettings.dnsOverHttps) }
	var openDialog by remember { mutableStateOf<NetworkDialog?>(null) }

	Column(modifier = Modifier.fillMaxSize()) {
		SettingsTopBar(
			title = FR_TITLE,
			onBackClick = onBackClick,
			onSearchClick = null,
		)
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			// 1. Section « Données mobiles »
			PreferenceCategoryHeader(title = FR_SECTION_METERED)

			// Tri-état modifiable : Toujours / Demander / Jamais (TriStateOption)
			PreferenceRow(
				icon = Icons.Outlined.Download,
				title = FR_DOWNLOAD_METERED,
				subtitle = frTriStateLabel(downloadMetered),
				onClick = { openDialog = NetworkDialog.DOWNLOAD_METERED },
			)
			PreferenceDivider()

			// KEY_SUGGESTIONS_WIFI_ONLY (var)
			PreferenceSwitchRow(
				icon = Icons.Outlined.Wifi,
				title = FR_SUGGESTIONS_WIFI_ONLY,
				checked = suggestionsWiFiOnly,
				onCheckedChange = {
					suggestionsWiFiOnly = it
					appSettings.isSuggestionsWiFiOnly = it
				},
			)
			PreferenceDivider()

			// KEY_TRACKER_WIFI_ONLY (var)
			PreferenceSwitchRow(
				icon = Icons.Outlined.Newspaper,
				title = FR_TRACKER_WIFI_ONLY,
				checked = trackerWiFiOnly,
				onCheckedChange = {
					trackerWiFiOnly = it
					appSettings.isTrackerWiFiOnly = it
				},
			)

			// 2. Section « Proxy »
			PreferenceCategoryHeader(title = FR_SECTION_PROXY)

			PreferenceRow(
				icon = Icons.Outlined.VpnKey,
				title = FR_PROXY_TYPE,
				subtitle = frProxyTypeLabel(proxyType),
				onClick = { openDialog = NetworkDialog.PROXY_TYPE },
			)
			PreferenceDivider()
			PreferenceRow(
				icon = Icons.Outlined.Language,
				title = FR_PROXY_ADDRESS,
				subtitle = proxyAddress?.takeIf { it.isNotEmpty() } ?: FR_PROXY_EMPTY,
				onClick = { openDialog = NetworkDialog.PROXY_ADDRESS },
			)
			PreferenceDivider()
			PreferenceRow(
				icon = Icons.Outlined.Settings,
				title = FR_PROXY_PORT,
				subtitle = proxyPort.takeIf { it != 0 }?.toString() ?: FR_PROXY_EMPTY,
				onClick = { openDialog = NetworkDialog.PROXY_PORT },
			)
			PreferenceDivider()
			PreferenceRow(
				icon = Icons.Outlined.Settings,
				title = FR_PROXY_LOGIN,
				subtitle = proxyLogin ?: FR_PROXY_EMPTY,
				onClick = { openDialog = NetworkDialog.PROXY_LOGIN },
			)
			PreferenceDivider()
			PreferenceRow(
				icon = Icons.Outlined.Settings,
				title = FR_PROXY_PASSWORD,
				subtitle = if (proxyPassword == null) FR_PROXY_EMPTY else FR_PROXY_PASSWORD_MASK,
				onClick = { openDialog = NetworkDialog.PROXY_PASSWORD },
			)

			// 3. Section « DNS sur HTTPS » (appliqué au redémarrage)
			PreferenceCategoryHeader(title = FR_SECTION_DOH)

			PreferenceRow(
				icon = Icons.Outlined.Dns,
				title = FR_DOH,
				subtitle = frDoHLabel(dnsOverHttps),
				onClick = { openDialog = NetworkDialog.DOH },
			)

			// Note locale : le réseau est reconstruit au démarrage de l'application
			InfoNote(text = FR_RESTART_NOTE)
		}
	}

	when (openDialog) {
		NetworkDialog.DOWNLOAD_METERED -> RadioDialog(
			title = FR_DOWNLOAD_METERED,
			options = TriStateOption.entries.map { frTriStateLabel(it) },
			selectedIndex = TriStateOption.entries.indexOf(downloadMetered),
			onDismiss = { openDialog = null },
			onSelect = {
				downloadMetered = TriStateOption.entries[it]
				appSettings.allowDownloadOnMeteredNetwork = downloadMetered
			},
		)
		NetworkDialog.DOH -> RadioDialog(
			title = FR_DOH,
			options = DoHProvider.entries.map { frDoHLabel(it) },
			selectedIndex = DoHProvider.entries.indexOf(dnsOverHttps),
			onDismiss = { openDialog = null },
			onSelect = {
				dnsOverHttps = DoHProvider.entries[it]
				appSettings.dnsOverHttps = dnsOverHttps
			},
		)
		NetworkDialog.PROXY_TYPE -> RadioDialog(
			title = FR_PROXY_TYPE,
			options = Proxy.Type.entries.map { frProxyTypeLabel(it) },
			selectedIndex = Proxy.Type.entries.indexOf(proxyType),
			onDismiss = { openDialog = null },
			onSelect = {
				proxyType = Proxy.Type.entries[it]
				appSettings.proxyType = proxyType
			},
		)
		NetworkDialog.PROXY_ADDRESS -> TextInputDialog(
			title = FR_PROXY_ADDRESS,
			hint = FR_PROXY_HOST_HINT,
			initial = proxyAddress.orEmpty(),
			onDismiss = { openDialog = null },
			onConfirm = { value ->
				proxyAddress = value.ifEmpty { null }
				appSettings.proxyAddress = proxyAddress
			},
		)
		NetworkDialog.PROXY_PORT -> TextInputDialog(
			title = FR_PROXY_PORT,
			hint = FR_PROXY_PORT_HINT,
			initial = proxyPort.takeIf { it != 0 }?.toString().orEmpty(),
			keyboardNumeric = true,
			isValid = ::isValidPort,
			onDismiss = { openDialog = null },
			onConfirm = { value ->
				proxyPort = value.trim().toIntOrNull() ?: 0
				appSettings.proxyPort = proxyPort
			},
		)
		NetworkDialog.PROXY_LOGIN -> TextInputDialog(
			title = FR_PROXY_LOGIN,
			hint = FR_PROXY_LOGIN_HINT,
			initial = proxyLogin.orEmpty(),
			onDismiss = { openDialog = null },
			onConfirm = { value ->
				proxyLogin = value.ifEmpty { null }
				appSettings.proxyLogin = proxyLogin
			},
		)
		NetworkDialog.PROXY_PASSWORD -> TextInputDialog(
			title = FR_PROXY_PASSWORD,
			hint = FR_PROXY_PASSWORD_HINT,
			initial = proxyPassword.orEmpty(),
			maskInput = true,
			onDismiss = { openDialog = null },
			onConfirm = { value ->
				proxyPassword = value.ifEmpty { null }
				appSettings.proxyPassword = proxyPassword
			},
		)
		null -> Unit
	}
}

/**
 * Dialog de saisie texte (adresse, port, identifiant, mot de passe) : champ sur une ligne,
 * validation optionnelle ([isValid] — « OK » inactif tant que la saisie est invalide) et
 * masquage optionnel ([maskInput], mot de passe). Saisie vide confirmée = champ vidé.
 */
@Composable
private fun TextInputDialog(
	title: String,
	hint: String,
	initial: String,
	modifier: Modifier = Modifier,
	keyboardNumeric: Boolean = false,
	maskInput: Boolean = false,
	isValid: (String) -> Boolean = { true },
	onDismiss: () -> Unit,
	onConfirm: (String) -> Unit,
) {
	var input by remember { mutableStateOf(initial) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = title) },
		text = {
			OutlinedTextField(
				value = input,
				onValueChange = { input = it },
				singleLine = true,
				label = { Text(text = hint) },
				isError = !isValid(input),
				visualTransformation = if (maskInput) {
					PasswordVisualTransformation()
				} else {
					VisualTransformation.None
				},
				keyboardOptions = KeyboardOptions(
					keyboardType = if (keyboardNumeric) KeyboardType.Number else KeyboardType.Text,
				),
				modifier = Modifier.fillMaxWidth(),
			)
		},
		confirmButton = {
			TextButton(
				enabled = isValid(input),
				onClick = {
					onConfirm(input.trim())
					onDismiss()
				},
			) {
				Text(text = FR_OK)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = FR_CANCEL)
			}
		},
		modifier = modifier,
	)
}

/** Note d'information en bas de section (icône i, texte secondaire). */
@Composable
private fun InfoNote(text: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = NOTE_HORIZONTAL_PADDING, vertical = NOTE_VERTICAL_PADDING),
		verticalAlignment = Alignment.Top,
	) {
		Icon(
			imageVector = Icons.Outlined.Info,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(top = 2.dp),
		)
		Text(
			text = text,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(start = 12.dp),
		)
	}
}

/** Dialogs ouverts de l'écran Réseau. */
private enum class NetworkDialog {
	DOWNLOAD_METERED,
	DOH,
	PROXY_TYPE,
	PROXY_ADDRESS,
	PROXY_PORT,
	PROXY_LOGIN,
	PROXY_PASSWORD,
}
