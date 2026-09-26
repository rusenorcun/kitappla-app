package com.kitappla.app.di

import android.content.Context
import com.kitappla.app.BuildConfig
import com.kitappla.app.core.net.CookiePersistence
import com.kitappla.app.core.net.CsrfInterceptor
import com.kitappla.app.core.net.EmailNotVerifiedInterceptor
import com.kitappla.app.core.net.EmailVerificationNotice
import com.kitappla.app.core.net.EncryptedCookiePersistence
import com.kitappla.app.core.net.KitapplaJson
import com.kitappla.app.core.net.PersistentCookieJar
import com.kitappla.app.core.net.UnauthorizedInterceptor
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.data.api.AuthApi
import com.kitappla.app.data.api.DonationApi
import com.kitappla.app.data.api.NotificationApi
import com.kitappla.app.data.api.UploadApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun cookiePersistence(@ApplicationContext context: Context): CookiePersistence =
        EncryptedCookiePersistence(context)

    @Provides @Singleton
    fun cookieJar(persistence: CookiePersistence): PersistentCookieJar = PersistentCookieJar(persistence)

    @Provides @Singleton
    fun baseUrl(): HttpUrl = BuildConfig.API_BASE_URL.toHttpUrl()

    @Provides @Singleton
    fun okHttp(
        jar: PersistentCookieJar,
        baseUrl: HttpUrl,
        session: SessionManager,
        emailVerificationNotice: EmailVerificationNotice,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(jar)
            .addInterceptor(CsrfInterceptor(baseUrl))
            .addInterceptor(UnauthorizedInterceptor { session.onUnauthorized() })
            .addInterceptor(EmailNotVerifiedInterceptor { emailVerificationNotice.report() })
            // Bağlantı yoksa/sunucu yanıtsızsa ekranlar sonsuza dek "yükleniyor"da kalmasın. callTimeout, yavaş
            // damlayan yanıtlar dahil tüm çağrıyı sınırlar (giriş gibi iptal edilemez hatların da üst sınırıdır).
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient, baseUrl: HttpUrl): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(KitapplaJson.instance.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton fun authApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun donationApi(retrofit: Retrofit): DonationApi = retrofit.create(DonationApi::class.java)
    @Provides @Singleton fun notificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)
    @Provides @Singleton fun requestApi(retrofit: Retrofit): com.kitappla.app.data.api.RequestApi = retrofit.create(com.kitappla.app.data.api.RequestApi::class.java)
    @Provides @Singleton fun swapApi(retrofit: Retrofit): com.kitappla.app.data.api.SwapApi = retrofit.create(com.kitappla.app.data.api.SwapApi::class.java)
    @Provides @Singleton fun messageApi(retrofit: Retrofit): com.kitappla.app.data.api.MessageApi = retrofit.create(com.kitappla.app.data.api.MessageApi::class.java)
    @Provides @Singleton fun profileApi(retrofit: Retrofit): com.kitappla.app.data.api.ProfileApi = retrofit.create(com.kitappla.app.data.api.ProfileApi::class.java)
    @Provides @Singleton fun reportApi(retrofit: Retrofit): com.kitappla.app.data.api.ReportApi = retrofit.create(com.kitappla.app.data.api.ReportApi::class.java)
    @Provides @Singleton fun pickupPointApi(retrofit: Retrofit): com.kitappla.app.data.api.PickupPointApi = retrofit.create(com.kitappla.app.data.api.PickupPointApi::class.java)
    @Provides @Singleton fun bookApi(retrofit: Retrofit): com.kitappla.app.data.api.BookApi = retrofit.create(com.kitappla.app.data.api.BookApi::class.java)
    @Provides @Singleton fun adminApi(retrofit: Retrofit): com.kitappla.app.data.api.AdminApi = retrofit.create(com.kitappla.app.data.api.AdminApi::class.java)
    @Provides @Singleton fun uploadApi(retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)
}
