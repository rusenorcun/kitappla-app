package com.kitappla.app.core.net

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.Cookie

@Serializable
private data class StoredCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
)

object CookieCodec {
    private val serializer = ListSerializer(StoredCookie.serializer())

    fun encode(cookies: List<Cookie>): String =
        KitapplaJson.instance.encodeToString(serializer, cookies.map { it.toStored() })

    fun decode(text: String): List<Cookie> =
        KitapplaJson.instance.decodeFromString(serializer, text).map { it.toCookie() }

    private fun Cookie.toStored() =
        StoredCookie(name, value, expiresAt, domain, path, secure, httpOnly, hostOnly)

    private fun StoredCookie.toCookie(): Cookie {
        val builder = Cookie.Builder().name(name).value(value).expiresAt(expiresAt).path(path)
        if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()
        return builder.build()
    }
}
