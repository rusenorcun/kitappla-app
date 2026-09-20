package com.kitap.app.ui.screens.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.MyClaimDto
import com.kitap.app.data.repo.DonationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AldiklarimState(
    val loading: Boolean = true,
    val items: List<MyClaimDto> = emptyList(),
    val error: String? = null,
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class AldiklarimViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AldiklarimState())
    val state: StateFlow<AldiklarimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = donationRepository.myClaims()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, items = r.value)
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
