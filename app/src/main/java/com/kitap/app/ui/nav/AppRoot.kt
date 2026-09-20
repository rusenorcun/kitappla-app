package com.kitap.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.core.session.SessionState

/**
 * Oturum durumuna göre grafiği seçer. Grafik değişince (Misafir→Üye, Üye→Misafir, →Yönetim) yeni bir
 * NavHost kurulur; böylece geri yığını sıfırlanır ve geri tuşu Giriş/kapı ekranlarına dönmez.
 */
@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val unread by viewModel.unread.collectAsStateWithLifecycle()
    var pendingRoute by rememberSaveable { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        when (val s = session) {
            SessionState.Loading -> SplashScreen()
            is SessionState.Admin -> {
                // Yönetici girişi kapıdan gelen üye hedefini geçersiz kılar; sonraki üye girişine sızmasın.
                LaunchedEffect(Unit) { pendingRoute = null }
                AdminNavHost(onLogout = viewModel::logout)
            }
            else -> key(s::class) {
                MemberNavHost(
                    session = s,
                    unread = unread,
                    pendingRoute = pendingRoute,
                    onNeedLogin = { pendingRoute = it },
                    onPendingConsumed = { pendingRoute = null },
                    onLogout = viewModel::logout,
                )
            }
        }
        ConnectionNoticeHost(viewModel.connectionProblems, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun SplashScreen() {
    AppExitGuardHandler(enabled = true)   // oturum çözülürken de tek geri basışı çıkarmaz
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
