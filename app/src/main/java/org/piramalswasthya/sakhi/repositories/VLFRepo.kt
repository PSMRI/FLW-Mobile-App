package org.piramalswasthya.sakhi.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.PwrPost
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.network.AHDDTO
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.DewormingDTO
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.network.GetVHNDRequest
import org.piramalswasthya.sakhi.network.PHCReviewDTO
import org.piramalswasthya.sakhi.network.UserDataDTO
import org.piramalswasthya.sakhi.network.VHNCDTO
import org.piramalswasthya.sakhi.network.VHNDDTO
import timber.log.Timber
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class VLFRepo @Inject constructor(
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService,
    private val vlfDao: VLFDao

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


    fun getLastSubmissionDate(formId: String): Flow<String?> {
        Log.d("OverdueCheck", "Is overdue: $formId")
        return when (formId) {

            "vhnd" -> vlfDao.getLastVHNDSubmissionDate()
            "vhnc" -> vlfDao.getLastVHNCSubmissionDate()
            "phc_review" -> vlfDao.getLastPHCSubmissionDate()
            "ahd" -> vlfDao.getLastAHDSubmissionDate()
            "deworming" -> vlfDao.getLastDewormingSubmissionDate()
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

                        val errorMessage = jsonObj.getString("errorMessage")
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

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit hrp assess data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveDeworming(dataObj)
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
//    fun isFormFilledForCurrentMonth(): Flow<Boolean> {
//        val currentMonthStart = YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val currentMonthEnd = YearMonth.now().atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        return vlfDao.countVHNDFormsInRange(currentMonthStart, currentMonthEnd).map { count -> count > 0 }
//    }

    fun isFormFilledForCurrentMonth(): Flow<Map<String, Boolean>> {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val currentYearMonth = YearMonth.now()
        val startDate = currentYearMonth.atDay(1).format(formatter)   // 01-04-2025
        val endDate = currentYearMonth.atEndOfMonth().format(formatter) // 30-04-2025

        val vhnd = vlfDao.countVHNDFormsInDateRange(startDate, endDate)
        val vhnc = vlfDao.countVHNCFormsInDateRange(startDate, endDate)
        val phc = vlfDao.countPHCFormsInDateRange(startDate, endDate)
        val ahd = vlfDao.countAHDFormsInDateRange(startDate, endDate)
        val deworming = vlfDao.countDewormingFormsInDateRange(startDate, endDate)
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



}