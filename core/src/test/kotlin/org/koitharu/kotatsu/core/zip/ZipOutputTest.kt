/*
 * Fichier nouveau — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 */
package org.koitharu.kotatsu.core.zip

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile

class ZipOutputTest {

	@get:Rule
	val tempFolder = TemporaryFolder()

	private fun fakeImage(size: Int, seed: Byte): ByteArray = ByteArray(size) { i ->
		((i + seed) % 127).toByte()
	}

	@Test
	fun `put files and read back`() {
		val zipFile = File(tempFolder.root, "test.cbz")
		val image1 = File(tempFolder.root, "img1.jpg")
		val image2 = File(tempFolder.root, "img2.jpg")
		val data1 = fakeImage(128, 0)
		val data2 = fakeImage(64, 7)
		image1.writeBytes(data1)
		image2.writeBytes(data2)

		val output = ZipOutput(zipFile)
		try {
			assertTrue(output.put("00000001_00010000.jpg", image1))
			assertTrue(output.put("00000001_00010001.jpg", image2))
			assertTrue(output.put("index.json", "{\"id\":42}"))
			output.finish()
		} finally {
			output.close()
		}

		ZipFile(zipFile).use { zip ->
			val entries = zip.entries().toList().map { it.name }
			assertEquals(listOf("00000001_00010000.jpg", "00000001_00010001.jpg", "index.json"), entries)
			val read1 = zip.getInputStream(zip.getEntry("00000001_00010000.jpg")).readBytes()
			assertArrayEquals(data1, read1)
			val read2 = zip.getInputStream(zip.getEntry("00000001_00010001.jpg")).readBytes()
			assertArrayEquals(data2, read2)
			assertEquals("{\"id\":42}", zip.getInputStream(zip.getEntry("index.json")).readBytes().decodeToString())
		}
	}

	@Test
	fun `append mode deduplicates entries`() {
		val zipFile = File(tempFolder.root, "append.cbz")
		val data = fakeImage(16, 3)
		val image = File(tempFolder.root, "img.png")
		image.writeBytes(data)

		val output = ZipOutput(zipFile)
		try {
			// deux finish() successifs sur la même instance : le mode append conserve les entrées
			assertTrue(output.put("a.png", image))
			output.finish()
			assertTrue(output.put("b.png", image))
			output.finish()
		} finally {
			output.close()
		}

		ZipFile(zipFile).use { zip ->
			val entries = zip.entries().toList().map { it.name }
			assertEquals(listOf("a.png", "b.png"), entries)
		}
	}
}
