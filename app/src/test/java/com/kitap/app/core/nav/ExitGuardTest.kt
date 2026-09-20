package com.kitap.app.core.nav

import com.kitap.app.core.nav.ExitGuard.Decision
import org.junit.Assert.assertEquals
import org.junit.Test

class ExitGuardTest {
    private var now = 0L
    private val guard = ExitGuard(windowMs = 2_000L, clock = { now })

    @Test
    fun firstPressWarnsAndNeverExits() {
        assertEquals(Decision.WARN, guard.onBackAtRoot())
    }

    @Test
    fun secondPressWithinWindowExits() {
        guard.onBackAtRoot()
        now = 1_500L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }

    @Test
    fun pressExactlyAtWindowBoundaryExits() {
        guard.onBackAtRoot()
        now = 2_000L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }

    @Test
    fun secondPressAfterWindowWarnsAgain() {
        guard.onBackAtRoot()
        now = 2_001L
        assertEquals(Decision.WARN, guard.onBackAtRoot())
        now = 2_500L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }

    @Test
    fun afterExitNextPressWarnsAgain() {
        guard.onBackAtRoot()
        guard.onBackAtRoot()   // EXIT
        assertEquals(Decision.WARN, guard.onBackAtRoot())
    }

    @Test
    fun resetForgetsPendingWarning() {
        guard.onBackAtRoot()   // WARN
        guard.reset()
        now = 500L
        assertEquals(Decision.WARN, guard.onBackAtRoot())
        now = 1_000L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }
}
