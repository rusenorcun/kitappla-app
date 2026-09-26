package com.kitappla.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.kitappla.app.ui.screens.admin.AdminBelgeGoruntuleScreen
import com.kitappla.app.ui.screens.admin.AdminBelgeGoruntuleViewModel
import com.kitappla.app.ui.screens.admin.AdminIcerikScreen
import com.kitappla.app.ui.screens.admin.AdminIzlecScreen
import com.kitappla.app.ui.screens.admin.AdminNoktalarScreen
import com.kitappla.app.ui.screens.admin.AdminPanoScreen
import com.kitappla.app.ui.screens.admin.AdminSikayetDetayScreen
import com.kitappla.app.ui.screens.admin.AdminSikayetlerScreen
import com.kitappla.app.ui.screens.admin.AdminUyelerScreen
import com.kitappla.app.ui.screens.message.MesajlarScreen
import com.kitappla.app.ui.screens.message.SohbetScreen
import com.kitappla.app.ui.theme.AdminTheme

/** Yönetim grafiği: yalnızca Admin oturumunda kurulur; kök hedef Pano'dur. */
@Composable
fun AdminNavHost(onLogout: () -> Unit) {
    val navController = rememberNavController()
    AppRootBackGuard(navController)
    val back: () -> Unit = { navController.popIfNotRoot() }

    AdminTheme {
        NavHost(navController, startDestination = Routes.ADMIN_PANO) {
            composable(Routes.ADMIN_PANO) {
                AdminPanoScreen(
                    onNavigateToBelgeler = { navController.navigate(Routes.ADMIN_BELGELER) { launchSingleTop = true } },
                    onNavigateToUyeler = { navController.navigate(Routes.ADMIN_UYELER) { launchSingleTop = true } },
                    onNavigateToIcerik = { navController.navigate(Routes.ADMIN_ICERIK) { launchSingleTop = true } },
                    onNavigateToNoktalar = { navController.navigate(Routes.ADMIN_NOKTALAR) { launchSingleTop = true } },
                    onNavigateToSikayetler = { navController.navigate(Routes.ADMIN_SIKAYETLER) { launchSingleTop = true } },
                    onNavigateToMesajlar = { navController.navigate(Routes.ADMIN_MESAJLAR) { launchSingleTop = true } },
                    onNavigateToIzlec = { navController.navigate(Routes.ADMIN_IZLEC) { launchSingleTop = true } },
                    onLogout = onLogout,
                )
            }
            composable(Routes.ADMIN_BELGELER) {
                com.kitappla.app.ui.screens.admin.AdminBelgelerScreen(
                    onBack = back,
                    onOpenDocument = { id, name ->
                        navController.navigate(Routes.adminBelge(id, name)) { launchSingleTop = true }
                    },
                )
            }
            composable(
                route = Routes.ADMIN_BELGE,
                arguments = listOf(
                    navArgument(AdminBelgeGoruntuleViewModel.ID_ARG) { type = NavType.StringType },
                    navArgument(AdminBelgeGoruntuleViewModel.NAME_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                AdminBelgeGoruntuleScreen(onBack = back)
            }
            composable(Routes.ADMIN_UYELER) {
                AdminUyelerScreen(
                    onBack = back,
                    onOpenConversation = { convId ->
                        navController.navigate(Routes.sohbet(convId)) { launchSingleTop = true }
                    },
                )
            }
            composable(Routes.ADMIN_ICERIK) {
                AdminIcerikScreen(onBack = back)
            }
            composable(Routes.ADMIN_NOKTALAR) {
                AdminNoktalarScreen(onBack = back)
            }
            composable(Routes.ADMIN_SIKAYETLER) {
                AdminSikayetlerScreen(
                    onBack = back,
                    onNavigateToDetail = { reportId ->
                        navController.navigate(Routes.adminSikayetDetay(reportId)) { launchSingleTop = true }
                    },
                )
            }
            composable(Routes.ADMIN_SIKAYET_DETAY) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                AdminSikayetDetayScreen(
                    reportId = reportId,
                    onBack = back,
                )
            }
            composable(Routes.ADMIN_IZLEC) {
                AdminIzlecScreen(onBack = back)
            }
            composable(Routes.ADMIN_MESAJLAR) {
                MesajlarScreen(
                    onOpenConversation = { convId ->
                        navController.navigate(Routes.sohbet(convId)) { launchSingleTop = true }
                    },
                )
            }
            composable(Routes.SOHBET) {
                SohbetScreen(onBack = back)
            }
        }
    }
}
