package com.kitap.app.data.api

import com.kitap.app.data.dto.NotificationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {
    @GET("api/v1/notifications")
    suspend fun notifications(): Response<NotificationsResponse>

    @POST("api/v1/notifications/read-all")
    suspend fun readAll(): Response<Map<String, Int>>

    @POST("api/v1/notifications/{id}/read")
    suspend fun readOne(@Path("id") id: Long): Response<Unit>
}
