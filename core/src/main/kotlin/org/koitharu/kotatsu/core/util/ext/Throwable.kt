/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : les fonctions dépendantes de Resources/R.string/ACRA (getDisplayMessage, getDisplayIcon,
 * parseMessage, report) sont retirées ; les utilitaires purs sont conservés.
 */
package org.koitharu.kotatsu.core.util.ext

import coil3.network.HttpException
import okhttp3.Response
import okhttp3.internal.http2.StreamResetException
import okio.IOException
import org.jsoup.HttpStatusException
import org.koitharu.kotatsu.core.exceptions.BadBackupFormatException
import org.koitharu.kotatsu.core.exceptions.CaughtException
import org.koitharu.kotatsu.core.exceptions.CloudFlareBlockedException
import org.koitharu.kotatsu.core.exceptions.CloudFlareException
import org.koitharu.kotatsu.core.exceptions.CloudFlareProtectedException
import org.koitharu.kotatsu.core.exceptions.EmptyMangaException
import org.koitharu.kotatsu.core.exceptions.IncompatiblePluginException
import org.koitharu.kotatsu.core.exceptions.NoDataReceivedException
import org.koitharu.kotatsu.core.exceptions.ProxyConfigException
import org.koitharu.kotatsu.core.exceptions.SyncApiException
import org.koitharu.kotatsu.core.exceptions.UnsupportedFileException
import org.koitharu.kotatsu.core.exceptions.UnsupportedSourceException
import org.koitharu.kotatsu.core.exceptions.WrapperIOException
import org.koitharu.kotatsu.core.exceptions.WrongPasswordException
import org.koitharu.kotatsu.parsers.exception.AuthRequiredException
import org.koitharu.kotatsu.parsers.exception.ContentUnavailableException
import org.koitharu.kotatsu.parsers.exception.NotFoundException
import org.koitharu.kotatsu.parsers.exception.ParseException
import org.koitharu.kotatsu.parsers.exception.TooManyRequestExceptions
import org.koitharu.kotatsu.parsers.util.ifNullOrEmpty
import java.io.FileNotFoundException
import java.io.File
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.zip.ZipException

private const val MSG_NO_SPACE_LEFT = "No space left on device"
private const val MSG_CONNECTION_RESET = "Connection reset"

private val FNFE_MESSAGE_REGEX = Regex("^(/[^\\s:]+)?.+?\\s([A-Z]{2,6})?\\s.+$")

/** Walks the exception cause chain and returns the first [CloudFlareException] found, or `null`. */
fun Throwable.findCloudFlareException(): CloudFlareException? =
	generateSequence(this) { it.cause?.takeIf { c -> c !== it } }
		.filterIsInstance<CloudFlareException>()
		.firstOrNull()

/**
 * `true` when the source answered "this title is gone": a 404, or a parser explicitly reporting the
 * content as unavailable. Walks the cause chain, because loaders wrap parser errors in
 * [CaughtException] / [WrapperIOException].
 */
fun Throwable.isContentNotFound(): Boolean = generateSequence(this) { it.cause?.takeIf { c -> c !== it } }
	.any { e ->
		when (e) {
			is NotFoundException,
			is ContentUnavailableException -> true

			is HttpException -> e.response.code == HttpURLConnection.HTTP_NOT_FOUND
			is HttpStatusException -> e.statusCode == HttpURLConnection.HTTP_NOT_FOUND
			else -> false
		}
	}

fun Throwable.getCauseUrl(): String? = when (this) {
	is ParseException -> url
	is NotFoundException -> url
	is TooManyRequestExceptions -> url
	is CaughtException -> cause.getCauseUrl()
	is WrapperIOException -> cause.getCauseUrl()
	is NoDataReceivedException -> url
	is CloudFlareBlockedException -> url
	is CloudFlareProtectedException -> url
	is HttpStatusException -> url
	is UnsupportedSourceException -> manga?.publicUrl?.takeIf { it.isHttpUrl() }
	is EmptyMangaException -> manga.publicUrl.takeIf { it.isHttpUrl() }
	is HttpException -> (response.delegate as? Response)?.request?.url?.toString()
	else -> null
}

fun Throwable.isNetworkError(): Boolean {
	return this is UnknownHostException
		|| this is SocketTimeoutException
		|| this is StreamResetException
		|| this is SocketException
		|| this is HttpException && response.code == HttpURLConnection.HTTP_GATEWAY_TIMEOUT
}

@Suppress("FunctionName")
fun NoSpaceLeftException() = IOException(MSG_NO_SPACE_LEFT)

fun FileNotFoundException.getFile(): File? {
	val groups = FNFE_MESSAGE_REGEX.matchEntire(message ?: return null)?.groupValues ?: return null
	return groups.getOrNull(1)?.let { File(it) }
}

fun Throwable.isCorruptedZip(): Boolean = this is ZipException || cause is ZipException

fun Throwable.isBadBackupFormat(): Boolean = this is BadBackupFormatException || cause is BadBackupFormatException

fun Throwable.isWrongPassword(): Boolean = this is WrongPasswordException || cause is WrongPasswordException

fun Throwable.isIncompatiblePlugin(): Boolean =
	this is IncompatiblePluginException || cause is IncompatiblePluginException

fun Throwable.isUnsupportedFile(): Boolean = this is UnsupportedFileException || cause is UnsupportedFileException

fun Throwable.isProxyConfigError(): Boolean = this is ProxyConfigException || cause is ProxyConfigException

fun Throwable.isSyncApiError(): Boolean = this is SyncApiException || cause is SyncApiException

fun Throwable.isConnectionReset(): Boolean = message == MSG_CONNECTION_RESET

fun Throwable.isNoSpaceLeft(): Boolean = message?.contains(MSG_NO_SPACE_LEFT) == true

fun Throwable.isAuthRequired(): Boolean = generateSequence(this) { it.cause?.takeIf { c -> c !== it } }
	.any { it is AuthRequiredException }
