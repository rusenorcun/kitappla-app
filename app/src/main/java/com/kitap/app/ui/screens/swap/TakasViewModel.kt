package com.kitap.app.ui.screens.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.data.repo.SwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TakasState(
    val items: List<SwapListingDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val page: Int = 0,
    val hasMore: Boolean = true,
)

@HiltViewModel
class TakasViewModel @Inject constructor(
    private val swapRepository: SwapRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TakasState())
    val state: StateFlow<TakasState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load(reset = true)
    }

    fun load(reset: Boolean = false) {
        viewModelScope.launch {
            val s = _state.value
            val page = if (reset) 0 else s.page
            _state.value = s.copy(loading = reset, error = null)

            when (val r = swapRepository.discover(s.query.ifBlank { null }, page = page)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> {
                    val paged = r.value
                    val current = if (reset) emptyList() else s.items
                    val newItems = (current + paged.items).distinctBy { it.id }
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
}
