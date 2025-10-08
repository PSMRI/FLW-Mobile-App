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
import androidx.core.content.FileProvider
import javax.inject.Inject
import com.squareup.moshi.Moshi
import org.piramalswasthya.sakhi.repositories.BenRepo.Companion.getCurrentDate
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.network.GetDataRequest
import org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.convertToLocalDate
import org.piramalswasthya.sakhi.utils.HelperUtil.convertToServerDate
import org.piramalswasthya.sakhi.utils.HelperUtil.copyToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.detectExtAndMime
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileName
import org.piramalswasthya.sakhi.model.MaaMeetingGetAllResponse

class MaaMeetingRepo @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val dao: MaaMeetingDao,
    private val api: AmritApiService,
    private val pref: PreferenceDao,
    private val moshi: Moshi
) {

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
                val name = getFileName(uri,appContext) ?: "upload"
                val mime = appContext.contentResolver.getType(uri) ?: "application/octet-stream"
                val fileForUpload = if (mime.startsWith("image/")) compressImageToTemp(uri, name,appContext) else copyToTemp(uri, name, appContext)
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
                id = item.id?.toLong()!!,
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

    fun getAllMaaMeetings(): Flow<List<MaaMeetingEntity>> = dao.getAllMaaData()

    suspend fun getMaaMeetingById(id: Long): MaaMeetingEntity? {
        return dao.getMaaMeetingById(id)
    }
}


