package com.kitappla.app.ui.screens.swap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.SwapListingDto
import com.kitappla.app.data.dto.SwapBookStatus
import com.kitappla.app.data.repo.SwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TakasTeklifState(
    val targetBookId: Long? = null,
    val myBooks: List<SwapListingDto> = emptyList(),
    val selectedMyBookId: Long? = null,
    val message: String = "",
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TakasTeklifViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val swapRepository: SwapRepository,
) : ViewModel() {

    /** Rota argümanı metin olarak gelir (`takas/teklif?targetBookId=5`); boşsa hedef yoktur. */
    private val targetBookId: Long? = when (val raw = savedStateHandle.get<Any?>("targetBookId")) {
        is Long -> raw
        is String -> raw.toLongOrNull()
        else -> null
    }

    private val _state = MutableStateFlow(TakasTeklifState(targetBookId = targetBookId))
    val state: StateFlow<TakasTeklifState> = _state.asStateFlow()

    init {
        loadMyBooks()
    }

    private fun loadMyBooks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = swapRepository.myBooks()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> {
                    val openBooks = r.value.filter { it.status == SwapBookStatus.OPEN }
                    _state.value = _state.value.copy(
                        loading = false,
                        myBooks = openBooks,
                        selectedMyBookId = openBooks.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun selectBook(id: Long) {
        _state.value = _state.value.copy(selectedMyBookId = id)
    }

    fun updateMessage(msg: String) {
        _state.value = _state.value.copy(message = msg)
    }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        val target = s.targetBookId
        val offered = s.selectedMyBookId

        if (target == null || offered == null) {
            _state.value = s.copy(error = "Lütfen takas edeceğiniz kitabı seçin.")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(submitting = true, error = null)
            when (val r = swapRepository.createOffer(target, offered, s.message.ifBlank { null })) {
                is ApiResult.Failure -> _state.value = _state.value.copy(submitting = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(submitting = false)
                    onSuccess()
                }
            }
        }
    }
}
