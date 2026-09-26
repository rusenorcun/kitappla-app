package com.kitappla.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.DonationDto
import com.kitappla.app.data.dto.RequestDto
import com.kitappla.app.data.dto.SwapListingDto
import com.kitappla.app.data.repo.AdminRepository
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
    private val adminRepository: AdminRepository,
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
            when (val res = adminRepository.getContent()) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            donations = res.value.donations,
                            requests = res.value.requests,
                            swaps = res.value.swaps,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isLoading = false, error = res.message)
                    }
                }
            }
        }
    }

    fun removeDonation(id: Long, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.removeDonation(id, reason)) {
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

    fun removeRequest(id: Long, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.removeRequest(id, reason)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "İstek kaldırıldı.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun removeSwapBook(id: Long, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.removeSwapBook(id, reason)) {
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
