package com.kitappla.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onBack: (() -> Unit)? = null) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Bu sayfa yakında.")
        }
    }
}

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
