package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService
import javax.inject.Inject

class ABHAGenratedRepo @Inject constructor(
    private val abhaGenratedDao: ABHAGenratedDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {
    suspend fun saveAbhaGenrated(abhaModel: ABHAModel) {
        withContext(Dispatchers.IO) {
            abhaGenratedDao.saveABHA(abhaModel)
        }
    }

    suspend fun deleteAbhaByBenId(benId: Long) {
        withContext(Dispatchers.IO) {
            abhaGenratedDao.deleteAbhaByBenId(benId)
        }
    }
}