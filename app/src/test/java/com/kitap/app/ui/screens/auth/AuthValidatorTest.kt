package com.kitap.app.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun loginNeedsBothFields() {
        assertEquals("E-posta ve şifre gerekli.", AuthValidator.validateLogin("", "x"))
        assertEquals("E-posta ve şifre gerekli.", AuthValidator.validateLogin("a@b.com", "  "))
        assertNull(AuthValidator.validateLogin("a@b.com", "x"))
    }

    @Test
    fun registerChecksFieldsInOrder() {
        assertEquals("Ad soyad gerekli.", AuthValidator.validateRegister(" ", "a@b.com", "s", "s"))
        assertEquals("Geçerli bir e-posta adresi girin.", AuthValidator.validateRegister("Ayşe", "abc", "s", "s"))
        assertEquals("Şifre gerekli.", AuthValidator.validateRegister("Ayşe", "a@b.com", "", ""))
        assertEquals("Şifreler eşleşmiyor.", AuthValidator.validateRegister("Ayşe", "a@b.com", "s1", "s2"))
        assertEquals("Lütfen bir okul seçin.", AuthValidator.validateRegister("Ayşe", "a@b.com", "s1", "s1", "  "))
        assertNull(AuthValidator.validateRegister("Ayşe", "a@b.com", "s1", "s1", "ATATURK_UNIVERSITESI"))
        assertNull(AuthValidator.validateRegister("Ayşe", "a@b.com", "s1", "s1"))
    }
}
