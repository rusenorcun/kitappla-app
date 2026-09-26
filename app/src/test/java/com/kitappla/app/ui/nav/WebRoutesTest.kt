package com.kitappla.app.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Backend bildirimlerinin bağlantıları (NotificationService.notify çağrılarındaki yollar). */
class WebRoutesTest {
    @Test
    fun plainListPagesMapToTheSameNamedRoutes() {
        listOf("/bagislarim", "/aldiklarim", "/isteklerim", "/karsiladiklarim", "/sikayetlerim", "/panom", "/profil")
            .forEach { assertEquals(it.trim('/'), WebRoutes.toRoute(it)) }
        assertEquals("hesap-dogrulama", WebRoutes.toRoute("/hesap-dogrulama"))
    }

    @Test
    fun webOnlyPathsAreTranslated() {
        assertEquals(Routes.TAKASLARIM, WebRoutes.toRoute("/takas/takaslarim"))
        assertEquals(Routes.TAKAS_KITAPLARIM, WebRoutes.toRoute("/takas/kitaplarim"))
        assertEquals(Routes.OGRENCI_DOGRULAMA, WebRoutes.toRoute("/profil/ogrenci"))
        assertEquals("takas/teklif/5", WebRoutes.toRoute("/takas/teklifler/5"))
        assertEquals("sohbet/12", WebRoutes.toRoute("/mesajlar/12"))
        assertEquals("kitap/7", WebRoutes.toRoute("/kitap/7"))
    }

    @Test
    fun fullAddressesQueriesAndFragmentsAreTolerated() {
        assertEquals("bagislarim", WebRoutes.toRoute("https://www.kitappla.com/bagislarim?sekme=aktif#son"))
        assertEquals("sohbet/3", WebRoutes.toRoute("/mesajlar/3/"))
    }

    @Test
    fun unknownOrAdminPathsAreIgnoredInsteadOfCrashing() {
        assertNull(WebRoutes.toRoute(null))
        assertNull(WebRoutes.toRoute(""))
        assertNull(WebRoutes.toRoute("/admin/sikayet/4"))
        assertNull(WebRoutes.toRoute("/mesajlar/abc"))
        assertNull(WebRoutes.toRoute("/takas/teklifler/"))
        assertNull(WebRoutes.toRoute("/bilinmeyen/sayfa"))
    }
}
