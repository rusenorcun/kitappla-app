package com.kitappla.app.data.api

import com.kitappla.app.data.dto.AdminContentDto
import com.kitappla.app.data.dto.AdminMonitorDto
import com.kitappla.app.data.dto.AdminReportDetailDto
import com.kitappla.app.data.dto.AdminReportDto
import com.kitappla.app.data.dto.AdminStatsDto
import com.kitappla.app.data.dto.ChatMessageDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.dto.PickupPointInputBody
import com.kitappla.app.data.dto.RemoveContentBody
import com.kitappla.app.data.dto.ResolveReportBody
import com.kitappla.app.data.dto.SendMessageBody
import com.kitappla.app.data.dto.SetAdminRoleBody
import com.kitappla.app.data.dto.UserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface AdminApi {
    @GET("api/v1/admin/stats")
    suspend fun getStats(): Response<AdminStatsDto>

    /** Canlı oturumlar + sunucu çalışma parametreleri (web: /admin/izlec). */
    @GET("api/v1/admin/monitor")
    suspend fun getMonitor(): Response<AdminMonitorDto>

    /** 204; kendi oturumu 400, bilinmeyen tanıtıcı 404 (`{"error": …}`). */
    @POST("api/v1/admin/monitor/sessions/{id}/expire")
    suspend fun expireSession(@Path("id") id: String): Response<Unit>

    // Belgeler
    @GET("api/v1/admin/pending-docs")
    suspend fun getPendingDocs(): Response<List<UserDto>>

    /** Öğrenci belgesinin kendisi (pdf/jpeg/png/metin, en çok 5 MB); yöneticinin oturumuyla indirilir. */
    @Streaming
    @GET("api/v1/admin/docs/{userId}/file")
    suspend fun getDocumentFile(@Path("userId") userId: Long): Response<ResponseBody>

    @POST("api/v1/admin/docs/{id}/approve")
    suspend fun approveDoc(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/admin/docs/{id}/reject")
    suspend fun rejectDoc(
        @Path("id") id: Long,
        @Query("reason") reason: String? = null,
    ): Response<Unit>

    // Üyeler
    @GET("api/v1/admin/users")
    suspend fun getAllUsers(@Query("q") query: String? = null): Response<List<UserDto>>

    @POST("api/v1/admin/users/{id}/block")
    suspend fun toggleBlockUser(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/admin/users/{id}/role")
    suspend fun toggleUserAdminRole(
        @Path("id") id: Long,
        @Body body: SetAdminRoleBody? = null,
    ): Response<UserDto>

    @DELETE("api/v1/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>

    // Teslim Noktaları
    @GET("api/v1/admin/pickup-points")
    suspend fun getAllPickupPoints(): Response<List<PickupPointDto>>

    @POST("api/v1/admin/pickup-points")
    suspend fun createPickupPoint(@Body body: PickupPointInputBody): Response<PickupPointDto>

    @PUT("api/v1/admin/pickup-points/{id}")
    suspend fun updatePickupPoint(
        @Path("id") id: Long,
        @Body body: PickupPointInputBody,
    ): Response<PickupPointDto>

    @POST("api/v1/admin/pickup-points/{id}/toggle-active")
    suspend fun togglePickupPointActive(@Path("id") id: Long): Response<PickupPointDto>

    // İçerik Moderasyonu
    @GET("api/v1/admin/content")
    suspend fun getContent(): Response<AdminContentDto>

    @POST("api/v1/admin/content/donations/{id}/remove")
    suspend fun removeDonation(
        @Path("id") id: Long,
        @Body body: RemoveContentBody? = null,
    ): Response<Unit>

    @POST("api/v1/admin/content/requests/{id}/remove")
    suspend fun removeRequest(
        @Path("id") id: Long,
        @Body body: RemoveContentBody? = null,
    ): Response<Unit>

    @POST("api/v1/admin/content/swaps/{id}/remove")
    suspend fun removeSwapBook(
        @Path("id") id: Long,
        @Body body: RemoveContentBody? = null,
    ): Response<Unit>

    // Şikâyetler
    @GET("api/v1/admin/reports")
    suspend fun getReports(@Query("all") all: Boolean = false): Response<List<AdminReportDto>>

    @GET("api/v1/admin/reports/{id}")
    suspend fun getReportDetail(@Path("id") id: Long): Response<AdminReportDetailDto>

    @POST("api/v1/admin/reports/{id}/messages")
    suspend fun sendSupportMessage(
        @Path("id") id: Long,
        @Body body: SendMessageBody,
    ): Response<ChatMessageDto>

    @POST("api/v1/admin/reports/{id}/resolve")
    suspend fun resolveReport(
        @Path("id") id: Long,
        @Body body: ResolveReportBody,
    ): Response<Unit>
}
