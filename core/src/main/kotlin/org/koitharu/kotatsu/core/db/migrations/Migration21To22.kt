/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration21To22 : Migration(21, 22) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("ALTER TABLE sources ADD COLUMN `used_at` INTEGER NOT NULL DEFAULT 0")
		db.execSQL("ALTER TABLE sources ADD COLUMN `pinned` INTEGER NOT NULL DEFAULT 0")
	}
}
