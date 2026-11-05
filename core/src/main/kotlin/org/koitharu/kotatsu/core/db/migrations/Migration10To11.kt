/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration10To11 : Migration(10, 11) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL(
			"""
			CREATE TABLE IF NOT EXISTS `bookmarks` (
				`manga_id` INTEGER NOT NULL,
				`page_id` INTEGER NOT NULL,
				`chapter_id` INTEGER NOT NULL, 
				`page` INTEGER NOT NULL,
				`scroll` INTEGER NOT NULL,
				`image` TEXT NOT NULL,
				`created_at` INTEGER NOT NULL,
				PRIMARY KEY(`manga_id`, `page_id`),
				FOREIGN KEY(`manga_id`) REFERENCES `manga`(`manga_id`) ON UPDATE NO ACTION ON DELETE CASCADE )
			""".trimIndent()
		)
		db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_manga_id` ON `bookmarks` (`manga_id`)")
		db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_page_id` ON `bookmarks` (`page_id`)")
	}
}
