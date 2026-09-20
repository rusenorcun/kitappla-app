package com.kitap.app.ui.screens.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.CreateSwapBookBody
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.data.repo.BookRepository
import com.kitap.app.data.repo.SwapRepository
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
    private val bookRepository: BookRepository,
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

    fun addBook(title: String, author: String?, note: String?, link: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            val body = CreateSwapBookBody(title = title.trim(), author = author?.trim(), note = note?.trim(), purchaseLink = link?.trim())
            when (val r = swapRepository.addSwapBook(body)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "Kitap takasa eklendi.")
                    load()
                }
            }
        }
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
