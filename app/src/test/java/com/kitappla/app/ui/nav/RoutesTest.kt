package com.kitappla.app.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {
    @Test
    fun publicRoutesDoNotRequireAuth() {
        listOf(
            Routes.KESFET, Routes.ISTEKLER, Routes.kitapDetay(5), Routes.SSS, Routes.KURALLAR,
            Routes.GIZLILIK, Routes.ILETISIM, Routes.LOGIN, Routes.REGISTER,
            Routes.SIFREMI_UNUTTUM, Routes.SIFRE_SIFIRLA, Routes.ADMIN_LOGIN,
            // E-posta bağlantısından (oturum olmadan) açılabilen rotalar; sorgu kısmı karara karışmamalı.
            Routes.sifreSifirla("abc"), Routes.epostaDogrulandi(), Routes.epostaDogrulandi(invalid = true),
            Routes.epostaDogrula("abc"),
        ).forEach { assertFalse("$it herkese açık olmalı", Routes.requiresAuth(it)) }
    }

    @Test
    fun memberRoutesRequireAuth() {
        listOf(
            Routes.TAKAS, Routes.MESAJLAR, Routes.PANOM, Routes.BILDIRIMLER, Routes.BAGIS_YENI,
            Routes.ISTEK_YENI, Routes.PROFIL, "sohbet/3", "sikayet/bagis/4", Routes.BAGISLARIM,
            Routes.ALDIKLARIM, Routes.TAKAS_TEKLIF,
            Routes.HESAP_DOGRULAMA, Routes.hesapDogrulamaAfterRegister(emailSent = false),
            Routes.ogrenciDogrulama("abc"), Routes.takasTeklif(3),
        ).forEach { assertTrue("$it giriş istemeli", Routes.requiresAuth(it)) }
    }

    @Test
    fun tabsAreTheFiveMainDestinationsStartingWithKesfet() {
        assertEquals(
            listOf(Routes.KESFET, Routes.ISTEKLER, Routes.TAKAS, Routes.MESAJLAR, Routes.PANOM),
            Routes.TABS,
        )
    }

    @Test
    fun kitapDetayBuildsPath() {
        assertEquals("kitap/42", Routes.kitapDetay(42))
    }

    @Test
    fun ogrenciDogrulamaBuildsPathWithOptionalToken() {
        assertEquals("profil/ogrenci", Routes.ogrenciDogrulama())
        assertEquals("profil/ogrenci", Routes.ogrenciDogrulama(null))
        assertEquals("profil/ogrenci", Routes.ogrenciDogrulama(" "))
        assertEquals("profil/ogrenci?token=xyz123", Routes.ogrenciDogrulama("xyz123"))
    }

    @Test
    fun adminDocumentRouteEncodesTheName() {
        assertEquals("admin/belgeler/5?ad=Ay%C5%9Fe%20Kaya", Routes.adminBelge(5, "Ayşe Kaya"))
        assertEquals("admin/belgeler/5?ad=A%26B%3Fx", Routes.adminBelge(5, "A&B?x"))
    }

    @Test
    fun emailLinkRoutesBuildPaths() {
        assertEquals("sifre-sifirla", Routes.sifreSifirla())
        assertEquals("sifre-sifirla?token=t", Routes.sifreSifirla("t"))
        assertEquals("eposta-dogrulandi", Routes.epostaDogrulandi())
        assertEquals("eposta-dogrulandi?durum=gecersiz", Routes.epostaDogrulandi(invalid = true))
        assertEquals("eposta-dogrula?token=t", Routes.epostaDogrula("t"))
    }

    @Test
    fun pendingLoginTargetIsKeptWhileMovingWithinAuthFlow() {
        listOf(
            Routes.LOGIN, Routes.REGISTER, Routes.SIFREMI_UNUTTUM, Routes.SIFRE_SIFIRLA, Routes.SIFRE_SIFIRLA_PATTERN,
            Routes.ADMIN_LOGIN,
        ).forEach { assertTrue("$it kimlik akışında", Routes.shouldKeepPendingRoute(it)) }
    }

    @Test
    fun pendingLoginTargetIsKeptUntilFirstDestinationIsKnown() {
        assertTrue(Routes.shouldKeepPendingRoute(null))
    }

    @Test
    fun pendingLoginTargetIsDroppedOnceUserLeavesAuthFlow() {
        listOf(Routes.KESFET, Routes.ISTEKLER, Routes.PANOM, Routes.kitapDetay(5), Routes.SSS, Routes.KITAP_DETAY)
            .forEach { assertFalse("$it kimlik akışı değil", Routes.shouldKeepPendingRoute(it)) }
    }
}
