/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration23To24 : Migration(23, 24) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("CREATE TABLE IF NOT EXISTS `chapters` (`chapter_id` INTEGER NOT NULL, `manga_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `number` REAL NOT NULL, `volume` INTEGER NOT NULL, `url` TEXT NOT NULL, `scanlator` TEXT, `upload_date` INTEGER NOT NULL, `branch` TEXT, `source` TEXT NOT NULL, `index` INTEGER NOT NULL, PRIMARY KEY(`manga_id`, `chapter_id`), FOREIGN KEY(`manga_id`) REFERENCES `manga`(`manga_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
	}
}
