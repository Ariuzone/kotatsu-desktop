/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration27To28 : Migration(27, 28) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL(
			"""CREATE TABLE IF NOT EXISTS source_presets (
				preset_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
				title TEXT NOT NULL,
				languages TEXT NOT NULL,
				sources TEXT NOT NULL,
				created_at INTEGER NOT NULL,
				sort_key INTEGER NOT NULL,
				deleted_at INTEGER NOT NULL DEFAULT 0
			)""",
		)
	}
}
