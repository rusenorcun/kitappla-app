package com.kitappla.app.ui.screens.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.repo.MessageRepository
import com.kitappla.app.data.repo.SUPPORT_CONVERSATION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * "Yöneticiye mesaj gönder": üyenin destek sohbetini açar (yoksa sunucu oluşturur). Sohbet listesine götürmek yeterli
 * değildi; daha önce destek sohbeti olmayan üye yöneticiye yazamıyordu.
 */
@HiltViewModel
class IletisimViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _opening = MutableStateFlow(false)
    val opening: StateFlow<Boolean> = _opening.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Misafirde [onNeedLogin], açılınca [onOpened] (sohbet kimliğiyle) çağrılır. */
    fun openSupportChat(onOpened: (Long) -> Unit, onNeedLogin: () -> Unit) {
        val me = (sessionManager.state.value as? SessionState.Member)?.user
        if (me == null) {
            onNeedLogin()
            return
        }
        if (_opening.value) return
        _opening.value = true
        _error.value = null
        viewModelScope.launch {
            when (val r = messageRepository.openConversation(SUPPORT_CONVERSATION, me.id)) {
                is ApiResult.Success -> onOpened(r.value.id)
                is ApiResult.Failure -> _error.value = r.message
            }
            _opening.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
