/*
 * Fichier nouveau (placeholder) — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * TODO(merge) : version MINIMALE de `org.koitharu.kotatsu.list.domain.ListFilterOption`,
 * sans ressources Android (R.string / R.drawable) ni favicon. À REMPLACER par la version
 * complète d'origine quand le paquet list/domain est porté. Conservée ici uniquement pour
 * que les DAO (MangaQueryBuilder, HistoryDao, FavouritesDao, TracksDao, TrackLogsDao,
 * SuggestionDao) exposent la même API que l'app d'origine.
 */
package org.koitharu.kotatsu.list.domain

import org.koitharu.kotatsu.core.db.entity.toEntity
import org.koitharu.kotatsu.core.model.FavouriteCategory
import org.koitharu.kotatsu.parsers.model.ContentType as MangaContentType
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.MangaTag

sealed interface ListFilterOption {

	val titleText: CharSequence?

	val groupKey: String

	data object Downloaded : ListFilterOption {
		override val titleText: CharSequence?
			get() = null
		override val groupKey: String
			get() = "_downloaded"
	}

	enum class Macro : ListFilterOption {
		COMPLETED,
		NEW_CHAPTERS,
		FAVORITE,
		NSFW,
		;

		override val titleText: CharSequence?
			get() = null

		override val groupKey: String
			get() = name
	}

	data class Tag(
		val tag: MangaTag,
	) : ListFilterOption {

		val tagId: Long = tag.toEntity().id

		override val titleText: String
			get() = tag.title.orEmpty()

		override val groupKey: String
			get() = "_tag"
	}

	data class TagTitle(
		override val titleText: String,
	) : ListFilterOption {
		override val groupKey: String
			get() = "_tag"
	}

	data class Favorite(
		val category: FavouriteCategory,
	) : ListFilterOption {
		override val titleText: String
			get() = category.title

		override val groupKey: String
			get() = "_favcat"
	}

	data class Source(
		val mangaSource: MangaSource,
	) : ListFilterOption {
		override val titleText: CharSequence?
			get() = (mangaSource as? MangaParserSource)?.title

		override val groupKey: String
			get() = "_source"
	}

	data class ContentType(
		val contentType: MangaContentType,
	) : ListFilterOption {
		override val titleText: CharSequence?
			get() = null

		override val groupKey: String
			get() = "_content_type"
	}

	data class Inverted(
		val option: ListFilterOption,
		override val titleText: CharSequence?,
	) : ListFilterOption {

		override val groupKey: String
			get() = "_inv" + option.groupKey
	}

	companion object {

		val SFW
			get() = Inverted(
				option = Macro.NSFW,
				titleText = null,
			)

		val NOT_FAVORITE
			get() = Inverted(
				option = Macro.FAVORITE,
				titleText = null,
			)
	}
}
