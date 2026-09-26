package com.kitappla.app.ui.nav

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Arayüzdeki geri okuyla hızlı çift dokunuş, kök hedefi yığından atıp NavHost'u boş bırakmamalı. */
class PopIfNotRootTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var nav: NavHostController
    private var warned = 0
    private var exited = 0

    private fun show() = rule.setContent {
        nav = rememberNavController()
        NavHost(nav, startDestination = "home") {
            composable("home") { Text("home") }
            composable("detail") { Text("detail") }
        }
        RootBackGuard(nav, onWarn = { warned++ }, onExit = { exited++ })
    }

    private fun goTo(route: String) {
        rule.runOnUiThread { nav.navigate(route) }
        rule.waitForIdle()
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun route(): String? = rule.runOnUiThread { nav.currentBackStackEntry?.destination?.route }

    @Test
    fun singlePopFromDetailReturnsToRoot() {
        show()
        goTo("detail")
        rule.runOnUiThread { nav.popIfNotRoot() }
        rule.waitForIdle()
        assertEquals("home", route())
    }

    @Test
    fun popAtRootIsNoOp() {
        show()
        rule.runOnUiThread { nav.popIfNotRoot() }
        rule.waitForIdle()
        assertEquals("home", route())
        assertNotNull(rule.runOnUiThread { nav.currentBackStackEntry })
    }

    @Test
    fun doubleTapOnBackArrowStopsAtRootAndSystemBackStillWarns() {
        show()
        goTo("detail")
        // Çift dokunuş: iki pop, arada çerçeve/boşta bekleme yok.
        rule.runOnUiThread {
            nav.popIfNotRoot()
            nav.popIfNotRoot()
        }
        rule.waitForIdle()
        assertEquals("home", route())
        assertNull(rule.runOnUiThread { nav.previousBackStackEntry })

        pressBack()
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }
}
