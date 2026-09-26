package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeUnitCall
import com.kitappla.app.data.api.ReportApi
import com.kitappla.app.data.dto.MyReportDto
import com.kitappla.app.data.dto.ReportBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(private val api: ReportApi) {
    suspend fun myReports(): ApiResult<List<MyReportDto>> = safeApiCall { api.myReports() }

    suspend fun report(kind: String, refId: Long, reason: String, note: String? = null): ApiResult<Unit> =
        safeUnitCall { api.report(kind, refId, ReportBody(reason, note)) }
}
