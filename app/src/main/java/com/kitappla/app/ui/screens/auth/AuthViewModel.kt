package com.kitappla.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.repo.AuthRepository
import com.kitappla.app.data.repo.LoginMode
import com.kitappla.app.ui.screens.profile.extractVerificationToken
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

    fun register(
        name: String,
        email: String,
        password: String,
        confirm: String,
        school: String = "",
        phone: String = "",
        onRegistered: (user: UserDto, verificationEmailSent: Boolean) -> Unit = { _, _ -> },
    ) {
        val problem = AuthValidator.validateRegister(name, email, password, confirm, school)
        if (problem != null) {
            _form.value = AuthFormState(error = problem)
            return
        }
        submit({ onRegistered(it.user, it.verificationEmailSent) }) { repo.register(name, email, password, school, phone) }
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

    /** [token] e-postadaki bağlantının tamamı da olabilir (yapıştırılan bağlantıdan jeton ayıklanır). */
    fun resetPassword(token: String, newPassword: String, confirmPassword: String, onSuccess: () -> Unit) {
        val code = extractVerificationToken(token)
        if (code.isBlank() || newPassword.isBlank()) {
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
            when (val r = repo.resetPassword(code, newPassword, confirmPassword)) {
                is ApiResult.Failure -> _form.value = AuthFormState(error = r.message)
                is ApiResult.Success -> {
                    _form.value = AuthFormState()
                    onSuccess()
                }
            }
        }
    }

    /** Başarıda oturum durumu değişir ve AppRoot grafiği yeniden kurar; form yalnızca sıfırlanır. */
    private fun <T> submit(onSuccess: (T) -> Unit = {}, call: suspend () -> ApiResult<T>) {
        if (_form.value.loading) return
        _form.value = AuthFormState(loading = true)
        viewModelScope.launch {
            when (val r = call()) {
                is ApiResult.Success -> {
                    _form.value = AuthFormState()
                    onSuccess(r.value)
                }
                is ApiResult.Failure -> _form.value = AuthFormState(error = r.message)
            }
        }
    }
}
