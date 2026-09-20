package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.net.safeUnitCall
import com.kitap.app.data.api.ReportApi
import com.kitap.app.data.dto.MyReportDto
import com.kitap.app.data.dto.ReportBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(private val api: ReportApi) {
    suspend fun myReports(): ApiResult<List<MyReportDto>> = safeApiCall { api.myReports() }

    suspend fun report(kind: String, refId: Long, reason: String, note: String? = null): ApiResult<Unit> =
        safeUnitCall { api.report(kind, refId, ReportBody(reason, note)) }
}
