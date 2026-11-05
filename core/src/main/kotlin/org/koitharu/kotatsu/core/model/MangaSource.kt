/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : version desktop minimale du fichier core/model/MangaSource.kt Android — seules les
 * déclarations sans dépendance UI/Android sont conservées (LocalMangaSource, UnknownMangaSource,
 * TestMangaSource, factory MangaSource(name), isNsfw, getLocale). Les fonctions Context/StringRes
 * sont retirées (définies côté UI desktop). NB : point de fusion possible avec le lot L1 « modèles ».
 */
package org.koitharu.kotatsu.core.model

import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.util.splitTwoParts
import java.util.Locale

data object LocalMangaSource : MangaSource {
	override val name = "LOCAL"
}

data object UnknownMangaSource : MangaSource {
	override val name = "UNKNOWN"
}

data object TestMangaSource : MangaSource {
	override val name = "TEST"
}

fun MangaSource(name: String?): MangaSource {
	when (name ?: return UnknownMangaSource) {
		UnknownMangaSource.name -> return UnknownMangaSource
		LocalMangaSource.name -> return LocalMangaSource
		TestMangaSource.name -> return TestMangaSource
	}
	MangaParserSource.entries.forEach {
		if (it.name == name) return it
	}
	return UnknownMangaSource
}

fun Collection<String>.toMangaSources() = map(::MangaSource)

fun MangaSource.isNsfw(): Boolean = when (this) {
	is MangaParserSource -> contentType == ContentType.HENTAI
	else -> false
}

fun MangaSource.getLocale(): Locale? = (this as? MangaParserSource)?.locale
	?.let { l -> runCatching { Locale.forLanguageTag(l) }.getOrNull() }
