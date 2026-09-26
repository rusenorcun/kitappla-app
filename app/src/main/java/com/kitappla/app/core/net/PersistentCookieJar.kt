package com.kitappla.app.core.net

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

interface CookiePersistence {
    fun load(): List<Cookie>
    fun save(cookies: List<Cookie>)
}

/**
 * Çerezleri bellekte tutar ve her değişiklikte kalıcı depoya yazar. Tek çerez deposu vardır.
 * Kalıcı depo en iyi çabayla kullanılır: okuma/yazma hatası (ör. bozuk Keystore) ne çökme ne de oturum
 * kaymasına yol açar; bellekteki liste her zaman geçerlidir.
 */
class PersistentCookieJar(
    private val persistence: CookiePersistence,
    private val clock: () -> Long = System::currentTimeMillis,
) : CookieJar {
    private val lock = Any()
    private val cookies: MutableList<Cookie> =
        try {
            persistence.load().toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = synchronized(lock) {
        for (incoming in cookies) {
            this.cookies.removeAll {
                it.name == incoming.name && it.domain == incoming.domain && it.path == incoming.path
            }
            this.cookies.add(incoming)
        }
        prune()
        persist()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(lock) {
        prune()
        cookies.filter { it.matches(url) }
    }

    fun clear() = synchronized(lock) {
        cookies.clear()
        persist()
    }

    private fun persist() {
        try {
            persistence.save(cookies.toList())
        } catch (e: Exception) {
            // Bellek yetkili kalır; kalıcı yazma başarısızlığı yutulur.
        }
    }

    private fun prune() {
        cookies.removeAll { it.expiresAt < clock() }
    }
}
