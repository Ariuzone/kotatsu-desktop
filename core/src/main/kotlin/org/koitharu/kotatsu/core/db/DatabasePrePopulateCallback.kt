/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.parsers.model.SortOrder

/**
 * TODO(merge) : sur Android le titre par défaut venait de `R.string.read_later`.
 * Découplé ici : le titre est passé au constructeur (à recâbler avec la i18n desktop
 * à la fusion).
 */
class DatabasePrePopulateCallback(
	private val defaultCategoryTitle: String,
) : RoomDatabase.Callback() {

	override fun onCreate(db: SQLiteConnection) {
		db.execSQL(
			"INSERT INTO favourite_categories (created_at, sort_key, title, `order`, track, show_in_lib, `deleted_at`) VALUES (?,?,?,?,?,?,?)",
			listOf(
				System.currentTimeMillis(),
				1,
				defaultCategoryTitle,
				SortOrder.NEWEST.name,
				1,
				1,
				0L,
			),
		)
	}
}
