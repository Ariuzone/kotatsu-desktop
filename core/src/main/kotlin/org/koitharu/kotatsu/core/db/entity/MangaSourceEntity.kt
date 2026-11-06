/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.koitharu.kotatsu.core.db.TABLE_SOURCES

@Entity(
	tableName = TABLE_SOURCES,
)
data class MangaSourceEntity(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "source")
	val source: String,
	@ColumnInfo(name = "enabled") val isEnabled: Boolean,
	@ColumnInfo(name = "sort_key", index = true) val sortKey: Int,
	@ColumnInfo(name = "added_in") val addedIn: Int,
	@ColumnInfo(name = "used_at") val lastUsedAt: Long,
	@ColumnInfo(name = "pinned") val isPinned: Boolean,
	@ColumnInfo(name = "cf_state") val cfState: Int,
)
