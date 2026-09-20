package com.kitap.app.core.nav

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ExitGuardHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun pressBack() = rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }

    @Test
    fun singleBackPressDoesNotFinishActivity() {
        var warned = 0
        var exited = 0
        rule.setContent { ExitGuardHandler(enabled = true, onWarn = { warned++ }, onExit = { exited++ }) }
        pressBack()
        rule.waitForIdle()
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }

    @Test
    fun secondBackPressWithinWindowExits() {
        var warned = 0
        var exited = 0
        rule.setContent { ExitGuardHandler(enabled = true, onWarn = { warned++ }, onExit = { exited++ }) }
        pressBack()
        pressBack()
        rule.waitForIdle()
        assertEquals(1, warned)
        assertEquals(1, exited)
    }

    @Test
    fun disabledHandlerIgnoresBackPress() {
        var warned = 0
        var exited = 0
        rule.setContent { ExitGuardHandler(enabled = false, onWarn = { warned++ }, onExit = { exited++ }) }
        pressBack()
        assertEquals(0, warned)
        assertEquals(0, exited)
    }

    @Test
    fun leavingAndReturningToRootStartsAFreshWarningWindow() {
        var warned = 0
        var exited = 0
        var enabled by mutableStateOf(true)
        rule.setContent { ExitGuardHandler(enabled = enabled, onWarn = { warned++ }, onExit = { exited++ }) }
        pressBack()                                   // uyarı
        rule.runOnUiThread { enabled = false }        // kökten ayrıl (ör. bir detaya git)
        rule.waitForIdle()
        rule.runOnUiThread { enabled = true }         // köke dön, pencere hâlâ dolmadı
        rule.waitForIdle()
        pressBack()                                   // bayat uyarı sayılmamalı: yeniden uyarır
        assertEquals(2, warned)
        assertEquals(0, exited)
    }
}
