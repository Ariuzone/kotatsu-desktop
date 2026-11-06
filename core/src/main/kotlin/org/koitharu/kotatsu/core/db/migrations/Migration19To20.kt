/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration19To20 : Migration(19, 20) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("CREATE TABLE tracks_bk (manga_id INTEGER NOT NULL, chapters_total INTEGER NOT NULL, last_chapter_id INTEGER NOT NULL, chapters_new INTEGER NOT NULL, last_check INTEGER NOT NULL, last_notified_id INTEGER NOT NULL, PRIMARY KEY(manga_id))")
		db.execSQL("INSERT INTO tracks_bk SELECT manga_id, chapters_total, last_chapter_id, chapters_new, last_check, last_notified_id FROM tracks")
		db.execSQL("DROP TABLE tracks")
		db.execSQL("CREATE TABLE tracks (`manga_id` INTEGER NOT NULL, `last_chapter_id` INTEGER NOT NULL, `chapters_new` INTEGER NOT NULL, `last_check_time` INTEGER NOT NULL, `last_chapter_date` INTEGER NOT NULL, `last_result` INTEGER NOT NULL, PRIMARY KEY(`manga_id`), FOREIGN KEY(`manga_id`) REFERENCES `manga`(`manga_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO tracks SELECT manga_id, last_chapter_id, chapters_new, last_check AS last_check_time, 0 AS last_chapter_date, 0 AS last_result FROM tracks_bk")
		db.execSQL("DROP TABLE tracks_bk")

		db.execSQL("ALTER TABLE track_logs ADD COLUMN `unread` INTEGER NOT NULL DEFAULT 0")
	}
}
