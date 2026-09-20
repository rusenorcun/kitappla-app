package com.kitap.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.AdminReportDto
import com.kitap.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminSikayetUiState(
    val reports: List<AdminReportDto> = emptyList(),
    val selectedReport: AdminReportDto? = null,
    val showAll: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class AdminSikayetViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminSikayetUiState())
    val state: StateFlow<AdminSikayetUiState> = _state.asStateFlow()

    init {
        loadReports()
    }

    fun toggleShowAll() {
        val newShowAll = !_state.value.showAll
        _state.update { it.copy(showAll = newShowAll) }
        loadReports(newShowAll)
    }

    fun loadReports(all: Boolean = _state.value.showAll) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = adminRepository.getReports(all)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(reports = res.value, isLoading = false, error = null) }
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun selectReport(id: Long) {
        viewModelScope.launch {
            val existing = _state.value.reports.find { it.id == id }
            if (existing != null) {
                _state.update { it.copy(selectedReport = existing) }
            } else {
                // Fetch reports if not in memory
                when (val res = adminRepository.getReports(true)) {
                    is ApiResult.Success -> {
                        val found = res.value.find { it.id == id }
                        _state.update { it.copy(reports = res.value, selectedReport = found) }
                    }
                    is ApiResult.Failure -> {
                        _state.update { it.copy(error = res.message) }
                    }
                }
            }
        }
    }

    fun resolveReport(id: Long, actioned: Boolean, adminNote: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.resolveReport(id, actioned, adminNote)) {
                is ApiResult.Success -> {
                    val msg = if (actioned) "Şikâyet işleme alındı olarak sonuçlandırıldı." else "Şikâyet işlem gerekmedi olarak kapatıldı."
                    _state.update { it.copy(actionMessage = msg, isActionLoading = false) }
                    loadReports()
                    // update selected report if currently viewed
                    _state.update { s ->
                        s.copy(
                            selectedReport = s.selectedReport?.copy(
                                status = if (actioned) "ACTIONED" else "DISMISSED",
                                adminNote = adminNote,
                            ),
                        )
                    }
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
