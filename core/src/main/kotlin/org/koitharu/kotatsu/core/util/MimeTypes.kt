@file:OptIn(coil3.annotation.InternalCoilApi::class)
/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : MimeTypeMap Android retiré (table Coil uniquement) ; Files.probeContentType
 * toujours disponible sur JVM, plus de garde Build.VERSION.
 */
package org.koitharu.kotatsu.core.util

import org.jetbrains.annotations.Blocking
import org.koitharu.kotatsu.core.util.ext.MimeType
import org.koitharu.kotatsu.core.util.ext.toMimeTypeOrNull
import org.koitharu.kotatsu.parsers.util.nullIfEmpty
import org.koitharu.kotatsu.parsers.util.removeSuffix
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import java.io.File
import java.nio.file.Files
import coil3.util.MimeTypeMap as CoilMimeTypeMap

object MimeTypes {

	// Neither Coil's table nor the platform's knows JPEG XL. Without this, .jxl pages are not
	// recognised as images in local archives, and downloaded ones are saved without an extension.
	private const val EXTENSION_JXL = "jxl"
	val JXL = MimeType("image/jxl")

	private const val EXTENSION_AVIF = "avif"
	val AVIF = MimeType("image/avif")

	fun getMimeTypeFromExtension(fileName: String): MimeType? {
		val extension = getNormalizedExtension(fileName) ?: return null
		if (extension == EXTENSION_JXL) {
			return JXL
		}
		return CoilMimeTypeMap.getMimeTypeFromExtension(extension)?.toMimeTypeOrNull()
	}

	fun getMimeTypeFromUrl(url: String): MimeType? {
		val fileName = url.substringBefore('#').substringBefore('?').substringAfterLast('/')
		if (getNormalizedExtension(fileName) == EXTENSION_JXL) {
			return JXL
		}
		return CoilMimeTypeMap.getMimeTypeFromUrl(url)?.toMimeTypeOrNull()
	}

	fun getExtension(mimeType: MimeType?): String? {
		if (mimeType == JXL) {
			return EXTENSION_JXL
		}
		if (mimeType == AVIF) {
			return EXTENSION_AVIF
		}
		mimeType ?: return null
		// Reuse Coil's reverse lookup table (the platform's MimeTypeMap is Android-only).
		val mime = mimeType.toString()
		return EXTENSIONS_BY_MIME[mime]?.nullIfEmpty()
	}

	@Blocking
	fun probeMimeType(file: File): MimeType? {
		runCatchingCancellable {
			Files.probeContentType(file.toPath())?.toMimeTypeOrNull()
		}.getOrNull()?.let { return it }
		return getMimeTypeFromExtension(file.name)
	}

	fun getNormalizedExtension(name: String): String? = name
		.lowercase()
		.removeSuffix('~')
		.removeSuffix(".tmp")
		.substringAfterLast('.', "")
		.takeIf { it.length in 2..5 }

	// Minimal reverse table for the mime types Coil does not map back to an extension.
	private val EXTENSIONS_BY_MIME = mapOf(
		"image/jpeg" to "jpg",
		"image/png" to "png",
		"image/gif" to "gif",
		"image/webp" to "webp",
		"image/bmp" to "bmp",
		"image/heif" to "heif",
		"application/pdf" to "pdf",
		"application/zip" to "zip",
		"text/plain" to "txt",
		"text/html" to "html",
	)
}
