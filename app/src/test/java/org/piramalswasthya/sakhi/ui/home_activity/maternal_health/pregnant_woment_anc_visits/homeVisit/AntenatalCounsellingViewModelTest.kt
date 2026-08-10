package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.homeVisit

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.ReferralStatusManager
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class AntenatalCounsellingViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FormRepository
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var infantRegRepo: InfantRegRepo
    @MockK private lateinit var referalRepo: NcdReferalRepo
    @MockK private lateinit var referralStatusManager: ReferralStatusManager
    @MockK private lateinit var context: Context

    private lateinit var viewModel: AntenatalCounsellingViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AntenatalCounsellingViewModel(repository, benRepo, infantRegRepo, referalRepo, referralStatusManager, context)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial schema is null`() {
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `initial visitCount is 0`() {
        assertEquals(0, viewModel.visitCount.value)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(AntenatalCounsellingViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `initial isBenDead is false`() {
        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `initial isSNCU is false`() {
        assertFalse(viewModel.isSNCU.value)
    }

    @Test
    fun `initial visitDay is empty`() {
        assertEquals("", viewModel.visitDay)
    }

    // =====================================================
    // setMotherAge() Tests
    // =====================================================

    @Test
    fun `setMotherAge does not throw`() {
        viewModel.setMotherAge(25)
    }

    // =====================================================
    // calculateDueDate() Tests
    // =====================================================

    @Test
    fun `calculateDueDate returns null for unknown visit month`() {
        assertNull(viewModel.calculateDueDate(System.currentTimeMillis(), "Unknown"))
    }

    @Test
    fun `calculateDueDate returns value for valid visit month`() {
        val result = viewModel.calculateDueDate(System.currentTimeMillis(), "3 Months")
        assertNotNull(result)
    }

    // =====================================================
    // formatDate() Tests
    // =====================================================

    @Test
    fun `formatDate returns formatted string`() {
        val result = viewModel.formatDate(0L)
        assertNotNull(result)
    }

    // =====================================================
    // getBabyAgeMonths() Tests
    // =====================================================

    @Test
    fun `getBabyAgeMonths returns positive for past dob`() {
        val pastDob = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        val result = viewModel.getBabyAgeMonths(pastDob)
        assert(result >= 11)
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields returns empty when no schema`() {
        assertEquals(0, viewModel.getVisibleFields().size)
    }

    // =====================================================
    // checkForReferralTriggers() Tests
    // =====================================================

    @Test
    fun `checkForReferralTriggers returns false for empty data`() {
        assertFalse(viewModel.checkForReferralTriggers(emptyMap()))
    }

    @Test
    fun `checkForReferralTriggers returns true when danger sign present`() {
        val data = mapOf("swelling" to "Yes")
        assert(viewModel.checkForReferralTriggers(data))
    }

    // =====================================================
    // getMinVisitDate() Tests
    // =====================================================

    @Test
    fun `getMinVisitDate returns null when no previous date`() {
        assertNull(viewModel.getMinVisitDate())
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue does not throw when no schema`() {
        viewModel.updateFieldValue("test", "value")
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun ancField(
        id: String,
        default: Any? = null,
        conditional: ConditionalLogic? = null,
        options: Any? = null
    ) = FormFieldDto(
        fieldId = id,
        label = id,
        type = "text",
        options = options,
        conditional = conditional,
        default = default
    )

    private fun ancSchema(vararg fields: FormFieldDto) = FormSchemaDto(
        formId = "ANC_01",
        formName = "Antenatal Counselling",
        version = 2,
        sections = listOf(
            FormSectionDto(sectionId = "s1", sectionTitle = "Section 1", fields = fields.toList())
        )
    )

    private fun ancVisit(
        visitDay: String = "1 Months",
        visitDate: String = "01-01-2024",
        formDataJson: String = """{"visitDate":"01-01-2024","fields":{}}"""
    ) = ANCFormResponseJsonEntity(
        benId = 10L,
        visitDay = visitDay,
        visitDate = visitDate,
        formId = "ANC_01",
        version = 1,
        formDataJson = formDataJson
    )

    private fun hbycVisit(
        visitDay: String,
        formDataJson: String = """{"fields":{"visit_date":"05-03-2024"}}"""
    ) = FormResponseJsonEntityHBYC(
        benId = 10L,
        hhId = 20L,
        visitDay = visitDay,
        visitDate = "05-03-2024",
        formId = "HBYC_01",
        version = 1,
        formDataJson = formDataJson
    )

    private fun stubAncSchema(
        dto: FormSchemaDto?,
        savedJson: String? = null,
        visits: List<ANCFormResponseJsonEntity> = emptyList(),
        cached: FormSchemaEntity? = null
    ) {
        coEvery { repository.getSyncedVisitsByRchIdANC(any()) } returns visits
        coEvery { repository.getSavedSchema(any()) } returns cached
        coEvery { repository.getFormSchema(any(), any()) } returns dto
        coEvery { repository.loadFormResponseJsonANC(any(), any()) } returns savedJson
    }

    private fun loadAncSchema(
        viewMode: Boolean = false,
        visitDay: String = "1 Months",
        visitNumberString: String = "Visit 1"
    ) {
        viewModel.loadFormSchema(10L, "ANC_01", visitDay, viewMode, "en", 1, visitNumberString)
    }

    private fun ancFieldOf(id: String): FormFieldDto =
        viewModel.schema.value!!.sections.flatMap { it.fields }.first { it.fieldId == id }

    private fun midnightToday(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    // =====================================================
    // Referral tracking Tests
    // =====================================================

    @Test
    fun `isReferralAlreadyDone returns false when not referred anywhere`() {
        every { referralStatusManager.isReferred(any(), any()) } returns false
        assertFalse(viewModel.isReferralAlreadyDone(AntenatalCounsellingViewModel.ReferralType.NCD))
    }

    @Test
    fun `isReferralAlreadyDone returns true when persisted in manager`() {
        every { referralStatusManager.isReferred(any(), any()) } returns true
        assertTrue(viewModel.isReferralAlreadyDone(AntenatalCounsellingViewModel.ReferralType.TB))
    }

    @Test
    fun `isReferralAlreadyDone returns true when marked in memory`() {
        every { referralStatusManager.isReferred(any(), any()) } returns false
        viewModel.markReferralCompleted(AntenatalCounsellingViewModel.ReferralType.HRP)
        assertTrue(viewModel.isReferralAlreadyDone(AntenatalCounsellingViewModel.ReferralType.HRP))
    }

    @Test
    fun `markReferralCompleted updates completedReferrals and persists`() {
        viewModel.markReferralCompleted(AntenatalCounsellingViewModel.ReferralType.COPD)
        assertTrue(
            viewModel.completedReferrals.value!!
                .contains(AntenatalCounsellingViewModel.ReferralType.COPD)
        )
        verify { referralStatusManager.markAsReferred(0L, "COPD") }
    }

    @Test
    fun `addReferral adds referral and sets referralCache`() {
        val referral = ReferalCache(
            benId = 10L,
            referralReason = "Anemia",
            syncState = SyncState.UNSYNCED
        )
        viewModel.addReferral(referral)

        assertEquals(1, viewModel.referralList.value!!.size)
        assertEquals(referral, viewModel.referralCache)
        verify { referralStatusManager.markAsReferred(0L, "MATERNAL") }
    }

    @Test
    fun `addReferral ignores duplicate referral reason`() {
        viewModel.addReferral(
            ReferalCache(benId = 10L, referralReason = "Anemia", syncState = SyncState.UNSYNCED)
        )
        viewModel.addReferral(
            ReferalCache(benId = 11L, referralReason = "Anemia", syncState = SyncState.UNSYNCED)
        )
        assertEquals(1, viewModel.referralList.value!!.size)
    }

    @Test
    fun `addReferral keeps referrals with different reasons`() {
        viewModel.addReferral(
            ReferalCache(benId = 10L, referralReason = "Anemia", syncState = SyncState.UNSYNCED)
        )
        viewModel.addReferral(
            ReferalCache(benId = 10L, referralReason = "High BP", syncState = SyncState.UNSYNCED)
        )
        assertEquals(2, viewModel.referralList.value!!.size)
    }

    @Test
    fun `showReferralDialog is initially false`() {
        assertFalse(viewModel.showReferralDialog.value!!)
    }

    @Test
    fun `referralList is initially empty`() {
        assertTrue(viewModel.referralList.value!!.isEmpty())
    }

    @Test
    fun `completedReferrals is initially empty`() {
        assertTrue(viewModel.completedReferrals.value!!.isEmpty())
    }

    @Test
    fun `checkForReferralTriggers matches lowercase yes`() {
        assertTrue(viewModel.checkForReferralTriggers(mapOf("high_bp" to "yes")))
    }

    @Test
    fun `checkForReferralTriggers returns false when all answers are No`() {
        val data = mapOf("swelling" to "No", "high_bp" to "No", "bleeding" to "No")
        assertFalse(viewModel.checkForReferralTriggers(data))
    }

    // =====================================================
    // Simple state Tests
    // =====================================================

    @Test
    fun `visitOrder contains nine months`() {
        assertEquals(9, viewModel.visitOrder.size)
        assertEquals("1 Months", viewModel.visitOrder.first())
    }

    @Test
    fun `visitMonth can be assigned`() {
        viewModel.visitMonth = "3 Months"
        assertEquals("3 Months", viewModel.visitMonth)
    }

    @Test
    fun `resetState posts IDLE state`() {
        viewModel.resetState()
        assertEquals(AntenatalCounsellingViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `getBabyAgeMonths returns zero for current date`() {
        assertEquals(0, viewModel.getBabyAgeMonths(System.currentTimeMillis()))
    }

    @Test
    fun `calculateDueDate supports all mapped visit months`() {
        val dob = System.currentTimeMillis()
        assertNotNull(viewModel.calculateDueDate(dob, "6 Months"))
        assertNotNull(viewModel.calculateDueDate(dob, "9 Months"))
        assertNotNull(viewModel.calculateDueDate(dob, "12 Months"))
        assertNotNull(viewModel.calculateDueDate(dob, "15 Months"))
    }

    // =====================================================
    // Visit list loading Tests
    // =====================================================

    @Test
    fun `loadSyncedVisitList updates list and invokes callback`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns listOf(ancVisit())
        var called = false

        viewModel.loadSyncedVisitList(10L) { called = true }
        advanceUntilIdle()

        assertEquals(1, viewModel.syncedVisitList.value.size)
        assertTrue(called)
    }

    @Test
    fun `loadSyncedVisitList works without a callback`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns listOf(ancVisit(), ancVisit())

        viewModel.loadSyncedVisitList(10L)
        advanceUntilIdle()

        assertEquals(2, viewModel.syncedVisitList.value.size)
    }

    @Test
    fun `loadLastVisitData selects the latest visit`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns listOf(
            ancVisit(visitDate = "01-01-2024"),
            ancVisit(visitDate = "05-02-2024")
        )

        viewModel.loadLastVisitData(10L)

        assertEquals("05-02-2024", viewModel.lastVisitData.value?.visitDate)
    }

    @Test
    fun `loadLastVisitData sets null when there are no visits`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns emptyList()

        viewModel.loadLastVisitData(10L)

        assertNull(viewModel.lastVisitData.value)
    }

    @Test
    fun `loadLastVisitData handles repository failure`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } throws RuntimeException("db down")

        viewModel.loadLastVisitData(10L)

        assertNull(viewModel.lastVisitData.value)
    }

    @Test
    fun `getLastVisitData returns the loaded visit`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns listOf(ancVisit())

        viewModel.loadLastVisitData(10L)

        assertEquals("01-01-2024", viewModel.getLastVisitData()?.visitDate)
    }

    @Test
    fun `getLastVisitDates returns NA when no visit exists`() = runTest {
        coEvery { repository.getLastVisitForBenANC(10L) } returns null
        assertEquals("NA", viewModel.getLastVisitDates(10L))
    }

    @Test
    fun `getLastVisitDates returns the stored visit date`() = runTest {
        coEvery { repository.getLastVisitForBenANC(10L) } returns ancVisit(visitDate = "12-12-2024")
        assertEquals("12-12-2024", viewModel.getLastVisitDates(10L))
    }

    // =====================================================
    // SNCU / death flags Tests
    // =====================================================

    @Test
    fun `fetchSNCUStatus sets true when infant is sncu`() = runTest {
        val record = mockk<InfantRegCache>(relaxed = true)
        every { record.isSNCU } returns "Yes"
        coEvery { infantRegRepo.getInfantReg(10L, 1) } returns record

        viewModel.fetchSNCUStatus(10L)
        advanceUntilIdle()

        assertTrue(viewModel.isSNCU.value)
    }

    @Test
    fun `fetchSNCUStatus sets false when infant record is missing`() = runTest {
        coEvery { infantRegRepo.getInfantReg(10L, 1) } returns null

        viewModel.fetchSNCUStatus(10L)
        advanceUntilIdle()

        assertFalse(viewModel.isSNCU.value)
    }

    @Test
    fun `checkIfBenDead sets true when repository reports dead`() = runTest {
        coEvery { benRepo.isBenDead(10L) } returns true

        viewModel.checkIfBenDead(10L)
        advanceUntilIdle()

        assertTrue(viewModel.isBenDead.value)
    }

    @Test
    fun `checkIfBenDead sets false when repository throws`() = runTest {
        coEvery { benRepo.isBenDead(10L) } throws RuntimeException("boom")

        viewModel.checkIfBenDead(10L)
        advanceUntilIdle()

        assertFalse(viewModel.isBenDead.value)
    }

    // =====================================================
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema populates schema and locks fixed fields`() = runTest {
        viewModel.setMotherAge(25)
        stubAncSchema(
            ancSchema(
                ancField("visit_day"),
                ancField("visit_number"),
                ancField("age_risk"),
                ancField("weight")
            )
        )

        loadAncSchema()
        advanceUntilIdle()

        assertNotNull(viewModel.schema.value)
        assertEquals("1 Months", ancFieldOf("visit_day").value)
        assertEquals("Visit 1", ancFieldOf("visit_number").value)
        assertEquals("No", ancFieldOf("age_risk").value)
        assertFalse(ancFieldOf("visit_day").isEditable)
        assertFalse(ancFieldOf("age_risk").isEditable)
        assertTrue(ancFieldOf("weight").isEditable)
    }

    @Test
    fun `loadFormSchema computes age risk for a young mother`() = runTest {
        viewModel.setMotherAge(16)
        stubAncSchema(ancSchema(ancField("age_risk")))

        loadAncSchema()
        advanceUntilIdle()

        assertEquals("Yes", ancFieldOf("age_risk").value)
    }

    @Test
    fun `loadFormSchema updates visit count from synced visits`() = runTest {
        stubAncSchema(
            ancSchema(ancField("weight")),
            visits = listOf(ancVisit(), ancVisit(visitDate = "02-01-2024"))
        )

        loadAncSchema()
        advanceUntilIdle()

        assertEquals(2, viewModel.visitCount.value)
    }

    @Test
    fun `loadFormSchema keeps schema null when none is available`() = runTest {
        stubAncSchema(null)

        loadAncSchema()
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
        assertEquals(0, viewModel.visitCount.value)
    }

    @Test
    fun `loadFormSchema applies saved field values`() = runTest {
        stubAncSchema(
            ancSchema(ancField("weight", default = "0"), ancField("hb")),
            savedJson = """{"fields":{"weight":"52","hb":"11.2"}}"""
        )

        loadAncSchema()
        advanceUntilIdle()

        assertEquals("52", ancFieldOf("weight").value)
        assertEquals("11.2", ancFieldOf("hb").value)
    }

    @Test
    fun `loadFormSchema falls back to defaults on malformed saved json`() = runTest {
        stubAncSchema(
            ancSchema(ancField("weight", default = "0")),
            savedJson = "{not-json"
        )

        loadAncSchema()
        advanceUntilIdle()

        assertEquals("0", ancFieldOf("weight").value)
    }

    @Test
    fun `loadFormSchema loads schema from the cached entity`() = runTest {
        val dto = ancSchema(ancField("weight", default = "0"))
        stubAncSchema(
            dto = null,
            cached = FormSchemaEntity(
                formId = "ANC_01",
                formName = "Antenatal Counselling",
                language = "en",
                version = 2,
                schemaJson = dto.toJson()
            )
        )

        loadAncSchema()
        advanceUntilIdle()

        assertEquals("ANC_01", viewModel.schema.value?.formId)
        assertEquals("0", ancFieldOf("weight").value)
    }

    @Test
    fun `loadFormSchema in view mode disables editable fields`() = runTest {
        stubAncSchema(ancSchema(ancField("weight")))

        loadAncSchema(viewMode = true)
        advanceUntilIdle()

        assertFalse(ancFieldOf("weight").isEditable)
    }

    @Test
    fun `loadFormSchema evaluates conditional visibility`() = runTest {
        stubAncSchema(
            ancSchema(
                ancField("has_symptom", default = "Yes"),
                ancField(
                    "symptom_detail",
                    conditional = ConditionalLogic("has_symptom", "Yes")
                ),
                ancField(
                    "other_detail",
                    conditional = ConditionalLogic("has_symptom", "No")
                )
            )
        )

        loadAncSchema()
        advanceUntilIdle()

        assertTrue(ancFieldOf("symptom_detail").visible)
        assertFalse(ancFieldOf("other_detail").visible)
    }

    @Test
    fun `loadFormSchema locks one time questions from the last visit`() = runTest {
        stubAncSchema(
            ancSchema(
                ancField("child_gap"),
                ancField("twin_pregnancy"),
                ancField("weight")
            ),
            visits = listOf(
                ancVisit(
                    formDataJson = """{"fields":{"child_gap":"Yes","twin_pregnancy":"No"}}"""
                )
            )
        )

        loadAncSchema()
        advanceUntilIdle()

        assertEquals("Yes", ancFieldOf("child_gap").value)
        assertFalse(ancFieldOf("child_gap").isEditable)
        assertEquals("No", ancFieldOf("twin_pregnancy").value)
        assertTrue(ancFieldOf("twin_pregnancy").isEditable)
        assertTrue(ancFieldOf("weight").isEditable)
    }

    @Test
    fun `loadFormSchema keeps one time questions editable when last visit has no answer`() =
        runTest {
            stubAncSchema(
                ancSchema(ancField("child_gap"), ancField("twin_pregnancy")),
                visits = listOf(ancVisit(formDataJson = """{"fields":{}}"""))
            )

            loadAncSchema()
            advanceUntilIdle()

            assertTrue(ancFieldOf("child_gap").isEditable)
            assertTrue(ancFieldOf("twin_pregnancy").isEditable)
        }

    @Test
    fun `loadFormSchema ignores last visit with blank form data`() = runTest {
        stubAncSchema(
            ancSchema(ancField("child_gap")),
            visits = listOf(ancVisit(formDataJson = ""))
        )

        loadAncSchema()
        advanceUntilIdle()

        assertNull(ancFieldOf("child_gap").value)
    }

    @Test
    fun `loadFormSchema recovers from invalid last visit json`() = runTest {
        stubAncSchema(
            ancSchema(ancField("weight")),
            visits = listOf(ancVisit(formDataJson = "###"))
        )

        loadAncSchema()
        advanceUntilIdle()

        assertTrue(ancFieldOf("weight").isEditable)
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue updates value and recomputes visibility`() = runTest {
        stubAncSchema(
            ancSchema(
                ancField("has_symptom", default = "No"),
                ancField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        loadAncSchema()
        advanceUntilIdle()
        assertFalse(ancFieldOf("symptom_detail").visible)

        viewModel.updateFieldValue("has_symptom", "Yes")

        assertEquals("Yes", ancFieldOf("has_symptom").value)
        assertTrue(ancFieldOf("symptom_detail").visible)
    }

    @Test
    fun `updateFieldValue autofills sncu field when baby is alive`() = runTest {
        val record = mockk<InfantRegCache>(relaxed = true)
        every { record.isSNCU } returns "Yes"
        coEvery { infantRegRepo.getInfantReg(10L, 1) } returns record
        viewModel.fetchSNCUStatus(10L)
        advanceUntilIdle()

        stubAncSchema(
            ancSchema(ancField("is_baby_alive"), ancField("discharged_from_sncu"))
        )
        loadAncSchema()
        advanceUntilIdle()

        viewModel.updateFieldValue("is_baby_alive", "Yes")

        assertEquals("Yes", ancFieldOf("discharged_from_sncu").value)
    }

    // =====================================================
    // saveFormResponses() Tests
    // =====================================================

    @Test
    fun `saveFormResponses does nothing when schema is null`() = runTest {
        viewModel.saveFormResponses(10L)
        advanceUntilIdle()

        assertEquals(AntenatalCounsellingViewModel.State.IDLE, viewModel.state.value)
        coVerify(exactly = 0) { repository.insertFormResponseANC(any()) }
    }

    @Test
    fun `saveFormResponses posts SUCCESS and inserts the entity`() = runTest {
        stubAncSchema(
            ancSchema(ancField("home_visit_date"), ancField("weight")),
            savedJson = """{"fields":{"home_visit_date":"10-05-2024","weight":"52"}}"""
        )
        viewModel.visitMonth = "1 Months"
        loadAncSchema()
        advanceUntilIdle()

        viewModel.saveFormResponses(10L)
        advanceUntilIdle()

        assertEquals(AntenatalCounsellingViewModel.State.SUCCESS, viewModel.state.value)
        coVerify { repository.insertFormResponseANC(any()) }
    }

    @Test
    fun `saveFormResponses posts FAIL when insert throws`() = runTest {
        stubAncSchema(
            ancSchema(ancField("home_visit_date")),
            savedJson = """{"fields":{"home_visit_date":"10-05-2024"}}"""
        )
        loadAncSchema()
        advanceUntilIdle()
        coEvery { repository.insertFormResponseANC(any()) } throws RuntimeException("insert failed")

        viewModel.saveFormResponses(10L)
        advanceUntilIdle()

        assertEquals(AntenatalCounsellingViewModel.State.FAIL, viewModel.state.value)
    }

    @Test
    fun `saveFormResponses stores pending referrals`() = runTest {
        stubAncSchema(
            ancSchema(ancField("home_visit_date")),
            savedJson = """{"fields":{"home_visit_date":"10-05-2024"}}"""
        )
        loadAncSchema()
        advanceUntilIdle()
        viewModel.addReferral(
            ReferalCache(benId = 10L, referralReason = "Anemia", syncState = SyncState.UNSYNCED)
        )

        viewModel.saveFormResponses(10L)
        advanceUntilIdle()

        coVerify { referalRepo.saveReferedNCD(any()) }
        assertTrue(
            viewModel.completedReferrals.value!!
                .contains(AntenatalCounsellingViewModel.ReferralType.MATERNAL)
        )
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields skips hidden fields`() = runTest {
        stubAncSchema(
            ancSchema(
                ancField("has_symptom", default = "No"),
                ancField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        loadAncSchema()
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(1, fields.size)
        assertEquals("has_symptom", fields.first().fieldId)
    }

    @Test
    fun `getVisibleFields maps options and conditional logic`() = runTest {
        stubAncSchema(
            ancSchema(
                ancField("has_symptom", default = "Yes", options = listOf("Yes", "No")),
                ancField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        loadAncSchema()
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(2, fields.size)
        assertEquals(2, fields.first { it.fieldId == "has_symptom" }.options?.size)
        assertNotNull(fields.first { it.fieldId == "symptom_detail" }.conditional)
    }

    // =====================================================
    // getVisitCardList() Tests
    // =====================================================

    @Test
    fun `getVisitCardList marks completed and next editable visits`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns listOf(
            ancVisit(
                visitDay = "3 Months",
                formDataJson = """{"visitDate":"01-01-2024","fields":{"is_baby_alive":"No"}}"""
            )
        )
        viewModel.loadSyncedVisitList(10L)
        advanceUntilIdle()
        val dob = Calendar.getInstance().apply { add(Calendar.MONTH, -10) }.timeInMillis

        val cards = viewModel.getVisitCardList(10L, dob)

        assertEquals(11, cards.size)
        val third = cards.first { it.visitDay == "3 Months" }
        assertTrue(third.isCompleted)
        assertEquals("01-01-2024", third.visitDate)
        assertTrue(third.isBabyDeath)
        assertTrue(cards.first { it.visitDay == "1 Months" }.isEditable)
        assertFalse(cards.first { it.visitDay == "12 Months" }.isEditable)
    }

    @Test
    fun `getVisitCardList returns all cards for a newborn`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns emptyList()
        viewModel.loadSyncedVisitList(10L)
        advanceUntilIdle()

        val cards = viewModel.getVisitCardList(10L, System.currentTimeMillis())

        assertEquals(11, cards.size)
        assertFalse(cards.any { it.isCompleted })
    }

    // =====================================================
    // Date boundary Tests
    // =====================================================

    @Test
    fun `getMaxVisitDate returns today when nothing is filled`() {
        assertEquals(midnightToday(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMaxVisitDate ignores malformed visit json`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdANC(10L) } returns listOf(
            ancVisit(formDataJson = "###")
        )
        viewModel.loadSyncedVisitList(10L)
        advanceUntilIdle()

        assertEquals(midnightToday(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate returns the day after the previous visit`() {
        val previous = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse("01-01-2024")!!
        viewModel.previousVisitDate = previous
        val expected = Calendar.getInstance().apply {
            time = previous
            add(Calendar.DATE, 1)
        }.time

        assertEquals(expected, viewModel.getMinVisitDate())
    }

    // =====================================================
    // HBYC last visit Tests
    // =====================================================

    @Test
    fun `getLastVisitDay returns the latest hbyc visit day`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(10L) } returns listOf(
            hbycVisit("2 Months"),
            hbycVisit("5 Months"),
            hbycVisit("Unknown")
        )

        assertEquals("5 Months", viewModel.getLastVisitDay(10L))
    }

    @Test
    fun `getLastVisitDay returns null when no known visit day`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(10L) } returns listOf(hbycVisit("Unknown"))

        assertNull(viewModel.getLastVisitDay(10L))
    }

    @Test
    fun `getLastVisitDate parses the visit date from hbyc json`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(10L) } returns listOf(hbycVisit("5 Months"))
        val expected = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse("05-03-2024")

        assertEquals(expected, viewModel.getLastVisitDate(10L))
    }

    @Test
    fun `getLastVisitDate returns null for malformed hbyc json`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(10L) } returns listOf(
            hbycVisit("5 Months", formDataJson = "###")
        )

        assertNull(viewModel.getLastVisitDate(10L))
    }

    @Test
    fun `loadVisitDates populates previous visit fields`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(10L) } returns listOf(hbycVisit("5 Months"))

        viewModel.loadVisitDates(10L)
        advanceUntilIdle()

        assertEquals("5 Months", viewModel.lastVisitDay)
        assertNotNull(viewModel.previousVisitDate)
    }
}
