package com.kitappla.app.data.repo

import java.net.URI

/**
 * Sunucu kapak adresi göreli (`/uploads/covers/x.jpg`) ya da mutlak olabilir. Linkten getirilen kapaklar mağazanın
 * adresidir ve bazen `http://` ya da `//cdn...` biçimindedir: uygulama şifresiz trafiğe izin vermediği için bunlar
 * önizlemede hiç görünmüyordu. Tarayıcıların karışık içerikte yaptığı gibi https'e yükseltilir; API sunucusunun
 * kendi http adresi (yerel geliştirme) olduğu gibi kalır.
 */
fun resolveCoverUrl(baseUrl: String, cover: String?): String? {
    val c = cover?.trim().orEmpty()
    if (c.isEmpty()) return null
    if (c.startsWith("//")) return "https:$c"
    if (c.startsWith("https://", ignoreCase = true)) return c
    if (c.startsWith("http://", ignoreCase = true)) {
        return if (hostOf(c) == hostOf(baseUrl)) c else "https://" + c.substring("http://".length)
    }
    return baseUrl.trimEnd('/') + "/" + c.trimStart('/')
}

private fun hostOf(url: String): String? = runCatching { URI(url).host?.lowercase() }.getOrNull()
