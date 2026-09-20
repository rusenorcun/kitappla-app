package com.kitap.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilState(
    val user: UserDto? = null,
    val name: String = "",
    val school: String = "",
    val address: String = "",
    val phone: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = true,
    val savingProfile: Boolean = false,
    val changingPassword: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
)

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfilState())
    val state: StateFlow<ProfilState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = profileRepository.getProfile()) {
                is ApiResult.Failure -> _state.value = _state.value.copy(loading = false, error = r.message)
                is ApiResult.Success -> {
                    val u = r.value.user
                    _state.value = _state.value.copy(
                        loading = false,
                        user = u,
                        name = u.name,
                        school = u.school.orEmpty(),
                        address = u.address.orEmpty(),
                        phone = u.phone.orEmpty(),
                    )
                }
            }
        }
    }

    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updateSchool(v: String) { _state.value = _state.value.copy(school = v) }
    fun updateAddress(v: String) { _state.value = _state.value.copy(address = v) }
    fun updatePhone(v: String) { _state.value = _state.value.copy(phone = v) }
    fun updateCurrentPassword(v: String) { _state.value = _state.value.copy(currentPassword = v) }
    fun updateNewPassword(v: String) { _state.value = _state.value.copy(newPassword = v) }
    fun updateConfirmPassword(v: String) { _state.value = _state.value.copy(confirmPassword = v) }

    fun saveProfile() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Ad Soyad alanı boş bırakılamaz.")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(savingProfile = true, error = null)
            when (val r = profileRepository.updateProfile(s.name, s.address.ifBlank { null }, s.phone.ifBlank { null }, s.school.ifBlank { null })) {
                is ApiResult.Failure -> _state.value = _state.value.copy(savingProfile = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(savingProfile = false, user = r.value, actionMessage = "Profil güncellendi.")
                }
            }
        }
    }

    fun changePassword() {
        val s = _state.value
        if (s.currentPassword.isBlank() || s.newPassword.isBlank()) {
            _state.value = s.copy(error = "Mevcut şifre ve yeni şifre alanları zorunludur.")
            return
        }
        if (s.newPassword != s.confirmPassword) {
            _state.value = s.copy(error = "Yeni şifre ile onay şifresi eşleşmiyor.")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(changingPassword = true, error = null)
            when (val r = profileRepository.changePassword(s.currentPassword, s.newPassword, s.confirmPassword)) {
                is ApiResult.Failure -> _state.value = _state.value.copy(changingPassword = false, error = r.message)
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        changingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        actionMessage = "Şifreniz başarıyla değiştirildi.",
                    )
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
