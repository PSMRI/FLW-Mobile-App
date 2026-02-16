package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationPost
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ImmunizationRepo @Inject constructor(
    private val tbDao: TBDao,
    private val immunizationDao: ImmunizationDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getImmunizationDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getChildImmunizationDetails(
                    GetDataPaginatedRequest(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate()
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit child immunization data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveImmunizationCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Child Immunization entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            401,5002 -> {
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
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_child_immunization error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_child_immunization error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveImmunizationCacheFromResponse(dataObj: String): List<ImmunizationPost> {
        val immunizationList =
            Gson().fromJson(dataObj, Array<ImmunizationPost>::class.java).toList()
        immunizationList.forEach { immunizationDTO ->
            val immunization: ImmunizationCache? =
                immunizationDao.getImmunizationRecord(
                    immunizationDTO.beneficiaryId,
                    immunizationDTO.vaccineId
                )
            if (immunization == null) {
                val immunizationCache = immunizationDTO.toCacheModel()
                immunizationCache.vaccineId = immunizationDTO.vaccineId
                immunizationDao.addImmunizationRecord(immunizationCache)
            }
        }
        return immunizationList
    }

    // RECORD-LEVEL ISOLATION: Immunization records are now sent in
    // chunks of 20 instead of one giant batch. Previously, if ANY record in
    // the batch was malformed, the ENTIRE batch failed and ALL records stayed
    // UNSYNCED. Now each chunk is independent — one bad chunk doesn't affect
    // the others. Failed chunks' records stay UNSYNCED for the next sync cycle.
    suspend fun pushUnSyncedChildImmunizationRecords(): Boolean {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val immunizationCacheList: List<ImmunizationCache> =
                immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED)

            if (immunizationCacheList.isEmpty()) return@withContext true

            // Chunk records to prevent all-or-nothing batch failure
            val CHUNK_SIZE = 20
            val chunks = immunizationCacheList.chunked(CHUNK_SIZE)
            var successCount = 0
            var failCount = 0

            for (chunk in chunks) {
                try {
                    val chunkDtos = chunk.map { cache ->
                        val immunizationDTO = cache.asPostModel()
                        val vaccine = immunizationDao.getVaccineById(cache.vaccineId)!!
                        immunizationDTO.vaccineName = vaccine.vaccineName
                        immunizationDTO
                    }

                    val response = tmcNetworkApiService.postChildImmunizationDetails(chunkDtos)
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)
                            val responseStatusCode = jsonObj.getInt("statusCode")
                            Timber.d("Push to Amrit Child Immunization chunk: $responseStatusCode")
                            when (responseStatusCode) {
                                200 -> {
                                    updateSyncStatusImmunization(chunk)
                                    successCount += chunk.size
                                }

                                401, 5002 -> {
                                    // Token expired — try refreshing for subsequent chunks
                                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                        Timber.d("Token refreshed, chunk will retry next cycle")
                                    }
                                    failCount += chunk.size
                                }

                                else -> {
                                    Timber.e("Immunization chunk failed with statusCode: $responseStatusCode")
                                    failCount += chunk.size
                                }
                            }
                        }
                    } else {
                        Timber.e("Immunization chunk HTTP error: $statusCode")
                        failCount += chunk.size
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Immunization chunk push failed: ${chunk.size} records")
                    failCount += chunk.size
                }
            }

            Timber.d("Immunization push complete: $successCount succeeded, $failCount failed out of ${immunizationCacheList.size}")
            // Worker succeeds — failed records stay UNSYNCED for next cycle
            return@withContext true
        }
    }

    private suspend fun updateSyncStatusImmunization(immunizationList: List<ImmunizationCache>) {
        immunizationList.forEach {
            it.syncState = SyncState.SYNCED
            it.processed = "P"
            immunizationDao.addImmunizationRecord(it)
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        private fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }
    }

    suspend fun getVaccineDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getAllChildVaccines(category = "CHILD")
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit child vaccine data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveVaccinesFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Child Vaccine entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            401,5002 -> {
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
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_child_vaccines error : $e")
                getVaccineDetailsFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_child_vaccines error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveVaccinesFromResponse(dataObj: String) {
        val vaccineList = Gson().fromJson(dataObj, Array<Vaccine>::class.java).toList()
        val mitaninOnlyVaccines = setOf(
            "PCV-1",
            "PCV-2",
            "PCV-Booster"
        )
        vaccineList.forEach { vaccine ->
            if (
                vaccine.vaccineName in mitaninOnlyVaccines &&
                !BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)
            ) {
                return@forEach
            }
            val existingVaccine = immunizationDao.getVaccineByName(vaccine.vaccineName)

            if (existingVaccine == null) {
                immunizationDao.addVaccine(vaccine)
            }
        }
    }

}