package org.piramalswasthya.sakhi.network

import org.piramalswasthya.sakhi.model.chat.AudioUploadData
import org.piramalswasthya.sakhi.model.chat.AudioUploadUrlRequest
import org.piramalswasthya.sakhi.model.chat.ChatApiResponse
import org.piramalswasthya.sakhi.model.chat.CreateSessionData
import org.piramalswasthya.sakhi.model.chat.CreateSessionRequest
import org.piramalswasthya.sakhi.model.chat.MessagesData
import org.piramalswasthya.sakhi.model.chat.SendMessageData
import org.piramalswasthya.sakhi.model.chat.SendMessageRequest
import org.piramalswasthya.sakhi.model.chat.SessionsData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {

    @GET("sessions")
    suspend fun getSessions(
        @Query("user_id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ChatApiResponse<SessionsData>>

    @GET("sessions/{session_id}/messages")
    suspend fun getMessages(
        @Path("session_id") sessionId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ChatApiResponse<MessagesData>>

    @POST("sessions")
    suspend fun createSession(
        @Body request: CreateSessionRequest
    ): Response<ChatApiResponse<CreateSessionData>>

    @POST("sessions/{session_id}/messages")
    suspend fun sendMessage(
        @Path("session_id") sessionId: String,
        @Body request: SendMessageRequest
    ): Response<ChatApiResponse<SendMessageData>>

    @POST("audio/upload-url")
    suspend fun getAudioUploadUrl(
        @Body request: AudioUploadUrlRequest
    ): Response<ChatApiResponse<AudioUploadData>>
}
