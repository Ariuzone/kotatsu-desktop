/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-05 — adaptation JVM/desktop.
 * Adaptation : SystemClock.elapsedRealtime() remplacé par System.nanoTime() (horloge monotone JVM) ;
 * annotations androidx retirées. androidx.collection.CircularArray (KMP) conservé.
 */
package org.koitharu.kotatsu.core.util.progress

import androidx.collection.CircularArray
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

class RealtimeEtaEstimator {

	private val ticks = CircularArray<Tick>(MAX_TICKS)

	@Volatile
	private var lastChange = 0L

	fun onProgressChanged(value: Int, total: Int) {
		if (total <= 0 || value > total) {
			reset()
			return
		}
		val tick = Tick(value, total, nowMillis())
		synchronized(this) {
			if (!ticks.isEmpty()) {
				val last = ticks.last
				if (last.value == tick.value && last.total == tick.total) {
					ticks.popLast()
				} else {
					lastChange = tick.timestamp
				}
			} else {
				lastChange = tick.timestamp
			}
			ticks.addLast(tick)
		}
	}

	fun reset() = synchronized(this) {
		ticks.clear()
		lastChange = 0L
	}

	fun getEta(): Long {
		val etl = getEstimatedTimeLeft()
		return if (etl == NO_TIME || etl > MAX_TIME) NO_TIME else System.currentTimeMillis() + etl
	}

	fun isStuck(): Boolean = synchronized(this) {
		return ticks.size() >= MIN_ESTIMATE_TICKS && (nowMillis() - lastChange) > STUCK_DELAY
	}

	private fun getEstimatedTimeLeft(): Long = synchronized(this) {
		val ticksCount = ticks.size()
		if (ticksCount < MIN_ESTIMATE_TICKS) {
			return NO_TIME
		}
		val percentDiff = ticks.last.percent - ticks.first.percent
		val timeDiff = ticks.last.timestamp - ticks.first.timestamp
		if (percentDiff <= 0 || timeDiff <= 0) {
			return NO_TIME
		}
		val averageTime = timeDiff / percentDiff
		val percentLeft = 1.0 - ticks.last.percent
		return (percentLeft * averageTime).roundToLong()
	}

	private class Tick(
		@JvmField val value: Int,
		@JvmField val total: Int,
		@JvmField val timestamp: Long,
	) {

		init {
			require(total > 0) { "total = $total" }
			require(value >= 0) { "value = $value" }
			require(value <= total) { "total = $total, value = $value" }
		}

		@JvmField
		val percent = value.toDouble() / total.toDouble()
	}

	private companion object {

		const val MAX_TICKS = 20
		const val MIN_ESTIMATE_TICKS = 4
		const val NO_TIME = -1L
		const val STUCK_DELAY = 10_000L
		val MAX_TIME = TimeUnit.DAYS.toMillis(1)

		fun nowMillis(): Long = System.nanoTime() / 1_000_000L
	}
}
