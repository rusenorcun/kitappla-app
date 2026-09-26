package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.data.api.MessageApi
import com.kitappla.app.data.dto.ChatMessageDto
import com.kitappla.app.data.dto.ConversationDto
import com.kitappla.app.data.dto.SendMessageBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Üye ile yönetim arasındaki destek sohbeti (backend `ConversationKind.SUPPORT`): `refId` her zaman üyenin kimliğidir;
 * üye kendi sohbetini, yönetici herhangi bir üyeninkini açar.
 */
const val SUPPORT_CONVERSATION = "SUPPORT"

@Singleton
class MessageRepository @Inject constructor(private val api: MessageApi) {
    suspend fun conversations(): ApiResult<List<ConversationDto>> = safeApiCall { api.conversations() }

    suspend fun openConversation(kind: String, refId: Long): ApiResult<ConversationDto> =
        safeApiCall { api.openConversation(kind, refId) }

    suspend fun messages(id: Long): ApiResult<List<ChatMessageDto>> = safeApiCall { api.messages(id) }

    suspend fun sendMessage(id: Long, body: String): ApiResult<ChatMessageDto> =
        safeApiCall { api.sendMessage(id, SendMessageBody(body)) }
}
