package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.ABHAProfile
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.MapHIDtoBeneficiary
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import timber.log.Timber
import java.net.SocketTimeoutException
@HiltWorker
class PushMapAbhatoBenficiaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val benRepo: BenRepo,
    private val abhaGenratedDao: ABHAGenratedDao,
    private val preferenceDao: PreferenceDao,
    private val abhaIdRepo: AbhaIdRepo,

    ) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushMapAbhatoBenficiaryWorker"
    }

    override suspend fun doWork(): Result {

        init()
        val unsyncedList = abhaGenratedDao.getAllAbha()
        var allSuccessful = true
        for (model in unsyncedList) {
            val ben = benRepo.getBenFromId(model?.beneficiaryID!!)
            val gson = Gson()
            val profile = gson.fromJson(model.abhaProfileJson, ABHAProfile::class.java)
            val request = MapHIDtoBeneficiary(
                beneficiaryID = model?.beneficiaryID,
                beneficiaryRegID = model?.beneficiaryRegID,
                healthId = model?.healthId,
                healthIdNumber = model?.healthIdNumber,
                providerServiceMapId = model?.providerServiceMapId,
                createdBy = model?.createdBy,
                message = model?.message,
                txnId = model?.txnId,
                ABHAProfile = profile,
                isNew = model?.isNewAbha
            )

            try {
                when (val result = abhaIdRepo.mapHealthIDToBeneficiary(request,ben)) {
                    is NetworkResult.Success -> {
                        val response = JSONObject(result.data)
                        Timber.d("Success: ${response}")
                        abhaGenratedDao.deleteAbhaByBenId(request.beneficiaryID!!)
                    }

                    is NetworkResult.Error -> {
                        Timber.e("Error [${result.code}]: ${result.message}")
                        allSuccessful = false
                    }

                    is NetworkResult.NetworkError -> {
                        Timber.e("Network connection failed.")
                        allSuccessful = false
                    }
                }
            } catch (e: Exception) {
                Timber.e("Exception syncing ABHA for benId=${model.beneficiaryID}: $e")
                return if (e is SocketTimeoutException) Result.retry() else Result.failure()
            }
        }

        return if (allSuccessful) Result.success() else Result.retry()
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken().isEmpty()) {
            preferenceDao.getAmritToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
        }
    }
}