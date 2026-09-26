package com.kitappla.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kitappla.app.ui.nav.AppRoot
import com.kitappla.app.ui.nav.AppViewModel
import com.kitappla.app.ui.nav.DeepLinks
import com.kitappla.app.ui.theme.KitapplaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Döndürme/süreç yeniden kurulumunda aynı Intent yeniden gelir; bağlantı yalnızca ilk açılışta işlenir
        // (aksi hâlde tek kullanımlık jeton ikinci kez gönderilir, ekran yığına yeniden eklenirdi).
        if (savedInstanceState == null) handleDeepLink(intent)
        setContent {
            KitapplaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(viewModel = appViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    /** E-posta bağlantısını (bkz. [DeepLinks]) rotaya çevirir; tanınmayan bağlantı uygulamayı yalnızca açar. */
    private fun handleDeepLink(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val route = DeepLinks.toRoute(intent.dataString) ?: return
        appViewModel.handleDeepLink(route)
    }
}
