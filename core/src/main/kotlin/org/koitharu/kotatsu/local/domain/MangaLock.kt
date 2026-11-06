/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : injection Hilt retirée (constructeur explicite fourni par l'hôte desktop).
 */
package org.koitharu.kotatsu.local.domain

import org.koitharu.kotatsu.core.util.MultiMutex
import org.koitharu.kotatsu.parsers.model.Manga

class MangaLock : MultiMutex<Manga>()
