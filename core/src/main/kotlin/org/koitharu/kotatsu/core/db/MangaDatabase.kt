/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.core.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import org.koitharu.kotatsu.bookmarks.data.BookmarkEntity
import org.koitharu.kotatsu.bookmarks.data.BookmarksDao
import org.koitharu.kotatsu.core.db.dao.ChaptersDao
import org.koitharu.kotatsu.core.db.dao.MangaDao
import org.koitharu.kotatsu.core.db.dao.MangaSourcesDao
import org.koitharu.kotatsu.core.db.dao.PreferencesDao
import org.koitharu.kotatsu.core.db.dao.TagsDao
import org.koitharu.kotatsu.core.db.dao.TrackLogsDao
import org.koitharu.kotatsu.core.db.entity.ChapterEntity
import org.koitharu.kotatsu.core.db.entity.MangaEntity
import org.koitharu.kotatsu.core.db.entity.MangaPrefsEntity
import org.koitharu.kotatsu.core.db.entity.MangaSourceEntity
import org.koitharu.kotatsu.core.db.entity.MangaTagsEntity
import org.koitharu.kotatsu.core.db.entity.TagEntity
import org.koitharu.kotatsu.core.db.migrations.Migration10To11
import org.koitharu.kotatsu.core.db.migrations.Migration11To12
import org.koitharu.kotatsu.core.db.migrations.Migration12To13
import org.koitharu.kotatsu.core.db.migrations.Migration13To14
import org.koitharu.kotatsu.core.db.migrations.Migration14To15
import org.koitharu.kotatsu.core.db.migrations.Migration15To16
import org.koitharu.kotatsu.core.db.migrations.Migration16To17
import org.koitharu.kotatsu.core.db.migrations.Migration17To18
import org.koitharu.kotatsu.core.db.migrations.Migration18To19
import org.koitharu.kotatsu.core.db.migrations.Migration19To20
import org.koitharu.kotatsu.core.db.migrations.Migration1To2
import org.koitharu.kotatsu.core.db.migrations.Migration20To21
import org.koitharu.kotatsu.core.db.migrations.Migration21To22
import org.koitharu.kotatsu.core.db.migrations.Migration22To23
import org.koitharu.kotatsu.core.db.migrations.Migration23To24
import org.koitharu.kotatsu.core.db.migrations.Migration24To23
import org.koitharu.kotatsu.core.db.migrations.Migration24To25
import org.koitharu.kotatsu.core.db.migrations.Migration25To26
import org.koitharu.kotatsu.core.db.migrations.Migration26To27
import org.koitharu.kotatsu.core.db.migrations.Migration27To28
import org.koitharu.kotatsu.core.db.migrations.Migration2To3
import org.koitharu.kotatsu.core.db.migrations.Migration3To4
import org.koitharu.kotatsu.core.db.migrations.Migration4To5
import org.koitharu.kotatsu.core.db.migrations.Migration5To6
import org.koitharu.kotatsu.core.db.migrations.Migration6To7
import org.koitharu.kotatsu.core.db.migrations.Migration7To8
import org.koitharu.kotatsu.core.db.migrations.Migration8To9
import org.koitharu.kotatsu.core.db.migrations.Migration9To10
import org.koitharu.kotatsu.explore.data.SourcePresetEntity
import org.koitharu.kotatsu.explore.data.SourcePresetsDao
import org.koitharu.kotatsu.favourites.data.FavouriteCategoriesDao
import org.koitharu.kotatsu.favourites.data.FavouriteCategoryEntity
import org.koitharu.kotatsu.favourites.data.FavouriteEntity
import org.koitharu.kotatsu.favourites.data.FavouritesDao
import org.koitharu.kotatsu.history.data.HistoryDao
import org.koitharu.kotatsu.history.data.HistoryEntity
import org.koitharu.kotatsu.local.data.index.LocalMangaIndexDao
import org.koitharu.kotatsu.local.data.index.LocalMangaIndexEntity
import org.koitharu.kotatsu.scrobbling.common.data.ScrobblingDao
import org.koitharu.kotatsu.scrobbling.common.data.ScrobblingEntity
import org.koitharu.kotatsu.stats.data.StatsDao
import org.koitharu.kotatsu.stats.data.StatsEntity
import org.koitharu.kotatsu.suggestions.data.SuggestionDao
import org.koitharu.kotatsu.suggestions.data.SuggestionEntity
import org.koitharu.kotatsu.tracker.data.TrackEntity
import org.koitharu.kotatsu.tracker.data.TrackLogEntity
import org.koitharu.kotatsu.tracker.data.TracksDao
import java.io.File
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

