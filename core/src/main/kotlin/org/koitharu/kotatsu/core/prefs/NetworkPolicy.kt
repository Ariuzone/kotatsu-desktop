/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : ConnectivityManager remplacé par un paramètre isMetered passé par l'appelant
 * (l'hôte desktop décide si sa connexion est mesurée).
 */
package org.koitharu.kotatsu.core.prefs

enum class NetworkPolicy(
	private val key: Int,
) {

	NEVER(0),
	ALWAYS(1),
	NON_METERED(2);

	fun isNetworkAllowed(isMetered: Boolean = false) = when (this) {
		NEVER -> false
		ALWAYS -> true
		NON_METERED -> !isMetered
	}

	companion object {

		fun from(key: String?, default: NetworkPolicy): NetworkPolicy {
			val intKey = key?.toIntOrNull() ?: return default
			return NetworkPolicy.entries.find { it.key == intKey } ?: default
		}
	}
}
