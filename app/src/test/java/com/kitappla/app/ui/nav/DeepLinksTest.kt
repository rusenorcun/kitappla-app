package com.kitappla.app.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Backend'in `uygulamayaDon` hedefleri (eposta-dogrula.html, sifre-sifirla.html, uygulamada-ac.html) ve App Link yolları. */
class DeepLinksTest {
    @Test
    fun accountVerifiedLinkOpensTheResultScreen() {
        assertEquals("eposta-dogrulandi", DeepLinks.toRoute("kitappla://eposta-dogrulandi"))
        assertEquals("eposta-dogrulandi", DeepLinks.toRoute("kitappla://eposta-dogrulandi/"))
    }

    @Test
    fun invalidAccountLinkCarriesItsStatus() {
        assertEquals("eposta-dogrulandi?durum=gecersiz", DeepLinks.toRoute("kitappla://eposta-dogrulandi?durum=gecersiz"))
        assertEquals("eposta-dogrulandi", DeepLinks.toRoute("kitappla://eposta-dogrulandi?durum=baska"))
    }

    @Test
    fun accountLinkOpenedInTheAppCarriesItsTokenToTheVerificationScreen() {
        assertEquals("eposta-dogrula?token=abc_DEF-123", DeepLinks.toRoute("https://www.kitappla.com/uygulamada-ac/eposta-dogrula?token=abc_DEF-123"))
        assertEquals("eposta-dogrula?token=t0k", DeepLinks.toRoute("https://kitappla.com/uygulamada-ac/eposta-dogrula/?token=t0k"))
        // Jetonsuz ya da bozuk jetonlu bağlantı onaylanamaz: "bağlantı geçersiz"
        assertEquals("eposta-dogrulandi?durum=gecersiz", DeepLinks.toRoute("https://www.kitappla.com/uygulamada-ac/eposta-dogrula"))
        assertEquals("eposta-dogrulandi?durum=gecersiz", DeepLinks.toRoute("https://www.kitappla.com/uygulamada-ac/eposta-dogrula?token=a%26b"))
    }

    @Test
    fun passwordResetLinkIsNotMistakenForStudentVerification() {
        assertEquals("sifre-sifirla?token=abc_DEF-123", DeepLinks.toRoute("kitappla://sifre-sifirla?token=abc_DEF-123"))
        assertEquals(
            "sifre-sifirla?token=abc",
            DeepLinks.toRoute("https://www.kitappla.com/uygulamada-ac/sifre-sifirla?token=abc"),
        )
        assertEquals(
            "sifre-sifirla?token=abc",
            DeepLinks.toRoute("https://www.kitappla.com/sifre-sifirla?token=abc&uygulama=1"),
        )
    }

    @Test
    fun studentLinksOpenTheStudentScreenWithToken() {
        assertEquals("profil/ogrenci?token=t0k", DeepLinks.toRoute("kitappla://profil/ogrenci?token=t0k"))
        assertEquals(
            "profil/ogrenci?token=t0k",
            DeepLinks.toRoute("https://www.kitappla.com/profil/ogrenci/eposta/onay?token=t0k"),
        )
        assertEquals("profil/ogrenci?token=t0k", DeepLinks.toRoute("https://kitappla.com/uygulamada-ac/okul-eposta?token=t0k"))
    }

    @Test
    fun pendingVerificationLinkOpensTheInboxScreen() {
        assertEquals("hesap-dogrulama", DeepLinks.toRoute("kitappla://hesap-dogrulama"))
    }

    @Test
    fun unsafeTokensAreDroppedInsteadOfBeingInjectedIntoTheRoute() {
        assertEquals("sifre-sifirla", DeepLinks.toRoute("kitappla://sifre-sifirla?token=a%26durum%3Dx"))
        assertEquals("profil/ogrenci", DeepLinks.toRoute("kitappla://profil/ogrenci?token=a/b"))
        assertEquals("profil/ogrenci", DeepLinks.toRoute("kitappla://profil/ogrenci?token="))
    }

    @Test
    fun unknownOrBrokenLinksAreIgnored() {
        assertNull(DeepLinks.toRoute(null))
        assertNull(DeepLinks.toRoute(""))
        assertNull(DeepLinks.toRoute("kitappla://bilinmeyen?token=abc"))
        assertNull(DeepLinks.toRoute("https://www.kitappla.com/eposta-dogrula?token=abc"))
        assertNull(DeepLinks.toRoute("ftp://www.kitappla.com/sifre-sifirla?token=abc"))
        assertNull(DeepLinks.toRoute("kitappla://bozuk adres"))
    }
}
