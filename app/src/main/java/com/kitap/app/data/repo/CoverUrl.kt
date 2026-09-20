package com.kitap.app.data.repo

/** Sunucu kapak adresi göreli (`/uploads/covers/x.jpg`) ya da mutlak olabilir. */
fun resolveCoverUrl(baseUrl: String, cover: String?): String? {
    val c = cover?.trim().orEmpty()
    if (c.isEmpty()) return null
    if (c.startsWith("http://") || c.startsWith("https://")) return c
    return baseUrl.trimEnd('/') + "/" + c.trimStart('/')
}
