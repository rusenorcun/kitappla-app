package com.kitappla.app.ui.nav

import java.net.URI
import java.net.URLDecoder

/**
 * E-posta bağlantılarından gelen adresleri uygulama rotasına çevirir. Uygulamadan başlatılan işlemlerin bağlantıları
 * `https://www.kitappla.com/uygulamada-ac/...` yolundadır ve doğrulanmış web bağlantısı (App Link) olarak tarayıcıya
 * uğramadan buraya gelir. Bağlantı yine de tarayıcıda açılırsa web sayfası kullanıcıyı `kitappla://` şemasıyla geri
 * gönderir (backend `fragments.html` → `uygulamayaDon`).
 *
 * | Bağlantı                                             | Rota                              |
 * |------------------------------------------------------|-----------------------------------|
 * | `https://…/uygulamada-ac/eposta-dogrula?token=T`     | hesap doğrulama (onayı ekran yapar)|
 * | `https://…/uygulamada-ac/sifre-sifirla?token=T`      | yeni şifre (jeton dolu)           |
 * | `https://…/uygulamada-ac/okul-eposta?token=T`        | okul e-postası onayı              |
 * | `https://…/profil/ogrenci/eposta/onay?token=T`       | okul e-postası onayı              |
 * | `https://…/sifre-sifirla?token=T`                    | yeni şifre (jeton dolu)           |
 * | `kitappla://eposta-dogrulandi[?durum=gecersiz]`      | hesap doğrulama sonucu            |
 * | `kitappla://sifre-sifirla?token=T`                   | yeni şifre (jeton dolu)           |
 * | `kitappla://profil/ogrenci?token=T`                  | okul e-postası onayı              |
 * | `kitappla://hesap-dogrulama`                         | "gelen kutunu kontrol et"         |
 *
 * Jeton rota dizgesine eklendiği için yalnızca URL-güvenli Base64 karakterleri kabul edilir; başka bir şey içeren
 * jeton (`&`, `/`, `?` ile rotaya parametre sokma denemesi dahil) yok sayılır. Tanınmayan bağlantı `null` döner:
 * uygulama yalnızca açılır, kullanıcı hiçbir yere yönlendirilmez.
 */
object DeepLinks {
    const val SCHEME = "kitappla"

    private val SAFE_TOKEN = Regex("[A-Za-z0-9_-]{1,512}")

    fun toRoute(link: String?): String? {
        if (link.isNullOrBlank()) return null
        val uri = runCatching { URI(link.trim()) }.getOrNull() ?: return null
        val query = parseQuery(uri.rawQuery)
        val token = query["token"]?.takeIf { SAFE_TOKEN.matches(it) }
        return when (uri.scheme?.lowercase()) {
            SCHEME -> appLink(host = uri.host ?: uri.authority, path = uri.path.orEmpty(), query = query, token = token)
            "http", "https" -> webLink(path = uri.path.orEmpty(), token = token)
            else -> null
        }
    }

    /** `kitappla://<yol>`: URI ayrıştırıcısı ilk yol parçasını "host" sayar (`kitappla://profil/ogrenci` → host=profil). */
    private fun appLink(host: String?, path: String, query: Map<String, String>, token: String?): String? {
        val full = (host.orEmpty() + path).trim('/').lowercase()
        return when (full) {
            Routes.EPOSTA_DOGRULANDI -> Routes.epostaDogrulandi(invalid = query["durum"] == Routes.EPOSTA_DURUM_GECERSIZ)
            Routes.HESAP_DOGRULAMA -> Routes.HESAP_DOGRULAMA
            Routes.SIFRE_SIFIRLA -> Routes.sifreSifirla(token)
            Routes.OGRENCI_DOGRULAMA -> Routes.ogrenciDogrulama(token)
            else -> null
        }
    }

    private fun webLink(path: String, token: String?): String? = when (path.trimEnd('/').lowercase()) {
        "/profil/ogrenci/eposta/onay", "/uygulamada-ac/okul-eposta" -> Routes.ogrenciDogrulama(token)
        "/sifre-sifirla", "/uygulamada-ac/sifre-sifirla" -> Routes.sifreSifirla(token)
        // Jetonsuz (ya da bozuk jetonlu) bağlantı onaylanamaz: doğrudan "bağlantı geçersiz" gösterilir.
        "/uygulamada-ac/eposta-dogrula" -> token?.let(Routes::epostaDogrula) ?: Routes.epostaDogrulandi(invalid = true)
        else -> null
    }

    private fun parseQuery(raw: String?): Map<String, String> {
        if (raw.isNullOrEmpty()) return emptyMap()
        return raw.split('&').mapNotNull { part ->
            val key = part.substringBefore('=')
            if (key.isEmpty()) return@mapNotNull null
            val value = part.substringAfter('=', missingDelimiterValue = "")
            key to (runCatching { URLDecoder.decode(value, "UTF-8") }.getOrNull() ?: return@mapNotNull null)
        }.toMap()
    }
}
