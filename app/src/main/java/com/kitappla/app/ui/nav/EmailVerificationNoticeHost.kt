package com.kitappla.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

const val EMAIL_VERIFICATION_REQUIRED_MESSAGE = "Bu işlem için önce e-posta adresini doğrulamalısın."
const val EMAIL_VERIFICATION_ACTION = "Doğrula"

/**
 * Sunucu bir işlemi e-posta doğrulanmadığı için reddettiğinde "Doğrula" düğmeli bir uyarı gösterir; düğme
 * [onVerify] ile doğrulama ekranına götürür. [ConnectionNoticeHost] gibi ekranın üstüne yerleştirilir.
 */
@Composable
fun EmailVerificationNoticeHost(events: Flow<Unit>, onVerify: () -> Unit, modifier: Modifier = Modifier) {
    val hostState = remember { SnackbarHostState() }
    val currentOnVerify by rememberUpdatedState(onVerify)
    LaunchedEffect(events) {
        events.collect {
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(
                message = EMAIL_VERIFICATION_REQUIRED_MESSAGE,
                actionLabel = EMAIL_VERIFICATION_ACTION,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) currentOnVerify()
        }
    }
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
