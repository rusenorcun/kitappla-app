package com.kitap.app.data.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoverUrlTest {
    private val base = "http://10.0.2.2:8080/"

    @Test
    fun nullOrBlankGivesNull() {
        assertNull(resolveCoverUrl(base, null))
        assertNull(resolveCoverUrl(base, "   "))
    }

    @Test
    fun absoluteUrlIsKept() {
        assertEquals("https://cdn.example.com/a.jpg", resolveCoverUrl(base, "https://cdn.example.com/a.jpg"))
    }

    @Test
    fun relativePathIsResolvedAgainstBase() {
        assertEquals("http://10.0.2.2:8080/uploads/covers/a.jpg", resolveCoverUrl(base, "/uploads/covers/a.jpg"))
        assertEquals("http://10.0.2.2:8080/uploads/covers/a.jpg", resolveCoverUrl(base, "uploads/covers/a.jpg"))
    }
}
