package com.kitappla.app.ui.screens.profile

private val TOKEN_PARAM = Regex("[?&]token=([A-Za-z0-9_-]+)", RegexOption.IGNORE_CASE)

/**
 * Kullanıcı e-postadaki bağlantının tamamını ya da yalnızca kodu yapıştırabilir. Bağlantıysa (`...?token=KOD`) sadece
 * `token` değerini döndürür; token'lar URL-güvenli Base64'tür (`A-Z a-z 0-9 - _`), yani ayrıca çözmeye gerek yoktur.
 * Bağlantıda token yoksa girdiyi olduğu gibi (kırpılmış) bırakır; reddi sunucu yapar.
 */
fun extractVerificationToken(input: String): String {
    val text = input.trim()
    return TOKEN_PARAM.find(text)?.groupValues?.get(1) ?: text
}
