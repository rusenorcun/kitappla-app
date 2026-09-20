package com.kitap.app.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.MyReportDto
import com.kitap.app.data.repo.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SikayetlerimState(
    val items: List<MyReportDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class SikayetlerimViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SikayetlerimState())
    val state: StateFlow<SikayetlerimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = reportRepository.myReports()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, items = r.value)
            }
        }
    }
}
