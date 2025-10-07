package org.piramalswasthya.sakhi.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.MaaMeetingDao
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.content.ContentResolver
import android.provider.OpenableColumns
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.FileOutputStream
import javax.inject.Inject
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.piramalswasthya.sakhi.repositories.BenRepo.Companion.getCurrentDate
import android.util.Base64
import org.piramalswasthya.sakhi.network.GetDataRequest

class MaaMeetingRepo @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val dao: MaaMeetingDao,
    private val api: AmritApiService,
    private val pref: PreferenceDao,
    private val moshi: Moshi
) {

    suspend fun getLatest(): MaaMeetingEntity? = withContext(Dispatchers.IO) { dao.getLatest() }

    fun buildEntity(
        date: String?,
        place: String?,
        participants: Int?,
        u1: String?,
        u2: String?,
        u3: String?,
        u4: String?,
        u5: String?
    ) = MaaMeetingEntity(
        meetingDate = date,
        place = place,
        participants = participants,
        ashaId = pref.getLoggedInUser()?.userId,
        meetingImages = listOfNotNull(u1, u2, u3, u4, u5),
        syncState = SyncState.UNSYNCED
    )

    suspend fun save(entity: MaaMeetingEntity) = withContext(Dispatchers.IO) {
        val id = dao.insert(entity)
        id
    }

    suspend fun tryUpsync() = withContext(Dispatchers.IO) {
        val pending = dao.getBySyncState(SyncState.UNSYNCED)
        if (pending.isEmpty()) return@withContext
        pending.forEach { row ->
            val imagesParts = (row.meetingImages ?: emptyList()).mapNotNull { uriStr ->
                val uri = android.net.Uri.parse(uriStr)
                val name = getFileName(uri) ?: "upload"
                val mime = appContext.contentResolver.getType(uri) ?: "application/octet-stream"
                val fileForUpload = if (mime.startsWith("image/")) compressImageToTemp(uri, name) else copyToTemp(uri, name)
                fileForUpload?.let { file ->
                    val body = file.asRequestBody(mime.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("meetingImages", file.name, body)
                }
            }

            val response = api.postMaaMeetingMultipart(
                meetingDate = (convertToServerDate(row.meetingDate) ?: "").toRequestBody("text/plain".toMediaTypeOrNull()),
                place = (row.place ?: "").toRequestBody("text/plain".toMediaTypeOrNull()),
                participants = ((row.participants ?: 0).toString()).toRequestBody("text/plain".toMediaTypeOrNull()),
                ashaId = ((row.ashaId ?: 0).toString()).toRequestBody("text/plain".toMediaTypeOrNull()),
                createdBy = ((pref.getLoggedInUser()?.userName ?: 0).toString()).toRequestBody("text/plain".toMediaTypeOrNull()),
                meetingImages = imagesParts
            )
            if (response.isSuccessful) {
                dao.updateSyncState(row.id, SyncState.SYNCED)
            }
        }
    }

    suspend fun downSyncAndPersist() = withContext(Dispatchers.IO) {

        val response = api.getMaaMeetings(
            GetDataRequest(
                0,
                getCurrentDate(pref.getLastSyncedTimeStamp()),
                getCurrentDate(),
                0,
                pref.getLoggedInUser()?.userId?.toLong()!!,
                pref.getLoggedInUser()?.userName!!,
                pref.getLoggedInUser()?.userId?.toLong()!!
            )
        )
        if (!response.isSuccessful) return@withContext
        val body = response.body()?.string() ?: return@withContext
        val adapter = moshi.adapter(MaaMeetingGetAllResponse::class.java)
        val parsed = adapter.fromJson(body) ?: return@withContext
        dao.clearAll()
        parsed.data?.forEach { item ->
            val imageBase64List = item.meetingImages ?: emptyList()
            val imageUriList = imageBase64List.mapNotNull { base64 ->
                try {
                    val base64Data = base64.substringAfter(",", base64)
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val (ext, _) = detectExtAndMime(bytes)
                    val file = File(appContext.cacheDir, "meeting_${System.currentTimeMillis()}.$ext")
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
            }

            val entity = MaaMeetingEntity(
                meetingDate = convertToLocalDate(item.meetingDate),
                place = item.place,
                participants = item.participants,
                ashaId = item.ashaId,
                meetingImages = imageUriList,
                syncState = SyncState.SYNCED
            )

            dao.insert(entity)
        }
    }

    private fun detectExtAndMime(bytes: ByteArray): Pair<String, String> {
        if (bytes.size >= 4) {
            if (bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()) {
                return "pdf" to "application/pdf"
            }
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                return "jpg" to "image/jpeg"
            }
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
                return "png" to "image/png"
            }
        }
        return "bin" to "application/octet-stream"
    }

    suspend fun hasMeetingInSameQuarter(meetingDate: String?): Boolean = withContext(Dispatchers.IO) {
        if (meetingDate.isNullOrBlank()) return@withContext false
        val parts = meetingDate.split("-")
        if (parts.size != 3) return@withContext false
        val day = parts[0].toIntOrNull() ?: return@withContext false
        val month = parts[1].toIntOrNull() ?: return@withContext false
        val year = parts[2].toIntOrNull() ?: return@withContext false
        val q = ((month - 1) / 3) + 1
        dao.getAll().any { row ->
            val p = row.meetingDate?.split("-")
            if (p == null || p.size != 3) return@any false
            val m = p[1].toIntOrNull() ?: return@any false
            val y = p[2].toIntOrNull() ?: return@any false
            val rq = ((m - 1) / 3) + 1
            y == year && rq == q
        }
    }

    private fun getFileName(uri: android.net.Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
            }
        } else {
            uri.path?.let { path -> File(path).name }
        }
    }

    private fun copyToTemp(uri: android.net.Uri, nameHint: String): File? {
        return try {
            val suffix = nameHint.substringAfterLast('.', missingDelimiterValue = "")
            val temp = if (suffix.isNotEmpty()) File.createTempFile("maa_upload_", ".${suffix}", appContext.cacheDir) else File.createTempFile("maa_upload_", null, appContext.cacheDir)
            appContext.contentResolver.openInputStream(uri)?.use { ins ->
                FileOutputStream(temp).use { outs -> ins.copyTo(outs) }
            }
            temp
        } catch (_: Exception) { null }
    }

    private fun compressImageToTemp(uri: android.net.Uri, nameHint: String): File? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            val (srcW, srcH) = opts.outWidth to opts.outHeight
            if (srcW <= 0 || srcH <= 0) return copyToTemp(uri, nameHint)
            val maxDim = 1280
            var sample = 1
            while (srcW / sample > maxDim || srcH / sample > maxDim) sample *= 2
            val opts2 = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts2) } ?: return copyToTemp(uri, nameHint)
            val temp = File.createTempFile("maa_img_", ".jpg", appContext.cacheDir)
            FileOutputStream(temp).use { fos -> bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos) }
            temp
        } catch (_: Exception) { null }
    }

    private fun convertToServerDate(local: String?): String? {
        if (local.isNullOrBlank()) return null
        val parts = local.split("-")
        if (parts.size != 3) return local
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }

    private fun convertToLocalDate(server: String?): String? {
        if (server.isNullOrBlank()) return null
        val parts = server.split("-")
        if (parts.size != 3) return server
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }

}

@JsonClass(generateAdapter = true)
data class MaaMeetingGetAllResponse(
    @Json(name = "data") val data: List<MaaMeetingServerItem>?,
    @Json(name = "statusCode") val statusCode: Int?,
    @Json(name = "status") val status: String?
)

@JsonClass(generateAdapter = true)
data class MaaMeetingServerItem(
    @Json(name = "id") val id: Int?,
    @Json(name = "meetingDate") val meetingDate: String?,
    @Json(name = "place") val place: String?,
    @Json(name = "participants") val participants: Int?,
    @Json(name = "ashaId") val ashaId: Int?,
    @Json(name = "meetingImages") val meetingImages: List<String>?
)


