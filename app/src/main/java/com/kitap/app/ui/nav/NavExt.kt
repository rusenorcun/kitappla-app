package com.kitap.app.ui.nav

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.kitap.app.ui.screens.common.PlaceholderScreen

internal fun NavGraphBuilder.placeholder(route: String, title: String, onBack: (() -> Unit)?) {
    composable(route) { PlaceholderScreen(title, onBack) }
}

/**
 * Arayüzdeki geri okları için: yalnızca altında başka hedef varken çıkarır. `popBackStack()` kök hedefi de atar;
 * hızlı çift dokunuşta NavHost boş kalır ve tek bir sistem geri basışı uygulamayı kapatırdı.
 */
fun NavHostController.popIfNotRoot(): Boolean = previousBackStackEntry != null && popBackStack()

/**
 * Sekme geçişi: yığında yalnızca başlangıç hedefi (Keşfet) kalır, böylece geri önce Keşfet'e döner.
 *
 * Başlangıç hedefinin kendisine dönerken `restoreState` **kullanılmaz**: `popUpTo(start){saveState = true}`
 * aynı `navigate` çağrısı içinde üstteki hedefi kaydeder ve bu kaydı başlangıç hedefiyle de ilişkilendirir;
 * `restoreState` açıkken NavController onu hemen geri yükler ve Keşfet sekmesi hiç açılmazdı (D1).
 */
fun NavHostController.navigateToTab(route: String) {
    val start = graph.findStartDestination()
    navigate(route) {
        popUpTo(start.id) { saveState = true }
        launchSingleTop = true
        restoreState = route != start.route
    }
}
