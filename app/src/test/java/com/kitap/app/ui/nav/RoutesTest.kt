package com.kitap.app.ui.nav

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
        ).forEach { assertFalse("$it herkese açık olmalı", Routes.requiresAuth(it)) }
    }

    @Test
    fun memberRoutesRequireAuth() {
        listOf(
            Routes.TAKAS, Routes.MESAJLAR, Routes.PANOM, Routes.BILDIRIMLER, Routes.BAGIS_YENI,
            Routes.ISTEK_YENI, Routes.PROFIL, "sohbet/3", "sikayet/bagis/4", Routes.BAGISLARIM,
            Routes.ALDIKLARIM, Routes.TAKAS_TEKLIF,
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
    fun pendingLoginTargetIsKeptWhileMovingWithinAuthFlow() {
        listOf(
            Routes.LOGIN, Routes.REGISTER, Routes.SIFREMI_UNUTTUM, Routes.SIFRE_SIFIRLA, Routes.ADMIN_LOGIN,
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
