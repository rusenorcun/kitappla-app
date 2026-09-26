package com.kitappla.app.core.net

import kotlinx.serialization.json.Json

object KitapplaJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
