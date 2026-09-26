package com.kitappla.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.repo.AdminRepository
import com.kitappla.app.data.repo.MessageRepository
import com.kitappla.app.data.repo.SUPPORT_CONVERSATION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUyelerUiState(
    val users: List<UserDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class AdminUyelerViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val messageRepository: MessageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUyelerUiState())
    val state: StateFlow<AdminUyelerUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        searchUsers("")
    }

    /** Her tuşta istek atılmaz: yazma durunca ([SEARCH_DEBOUNCE_MILLIS]) aranır. */
    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            runSearch(query)
        }
    }

    /** Süren aramayı iptal eder: geç gelen eski sorgu yanıtı yeni sorgunun sonucunun üstüne yazılamaz. */
    fun searchUsers(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(query) }
    }

    private suspend fun runSearch(query: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val q = query.trim().ifBlank { null }
        when (val res = adminRepository.getAllUsers(q)) {
            is ApiResult.Success -> {
                _state.update { it.copy(users = res.value, isLoading = false, error = null) }
            }
            is ApiResult.Failure -> {
                _state.update { it.copy(isLoading = false, error = res.message) }
            }
        }
    }

    fun toggleBlockUser(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.toggleBlockUser(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Kullanıcı durumu güncellendi.", isActionLoading = false) }
                    searchUsers(_state.value.searchQuery)
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun toggleAdminRole(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.toggleAdminRole(id)) {
                is ApiResult.Success -> {
                    val statusText = if (res.value.admin) "artık yönetici" else "artık yönetici değil"
                    _state.update { it.copy(actionMessage = "${res.value.name} $statusText.", isActionLoading = false) }
                    searchUsers(_state.value.searchQuery)
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.deleteUser(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Üye kalıcı olarak silindi.", isActionLoading = false) }
                    searchUsers(_state.value.searchQuery)
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun sendMessageToUser(userId: Long, text: String, onSuccess: (() -> Unit)? = null) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val convRes = messageRepository.openConversation(SUPPORT_CONVERSATION, userId)) {
                is ApiResult.Success -> {
                    val convId = convRes.value.id
                    when (val msgRes = messageRepository.sendMessage(convId, trimmed)) {
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    actionMessage = "Mesaj başarıyla gönderildi.",
                                    isActionLoading = false,
                                )
                            }
                            onSuccess?.invoke()
                        }
                        is ApiResult.Failure -> {
                            _state.update {
                                it.copy(
                                    actionMessage = msgRes.message,
                                    isActionLoading = false,
                                )
                            }
                        }
                    }
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(
                            actionMessage = convRes.message,
                            isActionLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun openConversationWithUser(userId: Long, onOpened: (Long) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val convRes = messageRepository.openConversation(SUPPORT_CONVERSATION, userId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isActionLoading = false) }
                    onOpened(convRes.value.id)
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(
                            actionMessage = convRes.message,
                            isActionLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
