package com.kitap.app.ui.screens.message

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.ChatMessageDto
import com.kitap.app.data.repo.MessageRepository
import com.kitap.app.data.repo.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SohbetState(
    val conversationId: Long = 0,
    val messages: List<ChatMessageDto> = emptyList(),
    val loading: Boolean = true,
    val sending: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
)

@HiltViewModel
class SohbetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val conversationId: Long = savedStateHandle.get<Any>("id")?.toString()?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(SohbetState(conversationId = conversationId))
    val state: StateFlow<SohbetState> = _state.asStateFlow()

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = messageRepository.messages(conversationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, messages = r.value)
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(sending = true)
            when (val r = messageRepository.sendMessage(conversationId, trimmed)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(sending = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        sending = false,
                        messages = _state.value.messages + r.value,
                    )
                }
            }
        }
    }

    fun report(reason: String, note: String?) {
        viewModelScope.launch {
            when (val r = reportRepository.report("CONVERSATION", conversationId, reason, note)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionMessage = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(actionMessage = "Sohbet şikâyet edildi.")
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
