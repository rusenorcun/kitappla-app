package com.kitappla.app.data.api

import com.kitappla.app.data.dto.MeDto
import com.kitappla.app.data.dto.MessageDto
import com.kitappla.app.data.dto.PasswordChangeBody
import com.kitappla.app.data.dto.ProfileUpdateBody
import com.kitappla.app.data.dto.QuotaDto
import com.kitappla.app.data.dto.StudentConfirmationBody
import com.kitappla.app.data.dto.StudentEmailBody
import com.kitappla.app.data.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ProfileApi {
    @GET("api/v1/me")
    suspend fun getProfile(): Response<MeDto>

    @PUT("api/v1/me")
    suspend fun updateProfile(@Body body: ProfileUpdateBody): Response<UserDto>

    /** Hesap e-postası doğrulama bağlantısını yeniden gönderir (202); bağlantı açılınca uygulamaya dönülür. */
    @POST("api/v1/me/email-verification")
    suspend fun resendEmailVerification(): Response<MessageDto>

    @POST("api/v1/me/password")
    suspend fun changePassword(@Body body: PasswordChangeBody): Response<Unit>

    @POST("api/v1/me/student")
    suspend fun verifyStudent(@Body body: StudentEmailBody): Response<UserDto>

    @POST("api/v1/me/student/confirm")
    suspend fun confirmStudent(@Body body: StudentConfirmationBody): Response<UserDto>

    @Multipart
    @POST("api/v1/me/student/document")
    suspend fun uploadStudentDocument(
        @Part("schoolLevel") schoolLevel: RequestBody?,
        @Part("documentNo") documentNo: RequestBody?,
        @Part document: MultipartBody.Part?,
    ): Response<UserDto>

    @GET("api/v1/quota")
    suspend fun getQuota(): Response<QuotaDto>
}
