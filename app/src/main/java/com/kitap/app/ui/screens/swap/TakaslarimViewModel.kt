package com.kitap.app.ui.screens.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.ArrangeMeetingBody
import com.kitap.app.data.dto.OfferDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.repo.PickupPointRepository
import com.kitap.app.data.repo.SwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TakaslarimState(
    val selectedTab: Int = 0, // 0: Gelen, 1: Giden
    val incoming: List<OfferDto> = emptyList(),
    val outgoing: List<OfferDto> = emptyList(),
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class TakaslarimViewModel @Inject constructor(
    private val swapRepository: SwapRepository,
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TakaslarimState())
    val state: StateFlow<TakaslarimState> = _state.asStateFlow()

    init {
        load()
    }

    fun setTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val pointsRes = pickupPointRepository.getActivePoints()
            val points = if (pointsRes is ApiResult.Success) pointsRes.value else emptyList()

            val inRes = swapRepository.incomingOffers()
            val outRes = swapRepository.outgoingOffers()

            val inList = if (inRes is ApiResult.Success) inRes.value else emptyList()
            val outList = if (outRes is ApiResult.Success) outRes.value else emptyList()

            val err = (inRes as? ApiResult.Failure)?.message ?: (outRes as? ApiResult.Failure)?.message

            _state.value = _state.value.copy(
                loading = false,
                incoming = inList,
                outgoing = outList,
                pickupPoints = points,
                error = err,
            )
        }
    }

    fun acceptOffer(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.acceptOffer(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teklif kabul edildi!")
                    load()
                }
            }
        }
    }

    fun rejectOffer(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.rejectOffer(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teklif reddedildi.")
                    load()
                }
            }
        }
    }

    fun cancelOffer(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.cancelOffer(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teklif iptal edildi.")
                    load()
                }
            }
        }
    }

    fun handoverOffer(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.handoverOffer(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teslimat onaylandı.")
                    load()
                }
            }
        }
    }

    fun arrangeMeeting(id: Long, pointId: Long?, note: String?, at: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.arrangeMeeting(id, ArrangeMeetingBody(pointId, note, at))) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Buluşma ayarlandı.")
                    load()
                }
            }
        }
    }

    fun noShowOffer(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.noShow(id)) {
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
