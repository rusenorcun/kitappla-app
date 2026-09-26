package com.kitappla.app.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.dto.RequestDto
import com.kitappla.app.data.repo.PickupPointRepository
import com.kitappla.app.data.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KarsiladiklarimState(
    val loading: Boolean = true,
    val items: List<RequestDto> = emptyList(),
    val error: String? = null,
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class KarsiladiklarimViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(KarsiladiklarimState())
    val state: StateFlow<KarsiladiklarimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val pointsRes = pickupPointRepository.getActivePoints()
            val points = if (pointsRes is ApiResult.Success) pointsRes.value else emptyList()

            when (val r = requestRepository.fulfilledByMe()) {
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
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Buluşma ayarlandı.")
                    load()
                }
            }
        }
    }

    fun noShow(requestId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = requestRepository.noShow(requestId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Gelinmedi olarak işaretlendi.")
                    load()
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
