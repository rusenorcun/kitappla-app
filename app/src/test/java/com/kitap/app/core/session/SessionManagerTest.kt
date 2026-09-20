package com.kitap.app.core.session

import com.kitap.app.core.cache.ApiCache
import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.InMemoryCookiePersistence
import com.kitap.app.core.net.PersistentCookieJar
import com.kitap.app.core.net.ThrowingCookiePersistence
import com.kitap.app.data.dto.UserDto
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {
    private val url = "http://10.0.2.2:8080/".toHttpUrl()
    private val store = InMemoryCookiePersistence()
    private val jar = PersistentCookieJar(store)
    private val cache = ApiCache(onStaleServed = {}, clock = { 0L })
    private val manager = SessionManager(jar, cache)
    private val member = UserDto(id = 1, name = "Ayşe", admin = false)
    private val admin = UserDto(id = 2, name = "Yönetici", admin = true)

    private fun seedCookie() = jar.saveFromResponse(
        url, listOf(Cookie.Builder().name("S").value("v").hostOnlyDomain("10.0.2.2").path("/").build())
    )

    @Test
    fun startsAsLoading() {
        assertEquals(SessionState.Loading, manager.state.value)
    }

    @Test
    fun signedInPicksStateByAdminFlag() {
        manager.signedIn(member)
        assertEquals(SessionState.Member(member), manager.state.value)
        manager.signedIn(admin)
        assertEquals(SessionState.Admin(admin), manager.state.value)
    }

    @Test
    fun signedOutClearsCookiesAndBecomesGuest() {
        seedCookie()
        manager.signedIn(member)
        manager.signedOut()
        assertEquals(SessionState.Guest, manager.state.value)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun markGuestKeepsCookies() {
        seedCookie()
        manager.markGuest()
        assertEquals(SessionState.Guest, manager.state.value)
        assertEquals(1, store.stored.size)
    }

    @Test
    fun onUnauthorizedDropsMemberSessionAndCookies() {
        seedCookie()
        manager.signedIn(member)
        manager.onUnauthorized()
        assertEquals(SessionState.Guest, manager.state.value)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun onUnauthorizedWhileGuestIsNoOp() {
        seedCookie()
        manager.markGuest()
        manager.onUnauthorized()
        assertEquals(1, store.stored.size)
    }

    @Test
    fun signedOutStillBecomesGuestWhenPersistenceFails() {
        val failing = ThrowingCookiePersistence(failSave = true)
        val failingManager = SessionManager(PersistentCookieJar(failing), cache)
        failingManager.signedIn(member)
        failingManager.signedOut()
        assertEquals(SessionState.Guest, failingManager.state.value)
    }

    /** Bağış yanıtları oturuma özel `eligibility` taşır; hesap değişince önceki hesabın verisi gösterilmemeli. */
    private fun fetchesNeededNow(): Int = runBlocking {
        var fetches = 0
        cache.cached("donation:list:0", 60_000) { fetches++; ApiResult.Success("v") }
        fetches
    }

    @Test
    fun signedInDropsCachedData() {
        runBlocking { cache.cached("donation:list:0", 60_000) { ApiResult.Success("eski") } }
        manager.signedIn(member)
        assertEquals(1, fetchesNeededNow())
    }

    @Test
    fun signedOutDropsCachedData() {
        runBlocking { cache.cached("donation:list:0", 60_000) { ApiResult.Success("eski") } }
        manager.signedIn(member)
        runBlocking { cache.cached("donation:list:0", 60_000) { ApiResult.Success("eski") } }
        manager.signedOut()
        assertEquals(1, fetchesNeededNow())
    }

    @Test
    fun onUnauthorizedDropsCachedData() {
        manager.signedIn(member)
        runBlocking { cache.cached("donation:list:0", 60_000) { ApiResult.Success("eski") } }
        manager.onUnauthorized()
        assertEquals(1, fetchesNeededNow())
    }
}
