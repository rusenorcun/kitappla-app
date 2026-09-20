package com.kitap.app.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.CreateRequestBody
import com.kitap.app.data.repo.BookRepository
import com.kitap.app.data.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IstekYeniState(
    val title: String = "",
    val author: String = "",
    val purchaseLink: String = "",
    val description: String = "",
    val fetchingPreview: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class IstekYeniViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IstekYeniState())
    val state: StateFlow<IstekYeniState> = _state.asStateFlow()

    fun updateTitle(v: String) { _state.value = _state.value.copy(title = v) }
    fun updateAuthor(v: String) { _state.value = _state.value.copy(author = v) }
    fun updatePurchaseLink(v: String) { _state.value = _state.value.copy(purchaseLink = v) }
    fun updateDescription(v: String) { _state.value = _state.value.copy(description = v) }

    fun fetchPreview() {
        val link = _state.value.purchaseLink.trim()
        if (link.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(fetchingPreview = true)
            when (val r = bookRepository.preview(link)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(fetchingPreview = false)
                is ApiResult.Success -> {
                    val meta = r.value
                    _state.value = _state.value.copy(
                        fetchingPreview = false,
                        title = if (_state.value.title.isBlank()) meta.title.orEmpty() else _state.value.title,
                        author = if (_state.value.author.isBlank()) meta.author.orEmpty() else _state.value.author,
                        description = if (_state.value.description.isBlank()) meta.description.orEmpty() else _state.value.description,
                    )
                }
            }
        }
    }

    fun submit(onSuccess: (Long) -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.value = s.copy(error = "Lütfen kitap adını girin.")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(submitting = true, error = null)
            val body = CreateRequestBody(
                title = s.title.trim(),
                author = s.author.trim().ifBlank { null },
                purchaseLink = s.purchaseLink.trim().ifBlank { null },
                description = s.description.trim().ifBlank { null },
            )
            when (val r = requestRepository.createRequest(body)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(submitting = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(submitting = false)
                    onSuccess(r.value.id)
                }
            }
        }
    }
}
