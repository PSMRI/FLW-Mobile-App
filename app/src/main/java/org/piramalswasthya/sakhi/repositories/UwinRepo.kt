package org.piramalswasthya.sakhi.repositories


import android.content.Context
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
import org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.copyToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.detectExtAndMime
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileName
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
                val fileName = getFileName(uri, appContext) ?: "upload"
                val file =
                    if (mime.startsWith("image/")) compressImageToTemp(uri, fileName, appContext)
                    else copyToTemp(uri, fileName, appContext)
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
        val participants =
            network.participantsCount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val ashaId = user.userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val createdBy = user.userName.toRequestBody("text/plain".toMediaTypeOrNull())


        try {
            val response = amritApiService.saveUwinSession(
                meetingDate, place, participants, ashaId, createdBy, images
            )


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

        val entries = parsed.data?.entries ?: emptyList()


        val localList = entries.mapNotNull { item ->
            val imageUriList = item.meetingImages?.mapNotNull { base64 ->
                try {
                    val base64Data = base64.substringAfter(",", base64)
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val (ext, _) = detectExtAndMime(bytes)
                    val file = File(appContext.cacheDir, "uwin_${System.currentTimeMillis()}.$ext")
                    file.outputStream().use { it.write(bytes) }
                    val uri = FileProvider.getUriForFile(
                        appContext,
                        "${appContext.packageName}.provider",
                        file
                    )
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
