package com.kitap.app.core.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CsrfInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        client = OkHttpClient.Builder().addInterceptor(CsrfInterceptor(server.url("/"))).build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun csrf(token: String) = MockResponse().setBody(
        """{"token":"$token","headerName":"X-CSRF-TOKEN","parameterName":"_csrf"}"""
    )

    private fun post(path: String) = client.newCall(
        Request.Builder().url(server.url(path)).post("{}".toRequestBody()).build()
    ).execute()

    @Test
    fun mutatingRequestFetchesTokenFirstAndSendsHeader() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(200))
        assertEquals(200, post("/api/v1/donations").code)

        val first = server.takeRequest()
        assertEquals("GET", first.method)
        assertEquals("/api/v1/auth/csrf", first.path)
        val second = server.takeRequest()
        assertEquals("POST", second.method)
        assertEquals("t1", second.getHeader("X-CSRF-TOKEN"))
    }

    @Test
    fun getRequestsDoNotFetchToken() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/api/v1/donations")).build()).execute().close()
        assertEquals(1, server.requestCount)
        assertNull(server.takeRequest().getHeader("X-CSRF-TOKEN"))
    }

    @Test
    fun tokenIsReusedAcrossRequests() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))
        post("/api/v1/a").close()
        post("/api/v1/b").close()
        assertEquals(3, server.requestCount)
    }

    @Test
    fun forbiddenRefreshesTokenAndRetriesOnce() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(403))
        server.enqueue(csrf("t2"))
        server.enqueue(MockResponse().setResponseCode(200))
        assertEquals(200, post("/api/v1/donations").code)

        repeat(2) { server.takeRequest() }          // csrf(t1), POST(t1) -> 403
        assertEquals("/api/v1/auth/csrf", server.takeRequest().path)
        assertEquals("t2", server.takeRequest().getHeader("X-CSRF-TOKEN"))
    }

    @Test
    fun secondForbiddenIsReturnedAsIs() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(403))
        server.enqueue(csrf("t2"))
        server.enqueue(MockResponse().setResponseCode(403))
        assertEquals(403, post("/api/v1/donations").code)
        assertEquals(4, server.requestCount)
    }

    @Test
    fun successfulLoginInvalidatesToken() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(200))   // login
        server.enqueue(csrf("t2"))
        server.enqueue(MockResponse().setResponseCode(200))   // sonraki POST
        post("/api/v1/auth/login").close()
        post("/api/v1/donations").close()

        repeat(2) { server.takeRequest() }
        assertEquals("/api/v1/auth/csrf", server.takeRequest().path)
        assertEquals("t2", server.takeRequest().getHeader("X-CSRF-TOKEN"))
    }
}
