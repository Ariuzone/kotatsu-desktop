/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 *
 * Test bout-en-bout du parcours P1 : liste d'une source → détails → chapitres → pages → URL.
 * Appel réseau réel (comme SourcesSmokeTest du module core).
 */
package org.koitharu.kotatsu.desktop

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import java.io.File

class EndToEndSmokeTest {

	private fun newContainer(): AppContainer {
		// AppContainer.INSTANCE est privé par design (singleton applicatif) ;
		// pour le test on passe par un dossier temporaire via getInstance ? Non :
		// on recrée le câblage minimal comme SourcesSmokeTest côté core.
		val tmp = File.createTempFile("kotatsu-e2e", "").parentFile
		return AppContainer.createForTest(File(tmp, "kotatsu-e2e-${System.nanoTime()}"))
	}

	@Test
	fun fullP1Chain() = runBlocking {
		val container = newContainer()
		val source = MangaParserSource.entries.first { it.name == "DYNASTYSCANS" }

		// 1. Catalogue
		val list = container.getList(source, 0)
		assertTrue("La source devrait renvoyer une liste non vide", list.isNotEmpty())

		// 2. Détails + chapitres (certaines entrées de catalogue n'ont pas de chapitres :
		// on parcourt les premières entrées jusqu'à trouver un manga lisible)
		val details = list.take(5)
			.mapNotNull { m ->
				runCatching { container.getDetails(m) }.getOrNull()
					?.takeIf { it.chapters?.isNotEmpty() == true }
			}
			.firstOrNull()
		assertTrue("Aucun des 5 premiers mangas n'a de chapitres", details != null)

		// 3. Pages du premier chapitre
		val pages = container.getPages(details!!.chapters!!.first())
		assertTrue("Le chapitre devrait avoir des pages", pages.isNotEmpty())

		// 4. URL de la première page
		val url = container.getPageUrl(pages.first())
		assertTrue("L'URL de la page devrait être résolue", url.isNotBlank())
	}
}
