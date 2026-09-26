package com.kitappla.app.ui.screens.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.SwapListingDto
import com.kitappla.app.data.repo.SwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TakasKitaplarimState(
    val loading: Boolean = true,
    val items: List<SwapListingDto> = emptyList(),
    val error: String? = null,
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class TakasKitaplarimViewModel @Inject constructor(
    private val swapRepository: SwapRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TakasKitaplarimState())
    val state: StateFlow<TakasKitaplarimState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = swapRepository.myBooks()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, items = r.value)
            }
        }
    }

    /** Kitap, ayrı "Takasa Kitap Ekle" ekranında eklenip geri dönüldü: listeyi tazele ve bildir. */
    fun onBookAdded() {
        _state.value = _state.value.copy(actionMessage = "Kitap takasa eklendi.")
        load()
    }

    fun setStatus(id: Long, status: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.setSwapBookStatus(id, status)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Durum güncellendi.")
                    load()
                }
            }
        }
    }

    fun moveToDonation(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.moveToDonation(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Kitap bağış havuzuna aktarıldı.")
                    load()
                }
            }
        }
    }

    fun removeBook(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = swapRepository.removeSwapBook(id)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Kitap takastan kaldırıldı.")
                    load()
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
