/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : SharedPreferences Android remplacé par KeyValueStore (fichier JSON par source,
 * créé par [storeProvider]) ; DomainValidator Android remplacé par une validation locale simple ;
 * OnSharedPreferenceChangeListener remplacé par KeyValueStore.observeChanges().
 */
package org.koitharu.kotatsu.core.prefs

import kotlinx.coroutines.flow.Flow
import org.koitharu.kotatsu.core.util.ext.sanitizeHeaderValue
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.config.MangaSourceConfig
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.ifNullOrEmpty
import org.koitharu.kotatsu.parsers.util.nullIfEmpty

fun interface SourceSettingsStoreProvider {

	/** Renvoie (et crée au besoin) le KeyValueStore dédié à [source]. */
	operator fun invoke(source: MangaSource): KeyValueStore
}

class SourceSettings(
	store: KeyValueStore,
	val source: MangaSource,
) : MangaSourceConfig {

	private val prefs: KeyValueStore = store

	var defaultSortOrder: SortOrder?
		get() = prefs.getEnumValue(KEY_SORT_ORDER, SortOrder::class.java, null)
		set(value) = prefs.edit { putString(KEY_SORT_ORDER, value?.name) }

	val isSlowdownEnabled: Boolean
		get() = prefs.getBoolean(KEY_SLOWDOWN, false)

	val isCaptchaNotificationsDisabled: Boolean
		get() = prefs.getBoolean(KEY_NO_CAPTCHA, false)

	val isCaptchaAutoResolveDisabled: Boolean
		get() = prefs.getBoolean(KEY_NO_AUTO_CAPTCHA, false)

	/**
	 * The User-Agent that actually earned this source's current `cf_clearance` cookie.
	 *
	 * Cloudflare only honours a clearance for the exact agent it was issued to. Set by the solver,
	 * applied by `CommonHeadersInterceptor`; null means the parser's own agent is fine.
	 */
	var cloudFlareUserAgent: String?
		get() = prefs.getString(KEY_CF_USER_AGENT, null)?.nullIfEmpty()
		set(value) = prefs.edit { putString(KEY_CF_USER_AGENT, value?.sanitizeHeaderValue()) }

	@Suppress("UNCHECKED_CAST")
	override fun <T> get(key: ConfigKey<T>): T {
		return when (key) {
			is ConfigKey.UserAgent -> prefs.getString(key.key, key.defaultValue)
				.ifNullOrEmpty { key.defaultValue }
				.sanitizeHeaderValue()

			is ConfigKey.Domain -> prefs.getString(key.key, key.defaultValue)
				?.trim()
				?.takeIf { isValidDomain(it) }
				?: key.defaultValue

			is ConfigKey.ShowSuspiciousContent -> prefs.getBoolean(key.key, key.defaultValue)
			is ConfigKey.SplitByTranslations -> prefs.getBoolean(key.key, key.defaultValue)
			is ConfigKey.PreferredImageServer -> prefs.getString(key.key, key.defaultValue)?.nullIfEmpty()
			is ConfigKey.DisableUpdateChecking -> prefs.getBoolean(key.key, key.defaultValue)
			is ConfigKey.InterceptCloudflare -> prefs.getBoolean(key.key, key.defaultValue)
		} as T
	}

	operator fun <T> set(key: ConfigKey<T>, value: T) = prefs.edit {
		when (key) {
			is ConfigKey.Domain -> putString(key.key, value as String?)
			is ConfigKey.ShowSuspiciousContent -> putBoolean(key.key, value as Boolean)
			is ConfigKey.UserAgent -> putString(key.key, (value as String?)?.sanitizeHeaderValue())
			is ConfigKey.SplitByTranslations -> putBoolean(key.key, value as Boolean)
			is ConfigKey.PreferredImageServer -> putString(key.key, value as String? ?: "")
			is ConfigKey.InterceptCloudflare -> putBoolean(key.key, value as Boolean)
			is ConfigKey.DisableUpdateChecking -> {
				// Read-only - parser-controlled only, users cannot change this
			}
		}
	}

	fun observeChanges(): Flow<String?> = prefs.observeChanges()

	companion object {

		const val KEY_DOMAIN = "domain"
		const val KEY_NO_CAPTCHA = "no_captcha"
		const val KEY_NO_AUTO_CAPTCHA = "no_auto_captcha"
		const val KEY_SLOWDOWN = "slowdown"
		const val KEY_SORT_ORDER = "sort_order"
		const val KEY_CF_USER_AGENT = "cf_user_agent"

		// Équivalent desktop de settings.utils.validation.DomainValidator :
		// hostname simple (labels alphanumériques + tirets, TLD d'au moins 2 lettres) ou adresse IP.
		private val DOMAIN_REGEX = Regex(
			"^(localhost|(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z]{2,63}|(?:\\d{1,3}\\.){3}\\d{1,3}|\\[[0-9a-f:]+])$",
			RegexOption.IGNORE_CASE,
		)

		fun isValidDomain(value: String): Boolean = value.isNotEmpty() && DOMAIN_REGEX.matches(value)
	}
}
