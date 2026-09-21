package com.kitap.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kitap.app.ui.nav.AppRoot
import com.kitap.app.ui.nav.AppViewModel
import com.kitap.app.ui.nav.Routes
import com.kitap.app.ui.theme.KitapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        setContent {
            KitapTheme {
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

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val token = uri.getQueryParameter("token")
        if (!token.isNullOrBlank()) {
            val route = Routes.ogrenciDogrulama(token)
            appViewModel.handleDeepLink(route)
        }
    }
}
