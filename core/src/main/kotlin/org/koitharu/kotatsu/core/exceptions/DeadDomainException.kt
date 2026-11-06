/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — exception typée desktop.
 * Fichier nouveau : distingue un « domaine mort » (DNS introuvable, ex. source abandonnée) d'un
 * échec réseau temporaire, pour que l'UI puisse proposer une action adaptée (désactiver la source
 * vs réessayer plus tard). Côté Android la distinction est faite dans Throwable.isNetworkError ;
 * ici on la matérialise dans le type pour la consommation directe par l'UI desktop.
 */
package org.koitharu.kotatsu.core.exceptions

import okio.IOException

/**
 * Levée quand le domaine d'une source ne résout plus (UnknownHostException persistant après
 * fallback DoH) ou renvoie systématiquement une erreur serveur irrécupérable type 404 de domaine
 * repris. Ce n'est PAS un échec temporaire : réessayer plus tard n'a aucune chance d'aboutir,
 * l'UI peut marquer la source comme indisponible durablement.
 */
class DeadDomainException(
	val hostname: String,
	message: String = "Domain is unreachable or no longer exists: $hostname",
	cause: Throwable? = null,
) : IOException(message, cause)
