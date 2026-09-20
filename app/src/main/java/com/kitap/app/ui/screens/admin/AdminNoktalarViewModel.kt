package com.kitap.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.repo.PickupPointRepository
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
)

@HiltViewModel
class AdminNoktalarViewModel @Inject constructor(
    private val pickupPointRepository: PickupPointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminNoktalarUiState())
    val state: StateFlow<AdminNoktalarUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = pickupPointRepository.getActivePoints()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(points = res.value, isLoading = false, error = null) }
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }
}
