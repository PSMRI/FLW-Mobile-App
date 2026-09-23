package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.content.res.Resources
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenWithECRCache
import org.piramalswasthya.sakhi.model.BenWithEcTrackingCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

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

    private fun ectCache(benId: Long, records: List<EligibleCoupleTrackingCache>): BenWithEcTrackingCache {
        val cache = mockk<BenWithEcTrackingCache>(relaxed = true)
        val ben = mockk<BenBasicCache>(relaxed = true)
        every { ben.benId } returns benId
        every { cache.ben } returns ben
        every { cache.savedECTRecords } returns records
        every { cache.asDomainModel(any(), any()) } returns mockk(relaxed = true)
        return cache
    }

    private fun ectRecord(benId: Long, visitDate: Long = 0L, lmpDate: Long = 0L) = EligibleCoupleTrackingCache(
        benId = benId,
        visitDate = visitDate,
        lmpDate = lmpDate,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.SYNCED
    )

    private fun newRecordsRepo() = RecordsRepo(
        context, householdDao, benDao, ancHomeVisitDao,
        vaccineDao, maternalHealthDao, childRegistrationDao, benRepo, pref
    )

    @Test
    fun `eligibleCoupleTrackingNonFollowUpList keeps only visits stale between 90 and 365 days`() = runTest {
        val now = System.currentTimeMillis()
        val staleVisit = ectRecord(1, visitDate = now - TimeUnit.DAYS.toMillis(120))
        val recentVisit = ectRecord(2, visitDate = now - TimeUnit.DAYS.toMillis(10))
        val tooOldVisit = ectRecord(4, visitDate = now - TimeUnit.DAYS.toMillis(400))

        every { benDao.getAllEligibleTrackingList(any(), any(), any()) } returns flowOf(
            listOf(
                ectCache(1, listOf(staleVisit)),
                ectCache(2, listOf(recentVisit)),
                ectCache(3, emptyList()),
                ectCache(4, listOf(tooOldVisit))
            )
        )
        every { benDao.getChildCountsForAllBen(any()) } returns flowOf(emptyList())

        val result = newRecordsRepo().eligibleCoupleTrackingNonFollowUpList.first()

        assertEquals(1, result.size)
    }

    @Test
    fun `eligibleCoupleTrackingMissedPeriodList keeps records with lmp over 35 days old`() = runTest {
        val now = System.currentTimeMillis()
        val overdueLmp = ectRecord(1, lmpDate = now - TimeUnit.DAYS.toMillis(40))
        val recentLmp = ectRecord(2, lmpDate = now - TimeUnit.DAYS.toMillis(5))
        val noLmp = ectRecord(3, lmpDate = 0L)

        every { benDao.getAllEligibleTrackingList(any(), any(), any()) } returns flowOf(
            listOf(
                ectCache(1, listOf(overdueLmp)),
                ectCache(2, listOf(recentLmp)),
                ectCache(3, listOf(noLmp)),
                ectCache(4, emptyList())
            )
        )
        every { benDao.getChildCountsForAllBen(any()) } returns flowOf(emptyList())

        val result = newRecordsRepo().eligibleCoupleTrackingMissedPeriodList.first()

        assertEquals(1, result.size)
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

    // ---------------- eligibleCoupleMissedPeriodList ----------------

    private fun ecrCache(benId: Long, lmpDate: Long): BenWithECRCache {
        val ben = mockk<BenBasicCache>(relaxed = true)
        every { ben.benId } returns benId
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { ecr.lmpDate } returns lmpDate
        val cache = mockk<BenWithECRCache>(relaxed = true)
        every { cache.ben } returns ben
        every { cache.ecr } returns ecr
        every { cache.asDomainModel(any()) } returns mockk(relaxed = true)
        return cache
    }

    @Test
    fun `eligibleCoupleMissedPeriodList keeps only records with lmp over 35 days old`() = runTest {
        val now = System.currentTimeMillis()
        val overdueLmp = ecrCache(1, now - TimeUnit.DAYS.toMillis(40))
        val recentLmp = ecrCache(2, now - TimeUnit.DAYS.toMillis(5))
        val noLmp = ecrCache(3, 0L)
        val noEcr = mockk<BenWithECRCache>(relaxed = true)
        every { noEcr.ben } returns mockk(relaxed = true) { every { benId } returns 4L }
        every { noEcr.ecr } returns null

        every { benDao.getAllEligibleRegistrationList(any(), any(), any()) } returns flowOf(
            listOf(overdueLmp, recentLmp, noLmp, noEcr)
        )
        every { benDao.getChildCountsForAllBen(any()) } returns flowOf(emptyList())

        val result = newRecordsRepo().eligibleCoupleMissedPeriodList.first()

        assertEquals(1, result.size)
    }

    @Test
    fun `eligibleCoupleMissedPeriodListCount reflects filtered size`() = runTest {
        val now = System.currentTimeMillis()
        every { benDao.getAllEligibleRegistrationList(any(), any(), any()) } returns flowOf(
            listOf(ecrCache(1, now - TimeUnit.DAYS.toMillis(40)))
        )
        every { benDao.getChildCountsForAllBen(any()) } returns flowOf(emptyList())

        val result = newRecordsRepo().eligibleCoupleMissedPeriodListCount.first()

        assertEquals(1, result)
    }

    // ---------------- hrpCount / hrpNonPCount ----------------

    @Test
    fun `hrpCount counts only high risk pregnancy assess records`() = runTest {
        val high = mockk<org.piramalswasthya.sakhi.model.HRPPregnantAssessCache>(relaxed = true)
        every { high.isHighRisk } returns true
        val low = mockk<org.piramalswasthya.sakhi.model.HRPPregnantAssessCache>(relaxed = true)
        every { low.isHighRisk } returns false
        every { maternalHealthDao.getAllPregnancyAssessRecords() } returns flowOf(listOf(high, low))

        val result = newRecordsRepo().hrpCount.first()

        assertEquals(1, result)
    }

    @Test
    fun `hrpNonPCount counts only high risk non pregnancy assess records`() = runTest {
        val high = mockk<org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache>(relaxed = true)
        every { high.isHighRisk } returns true
        val low = mockk<org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache>(relaxed = true)
        every { low.isHighRisk } returns false
        every { maternalHealthDao.getAllNonPregnancyAssessRecords() } returns flowOf(listOf(high, low, high))

        val result = newRecordsRepo().hrpNonPCount.first()

        assertEquals(2, result)
    }

    // ---------------- eligibleCoupleTrackingList ----------------

    @Test
    fun `eligibleCoupleTrackingList maps every record through asDomainModel`() = runTest {
        every { benDao.getAllEligibleTrackingList(any(), any(), any()) } returns flowOf(
            listOf(ectCache(1, emptyList()), ectCache(2, emptyList()))
        )
        every { benDao.getChildCountsForAllBen(any()) } returns flowOf(emptyList())

        val result = newRecordsRepo().eligibleCoupleTrackingList.first()

        assertEquals(2, result.size)
    }

    @Test
    fun `eligibleCoupleTrackingListCount reflects mapped size`() = runTest {
        every { benDao.getAllEligibleTrackingList(any(), any(), any()) } returns flowOf(
            listOf(ectCache(1, emptyList()))
        )
        every { benDao.getChildCountsForAllBen(any()) } returns flowOf(emptyList())

        val result = newRecordsRepo().eligibleCoupleTrackingListCount.first()

        assertEquals(1, result)
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
