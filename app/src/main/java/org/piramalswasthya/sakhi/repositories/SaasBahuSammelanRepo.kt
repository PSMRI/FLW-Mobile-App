package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
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
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.getDateFromLong
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.model.SaasBahuSammelanGetAllResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataRequest
import org.piramalswasthya.sakhi.network.getLongFromDate
import org.piramalswasthya.sakhi.repositories.BenRepo.Companion.getCurrentDate
import org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.copyToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.detectExtAndMime
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileName
import timber.log.Timber
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.collections.forEach

class SaasBahuSammelanRepo @Inject constructor(
    private val userRepo: UserRepo,
    @ApplicationContext val appContext: Context,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService,
    private val saasBahuDao: SaasBahuSammelanDao,
    private val moshi: Moshi


)  {

    suspend fun saveSammelanForm(saasBahuSammelanCache: SaasBahuSammelanCache) {
        withContext(Dispatchers.IO) {
            saasBahuDao.insertSammelan(saasBahuSammelanCache)
        }
    }

     suspend fun pushUnSyncedRecordsSaasBahuSammelan(): Boolean {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val saasBahuList: List<SaasBahuSammelanCache> =
                saasBahuDao.getBySyncState(SyncState.UNSYNCED)
            try {
                saasBahuList.forEach { row ->
                    val imagesParts = (row.sammelanImages ?: emptyList()).mapNotNull { uriStr ->
                        val uri = android.net.Uri.parse(uriStr)
                        val name = getFileName(uri, appContext) ?: "upload"
                        val mime =
                            appContext.contentResolver.getType(uri) ?: "application/octet-stream"
                        val fileForUpload = if (mime.startsWith("image/")) compressImageToTemp(
                            uri,
                            name,
                            appContext
                        ) else copyToTemp(uri, name, appContext)
                        fileForUpload?.let { file ->
                            val body = file.asRequestBody(mime.toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("sammelanImages", file.name, body)
                        }
                    }

                    val response = tmcNetworkApiService.postSaasBahuSammelanMultipart(
                        meetingDate = (row.date.toString()).toRequestBody("text/plain".toMediaTypeOrNull()),
                        place = (row.place ?: "").toRequestBody("text/plain".toMediaTypeOrNull()),
                        participants = ((row.participants
                            ?: 0).toString()).toRequestBody("text/plain".toMediaTypeOrNull()),
                        ashaId = ((row.ashaId
                            ?: 0).toString()).toRequestBody("text/plain".toMediaTypeOrNull()),
                        meetingImages = imagesParts
                    )
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)

                            val responseStatusCode = jsonObj.getInt("statusCode")
                            Timber.d("Push to amrit saas bahu sammelan  data : $responseStatusCode")
                            when (responseStatusCode) {
                                200 -> {
                                    try {
                                        updateSyncStatusScreening(saasBahuList)
                                        return@withContext true
                                    } catch (e: Exception) {
                                        Timber.d("Saas Bahu Sammelan entries not synced $e")
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
                                    if (errorMessage == "No record found") return@withContext false
                                }

                                else -> {
                                    throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                                }
                            }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext false

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext false
            }
            true
        }
    }



    private suspend fun updateSyncStatusScreening(saasBahuList: List<SaasBahuSammelanCache>) {
        saasBahuList.forEach {
            it.syncState = SyncState.SYNCED
            saasBahuDao.insertSammelan(it)
        }
    }




    suspend fun SaasBahuSamelanGettDataFromServer() = withContext(Dispatchers.IO) {
        val response = tmcNetworkApiService.getSaasBahuSammelans(
            GetDataRequest(
                0,
                getCurrentDate(preferenceDao.getLastSyncedTimeStamp()),
                getCurrentDate(),
                0,
                preferenceDao.getLoggedInUser()?.userId?.toLong()!!,
                preferenceDao.getLoggedInUser()?.userName!!,
                preferenceDao.getLoggedInUser()?.userId?.toLong()!!
            )
        )
        if (!response.isSuccessful) return@withContext
        val body = response.body()?.string() ?: return@withContext
        Log.e("AHJAHA",body)
        val adapter = moshi.adapter(SaasBahuSammelanGetAllResponse::class.java)
        val parsed = adapter.fromJson(body) ?: return@withContext
        saasBahuDao.clearAll()
        parsed.data?.forEach { item ->
            val imageBase64List = item.meetingImages ?: emptyList()
            val imageUriList = imageBase64List.mapNotNull { base64 ->
                try {
                    val base64Data = base64.substringAfter(",", base64)
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val (ext, _) = detectExtAndMime(bytes)
                    val file = File(appContext.cacheDir, "saas_bahu_sammelan${System.currentTimeMillis()}.$ext")
                    file.outputStream().use { it.write(bytes) }
                    val uri = FileProvider.getUriForFile(
                        appContext,
                        "${appContext.packageName}.provider",
                        file
                    )
                    uri.toString()
                } catch (e: Exception) {
                    Log.e("AHJAHA",e.toString())
                    null
                }
            }

            val entity = SaasBahuSammelanCache(
                id = item.id?.toLong()!!,
                date = item.meetingDate,
                place = item.place,
                participants = item.participants,
                ashaId = item.ashaId!!,
                sammelanImages = imageUriList,
                syncState = SyncState.SYNCED
            )

            saasBahuDao.insertSammelan(entity)
        }
    }



}