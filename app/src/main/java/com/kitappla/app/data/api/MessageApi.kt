package com.kitappla.app.data.api

import com.kitappla.app.data.dto.ChatMessageDto
import com.kitappla.app.data.dto.ConversationDto
import com.kitappla.app.data.dto.SendMessageBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {
    @GET("api/v1/conversations")
    suspend fun conversations(): Response<List<ConversationDto>>

    @GET("api/v1/conversations/open/{kind}/{refId}")
    suspend fun openConversation(
        @Path("kind") kind: String,
        @Path("refId") refId: Long,
    ): Response<ConversationDto>

    @GET("api/v1/conversations/{id}/messages")
    suspend fun messages(@Path("id") id: Long): Response<List<ChatMessageDto>>

    @POST("api/v1/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") id: Long,
        @Body body: SendMessageBody,
    ): Response<ChatMessageDto>
}
