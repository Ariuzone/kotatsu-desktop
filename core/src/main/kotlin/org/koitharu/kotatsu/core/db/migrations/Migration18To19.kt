/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration18To19 : Migration(18, 19) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("ALTER TABLE history ADD COLUMN `chapters` INTEGER NOT NULL DEFAULT -1")
		db.execSQL("CREATE TABLE IF NOT EXISTS `stats` (`manga_id` INTEGER NOT NULL, `started_at` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `pages` INTEGER NOT NULL, PRIMARY KEY(`manga_id`, `started_at`), FOREIGN KEY(`manga_id`) REFERENCES `history`(`manga_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
	}
}
