package com.kitap.app.ui.screens.donation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.session.SessionManager
import com.kitap.app.data.dto.ClaimDto
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.repo.DonationRepository
import com.kitap.app.data.repo.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KitapDetayState(
    val loading: Boolean = true,
    val donation: DonationDto? = null,
    val error: String? = null,
    val currentUserId: Long? = null,
    val claimSuccess: ClaimDto? = null,
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class KitapDetayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val donationRepository: DonationRepository,
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val donationId: Long = savedStateHandle.get<Any>("id")?.toString()?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(KitapDetayState())
    val state: StateFlow<KitapDetayState> = _state.asStateFlow()

    init {
        val user = (sessionManager.state.value as? com.kitap.app.core.session.SessionState.Member)?.user
        _state.value = _state.value.copy(currentUserId = user?.id)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = donationRepository.getDonation(donationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, donation = r.value)
            }
        }
    }

    fun claim() {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.claim(donationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, claimSuccess = r.value, actionMessage = "Kitap başarıyla talep edildi!")
                    load()
                }
            }
        }
    }

    fun close() {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.closeDonation(donationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "İlan kapatıldı.")
                    load()
                }
            }
        }
    }

    fun reopen() {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.reopenDonation(donationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "İlan yeniden açıldı.")
                    load()
                }
            }
        }
    }

    fun moveToSwap(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.moveToSwap(donationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false)
                    onSuccess(r.value.id)
                }
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = donationRepository.deleteDonation(donationId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false)
                    onDeleted()
                }
            }
        }
    }

    fun report(reason: String, note: String?) {
        viewModelScope.launch {
            when (val r = reportRepository.report("DONATION", donationId, reason, note)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionMessage = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(actionMessage = "Şikâyetiniz yönetime iletildi.")
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
