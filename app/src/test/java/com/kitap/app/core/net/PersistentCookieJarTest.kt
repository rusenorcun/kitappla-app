package com.kitap.app.core.net

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistentCookieJarTest {
    private val url = "http://10.0.2.2:8080/api/v1/me".toHttpUrl()

    private fun session(value: String, expiresAt: Long? = null) =
        Cookie.Builder().name("KITAPLA_SESSION").value(value).hostOnlyDomain("10.0.2.2").path("/")
            .httpOnly().apply { if (expiresAt != null) expiresAt(expiresAt) }.build()

    @Test
    fun savedCookieIsReturnedForMatchingUrl() {
        val jar = PersistentCookieJar(InMemoryCookiePersistence())
        jar.saveFromResponse(url, listOf(session("abc")))
        assertEquals(listOf("abc"), jar.loadForRequest(url).map { it.value })
    }

    @Test
    fun cookieSurvivesNewJarInstanceOverSamePersistence() {
        val store = InMemoryCookiePersistence()
        PersistentCookieJar(store).saveFromResponse(url, listOf(session("abc")))
        assertEquals(listOf("abc"), PersistentCookieJar(store).loadForRequest(url).map { it.value })
    }

    @Test
    fun sameNameCookieIsReplaced() {
        val jar = PersistentCookieJar(InMemoryCookiePersistence())
        jar.saveFromResponse(url, listOf(session("eski")))
        jar.saveFromResponse(url, listOf(session("yeni")))
        assertEquals(listOf("yeni"), jar.loadForRequest(url).map { it.value })
    }

    @Test
    fun expiredCookieIsDroppedAndNotPersisted() {
        val store = InMemoryCookiePersistence()
        val jar = PersistentCookieJar(store, clock = { 10_000L })
        jar.saveFromResponse(url, listOf(session("bayat", expiresAt = 5_000L)))
        assertTrue(jar.loadForRequest(url).isEmpty())
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun clearRemovesEverythingIncludingPersistedCopy() {
        val store = InMemoryCookiePersistence()
        val jar = PersistentCookieJar(store)
        jar.saveFromResponse(url, listOf(session("abc")))
        jar.clear()
        assertTrue(jar.loadForRequest(url).isEmpty())
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun failingSaveDoesNotThrowAndMemoryStaysAuthoritative() {
        val jar = PersistentCookieJar(ThrowingCookiePersistence(failSave = true))
        jar.saveFromResponse(url, listOf(session("abc")))
        assertEquals(listOf("abc"), jar.loadForRequest(url).map { it.value })
    }

    @Test
    fun failingSaveDoesNotBreakClear() {
        val jar = PersistentCookieJar(ThrowingCookiePersistence(failSave = true))
        jar.saveFromResponse(url, listOf(session("abc")))
        jar.clear()
        assertTrue(jar.loadForRequest(url).isEmpty())
    }

    @Test
    fun failingLoadConstructsEmptyJar() {
        val jar = PersistentCookieJar(ThrowingCookiePersistence(failLoad = true))
        assertTrue(jar.loadForRequest(url).isEmpty())
        jar.saveFromResponse(url, listOf(session("abc")))
        assertEquals(listOf("abc"), jar.loadForRequest(url).map { it.value })
    }
}
