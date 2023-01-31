package org.piramalswasthya.sakhi.repositories

import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.BenKidRegFormDataset
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.network.TmcNetworkApiService
import timber.log.Timber
import javax.inject.Inject

class BenRepo @Inject constructor(
    private val database: InAppDb,
    private val tmcNetworkApiService: TmcNetworkApiService
) {

    val benList by lazy {
        //TODO(implement BenDao)
        Transformations.map(database.benDao.getAllBen()) { list ->
            list.map { it.asBasicDomainModel() }
        }
    }

    suspend fun getDraftForm(hhId: Long, isKid: Boolean): BenRegCache? {
        return withContext(Dispatchers.IO) {
            if(isKid)
                database.benDao.getDraftBenKidForHousehold(hhId)
            else
                null
        }
    }


    suspend fun persistFirstPage(form: BenKidRegFormDataset, hhId: Long) {
        Timber.d("Persisting first page!")
        val user =
            database.userDao.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
        val ben = form.getBenForFirstPage(user.userId, hhId)
        database.benDao.upsert(ben)
    }

    suspend fun persistSecondPage(form: BenKidRegFormDataset, locationRecord: LocationRecord) {
//        val draftBen = database.benDao.getDraftBenKidForHousehold(hhId)
//            ?: throw IllegalStateException("no draft saved!!")
        val user =
            database.userDao.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
        val ben =
            form.getBenForSecondPage()

        ben.apply {
            if(this.createdDate==null) {
                this.createdDate = System.currentTimeMillis()
                this.createdBy = user.userName
            }
            else{
                this.updatedDate = System.currentTimeMillis()
                this.updatedBy = user.userName
            }
            this.villageName = locationRecord.village
            this.countyId = locationRecord.countryId
            this.stateId = locationRecord.stateId
            this.districtId = locationRecord.districtId
            this.villageId = locationRecord.villageId
        }

        database.benDao.upsert(ben)
        return
    }

    suspend fun getBenHousehold(hhId: Long): HouseholdCache {
        return database.householdDao.getHousehold(hhId)

    }

    private suspend fun extractBenId(){


    }

    suspend fun genBen(count : Int = 300){

    }

    suspend fun deleteBenDraft(hhId: Long, isKid: Boolean) {
        withContext(Dispatchers.IO){
            database.benDao.deleteBen(hhId,isKid)
        }
    }

}