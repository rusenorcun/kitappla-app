package com.kitappla.app.ui.nav

/**
 * Sunucunun ürettiği web yollarını (bildirim bağlantıları: `/bagislarim`, `/takas/teklifler/5`, `/mesajlar/12`…)
 * uygulama rotasına çevirir. Web yolları uygulama rotalarıyla birebir aynı değildir ve başında `/` vardır; olduğu
 * gibi `navigate()`'e verilirse eşleşen hedef bulunamaz ve uygulama çöker. Karşılığı olmayan yol `null` döner
 * (dokunmak yalnızca bildirimi okundu yapar).
 */
object WebRoutes {
    private val SAME_NAME = setOf(
        Routes.KESFET, Routes.ISTEKLER, Routes.TAKAS, Routes.MESAJLAR, Routes.PANOM, Routes.BILDIRIMLER,
        Routes.BAGISLARIM, Routes.ALDIKLARIM, Routes.ISTEKLERIM, Routes.KARSILADIKLARIM, Routes.SIKAYETLERIM,
        Routes.PROFIL, Routes.HESAP_DOGRULAMA, Routes.SSS, Routes.KURALLAR, Routes.GIZLILIK, Routes.ILETISIM,
    )

    fun toRoute(link: String?): String? {
        if (link.isNullOrBlank()) return null
        val path = link.trim()
            .replace(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://[^/]*"), "")   // tam adres geldiyse şema ve alan adı atılır
            .substringBefore('#')
            .substringBefore('?')
            .trim('/')
            .lowercase()
        if (path in SAME_NAME) return path
        val parts = path.split('/')
        val id = parts.getOrNull(parts.lastIndex)?.toLongOrNull()
        return when {
            path == "takas/takaslarim" -> Routes.TAKASLARIM
            path == "takas/kitaplarim" -> Routes.TAKAS_KITAPLARIM
            path == "profil/ogrenci" -> Routes.OGRENCI_DOGRULAMA
            parts.size == 3 && parts[0] == "takas" && parts[1] == "teklifler" && id != null -> Routes.takasTeklifDetay(id)
            parts.size == 2 && parts[0] == "mesajlar" && id != null -> Routes.sohbet(id)
            parts.size == 2 && parts[0] == "kitap" && id != null -> Routes.kitapDetay(id)
            else -> null
        }
    }
}
