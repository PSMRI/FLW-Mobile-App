package org.piramalswasthya.sakhi.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioUploadService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadAudio(
        uploadUrl: String,
        audioBytes: ByteArray,
        contentType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = audioBytes.toRequestBody(contentType.toMediaType())
            val request = Request.Builder()
                .url(uploadUrl)
                .put(body)
                .build()
            val response = client.newCall(request).execute()
            response.use { it.isSuccessful }
        } catch (e: Exception) {
            Timber.e(e, "Audio upload failed")
            false
        }
    }
}
