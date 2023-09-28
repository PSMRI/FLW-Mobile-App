package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomePost
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class DeliveryOutcomeRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val benDao: BenDao,
    private val deliveryOutcomeDao: DeliveryOutcomeDao
) {

    suspend fun getDeliveryOutcome(benId: Long): DeliveryOutcomeCache? {
        return withContext(Dispatchers.IO) {
            deliveryOutcomeDao.getDeliveryOutcome(benId)
        }
    }

    suspend fun saveDeliveryOutcome(deliveryOutcomeCache: DeliveryOutcomeCache) {
        withContext(Dispatchers.IO) {
            deliveryOutcomeDao.saveDeliveryOutcome(deliveryOutcomeCache)
        }
    }

    suspend fun processNewDeliveryOutcome(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val deliveryOutcomeList = deliveryOutcomeDao.getAllUnprocessedDeliveryOutcomes()

            val deliveryOutcomePostList = mutableSetOf<DeliveryOutcomePost>()

            deliveryOutcomeList.forEach {
                deliveryOutcomePostList.clear()
                val ben = benDao.getBen(it.benId)
                    ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
                deliveryOutcomePostList.add(it.asPostModel())
                it.syncState = SyncState.SYNCING
                deliveryOutcomeDao.updateDeliveryOutcome(it)
                val uploadDone = postDataToAmritServer(deliveryOutcomePostList)
                if (uploadDone) {
                    it.processed = "P"
                    it.syncState = SyncState.SYNCED
                } else {
                    it.syncState = SyncState.UNSYNCED
                }
                deliveryOutcomeDao.updateDeliveryOutcome(it)
            }

            return@withContext true
        }
    }

    private suspend fun postDataToAmritServer(deliveryOutcomePostList: MutableSet<DeliveryOutcomePost>): Boolean {
        if (deliveryOutcomePostList.isEmpty()) return false
        val user =
            preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

        try {

            val response = amritApiService.postDeliveryOutcomeForm(deliveryOutcomePostList.toList())
            val statusCode = response.code()

            if (statusCode == 200) {
                try {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errormessage = jsonObj.getString("message")
                        if (jsonObj.isNull("statusCode")) throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")
                        val responsestatuscode = jsonObj.getInt("statusCode")

                        when (responsestatuscode) {
                            200 -> {
                                Timber.d("Saved Successfully to server")
                                return true
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName,
                                        user.password
                                    )
                                ) throw SocketTimeoutException()
                            }

                            else -> {
                                throw IOException("Throwing away IO eXcEpTiOn")
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                //server_resp5();
            }
            Timber.w("Bad Response from server, need to check $deliveryOutcomePostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postDataToAmritServer(deliveryOutcomePostList)
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        }
    }

    suspend fun getDeliveryOutcomesFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getDeliveryOutcomeData(
                    GetDataPaginatedRequest(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = getCurrentDate(lastTimeStamp),
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
                        Timber.d("Pull from amrit Delivery Outcome data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveDeliveryOutcomeCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Delivery Outcome entries not synced $e")
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
                Timber.d("get_delivery_outcome error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_delivery_outcome error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveDeliveryOutcomeCacheFromResponse(dataObj: String): List<DeliveryOutcomePost> {
        var deliveryOutcomeList = Gson().fromJson(dataObj, Array<DeliveryOutcomePost>::class.java).toList()
        deliveryOutcomeList.forEach { deliveryOutcome ->
            deliveryOutcome.createdDate?.let {
                var deliveryOutcomeCache: DeliveryOutcomeCache? =
                    deliveryOutcomeDao.getDeliveryOutcome(deliveryOutcome.benId)
                if (deliveryOutcomeCache == null) {
                    deliveryOutcomeDao.saveDeliveryOutcome(deliveryOutcome.toDeliveryCache())
                }
            }
        }
        return deliveryOutcomeList
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }
    }
}