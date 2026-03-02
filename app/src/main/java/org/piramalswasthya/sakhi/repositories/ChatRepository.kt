package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.model.chat.AudioUploadUrlRequest
import org.piramalswasthya.sakhi.model.chat.CreateSessionData
import org.piramalswasthya.sakhi.model.chat.CreateSessionRequest
import org.piramalswasthya.sakhi.model.chat.MessagesData
import org.piramalswasthya.sakhi.model.chat.SendMessageData
import org.piramalswasthya.sakhi.model.chat.SendMessageRequest
import org.piramalswasthya.sakhi.model.chat.SessionsData
import org.piramalswasthya.sakhi.network.AudioUploadService
import org.piramalswasthya.sakhi.network.ChatApiService
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val audioUploadService: AudioUploadService,
    private val preferenceDao: PreferenceDao
) {

    private fun currentLanguage(): String = preferenceDao.getCurrentLanguage().symbol

    suspend fun getSessions(page: Int = 1): NetworkResponse<SessionsData> =
        withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.getSessions(page = page)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success" && body.data != null) {
                        NetworkResponse.Success(body.data)
                    } else {
                        NetworkResponse.Error(body?.error?.message ?: "Failed to load sessions")
                    }
                } else {
                    NetworkResponse.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }

    suspend fun getMessages(
        sessionId: String,
        page: Int = 1
    ): NetworkResponse<MessagesData> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.getMessages(sessionId, page = page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    NetworkResponse.Success(body.data)
                } else {
                    NetworkResponse.Error(body?.error?.message ?: "Failed to load messages")
                }
            } else {
                NetworkResponse.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun createSession(language: String? = null): NetworkResponse<CreateSessionData> =
        withContext(Dispatchers.IO) {
            try {
                val lang = language ?: currentLanguage()
                val response = chatApiService.createSession(CreateSessionRequest(lang))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success" && body.data != null) {
                        NetworkResponse.Success(body.data)
                    } else {
                        NetworkResponse.Error(
                            body?.error?.message ?: "Failed to create session"
                        )
                    }
                } else {
                    NetworkResponse.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }

    suspend fun sendMessage(
        sessionId: String,
        content: String,
        contentType: String = "text",
        language: String? = null,
        audioRef: String? = null
    ): NetworkResponse<SendMessageData> = withContext(Dispatchers.IO) {
        try {
            val lang = language ?: currentLanguage()
            val request = SendMessageRequest(
                content = content,
                contentType = contentType,
                language = lang,
                audioRef = audioRef
            )
            val response = chatApiService.sendMessage(sessionId, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    NetworkResponse.Success(body.data)
                } else {
                    NetworkResponse.Error(body?.error?.message ?: "Failed to send message")
                }
            } else {
                NetworkResponse.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun sendAudioMessage(
        sessionId: String,
        audioBytes: ByteArray,
        fileName: String,
        audioContentType: String = "audio/mp4",
        language: String? = null
    ): NetworkResponse<SendMessageData> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Get signed upload URL
            val urlRequest = AudioUploadUrlRequest(
                fileName = fileName,
                contentType = audioContentType,
                sessionId = sessionId
            )
            val urlResponse = chatApiService.getAudioUploadUrl(urlRequest)
            if (!urlResponse.isSuccessful || urlResponse.body()?.data == null) {
                return@withContext NetworkResponse.Error("Failed to get upload URL")
            }
            val uploadData = urlResponse.body()!!.data!!

            // Step 2: Upload audio to signed URL
            val uploaded = audioUploadService.uploadAudio(
                uploadUrl = uploadData.uploadUrl,
                audioBytes = audioBytes,
                contentType = audioContentType
            )
            if (!uploaded) {
                return@withContext NetworkResponse.Error("Audio upload failed")
            }

            // Step 3: Send message with audio_ref
            sendMessage(
                sessionId = sessionId,
                content = "",
                contentType = "audio_transcript",
                language = language,
                audioRef = uploadData.audioRef
            )
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun <T> handleException(e: Exception): NetworkResponse<T> {
        Timber.e(e, "ChatRepository error")
        return when (e) {
            is SocketTimeoutException -> NetworkResponse.Error("Server timed out!")
            is ConnectException -> NetworkResponse.Error("Unable to connect to chat server!")
            is UnknownHostException -> NetworkResponse.Error("No internet connection!")
            is HttpException -> NetworkResponse.Error("Server error: ${e.code()}")
            else -> NetworkResponse.Error(e.message ?: "Something went wrong")
        }
    }
}
