package com.kitap.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.dto.isApprovedStudent
import com.kitap.app.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OgrenciDogrulamaState(
    val user: UserDto? = null,
    val email: String = "",
    val token: String = "",
    val sendingEmail: Boolean = false,
    val confirmingToken: Boolean = false,
    val isConfirmed: Boolean = false,
    val verificationSent: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
)

@HiltViewModel
class OgrenciDogrulamaViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OgrenciDogrulamaState())
    val state: StateFlow<OgrenciDogrulamaState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /**
     * Profili sessizce (yükleme göstergesi olmadan) yeniler; ekrana her dönüşte de çağrılır, çünkü doğrulama bağlantısı
     * başka bir cihazda/tarayıcıda onaylanmış olabilir. Devam eden bir yükleme varsa yeni istek atmadan ona katılır.
     */
    fun load(): Job {
        loadJob?.takeIf { it.isActive }?.let { return it }
        return viewModelScope.launch {
            when (val r = profileRepository.getProfile()) {
                is ApiResult.Failure -> {}
                is ApiResult.Success -> {
                    val u = r.value.user
                    _state.value = _state.value.copy(
                        user = u,
                        isConfirmed = u.isApprovedStudent,
                    )
                }
            }
        }.also { loadJob = it }
    }

    fun updateEmail(v: String) { _state.value = _state.value.copy(email = v) }
    fun updateToken(v: String) { _state.value = _state.value.copy(token = v) }

    fun sendVerification() {
        val em = _state.value.email.trim()
        if (em.isBlank()) {
            _state.value = _state.value.copy(error = "Lütfen öğrenci e-posta adresinizi girin.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(sendingEmail = true, error = null)
            when (val r = profileRepository.verifyStudent(em)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(sendingEmail = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        sendingEmail = false,
                        verificationSent = true,
                        actionMessage = "Doğrulama bağlantısı e-posta adresinize gönderildi.",
                    )
                }
            }
        }
    }

    fun confirmToken() {
        val t = extractVerificationToken(_state.value.token)
        if (t.isBlank()) {
            _state.value = _state.value.copy(error = "Lütfen doğrulama bağlantısını ya da kodunu yapıştırın.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(confirmingToken = true, error = null)
            when (val r = profileRepository.confirmStudent(t)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(confirmingToken = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        confirmingToken = false,
                        isConfirmed = true,
                        actionMessage = "Öğrenci durumunuz başarıyla doğrulandı!",
                    )
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
