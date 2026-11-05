/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL
import org.koitharu.kotatsu.parsers.model.SortOrder

class Migration8To9 : Migration(8, 9) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("ALTER TABLE favourite_categories ADD COLUMN `order` TEXT NOT NULL DEFAULT ${SortOrder.NEWEST.name}")
	}
}
