package com.kitap.app.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.ArrangeMeetingBody
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.repo.PickupPointRepository
import com.kitap.app.data.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IsteklerimState(
    val loading: Boolean = true,
    val items: List<RequestDto> = emptyList(),
    val error: String? = null,
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class IsteklerimViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IsteklerimState())
    val state: StateFlow<IsteklerimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val pointsRes = pickupPointRepository.getActivePoints()
            val points = if (pointsRes is ApiResult.Success) pointsRes.value else emptyList()

            when (val r = requestRepository.myRequests()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message, pickupPoints = points)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, items = r.value, pickupPoints = points)
            }
        }
    }

    fun arrangeMeeting(requestId: Long, pointId: Long?, note: String?, at: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = requestRepository.arrangeMeeting(requestId, ArrangeMeetingBody(pointId, note, at))) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Buluşma kaydedildi.")
                    load()
                }
            }
        }
    }

    fun deliver(requestId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = requestRepository.deliver(requestId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teslimat tamamlandı.")
                    load()
                }
            }
        }
    }

    fun thank(requestId: Long, message: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = requestRepository.thank(requestId, message)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teşekkürünüz iletildi.")
                    load()
                }
            }
        }
    }

    fun delete(requestId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = requestRepository.delete(requestId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "İstek silindi.")
                    load()
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
