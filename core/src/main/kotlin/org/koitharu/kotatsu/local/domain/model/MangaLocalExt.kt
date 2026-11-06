/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : ext extraite de core/model/Manga.kt (original) — core/model n'étant pas dans le
 * périmètre du lot download/local, elle est hébergée ici pour l'usage des paquets local/ et
 * download/. Identique à l'original.
 */
package org.koitharu.kotatsu.local.domain.model

import org.koitharu.kotatsu.core.model.LocalMangaSource
import org.koitharu.kotatsu.parsers.model.Manga

val Manga.isLocal: Boolean
	get() = source == LocalMangaSource
