package com.kitappla.app.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.RequestDto
import com.kitappla.app.data.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IsteklerState(
    val items: List<RequestDto> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val page: Int = 0,
    val hasMore: Boolean = true,
    val actionLoading: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class IsteklerViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IsteklerState())
    val state: StateFlow<IsteklerState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        load(reset = true)
    }

    /**
     * [reset] ilk sayfadan yeniden yükler (arama, yenileme); değilse sonraki sayfayı ekler. Yeni yükleme süreni iptal
     * eder: yavaş gelen eski arama yanıtı yeni sorgunun sonucunun üstüne yazılamaz. Sonraki sayfa yalnızca başka bir
     * yükleme yokken, hata beklemiyorken ve sunucuda kayıt kalmışken istenir.
     */
    fun load(reset: Boolean = false) {
        if (!reset) {
            val s = _state.value
            if (s.loading || s.loadingMore || !s.hasMore || s.error != null) return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val s = _state.value
            val page = if (reset) 0 else s.page
            _state.value = s.copy(loading = reset, loadingMore = !reset, error = null)

            when (val r = requestRepository.openRequests(s.query.ifBlank { null }, page = page)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, loadingMore = false, error = r.message)
                is ApiResult.Success -> {
                    val paged = r.value
                    val currentItems = if (reset) emptyList() else _state.value.items
                    val newItems = (currentItems + paged.items).distinctBy { it.id }
                    _state.value = _state.value.copy(
                        loading = false,
                        loadingMore = false,
                        items = newItems,
                        page = page + 1,
                        hasMore = paged.items.isNotEmpty() && newItems.size < paged.total,
                    )
                }
            }
        }
    }

    fun loadMore() = load(reset = false)

    /** Liste sonundaki "Tekrar deneyin": yüklenmiş sayfalar korunur, hata veren sayfa yeniden istenir. */
    fun retryMore() {
        _state.value = _state.value.copy(error = null)
        load(reset = false)
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load(reset = true)
        }
    }

    fun fulfill(requestId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionLoading = true)
            when (val r = requestRepository.fulfillRequest(requestId)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(actionLoading = false, actionMessage = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(actionLoading = false, actionMessage = "İsteği karşılamayı kabul ettiniz!")
                    load(reset = true)
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
