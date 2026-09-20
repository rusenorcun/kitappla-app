package com.kitap.app.data.api

import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.PasswordChangeBody
import com.kitap.app.data.dto.ProfileUpdateBody
import com.kitap.app.data.dto.QuotaDto
import com.kitap.app.data.dto.StudentConfirmationBody
import com.kitap.app.data.dto.StudentEmailBody
import com.kitap.app.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ProfileApi {
    @GET("api/v1/me")
    suspend fun getProfile(): Response<MeDto>

    @PUT("api/v1/me")
    suspend fun updateProfile(@Body body: ProfileUpdateBody): Response<UserDto>

    @POST("api/v1/me/password")
    suspend fun changePassword(@Body body: PasswordChangeBody): Response<Unit>

    @POST("api/v1/me/student")
    suspend fun verifyStudent(@Body body: StudentEmailBody): Response<UserDto>

    @POST("api/v1/me/student/confirm")
    suspend fun confirmStudent(@Body body: StudentConfirmationBody): Response<UserDto>

    @GET("api/v1/quota")
    suspend fun getQuota(): Response<QuotaDto>
}
