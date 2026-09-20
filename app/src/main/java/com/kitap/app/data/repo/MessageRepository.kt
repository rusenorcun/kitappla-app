package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.data.api.MessageApi
import com.kitap.app.data.dto.ChatMessageDto
import com.kitap.app.data.dto.ConversationDto
import com.kitap.app.data.dto.SendMessageBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(private val api: MessageApi) {
    suspend fun conversations(): ApiResult<List<ConversationDto>> = safeApiCall { api.conversations() }

    suspend fun openConversation(kind: String, refId: Long): ApiResult<ConversationDto> =
        safeApiCall { api.openConversation(kind, refId) }

    suspend fun messages(id: Long): ApiResult<List<ChatMessageDto>> = safeApiCall { api.messages(id) }

    suspend fun sendMessage(id: Long, body: String): ApiResult<ChatMessageDto> =
        safeApiCall { api.sendMessage(id, SendMessageBody(body)) }
}
