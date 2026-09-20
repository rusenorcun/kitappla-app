package com.kitap.app.core.net

import com.kitap.app.data.dto.CsrfTokenDto
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Uygulama interceptor'ı: değiştirici isteklere CSRF başlığı ekler. Token yoksa `GET /api/v1/auth/csrf`
 * ile alır; 403'te token'ı yenileyip bir kez yeniden dener; giriş/kayıt/çıkış başarısında token'ı düşürür
 * (sunucu oturum değişince token'ı yeniler).
 */
class CsrfInterceptor(private val baseUrl: HttpUrl) : Interceptor {
    @Volatile private var header: String? = null
    @Volatile private var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method in SAFE_METHODS) return chain.proceed(request)

        var response = chain.proceed(withToken(chain, request))
        if (response.code == 403) {
            response.close()
            invalidate()
            response = chain.proceed(withToken(chain, request))
        }
        if (response.isSuccessful && request.url.encodedPath in SESSION_CHANGING_PATHS) invalidate()
        return response
    }

    private fun withToken(chain: Interceptor.Chain, request: Request): Request {
        val (name, value) = currentToken(chain)
        return request.newBuilder().header(name, value).build()
    }

    @Synchronized
    private fun currentToken(chain: Interceptor.Chain): Pair<String, String> {
        val h = header
        val t = token
        if (h != null && t != null) return h to t
        val fetched = fetch(chain)
        header = fetched.headerName
        token = fetched.token
        return fetched.headerName to fetched.token
    }

    private fun fetch(chain: Interceptor.Chain): CsrfTokenDto {
        val url = baseUrl.newBuilder().addPathSegments("api/v1/auth/csrf").build()
        chain.proceed(Request.Builder().url(url).get().build()).use { response ->
            if (!response.isSuccessful) throw IOException("CSRF jetonu alınamadı (${response.code}).")
            val body = response.body?.string() ?: throw IOException("CSRF yanıtı boş.")
            return KitapJson.instance.decodeFromString(CsrfTokenDto.serializer(), body)
        }
    }

    @Synchronized
    private fun invalidate() {
        header = null
        token = null
    }

    private companion object {
        val SAFE_METHODS = setOf("GET", "HEAD", "OPTIONS", "TRACE")
        val SESSION_CHANGING_PATHS = setOf(
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/logout",
        )
    }
}
