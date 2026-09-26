package com.kitappla.app.data.repo

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.data.api.DonationApi
import com.kitappla.app.data.api.RequestApi
import com.kitappla.app.data.api.SwapApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Bağış oluşturan başka akışlar (takastan bağışa, isteği karşılama) Keşfet ön belleğini geçersiz kılmalı. */
class DonationCacheInvalidationTest {
    private lateinit var server: MockWebServer
    private val cache = ApiCache(onStaleServed = {}, clock = { 0L })

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() = server.shutdown()

    private fun donations() = DonationRepository(testRetrofit(server).create(DonationApi::class.java), cache)

    private fun enqueueEmptyPage() =
        server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]"))

    @Test
    fun movingASwapBookToDonationRefreshesTheDiscoverList() = runTest {
        val swaps = SwapRepository(testRetrofit(server).create(SwapApi::class.java), cache)
        enqueueEmptyPage()
        server.enqueue(MockResponse().setBody("""{"id":5,"status":"OPEN"}"""))
        enqueueEmptyPage()

        donations().page(0)
        swaps.moveToDonation(3)
        donations().page(0)

        assertEquals(3, server.requestCount)
    }

    @Test
    fun fulfillingARequestRefreshesTheDiscoverList() = runTest {
        val requests = RequestRepository(testRetrofit(server).create(RequestApi::class.java), cache)
        enqueueEmptyPage()
        server.enqueue(MockResponse().setBody("""{"id":5,"status":"FULFILLED"}"""))
        enqueueEmptyPage()

        donations().page(0)
        requests.fulfillRequest(7)
        donations().page(0)

        assertEquals(3, server.requestCount)
    }
}
