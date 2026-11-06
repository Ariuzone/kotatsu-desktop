/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — tests unitaires desktop.
 * Tests hors-ligne du RetryInterceptor et de l'ErrorClassificationInterceptor (chaîne simulée).
 */
package org.koitharu.kotatsu.core.network

import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koitharu.kotatsu.core.exceptions.DeadDomainException
import org.koitharu.kotatsu.core.util.ext.isDeadDomainError
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetryInterceptorTest {

	private val request = Request.Builder().url("https://example.org/list").build()

	private fun responseOf(code: Int): Response = Response.Builder()
		.request(request)
		.protocol(Protocol.HTTP_1_1)
		.code(code)
		.message("test")
		.body(ByteArray(0).toResponseBody(null))
		.build()

	/** Chaîne simulée qui rejoue le script donné à chaque proceed(). */
	private class FakeChain(
		private val request: Request,
		private val script: Iterator<Any>, // Response | IOException
	) : Interceptor.Chain {

		var proceedCalls = 0
			private set

		private val call = object : Call {
			override fun request(): Request = request
			override fun cancel() = Unit
			override fun enqueue(responseCallback: okhttp3.Callback) = Unit
			override fun execute(): Response = throw UnsupportedOperationException()
			override fun isCanceled(): Boolean = false
			override fun isExecuted(): Boolean = false
			override fun clone(): Call = throw UnsupportedOperationException()
			override fun timeout(): okio.Timeout = okio.Timeout()
			override fun <T : Any> tag(type: kotlin.reflect.KClass<T>): T? = null
			override fun <T> tag(type: Class<out T>): T? = null
			override fun <T : Any> tag(type: kotlin.reflect.KClass<T>, fallback: () -> T): T = fallback()
			override fun <T : Any> tag(type: Class<T>, fallback: () -> T): T = fallback()
		}

		override fun call(): Call = call
		override fun connection(): Connection? = null
		override fun request(): Request = request
		override fun connectTimeoutMillis(): Int = 10_000
		override fun readTimeoutMillis(): Int = 10_000
		override fun writeTimeoutMillis(): Int = 10_000
		override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
		override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
		override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this

		override fun proceed(request: Request): Response {
			proceedCalls++
			val next = script.next()
			return when (next) {
				is Response -> next
				is IOException -> throw next
				else -> throw AssertionError()
			}
		}
	}

	@Test
	fun `retries transient 503 then returns second response`() {
		val chain = FakeChain(request, listOf(responseOf(503), responseOf(200)).iterator())
		val response = RetryInterceptor().intercept(chain)
		assertEquals(200, response.code)
		assertEquals(2, chain.proceedCalls)
	}

	@Test
	fun `does not retry 4xx`() {
		val chain = FakeChain(request, listOf(responseOf(404)).iterator())
		val response = RetryInterceptor().intercept(chain)
		assertEquals(404, response.code)
		assertEquals(1, chain.proceedCalls)
	}

	@Test
	fun `does not retry dead domain`() {
		val chain = FakeChain(request, listOf(UnknownHostException("manga4life.com")).iterator())
		assertThrows(UnknownHostException::class.java) { RetryInterceptor().intercept(chain) }
		assertEquals(1, chain.proceedCalls)
	}

	@Test
	fun `retries transient timeout`() {
		val chain = FakeChain(request, listOf(SocketTimeoutException("read timed out"), responseOf(200)).iterator())
		val response = RetryInterceptor().intercept(chain)
		assertEquals(200, response.code)
		assertEquals(2, chain.proceedCalls)
	}

	@Test
	fun `gives up after max retries`() {
		val chain = FakeChain(request, listOf(responseOf(502), responseOf(502), responseOf(200)).iterator())
		val response = RetryInterceptor().intercept(chain)
		assertEquals(502, response.code)
		assertEquals(2, chain.proceedCalls)
	}

	@Test
	fun `unknown host is classified as dead domain`() {
		val chain = FakeChain(request, listOf(UnknownHostException("manga4life.com")).iterator())
		val e = assertThrows(DeadDomainException::class.java) {
			ErrorClassificationInterceptor().intercept(chain)
		}
		assertEquals("example.org", e.hostname)
		assertTrue(e.cause is UnknownHostException)
		assertTrue(e.isDeadDomainError())
	}
}
