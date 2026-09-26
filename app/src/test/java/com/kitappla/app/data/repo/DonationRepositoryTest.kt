package com.kitappla.app.data.repo

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.ApiMessages
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.KitapplaJson
import com.kitappla.app.data.api.DonationApi
import com.kitappla.app.data.dto.CreateDonationBody
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal fun donationJson(id: Int) =
    """{"id":$id,"book":{"id":$id,"title":"Kitap $id","author":"Yazar"},"donorName":"Ayşe","quantity":1,"remaining":1}"""

internal fun testRetrofit(server: MockWebServer): Retrofit = Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addConverterFactory(KitapplaJson.instance.asConverterFactory("application/json".toMediaType()))
    .build()

internal fun donationRepository(
    server: MockWebServer,
    cache: ApiCache = ApiCache(onStaleServed = {}, clock = System::currentTimeMillis),
): DonationRepository = DonationRepository(testRetrofit(server).create(DonationApi::class.java), cache)

class DonationRepositoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() { runCatching { server.shutdown() } }

    @Test
    fun requestAlwaysCarriesPageSizeAndAvailable() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]"))
        donationRepository(server).page(0)
        assertEquals("/api/v1/donations?page=0&size=24&available=true", server.takeRequest().path)
    }

    @Test
    fun readsTotalFromHeader() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "50").setBody("[${donationJson(1)}]"))
        val r = donationRepository(server).page(0) as ApiResult.Success
        assertEquals(1, r.value.items.size)
        assertEquals(50L, r.value.total)
    }

    @Test
    fun missingHeaderFallsBackToItemCount() = runTest {
        server.enqueue(MockResponse().setBody("[${donationJson(1)},${donationJson(2)}]"))
        val r = donationRepository(server).page(0) as ApiResult.Success
        assertEquals(2L, r.value.total)
    }

    @Test
    fun serverErrorBecomesFailure() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val r = donationRepository(server).page(0)
        assertEquals(ApiResult.Failure(ApiMessages.SERVER_ERROR, 500), r)
    }

    private var now = 0L
    private var staleNotices = 0
    private fun cachedRepository() =
        donationRepository(server, ApiCache(onStaleServed = { staleNotices++ }, clock = { now }))

    @Test
    fun repeatedPageRequestIsServedFromCache() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "1").setBody("[${donationJson(1)}]"))
        val repo = cachedRepository()
        repo.page(0)
        val second = repo.page(0) as ApiResult.Success
        assertEquals(1L, second.value.total)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun differentPageOrQueryIsACacheMiss() = runTest {
        repeat(3) { server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]")) }
        val repo = cachedRepository()
        repo.page(0)
        repo.page(1)
        repo.page(0, query = "harry")
        assertEquals(3, server.requestCount)
    }

    @Test
    fun forceRefreshGoesToTheNetwork() = runTest {
        repeat(2) { server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]")) }
        val repo = cachedRepository()
        repo.page(0)
        repo.page(0, forceRefresh = true)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun cachedPageExpiresAfterOneMinute() = runTest {
        repeat(2) { server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]")) }
        val repo = cachedRepository()
        repo.page(0)
        now += 61_000
        repo.page(0)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun detailIsCachedPerId() = runTest {
        server.enqueue(MockResponse().setBody(donationJson(1)))
        server.enqueue(MockResponse().setBody(donationJson(2)))
        val repo = cachedRepository()
        repo.getDonation(1)
        repo.getDonation(1)
        repo.getDonation(2)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun networkFailureShowsCachedPageAndRaisesTheNotice() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "1").setBody("[${donationJson(1)}]"))
        val repo = cachedRepository()
        repo.page(0)
        now += 10 * 60_000
        server.shutdown()   // bağlantı reddedilir → IOException
        val result = repo.page(0)
        assertTrue(result.toString(), result is ApiResult.Success)
        assertEquals(1, (result as ApiResult.Success).value.items.size)
        assertEquals(1, staleNotices)
    }

    @Test
    fun successfulMutationDropsCachedDonations() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]"))
        server.enqueue(MockResponse().setBody(donationJson(1)))
        server.enqueue(MockResponse().setBody("""{"id":9,"status":"OPEN"}"""))
        server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]"))
        server.enqueue(MockResponse().setBody(donationJson(1)))
        val repo = cachedRepository()
        repo.page(0)
        repo.getDonation(1)
        repo.createDonation(CreateDonationBody(title = "Yeni"))
        repo.page(0)
        repo.getDonation(1)
        assertEquals(5, server.requestCount)
    }

    @Test
    fun failedMutationKeepsTheCache() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]"))
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"Kota doldu."}"""))
        val repo = cachedRepository()
        repo.page(0)
        repo.claim(1)
        repo.page(0)
        assertEquals(2, server.requestCount)
    }
}
