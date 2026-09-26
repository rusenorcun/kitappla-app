package com.kitappla.app.ui.screens.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.MyDonationDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.repo.DonationRepository
import com.kitappla.app.data.repo.PickupPointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BagislarimState(
    val loading: Boolean = true,
    val items: List<MyDonationDto> = emptyList(),
    val error: String? = null,
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class BagislarimViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BagislarimState())
    val state: StateFlow<BagislarimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val pointsRes = pickupPointRepository.getActivePoints()
            val points = if (pointsRes is ApiResult.Success) pointsRes.value else emptyList()

            when (val r = donationRepository.myDonations()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message, pickupPoints = points)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, items = r.value, pickupPoints = points)
            }
        }
    }

    fun arrangeMeeting(claimId: Long, pointId: Long?, note: String?, at: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.arrangeMeeting(claimId, ArrangeMeetingBody(pointId, note, at))) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Buluşma ayarlandı.")
                    load()
                }
            }
        }
    }

    fun noShowClaim(claimId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.noShowClaim(claimId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Gelinmedi olarak işaretlendi.")
                    load()
                }
            }
        }
    }

    fun closeDonation(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.closeDonation(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Bağış kapatıldı.")
                    load()
                }
            }
        }
    }

    fun reopenDonation(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.reopenDonation(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Bağış yeniden açıldı.")
                    load()
                }
            }
        }
    }

    fun deleteDonation(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.deleteDonation(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Bağış silindi.")
                    load()
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
