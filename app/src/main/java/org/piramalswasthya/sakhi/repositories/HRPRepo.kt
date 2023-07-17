package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.model.*
import javax.inject.Inject

class HRPRepo @Inject constructor(
    private val database: InAppDb
)  {
    suspend fun getPregnantAssess(benId: Long): HRPPregnantAssessCache? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getPregnantAssess(benId)
        }
    }

    suspend fun saveRecord(hrpPregnantAssessCache: HRPPregnantAssessCache) {
        withContext(Dispatchers.IO){
            database.hrpDao.saveRecord(hrpPregnantAssessCache)
        }
    }

    suspend fun getNonPregnantAssess(benId: Long): HRPNonPregnantAssessCache? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getNonPregnantAssess(benId)
        }
    }

    suspend fun saveRecord(hrpNonPregnantAssessCache: HRPNonPregnantAssessCache) {
        withContext(Dispatchers.IO){
            database.hrpDao.saveRecord(hrpNonPregnantAssessCache)
        }
    }

    suspend fun getNonPregnantTrackList(benId: Long): List<HRPNonPregnantTrackCache>? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getNonPregnantTrackList(benId)
        }
    }

    suspend fun saveRecord(hrpNonPregnantTrackCache: HRPNonPregnantTrackCache) {
        withContext(Dispatchers.IO){
            database.hrpDao.saveRecord(hrpNonPregnantTrackCache)
        }
    }

    suspend fun getPregnantTrackList(benId: Long): List<HRPPregnantTrackCache>? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getPregnantTrackList(benId)
        }
    }

    suspend fun getHRPTrack(trackId: Long): HRPPregnantTrackCache? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getHRPTrack(trackId)
        }
    }

    suspend fun getHRPNonTrack(trackId: Long): HRPNonPregnantTrackCache? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getHRPNonTrack(trackId)
        }
    }

    suspend fun saveRecord(hrpPregnantTrackCache: HRPPregnantTrackCache) {
        withContext(Dispatchers.IO){
            database.hrpDao.saveRecord(hrpPregnantTrackCache)
        }
    }

    suspend fun getMicroBirthPlan(benId: Long): HRPMicroBirthPlanCache? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getMicroBirthPlan(benId)
        }
    }

    suspend fun saveRecord(hrpMicroBirthPlanCache: HRPMicroBirthPlanCache) {
        withContext(Dispatchers.IO){
            database.hrpDao.saveRecord(hrpMicroBirthPlanCache)
        }
    }

    suspend fun getAllPregTrack(): List<HRPPregnantTrackCache>? {
        return withContext(Dispatchers.IO){
            database.hrpDao.getAllPregTrack()
        }
    }

    suspend fun getHrPregTrackList(benId: Long): List<HRPPregnantTrackCache>? {
        return withContext(Dispatchers.IO){
            database.hrpDao.getAllPregTrackforBen(benId)
        }
    }

    suspend fun getAllNonPregTrack(): List<HRPNonPregnantTrackCache>? {
        return withContext(Dispatchers.IO){
            database.hrpDao.getAllNonPregTrack()
        }
    }

    suspend fun getHrNonPregTrackList(benId:Long) : List<HRPNonPregnantTrackCache>? {
        return withContext(Dispatchers.IO){
            database.hrpDao.getAllNonPregTrackforBen(benId)
        }
    }

    suspend fun getMaxLmp(benId: Long): Long? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getMaxLmp(benId)
        }
    }

    suspend fun getMaxDoV(benId: Long): Long? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getMaxDoV(benId)
        }
    }

    suspend fun getMaxDoVhrp(benId: Long): Long? {
        return withContext(Dispatchers.IO) {
            database.hrpDao.getMaxDoVhrp(benId)
        }
    }
}