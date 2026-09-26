package com.kitappla.app.data.repo

import android.content.Context
import android.net.Uri
import com.kitappla.app.core.net.ApiMessages
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeResponse
import com.kitappla.app.core.net.safeUnitCall
import com.kitappla.app.data.api.ProfileApi
import com.kitappla.app.data.dto.MeDto
import com.kitappla.app.data.dto.PasswordChangeBody
import com.kitappla.app.data.dto.ProfileUpdateBody
import com.kitappla.app.data.dto.QuotaDto
import com.kitappla.app.data.dto.StudentConfirmationBody
import com.kitappla.app.data.dto.StudentEmailBody
import com.kitappla.app.data.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/** `/api/v1/me/student` yanıtı: güncel kullanıcı ve e-postanın gerçekten gönderilip gönderilmediği. */
data class StudentVerificationResult(val user: UserDto, val emailSent: Boolean)

@Singleton
class ProfileRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: ProfileApi,
) {
    suspend fun getProfile(): ApiResult<MeDto> = safeApiCall { api.getProfile() }

    suspend fun updateProfile(name: String, address: String?, phone: String?, school: String?): ApiResult<UserDto> =
        safeApiCall { api.updateProfile(ProfileUpdateBody(name, address, phone, school)) }

    /** Başarıda sunucunun "… adresine doğrulama bağlantısı gönderildi." metnini döndürür. */
    suspend fun resendEmailVerification(): ApiResult<String> =
        when (val r = safeApiCall { api.resendEmailVerification() }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> ApiResult.Success(r.value.message)
        }

    suspend fun changePassword(current: String, new: String, confirm: String): ApiResult<Unit> =
        safeUnitCall { api.changePassword(PasswordChangeBody(current, new, confirm)) }

    /**
     * Backend, doğrulama e-postasını gerçekten gönderip göndermediğini gövdede değil
     * `X-Student-Verification-Sent` başlığında bildirir (ör. e-posta servisi kapalıyken ya da aynı
     * hesap art arda çok denediğinde 200 döner ama e-posta atılmaz). Bu bilgiyi kaybetmeden taşımak
     * için sonucu `UserDto` yerine [StudentVerificationResult] olarak döndürür.
     */
    suspend fun verifyStudent(email: String, level: String? = null, school: String? = null): ApiResult<StudentVerificationResult> =
        when (val r = safeResponse { api.verifyStudent(StudentEmailBody(email, level, school)) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                val body = r.value.body()
                if (body == null) {
                    ApiResult.Failure(ApiMessages.EMPTY_RESPONSE, r.value.code())
                } else {
                    val sent = r.value.headers()["X-Student-Verification-Sent"]?.toBooleanStrictOrNull() ?: true
                    ApiResult.Success(StudentVerificationResult(body, sent))
                }
            }
        }

    suspend fun confirmStudent(token: String): ApiResult<UserDto> =
        safeApiCall { api.confirmStudent(StudentConfirmationBody(token)) }

    suspend fun uploadStudentDocument(uri: Uri, schoolLevel: String, documentNo: String): ApiResult<UserDto> {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/pdf"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return ApiResult.Failure("Belge okunamadı.")

            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val extension = if (mimeType.contains("pdf")) "pdf" else "jpg"
            val filePart = MultipartBody.Part.createFormData("document", "belge.$extension", requestFile)
            val levelPart = schoolLevel.toRequestBody("text/plain".toMediaTypeOrNull())
            val noPart = documentNo.toRequestBody("text/plain".toMediaTypeOrNull())

            safeApiCall { api.uploadStudentDocument(levelPart, noPart, filePart) }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "Belge yüklenirken bir hata oluştu.")
        }
    }

    suspend fun getQuota(): ApiResult<QuotaDto> = safeApiCall { api.getQuota() }
}
