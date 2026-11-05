/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration5To6 : Migration(5, 6) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("CREATE TABLE IF NOT EXISTS track_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, manga_id INTEGER NOT NULL, chapters TEXT NOT NULL, created_at INTEGER NOT NULL, FOREIGN KEY(manga_id) REFERENCES manga(manga_id) ON UPDATE NO ACTION ON DELETE CASCADE)")
		db.execSQL("CREATE INDEX IF NOT EXISTS index_track_logs_manga_id ON track_logs (manga_id)")
	}
}
