package com.kitappla.app.core.net

import okhttp3.Cookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookieCodecTest {
    @Test
    fun roundTripKeepsAllFields() {
        val original = listOf(
            Cookie.Builder().name("KITAPPLA_SESSION").value("abc").hostOnlyDomain("10.0.2.2")
                .path("/").httpOnly().build(),
            Cookie.Builder().name("x").value("y").domain("kitappla.com").path("/api")
                .secure().expiresAt(4_102_444_800_000L).build(),
        )
        val decoded = CookieCodec.decode(CookieCodec.encode(original))
        assertEquals(original.size, decoded.size)
        original.zip(decoded).forEach { (a, b) ->
            assertEquals(a.name, b.name)
            assertEquals(a.value, b.value)
            assertEquals(a.domain, b.domain)
            assertEquals(a.path, b.path)
            assertEquals(a.secure, b.secure)
            assertEquals(a.httpOnly, b.httpOnly)
            assertEquals(a.hostOnly, b.hostOnly)
            assertEquals(a.expiresAt, b.expiresAt)
        }
    }

    @Test
    fun emptyListRoundTrips() {
        assertTrue(CookieCodec.decode(CookieCodec.encode(emptyList())).isEmpty())
    }
}
