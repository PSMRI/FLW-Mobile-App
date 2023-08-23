package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.model.ChildRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.InfantRegCache
import javax.inject.Inject

class ChildRegRepo @Inject constructor(
    private val childRegDao: ChildRegistrationDao,
    private val deliveryOutcomeDao: DeliveryOutcomeDao,
    private val infantRegDao: InfantRegDao
) {

    suspend fun getChildReg(benId: Long): ChildRegCache? {
        return withContext(Dispatchers.IO) {
            childRegDao.getInfantReg(benId)
        }
    }

    suspend fun saveChildReg(child: ChildRegCache) {
        withContext(Dispatchers.IO) {
            childRegDao.saveInfantReg(child)
        }
    }

    suspend fun getDeliveryOutcomeRepoFromMotherBenId(motherBenId: Long): DeliveryOutcomeCache? {
        return withContext(Dispatchers.IO) {
            deliveryOutcomeDao.getDeliveryOutcome(motherBenId)
        }
    }
    suspend fun getInfantRegFromMotherBenId(motherBenId: Long): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantReg(motherBenId)
        }
    }
}