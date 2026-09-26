package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeResponse
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.api.AuthApi
import com.kitappla.app.data.dto.LoginRequest
import com.kitappla.app.data.dto.RegisterRequest
import com.kitappla.app.data.dto.UserDto
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

enum class LoginMode { MEMBER, ADMIN }

const val LOGIN_FAILED_MESSAGE = "E-posta ya da şifre hatalı."

/**
 * Kayıt sonucu. [verificationEmailSent]: sunucu hesap doğrulama e-postasını gerçekten gönderdi mi (backend
 * `X-Email-Verification-Sent` başlığı; posta kapalıysa ya da saatlik sınır dolduysa `false`). Başlığı göndermeyen
 * eski sunucuda `true` sayılır.
 */
data class Registration(val user: UserDto, val verificationEmailSent: Boolean)
const val RESTORE_TIMEOUT_MILLIS = 10_000L

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val session: SessionManager,
) {
    /**
     * Uygulama açılışında kalıcı çerezle oturumu çözer. Herhangi bir hatada misafir görünümüne düşer.
     * Oturum zaten çözülmüşse (süreç canlıyken Activity yeniden açıldı) hiçbir şey yapmaz; aksi hâlde geçici bir
     * ağ hatası canlı Üye/Yönetici oturumunu geçerli çerezler dururken misafire düşürürdü.
     * Sunucu [timeoutMillis] içinde yanıt vermezse açılış ekranı sonsuza dek dönmesin diye misafir görünümüne
     * geçilir; çerezler korunur, bir sonraki açılışta oturum yeniden çözülür.
     */
    suspend fun restore(timeoutMillis: Long = RESTORE_TIMEOUT_MILLIS) {
        if (session.state.value !is SessionState.Loading) return
        when (val r = withTimeoutOrNull(timeoutMillis) { safeApiCall { api.me() } }) {
            is ApiResult.Success -> session.signedIn(r.value.user)
            is ApiResult.Failure, null -> session.markGuest()
        }
    }

    /**
     * Giriş/kayıt/çıkış hattı iptal edilemez: çerez, yanıt başlıkları okunur okunmaz kalıcı depoya yazılır; çağıran
     * korutin (ör. Giriş ekranından geri) araya girip iptal olursa reddedilmiş ya da yönetici çerezi oturum durumu
     * Misafir iken diskte kalır ve bir sonraki soğuk açılışta kapısız oturum açardı. Ağ süreleri (bağlanma 10 sn,
     * okuma 20 sn, toplam çağrı 25 sn — bkz. NetworkModule) iptal edilemez süreyi sınırlar.
     */
    suspend fun login(email: String, password: String, mode: LoginMode): ApiResult<UserDto> =
        withContext(NonCancellable) {
            when (val r = safeApiCall { api.login(LoginRequest(email.trim(), password)) }) {
                is ApiResult.Failure -> r
                is ApiResult.Success -> admitOrReject(r.value.user, mode)
            }
        }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        school: String? = null,
        phone: String? = null,
    ): ApiResult<Registration> =
        withContext(NonCancellable) {
            when (val r = safeResponse {
                api.register(
                    RegisterRequest(
                        name = name.trim(),
                        email = email.trim(),
                        password = password,
                        school = school?.trim()?.ifBlank { null },
                        phone = phone?.trim()?.ifBlank { null },
                    )
                )
            }) {
                is ApiResult.Failure -> r
                is ApiResult.Success -> {
                    val body = r.value.body()
                    if (body == null) {
                        ApiResult.Failure(com.kitappla.app.core.net.ApiMessages.EMPTY_RESPONSE, r.value.code())
                    } else {
                        val sent = r.value.headers()["X-Email-Verification-Sent"]?.toBooleanStrictOrNull() ?: true
                        when (val admitted = admitOrReject(body.user, LoginMode.MEMBER)) {
                            is ApiResult.Failure -> admitted
                            is ApiResult.Success -> ApiResult.Success(Registration(admitted.value, sent))
                        }
                    }
                }
            }
        }

    /**
     * Sunucudaki güncel kullanıcıyı okuyup açık üye oturumuna yazar (bkz. [SessionManager.updateUser]). E-posta
     * bağlantısı tarayıcıda onaylanıp uygulamaya dönülünce doğrulama durumunu tazelemek için kullanılır.
     */
    suspend fun refreshUser(): ApiResult<UserDto> =
        when (val r = safeApiCall { api.me() }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                session.updateUser(r.value.user)
                ApiResult.Success(r.value.user)
            }
        }

    /**
     * E-postadaki doğrulama bağlantısı uygulamada açıldığında (App Link) jetonu sunucuya onaylatır; oturum
     * gerektirmez. `true`: bağlantı geçerliydi (hesap doğrulandı ya da zaten doğrulanmıştı).
     */
    suspend fun confirmEmail(token: String): ApiResult<Boolean> =
        when (val r = safeApiCall { api.verifyEmail(com.kitappla.app.data.dto.VerifyEmailBody(token)) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> ApiResult.Success(r.value.linkValid)
        }

    suspend fun forgotPassword(email: String): ApiResult<String> =
        when (val r = safeApiCall { api.forgotPassword(com.kitappla.app.data.dto.ForgotPasswordBody(email.trim())) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> ApiResult.Success(r.value.message)
        }

    suspend fun resetPassword(token: String, newPassword: String, confirmPassword: String): ApiResult<Unit> =
        com.kitappla.app.core.net.safeUnitCall {
            api.resetPassword(com.kitappla.app.data.dto.ResetPasswordBody(token.trim(), newPassword, confirmPassword))
        }

    suspend fun logout() {
        withContext(NonCancellable) { closeServerSessionThenSignOut() }
    }

    /**
     * Rol/kapı uyumsuzsa sunucudaki oturumu hemen kapatır. Hata metni genel tutulur: hesabın
     * varlığı ya da rolü ifşa edilmez.
     */
    private suspend fun admitOrReject(user: UserDto, mode: LoginMode): ApiResult<UserDto> {
        val allowed = (mode == LoginMode.ADMIN) == user.admin
        if (!allowed) {
            closeServerSessionThenSignOut()
            return ApiResult.Failure(LOGIN_FAILED_MESSAGE, 400)
        }
        session.signedIn(user)
        return ApiResult.Success(user)
    }

    /** Sunucu oturumunu kapatmayı dener (204 gövdesizdir: safeResponse); ne olursa olsun yerel oturum kapanır. */
    private suspend fun closeServerSessionThenSignOut() {
        try {
            safeResponse { api.logout() }
        } finally {
            session.signedOut()
        }
    }
}
