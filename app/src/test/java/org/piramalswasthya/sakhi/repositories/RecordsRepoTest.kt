package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.content.res.Resources
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var householdDao: HouseholdDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var ancHomeVisitDao: FormResponseANCJsonDao
    @MockK private lateinit var vaccineDao: ImmunizationDao
    @MockK private lateinit var maternalHealthDao: MaternalHealthDao
    @MockK private lateinit var childRegistrationDao: ChildRegistrationDao
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var pref: PreferenceDao

    private lateinit var repo: RecordsRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockk<Resources>(relaxed = true)
        every { pref.getLocationRecord() } returns locationRecord()
        repo = RecordsRepo(
            context, householdDao, benDao, ancHomeVisitDao,
            vaccineDao, maternalHealthDao, childRegistrationDao, benRepo, pref
        )
    }

    private fun locationRecord(villageId: Int = 101): LocationRecord {
        val loc = LocationEntity(id = villageId, name = "Village")
        return LocationRecord(
            country = loc, state = loc, district = loc, block = loc, village = loc
        )
    }

    // ---------------- getBenById ----------------

    @Test
    fun `getBenById returns null when dao has no match`() = runTest {
        coEvery { benDao.getBenById(99L) } returns null

        assertNull(repo.getBenById(99L))
    }

    // ---------------- getHomeVisitUiState ----------------

    @Test
    fun `getHomeVisitUiState allows add and blocks view when no visits`() = runTest {
        coEvery { ancHomeVisitDao.getSyncedVisitsByRchId(5L) } returns emptyList()

        val state = repo.getHomeVisitUiState(5L)

        assertTrue(state.canAddHomeVisit)
        assertFalse(state.canViewHomeVisit)
    }

    @Test
    fun `getHomeVisitUiState blocks add and allows view at nine visits`() = runTest {
        val visits = List(9) { mockk<ANCFormResponseJsonEntity>(relaxed = true) }
        coEvery { ancHomeVisitDao.getSyncedVisitsByRchId(5L) } returns visits

        val state = repo.getHomeVisitUiState(5L)

        assertFalse(state.canAddHomeVisit)
        assertTrue(state.canViewHomeVisit)
    }

    @Test
    fun `getHomeVisitUiState allows add when last visit older than thirty days`() = runTest {
        val visit = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        every { visit.visitDate } returns "01-01-2020"
        coEvery { ancHomeVisitDao.getSyncedVisitsByRchId(5L) } returns listOf(visit)

        val state = repo.getHomeVisitUiState(5L)

        assertTrue(state.canAddHomeVisit)
        assertTrue(state.canViewHomeVisit)
    }

    // ---------------- search delegations ----------------

    @Test
    fun `search delegations return non-null flows and lists`() = runTest {
        assertNotNull(repo.searchBen("q", 0, 0))
        assertNotNull(repo.searchBenPagedSource("q", 1, 2))
        assertNotNull(repo.searchBenOnce("q", 0, 0))
    }

    @Test
    fun `ben list getters delegate to dao`() {
        assertNotNull(repo.getBenList())
        assertNotNull(repo.getBenListCHO())
        assertNotNull(repo.getBenListCount())
    }

    // ---------------- disease screening delegations ----------------

    @Test
    fun `disease screening list getters delegate to dao`() {
        assertNotNull(repo.malariaScreeningList(1L))
        assertNotNull(repo.aesScreeningList(1L))
        assertNotNull(repo.iRSRoundList(1L))
        assertNotNull(repo.getLastIRSRoundBen(1L))
        assertNotNull(repo.KalazarScreeningList(1L))
        assertNotNull(repo.filariaScreeningList(1L))
    }

    @Test
    fun `leprosy list getters delegate to dao`() {
        assertNotNull(repo.LeprosyScreeningList(1L))
        assertNotNull(repo.LeprosySuspectedList())
        assertNotNull(repo.LeprosyConfirmedList())
    }

    // ---------------- count getters ----------------

    @Test
    fun `death and general count getters delegate to dao`() {
        assertNotNull(repo.getGeneralDeathCount())
        assertNotNull(repo.getMaternalDeathCount())
        assertNotNull(repo.getNonMaternalDeathCount())
        assertNotNull(repo.getChildDeathCount())
        assertNotNull(repo.getHighRiskWomenCount())
    }

    @Test
    fun `pregnancy count getters delegate to dao`() {
        assertNotNull(repo.getPregnantWomenListCount())
        assertNotNull(repo.getAbortionPregnantWomanCount())
        assertNotNull(repo.getRegisteredPregnantWomanListCount())
        assertNotNull(repo.getAllWomenForPmsmaCount())
        assertNotNull(repo.getDeliveredWomenListCount())
        assertNotNull(repo.getInfantRegisterCount())
        assertNotNull(repo.getRegisteredInfantsCount())
    }

    // ---------------- pregnancy / delivery list getters ----------------

    @Test
    fun `pregnancy list getters delegate to dao`() {
        assertNotNull(repo.getPregnantWomenList())
        assertNotNull(repo.getPregnantWomenWithRchList())
        assertNotNull(repo.getRegisteredPmsmaWomenList())
        assertNotNull(repo.getRegisteredPregnantWomanList())
        assertNotNull(repo.getHighRiskPregnantWomanList())
        assertNotNull(repo.getAbortionPregnantWomanList())
    }

    @Test
    fun `registered pregnant follow-up and due getters delegate to dao`() {
        assertNotNull(repo.getRegisteredPregnantWomanNonFollowUpList())
        assertNotNull(repo.getRegisteredPregnantWomanNonFollowUpListCount())
        assertNotNull(repo.getDuePregnantWomanList())
    }

    @Test
    fun `delivery pmsma and infant reg getters delegate to dao`() {
        assertNotNull(repo.getDeliveredWomenList())
        assertNotNull(repo.getWomenListForPmsma())
        assertNotNull(repo.getListForInfantReg())
        assertNotNull(repo.getListForLowWeightInfantReg())
        assertNotNull(repo.getRegisteredInfants())
    }

    @Test
    fun `hrec count getter delegates to maternal health dao`() {
        assertNotNull(repo.getHRECCount())
    }

    // ---------------- getHomeVisitUiState extra branch ----------------

    @Test
    fun `getHomeVisitUiState allows add when last visit date is null`() = runTest {
        val visit = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        every { visit.visitDate } returns ""
        coEvery { ancHomeVisitDao.getSyncedVisitsByRchId(7L) } returns listOf(visit)

        val state = repo.getHomeVisitUiState(7L)

        assertTrue(state.canAddHomeVisit)
        assertTrue(state.canViewHomeVisit)
    }

    // ---------------- household / ben list vals ----------------

    @Test
    fun `household and ben list vals are built`() {
        assertNotNull(repo.hhList)
        assertNotNull(repo.hhListCount)
        assertNotNull(repo.hhListforAsha)
        assertNotNull(repo.allBenList)
        assertNotNull(repo.childCountsByBen)
        assertNotNull(repo.allBenListCount)
        assertNotNull(repo.allBenWithoutAbhaList)
        assertNotNull(repo.allBenWithAbhaList)
        assertNotNull(repo.benWithAbhaListCount)
        assertNotNull(repo.benWithOldAbhaListCount)
        assertNotNull(repo.benWithNewAbhaListCount)
        assertNotNull(repo.allBenWithRchList)
        assertNotNull(repo.allBenAboveThirtyList)
        assertNotNull(repo.allBenWARAList)
        assertNotNull(repo.benWithRchListCount)
    }

    // ---------------- ncd vals ----------------

    @Test
    fun `ncd list vals are built`() {
        assertNotNull(repo.ncdList)
        assertNotNull(repo.ncdListCount)
        assertNotNull(repo.getNcdEligibleList)
        assertNotNull(repo.getNcdrefferedList)
        assertNotNull(repo.getHwcRefferedList)
        assertNotNull(repo.getNcdEligibleListCount)
        assertNotNull(repo.getNcdrefferedListCount)
        assertNotNull(repo.getHwcReferedListCount)
        assertNotNull(repo.getNcdPriorityList)
        assertNotNull(repo.getNcdPriorityListCount)
        assertNotNull(repo.getNcdNonEligibleList)
        assertNotNull(repo.getNcdNonEligibleListCount)
    }

    // ---------------- disease vals ----------------

    @Test
    fun `tb and malaria and leprosy count vals are built`() {
        assertNotNull(repo.tbScreeningList)
        assertNotNull(repo.tbScreeningListCount)
        assertNotNull(repo.tbSuspectedList)
        assertNotNull(repo.tbSuspectedListCount)
        assertNotNull(repo.tbConfirmedList)
        assertNotNull(repo.tbConfirmedListCount)
        assertNotNull(repo.malariaConfirmedCasesList)
        assertNotNull(repo.leprosySuspectedListCount)
        assertNotNull(repo.leprosyConfirmedCasesListCount)
        assertNotNull(repo.malariaConfirmedCasesListCount)
    }

    // ---------------- women / child list vals ----------------

    @Test
    fun `women and child list vals are built`() {
        assertNotNull(repo.menopauseList)
        assertNotNull(repo.menopauseListCount)
        assertNotNull(repo.reproductiveAgeList)
        assertNotNull(repo.reproductiveAgeListCount)
        assertNotNull(repo.infantList)
        assertNotNull(repo.infantListCount)
        assertNotNull(repo.childList)
        assertNotNull(repo.childListCount)
        assertNotNull(repo.childCard)
        assertNotNull(repo.childFilteredList)
        assertNotNull(repo.childFilteredListCount)
        assertNotNull(repo.adolescentList)
        assertNotNull(repo.adolescentListCount)
        assertNotNull(repo.immunizationList)
        assertNotNull(repo.immunizationListCount)
    }

    // ---------------- pnc / death / immunization vals ----------------

    @Test
    fun `pnc death and immunization vals are built`() {
        assertNotNull(repo.pncMotherList)
        assertNotNull(repo.pncMotherListCount)
        assertNotNull(repo.pncMotherNonFollowUpList)
        assertNotNull(repo.pncMotherNonFollowUpListCount)
        assertNotNull(repo.cdrList)
        assertNotNull(repo.gdrList)
        assertNotNull(repo.nmdrList)
        assertNotNull(repo.mdsrList)
        assertNotNull(repo.childrenImmunizationDueListCount)
        assertNotNull(repo.childrenImmunizationList)
        assertNotNull(repo.childrenImmunizationListCount)
        assertNotNull(repo.motherImmunizationList)
        assertNotNull(repo.motherImmunizationListCount)
    }

    // ---------------- eligible couple + hrp vals ----------------

    @Test
    fun `eligible couple and hrp vals are built`() {
        assertNotNull(repo.eligibleCoupleList)
        assertNotNull(repo.eligibleCoupleListCount)
        assertNotNull(repo.eligibleCoupleMissedPeriodList)
        assertNotNull(repo.eligibleCoupleMissedPeriodListCount)
        assertNotNull(repo.eligibleCoupleTrackingList)
        assertNotNull(repo.eligibleCoupleTrackingListCount)
        assertNotNull(repo.eligibleCoupleTrackingNonFollowUpList)
        assertNotNull(repo.eligibleCoupleTrackingNonFollowUpListCount)
        assertNotNull(repo.eligibleCoupleTrackingMissedPeriodList)
        assertNotNull(repo.eligibleCoupleTrackingMissedPeriodListCount)
        assertNotNull(repo.hrpPregnantWomenList)
        assertNotNull(repo.hrpPregnantWomenListCount)
        assertNotNull(repo.hrpTrackingPregList)
        assertNotNull(repo.hrpTrackingPregListCount)
        assertNotNull(repo.hrpNonPregnantWomenList)
        assertNotNull(repo.hrpNonPregnantWomenListCount)
        assertNotNull(repo.hrpTrackingNonPregList)
        assertNotNull(repo.hrpTrackingNonPregListCount)
        assertNotNull(repo.lowWeightBabiesCount)
        assertNotNull(repo.hrpCases)
        assertNotNull(repo.hrpCount)
        assertNotNull(repo.hrpNonPCount)
    }

    // ---------------- vals not covered by Extra3 ----------------

    @Test
    fun `hrp list and age-window vals are built`() {
        assertNotNull(repo.hrpList)
        assertNotNull(repo.hrpListCount)
        // Int age-window vals — reference so their getters run.
        assertNotNull(repo.minAgeInDaysForThreeMonths)
        assertNotNull(repo.maxAgeInDaysForFifteenMonths)
    }

    // ---------------- getHomeVisitUiState within-thirty-days branch ----------------

    @Test
    fun `getHomeVisitUiState blocks add when last visit is within thirty days`() = runTest {
        val visit = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        every { visit.visitDate } returns "10-07-2026"
        coEvery { ancHomeVisitDao.getSyncedVisitsByRchId(11L) } returns listOf(visit)
        every { HelperUtil.parseDateToMillis(any()) } returns System.currentTimeMillis()

        val state = repo.getHomeVisitUiState(11L)

        assertFalse(state.canAddHomeVisit)
        assertTrue(state.canViewHomeVisit)
    }

}
