/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.sourcescore.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

const val COMMUNITY_DATABASE_VERSION = 2

/**
 * A **separate** Room database, deliberately not part of [org.koitharu.kotatsu.core.db.MangaDatabase].
 *
 * Kotatsu-Redo tracks upstream Kotatsu. `MangaDatabase` is at version 28, and if upstream ships its
 * own `Migration28To29` while this fork also adds one, every rebase becomes a conflict and users'
 * databases diverge in ways that are painful to unpick. Keeping community data in its own file with
 * its own version line sidesteps that permanently and keeps the diff against upstream small - which
 * matters for a fork whose stated goal is maintainability.
 *
 * It also means a user who turns community features off can have this database deleted outright,
 * with nothing else riding along in it.
 */
@Database(
	entities = [SourceStatsEntity::class, SourceScoreEntity::class, WorkRefEntity::class],
	version = COMMUNITY_DATABASE_VERSION,
)
abstract class CommunityDatabase : RoomDatabase() {

	abstract fun getSourceStatsDao(): SourceStatsDao

	abstract fun getSourceScoreDao(): SourceScoreDao

	abstract fun getWorkRefDao(): WorkRefDao
}

fun createCommunityDatabase(file: File): CommunityDatabase = Room
	.databaseBuilder(file.absolutePath, ::CommunityDatabase_Impl)
	.setDriver(BundledSQLiteDriver())
	// Every row here is derived data: local counters that rebuild themselves, and a score snapshot
	// that re-downloads. Losing it costs a few days of ranking quality, never user content - so a
	// destructive fallback is the right trade against ever failing to open.
	.fallbackToDestructiveMigration(dropAllTables = true)
	.build()
