/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.koitharu.kotatsu.core.db.execSQL
import org.koitharu.kotatsu.parsers.model.MangaParserSource

/**
 * TODO(merge) : sur Android, cette migration lisait les préférences partagées
 * (`sources_hidden`, `sources_order_2`). Découplée ici : les données de préférences sont
 * passées en paramètres de constructeur, à recâbler avec core/prefs à la fusion.
 */
class Migration16To17(
	private val hiddenSources: Set<String> = emptySet(),
	private val order: List<String> = emptyList(),
) : Migration(16, 17) {

	override fun migrate(db: SQLiteConnection) {
		db.execSQL("CREATE TABLE `sources` (`source` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `sort_key` INTEGER NOT NULL, PRIMARY KEY(`source`))")
		db.execSQL("CREATE INDEX `index_sources_sort_key` ON `sources` (`sort_key`)")
		val sources = MangaParserSource.entries
		for (source in sources) {
			val name = source.name
			val isHidden = name in hiddenSources
			var sortKey = order.indexOf(name)
			if (sortKey == -1) {
				if (isHidden) {
					sortKey = order.size + source.ordinal
				} else {
					continue
				}
			}
			db.execSQL(
				"INSERT INTO `sources` (`source`, `enabled`, `sort_key`) VALUES (?, ?, ?)",
				listOf(name, (!isHidden).toInt(), sortKey),
			)
		}
	}

	private fun Boolean.toInt() = if (this) 1 else 0
}
