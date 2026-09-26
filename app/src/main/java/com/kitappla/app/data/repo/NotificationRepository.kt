package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeResponse
import com.kitappla.app.core.net.safeUnitCall
import com.kitappla.app.data.api.NotificationApi
import com.kitappla.app.data.dto.NotificationsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(private val api: NotificationApi) {
    suspend fun getNotifications(): ApiResult<NotificationsResponse> = safeApiCall { api.notifications() }

    suspend fun markAllRead(): ApiResult<Unit> =
        when (val r = safeResponse { api.readAll() }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> ApiResult.Success(Unit)
        }

    suspend fun markOneRead(id: Long): ApiResult<Unit> = safeUnitCall { api.readOne(id) }
}
