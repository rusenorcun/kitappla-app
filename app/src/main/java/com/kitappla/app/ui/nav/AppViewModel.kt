package com.kitappla.app.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.cache.ConnectionNotice
import com.kitappla.app.core.net.EmailVerificationNotice
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.api.NotificationApi
import com.kitappla.app.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    emailVerificationNotice: EmailVerificationNotice,
) : ViewModel() {
    val session: StateFlow<SessionState> = sessionManager.state

    /** Ön bellekten eski veri gösterildiğinde tetiklenir; kök arayüz küçük bir uyarı çıkarır. */
    val connectionProblems: SharedFlow<Unit> = connectionNotice.events

    /** Sunucu bir işlemi "önce e-postanı doğrula" (403 EMAIL_NOT_VERIFIED) diyerek reddettiğinde tetiklenir. */
    val emailVerificationRequired: SharedFlow<Unit> = emailVerificationNotice.events

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

    /** [then] oturum kapandıktan (durum Misafir olduktan) sonra çağrılır; ör. girişe yönlendirmek için. */
    fun logout(then: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            then()
        }
    }

    private val _deepLinkRoute = MutableStateFlow<String?>(null)
    val deepLinkRoute: StateFlow<String?> = _deepLinkRoute.asStateFlow()

    fun handleDeepLink(route: String) {
        _deepLinkRoute.value = route
    }

    fun clearDeepLink() {
        _deepLinkRoute.value = null
    }

    private var unreadJob: Job? = null

    /**
     * Okunmamış bildirim sayısını yeniden çeker (uygulamaya dönüşte, bildirimlerden sekmeye dönünce). Yalnızca üye
     * oturumunda; süren bir istek varsa yenisi atılmaz.
     */
    fun refreshUnread() {
        if (session.value !is SessionState.Member || unreadJob?.isActive == true) return
        unreadJob = viewModelScope.launch {
            val count = fetchUnread()
            if (session.value is SessionState.Member) _unread.value = count
        }
    }

    private suspend fun fetchUnread(): Long =
        (safeApiCall { notificationApi.notifications() } as? ApiResult.Success)?.value?.unread ?: 0L
}
