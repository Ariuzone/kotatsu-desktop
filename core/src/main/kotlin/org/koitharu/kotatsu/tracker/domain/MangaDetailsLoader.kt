/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */

package org.koitharu.kotatsu.tracker.domain

import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.parsers.model.Manga

/**
 * Fournisseur de détails manga pour le tracker — abstraction testable au-dessus de
 * [MangaRepository.Factory] (dans l'app Android, [CheckNewChaptersUseCase] dépend directement
 * de `MangaRepository.Factory`, trop lourd à instancier en test unitaire JVM : contexte de
 * loaders, réseau, cache coil. Ici une fun interface minimaliste suffit).
 */
fun interface MangaDetailsLoader {

	suspend fun getDetails(manga: Manga): Manga

	companion object {

		/** Implémentation de production : délègue au dépôt de la source (`getDetails`). */
		fun of(factory: MangaRepository.Factory): MangaDetailsLoader = MangaDetailsLoader { manga ->
			factory.create(manga.source).getDetails(manga)
		}
	}
}
