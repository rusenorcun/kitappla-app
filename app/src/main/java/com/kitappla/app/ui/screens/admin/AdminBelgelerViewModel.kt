package com.kitappla.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBelgelerUiState(
    val pendingUsers: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class AdminBelgelerViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminBelgelerUiState())
    val state: StateFlow<AdminBelgelerUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = adminRepository.getPendingDocs()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(pendingUsers = res.value, isLoading = false, error = null) }
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun approve(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.approveDoc(id)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            actionMessage = "Öğrenci belgesi onaylandı.",
                            isActionLoading = false,
                        )
                    }
                    loadData()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun reject(id: Long, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.rejectDoc(id, reason)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            actionMessage = "Öğrenci belgesi reddedildi.",
                            isActionLoading = false,
                        )
                    }
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
