package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.model.ABHAModel
import javax.inject.Inject

class ABHAGenratedRepo @Inject constructor(
    private val abhaGenratedDao: ABHAGenratedDao,
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