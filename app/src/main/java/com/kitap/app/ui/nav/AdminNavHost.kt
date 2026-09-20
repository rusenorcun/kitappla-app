package com.kitap.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kitap.app.ui.screens.admin.AdminBelgelerScreen
import com.kitap.app.ui.screens.admin.AdminIcerikScreen
import com.kitap.app.ui.screens.admin.AdminNoktalarScreen
import com.kitap.app.ui.screens.admin.AdminPanoScreen
import com.kitap.app.ui.screens.admin.AdminSikayetDetayScreen
import com.kitap.app.ui.screens.admin.AdminSikayetlerScreen
import com.kitap.app.ui.screens.admin.AdminUyelerScreen
import com.kitap.app.ui.screens.message.MesajlarScreen
import com.kitap.app.ui.theme.AdminTheme

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
                    onLogout = onLogout,
                )
            }
            composable(Routes.ADMIN_BELGELER) {
                AdminBelgelerScreen(onBack = back)
            }
            composable(Routes.ADMIN_UYELER) {
                AdminUyelerScreen(onBack = back)
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
            composable(Routes.ADMIN_MESAJLAR) {
                MesajlarScreen(
                    onOpenConversation = { convId ->
                        navController.navigate(Routes.sohbet(convId)) { launchSingleTop = true }
                    },
                )
            }
        }
    }
}
