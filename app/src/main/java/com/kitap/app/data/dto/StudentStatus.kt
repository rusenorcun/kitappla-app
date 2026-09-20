package com.kitap.app.data.dto

/**
 * API'nin `studentStatus` alanının aldığı değerler: backend `StudentStatus` enum'unun adları
 * (`ApiDtoMapper` enum'u `name()` ile ham gönderir). Uygulamada bu metinleri yeniden uydurma; buradan kullan.
 */
object StudentStatus {
    const val NONE = "NONE"
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}

/** E-posta bağlantısı ya da yönetici onayıyla onaylanmış öğrenci. */
val UserDto.isApprovedStudent: Boolean get() = studentStatus == StudentStatus.APPROVED
