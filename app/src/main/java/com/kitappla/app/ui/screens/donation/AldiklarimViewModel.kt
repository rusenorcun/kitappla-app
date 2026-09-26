package com.kitappla.app.ui.screens.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.MyClaimDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.repo.DonationRepository
import com.kitappla.app.data.repo.PickupPointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AldiklarimState(
    val loading: Boolean = true,
    val items: List<MyClaimDto> = emptyList(),
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val error: String? = null,
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class AldiklarimViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AldiklarimState())
    val state: StateFlow<AldiklarimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val pointsRes = pickupPointRepository.getActivePoints()
            val points = if (pointsRes is ApiResult.Success) pointsRes.value else emptyList()

            when (val r = donationRepository.myClaims()) {
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

    fun noShow(claimId: Long) {
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

    /** Kitabı alan taraf teslimi onaylar (backend: yalnızca alıcı; buluşma ayarlandıktan sonra). */
    fun deliver(claimId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.deliverClaim(claimId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Kitabı teslim aldığınız kaydedildi.")
                    load()
                }
            }
        }
    }

    fun thank(claimId: Long, message: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.thankClaim(claimId, message)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Teşekkürünüz bağışçıya iletildi.")
                    load()
                }
            }
        }
    }

    fun cancel(claimId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.cancelClaim(claimId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Talep iptal edildi.")
                    load()
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
