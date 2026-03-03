package org.piramalswasthya.sakhi.model.chat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── API Envelope ──

@JsonClass(generateAdapter = true)
data class ChatApiResponse<T>(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: T?,
    @Json(name = "error") val error: ChatApiError? = null
)

@JsonClass(generateAdapter = true)
data class ChatApiError(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String
)

// ── Pagination ──

@JsonClass(generateAdapter = true)
data class Pagination(
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "total") val total: Int
)

// ── Sessions ──

@JsonClass(generateAdapter = true)
data class ChatSession(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "title") val title: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class SessionsData(
    @Json(name = "sessions") val sessions: List<ChatSession>,
    @Json(name = "pagination") val pagination: Pagination
)

@JsonClass(generateAdapter = true)
data class CreateSessionData(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "disclaimer") val disclaimer: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateSessionRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "language") val language: String
)

// ── Messages ──

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "role") val role: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "content_type") val contentType: String? = "text",
    @Json(name = "language") val language: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "metadata") val metadata: MessageMetadata? = null
)

@JsonClass(generateAdapter = true)
data class MessageMetadata(
    @Json(name = "is_emergency_referral") val isEmergencyReferral: Boolean = false,
    @Json(name = "is_escalation") val isEscalation: Boolean = false,
    @Json(name = "confidence") val confidence: String? = null,
    @Json(name = "related_faqs") val relatedFaqs: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class MessagesData(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "pagination") val pagination: Pagination
)

// ── Send Message ──

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "content") val content: String,
    @Json(name = "content_type") val contentType: String = "text",
    @Json(name = "language") val language: String = "hi",
    @Json(name = "audio_ref") val audioRef: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageData(
    @Json(name = "user_message") val userMessage: ChatMessage,
    @Json(name = "assistant_message") val assistantMessage: ChatMessage
)

// ── Audio Upload ──

@JsonClass(generateAdapter = true)
data class AudioUploadUrlRequest(
    @Json(name = "file_name") val fileName: String,
    @Json(name = "content_type") val contentType: String,
    @Json(name = "session_id") val sessionId: String
)

@JsonClass(generateAdapter = true)
data class AudioUploadData(
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "audio_ref") val audioRef: String,
    @Json(name = "expires_in") val expiresIn: Int = 300
)
