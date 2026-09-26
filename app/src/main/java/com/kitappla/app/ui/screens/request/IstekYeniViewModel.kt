package com.kitappla.app.ui.screens.request

import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.BookMetadataDto
import com.kitappla.app.data.dto.CreateRequestBody
import com.kitappla.app.data.repo.BookRepository
import com.kitappla.app.data.repo.RequestRepository
import com.kitappla.app.data.repo.UploadRepository
import com.kitappla.app.ui.screens.common.BookFormViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Kitap bilgileri (ad, yazar, link, kapak) [BookFormViewModel.book]'tadır. */
data class IstekYeniState(
    val description: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class IstekYeniViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    bookRepository: BookRepository,
    uploadRepository: UploadRepository,
) : BookFormViewModel(bookRepository, uploadRepository) {

    private val _state = MutableStateFlow(IstekYeniState())
    val state: StateFlow<IstekYeniState> = _state.asStateFlow()

    fun updateDescription(v: String) = _state.update { it.copy(description = v) }

    override fun onMetadata(meta: BookMetadataDto) {
        _state.update { if (it.description.isBlank()) it.copy(description = meta.description.orEmpty()) else it }
    }

    fun submit(onSuccess: (Long) -> Unit) {
        if (_state.value.submitting || book.value.busy) return
        if (!validateBook()) return

        val b = book.value
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val body = CreateRequestBody(
                title = b.title.trim(),
                author = b.author.trim(),
                purchaseLink = b.purchaseLink.trim().ifBlank { null },
                description = _state.value.description.trim().ifBlank { null },
                coverUrl = b.coverUrl.trim().ifBlank { null },
            )
            when (val r = requestRepository.createRequest(body)) {
                is ApiResult.Failure -> _state.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    onSuccess(r.value.id)
                }
            }
        }
    }
}
