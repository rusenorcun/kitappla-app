package com.kitappla.app.ui.screens.swap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.OfferDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.repo.PickupPointRepository
import com.kitappla.app.data.repo.SwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TakasTeklifDetayUiState(
    val offer: OfferDto? = null,
    val isIncoming: Boolean = false,
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class TakasTeklifDetayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val swapRepository: SwapRepository,
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    val offerId: Long = checkNotNull(savedStateHandle["id"]) {
        "Teklif kimliği gerekli"
    }.toString().toLong()

    private val _state = MutableStateFlow(TakasTeklifDetayUiState())
    val state: StateFlow<TakasTeklifDetayUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val pointsResult = pickupPointRepository.getActivePoints()
            val points = if (pointsResult is ApiResult.Success) pointsResult.value else emptyList()

            val incResult = swapRepository.incomingOffers()
            val outResult = swapRepository.outgoingOffers()

            var foundOffer: OfferDto? = null
            var isInc = false

            if (incResult is ApiResult.Success) {
                foundOffer = incResult.value.find { it.id == offerId }
                if (foundOffer != null) isInc = true
            }

            if (foundOffer == null && outResult is ApiResult.Success) {
                foundOffer = outResult.value.find { it.id == offerId }
                if (foundOffer != null) isInc = false
            }

            if (foundOffer != null) {
                _state.update {
                    it.copy(
                        offer = foundOffer,
                        isIncoming = isInc,
                        pickupPoints = points,
                        isLoading = false,
                        error = null,
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        pickupPoints = points,
                        isLoading = false,
                        error = "Takas teklifi bulunamadı.",
                    )
                }
            }
        }
    }

    fun acceptOffer() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = swapRepository.acceptOffer(offerId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Teklif kabul edildi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun rejectOffer() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = swapRepository.rejectOffer(offerId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Teklif reddedildi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun cancelOffer() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = swapRepository.cancelOffer(offerId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Teklif geri çekildi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun handoverOffer() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = swapRepository.handoverOffer(offerId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Kitap teslim edildi olarak işaretlendi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun arrangeMeeting(pointId: Long?, note: String?, at: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val body = ArrangeMeetingBody(pointId = pointId, note = note, at = at)
            when (val res = swapRepository.arrangeMeeting(offerId, body)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Buluşma ayarlandı.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun noShowOffer() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = swapRepository.noShow(offerId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Karşı taraf gelmedi olarak bildirildi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }
}
