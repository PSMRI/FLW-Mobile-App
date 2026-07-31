package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.filaria_mda

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
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
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMdaCampaignJsonDao
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.MDACampaignItem
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FilariaMdaCampaignRepository

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMdaFormCampaignViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FilariaMdaCampaignRepository
    @MockK private lateinit var context: Context
    @MockK private lateinit var dao: FilariaMdaCampaignJsonDao

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: FilariaMdaFormCampaignViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FilariaMdaFormCampaignViewModel(repository, context, savedStateHandle, dao)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `schema is initially null`() {
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `getVisibleFields returns empty when schema null`() {
        assertTrue(viewModel.getVisibleFields().isEmpty())
    }

    @Test
    fun `getCurrentYear returns four digit year`() {
        assertEquals(4, viewModel.getCurrentYear().length)
    }

    @Test
    fun `yearDate uses default empty value`() {
        assertEquals("", viewModel.yearDate)
    }

    @Test
    fun `isCampaignAlreadyAdded is not null`() {
        assertNotNull(viewModel.isCampaignAlreadyAdded)
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun campaignField(
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

    private fun campaignSchema(vararg fields: FormFieldDto) = FormSchemaDto(
        formId = "MDAC_01",
        formName = "Filaria MDA Campaign",
        version = 2,
        sections = listOf(
            FormSectionDto(sectionId = "s1", sectionTitle = "Section 1", fields = fields.toList())
        )
    )

    private fun campaignEntity() = FilariaMDACampaignFormResponseJsonEntity(
        visitDate = "01-05-2024",
        visitYear = "2024",
        formId = "MDAC_01",
        version = 1,
        formDataJson = """{"fields":{}}"""
    )

    private fun stubCampaign(
        dto: FormSchemaDto?,
        savedJson: String? = null,
        visits: List<FilariaMDACampaignFormResponseJsonEntity> = emptyList(),
        cached: FormSchemaEntity? = null
    ) {
        coEvery { repository.getSyncedVisitsByRchId() } returns visits
        coEvery { repository.getSavedSchema(any()) } returns cached
        coEvery { repository.getFormSchema(any()) } returns dto
        coEvery { repository.loadFormResponseJson(any()) } returns savedJson
    }

    private fun campaignFieldOf(id: String): FormFieldDto =
        viewModel.schema.value!!.sections.flatMap { it.fields }.first { it.fieldId == id }

    // =====================================================
    // Default state Tests
    // =====================================================

    @Test
    fun `infant is initially null`() {
        assertNull(viewModel.infant.value)
    }

    @Test
    fun `isBenDead is initially false`() {
        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `visitDay defaults to empty`() {
        assertEquals("", viewModel.visitDay)
    }

    @Test
    fun `isViewMode defaults to false`() {
        assertFalse(viewModel.isViewMode)
    }

    @Test
    fun `wasDuplicate defaults to false`() {
        assertFalse(viewModel.wasDuplicate)
    }

    @Test
    fun `previousVisitDate is initially null`() {
        assertNull(viewModel.previousVisitDate)
    }

    @Test
    fun `updateFieldValue does nothing when schema is null`() {
        viewModel.updateFieldValue("start_date", "01-05-2024")
        assertNull(viewModel.schema.value)
    }

    // =====================================================
    // Campaign lookup Tests
    // =====================================================

    @Test
    fun `checkCurrentYearCampaign posts true when a campaign exists`() = runTest {
        coEvery { dao.getCampaignByBenFormYear(any(), any()) } returns campaignEntity()

        viewModel.checkCurrentYearCampaign("MDAC_01")
        advanceUntilIdle()

        assertTrue(viewModel.isCampaignAlreadyAdded.value!!)
    }

    @Test
    fun `checkCurrentYearCampaign posts false when no campaign exists`() = runTest {
        coEvery { dao.getCampaignByBenFormYear(any(), any()) } returns null

        viewModel.checkCurrentYearCampaign("MDAC_01")
        advanceUntilIdle()

        assertFalse(viewModel.isCampaignAlreadyAdded.value!!)
    }

    @Test
    fun `loadBottleData posts the repository campaign items`() = runTest {
        val items = listOf(MDACampaignItem(1, "01-05-2024", "10-05-2024", "10", "40"))
        coEvery { repository.getBottleList() } returns items

        viewModel.loadBottleData()
        advanceUntilIdle()

        assertEquals(items, viewModel.bottleList.value)
    }

    @Test
    fun `loadSyncedVisitList queries the repository`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId() } returns listOf(campaignEntity())

        viewModel.loadSyncedVisitList()
        advanceUntilIdle()

        coVerify { repository.getSyncedVisitsByRchId() }
    }

    // =====================================================
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema populates the schema and locks fixed fields`() = runTest {
        viewModel.visitDay = "Day 1"
        stubCampaign(
            campaignSchema(
                campaignField("visit_day"),
                campaignField("due_date"),
                campaignField("start_date")
            )
        )

        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        assertNotNull(viewModel.schema.value)
        assertEquals("Day 1", campaignFieldOf("visit_day").value)
        assertFalse(campaignFieldOf("visit_day").isEditable)
        assertFalse(campaignFieldOf("due_date").isEditable)
        assertTrue(campaignFieldOf("start_date").isEditable)
    }

    @Test
    fun `loadFormSchema keeps schema null when nothing is available`() = runTest {
        stubCampaign(null)

        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema loads the schema from the cached entity`() = runTest {
        val dto = campaignSchema(campaignField("start_date", default = "01-05-2024"))
        stubCampaign(
            dto = null,
            cached = FormSchemaEntity(
                formId = "MDAC_01",
                formName = "Filaria MDA Campaign",
                language = "en",
                version = 2,
                schemaJson = dto.toJson()
            )
        )

        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("MDAC_01", viewModel.schema.value?.formId)
        assertEquals("01-05-2024", campaignFieldOf("start_date").value)
    }

    @Test
    fun `loadFormSchema applies saved field values`() = runTest {
        stubCampaign(
            campaignSchema(campaignField("start_date"), campaignField("end_date")),
            savedJson = """{"fields":{"start_date":"01-05-2024","end_date":"10-05-2024"}}"""
        )

        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("01-05-2024", campaignFieldOf("start_date").value)
        assertEquals("10-05-2024", campaignFieldOf("end_date").value)
    }

    @Test
    fun `loadFormSchema falls back to defaults on malformed saved json`() = runTest {
        stubCampaign(
            campaignSchema(campaignField("start_date", default = "01-01-2024")),
            savedJson = "{not-json"
        )

        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("01-01-2024", campaignFieldOf("start_date").value)
    }

    @Test
    fun `loadFormSchema in view mode disables fields`() = runTest {
        stubCampaign(campaignSchema(campaignField("start_date")))

        viewModel.loadFormSchema("MDAC_01", viewMode = true)
        advanceUntilIdle()

        assertFalse(campaignFieldOf("start_date").isEditable)
        assertTrue(viewModel.isViewMode)
    }

    @Test
    fun `loadFormSchema evaluates conditional visibility`() = runTest {
        stubCampaign(
            campaignSchema(
                campaignField("has_symptom", default = "Yes"),
                campaignField(
                    "symptom_detail",
                    conditional = ConditionalLogic("has_symptom", "Yes")
                ),
                campaignField(
                    "other_detail",
                    conditional = ConditionalLogic("has_symptom", "No")
                )
            )
        )

        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        assertTrue(campaignFieldOf("symptom_detail").visible)
        assertFalse(campaignFieldOf("other_detail").visible)
    }

    // =====================================================
    // updateFieldValue() / saveFormResponses() Tests
    // =====================================================

    @Test
    fun `updateFieldValue updates value and recomputes visibility`() = runTest {
        stubCampaign(
            campaignSchema(
                campaignField("has_symptom", default = "No"),
                campaignField(
                    "symptom_detail",
                    conditional = ConditionalLogic("has_symptom", "Yes")
                )
            )
        )
        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()
        assertFalse(campaignFieldOf("symptom_detail").visible)

        viewModel.updateFieldValue("has_symptom", "Yes")

        assertEquals("Yes", campaignFieldOf("has_symptom").value)
        assertTrue(campaignFieldOf("symptom_detail").visible)
    }

    @Test
    fun `saveFormResponses returns false when schema is null`() = runTest {
        assertFalse(viewModel.saveFormResponses(1L, 2L))
    }

    @Test
    fun `saveFormResponses flags a duplicate submission`() = runTest {
        stubCampaign(
            campaignSchema(campaignField("start_date")),
            savedJson = """{"fields":{"start_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()
        coEvery { repository.insertFormResponse(any()) } returns false

        val result = viewModel.saveFormResponses(1L, 2L)
        advanceUntilIdle()

        assertFalse(result)
        assertTrue(viewModel.wasDuplicate)
        assertEquals(
            "You have already submitted this form for this Year",
            viewModel.showToastLiveData.value
        )
    }

    @Test
    fun `saveFormResponses inserts the entity when it is not a duplicate`() = runTest {
        stubCampaign(
            campaignSchema(campaignField("start_date")),
            savedJson = """{"fields":{"start_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()
        coEvery { repository.insertFormResponse(any()) } returns true

        viewModel.saveFormResponses(1L, 2L)
        advanceUntilIdle()

        coVerify { repository.insertFormResponse(any()) }
        assertFalse(viewModel.wasDuplicate)
    }

    @Test
    fun `getVisibleFields maps visible fields only`() = runTest {
        stubCampaign(
            campaignSchema(
                campaignField("has_symptom", default = "No", options = listOf("Yes", "No")),
                campaignField(
                    "symptom_detail",
                    conditional = ConditionalLogic("has_symptom", "Yes")
                )
            )
        )
        viewModel.loadFormSchema("MDAC_01", viewMode = false)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(1, fields.size)
        assertEquals("has_symptom", fields.first().fieldId)
        assertEquals(2, fields.first().options?.size)
    }
}
