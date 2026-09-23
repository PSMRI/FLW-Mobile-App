package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

import android.content.Context
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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.NCDFollowUpFormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NCDReferalFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: NCDFollowUpFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: NCDReferalFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = NCDReferalFormViewModel(repository, context)
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
    fun `visitHistory is not null`() {
        assertNotNull(viewModel.visitHistory)
    }

    @Test
    fun `getVisibleFields returns empty when schema null`() {
        assertTrue(viewModel.getVisibleFields().isEmpty())
    }

    @Test
    fun `visitNo defaults to 1`() {
        assertEquals(1, viewModel.visitNo)
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun ncdField(
        id: String,
        conditional: ConditionalLogic? = null,
        options: Any? = null,
        validation: FieldValidationDto? = null
    ) = FormFieldDto(
        fieldId = id,
        label = id,
        type = "text",
        options = options,
        conditional = conditional,
        validation = validation
    )

    private fun ncdSchema(vararg fields: FormFieldDto) = FormSchemaDto(
        formId = "CDTF_001",
        formName = "NCD Referral",
        version = 3,
        sections = listOf(
            FormSectionDto(sectionId = "s1", sectionTitle = "Section 1", fields = fields.toList())
        )
    )

    private fun ncdVisit(
        visitNo: Int,
        followUpNo: Int,
        treatmentStartDate: String = "2024-01-10",
        followUpDate: String? = null,
        formDataJson: String = """{"fields":{}}"""
    ) = NCDReferalFormResponseJsonEntity(
        benId = 1L,
        hhId = 2L,
        visitNo = visitNo,
        followUpNo = followUpNo,
        treatmentStartDate = treatmentStartDate,
        followUpDate = followUpDate,
        diagnosisCodes = null,
        formId = "CDTF_001",
        version = 1,
        formDataJson = formDataJson
    )

    private fun defaultNcdSchema() = ncdSchema(
        ncdField("visit_label"),
        ncdField("follow_up_no"),
        ncdField("follow_up_date"),
        ncdField("diagnosis"),
        ncdField("treatment_start_date")
    )

    private fun stubNcd(
        dto: FormSchemaDto?,
        history: List<NCDReferalFormResponseJsonEntity> = emptyList(),
        cached: FormSchemaEntity? = null
    ) {
        coEvery { repository.getAllVisitsByBeneficiary(any(), any()) } returns history
        coEvery { repository.getSavedSchema(any()) } returns cached
        coEvery { repository.getFormSchema(any()) } returns dto
    }

    private fun ncdFieldOf(id: String): FormFieldDto =
        viewModel.schema.value!!.sections.flatMap { it.fields }.first { it.fieldId == id }

    // =====================================================
    // Default state Tests
    // =====================================================

    @Test
    fun `followUpNo defaults to 0`() {
        assertEquals(0, viewModel.followUpNo)
    }

    @Test
    fun `isFollowUpMode defaults to false`() {
        assertFalse(viewModel.isFollowUpMode)
    }

    @Test
    fun `isViewMode defaults to false`() {
        assertFalse(viewModel.isViewMode)
    }

    @Test
    fun `visitHistory is initially empty`() {
        assertTrue(viewModel.visitHistory.value.isEmpty())
    }

    @Test
    fun `updateFieldValue does nothing when schema is null`() {
        viewModel.updateFieldValue("diagnosis", "DM")
        assertNull(viewModel.schema.value)
    }

    // =====================================================
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema prepares a first visit`() = runTest {
        stubNcd(defaultNcdSchema())

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertNotNull(viewModel.schema.value)
        assertEquals(1, viewModel.visitNo)
        assertEquals(0, viewModel.followUpNo)
        assertFalse(viewModel.isFollowUpMode)
        assertEquals("Visit-1", ncdFieldOf("visit_label").value)
        assertFalse(ncdFieldOf("visit_label").isEditable)
        assertEquals("0", ncdFieldOf("follow_up_no").value)
        assertFalse(ncdFieldOf("follow_up_date").visible)
        assertTrue(ncdFieldOf("diagnosis").isEditable)
    }

    @Test
    fun `loadFormSchema keeps schema null when nothing is available`() = runTest {
        stubNcd(null)

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema loads schema from the cached entity`() = runTest {
        val dto = defaultNcdSchema()
        stubNcd(
            dto = null,
            cached = FormSchemaEntity(
                formId = "CDTF_001",
                formName = "NCD Referral",
                language = "en",
                version = 3,
                schemaJson = dto.toJson()
            )
        )

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals("CDTF_001", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema populates visitHistory`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0)))

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals(1, viewModel.visitHistory.value.size)
    }

    @Test
    fun `loadFormSchema switches to follow up mode after a main visit`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0)))

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals(1, viewModel.visitNo)
        assertEquals(1, viewModel.followUpNo)
        assertTrue(viewModel.isFollowUpMode)
        assertTrue(ncdFieldOf("follow_up_date").visible)
        assertTrue(ncdFieldOf("follow_up_date").isEditable)
        assertNull(ncdFieldOf("follow_up_date").value)
    }

    @Test
    fun `loadFormSchema rolls over to the next visit after six follow ups`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 6)))

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals(2, viewModel.visitNo)
        assertEquals(0, viewModel.followUpNo)
        assertFalse(viewModel.isFollowUpMode)
    }

    @Test
    fun `loadFormSchema copies the diagnosis array from the last main visit`() = runTest {
        stubNcd(
            defaultNcdSchema(),
            history = listOf(
                ncdVisit(
                    1,
                    0,
                    formDataJson = """{"fields":{"diagnosis":["DM","HTN"],"treatment_start_date":"2024-01-10"}}"""
                )
            )
        )

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals(listOf("DM", "HTN"), ncdFieldOf("diagnosis").value)
        assertFalse(ncdFieldOf("diagnosis").isEditable)
        assertEquals("2024-01-10", ncdFieldOf("treatment_start_date").value)
        assertFalse(ncdFieldOf("treatment_start_date").isEditable)
    }

    @Test
    fun `loadFormSchema splits a comma separated diagnosis`() = runTest {
        stubNcd(
            defaultNcdSchema(),
            history = listOf(
                ncdVisit(1, 0, formDataJson = """{"fields":{"diagnosis":"DM, HTN"}}""")
            )
        )

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals(listOf("DM", "HTN"), ncdFieldOf("diagnosis").value)
    }

    @Test
    fun `loadFormSchema uses an empty diagnosis when none is stored`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0)))

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertEquals(emptyList<String>(), ncdFieldOf("diagnosis").value)
    }

    @Test
    fun `loadFormSchema in view mode disables fields`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.isViewMode = true

        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        assertFalse(ncdFieldOf("diagnosis").isEditable)
        assertFalse(ncdFieldOf("visit_label").isEditable)
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue replaces the field value`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        viewModel.updateFieldValue("treatment_start_date", "10-01-2024")

        assertEquals("10-01-2024", ncdFieldOf("treatment_start_date").value)
    }

    @Test
    fun `updateFieldValue clears the field error message`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        ncdFieldOf("diagnosis").errorMessage = "required"

        viewModel.updateFieldValue("diagnosis", "DM")

        assertNull(ncdFieldOf("diagnosis").errorMessage)
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields resolves the visit label`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals("Visit-1", fields.first { it.fieldId == "visit_label" }.value)
    }

    @Test
    fun `getVisibleFields excludes hidden follow up date`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertTrue(fields.none { it.fieldId == "follow_up_date" })
    }

    @Test
    fun `getVisibleFields maps options validation and conditional`() = runTest {
        stubNcd(
            ncdSchema(
                ncdField("diagnosis", options = listOf("DM", "HTN")),
                ncdField(
                    "other_diagnosis",
                    conditional = ConditionalLogic("diagnosis", "DM"),
                    validation = FieldValidationDto(min = 1f, max = 5f, maxLength = 20)
                )
            )
        )
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(2, fields.first { it.fieldId == "diagnosis" }.options?.size)
        val other = fields.first { it.fieldId == "other_diagnosis" }
        assertNotNull(other.validation)
        assertNotNull(other.conditional)
    }

    // =====================================================
    // saveFormResponses() Tests
    // =====================================================

    @Test
    fun `saveFormResponses does nothing when schema is null`() = runTest {
        runCatching { viewModel.saveFormResponses(1L, 2L) }
        advanceUntilIdle()

        coVerify(exactly = 0) {
            repository.saveVisitOrFollowUp(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun `saveFormResponses persists the visit with a diagnosis list`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("treatment_start_date", "10-01-2024")
        viewModel.updateFieldValue("diagnosis", listOf("DM", "HTN"))

        runCatching { viewModel.saveFormResponses(1L, 2L) }
        advanceUntilIdle()

        coVerify {
            repository.saveVisitOrFollowUp(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun `saveFormResponses persists a comma separated diagnosis`() = runTest {
        stubNcd(defaultNcdSchema())
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("diagnosis", "DM, HTN")

        runCatching { viewModel.saveFormResponses(1L, 2L) }
        advanceUntilIdle()

        coVerify {
            repository.saveVisitOrFollowUp(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        }
    }

    // =====================================================
    // getFollowUpDateErrorFromUI() Tests
    // =====================================================

    @Test
    fun `getFollowUpDateErrorFromUI returns null when the field is absent`() {
        assertNull(viewModel.getFollowUpDateErrorFromUI())
    }

    @Test
    fun `getFollowUpDateErrorFromUI reports a missing follow up date`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0)))
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()

        val error = viewModel.getFollowUpDateErrorFromUI()

        assertNotNull(error)
        assertEquals(R.string.follow_up_date_required_or_invalid, error!!.resId)
    }

    @Test
    fun `getFollowUpDateErrorFromUI reports a date before the treatment start`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0)))
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("follow_up_date", "05-01-2024")

        val error = viewModel.getFollowUpDateErrorFromUI()

        assertNotNull(error)
        assertEquals(R.string.follow_up_must_be_after_treatment, error!!.resId)
    }

    @Test
    fun `getFollowUpDateErrorFromUI reports an invalid treatment date in db`() = runTest {
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0, treatmentStartDate = "10/01/2024")))
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("follow_up_date", "10-03-2024")

        val error = viewModel.getFollowUpDateErrorFromUI()

        assertNotNull(error)
        assertEquals(R.string.invalid_treatment_date_in_db, error!!.resId)
    }

    @Test
    fun `getFollowUpDateErrorFromUI rejects a future month`() = runTest {
        val treatment = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
            Calendar.getInstance().time
        )
        val nextMonthFirst = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(
            Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }.time
        )
        stubNcd(defaultNcdSchema(), history = listOf(ncdVisit(1, 0, treatmentStartDate = treatment)))
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("follow_up_date", nextMonthFirst)

        val error = viewModel.getFollowUpDateErrorFromUI()

        assertNotNull(error)
        assertEquals(R.string.follow_up_cannot_be_future_month, error!!.resId)
    }

    @Test
    fun `getFollowUpDateErrorFromUI accepts the month after the last follow up`() = runTest {
        stubNcd(
            defaultNcdSchema(),
            history = listOf(
                ncdVisit(1, 0),
                ncdVisit(1, 1, followUpDate = "2024-02-05")
            )
        )
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("follow_up_date", "10-03-2024")

        assertNull(viewModel.getFollowUpDateErrorFromUI())
    }

    @Test
    fun `getFollowUpDateErrorFromUI rejects a month that is not the next one`() = runTest {
        stubNcd(
            defaultNcdSchema(),
            history = listOf(
                ncdVisit(1, 0),
                ncdVisit(1, 1, followUpDate = "2024-02-05")
            )
        )
        viewModel.loadFormSchema(1L)
        advanceUntilIdle()
        viewModel.updateFieldValue("follow_up_date", "10-05-2024")

        val error = viewModel.getFollowUpDateErrorFromUI()

        assertNotNull(error)
        assertEquals(R.string.follow_up_must_be_next_month, error!!.resId)
    }
}
