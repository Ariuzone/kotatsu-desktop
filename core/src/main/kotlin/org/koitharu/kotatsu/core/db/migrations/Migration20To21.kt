/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration20To21 : Migration(20, 21) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("ALTER TABLE tracks ADD COLUMN `last_error` TEXT DEFAULT NULL")
		db.execSQL("ALTER TABLE sources ADD COLUMN `added_in` INTEGER NOT NULL DEFAULT 0")
	}
}
