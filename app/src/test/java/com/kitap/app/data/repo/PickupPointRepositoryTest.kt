package com.kitap.app.data.repo

import com.kitap.app.core.cache.ApiCache
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.api.PickupPointApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PickupPointRepositoryTest {
    private lateinit var server: MockWebServer
    private var now = 0L
    private var staleNotices = 0

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() { runCatching { server.shutdown() } }

    private fun repo() = PickupPointRepository(
        testRetrofit(server).create(PickupPointApi::class.java),
        ApiCache(onStaleServed = { staleNotices++ }, clock = { now }),
    )

    private val points = """[{"id":1,"name":"Kütüphane"}]"""

    @Test
    fun pointsAreCachedForTenMinutes() = runTest {
        server.enqueue(MockResponse().setBody(points))
        server.enqueue(MockResponse().setBody(points))
        val repo = repo()
        repo.getActivePoints()
        now += 9 * 60_000
        repo.getActivePoints()
        assertEquals(1, server.requestCount)
        now += 2 * 60_000
        repo.getActivePoints()
        assertEquals(2, server.requestCount)
    }

    @Test
    fun networkFailureServesCachedPoints() = runTest {
        server.enqueue(MockResponse().setBody(points))
        val repo = repo()
        repo.getActivePoints()
        now += 60 * 60_000
        server.shutdown()   // bağlantı reddedilir → IOException
        val result = repo.getActivePoints()
        assertTrue(result.toString(), result is ApiResult.Success)
        assertEquals("Kütüphane", (result as ApiResult.Success).value.single().name)
        assertEquals(1, staleNotices)
    }
}
