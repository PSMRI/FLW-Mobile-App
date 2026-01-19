package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.network.AHDDTO
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.DewormingDTO
import org.piramalswasthya.sakhi.network.GetVHNDRequest
import org.piramalswasthya.sakhi.network.PHCReviewDTO
import org.piramalswasthya.sakhi.network.UserDataDTO
import org.piramalswasthya.sakhi.network.VHNCDTO
import org.piramalswasthya.sakhi.network.VHNDDTO
import org.piramalswasthya.sakhi.ui.getFileName
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp
import timber.log.Timber
import java.io.File
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


class VLFRepo @Inject constructor(
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService,
    private val vlfDao: VLFDao,
    @ApplicationContext private val appContext: Context

) {

    suspend fun getVHND(id: Int): VHNDCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getVHND(id)
        }
    }

    suspend fun saveRecord(vhndCache: VHNDCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(vhndCache)
        }
    }

    var vhndList = vlfDao.getAllVHND()
        .map { list -> list.map { it.toVhndDTODTO() } }

    suspend fun getVHNC(id: Int): VHNCCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getVHNC(id)
        }
    }

    suspend fun saveRecord(vhncCache: VHNCCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(vhncCache)
        }
    }

    var vhncList = vlfDao.getAllVHNC()
        .map { list -> list.map { it.toVhncDTODTO() } }


    suspend fun getPHC(id: Int): PHCReviewMeetingCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getPHC(id)
        }
    }

    suspend fun saveRecord(phcCache: PHCReviewMeetingCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(phcCache)
        }
    }

    var phcList = vlfDao.getAllPHC()
        .map { list -> list.map { it.toPHCDTODTO() } }


    suspend fun getAHD(id: Int): AHDCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getAHD(id)
        }
    }

    suspend fun saveAHDRecord(ahdCache: AHDCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(ahdCache)
        }
    }

    var ahdList = vlfDao.getAllAHD()
        .map { list -> list.map { it.toAHDCache() } }


    suspend fun getDeworming(id: Int): DewormingCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getDeworming(id)
        }
    }

    suspend fun saveDeworming(dewormingCache: DewormingCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(dewormingCache)
        }
    }

    var dewormingList = vlfDao.getAllDeworming()
