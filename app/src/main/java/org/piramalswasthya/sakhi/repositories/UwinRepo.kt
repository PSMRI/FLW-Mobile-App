package org.piramalswasthya.sakhi.repositories

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.OpenableColumns
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.model.UwinGetAllRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import javax.inject.Inject
import android.util.Base64
import androidx.core.content.FileProvider
import org.piramalswasthya.sakhi.model.UwinNetwork
import org.piramalswasthya.sakhi.repositories.BenRepo.Companion.getCurrentDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UwinRepo @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val amritApiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
    private val syncDao: SyncDao,
    private val userRepo: UserRepo,
    private val uwinDao: UwinDao,
    private val moshi: Moshi
) {

    fun getAllLocalRecords() = uwinDao.getAllUwinRecords()

    suspend fun insertLocalRecord(record: UwinCache) = withContext(Dispatchers.IO) {
        uwinDao.insert(record)
    }

    suspend fun getUwinById(id: Int): UwinCache? = uwinDao.getUwinById(id)

    private fun buildMultipartFromUris(network: UwinNetwork): List<MultipartBody.Part> {
        val files = listOfNotNull(network.uploadedFiles1, network.uploadedFiles2)
        val parts = mutableListOf<MultipartBody.Part>()

        files.forEach { uriStr ->
            try {
                val uri = android.net.Uri.parse(uriStr)
                val mime = appContext.contentResolver.getType(uri) ?: "application/octet-stream"
                val fileName = getFileName(uri) ?: "upload"
                val file = if (mime.startsWith("image/")) compressImageToTemp(uri, fileName)
                else copyToTemp(uri, fileName)
                file?.let {
                    val body = it.asRequestBody(mime.toMediaTypeOrNull())
                    parts.add(MultipartBody.Part.createFormData("meetingImages", it.name, body))
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to build multipart from URI: $uriStr")
            }
        }
        return parts
    }

    suspend fun tryUpsync(): Boolean = withContext(Dispatchers.IO) {
        try {
            val unsyncedSessions = uwinDao.getUnsyncedSessions()
            if (unsyncedSessions.isEmpty()) {
                Timber.d("☑️ No unsynced sessions found.")
                return@withContext true
            }

            var allSuccess = true
            for (cache in unsyncedSessions) {
                val success = postUwinSession(cache.asDomainModel())
                if (!success) {
                    allSuccess = false
                    Timber.e("❌ Failed to sync session with id=${cache.id}")
                }
            }

            Timber.d("✅ Upsync completed. Success = $allSuccess")
            allSuccess
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception during upsync.")
            false
        }
    }

    suspend fun postUwinSession(network: UwinNetwork): Boolean = withContext(Dispatchers.IO) {
        val user = preferenceDao.getLoggedInUser() ?: return@withContext false

        val images = buildMultipartFromUris(network)
        val meetingDateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(network.sessionDate))

        val meetingDate = meetingDateFormatted.toRequestBody("text/plain".toMediaTypeOrNull())
        val place = (network.place ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val participants = network.participantsCount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val ashaId = user.userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val createdBy = user.userName.toRequestBody("text/plain".toMediaTypeOrNull())

        Timber.d(
            """
        🛰️ Posting UWIN session
        meetingDate = $meetingDateFormatted
        place = ${network.place}
        participants = ${network.participantsCount}
        ashaId = ${user.userId}
        createdBy = ${user.userName}
        imagesCount = ${images?.size ?: 0}
        """.trimIndent()
        )

        try {
            val response = amritApiService.saveUwinSession(
                meetingDate, place, participants, ashaId, createdBy, images
            )

            Timber.d("🌐 Raw HTTP response code: ${response.code()}")
            Timber.d("🌐 Raw HTTP message: ${response.message()}")

            if (response.isSuccessful) {
                val bodyString = response.body()?.string()
                Timber.d("📦 Raw Response Body: $bodyString")

                val json = try {
                    JSONObject(bodyString ?: "")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Failed to parse JSON response")
                    return@withContext false
                }
                val hasStatusCode = json.has("statusCode")
                val statusCode = json.optInt("statusCode", -1)
                val errorMessage = json.optString("errorMessage", "")
                Timber.d("🧩 Parsed Response → statusCode=$statusCode, errorMessage=$errorMessage")

                if (!hasStatusCode && json.has("id")) {
                    // API returned data directly — treat as success
                    Timber.d("✅ UWIN session saved successfully: id=${json.optInt("id")}")
                    uwinDao.updateSyncState(network.id, SyncState.SYNCED)
                    return@withContext true
                }
                when (statusCode) {
                    200 -> {
                        Timber.d("✅ UWIN saved successfully to server.")
                        uwinDao.updateSyncState(network.id, SyncState.SYNCED)
                        true
                    }
                    5002 -> {
                        Timber.w("🔁 Token expired. Refreshing token and retrying...")
                        if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                            throw SocketTimeoutException("Retry after token refresh")
                        }
                        false
                    }
                    else -> {
                        Timber.e("❌ Server returned error: $errorMessage (statusCode=$statusCode)")
                        false
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("❌ Bad HTTP Response: code=${response.code()}, errorBody=$errorBody")
                false
            }
        } catch (e: SocketTimeoutException) {
            Timber.w("⏳ Timeout — Retrying postUwinSession...")
            postUwinSession(network)
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception posting UWIN session")
            false
        }
    }



    suspend fun downSyncAndPersist() = withContext(Dispatchers.IO) {
        Timber.d("Uwin dowsync called")
        val user = preferenceDao.getLoggedInUser() ?: return@withContext
        val response = amritApiService.getAllUwinSessions(
            UwinGetAllRequest(
                villageID = 0,
                fromDate = getCurrentDate(preferenceDao.getLastSyncedTimeStamp()),
                toDate = getCurrentDate(),
                pageNo = 0,
                userId = user.userId,
                userName = user.userName,
                ashaId = user.userId
            )
        )

        if (!response.isSuccessful) {
            Timber.e("❌ DownSync failed: ${response.errorBody()?.string()}")
            return@withContext
        }

        val body = response.body()?.string() ?: return@withContext
        val adapter = moshi.adapter(UwinGetAllResponse::class.java)
        val parsed = adapter.fromJson(body) ?: return@withContext

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Access entries through the data object
        val entries = parsed.data?.entries ?: emptyList()
        Timber.d("✅ Got ${entries.size} UWIN sessions from server")

        val localList = entries.mapNotNull { item ->
            val imageUriList = item.meetingImages?.mapNotNull { base64 ->
                try {
                    val base64Data = base64.substringAfter(",", base64)
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val (ext, _) = detectExtAndMime(bytes)
                    val file = File(appContext.cacheDir, "uwin_${System.currentTimeMillis()}.$ext")
                    file.outputStream().use { it.write(bytes) }
                    val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.provider", file)
                    uri.toString()
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            val sessionDateMillis = try {
                item.meetingDate?.let { sdf.parse(it)?.time } ?: 0L
            } catch (e: ParseException) {
                0L
            }
            UwinCache(
                id = item.id ?: 0,
                sessionDate = sessionDateMillis,
                place = item.place,
                participantsCount = item.participants ?: 0,
                uploadedFiles1 = imageUriList.getOrNull(0),
                uploadedFiles2 = imageUriList.getOrNull(1),
                createdBy = user.userName,
                updatedBy = user.userName,
                syncState = SyncState.SYNCED
            )
        }

        uwinDao.replaceAll(localList)
        Timber.d("✅ Saved ${localList.size} UWIN sessions locally.")
    }



    private fun getFileName(uri: android.net.Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
            }
        } else uri.path?.let { File(it).name }
    }

    private fun copyToTemp(uri: android.net.Uri, nameHint: String): File? = try {
        val suffix = nameHint.substringAfterLast('.', "")
        val temp = if (suffix.isNotEmpty()) File.createTempFile("uwin_", ".$suffix", appContext.cacheDir)
        else File.createTempFile("uwin_", null, appContext.cacheDir)
        appContext.contentResolver.openInputStream(uri)?.use { ins ->
            FileOutputStream(temp).use { outs -> ins.copyTo(outs) }
        }
        temp
    } catch (_: Exception) {
        null
    }

    private fun compressImageToTemp(uri: android.net.Uri, nameHint: String): File? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }

            val (srcW, srcH) = opts.outWidth to opts.outHeight
            val maxDim = 1280
            var sample = 1
            while (srcW / sample > maxDim || srcH / sample > maxDim) sample *= 2

            val opts2 = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts2)
            } ?: return copyToTemp(uri, nameHint)

            val temp = File.createTempFile("uwin_img_", ".jpg", appContext.cacheDir)
            FileOutputStream(temp).use { fos -> bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos) }
            temp
        } catch (_: Exception) {
            null
        }
    }


    private fun detectExtAndMime(bytes: ByteArray): Pair<String, String> {
        if (bytes.size >= 4) {
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) return "jpg" to "image/jpeg"
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()) return "png" to "image/png"
            if (bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte()) return "pdf" to "application/pdf"
        }
        return "bin" to "application/octet-stream"
    }


    @JsonClass(generateAdapter = true)
    data class UwinGetAllResponse(
        @Json(name = "data") val data: UwinData?,
        @Json(name = "statusCode") val statusCode: Int?,
        @Json(name = "status") val status: String?
    )

    @JsonClass(generateAdapter = true)
    data class UwinData(
        @Json(name = "entries") val entries: List<UwinServerItem>?
    )

    @JsonClass(generateAdapter = true)
    data class UwinServerItem(
        @Json(name = "id") val id: Int?,
        @Json(name = "ashaId") val ashaId: Int?,
        @Json(name = "date") val meetingDate: String?,
        @Json(name = "place") val place: String?,
        @Json(name = "participants") val participants: Int?,
        @Json(name = "attachments") val meetingImages: List<String>?
    )
}
