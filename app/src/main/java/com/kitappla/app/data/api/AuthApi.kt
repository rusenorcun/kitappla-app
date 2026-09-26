package com.kitappla.app.data.api

import com.kitappla.app.data.dto.EmailVerificationDto
import com.kitappla.app.data.dto.ForgotPasswordBody
import com.kitappla.app.data.dto.LoginRequest
import com.kitappla.app.data.dto.MeDto
import com.kitappla.app.data.dto.MessageDto
import com.kitappla.app.data.dto.RegisterRequest
import com.kitappla.app.data.dto.ResetPasswordBody
import com.kitappla.app.data.dto.VerifyEmailBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<MeDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<MeDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/v1/me")
    suspend fun me(): Response<MeDto>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordBody): Response<MessageDto>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordBody): Response<Unit>

    @POST("api/v1/auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailBody): Response<EmailVerificationDto>
}
