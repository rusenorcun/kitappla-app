package com.kitappla.app.ui.screens.donation

import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.BookMetadataDto
import com.kitappla.app.data.dto.CreateDonationBody
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.repo.BookRepository
import com.kitappla.app.data.repo.DonationRepository
import com.kitappla.app.data.repo.PickupPointRepository
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
data class BagisYeniState(
    val quantity: Int = 1,
    val targetLevel: String = "HEPSI",
    val description: String = "",
    val pointId: Long? = null,
    val pointNote: String = "",
    val pickupPoints: List<PickupPointDto> = emptyList(),
    val loadingPoints: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class BagisYeniViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
    private val pickupPointRepository: PickupPointRepository,
    bookRepository: BookRepository,
    uploadRepository: UploadRepository,
) : BookFormViewModel(bookRepository, uploadRepository) {

    private val _state = MutableStateFlow(BagisYeniState())
    val state: StateFlow<BagisYeniState> = _state.asStateFlow()

    init {
        loadPickupPoints()
    }

    private fun loadPickupPoints() {
        viewModelScope.launch {
            _state.update { it.copy(loadingPoints = true) }
            when (val r = pickupPointRepository.getActivePoints()) {
                is ApiResult.Failure -> _state.update { it.copy(loadingPoints = false) }
                is ApiResult.Success -> {
                    val points = r.value
                    _state.update {
                        it.copy(
                            loadingPoints = false,
                            pickupPoints = points,
                            pointId = it.pointId ?: points.firstOrNull()?.id,
                        )
                    }
                }
            }
        }
    }

    fun updateQuantity(v: Int) { if (v in 1..99) _state.update { it.copy(quantity = v) } }
    fun updateTargetLevel(v: String) = _state.update { it.copy(targetLevel = v) }
    fun updateDescription(v: String) = _state.update { it.copy(description = v) }
    fun updatePointId(v: Long?) = _state.update { it.copy(pointId = v, error = null) }
    fun updatePointNote(v: String) = _state.update { it.copy(pointNote = v) }

    override fun onMetadata(meta: BookMetadataDto) {
        _state.update { if (it.description.isBlank()) it.copy(description = meta.description.orEmpty()) else it }
    }

    fun submit(onSuccess: (Long) -> Unit) {
        val s = _state.value
        if (s.submitting || book.value.busy) return
        if (!validateBook()) return
        if (s.pointId == null && s.pickupPoints.isNotEmpty()) {
            _state.update { it.copy(error = "Lütfen bir buluşma yeri seçin.") }
            return
        }

        val b = book.value
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val body = CreateDonationBody(
                title = b.title.trim(),
                author = b.author.trim(),
                purchaseLink = b.purchaseLink.trim().ifBlank { null },
                quantity = s.quantity,
                targetLevel = s.targetLevel,
                source = "OWN",
                description = s.description.trim().ifBlank { null },
                coverUrl = b.coverUrl.trim().ifBlank { null },
                pointId = s.pointId,
                pointNote = s.pointNote.trim().ifBlank { null },
            )
            when (val r = donationRepository.createDonation(body)) {
                is ApiResult.Failure -> _state.update { it.copy(submitting = false, error = r.message) }
                is ApiResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    onSuccess(r.value.id)
                }
            }
        }
    }
}
