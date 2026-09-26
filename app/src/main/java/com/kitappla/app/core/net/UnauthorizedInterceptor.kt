package com.kitappla.app.core.net

import okhttp3.Interceptor
import okhttp3.Response

/** Korumalı uçta 401 gelirse oturumun düştüğünü bildirir (auth uçları hariç: giriş hataları 400 döner). */
class UnauthorizedInterceptor(private val onUnauthorized: () -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 && !chain.request().url.encodedPath.startsWith("/api/v1/auth/")) {
            onUnauthorized()
        }
        return response
    }
}
