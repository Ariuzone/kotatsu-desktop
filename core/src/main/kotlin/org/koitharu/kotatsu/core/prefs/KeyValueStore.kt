/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Fichier nouveau : remplace SharedPreferences (Android) par une interface KeyValueStore
 * inspirée de son API, avec une implémentation JSON persistante (kotlinx.serialization).
 */
package org.koitharu.kotatsu.core.prefs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.long
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.writeText
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug

/**
 * Store clé-valeur typé, inspiré de l'API SharedPreferences (Android), porté pour desktop.
 * Les valeurs null ne sont pas stockées : mettre null supprime la clé.
 */
interface KeyValueStore {

	fun getString(key: String, defValue: String?): String?

	fun getInt(key: String, defValue: Int): Int

	fun getLong(key: String, defValue: Long): Long

	fun getFloat(key: String, defValue: Float): Float

	fun getBoolean(key: String, defValue: Boolean): Boolean

	fun getStringSet(key: String, defValues: Set<String>?): Set<String>?

	fun contains(key: String): Boolean

	/** Valeurs brutes courantes (Map<String, Any?> : String/Int/Long/Float/Boolean/Set<String>). */
	val all: Map<String, Any?>

	/** Édite les préférences de façon synchrone. */
	fun edit(commit: Boolean = false, block: Editor.() -> Unit)

	/** Édite les préférences hors du thread courant. */
	suspend fun editAsync(block: Editor.() -> Unit)

	/** Émet la clé modifiée (ou null après clear), précédé d'un état initial null. */
	fun observeChanges(): Flow<String?>

	interface Editor {

		fun putString(key: String, value: String?)

		fun putInt(key: String, value: Int)

		fun putLong(key: String, value: Long)

		fun putFloat(key: String, value: Float)

		fun putBoolean(key: String, value: Boolean)

		fun putStringSet(key: String, value: Set<String>?)

		fun remove(key: String)

		fun clear()
	}
}

fun <E : Enum<E>> KeyValueStore.getEnumValue(key: String, cls: Class<E>, defValue: E?): E? {
	val raw = getString(key, null) ?: return defValue
	return try {
		java.lang.Enum.valueOf(cls, raw)
	} catch (_: IllegalArgumentException) {
		defValue
	}
}

inline fun <reified E : Enum<E>> KeyValueStore.getEnumValueOrNull(key: String, defValue: E?): E? =
	getEnumValue(key, E::class.java, defValue)

inline fun <reified E : Enum<E>> KeyValueStore.getEnumValue(key: String, defValue: E): E =
	getEnumValue(key, E::class.java, defValue) ?: defValue

inline fun <reified E : Enum<E>> KeyValueStore.putEnumValue(key: String, value: E?) {
	edit { putString(key, value?.name) }
}

/**
 * Implémentation [KeyValueStore] persistée dans un fichier JSON (kotlinx.serialization).
 * Les écritures sont atomiques (fichier temporaire + move) et protégées par un mutex.
 */
class FileKeyValueStore(
	private val file: Path,
) : KeyValueStore {

	private val json = Json { prettyPrint = true }
	private val mutex = Mutex()
	private val changes = MutableSharedFlow<String?>(extraBufferCapacity = 64)
	private val data: MutableMap<String, JsonElement> = load()

	constructor(file: File) : this(file.toPath())

	private fun load(): MutableMap<String, JsonElement> {
		if (!Files.exists(file)) {
			return mutableMapOf()
		}
		return try {
			val text = Files.readAllBytes(file).decodeToString()
			if (text.isBlank()) {
				mutableMapOf()
			} else {
				json.parseToJsonElement(text).jsonObject.toMutableMap()
			}
		} catch (e: Exception) {
			e.printStackTraceDebug()
			mutableMapOf()
		}
	}

	private fun primitive(key: String): JsonPrimitive? = synchronized(data) {
		data[key] as? JsonPrimitive
	}

	private val JsonPrimitive.plain: Any?
		get() = when {
			!isString && booleanOrNull != null -> boolean
			!isString -> content.toLongOrNull() ?: content.toFloatOrNull() ?: content
			else -> content
		}

	override fun getString(key: String, defValue: String?): String? =
		primitive(key)?.contentOrNull ?: defValue

	override fun getInt(key: String, defValue: Int): Int =
		primitive(key)?.takeIf { !it.isString }?.int ?: defValue

	override fun getLong(key: String, defValue: Long): Long =
		primitive(key)?.takeIf { !it.isString }?.long ?: defValue

	override fun getFloat(key: String, defValue: Float): Float =
		primitive(key)?.takeIf { !it.isString }?.float ?: defValue

	override fun getBoolean(key: String, defValue: Boolean): Boolean =
		primitive(key)?.takeIf { !it.isString }?.boolean ?: defValue

	override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
		val array = synchronized(data) { data[key] as? JsonArray } ?: return defValues
		return array.mapNotNullTo(LinkedHashSet()) { (it as? JsonPrimitive)?.contentOrNull }
	}

	override fun contains(key: String): Boolean = synchronized(data) {
		data.containsKey(key)
	}

	override val all: Map<String, Any?>
		get() = synchronized(data) {
			data.entries.associate { (k, v) ->
				k to when (v) {
					is JsonPrimitive -> v.plain
					is JsonArray -> v.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.toSet()
					is JsonObject -> v
				}
			}
		}

	override fun edit(commit: Boolean, block: KeyValueStore.Editor.() -> Unit) {
		val editor = EditorImpl()
		editor.block()
		if (editor.dirty) {
			persist(editor.changedKeys)
		}
	}

	override suspend fun editAsync(block: KeyValueStore.Editor.() -> Unit) = withContext(Dispatchers.IO) {
		mutex.withLock {
			edit(commit = true, block = block)
		}
	}

	override fun observeChanges(): Flow<String?> = changes.onStart { emit(null) }

	private fun persist(changedKeys: Set<String?>) {
		val serialized = synchronized(data) {
			json.encodeToString(JsonObject.serializer(), JsonObject(data.toSortedMap()))
		}
		runCatching {
			file.parent?.let { Files.createDirectories(it) }
			val tmp = file.resolveSibling(file.fileName.toString() + ".tmp")
			tmp.writeText(serialized)
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
		}.onFailure { e ->
			e.printStackTraceDebug()
		}
		for (key in changedKeys) {
			changes.tryEmit(key)
		}
	}

	private inner class EditorImpl : KeyValueStore.Editor {

		val changedKeys = mutableSetOf<String?>()
		var dirty = false
			private set

		private fun put(key: String, value: JsonElement?) {
			synchronized(data) {
				if (value == null) {
					data.remove(key)
				} else {
					data[key] = value
				}
			}
			changedKeys.add(key)
			dirty = true
		}

		override fun putString(key: String, value: String?) = put(key, value?.let(::JsonPrimitive))

		override fun putInt(key: String, value: Int) = put(key, JsonPrimitive(value))

		override fun putLong(key: String, value: Long) = put(key, JsonPrimitive(value))

		override fun putFloat(key: String, value: Float) = put(key, JsonPrimitive(value))

		override fun putBoolean(key: String, value: Boolean) = put(key, JsonPrimitive(value))

		override fun putStringSet(key: String, value: Set<String>?) = put(
			key,
			value?.let { set -> JsonArray(set.map(::JsonPrimitive)) },
		)

		override fun remove(key: String) = put(key, null)

		override fun clear() {
			synchronized(data) {
				changedKeys.addAll(data.keys)
				data.clear()
				dirty = true
			}
			changedKeys.add(null)
		}
	}
}
