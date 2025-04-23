package org.piramalswasthya.sakhi.repositories
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.network.AmritApiService
import javax.inject.Inject

@ActivityRetainedScoped
class VLFRepo @Inject constructor(
    private val database: InAppDb,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService,
    private val vlfDao: VLFDao

) {

    suspend fun getVHND(id: Int): VHNDCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getVHND(id)
        //    suspend fun getAllVHND(): Flow<List<VHNDCache>> {
//        return withContext(Dispatchers.IO) {
//            vlfDao.getAllVHND()
//        }
//    }

        }
    }

    suspend fun saveRecord(vhndCache: VHNDCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(vhndCache)
        }
    }
    var vhndList = vlfDao.getAllVHND()
        .map { list -> list.map { it.toVhndDTODTO()} }

    suspend fun getVHNC(id: Int): VHNCCache? {
        return withContext(Dispatchers.IO) {
            database.vlfDao.getVHNC(id)
            //    suspend fun getAllVHND(): Flow<List<VHNDCache>> {
//        return withContext(Dispatchers.IO) {
//            vlfDao.getAllVHND()
//        }
//    }

        }
    }

    suspend fun saveRecord(vhncCache: VHNCCache) {
        withContext(Dispatchers.IO) {
            database.vlfDao.saveRecord(vhncCache)
        }
    }
    var vhncList = vlfDao.getAllVHNC()
        .map { list -> list.map { it.toVhncDTODTO()} }


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
        .map { list -> list.map { it.toPHCDTODTO()} }

}