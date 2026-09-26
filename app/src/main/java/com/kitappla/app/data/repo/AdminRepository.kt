package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeResponse
import com.kitappla.app.core.net.safeUnitCall
import com.kitappla.app.data.api.AdminApi
import com.kitappla.app.data.dto.AdminContentDto
import com.kitappla.app.data.dto.AdminReportDetailDto
import com.kitappla.app.data.dto.AdminReportDto
import com.kitappla.app.data.dto.AdminMonitorDto
import com.kitappla.app.data.dto.AdminStatsDto
import com.kitappla.app.data.dto.ChatMessageDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.dto.PickupPointInputBody
import com.kitappla.app.data.dto.RemoveContentBody
import com.kitappla.app.data.dto.ResolveReportBody
import com.kitappla.app.data.dto.SendMessageBody
import com.kitappla.app.data.dto.SetAdminRoleBody
import com.kitappla.app.data.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

/** İndirilen öğrenci belgesi. */
class DocumentFile(val bytes: ByteArray, val contentType: String?)

@Singleton
class AdminRepository @Inject constructor(private val api: AdminApi) {
    suspend fun getStats(): ApiResult<AdminStatsDto> = safeApiCall { api.getStats() }

    suspend fun getMonitor(): ApiResult<AdminMonitorDto> = safeApiCall { api.getMonitor() }

    suspend fun expireSession(id: String): ApiResult<Unit> = safeUnitCall { api.expireSession(id) }

    /** Belgenin baytları ve içerik türü. Sunucu en çok 5 MB kabul ettiği için bellekte tutulur. */
    suspend fun getDocumentFile(userId: Long): ApiResult<DocumentFile> =
        when (val r = safeResponse { api.getDocumentFile(userId) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                val body = r.value.body()
                if (body == null) {
                    ApiResult.Failure(com.kitappla.app.core.net.ApiMessages.EMPTY_RESPONSE, r.value.code())
                } else {
                    try {
                        val type = r.value.headers()["Content-Type"] ?: body.contentType()?.toString()
                        val bytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { body.use { it.bytes() } }
                        ApiResult.Success(DocumentFile(bytes, type))
                    } catch (e: java.io.IOException) {
                        ApiResult.Failure(com.kitappla.app.core.net.ApiMessages.NETWORK, null, isNetwork = true)
                    }
                }
            }
        }

    // Belgeler
    suspend fun getPendingDocs(): ApiResult<List<UserDto>> = safeApiCall { api.getPendingDocs() }

    suspend fun approveDoc(id: Long): ApiResult<Unit> = safeUnitCall { api.approveDoc(id) }

    suspend fun rejectDoc(id: Long, reason: String? = null): ApiResult<Unit> =
        safeUnitCall { api.rejectDoc(id, reason) }

    // Üyeler
    suspend fun getAllUsers(query: String? = null): ApiResult<List<UserDto>> =
        safeApiCall { api.getAllUsers(query) }

    suspend fun toggleBlockUser(id: Long): ApiResult<Unit> = safeUnitCall { api.toggleBlockUser(id) }

    suspend fun toggleAdminRole(id: Long, admin: Boolean? = null): ApiResult<UserDto> =
        safeApiCall { api.toggleUserAdminRole(id, SetAdminRoleBody(admin)) }

    suspend fun deleteUser(id: Long): ApiResult<Unit> = safeUnitCall { api.deleteUser(id) }

    // Teslim Noktaları
    suspend fun getAllPickupPoints(): ApiResult<List<PickupPointDto>> =
        safeApiCall { api.getAllPickupPoints() }

    suspend fun createPickupPoint(campus: String, name: String, description: String? = null): ApiResult<PickupPointDto> =
        safeApiCall { api.createPickupPoint(PickupPointInputBody(campus, name, description)) }

    suspend fun updatePickupPoint(id: Long, campus: String, name: String, description: String? = null): ApiResult<PickupPointDto> =
        safeApiCall { api.updatePickupPoint(id, PickupPointInputBody(campus, name, description)) }

    suspend fun togglePickupPointActive(id: Long): ApiResult<PickupPointDto> =
        safeApiCall { api.togglePickupPointActive(id) }

    // İçerik Moderasyonu
    suspend fun getContent(): ApiResult<AdminContentDto> = safeApiCall { api.getContent() }

    suspend fun removeDonation(id: Long, reason: String? = null): ApiResult<Unit> =
        safeUnitCall { api.removeDonation(id, RemoveContentBody(reason)) }

    suspend fun removeRequest(id: Long, reason: String? = null): ApiResult<Unit> =
        safeUnitCall { api.removeRequest(id, RemoveContentBody(reason)) }

    suspend fun removeSwapBook(id: Long, reason: String? = null): ApiResult<Unit> =
        safeUnitCall { api.removeSwapBook(id, RemoveContentBody(reason)) }

    // Şikâyetler
    suspend fun getReports(all: Boolean = false): ApiResult<List<AdminReportDto>> =
        safeApiCall { api.getReports(all) }

    suspend fun getReportDetail(id: Long): ApiResult<AdminReportDetailDto> =
        safeApiCall { api.getReportDetail(id) }

    suspend fun sendSupportMessage(id: Long, body: String): ApiResult<ChatMessageDto> =
        safeApiCall { api.sendSupportMessage(id, SendMessageBody(body)) }

    suspend fun resolveReport(id: Long, actioned: Boolean, adminNote: String? = null): ApiResult<Unit> =
        safeUnitCall { api.resolveReport(id, ResolveReportBody(actioned, adminNote)) }
}
