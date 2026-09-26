package com.kitappla.app.data.dto

/**
 * Şikâyet gerekçeleri: backend `domain/ReportReason` enum'unun adları ve web'deki etiketleri, aynı sırayla.
 * Sunucu listede olmayan bir adı "Geçersiz şikâyet gerekçesi" diye reddeder; yeni gerekçe önce backend'e eklenmeli.
 */
object ReportReasons {
    val ALL: List<Pair<String, String>> = listOf(
        "TACIZ" to "Taciz, hakaret ya da tehdit",
        "UYGUNSUZ" to "Uygunsuz içerik",
        "SPAM" to "Spam ya da reklam",
        "SAHTE" to "Sahte ilan ya da yanıltıcı bilgi",
        "GELMEDI" to "Buluşmaya gelmedi",
        "HASARLI" to "Kitap hasarlı, eksik veya ilandakinden farklı",
        "TESLIMAT_SORUNU" to "Teslimat gerçekleşmedi veya teslimat sorunu",
        "TICARET" to "Satış ya da ticari amaç",
        "DIGER" to "Diğer",
    )

    const val DEFAULT = "SAHTE"

    fun label(code: String?): String = ALL.firstOrNull { it.first == code }?.second ?: code.orEmpty()
}
