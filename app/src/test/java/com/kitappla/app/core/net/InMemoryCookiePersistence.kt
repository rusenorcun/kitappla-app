package com.kitappla.app.core.net

import okhttp3.Cookie

class InMemoryCookiePersistence : CookiePersistence {
    var stored: List<Cookie> = emptyList()
    override fun load(): List<Cookie> = stored
    override fun save(cookies: List<Cookie>) {
        stored = cookies
    }
}

/** Kalıcı depo arızasını (bozuk Keystore vb.) taklit eder. */
class ThrowingCookiePersistence(
    private val failLoad: Boolean = false,
    private val failSave: Boolean = false,
) : CookiePersistence {
    var stored: List<Cookie> = emptyList()
    override fun load(): List<Cookie> {
        if (failLoad) throw SecurityException("load")
        return stored
    }

    override fun save(cookies: List<Cookie>) {
        if (failSave) throw SecurityException("save")
        stored = cookies
    }
}
