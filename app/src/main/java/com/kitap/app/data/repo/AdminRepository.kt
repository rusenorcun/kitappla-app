package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.net.safeUnitCall
import com.kitap.app.data.api.AdminApi
import com.kitap.app.data.dto.AdminReportDto
import com.kitap.app.data.dto.AdminStatsDto
import com.kitap.app.data.dto.ResolveReportBody
import com.kitap.app.data.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(private val api: AdminApi) {
    suspend fun getStats(): ApiResult<AdminStatsDto> = safeApiCall { api.getStats() }

    suspend fun getPendingDocs(): ApiResult<List<UserDto>> = safeApiCall { api.getPendingDocs() }

    suspend fun approveDoc(id: Long): ApiResult<Unit> = safeUnitCall { api.approveDoc(id) }

    suspend fun rejectDoc(id: Long, reason: String? = null): ApiResult<Unit> =
        safeUnitCall { api.rejectDoc(id, reason) }

    suspend fun getAllUsers(query: String? = null): ApiResult<List<UserDto>> =
        safeApiCall { api.getAllUsers(query) }

    suspend fun toggleBlockUser(id: Long): ApiResult<Unit> = safeUnitCall { api.toggleBlockUser(id) }

    suspend fun getReports(all: Boolean = false): ApiResult<List<AdminReportDto>> =
        safeApiCall { api.getReports(all) }

    suspend fun resolveReport(id: Long, actioned: Boolean, adminNote: String? = null): ApiResult<Unit> =
        safeUnitCall { api.resolveReport(id, ResolveReportBody(actioned, adminNote)) }
}
