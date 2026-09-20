package com.kitap.app.ui.screens.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.CreateDonationBody
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.repo.BookRepository
import com.kitap.app.data.repo.DonationRepository
import com.kitap.app.data.repo.PickupPointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BagisYeniState(
    val title: String = "",
    val author: String = "",
    val purchaseLink: String = "",
    val coverUrl: String = "",
    val quantity: Int = 1,
    val targetLevel: String = "HEPSI",
    val description: String = "",
    val pointId: Long? = null,
    val pointNote: String = "",
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val loadingPoints: Boolean = false,
    val fetchingPreview: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class BagisYeniViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
    private val pickupPointRepository: PickupPointRepository,
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BagisYeniState())
    val state: StateFlow<BagisYeniState> = _state.asStateFlow()

    init {
        loadPickupPoints()
    }

    private fun loadPickupPoints() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingPoints = true)
            when (val r = pickupPointRepository.getActivePoints()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loadingPoints = false)
                is ApiResult.Success -> {
                    val points = r.value
                    _state.value = _state.value.copy(
                        loadingPoints = false,
                        pickupPoints = points,
                        pointId = points.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun updateTitle(v: String) { _state.value = _state.value.copy(title = v) }
    fun updateAuthor(v: String) { _state.value = _state.value.copy(author = v) }
    fun updatePurchaseLink(v: String) { _state.value = _state.value.copy(purchaseLink = v) }
    fun updateCoverUrl(v: String) { _state.value = _state.value.copy(coverUrl = v) }
    fun updateQuantity(v: Int) { if (v in 1..99) _state.value = _state.value.copy(quantity = v) }
    fun updateTargetLevel(v: String) { _state.value = _state.value.copy(targetLevel = v) }
    fun updateDescription(v: String) { _state.value = _state.value.copy(description = v) }
    fun updatePointId(v: Long?) { _state.value = _state.value.copy(pointId = v) }
    fun updatePointNote(v: String) { _state.value = _state.value.copy(pointNote = v) }

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
                        coverUrl = if (_state.value.coverUrl.isBlank()) meta.imageUrl.orEmpty() else _state.value.coverUrl,
                        description = if (_state.value.description.isBlank()) meta.description.orEmpty() else _state.value.description,
                    )
                }
            }
        }
    }

    fun submit(onSuccess: (Long) -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.value = s.copy(error = "Lütfen kitap başlığını girin.")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(submitting = true, error = null)
            val body = CreateDonationBody(
                title = s.title.trim(),
                author = s.author.trim().ifBlank { null },
                purchaseLink = s.purchaseLink.trim().ifBlank { null },
                quantity = s.quantity,
                targetLevel = s.targetLevel,
                source = "OWN",
                description = s.description.trim().ifBlank { null },
                coverUrl = s.coverUrl.trim().ifBlank { null },
                pointId = s.pointId,
                pointNote = s.pointNote.trim().ifBlank { null },
            )
            when (val r = donationRepository.createDonation(body)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(submitting = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(submitting = false)
                    onSuccess(r.value.id)
                }
            }
        }
    }
}
