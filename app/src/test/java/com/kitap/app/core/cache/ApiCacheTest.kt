package com.kitap.app.core.cache

import com.kitap.app.core.net.ApiMessages
import com.kitap.app.core.net.ApiResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiCacheTest {
    private var now = 1_000_000L
    private var staleNotices = 0
    private var fetches = 0
    private val cache = ApiCache(onStaleServed = { staleNotices++ }, clock = { now })

    private val network = ApiResult.Failure(ApiMessages.NETWORK, null, true)

    private suspend fun load(
        key: String = "k",
        ttl: Long = 60_000,
        force: Boolean = false,
        result: ApiResult<String>,
    ): ApiResult<String> = cache.cached(key, ttl, force) {
        fetches++
        result
    }

    @Test
    fun freshEntryIsServedWithoutFetching() = runTest {
        load(result = ApiResult.Success("a"))
        val r = load(result = ApiResult.Success("b"))
        assertEquals(ApiResult.Success("a"), r)
        assertEquals(1, fetches)
    }

    @Test
    fun expiredEntryIsRefetched() = runTest {
        load(result = ApiResult.Success("a"))
        now += 60_000
        val r = load(result = ApiResult.Success("b"))
        assertEquals(ApiResult.Success("b"), r)
        assertEquals(2, fetches)
    }

    @Test
    fun forceRefreshBypassesAFreshEntryAndStoresTheNewValue() = runTest {
        load(result = ApiResult.Success("a"))
        assertEquals(ApiResult.Success("b"), load(force = true, result = ApiResult.Success("b")))
        assertEquals(ApiResult.Success("b"), load(result = ApiResult.Success("c")))
        assertEquals(2, fetches)
    }

    @Test
    fun networkFailureServesTheExpiredEntryAndNotifiesOnce() = runTest {
        load(result = ApiResult.Success("a"))
        now += 10 * 60_000
        val r = load(result = network)
        assertEquals(ApiResult.Success("a"), r)
        assertEquals(1, staleNotices)
    }

    @Test
    fun serverErrorAlsoServesTheStaleEntry() = runTest {
        load(result = ApiResult.Success("a"))
        now += 60_000
        val r = load(result = ApiResult.Failure(ApiMessages.SERVER_ERROR, 503))
        assertEquals(ApiResult.Success("a"), r)
        assertEquals(1, staleNotices)
    }

    @Test
    fun clientErrorIsNeverMaskedByTheCache() = runTest {
        load(result = ApiResult.Success("a"))
        now += 60_000
        val failure = ApiResult.Failure(ApiMessages.NOT_FOUND, 404)
        assertEquals(failure, load(result = failure))
        assertEquals(0, staleNotices)
    }

    @Test
    fun failureWithoutAnyEntryIsReturnedAsIs() = runTest {
        assertEquals(network, load(result = network))
        assertEquals(0, staleNotices)
    }

    @Test
    fun aFailedRefetchDoesNotDropTheStaleEntry() = runTest {
        load(result = ApiResult.Success("a"))
        now += 60_000
        load(result = network)
        load(result = network)
        assertEquals(2, staleNotices)
    }

    @Test
    fun removePrefixDropsOnlyMatchingKeys() = runTest {
        load(key = "donation:list:0", result = ApiResult.Success("l"))
        load(key = "donation:detail:1", result = ApiResult.Success("d"))
        load(key = "pickup", result = ApiResult.Success("p"))
        cache.removePrefix("donation:")
        fetches = 0
        load(key = "donation:list:0", result = ApiResult.Success("l2"))
        load(key = "donation:detail:1", result = ApiResult.Success("d2"))
        assertEquals(ApiResult.Success("p"), load(key = "pickup", result = ApiResult.Success("p2")))
        assertEquals(2, fetches)
    }

    @Test
    fun removeDropsOnlyThatExactKey() = runTest {
        load(key = "detail:1", result = ApiResult.Success("a"))
        load(key = "detail:12", result = ApiResult.Success("b"))
        cache.remove("detail:1")
        fetches = 0
        assertEquals(ApiResult.Success("b"), load(key = "detail:12", result = ApiResult.Success("x")))
        assertEquals(ApiResult.Success("y"), load(key = "detail:1", result = ApiResult.Success("y")))
        assertEquals(1, fetches)
    }

    @Test
    fun clearDropsEverythingIncludingStaleFallbacks() = runTest {
        load(result = ApiResult.Success("a"))
        cache.clear()
        assertEquals(network, load(result = network))
        assertEquals(0, staleNotices)
    }

    @Test
    fun oldestEntryIsEvictedBeyondMaxEntries() = runTest {
        val small = ApiCache(onStaleServed = {}, clock = { now }, maxEntries = 2)
        suspend fun get(key: String) = small.cached(key, 60_000) { fetches++; ApiResult.Success(key) }
        get("a"); get("b"); get("c")
        fetches = 0
        get("b"); get("c")
        assertEquals(0, fetches)
        get("a")
        assertTrue(fetches == 1)
    }
}
