package com.kitappla.app.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.NotificationDto
import com.kitappla.app.data.repo.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BildirimlerState(
    val items: List<NotificationDto> = emptyList(),
    val unread: Long = 0,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class BildirimlerViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BildirimlerState())
    val state: StateFlow<BildirimlerState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = notificationRepository.getNotifications()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> {
                    val resp = r.value
                    _state.value = _state.value.copy(
                        loading = false,
                        items = resp.items,
                        unread = resp.unread,
                    )
                }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            when (notificationRepository.markAllRead()) {
                is ApiResult.Failure -> {}
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        unread = 0,
                        items = _state.value.items.map { it.copy(read = true) },
                    )
                }
            }
        }
    }

    fun markOneRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markOneRead(id)
            _state.value = _state.value.copy(
                items = _state.value.items.map { if (it.id == id) it.copy(read = true) else it },
                unread = maxOf(0L, _state.value.unread - 1),
            )
        }
    }
}
