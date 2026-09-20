package com.kitap.app.data.api

import com.kitap.app.data.dto.MyReportDto
import com.kitap.app.data.dto.ReportBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReportApi {
    @GET("api/v1/reports")
    suspend fun myReports(): Response<List<MyReportDto>>

    @POST("api/v1/reports/{kind}/{refId}")
    suspend fun report(
        @Path("kind") kind: String,
        @Path("refId") refId: Long,
        @Body body: ReportBody,
    ): Response<Unit>
}
