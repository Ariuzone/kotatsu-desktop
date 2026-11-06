/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.explore.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.koitharu.kotatsu.core.db.TABLE_SOURCE_PRESETS

@Entity(tableName = TABLE_SOURCE_PRESETS)
data class SourcePresetEntity(
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "preset_id") val presetId: Long,
	@ColumnInfo(name = "title") val title: String,
	@ColumnInfo(name = "languages") val languages: String,
	@ColumnInfo(name = "sources") val sources: String,
	@ColumnInfo(name = "created_at") val createdAt: Long,
	@ColumnInfo(name = "sort_key") val sortKey: Int,
	@ColumnInfo(name = "deleted_at") val deletedAt: Long,
)
