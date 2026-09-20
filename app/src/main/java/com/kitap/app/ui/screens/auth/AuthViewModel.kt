package com.kitap.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.repo.AuthRepository
import com.kitap.app.data.repo.LoginMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(val loading: Boolean = false, val error: String? = null)

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {
    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    fun login(email: String, password: String, mode: LoginMode) {
        val problem = AuthValidator.validateLogin(email, password)
        if (problem != null) {
            _form.value = AuthFormState(error = problem)
            return
        }
        submit { repo.login(email, password, mode) }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        val problem = AuthValidator.validateRegister(name, email, password, confirm)
        if (problem != null) {
            _form.value = AuthFormState(error = problem)
            return
        }
        submit { repo.register(name, email, password) }
    }

    fun forgotPassword(email: String, onSent: (String) -> Unit) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains('@')) {
            _form.value = AuthFormState(error = "Lütfen geçerli bir e-posta adresi girin.")
            return
        }
        _form.value = AuthFormState(loading = true)
        viewModelScope.launch {
            when (val r = repo.forgotPassword(trimmed)) {
                is ApiResult.Failure -> _form.value = AuthFormState(error = r.message)
                is ApiResult.Success -> {
                    _form.value = AuthFormState()
                    onSent(r.value)
                }
            }
        }
    }

    fun resetPassword(token: String, newPassword: String, confirmPassword: String, onSuccess: () -> Unit) {
        if (token.isBlank() || newPassword.isBlank()) {
            _form.value = AuthFormState(error = "Lütfen tüm alanları doldurun.")
            return
        }
        if (newPassword.length < 6) {
            _form.value = AuthFormState(error = "Şifre en az 6 karakter olmalıdır.")
            return
        }
        if (newPassword != confirmPassword) {
            _form.value = AuthFormState(error = "Şifreler eşleşmiyor.")
            return
        }
        _form.value = AuthFormState(loading = true)
        viewModelScope.launch {
            when (val r = repo.resetPassword(token, newPassword, confirmPassword)) {
                is ApiResult.Failure -> _form.value = AuthFormState(error = r.message)
                is ApiResult.Success -> {
                    _form.value = AuthFormState()
                    onSuccess()
                }
            }
        }
    }

    /** Başarıda oturum durumu değişir ve AppRoot grafiği yeniden kurar; form yalnızca sıfırlanır. */
    private fun submit(call: suspend () -> ApiResult<UserDto>) {
        if (_form.value.loading) return
        _form.value = AuthFormState(loading = true)
        viewModelScope.launch {
            _form.value = when (val r = call()) {
                is ApiResult.Success -> AuthFormState()
                is ApiResult.Failure -> AuthFormState(error = r.message)
            }
        }
    }
}
