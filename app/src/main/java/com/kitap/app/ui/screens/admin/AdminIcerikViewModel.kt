package com.kitap.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.data.repo.DonationRepository
import com.kitap.app.data.repo.RequestRepository
import com.kitap.app.data.repo.SwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AdminIcerikTab {
    BAGISLAR, ISTEKLER, TAKASLAR
}

data class AdminIcerikUiState(
    val selectedTab: AdminIcerikTab = AdminIcerikTab.BAGISLAR,
    val donations: List<DonationDto> = emptyList(),
    val requests: List<RequestDto> = emptyList(),
    val swaps: List<SwapListingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class AdminIcerikViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
    private val requestRepository: RequestRepository,
    private val swapRepository: SwapRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminIcerikUiState())
    val state: StateFlow<AdminIcerikUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun setTab(tab: AdminIcerikTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val donResult = donationRepository.page(page = 0, size = 50)
            val reqResult = requestRepository.openRequests(size = 50)
            val swapResult = swapRepository.discover(size = 50)

            val donList = if (donResult is ApiResult.Success) donResult.value.items else emptyList()
            val reqList = if (reqResult is ApiResult.Success) reqResult.value.items else emptyList()
            val swapList = if (swapResult is ApiResult.Success) swapResult.value.items else emptyList()

            _state.update {
                it.copy(
                    donations = donList,
                    requests = reqList,
                    swaps = swapList,
                    isLoading = false,
                    error = null,
                )
            }
        }
    }

    fun removeDonation(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = donationRepository.closeDonation(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Bağış yayından kaldırıldı.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun removeSwapBook(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = swapRepository.removeSwapBook(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Takas ilanı kaldırıldı.", isActionLoading = false) }
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