const val DATABASE_VERSION = 28

/**
 * TODO(merge) : remplaçant de `BuildConfig.VERSION_CODE` (app Android) pour la colonne
 * `sources.added_in`. À rebrancher sur la version de l'app desktop à la fusion.
 */
const val CURRENT_APP_VERSION_CODE = 0

@Database(
	entities = [
		MangaEntity::class, TagEntity::class, HistoryEntity::class, MangaTagsEntity::class, ChapterEntity::class,
		FavouriteCategoryEntity::class, FavouriteEntity::class, MangaPrefsEntity::class, TrackEntity::class,
		TrackLogEntity::class, SuggestionEntity::class, BookmarkEntity::class, ScrobblingEntity::class,
		MangaSourceEntity::class, StatsEntity::class, LocalMangaIndexEntity::class,
		SourcePresetEntity::class,
	],
	version = DATABASE_VERSION,
)
abstract class MangaDatabase : RoomDatabase() {

	abstract fun getHistoryDao(): HistoryDao

	abstract fun getTagsDao(): TagsDao

	abstract fun getMangaDao(): MangaDao

	abstract fun getFavouritesDao(): FavouritesDao

	abstract fun getPreferencesDao(): PreferencesDao

	abstract fun getFavouriteCategoriesDao(): FavouriteCategoriesDao

	abstract fun getTracksDao(): TracksDao

	abstract fun getTrackLogsDao(): TrackLogsDao

	abstract fun getSuggestionDao(): SuggestionDao

	abstract fun getBookmarksDao(): BookmarksDao

	abstract fun getScrobblingDao(): ScrobblingDao

	abstract fun getSourcesDao(): MangaSourcesDao

	abstract fun getStatsDao(): StatsDao

	abstract fun getLocalMangaIndexDao(): LocalMangaIndexDao

	abstract fun getChaptersDao(): ChaptersDao

	abstract fun getSourcePresetsDao(): SourcePresetsDao
}

/**
 * TODO(merge) : sur Android, `Migration16To17` lisait les préférences (sources masquées et
 * ordre manuel). Sur desktop, on passe des listes vides ; à recâbler avec core/prefs à la fusion.
 */
fun getDatabaseMigrations(): Array<Migration> = arrayOf(
	Migration1To2(),
	Migration2To3(),
	Migration3To4(),
	Migration4To5(),
	Migration5To6(),
	Migration6To7(),
	Migration7To8(),
	Migration8To9(),
	Migration9To10(),
	Migration10To11(),
	Migration11To12(),
	Migration12To13(),
	Migration13To14(),
	Migration14To15(),
	Migration15To16(),
	Migration16To17(),
	Migration17To18(),
	Migration18To19(),
	Migration19To20(),
	Migration20To21(),
	Migration21To22(),
	Migration22To23(),
	Migration23To24(),
	Migration24To23(),
	Migration24To25(),
	Migration25To26(),
	Migration26To27(),
	Migration27To28(),
)

/**
 * TODO(merge) : le builder Android (`Room.databaseBuilder(context, ...)`) est remplacé par le
 * builder JVM KMP avec `BundledSQLiteDriver`. Le callback de pré-remplissage reçoit un titre
 * en dur tant que la i18n desktop n'est pas portée.
 */
fun createMangaDatabase(file: File): MangaDatabase = Room
	.databaseBuilder(file.absolutePath, ::MangaDatabase_Impl)
	.setDriver(BundledSQLiteDriver())
	.addMigrations(*getDatabaseMigrations())
	.addCallback(DatabasePrePopulateCallback("Read later"))
	.build()
