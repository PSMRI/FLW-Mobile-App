package org.piramalswasthya.sakhi.database.room.dao

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * [BenDao] is a Room `@Dao` interface with many query methods that declare default
 * parameter values (e.g. `min: Int = Konstants.minAgeForNcd`). Kotlin generates a
 * synthetic `BenDao$DefaultImpls` bridge class to fill in omitted defaults at call
 * sites; that bridge code only runs when a method is actually invoked with one or
 * more trailing arguments omitted. Production call sites always pass every argument
 * explicitly, so this bridge was never exercised. These tests call every
 * default-parameter method on a relaxed mock with the optional arguments omitted,
 * which forces the real (non-mocked) `$DefaultImpls` bridge bytecode to run before
 * delegating to the mocked interface method.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BenDaoDefaultsTest {

    private val dao: BenDao = mockk(relaxed = true)

    @Test
    fun `suspend methods with default sync state omitted`() = runTest {
        dao.getAllUnprocessedBen()
        dao.getAllUnsyncedBen()
        dao.getAllBenForSyncWithServer()
        dao.getAllUnprocessedBen(SyncState.SYNCED)
        dao.updateToFinalBenId(hhId = 1L, oldId = 2L, newId = 3L, newBenRegId = 4L)
    }

    @Test
    fun `eligible couple flow methods with default age range omitted`() {
        dao.getAllEligibleCoupleList(selectedVillage = 1)
        dao.getAllEligibleTrackingList(selectedVillage = 1)
        dao.getAllEligibleRegistrationList(selectedVillage = 1)
        dao.getAllEligibleCoupleListCount(selectedVillage = 1)
    }

    @Test
    fun `non follow up count method with all defaults omitted`() {
        dao.getAllRegisteredPregnancyWomenNonFollowUpListCount(selectedVillage = 1)
    }

    @Test
    fun `ncd methods with default min age omitted`() {
        dao.getAllNCDList(selectedVillage = 1)
        dao.getAllNCDEligibleList(selectedVillage = 1)
        dao.getBenWithCbac(selectedVillage = 1)
        dao.getBenWithReferredCbac(selectedVillage = 1)
        dao.getReferredBenCount(selectedVillage = 1)
        dao.getBenWithCbacCount(selectedVillage = 1)
    }

    @Test
    fun `reproductive age list with default min max gender omitted`() {
        dao.getAllReproductiveAgeList(selectedVillage = 1)
    }

    @Test
    fun `infant and child age-bounded methods with defaults omitted`() {
        dao.getAllInfantList(selectedVillage = 1)
        dao.getAllInfantWithRchList(selectedVillage = 1)
        dao.getAllChildList(selectedVillage = 1)
        dao.getAllChildWithRchList(selectedVillage = 1)
        dao.getAllAdolescentList(selectedVillage = 1)
        dao.getAllChildrenImmunizationList(selectedVillage = 1)
    }

    @Test
    fun `cdr methods with default max age omitted`() {
        dao.getAllCDRList(selectedVillage = 1)
        dao.getAllCDRListCount(selectedVillage = 1)
    }

    @Test
    fun `low weight infant methods with default weight and age millis omitted`() {
        dao.getLowWeightBabiesCount(villageId = 1)
        dao.getListForLowWeightInfantRegister(villageId = 1)
    }
}
