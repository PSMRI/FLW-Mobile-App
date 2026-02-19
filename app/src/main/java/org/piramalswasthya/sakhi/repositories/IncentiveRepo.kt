package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.setToEndOfTheDay
import org.piramalswasthya.sakhi.model.IncentiveActivityListRequest
import org.piramalswasthya.sakhi.model.IncentiveActivityNetwork
import org.piramalswasthya.sakhi.model.IncentiveRecordListRequest
import org.piramalswasthya.sakhi.model.IncentiveRecordNetwork
import org.piramalswasthya.sakhi.model.UploadResponse
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.getDateTimeStringFromLong
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil.getFilesName
import org.piramalswasthya.sakhi.utils.HelperUtil.toRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.util.Calendar
import javax.inject.Inject

class IncentiveRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val incentiveDao: IncentiveDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context

) {

    val list = incentiveDao.getAllRecords()
    val activity_list = incentiveDao.getAllActivity()


    suspend fun pullAndSaveAllIncentiveActivities(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val currentLang = preferenceDao.getCurrentLanguage().symbol
                val stateId = user.state.id
                val districtId = user.district.id
                val requestBody = IncentiveActivityListRequest(stateId, districtId, currentLang)
                val response = amritApiService.getAllIncentiveActivities(requestBody = requestBody)
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit incentives data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveIncentiveMasterData(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Incentive master data not synced $e")
                                    return@withContext false
                                }

                                return@withContext true
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                if (errorMessage == "No record found") return@withContext true
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("incentives error : $e")
                pullAndSaveAllIncentiveActivities(user)
                return@withContext true
            } catch (e: Exception) {
                Timber.d("Caught $e at incentives!")
                return@withContext false
            }
            true
        }
    }

    private suspend fun saveIncentiveMasterData(dataObj: String) {

        val activities =
            Gson().fromJson(dataObj, Array<IncentiveActivityNetwork>::class.java).toList()

        val activityList = activities.map { it.asCacheModel() }
        incentiveDao.insert(*activityList.toTypedArray())

    }

    suspend fun pullAndSaveAllIncentiveRecords(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {

                val requestBody = IncentiveRecordListRequest(
                    user.userId,
                    getDateTimeStringFromLong(
                        preferenceDao.lastIncentivePullTimestamp
                    )!!,
                    getDateTimeStringFromLong(
                        Calendar.getInstance().setToEndOfTheDay().timeInMillis
                    )!!,
                    villageID = user.state.id
                )
                val response = amritApiService.getAllIncentiveRecords(requestBody = requestBody)
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit incentives data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveIncentiveRecordsData(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Incentive master data not synced $e")
                                    return@withContext false
                                }

                                return@withContext true
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                if (errorMessage == "No record found") return@withContext true
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("incentives error : $e")
                pullAndSaveAllIncentiveRecords(user)
                return@withContext true
            } catch (e: Exception) {
                Timber.d("Caught $e at incentives!")
                return@withContext false
            }
            true
        }
    }

    private suspend fun saveIncentiveRecordsData(dataObj: String) {
        val records = Gson().fromJson(dataObj, Array<IncentiveRecordNetwork>::class.java).toList()
        val recordList = records.map { it.asCacheModel() }
        incentiveDao.insert(*recordList.toTypedArray())

    }

   /* suspend fun uploadIncentiveFiles(
        id : Long,
        userId: Long,
        moduleName: String,
        fileUris: List<String>
    ): Result<UploadResponse> {
        return try {
            val fileParts = mutableListOf<MultipartBody.Part>()

            fileUris.forEach { uriString ->
                val uri = Uri.parse(uriString)
                val file = createTempFileFromUri(uri)

                if (file != null) {
                    val requestFile = file.asRequestBody(
                        getMimeType(uri)?.toMediaTypeOrNull()
                    )

                    val part = MultipartBody.Part.createFormData(
                        "files",
                        file.name,
                        requestFile
                    )

                    fileParts.add(part)
                } else {
                    Timber.w("Could not create file from URI: $uriString")
                }
            }

            if (fileParts.isEmpty()) {
                return Result.failure(Exception("No valid files to upload"))
            }

            val response = amritApiService.uploadIncentiveDocuments(
                id = id.toRequestBody(),
                userId = userId.toRequestBody(),
                moduleName = moduleName.toRequestBody(),
                images = fileParts
            )

            // Clean up temp files
            fileParts.forEach { part ->
                // Delete temp files if needed
            }

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error uploading files")
            Result.failure(e)
        }
    }*/

    suspend fun uploadIncentiveFiles(
        id: Long,
        userId: Long,
        moduleName: String,
        activityName : String,
        fileUris: List<String>
    ): Result<UploadResponse> {
        val tempFiles = mutableListOf<File>()

        return try {
            val fileParts = mutableListOf<MultipartBody.Part>()

            fileUris.forEach { uriString ->
                val uri = Uri.parse(uriString)
                val file = createTempFileFromUri(uri)

                if (file != null) {
                    tempFiles.add(file)
                    val mimeType = getMimeType(uri) ?: "image/*"

                    val requestFile = file.asRequestBody(
                        mimeType.toMediaTypeOrNull()
                    )

                    val part = MultipartBody.Part.createFormData(
                        "images[]",
                        file.name,
                        requestFile
                    )

                    fileParts.add(part)
                }
            }

            if (fileParts.isEmpty()) {
                return Result.failure(Exception("No valid files to upload"))
            }

            val response = amritApiService.uploadIncentiveDocuments(
                id = id.toRequestBody(),
                userId = userId.toRequestBody(),
                moduleName = moduleName.toRequestBody(),
                activityName = activityName.toRequestBody(),
                images = fileParts
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception("Upload failed: ${response.code()} ${response.message()}")
                )
            }

        } catch (e: Exception) {
            Timber.e(e, "Error uploading files")
            Result.failure(e)
        }
        finally {
            tempFiles.forEach { file ->
                if (file.exists()) {
                    file.delete()
                    Timber.d("Temp file deleted: ${file.name}")
                }
            }
        }
    }


    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: run {
                    Timber.w("InputStream is null for URI: $uri")
                    return null
                }

            val fileName = getFilesName(uri, context) ?: "temp_file_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)

            inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.length() == 0L) {
                Timber.w("Temp file is empty, deleting: ${tempFile.name}")
                tempFile.delete()
                return null
            }

            tempFile
        } catch (e: Exception) {
            Timber.e(e, "Error creating temp file from URI")
            null
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }


}