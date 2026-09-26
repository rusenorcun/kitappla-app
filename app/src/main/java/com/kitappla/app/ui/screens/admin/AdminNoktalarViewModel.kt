package com.kitappla.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminNoktalarUiState(
    val points: List<PickupPointDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class AdminNoktalarViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminNoktalarUiState())
    val state: StateFlow<AdminNoktalarUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = adminRepository.getAllPickupPoints()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(points = res.value, isLoading = false, error = null) }
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun createPoint(campus: String, name: String, description: String?) {
        val c = campus.trim()
        val n = name.trim()
        if (c.isBlank() || n.isBlank()) {
            _state.update { it.copy(actionMessage = "Kampüs ve nokta adı zorunludur.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.createPickupPoint(c, n, description?.trim()?.ifBlank { null })) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "${res.value.name} noktası eklendi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun updatePoint(id: Long, campus: String, name: String, description: String?) {
        val c = campus.trim()
        val n = name.trim()
        if (c.isBlank() || n.isBlank()) {
            _state.update { it.copy(actionMessage = "Kampüs ve nokta adı zorunludur.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.updatePickupPoint(id, c, n, description?.trim()?.ifBlank { null })) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "${res.value.name} güncellendi.", isActionLoading = false) }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun toggleActive(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.togglePickupPointActive(id)) {
                is ApiResult.Success -> {
                    val statusText = if (res.value.active) "aktifleştirildi" else "pasifleştirildi"
                    _state.update { it.copy(actionMessage = "${res.value.name} $statusText.", isActionLoading = false) }
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
