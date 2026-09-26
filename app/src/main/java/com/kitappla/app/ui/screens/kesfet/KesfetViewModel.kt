package com.kitappla.app.ui.screens.kesfet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.DonationDto
import com.kitappla.app.data.repo.DonationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KesfetState(
    val items: List<DonationDto> = emptyList(),
    val total: Long = 0,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
)

@HiltViewModel
class KesfetViewModel @Inject constructor(private val repo: DonationRepository) : ViewModel() {
    private val _state = MutableStateFlow(KesfetState(loading = true))
    val state: StateFlow<KesfetState> = _state.asStateFlow()

    private var nextPage = 0
    private var job: Job? = null

    init {
        // Açılışta 1 dk içinde görülmüş ilk sayfa ön bellekten gelir; kullanıcı yenilediğinde ağa gidilir.
        nextPage = 0
        load(reset = true, forceRefresh = false)
    }

    fun refresh() {
        nextPage = 0
        load(reset = true, forceRefresh = true)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || s.endReached || s.error != null) return
        load(reset = false)
    }

    /** Alttaki hata satırı için: yüklenmiş sayfaları atmadan, hata veren sayfadan devam eder. */
    fun retry() {
        if (_state.value.items.isEmpty()) {
            refresh()
            return
        }
        _state.update { it.copy(error = null) }
        load(reset = false, forceRefresh = true)
    }

    private fun load(reset: Boolean, forceRefresh: Boolean = false) {
        job?.cancel()
        job = viewModelScope.launch {
            _state.update { if (reset) it.copy(loading = true, error = null) else it.copy(loadingMore = true) }
            when (val r = repo.page(nextPage, forceRefresh = forceRefresh)) {
                is ApiResult.Success -> {
                    nextPage++
                    _state.update { s ->
                        // Sayfalar arasında yeni bağış oluşursa aynı kayıt iki sayfada gelebilir; LazyColumn anahtarı
                        // benzersiz olmalı, bu yüzden ilk görülen kalır.
                        val items = (if (reset) r.value.items else s.items + r.value.items).distinctBy { it.id }
                        s.copy(
                            items = items,
                            total = r.value.total,
                            loading = false,
                            loadingMore = false,
                            error = null,
                            endReached = r.value.items.isEmpty() || items.size >= r.value.total,
                        )
                    }
                }
                is ApiResult.Failure ->
                    _state.update { it.copy(loading = false, loadingMore = false, error = r.message) }
            }
        }
    }
}
