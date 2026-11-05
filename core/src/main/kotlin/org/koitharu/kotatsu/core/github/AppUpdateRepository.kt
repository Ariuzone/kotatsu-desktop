/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : Retrofit n'est pas disponible ; l'original utilisait déjà OkHttp, mais le parsing
 * org.json (Android) est remplacé par kotlinx.serialization. Le dépôt GitHub est paramétrable ;
 * l'asset APK est remplacé par n'importe quel asset d'installateur desktop (ou l'URL de la release).
 */
package org.koitharu.kotatsu.core.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koitharu.kotatsu.core.util.ext.asArrayList
import org.koitharu.kotatsu.core.util.ext.ensureSuccess
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.parsers.util.await
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable

class AppUpdateRepository(
	private val settings: AppSettingsPlaceholder?,
	private val okHttp: OkHttpClient,
	private val updatesRepo: String = DEFAULT_UPDATES_REPO,
) {

	/** Placeholder pour compatibilité : l'original reçoit AppSettings (préférence updates_unstable). */
	class AppSettingsPlaceholder(val isUnstableUpdatesAllowed: Boolean = false)

	private val json = Json { ignoreUnknownKeys = true }

	private val availableUpdate = MutableStateFlow<AppVersion?>(null)
	private val releasesUrl = buildString {
		append("https://api.github.com/repos/")
		append(updatesRepo)
		append("/releases?page=1&per_page=10")
	}

	val isUpdateAvailable: Boolean
		get() = availableUpdate.value != null

	fun observeAvailableUpdate() = availableUpdate.asStateFlow()

	suspend fun getAvailableVersions(): List<AppVersion> = withContext(Dispatchers.Default) {
		val request = Request.Builder()
			.get()
			.url(releasesUrl)
		val response = okHttp.newCall(request.build()).await().ensureSuccess()
		val body = response.use { it.body?.string() } ?: return@withContext emptyList()
		val releases = json.parseToJsonElement(body).jsonArray

		releases.mapNotNull { release ->
			val obj = release.jsonObject
			val releaseName = obj["name"]?.jsonPrimitive?.content
			val releaseTag = obj["tag_name"]?.jsonPrimitive?.content ?: return@mapNotNull null
			val assets = obj["assets"] as? JsonArray
			val asset = assets?.firstOrNull { assetObj ->
				(assetObj.jsonObject["content_type"]?.jsonPrimitive?.content)
					?.equals(CONTENT_TYPE_EXE, ignoreCase = true) == true
			} ?: assets?.firstOrNull { assetObj ->
				(assetObj.jsonObject["name"]?.jsonPrimitive?.content)?.endsWith(".exe", ignoreCase = true) == true
			} ?: assets?.firstOrNull { assetObj ->
				(assetObj.jsonObject["name"]?.jsonPrimitive?.content)?.endsWith(".zip", ignoreCase = true) == true
			} ?: return@mapNotNull null

			AppVersion(
				id = obj["id"]?.jsonPrimitive?.long ?: 0L,
				url = obj["html_url"]?.jsonPrimitive?.content.orEmpty(),
				name = releaseTag.removePrefix("v"),
				apkSize = asset.jsonObject["size"]?.jsonPrimitive?.long ?: 0L,
				apkUrl = asset.jsonObject["browser_download_url"]?.jsonPrimitive?.content.orEmpty(),
				description = obj["body"]?.jsonPrimitive?.content.orEmpty(),
			)
		}
	}

	suspend fun fetchUpdate(): AppVersion? = withContext(Dispatchers.Default) {
		if (!isUpdateSupported()) {
			return@withContext null
		}
		runCatchingCancellable {
			val currentVersion = VersionId(CURRENT_VERSION)
			val available = getAvailableVersions().asArrayList()
			available.sortBy { it.versionId }
			if (currentVersion.isStable && (settings?.isUnstableUpdatesAllowed != true)) {
				available.retainAll { it.versionId.isStable }
			}
			available.maxByOrNull { it.versionId }?.takeIf { it.versionId > currentVersion }
		}.onFailure {
			it.printStackTraceDebug()
		}.onSuccess {
			availableUpdate.value = it
		}.getOrNull()
	}

	fun isUpdateSupported(): Boolean {
		return true
	}

	companion object {

		/** Dépôt de mises à jour par défaut du portage (à ajuster au moment du packaging). */
		const val DEFAULT_UPDATES_REPO = "Kotatsu-Redo/Kotatsu-Redo"

		/** Version courante du portage ; sera alimentée par le packaging (lot L5). */
		const val CURRENT_VERSION = "0.1.0"

		private const val CONTENT_TYPE_EXE = "application/x-msdownload"
	}
}
