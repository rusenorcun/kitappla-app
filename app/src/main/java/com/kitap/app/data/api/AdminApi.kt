package com.kitap.app.data.api

import com.kitap.app.data.dto.AdminReportDto
import com.kitap.app.data.dto.AdminStatsDto
import com.kitap.app.data.dto.ResolveReportBody
import com.kitap.app.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApi {
    @GET("api/v1/admin/stats")
    suspend fun getStats(): Response<AdminStatsDto>

    @GET("api/v1/admin/pending-docs")
    suspend fun getPendingDocs(): Response<List<UserDto>>

    @POST("api/v1/admin/docs/{id}/approve")
    suspend fun approveDoc(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/admin/docs/{id}/reject")
    suspend fun rejectDoc(
        @Path("id") id: Long,
        @Query("reason") reason: String? = null,
    ): Response<Unit>

    @GET("api/v1/admin/users")
    suspend fun getAllUsers(@Query("q") query: String? = null): Response<List<UserDto>>

    @POST("api/v1/admin/users/{id}/block")
    suspend fun toggleBlockUser(@Path("id") id: Long): Response<Unit>

    @GET("api/v1/admin/reports")
    suspend fun getReports(@Query("all") all: Boolean = false): Response<List<AdminReportDto>>

    @POST("api/v1/admin/reports/{id}/resolve")
    suspend fun resolveReport(
        @Path("id") id: Long,
        @Body body: ResolveReportBody,
    ): Response<Unit>
}
