package com.kitappla.app

import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConfigTest {
    @Test
    fun apiBaseUrlIsHttpAndEndsWithSlash() {
        val url = BuildConfig.API_BASE_URL
        assertTrue(url.startsWith("http"))
        assertTrue("Retrofit için taban adres '/' ile bitmeli", url.endsWith("/"))
    }
}
