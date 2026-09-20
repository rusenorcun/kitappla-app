package com.kitap.app.ui.screens.auth

object AuthValidator {
    fun validateLogin(email: String, password: String): String? =
        if (email.isBlank() || password.isBlank()) "E-posta ve şifre gerekli." else null

    fun validateRegister(name: String, email: String, password: String, confirm: String): String? = when {
        name.isBlank() -> "Ad soyad gerekli."
        !email.trim().contains('@') -> "Geçerli bir e-posta adresi girin."
        password.isBlank() -> "Şifre gerekli."
        password != confirm -> "Şifreler eşleşmiyor."
        else -> null
    }
}
