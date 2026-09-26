package com.kitappla.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.AdminReportDetailDto
import com.kitappla.app.data.dto.AdminReportDto
import com.kitappla.app.data.repo.AdminRepository
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
    val selectedDetail: AdminReportDetailDto? = null,
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
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = adminRepository.getReportDetail(id)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            selectedDetail = res.value,
                            selectedReport = res.value.report,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                is ApiResult.Failure -> {
                    // Fallback to basic report from list if detail fails
                    val found = _state.value.reports.find { it.id == id }
                    _state.update {
                        it.copy(
                            selectedReport = found,
                            isLoading = false,
                            error = if (found == null) res.message else null,
                        )
                    }
                }
            }
        }
    }

    /** [onSent] yalnızca mesaj iletilince çağrılır; başarısızlıkta yazılan metin kutuda kalır. */
    fun sendSupportMessage(id: Long, text: String, onSent: () -> Unit = {}) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _state.value.isActionLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.sendSupportMessage(id, trimmed)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            actionMessage = "Mesajınız kullanıcıya iletildi.",
                            isActionLoading = false,
                        )
                    }
                    onSent()
                    selectReport(id)
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
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
                    selectReport(id)
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
