/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL

class Migration13To14 : Migration(13, 14) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("ALTER TABLE favourite_categories ADD COLUMN `deleted_at` INTEGER NOT NULL DEFAULT 0")
		db.execSQL("ALTER TABLE favourites ADD COLUMN `deleted_at` INTEGER NOT NULL DEFAULT 0")
		db.execSQL("ALTER TABLE history ADD COLUMN `deleted_at` INTEGER NOT NULL DEFAULT 0")
		db.execSQL("ALTER TABLE preferences ADD COLUMN `cf_brightness` REAL NOT NULL DEFAULT 0")
		db.execSQL("ALTER TABLE preferences ADD COLUMN `cf_contrast` REAL NOT NULL DEFAULT 0")
	}
}
