/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration22To23 : Migration(22, 23) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("CREATE TABLE IF NOT EXISTS `local_index` (`manga_id` INTEGER NOT NULL, `path` TEXT NOT NULL, PRIMARY KEY(`manga_id`), FOREIGN KEY(`manga_id`) REFERENCES `manga`(`manga_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
	}
}
