package com.kitappla.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.MeDto
import com.kitappla.app.data.repo.AuthRepository
import com.kitappla.app.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PanomState(
    val me: MeDto? = null,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class PanomViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PanomState())
    val state: StateFlow<PanomState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = profileRepository.getProfile()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, me = r.value)
            }
        }
    }

    /**
     * Uygulamaya dönünce sessiz yenileme (ör. öğrenci doğrulaması başka bir cihazda onaylandı): içerik yerinde kalır,
     * hata yutulur. Devam eden bir yükleme varsa yeni istek atmadan ona katılır.
     */
    fun refresh(): Job {
        loadJob?.takeIf { it.isActive }?.let { return it }
        return viewModelScope.launch {
            when (val r = profileRepository.getProfile()) {
                is ApiResult.Success -> _state.value = _state.value.copy(me = r.value, error = null)
                is ApiResult.Failure -> Unit
            }
        }.also { loadJob = it }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
