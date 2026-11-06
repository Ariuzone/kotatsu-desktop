/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration3To4 : Migration(3, 4) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("CREATE TABLE IF NOT EXISTS tracks (manga_id INTEGER NOT NULL, chapters_total INTEGER NOT NULL, last_chapter_id INTEGER NOT NULL, chapters_new INTEGER NOT NULL, last_check INTEGER NOT NULL, last_notified_id INTEGER NOT NULL, PRIMARY KEY(manga_id), FOREIGN KEY(manga_id) REFERENCES manga(manga_id) ON UPDATE NO ACTION ON DELETE CASCADE )")
	}
}
