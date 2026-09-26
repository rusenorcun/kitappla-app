package com.kitappla.app.ui.screens.swap

import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.CreateSwapBookBody
import com.kitappla.app.data.repo.BookRepository
import com.kitappla.app.data.repo.SwapRepository
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
data class TakasKitapEkleState(
    val note: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
)

/** Web'deki "Takas kitaplarım → Kitap ekle" formunun karşılığı: linkten getirme ve kapak görseli dahil. */
@HiltViewModel
class TakasKitapEkleViewModel @Inject constructor(
    private val swapRepository: SwapRepository,
    bookRepository: BookRepository,
    uploadRepository: UploadRepository,
) : BookFormViewModel(bookRepository, uploadRepository) {

    private val _state = MutableStateFlow(TakasKitapEkleState())
    val state: StateFlow<TakasKitapEkleState> = _state.asStateFlow()

    fun updateNote(v: String) = _state.update { it.copy(note = v) }

    fun submit(onSuccess: () -> Unit) {
        if (_state.value.submitting || book.value.busy) return
        if (!validateBook()) return

        val b = book.value
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val body = CreateSwapBookBody(
                title = b.title.trim(),
                author = b.author.trim(),
                note = _state.value.note.trim().ifBlank { null },
                purchaseLink = b.purchaseLink.trim().ifBlank { null },
                coverUrl = b.coverUrl.trim().ifBlank { null },
            )
            when (val r = swapRepository.addSwapBook(body)) {
                is ApiResult.Failure -> _state.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    onSuccess()
                }
            }
        }
    }
}
