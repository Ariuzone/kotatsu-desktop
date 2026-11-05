/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.db

import androidx.sqlite.SQLiteConnection

/**
 * Échappe une chaîne pour un littéral SQL (remplace l'équivalent Android
 * `android.database.DatabaseUtils.sqlEscapeString`, indisponible sur JVM).
 */
fun sqlEscapeString(value: String): String {
	val sb = StringBuilder(value.length + 2)
	sb.append('\'')
	for (ch in value) {
		if (ch == '\'') {
			sb.append('\'')
		}
		sb.append(ch)
	}
	sb.append('\'')
	return sb.toString()
}

/**
 * TODO(merge) : androidx.sqlite KMP (JVM) n'expose pas `execSQL` sur SQLiteConnection
 * (équivalent de `SupportSQLiteDatabase.execSQL`). Helpers minimaux, à remplacer par
 * les helpers officiels s'ils arrivent dans androidx.sqlite.
 */
fun SQLiteConnection.execSQL(sql: String) {
	prepare(sql).use { statement ->
		while (statement.step()) {
			// DDL / DML sans résultat : on épuise le curseur.
		}
	}
}

fun SQLiteConnection.execSQL(sql: String, parameters: List<Any?>) {
	prepare(sql).use { statement ->
		parameters.forEachIndexed { index, parameter ->
			val position = index + 1
			when (parameter) {
				null -> statement.bindNull(position)
				is Long -> statement.bindLong(position, parameter)
				is Int -> statement.bindInt(position, parameter)
				is Short -> statement.bindLong(position, parameter.toLong())
				is Byte -> statement.bindLong(position, parameter.toLong())
				is Boolean -> statement.bindBoolean(position, parameter)
				is Float -> statement.bindDouble(position, parameter.toDouble())
				is Double -> statement.bindDouble(position, parameter)
				is String -> statement.bindText(position, parameter)
				is ByteArray -> statement.bindBlob(position, parameter)
				else -> throw IllegalArgumentException("Type de bind non supporté : ${parameter::class.java}")
			}
		}
		while (statement.step()) {
			// DDL / DML sans résultat.
		}
	}
}
