/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation Compose Desktop.
 *
 * Préférences applicatives du desktop (thème/apparence en P1). Sera fusionnée avec
 * core/prefs/AppSettings lors du port complet ; les clés reprennent celles de l'app d'origine
 * (cf. AppSettings.kt : KEY_THEME_MODE, KEY_IS_AMOLED, KEY_COLOR_SCHEME).
 */
package org.koitharu.kotatsu.desktop.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koitharu.kotatsu.desktop.ui.theme.ThemeMode
import java.io.File

@Serializable
private data class PersistedSettings(
	val themeMode: String = "SYSTEM",
	val isAmoled: Boolean = false,
	val colorScheme: String = "DEFAULT",
)

/**
 * Équivalent desktop des préférences d'apparence. Stockage : fichier JSON dans
 * %APPDATA%/Kotatsu (Windows), comme SharedPreferences côté Android.
 */
public class DesktopSettings private constructor() {

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val json = Json { ignoreUnknownKeys = true }

	private val file: File by lazy {
		val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
		File(appData, "Kotatsu").apply { mkdirs() }.resolve("settings.json")
	}

	private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
	private val _isAmoled = mutableStateOf(false)
	private val _colorScheme = mutableStateOf("DEFAULT")

	public val themeMode: State<ThemeMode> get() = _themeMode
	public val isAmoled: State<Boolean> get() = _isAmoled
	public val colorScheme: State<String> get() = _colorScheme

	init {
		scope.launch { load() }
	}

	private suspend fun load() {
		val persisted = runCatching {
			if (file.isFile) json.decodeFromString<PersistedSettings>(file.readText()) else PersistedSettings()
		}.getOrDefault(PersistedSettings())
		_themeMode.value = runCatching { ThemeMode.valueOf(persisted.themeMode) }.getOrDefault(ThemeMode.SYSTEM)
		_isAmoled.value = persisted.isAmoled
		_colorScheme.value = persisted.colorScheme
	}

	public fun setThemeMode(mode: ThemeMode) {
		_themeMode.value = mode
		persist()
	}

	public fun setAmoled(enabled: Boolean) {
		_isAmoled.value = enabled
		persist()
	}

	public fun setColorScheme(scheme: String) {
		_colorScheme.value = scheme
		persist()
	}

	private fun persist() {
		val snapshot = PersistedSettings(
			themeMode = _themeMode.value.name,
			isAmoled = _isAmoled.value,
			colorScheme = _colorScheme.value,
		)
		scope.launch {
			runCatching { file.writeText(json.encodeToString(snapshot)) }
		}
	}

	public companion object {

		@Volatile
		private var instance: DesktopSettings? = null

		public fun getInstance(): DesktopSettings =
			instance ?: synchronized(this) { instance ?: DesktopSettings().also { instance = it } }
	}
}
