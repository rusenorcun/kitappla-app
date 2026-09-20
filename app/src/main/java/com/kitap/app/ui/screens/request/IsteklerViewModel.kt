package com.kitap.app.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.repo.RequestRepository
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

    init {
        load(reset = true)
    }

    fun load(reset: Boolean = false) {
        viewModelScope.launch {
            val s = _state.value
            val page = if (reset) 0 else s.page
            _state.value = s.copy(loading = reset, error = null)

            when (val r = requestRepository.openRequests(s.query.ifBlank { null }, page = page)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> {
                    val paged = r.value
                    val currentItems = if (reset) emptyList() else s.items
                    val newItems = (currentItems + paged.items).distinctBy { it.id }
                    _state.value = _state.value.copy(
                        loading = false,
                        items = newItems,
                        page = page + 1,
                        hasMore = newItems.size < paged.total,
                    )
                }
            }
        }
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
