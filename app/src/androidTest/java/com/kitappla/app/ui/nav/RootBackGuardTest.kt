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
import org.junit.Rule
import org.junit.Test

class RootBackGuardTest {
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

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun goTo(route: String) {
        rule.runOnUiThread { nav.navigate(route) }
        rule.waitForIdle()
    }

    private fun route(): String? = rule.runOnUiThread { nav.currentBackStackEntry?.destination?.route }

    @Test
    fun backAtRootWarnsAndKeepsActivityAlive() {
        show()
        pressBack()
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }

    @Test
    fun backFromDetailPopsToRootWithoutWarning() {
        show()
        goTo("detail")
        pressBack()
        assertEquals("home", route())
        assertEquals(0, warned)
        assertEquals(0, exited)
    }

    @Test
    fun secondBackAtRootWithinWindowExits() {
        show()
        pressBack()
        pressBack()
        assertEquals(1, warned)
        assertEquals(1, exited)
    }

    @Test
    fun singleBackNeverFinishesActivityAcrossNavigation() {
        show()
        goTo("detail")
        pressBack()   // detail -> home (NavHost)
        pressBack()   // home: uyarı
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }

    @Test
    fun backWithNoNavGraphYetStillWarnsAndNeverFinishesActivity() {
        // Grafik değişiminden sonraki ilk kareler: NavHost/grafik yok, giriş yok. Koruma açık kalmalı.
        rule.setContent {
            val emptyNav = rememberNavController()
            RootBackGuard(emptyNav, onWarn = { warned++ }, onExit = { exited++ })
        }
        pressBack()
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }
}
