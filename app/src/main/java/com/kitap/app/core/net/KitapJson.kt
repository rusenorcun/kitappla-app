package com.kitap.app.core.net

import kotlinx.serialization.json.Json

object KitapJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
