package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FPOTCache
import timber.log.Timber
import javax.inject.Inject

class FpotRepo @Inject constructor(
    private val database: InAppDb,
    private val preferenceDao: PreferenceDao
) {

    suspend fun saveFpotData(fpotCache: FPOTCache): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                fpotCache.apply {
                    createdBy = user.userName
                    createdDate = System.currentTimeMillis()
                }

                database.fpotDao.upsert(fpotCache)

                true
            } catch (e: Exception) {
                Timber.e("Error : $e raised at saveCdrData")
                false
            }
        }
    }
}