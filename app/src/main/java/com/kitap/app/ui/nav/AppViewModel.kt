package com.kitap.app.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.cache.ConnectionNotice
import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.session.SessionManager
import com.kitap.app.core.session.SessionState
import com.kitap.app.data.api.NotificationApi
import com.kitap.app.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationApi: NotificationApi,
    sessionManager: SessionManager,
    connectionNotice: ConnectionNotice,
) : ViewModel() {
    val session: StateFlow<SessionState> = sessionManager.state

    /** Ön bellekten eski veri gösterildiğinde tetiklenir; kök arayüz küçük bir uyarı çıkarır. */
    val connectionProblems: SharedFlow<Unit> = connectionNotice.events

    private val _unread = MutableStateFlow(0L)
    val unread: StateFlow<Long> = _unread.asStateFlow()

    init {
        viewModelScope.launch { authRepository.restore() }
        viewModelScope.launch {
            session.collectLatest { s ->
                _unread.value = if (s is SessionState.Member) fetchUnread() else 0L
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private val _deepLinkRoute = MutableStateFlow<String?>(null)
    val deepLinkRoute: StateFlow<String?> = _deepLinkRoute.asStateFlow()

    fun handleDeepLink(route: String) {
        _deepLinkRoute.value = route
    }

    fun clearDeepLink() {
        _deepLinkRoute.value = null
    }

    private suspend fun fetchUnread(): Long =
        (safeApiCall { notificationApi.notifications() } as? ApiResult.Success)?.value?.unread ?: 0L
}
