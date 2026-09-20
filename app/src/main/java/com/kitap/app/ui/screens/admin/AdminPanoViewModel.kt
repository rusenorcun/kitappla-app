package com.kitap.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.AdminStatsDto
import com.kitap.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminPanoUiState(
    val stats: AdminStatsDto? = null,
    val pendingDocsCount: Int = 0,
    val openReportsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AdminPanoViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPanoUiState())
    val state: StateFlow<AdminPanoUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val statsResult = adminRepository.getStats()
            val docsResult = adminRepository.getPendingDocs()
            val reportsResult = adminRepository.getReports(all = false)

            if (statsResult is ApiResult.Success) {
                val docsCount = if (docsResult is ApiResult.Success) docsResult.value.size else 0
                val reportsCount = if (reportsResult is ApiResult.Success) reportsResult.value.size else 0
                _state.update {
                    it.copy(
                        stats = statsResult.value,
                        pendingDocsCount = docsCount,
                        openReportsCount = reportsCount,
                        isLoading = false,
                        error = null,
                    )
                }
            } else if (statsResult is ApiResult.Failure) {
                _state.update { it.copy(isLoading = false, error = statsResult.message) }
            }
        }
    }
}
