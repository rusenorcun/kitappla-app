package com.kitappla.app.ui.nav

object Routes {
    // Alt sekmeler
    const val KESFET = "kesfet"
    const val ISTEKLER = "istekler"
    const val TAKAS = "takas"
    const val MESAJLAR = "mesajlar"
    const val PANOM = "panom"
    val TABS = listOf(KESFET, ISTEKLER, TAKAS, MESAJLAR, PANOM)

    // Üye rotaları
    const val BILDIRIMLER = "bildirimler"
    const val BAGIS_YENI = "bagis/yeni"
    const val ISTEK_YENI = "istek/yeni"
    const val KITAP_DETAY = "kitap/{id}"
    fun kitapDetay(id: Long) = "kitap/$id"
    const val BAGISLARIM = "bagislarim"
    const val ALDIKLARIM = "aldiklarim"
    const val ISTEKLERIM = "isteklerim"
    const val KARSILADIKLARIM = "karsiladiklarim"
    const val TAKASLARIM = "takaslarim"
    const val TAKAS_KITAPLARIM = "takas/kitaplarim"
    const val TAKAS_KITAP_EKLE = "takas/kitap-ekle"
    const val TAKAS_TEKLIF = "takas/teklif"
    /** Sorgu argümanı tanımlanmazsa Navigation onu atar ve teklif hedefsiz kalır. */
    const val TAKAS_TEKLIF_PATTERN = "takas/teklif?targetBookId={targetBookId}"
    const val TAKAS_TEKLIF_TARGET_ARG = "targetBookId"
    fun takasTeklif(targetBookId: Long? = null) = if (targetBookId != null) "takas/teklif?targetBookId=$targetBookId" else "takas/teklif"
    const val TAKAS_TEKLIF_DETAY = "takas/teklif/{id}"
    fun takasTeklifDetay(id: Long) = "takas/teklif/$id"
    const val SIKAYETLERIM = "sikayetlerim"
    const val SIKAYET_ET = "sikayet/{kind}/{refId}"
    fun sikayetEt(kind: String, refId: Long) = "sikayet/$kind/$refId"
    const val PROFIL = "profil"
    /** E-postası doğrulanmamış üyenin "gelen kutunu kontrol et" ekranı (web: /hesap-dogrulama). */
    const val HESAP_DOGRULAMA = "hesap-dogrulama"
    /** Kayıttan hemen sonra: `kayit` = [KAYIT_GONDERILDI] ya da [KAYIT_GONDERILMEDI] (doğrulama e-postası durumu). */
    const val HESAP_DOGRULAMA_PATTERN = "hesap-dogrulama?kayit={kayit}"
    const val KAYIT_GONDERILDI = "gonderildi"
    const val KAYIT_GONDERILMEDI = "gonderilmedi"
    fun hesapDogrulamaAfterRegister(emailSent: Boolean) =
        "$HESAP_DOGRULAMA?kayit=" + (if (emailSent) KAYIT_GONDERILDI else KAYIT_GONDERILMEDI)
    /** Web'de bağlantı onaylandıktan sonra `kitappla://eposta-dogrulandi[?durum=gecersiz]` ile dönülen sonuç ekranı. */
    const val EPOSTA_DOGRULANDI = "eposta-dogrulandi"
    const val EPOSTA_DOGRULANDI_PATTERN = "eposta-dogrulandi?durum={durum}"
    const val EPOSTA_DURUM_GECERSIZ = "gecersiz"
    fun epostaDogrulandi(invalid: Boolean = false) =
        if (invalid) "$EPOSTA_DOGRULANDI?durum=$EPOSTA_DURUM_GECERSIZ" else EPOSTA_DOGRULANDI
    /** Doğrulama bağlantısı doğrudan uygulamada açıldı (App Link): jetonu ekran onaylatır (web: /eposta-dogrula). */
    const val EPOSTA_DOGRULA = "eposta-dogrula"
    const val EPOSTA_DOGRULA_PATTERN = "eposta-dogrula?token={token}"
    fun epostaDogrula(token: String) = "$EPOSTA_DOGRULA?token=$token"
    const val OGRENCI_DOGRULAMA = "profil/ogrenci"
    const val OGRENCI_DOGRULAMA_PATTERN = "profil/ogrenci?token={token}"
    fun ogrenciDogrulama(token: String? = null) = if (token.isNullOrBlank()) OGRENCI_DOGRULAMA else "profil/ogrenci?token=$token"
    const val SOHBET = "sohbet/{id}"
    fun sohbet(id: Long) = "sohbet/$id"
    const val SSS = "sss"
    const val KURALLAR = "kurallar"
    const val GIZLILIK = "gizlilik"
    const val ILETISIM = "iletisim"

