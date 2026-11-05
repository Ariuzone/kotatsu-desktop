/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration11To12 : Migration(11, 12) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL(
			"""
			CREATE TABLE IF NOT EXISTS `scrobblings` (
				`scrobbler` INTEGER NOT NULL,
				`id` INTEGER NOT NULL,
				`manga_id` INTEGER NOT NULL,
				`target_id` INTEGER NOT NULL, 
				`status` TEXT,
				`chapter` INTEGER NOT NULL, 
				`comment` TEXT,
				`rating` REAL NOT NULL,
				PRIMARY KEY(`scrobbler`, `id`, `manga_id`)
			)
			""".trimIndent()
		)
		db.execSQL("ALTER TABLE history ADD COLUMN `percent` REAL NOT NULL DEFAULT -1")
		db.execSQL("ALTER TABLE bookmarks ADD COLUMN `percent` REAL NOT NULL DEFAULT -1")
	}
}
