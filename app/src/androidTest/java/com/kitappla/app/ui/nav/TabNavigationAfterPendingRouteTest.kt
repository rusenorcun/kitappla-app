package com.kitappla.app.ui.nav

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * D1: Misafir giris kapisindan sonra bekleyen hedefe duz `navigate` ile gidilince (MemberNavHost'taki
 * `LaunchedEffect(session)`) alt sekmeler olu kaliyordu: Keşfet'e dokunmak ekrani degistirmiyordu.
 * Kok neden [navigateToTab]'daki `popUpTo(start){saveState}` + `restoreState` ciftinin hedefin kendisi
 * baslangic hedefiyken ayni cagrida kaydedilen alt yigini geri yuklemesiydi.
 */
class TabNavigationAfterPendingRouteTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var nav: NavHostController
    private var warned = 0
    private var exited = 0

    private fun show() = rule.setContent {
        nav = rememberNavController()
        NavHost(nav, startDestination = Routes.KESFET) {
            Routes.TABS.forEach { route -> composable(route) { Text(route) } }
        }
        RootBackGuard(nav, onWarn = { warned++ }, onExit = { exited++ })
    }

    /** Giris sonrasi bekleyen hedefe gidis: MemberNavHost'taki cagrinin aynisi (secenek verilmez). */
    private fun pendingNavigate(route: String) {
        rule.runOnUiThread { nav.navigate(route) }
        rule.waitForIdle()
    }

    private fun tab(route: String) {
        rule.runOnUiThread { nav.navigateToTab(route) }
        rule.waitForIdle()
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun route(): String? = rule.runOnUiThread { nav.currentBackStackEntry?.destination?.route }

    /** Grafik girdisinin rotasi null oldugundan dusulur; kalanlar gercek hedeflerdir. */
    private fun stack(): List<String> =
        rule.runOnUiThread { nav.currentBackStack.value.mapNotNull { it.destination.route } }

    @Test
    fun kesfetTabSwitchesAfterPendingNavigate() {
        show()
        pendingNavigate(Routes.TAKAS)
        assertEquals(listOf(Routes.KESFET, Routes.TAKAS), stack())

        tab(Routes.KESFET)
        assertEquals(Routes.KESFET, route())
        assertEquals(listOf(Routes.KESFET), stack())
    }

    @Test
    fun otherTabThenKesfetSwitchesAfterPendingNavigate() {
        show()
        pendingNavigate(Routes.TAKAS)

        tab(Routes.MESAJLAR)
        assertEquals(Routes.MESAJLAR, route())

        tab(Routes.KESFET)
        assertEquals(Routes.KESFET, route())
        assertEquals(listOf(Routes.KESFET), stack())
    }

    @Test
    fun allTabsSwitchAfterPendingNavigate() {
        show()
        pendingNavigate(Routes.TAKAS)

        listOf(
            Routes.ISTEKLER, Routes.KESFET, Routes.MESAJLAR, Routes.KESFET,
            Routes.PANOM, Routes.KESFET, Routes.TAKAS, Routes.KESFET,
        ).forEach { target ->
            tab(target)
            assertEquals("sekme gecisi: $target", target, route())
        }
    }

    @Test
    fun backAfterPendingNavigateAndTabSwitchGoesToKesfetThenWarns() {
        show()
        pendingNavigate(Routes.TAKAS)
        tab(Routes.PANOM)
        assertEquals(Routes.PANOM, route())

        pressBack()
        assertEquals(Routes.KESFET, route())
        assertEquals(0, warned)

        pressBack()
        assertEquals(1, warned)
        assertEquals(0, exited)
    }
}
