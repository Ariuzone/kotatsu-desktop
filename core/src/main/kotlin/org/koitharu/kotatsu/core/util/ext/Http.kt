/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : les fonctions org.json.JSONObject (non disponible sur JVM desktop) sont remplacées
 * par des équivalents basés sur kotlinx.serialization / texte brut.
 */
package org.koitharu.kotatsu.core.util.ext

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.jsoup.HttpStatusException
import java.net.HttpURLConnection

private val TYPE_JSON = "application/json".toMediaType()

fun String.toJsonRequestBody() = toRequestBody(TYPE_JSON)

fun Response.ensureSuccess() = apply {
	if (!isSuccessful || code == HttpURLConnection.HTTP_NO_CONTENT) {
		closeQuietly()
		throw HttpStatusException(message, code, request.url.toString())
	}
}

fun Response.closeQuietly() {
	try {
		close()
	} catch (_: Exception) {
	}
}

fun String.sanitizeHeaderValue(): String {
	return if (all(Char::isValidForHeaderValue)) {
		this // fast path
	} else {
		filter(Char::isValidForHeaderValue)
	}
}

private fun Char.isValidForHeaderValue(): Boolean {
	// from okhttp3.Headers$Companion.checkValue
	return this == '\t' || this in '\u0020'..'\u007e'
}
