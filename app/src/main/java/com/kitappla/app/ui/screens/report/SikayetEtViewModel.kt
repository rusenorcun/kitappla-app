package com.kitappla.app.ui.screens.report


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.data.dto.ReportReasons
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.repo.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SikayetEtState(
    val kind: String = "",
    val refId: Long = 0,
    val reason: String = ReportReasons.DEFAULT,
    val note: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class SikayetEtViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val kind: String = savedStateHandle["kind"] ?: "DONATION"
    private val refId: Long = (savedStateHandle.get<String>("refId"))?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(SikayetEtState(kind = kind, refId = refId))
    val state: StateFlow<SikayetEtState> = _state.asStateFlow()

    fun updateReason(v: String) { _state.value = _state.value.copy(reason = v) }
    fun updateNote(v: String) { _state.value = _state.value.copy(note = v) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(submitting = true, error = null)
            when (val r = reportRepository.report(s.kind, s.refId, s.reason, s.note.ifBlank { null })) {
                is ApiResult.Failure -> _state.value = _state.value.copy(submitting = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(submitting = false, success = true)
                    onSuccess()
                }
            }
        }
    }
}
