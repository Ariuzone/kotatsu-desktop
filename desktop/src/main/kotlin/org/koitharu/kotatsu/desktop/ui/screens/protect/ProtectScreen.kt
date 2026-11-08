/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : portage Compose de ProtectActivity + activity_protect.xml (texts exacts de
 * strings.xml : app_name « Kotatsu », enter_password, wrong_password, next ; bouton cancel
 * d'Android = « Cancel »). La biométrie et FLAG_SECURE sont absents (API Android) ;
 * le délai anti-bruteforce est dans ProtectManager.tryUnlock.
 * Modification : 2026-11-08 — centralisation des chaînes (règle client : aucune chaîne en brut
 * dans les écrans).
 */

package org.koitharu.kotatsu.desktop.ui.screens.protect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_CANCEL
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_NEXT
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_SUBTITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_TITLE
import org.koitharu.kotatsu.desktop.ui.strings.FR_PROTECT_WRONG_PASSWORD

/**
 * Écran de déverrouillage (ProtectActivity / activity_protect.xml) : titre, sous-titre,
 * champ mot de passe, boutons Annuler/Suivant, message d'erreur exact.
 * [manager] porte la logique (verrou, hash, délai anti-bruteforce) ; l'hôte fournit
 * [onUnlocked] / [onCancel].
 */
@Composable
fun ProtectScreen(
	manager: ProtectManager,
	onUnlocked: () -> Unit,
	onCancel: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var password by rememberSaveable { mutableStateOf("") }
	var isError by remember { mutableStateOf(false) }
	var isLoading by remember { mutableStateOf(false) }
	var passwordVisible by rememberSaveable { mutableStateOf(false) }
	val scope = rememberCoroutineScope()
	val focusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) { focusRequester.requestFocus() }

	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
	) {
		// textView_title (app_name)
		Text(text = FR_PROTECT_TITLE, style = MaterialTheme.typography.headlineMedium)
		// textView_subtitle (enter_password)
		Text(
			text = FR_PROTECT_SUBTITLE,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)

		// layout_password / edit_password
		OutlinedTextField(
			value = password,
			onValueChange = {
				password = it
				isError = false // DefaultTextWatcher.afterTextChanged : efface l'erreur
			},
			modifier = Modifier
				.fillMaxWidth(0.5f)
				.focusRequester(focusRequester),
			label = { Text(FR_PROTECT_SUBTITLE) },
			singleLine = true,
			enabled = !isLoading, // onLoadingStateChanged : layout désactivé pendant la vérification
			isError = isError,
			keyboardOptions = KeyboardOptions(
				keyboardType = if (manager.isNumericPassword) KeyboardType.NumberPassword else KeyboardType.Password,
			),
			visualTransformation = if (passwordVisible) {
				VisualTransformation.None
			} else {
				PasswordVisualTransformation()
			},
			trailingIcon = {
				IconButton(onClick = { passwordVisible = !passwordVisible }) {
					Icon(
						imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
						contentDescription = null,
					)
				}
			},
			supportingText = if (isError) {
				{ Text(FR_PROTECT_WRONG_PASSWORD, color = MaterialTheme.colorScheme.error) }
			} else {
				null
			},
		)

		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			// button_cancel (android:string/cancel)
			OutlinedButton(onClick = onCancel) {
				Text(FR_PROTECT_CANCEL)
			}
			// button_next (next) — actif seulement si le champ n'est pas vide (comme l'original)
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				onClick = {
					isLoading = true
					scope.launch {
						isError = !manager.tryUnlock(password)
						isLoading = false
						if (!isError) onUnlocked()
					}
				},
				enabled = password.isNotEmpty() && !isLoading,
			) {
				Text(FR_PROTECT_NEXT)
			}
		}
	}
}
