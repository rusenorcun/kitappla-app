package com.kitappla.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

const val CONNECTION_PROBLEM_MESSAGE =
    "Şu anda bağlantı konusunda bir sorun yaşıyoruz. Kayıtlı veriler gösteriliyor."

/**
 * [events]'te her değer geldiğinde kısa süreli bir "bağlantı sorunu" uyarısı gösterir. Ekranın üstüne yerleştirilmeli
 * (alt gezinme çubuğunu ve sekmeleri örtmez); konumu çağıran `modifier` ile verir.
 */
@Composable
fun ConnectionNoticeHost(events: Flow<Unit>, modifier: Modifier = Modifier) {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(events) {
        events.collect {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(CONNECTION_PROBLEM_MESSAGE, duration = SnackbarDuration.Short)
        }
    }
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