//        .map { list -> list.map { it.toDTO() } }
        .map { list -> list.map { it.toDewormingCache() } }

    suspend fun getPulsePolioCampaign(id: Int): PulsePolioCampaignCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getPulsePolioCampaign(id)
        }
    }

    suspend fun savePulsePolioCampaign(pulsePolioCampaignCache: PulsePolioCampaignCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(pulsePolioCampaignCache)
        }
    }

    var pulsePolioCampaignList = vlfDao.getAllPulsePolioCampaign()
        .map { list -> list }

    suspend fun getUnsyncedPulsePolioCampaign(): List<PulsePolioCampaignCache> {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getPulsePolioCampaign(SyncState.UNSYNCED) ?: emptyList()
        }
    }

    suspend fun getORSCampaign(id: Int): ORSCampaignCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getORSCampaign(id)
        }
    }

    suspend fun saveORSCampaign(orsCampaignCache: ORSCampaignCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(orsCampaignCache)
        }
    }

    var orsCampaignList = vlfDao.getAllORSCampaign()
        .map { list -> list }

    suspend fun getUnsyncedORSCampaign(): List<ORSCampaignCache> {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getORSCampaign(SyncState.UNSYNCED) ?: emptyList()
        }
    }

    suspend fun saveORSCampaignToServer(orsCampaignCache: ORSCampaignCache): Boolean {
        return withContext(Dispatchers.IO) {
            try {

                val formDataJson = orsCampaignCache.formDataJson ?: ""
                val formDataObj = try {
                    JSONObject(formDataJson)
                } catch (e: Exception) {
                    JSONObject()
                }
                
                val fieldsObj = formDataObj.optJSONObject("fields") ?: JSONObject()
                val campaignPhotosValue = fieldsObj.opt("campaign_photos") ?: fieldsObj.opt("campaignPhotos")
                
                val photoUris = when {
                    campaignPhotosValue is String -> {
                        try {
                            Gson().fromJson(campaignPhotosValue as String, Array<String>::class.java).toList()
                        } catch (e: Exception) {
                            listOf(campaignPhotosValue.toString()).filter { it.isNotEmpty() }
                        }
                    }
                    campaignPhotosValue is org.json.JSONArray -> {
                        (0 until (campaignPhotosValue as org.json.JSONArray).length())
                            .mapNotNull { campaignPhotosValue.opt(it)?.toString() }
                    }
                    else -> emptyList()
                }

                val multipartParts = mutableListOf<MultipartBody.Part>()
                
                val formDataJsonBody = formDataJson.toRequestBody("application/json".toMediaTypeOrNull())
                multipartParts.add(
                    MultipartBody.Part.createFormData("formDataJson", null, formDataJsonBody)
                )

                val imageParts = photoUris.mapNotNull { photoData ->
                    try {
                        val file: File? = when {
                            photoData.startsWith("data:image/") || photoData.contains(",") -> {
                                val base64Data = photoData.substringAfter(",", photoData)
                                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                val tempFile = File.createTempFile("campaign_photo_", ".jpg", appContext.cacheDir)
                                tempFile.writeBytes(bytes)
                                tempFile
                            }
                            photoData.startsWith("content://") || photoData.startsWith("file://") -> {
                                val uri = android.net.Uri.parse(photoData)
                                val name = getFileName(uri, appContext) ?: "campaign_photo"
                                val mime = appContext.contentResolver.getType(uri) ?: "image/jpeg"
                                if (mime.startsWith("image/")) {
                                    compressImageToTemp(uri, name, appContext)
                                } else {
                                    null
                                }
                            }
                            else -> {
                                val filePath = File(photoData)
                                if (filePath.exists()) filePath else null
                            }
                        }
                        file?.let {
                            val mime = "image/jpeg"
                            val body = it.asRequestBody(mime.toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("campaignPhotos", it.name, body)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing image: $photoData")
                        null
                    }
                }
                multipartParts.addAll(imageParts)

                val response = tmcNetworkApiService.saveORSCampaignData(
                    campaignData = multipartParts
                )

                if (response.isSuccessful) {
                    orsCampaignCache.syncState = SyncState.SYNCED
                    database.vlfDao.saveRecord(orsCampaignCache)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving ORS Campaign to server")
                false
            }
        }
    }

    suspend fun getORSCampaignFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getORSCampaignData()
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    when (val dataValue = jsonObj.opt("data")) {
                                        is org.json.JSONArray -> {
                                            saveORSCampaignFromServer(dataValue.toString())
                                            return@withContext 1
                                        }

                                        is String -> {
                                            saveORSCampaignFromServer(dataValue)
                                            return@withContext 1
                                        }

                                        is JSONObject -> {
                                            saveORSCampaignFromServer(dataValue.toString())
                                            return@withContext 1
                                        }

                                        else -> {
                                            Timber.e("Unexpected data format: ${dataValue?.javaClass}")
                                            return@withContext 0
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "ORS Campaign entries not synced")
                                    return@withContext 0
                                }
                            }
                            5002 -> {
                                if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                    throw SocketTimeoutException("Refreshed Token!")
                                } else {
                                    throw IllegalStateException("User Logged out!!")
                                }
                            }
                            else -> {
                                throw IllegalStateException("$responseStatusCode received")
                            }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("get ORS Campaign data error : $e")
                return@withContext getORSCampaignFromServer()
            } catch (e: IllegalStateException) {
                Timber.d("get ORS Campaign data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveORSCampaignFromServer(dataObj: String) {
        try {
            val jsonArray = org.json.JSONArray(dataObj)
            
            var savedCount = 0
            for (i in 0 until jsonArray.length()) {
                try {
                    val entryObj = jsonArray.getJSONObject(i)
                    val id = entryObj.optInt("id", 0)
                    
                    val fieldsObj = entryObj.optJSONObject("fields")
                    val formDataJson = if (fieldsObj != null) {
                        val formData = org.json.JSONObject()
                        formData.put("fields", fieldsObj)
                        formData.toString()
                    } else {
                        entryObj.optString("formDataJson", null)
                    }
                    
                    if (formDataJson != null && id > 0) {
                        val existing = database.vlfDao.getORSCampaign(id)
                        if (existing == null) {
                            val cache = ORSCampaignCache(
                                id = id,
                                formDataJson = formDataJson,
                                syncState = SyncState.SYNCED
                            )
                            database.vlfDao.saveRecord(cache)
                            savedCount++
                            Timber.d("Saved new ORS Campaign entry with id: $id")
                        } else {
                            // Update existing record if needed
                            existing.formDataJson = formDataJson
                            existing.syncState = SyncState.SYNCED
                            database.vlfDao.saveRecord(existing)
                            savedCount++
                            Timber.d("Updated ORS Campaign entry with id: $id")
                        }
                    } else {
                        Timber.w("Skipping entry with id: $id, formDataJson is null or id is 0")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "cannot save ORS Campaign entry at index $i due to : $e")
                }
            }
            Timber.d("Saved $savedCount ORS Campaign entries to database")
        } catch (e: Exception) {
            Timber.e(e, "Error parsing ORS Campaign data: $e")
        }
    }

    suspend fun savePulsePolioCampaignToServer(pulsePolioCampaignCache: PulsePolioCampaignCache): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val formDataJson = pulsePolioCampaignCache.formDataJson ?: ""
                val formDataObj = try {
                    JSONObject(formDataJson)
                } catch (e: Exception) {
                    JSONObject()
                }
                
                val fieldsObj = formDataObj.optJSONObject("fields") ?: JSONObject()
                val campaignPhotosValue = fieldsObj.opt("campaign_photos") ?: fieldsObj.opt("campaignPhotos")
                
                val photoUris = when {
                    campaignPhotosValue is String -> {
                        try {
                            Gson().fromJson(campaignPhotosValue as String, Array<String>::class.java).toList()
                        } catch (e: Exception) {
                            listOf(campaignPhotosValue.toString()).filter { it.isNotEmpty() }
                        }
                    }
                    campaignPhotosValue is org.json.JSONArray -> {
                        (0 until (campaignPhotosValue as org.json.JSONArray).length())
                            .mapNotNull { campaignPhotosValue.opt(it)?.toString() }
                    }
                    else -> emptyList()
                }

                val imageParts = photoUris.mapNotNull { photoData ->
                    try {
                        val file: File? = when {
                            photoData.startsWith("data:image/") || photoData.contains(",") -> {
                                val base64Data = photoData.substringAfter(",", photoData)
                                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                val tempFile = File.createTempFile("campaign_photo_", ".jpg", appContext.cacheDir)
                                tempFile.writeBytes(bytes)
                                tempFile
                            }
                            photoData.startsWith("content://") || photoData.startsWith("file://") -> {
                                val uri = android.net.Uri.parse(photoData)
                                val name = getFileName(uri, appContext) ?: "campaign_photo"
                                val mime = appContext.contentResolver.getType(uri) ?: "image/jpeg"
                                if (mime.startsWith("image/")) {
                                    compressImageToTemp(uri, name, appContext)
                                } else {
                                    null
                                }
                            }

                            else -> {
                                val filePath = File(photoData)
                                if (filePath.exists()) filePath else null
                            }
                        }
                        file?.let {
                            val mime = "image/jpeg"
                            val body = it.asRequestBody(mime.toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("campaignPhotos", it.name, body)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing image: $photoData")
                        null
                    }
                }

                val multipartParts = mutableListOf<MultipartBody.Part>()
                
                val formDataJsonBody = formDataJson.toRequestBody("application/json".toMediaTypeOrNull())
                multipartParts.add(
                    MultipartBody.Part.createFormData("formDataJson", null, formDataJsonBody)
                )
                
                multipartParts.addAll(imageParts)

                val response = tmcNetworkApiService.savePulsePolioCampaignData(
                    campaignData = multipartParts
                )

                if (response.isSuccessful) {
                    pulsePolioCampaignCache.syncState = SyncState.SYNCED
                    database.vlfDao.saveRecord(pulsePolioCampaignCache)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving Pulse Polio Campaign to server")
                false
            }
        }
    }

    suspend fun getAllPulsePolioCampaigns(): List<PulsePolioCampaignCache> {
        return database.vlfDao.getAllPulsePolioCampaigns()
    }

    suspend fun getAllORSCampaigns(): List<ORSCampaignCache> {
        return database.vlfDao.getAllORSCampaigns()
    }


    suspend fun getPulsePolioCampaignFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getPulsePolioCampaignData()
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull Pulse Polio Campaign data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    when (val dataValue = jsonObj.opt("data")) {
                                        is org.json.JSONArray -> {
                                            savePulsePolioCampaignFromServer(dataValue.toString())
                                            return@withContext 1
                                        }

                                        is String -> {
                                            savePulsePolioCampaignFromServer(dataValue)
                                            return@withContext 1
                                        }

                                        is JSONObject -> {
                                            savePulsePolioCampaignFromServer(dataValue.toString())
                                            return@withContext 1
                                        }

                                        else -> {
                                            Timber.e("Unexpected data format: ${dataValue?.javaClass}")
                                            return@withContext 0
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Pulse Polio Campaign entries not synced")
                                    return@withContext 0
                                }
                            }
                            5002 -> {
                                if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                    throw SocketTimeoutException("Refreshed Token!")
                                } else {
                                    throw IllegalStateException("User Logged out!!")
                                }
                            }
                            else -> {
                                throw IllegalStateException("$responseStatusCode received")
                            }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("get Pulse Polio Campaign data error : $e")
                return@withContext getPulsePolioCampaignFromServer()
            } catch (e: IllegalStateException) {
                Timber.d("get Pulse Polio Campaign data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun savePulsePolioCampaignFromServer(dataObj: String) {
        try {
            val jsonArray = org.json.JSONArray(dataObj)
            
            var savedCount = 0
            for (i in 0 until jsonArray.length()) {
                try {
                    val entryObj = jsonArray.getJSONObject(i)
                    val id = entryObj.optInt("id", 0)
                    
                    val fieldsObj = entryObj.optJSONObject("fields")
                    val formDataJson = if (fieldsObj != null) {
                        val formData = JSONObject()
                        formData.put("fields", fieldsObj)
                        formData.toString()
                    } else {
                        entryObj.optString("formDataJson", null)
                    }
                    
                    if (formDataJson != null && id > 0) {
                        val existing = database.vlfDao.getPulsePolioCampaign(id)
                        if (existing == null) {
                            val cache = PulsePolioCampaignCache(
                                id = id,
                                formDataJson = formDataJson,
                                syncState = SyncState.SYNCED
                            )
                            database.vlfDao.saveRecord(cache)
                            savedCount++
                            Timber.d("Saved new Pulse Polio Campaign entry with id: $id")
                        } else {
                            existing.formDataJson = formDataJson
                            existing.syncState = SyncState.SYNCED
                            database.vlfDao.saveRecord(existing)
                            savedCount++
                            Timber.d("Updated Pulse Polio Campaign entry with id: $id")
                        }
                    } else {
                        Timber.w("Skipping entry with id: $id, formDataJson is null or id is 0")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "cannot save Pulse Polio Campaign entry at index $i due to : $e")
                }
            }
            Timber.d("Saved $savedCount Pulse Polio Campaign entries to database")
        } catch (e: Exception) {
            Timber.e(e, "Error parsing Pulse Polio Campaign data: $e")
        }
    }


    suspend fun getFilariaMdaCampaign(id: Int): FilariaMDAFormResponseJsonEntity? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getFilariaMdaCampaign(id)
        }
    }

    suspend fun saveFilariaMdaCampaign(filariaMDaCampaignCache: FilariaMDAFormResponseJsonEntity) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(filariaMDaCampaignCache)
        }
    }

    suspend fun getUnsyncedFilariaMdaCampaign(): List<FilariaMDAFormResponseJsonEntity> {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getFilariaMdaCampaign(SyncState.UNSYNCED) ?: emptyList()
        }
    }


    fun getLastSubmissionDate(formId: String): Flow<String?> {
        Log.d("OverdueCheck", "Is overdue: $formId")
        return when (formId) {

            "vhnd" -> vlfDao.getLastVHNDSubmissionDate()
            "vhnc" -> vlfDao.getLastVHNCSubmissionDate()
            "phc_review" -> vlfDao.getLastPHCSubmissionDate()
            "ahd" -> vlfDao.getLastAHDSubmissionDate()
            "deworming" -> vlfDao.getLastDewormingSubmissionDate()
            "pulse_polio_campaign_form" -> {
                vlfDao.getAllPulsePolioCampaignForDate().map { list ->
                    list.mapNotNull { cache ->
                        try {
                            val formDataJson = cache.formDataJson ?: return@mapNotNull null
                            val jsonObj = JSONObject(formDataJson)
                            val fieldsObj = jsonObj.optJSONObject("fields") ?: return@mapNotNull null
                            fieldsObj.optString("campaign_date").takeIf { it.isNotEmpty() }
                        } catch (e: Exception) {
                            null
                        }
                    }.maxOrNull()
                }
            }
            "ors_campaign_form" -> {
                vlfDao.getAllORSCampaignForDate().map { list ->
                    list.mapNotNull { cache ->
                        try {
                            val formDataJson = cache.formDataJson ?: return@mapNotNull null
                            val jsonObj = JSONObject(formDataJson)
                            val fieldsObj = jsonObj.optJSONObject("fields") ?: return@mapNotNull null
                            fieldsObj.optString("campaign_date").takeIf { it.isNotEmpty() }
                        } catch (e: Exception) {
                            null
                        }
                    }.maxOrNull()
                }
            }
            else -> flowOf(null) // Return null for unknown formIds
        }
    }

    suspend fun pushUnSyncedRecords(): Boolean {
        val vlfVHNDResult = pushUnSyncedRecordsVHND()
        val vlfVHNCResult = pushUnSyncedRecordsVHNC()
        val vlfPHCResult = pushUnSyncedRecordsPHC()
        val vlfAHDResult = pushUnSyncedRecordsAHD()
        val vlfDewormingResult = pushUnSyncedRecordsDeworming()
//        return (vlfVHNDResult == 1) && (hrptResult == 1) && (hrnpaResult == 1) && (hrnptResult == 1)
        return (vlfVHNDResult == 1 && (vlfVHNCResult == 1) && (vlfPHCResult == 1) && (vlfAHDResult == 1) && (vlfDewormingResult == 1))
    }

    //...................for VHND.........................................
    private fun saveVHND(dataObj: String) {
        val requestDTO = Gson().fromJson(dataObj, JsonObject::class.java)
        val entries = requestDTO.getAsJsonArray("entries")
        for (dto in entries) {
            try {
                val entry = Gson().fromJson(dto.toString(), VHNDDTO::class.java)
                entry.vhndDate?.let {
                    val vhndCache = database
                        .vlfDao.getVHND(entry.id)
                    if (vhndCache == null) {
                        database.vlfDao.saveRecord(entry.toCache())
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.d("cannot save entry $dto due to : $e")
            }
        }
    }

    suspend fun getVHNDFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getVLFData(
                    GetVHNDRequest(
                        "VHND",
                        user.userId,

                        )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

//                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit hrp assess data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveVHND(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("HRP Assess entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get data error : $e")
                return@withContext getVHNDFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private fun updateSyncStatusVHND(entities: List<VHNDCache>?) {
        entities?.let {
            entities.forEach {
                database.vlfDao.saveRecord(it)
            }
        }
    }

    private suspend fun pushUnSyncedRecordsVHND(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val entities = database.vlfDao.getVHND(SyncState.UNSYNCED)

            val assessDtos = mutableListOf<Any>()
            entities?.let {
                it.forEach { cache ->
                    assessDtos.add(cache.toVhndDTODTO())
//                    pwrDtos.add(mapPWR(cache))
                    cache.syncState = SyncState.SYNCED
                }
            }

            if (assessDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveVHNDData(
                    UserDataDTO(
                        userId = user.userId,
                        entries = assessDtos
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to amrit vlf data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusVHND(entities)
                                    return@withContext 1
                                } catch (e: Exception) {
                                    Timber.d("VHND entries not synced $e")
                                }

                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext pushUnSyncedRecordsVHND()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    //.............................................................................
    //...................for VHNC.........................................
    private fun saveVHNC(dataObj: String) {
        val requestDTO = Gson().fromJson(dataObj, JsonObject::class.java)
        val entries = requestDTO.getAsJsonArray("entries")
        for (dto in entries) {
            try {
                val entry = Gson().fromJson(dto.toString(), VHNCDTO::class.java)
                entry.vhncDate?.let {
                    val vhncCache = database
                        .vlfDao.getVHNC(entry.id)
                    if (vhncCache == null) {
                        database.vlfDao.saveRecord(entry.toCache())
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.d("cannot save entry $dto due to : $e")
            }
        }
    }

    suspend fun getVHNCFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getVLFData(
                    GetVHNDRequest(
                        "VHNC",
                        user.userId,

                        )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

//                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit hrp assess data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveVHNC(dataObj)
                                } catch (e: Exception) {
//                                    Timber.d("HRP Assess entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get data error : $e")
                return@withContext getVHNCFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private fun updateSyncStatusVHNC(entities: List<VHNCCache>?) {
        entities?.let {
            entities.forEach {
                database.vlfDao.saveRecord(it)
            }
        }
    }

    private suspend fun pushUnSyncedRecordsVHNC(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val entities = database.vlfDao.getVHNC(SyncState.UNSYNCED)

            val assessDtos = mutableListOf<Any>()
            entities?.let {
                it.forEach { cache ->
                    assessDtos.add(cache.toVhncDTODTO())
//                    pwrDtos.add(mapPWR(cache))
                    cache.syncState = SyncState.SYNCED
                }
            }

            if (assessDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveVHNCData(
                    UserDataDTO(
                        userId = user.userId,
                        entries = assessDtos
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to amrit vlf data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusVHNC(entities)
                                    return@withContext 1
                                } catch (e: Exception) {
                                    Timber.d("VHND entries not synced $e")
                                }

                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext pushUnSyncedRecordsVHNC()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    //.............................................................................

    //...................for PHC.........................................
    private fun savePHC(dataObj: String) {
        val requestDTO = Gson().fromJson(dataObj, JsonObject::class.java)
        val entries = requestDTO.getAsJsonArray("entries")
        for (dto in entries) {
            try {
                val entry = Gson().fromJson(dto.toString(), PHCReviewDTO::class.java)
                entry.phcReviewDate?.let {
                    val phcCache = database.vlfDao.getPHC(entry.id)
                    if (phcCache == null) {
                        database.vlfDao.saveRecord(entry.toCache())
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.d("cannot save entry $dto due to : $e")
            }
        }
    }

    suspend fun getPHCFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getVLFData(
                    GetVHNDRequest(
                        "PHC",
                        user.userId,

                        )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

//                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit hrp assess data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    savePHC(dataObj)
                                } catch (e: Exception) {
//                                    Timber.d("HRP Assess entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get data error : $e")
                return@withContext getPHCFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private fun updateSyncStatusPHC(entities: List<PHCReviewMeetingCache>?) {
        entities?.let {
            entities.forEach {
                database.vlfDao.saveRecord(it)
            }
        }
    }

    private suspend fun pushUnSyncedRecordsPHC(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val entities = database.vlfDao.getPHC(SyncState.UNSYNCED)

            val assessDtos = mutableListOf<Any>()
            entities?.let {
                it.forEach { cache ->
                    assessDtos.add(cache.toPHCDTODTO())
//                    pwrDtos.add(mapPWR(cache))
                    cache.syncState = SyncState.SYNCED
                }
            }

            if (assessDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.savePHCData(
                    UserDataDTO(
                        userId = user.userId,
                        entries = assessDtos
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to amrit vlf data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusPHC(entities)
                                    return@withContext 1
                                } catch (e: Exception) {
//                                    Timber.d("VHND entries not synced $e")
                                }

                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext pushUnSyncedRecordsPHC()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    //.............................................................................


    //...................for AHD.........................................
    suspend fun saveAHD(dataObj: String) {
        val requestDTO = Gson().fromJson(dataObj, JsonObject::class.java)
        val entries = requestDTO.getAsJsonArray("entries")
        for (dto in entries) {
            try {
                val entry = Gson().fromJson(dto.toString(), AHDDTO::class.java)
                entry.ahdDate?.let {
                    val ahdDate = database.vlfDao.getAHD(entry.id)
                    if (ahdDate == null) {
                        database.vlfDao.saveRecord(entry.toCache())
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.d("cannot save entry $dto due to : $e")
            }
        }
    }

    suspend fun getAHDFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getVLFData(
                    GetVHNDRequest(
                        "AHD",
                        user.userId,

                        )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit hrp assess data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveAHD(dataObj)
                                } catch (e: Exception) {
//                                    Timber.d("HRP Assess entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get data error : $e")
                return@withContext getAHDFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun updateSyncStatusAHD(entities: List<AHDCache>?) {
        entities?.let {
            entities.forEach {
                database.vlfDao.saveRecord(it)
            }
        }
    }

    private suspend fun pushUnSyncedRecordsAHD(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val entities = database.vlfDao.getAHD(SyncState.UNSYNCED)

            val assessDtos = mutableListOf<Any>()
            entities?.let {
                it.forEach { cache ->
                    assessDtos.add(cache.toDTO())
                    cache.syncState = SyncState.SYNCED
                }
            }

            if (assessDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveAHDData(
                    UserDataDTO(
                        userId = user.userId,
                        entries = assessDtos
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to amrit vlf data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusAHD(entities)
                                    return@withContext 1
                                } catch (e: Exception) {
//                                    Timber.d("VHND entries not synced $e")
                                }

                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext pushUnSyncedRecordsAHD()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    //.............................................................................
    //...................for Deworming.........................................
    suspend fun saveDeworming(dataObj: String) {
        val requestDTO = Gson().fromJson(dataObj, JsonObject::class.java)
        val entries = requestDTO.getAsJsonArray("entries")
        for (dto in entries) {
            try {
                val entry = Gson().fromJson(dto.toString(), DewormingDTO::class.java)
                entry.dewormingDone?.let {
                    val dewormingDate = database.vlfDao.getDeworming(entry.id)
                    if (dewormingDate == null) {
                        database.vlfDao.saveRecord(entry.toCache())
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.d("cannot save entry $dto due to : $e")
            }
        }
    }

    suspend fun getDewormingFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getVLFData(
                    GetVHNDRequest(
                        "Deworming",
                        user.userId,

                        )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit hrp assess data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveDeworming(dataObj)
                                } catch (e: Exception) {
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get data error : $e")
                return@withContext getDewormingFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get data error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun updateSyncStatusDeworming(entities: List<DewormingCache>?) {
        entities?.let {
            entities.forEach {
                database.vlfDao.saveRecord(it)
            }
        }
    }

    private suspend fun pushUnSyncedRecordsDeworming(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val entities = database.vlfDao.getDeworming(SyncState.UNSYNCED)

            val assessDtos = mutableListOf<Any>()
            entities?.let {
                it.forEach { cache ->
                    assessDtos.add(cache.toDTO())
                    cache.syncState = SyncState.SYNCED
                }
            }

            if (assessDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveDewormingData(
                    UserDataDTO(
                        userId = user.userId,
                        entries = assessDtos
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to amrit vlf data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusDeworming(entities)
                                    return@withContext 1
                                } catch (e: Exception) {
//                                    Timber.d("VHND entries not synced $e")
                                }

                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext pushUnSyncedRecordsDeworming()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    //.............................................................................


    @RequiresApi(Build.VERSION_CODES.O)
    fun isFormFilledForCurrentMonth(): Flow<Map<String, Boolean>> {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val currentYearMonth = YearMonth.now()
        val startDate = currentYearMonth.atDay(1).format(formatter)
        val endDate = currentYearMonth.atEndOfMonth().format(formatter)

        val vhnd = vlfDao.countVHNDFormsInDateRange(startDate, endDate)
        val vhnc = vlfDao.countVHNCFormsInDateRange(startDate, endDate)
        val phc = vlfDao.countPHCFormsInDateRange(startDate, endDate)
        val ahd = vlfDao.countAHDFormsInDateRange(startDate, endDate)
        val deworming = vlfDao.countDewormingInLastSixMonths()
        return combine(vhnd, vhnc, phc, ahd, deworming) { vhndCount, vhncCount, phcCount, ahdCount, dewormingCount ->
            mapOf(
                "VHND" to (vhndCount > 0),
                "VHNC" to (vhncCount > 0),
                "PHC" to (phcCount > 0),
                "AHD" to (ahdCount > 0),
                "DEWORMING" to (dewormingCount > 0)
            )
        }
    }


    suspend fun getFilariaMdaCampaignFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getFilariaMdaCampaign()
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull Filaria Mda Campaign data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    when (val dataValue = jsonObj.opt("data")) {
                                        is org.json.JSONArray -> {
                                            savefilariaMdaCampaignToServer(dataValue.toString())
                                            return@withContext 1
                                        }

                                        is String -> {
                                            savefilariaMdaCampaignToServer(dataValue)
                                            return@withContext 1
                                        }

                                        is JSONObject -> {
                                            savefilariaMdaCampaignToServer(dataValue.toString())
                                            return@withContext 1
                                        }

                                        else -> {
                                            Timber.e("Unexpected data format: ${dataValue?.javaClass}")
                                            return@withContext 0
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Pulse Polio Campaign entries not synced")
                                    return@withContext 0
                                }
                            }
                            5002 -> {
                                if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                    throw SocketTimeoutException("Refreshed Token!")
                                } else {
                                    throw IllegalStateException("User Logged out!!")
                                }
                            }
                            else -> {
                                throw IllegalStateException("$responseStatusCode received")
                            }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("get Filaria Mda Campaign data error : $e")
                return@withContext getFilariaMdaCampaignFromServer()
            } catch (e: IllegalStateException) {
                Timber.d("get Filaria Mda Campaign data error : $e")
                return@withContext -1
            }
            -1
        }
    }


    suspend fun saveMdaFilariaCampaignToServer(filariaMdaCache: FilariaMDAFormResponseJsonEntity): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val formDataJson = filariaMdaCache.formDataJson ?: ""
                val formDataObj = try {
                    JSONObject(formDataJson)
                } catch (e: Exception) {
                    JSONObject()
                }

                val fieldsObj = formDataObj.optJSONObject("fields") ?: JSONObject()
                val campaignPhotosValue = fieldsObj.opt("campaign_photos") ?: fieldsObj.opt("campaignPhotos")

                val photoUris = when {
                    campaignPhotosValue is String -> {
                        try {
                            Gson().fromJson(campaignPhotosValue as String, Array<String>::class.java).toList()
                        } catch (e: Exception) {
                            listOf(campaignPhotosValue.toString()).filter { it.isNotEmpty() }
                        }
                    }
                    campaignPhotosValue is org.json.JSONArray -> {
                        (0 until (campaignPhotosValue as org.json.JSONArray).length())
                            .mapNotNull { campaignPhotosValue.opt(it)?.toString() }
                    }
                    else -> emptyList()
                }

                val imageParts = photoUris.mapNotNull { photoData ->
                    try {
                        val file: File? = when {
                            photoData.startsWith("data:image/") || photoData.contains(",") -> {
                                val base64Data = photoData.substringAfter(",", photoData)
                                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                val tempFile = File.createTempFile("campaign_photo_", ".jpg", appContext.cacheDir)
                                tempFile.writeBytes(bytes)
                                tempFile
                            }
                            photoData.startsWith("content://") || photoData.startsWith("file://") -> {
                                val uri = android.net.Uri.parse(photoData)
                                val name = HelperUtil.getFileName(uri, appContext) ?: "campaign_photo"
                                val mime = appContext.contentResolver.getType(uri) ?: "image/jpeg"
                                if (mime.startsWith("image/")) {
                                    compressImageToTemp(uri, name, appContext)
                                } else {
                                    null
                                }
                            }

                            else -> {
                                val filePath = File(photoData)
                                if (filePath.exists()) filePath else null
                            }
                        }
                        file?.let {
                            val mime = "image/jpeg"
                            val body = it.asRequestBody(mime.toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("campaignPhotos", it.name, body)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing image: $photoData")
                        null
                    }
                }

                val multipartParts = mutableListOf<MultipartBody.Part>()

                val formDataJsonBody = formDataJson.toRequestBody("application/json".toMediaTypeOrNull())
                multipartParts.add(
                    MultipartBody.Part.createFormData("formDataJson", null, formDataJsonBody)
                )

                multipartParts.addAll(imageParts)

                val response = tmcNetworkApiService.saveFilariaMdaCampaign(
                    campaignData = multipartParts
                )

                if (response.isSuccessful) {
                    filariaMdaCache.syncState = SyncState.SYNCED
                    database.vlfDao.saveRecord(filariaMdaCache)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving Pulse Polio Campaign to server")
                false
            }
        }
    }


    suspend fun savefilariaMdaCampaignToServer(dataObj: String) {

        val requestDTO = Gson().fromJson(dataObj, JsonObject::class.java)
        val entries = requestDTO.getAsJsonArray("entries")
        for (dto in entries) {
            try {
                val entry = dto.asJsonObject
                val id = entry.get("id")?.asInt ?: 0
                val formDataJson = entry.get("formDataJson")?.asString
                val jsonObject = Gson().fromJson(formDataJson, JsonObject::class.java)
                val fieldsObj = jsonObject.getAsJsonObject("fields")
                val startDate = fieldsObj?.get("start_date")?.asString
                if (formDataJson != null) {
                    val existing = database.vlfDao.getFilariaMdaCampaign(id)
                    if (existing == null) {
                        val cache = FilariaMDAFormResponseJsonEntity(
                            id = id,
                            hhId = 0,
                            formDataJson = formDataJson,
                            visitDate = startDate.toString(),
                            visitMonth = toMonthKey(startDate),
                            formId = "LF_MDA_CAMPAIGN",
                            version = 1,
                            isSynced = true,
                            syncState = SyncState.SYNCED
                        )
                        database.vlfDao.saveRecord(cache)
                    }
                }
            } catch (e: Exception) {
                Timber.d("cannot save Filaria MDA Campaign entry $dto due to : $e")
            }
        }
    }

    private fun toMonthKey(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        val inputs = listOf("dd-MM-yyyy", "yyyy-MM-dd")
        val out = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        for (fmt in inputs) {
            try {
                val d = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)
                if (d != null) return out.format(d)
            } catch (_: Exception) {}
        }
        return try {
            if (Regex("\\d{2}-\\d{2}-\\d{4}").matches(dateStr)) {
                val yyyy = dateStr.substring(6, 10)
                val mm = dateStr.substring(3, 5)
                "$yyyy-$mm"
            } else ""
        } catch (_: Exception) { "" }
    }

}