    // Kimlik
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SIFREMI_UNUTTUM = "sifremi-unuttum"
    const val SIFRE_SIFIRLA = "sifre-sifirla"
    const val SIFRE_SIFIRLA_PATTERN = "sifre-sifirla?token={token}"
    fun sifreSifirla(token: String? = null) = if (token.isNullOrBlank()) SIFRE_SIFIRLA else "sifre-sifirla?token=$token"
    const val ADMIN_LOGIN = "admin/login"

    // Yönetim grafiği
    const val ADMIN_PANO = "admin/pano"
    const val ADMIN_BELGELER = "admin/belgeler"
    const val ADMIN_BELGE = "admin/belgeler/{id}?ad={ad}"
    /** Ad sorguya yazıldığı için kodlanır (Navigation `%XX`'i çözer; `+`'yı boşluk saymaz). */
    fun adminBelge(userId: Long, name: String) =
        "admin/belgeler/$userId?ad=" + java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20")
    const val ADMIN_UYELER = "admin/uyeler"
    const val ADMIN_ICERIK = "admin/icerik"
    const val ADMIN_NOKTALAR = "admin/noktalar"
    const val ADMIN_SIKAYETLER = "admin/sikayetler"
    const val ADMIN_SIKAYET_DETAY = "admin/sikayetler/{id}"
    fun adminSikayetDetay(id: Long) = "admin/sikayetler/$id"
    const val ADMIN_MESAJLAR = "admin/mesajlar"
    const val ADMIN_IZLEC = "admin/izlec"

    /** Web'de herkese açık sayfalar + kimlik rotaları (ilk yol parçasına göre). */
    private val PUBLIC_PREFIXES = setOf(
        "kesfet", "istekler", "kitap", "sss", "kurallar", "gizlilik", "iletisim",
        "login", "register", "sifremi-unuttum", "sifre-sifirla", "admin", "eposta-dogrulandi", "eposta-dogrula",
    )

    /** Sorgu kısmı (`?token=...`) yok sayılır; aksi hâlde "sifre-sifirla?token=x" giriş isteyen rota sanılırdı. */
    fun requiresAuth(route: String): Boolean = route.substringBefore('?').substringBefore('/') !in PUBLIC_PREFIXES

    /** Giriş/kayıt/şifre/yönetici girişi ekranları: kapıdan başlayan giriş akışının içinde kalınan hedefler. */
    private val AUTH_FLOW = setOf(LOGIN, REGISTER, SIFREMI_UNUTTUM, SIFRE_SIFIRLA, SIFRE_SIFIRLA_PATTERN, ADMIN_LOGIN)

    /**
     * Misafir giriş kapısından başlayan "giriş sonrası hedefe dön" isteği yalnızca kullanıcı kimlik akışında
     * kaldıkça geçerlidir; akıştan çıkınca (ör. Giriş'ten geri) hedef unutulur. İlk hedef henüz bilinmiyorsa (null) korunur.
     */
    fun shouldKeepPendingRoute(currentRoute: String?): Boolean = currentRoute == null || currentRoute in AUTH_FLOW
}
