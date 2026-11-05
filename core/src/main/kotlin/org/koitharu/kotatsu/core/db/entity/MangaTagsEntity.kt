/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import org.koitharu.kotatsu.core.db.TABLE_MANGA_TAGS

@Entity(
	tableName = TABLE_MANGA_TAGS,
	primaryKeys = ["manga_id", "tag_id"],
	foreignKeys = [
		ForeignKey(
			entity = MangaEntity::class,
			parentColumns = ["manga_id"],
			childColumns = ["manga_id"],
			onDelete = ForeignKey.CASCADE,
		),
		ForeignKey(
			entity = TagEntity::class,
			parentColumns = ["tag_id"],
			childColumns = ["tag_id"],
			onDelete = ForeignKey.CASCADE,
		)
	]
)
class MangaTagsEntity(
	@ColumnInfo(name = "manga_id", index = true) val mangaId: Long,
	@ColumnInfo(name = "tag_id", index = true) val tagId: Long,
)