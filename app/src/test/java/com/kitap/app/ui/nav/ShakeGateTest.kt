package com.kitap.app.ui.nav

import com.kitap.app.core.session.SessionState
import com.kitap.app.data.dto.UserDto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeGateTest {
    private val user = UserDto(id = 1)

    @Test
    fun guestOnOrdinaryRouteListens() {
        assertTrue(shakeGateEnabled(SessionState.Guest, Routes.KESFET))
    }

    @Test
    fun guestWithoutRouteYetListens() {
        assertTrue(shakeGateEnabled(SessionState.Guest, null))
    }

    @Test
    fun guestOnAdminLoginDoesNotListen() {
        assertFalse(shakeGateEnabled(SessionState.Guest, Routes.ADMIN_LOGIN))
    }

    @Test
    fun nonGuestStatesNeverListen() {
        assertFalse(shakeGateEnabled(SessionState.Loading, Routes.KESFET))
        assertFalse(shakeGateEnabled(SessionState.Member(user), Routes.KESFET))
        assertFalse(shakeGateEnabled(SessionState.Admin(user.copy(admin = true)), Routes.KESFET))
        assertFalse(shakeGateEnabled(SessionState.Member(user), null))
    }
}
