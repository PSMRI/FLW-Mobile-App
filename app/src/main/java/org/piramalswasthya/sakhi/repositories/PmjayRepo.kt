package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PMJAYCache
import org.piramalswasthya.sakhi.model.PMJAYPost
import timber.log.Timber
import javax.inject.Inject

class PmjayRepo @Inject constructor(
    private val database: InAppDb,
    private val preferenceDao: PreferenceDao
) {

    suspend fun savePmjayData(pmjayCache: PMJAYCache): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                pmjayCache.apply {
                    createdBy = user.userName
                    createdDate = System.currentTimeMillis()
                }

                database.pmjayDao.upsert(pmjayCache)

                true
            } catch (e: Exception) {
                Timber.e("Error : $e raised at saveCdrData")
                false
            }
        }
    }

    suspend fun processNewPmjay(): Boolean {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val pmjayList = database.pmjayDao.getAllUnprocessedPMJAY()

            val pmjayPostList = mutableSetOf<PMJAYPost>()

            pmjayList.forEach {
                pmjayPostList.clear()
                val household =
                    database.householdDao.getHousehold(it.hhId)
                        ?: throw IllegalStateException("No household exists for hhId: ${it.hhId}!!")
                val ben =
                    database.benDao.getBen(it.hhId, it.benId)
                        ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
                val mdsrCount = database.mdsrDao.mdsrCount()
                pmjayPostList.add(it.asPostModel(user, household, ben, mdsrCount))
//                val uploadDone = postDataToD2dServer(pmjayPostList)
//                if(uploadDone) {
//                    it.processed = "P"
//                    database.pmjayDao.setSynced(it)
//                }
            }

            return@withContext true
        }
    }
}