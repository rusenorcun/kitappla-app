package com.kitappla.app.data.dto

enum class School(val label: String) {
    ATATURK_UNIVERSITESI("Atatürk Üniversitesi"),
    ERZURUM_TEKNIK_UNIVERSITESI("Erzurum Teknik Üniversitesi");

    companion object {
        fun of(name: String?): School? {
            if (name.isNullOrBlank()) return null
            return try {
                entries.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
            } catch (_: Exception) {
                null
            }
        }
    }
}
