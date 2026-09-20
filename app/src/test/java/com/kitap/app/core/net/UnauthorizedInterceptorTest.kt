package com.kitap.app.core.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UnauthorizedInterceptorTest {
    private lateinit var server: MockWebServer
    private var calls = 0
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        client = OkHttpClient.Builder().addInterceptor(UnauthorizedInterceptor { calls++ }).build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun get(path: String, code: Int) {
        server.enqueue(MockResponse().setResponseCode(code))
        client.newCall(Request.Builder().url(server.url(path)).build()).execute().close()
    }

    @Test
    fun unauthorizedOnProtectedEndpointTriggersCallback() {
        get("/api/v1/me", 401)
        assertEquals(1, calls)
    }

    @Test
    fun unauthorizedOnAuthEndpointDoesNot() {
        get("/api/v1/auth/login", 401)
        assertEquals(0, calls)
    }

    @Test
    fun otherStatusesDoNot() {
        get("/api/v1/me", 200)
        get("/api/v1/me", 403)
        assertEquals(0, calls)
    }
}
