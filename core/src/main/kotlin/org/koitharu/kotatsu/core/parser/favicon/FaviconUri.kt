/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : android.net.Uri remplacé par une chaîne « favicon:<source> » (schéma équivalent) ;
 * FaviconFetcher (fetcher Coil Android avec drawable/AdaptiveIcon) n'est pas porté.
 */
package org.koitharu.kotatsu.core.parser.favicon

import org.koitharu.kotatsu.parsers.model.MangaSource

const val URI_SCHEME_FAVICON = "favicon"

fun MangaSource.faviconUri(): String = "$URI_SCHEME_FAVICON:$name"
