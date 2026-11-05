/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : variantes debug/release Android (app/src/{debug,release}/Debug.kt) fusionnées en
 * une version JVM :
 * le stack trace est toujours affiché ; la garde « main thread » n'a pas d'équivalent desktop (no-op).
 */
package org.koitharu.kotatsu.core.util.ext

fun Throwable.printStackTraceDebug() = printStackTrace()

fun assertNotInMainThread() = Unit
