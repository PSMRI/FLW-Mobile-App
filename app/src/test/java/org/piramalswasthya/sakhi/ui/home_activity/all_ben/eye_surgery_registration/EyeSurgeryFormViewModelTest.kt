package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration

import android.content.Context
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
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
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.EyeSurgeryFormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class EyeSurgeryFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: EyeSurgeryFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: EyeSurgeryFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        viewModel = EyeSurgeryFormViewModel(repository, context)
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
    fun `getMaxVisitDate is not null`() {
        assertNotNull(viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate is null when no previous visit`() {
        assertNull(viewModel.getMinVisitDate())
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun eyeField(
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

    private fun eyeSchema(vararg fields: FormFieldDto) = FormSchemaDto(
        formId = "EYE_01",
        formName = "Eye Surgery",
        version = 2,
        sections = listOf(
            FormSectionDto(sectionId = "s1", sectionTitle = "Section 1", fields = fields.toList())
        )
    )

    private fun eyeVisit(visitMonth: String) = EyeSurgeryFormResponseJsonEntity(
        benId = 1L,
        hhId = 2L,
        visitDate = "01-05-2024",
        visitMonth = visitMonth,
        eyeSide = "LEFT",
        formId = "EYE_01",
        version = 1,
        formDataJson = """{"fields":{}}"""
    )

    private fun stubEye(
        dto: FormSchemaDto?,
        savedJson: String? = null,
        visits: List<EyeSurgeryFormResponseJsonEntity> = emptyList(),
        cached: FormSchemaEntity? = null
    ) {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns visits
        coEvery { repository.getSavedSchema(any()) } returns cached
        coEvery { repository.getFormSchema(any()) } returns dto
        coEvery { repository.loadFormResponseJson(any(), any()) } returns savedJson
    }

    private fun eyeFieldOf(id: String): FormFieldDto =
        viewModel.schema.value!!.sections.flatMap { it.fields }.first { it.fieldId == id }

    private fun midnightToday(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun currentMonthKey(): String =
        SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(midnightToday())

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
    fun `updateFieldValue does nothing when schema is null`() {
        viewModel.updateFieldValue("eye_affected", "LEFT")
        assertNull(viewModel.schema.value)
    }

    // =====================================================
    // Ben id / visit list Tests
    // =====================================================

    @Test
    fun `loadAllBenIds posts the repository ids`() = runTest {
        coEvery { repository.getAllBenIds() } returns listOf(1L, 2L, 3L)

        viewModel.loadAllBenIds()
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L, 3L), viewModel.benIdList.value)
    }

    @Test
    fun `getMaxVisitDate returns yesterday when a visit exists this month`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(1L) } returns listOf(eyeVisit(currentMonthKey()))
        viewModel.loadSyncedVisitList(1L)
        advanceUntilIdle()

        val expected = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, -1)
        }.time

        assertEquals(expected, viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMaxVisitDate returns today when no visit this month`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(1L) } returns listOf(eyeVisit("1999-01"))
        viewModel.loadSyncedVisitList(1L)
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
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema populates the schema and locks fixed fields`() = runTest {
        viewModel.visitDay = "Day 1"
        stubEye(eyeSchema(eyeField("visit_day"), eyeField("due_date"), eyeField("visit_date")))

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        assertNotNull(viewModel.schema.value)
        assertEquals("Day 1", eyeFieldOf("visit_day").value)
        assertFalse(eyeFieldOf("visit_day").isEditable)
        assertFalse(eyeFieldOf("due_date").isEditable)
        assertTrue(eyeFieldOf("visit_date").isEditable)
    }

    @Test
    fun `loadFormSchema keeps schema null when nothing is available`() = runTest {
        stubEye(null)

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema loads the schema from the cached entity`() = runTest {
        val dto = eyeSchema(eyeField("visit_date"))
        stubEye(
            dto = null,
            cached = FormSchemaEntity(
                formId = "EYE_01",
                formName = "Eye Surgery",
                language = "en",
                version = 2,
                schemaJson = dto.toJson()
            )
        )

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("EYE_01", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema in view mode disables fields`() = runTest {
        stubEye(eyeSchema(eyeField("visit_date")))

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = true)
        advanceUntilIdle()

        assertFalse(eyeFieldOf("visit_date").isEditable)
    }

    @Test
    fun `loadFormSchema applies saved values including arrays`() = runTest {
        stubEye(
            eyeSchema(eyeField("symptoms_observed"), eyeField("note"), eyeField("visit_date")),
            savedJson = """{"fields":{"symptoms_observed":["Blurred","Pain"],"note":"a,b","visit_date":"01-05-2024"}}"""
        )

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        assertEquals(listOf("Blurred", "Pain"), eyeFieldOf("symptoms_observed").value)
        assertEquals(listOf("a", "b"), eyeFieldOf("note").value)
        assertEquals("01-05-2024", eyeFieldOf("visit_date").value)
    }

    @Test
    fun `loadFormSchema falls back to defaults on malformed saved json`() = runTest {
        stubEye(eyeSchema(eyeField("visit_date", default = "01-01-2024")), savedJson = "{not-json")

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("01-01-2024", eyeFieldOf("visit_date").value)
    }

    @Test
    fun `loadFormSchema preselects the eye side from list options`() = runTest {
        stubEye(eyeSchema(eyeField("eye_affected", options = listOf("LEFT", "RIGHT"))))

        viewModel.loadFormSchema(
            1L, "EYE_01", viewMode = false, loadSavedData = false, eyeSide = "left"
        )
        advanceUntilIdle()

        assertEquals("LEFT", eyeFieldOf("eye_affected").value)
        assertFalse(eyeFieldOf("eye_affected").isEditable)
    }

    @Test
    fun `loadFormSchema preselects the eye side from a string option list`() = runTest {
        stubEye(eyeSchema(eyeField("eye_affected", options = "LEFT,RIGHT")))

        viewModel.loadFormSchema(
            1L, "EYE_01", viewMode = false, loadSavedData = false, eyeSide = "RIGHT"
        )
        advanceUntilIdle()

        assertEquals("RIGHT", eyeFieldOf("eye_affected").value)
    }

    @Test
    fun `loadFormSchema falls back to the raw eye side when no option matches`() = runTest {
        stubEye(eyeSchema(eyeField("eye_affected"), eyeField("visit_date")))

        viewModel.loadFormSchema(
            1L, "EYE_01", viewMode = false, loadSavedData = false, eyeSide = "BOTH"
        )
        advanceUntilIdle()

        assertEquals("BOTH", eyeFieldOf("eye_affected").value)
        assertNull(eyeFieldOf("visit_date").value)
    }

    @Test
    fun `loadFormSchema evaluates conditional visibility`() = runTest {
        stubEye(
            eyeSchema(
                eyeField("has_symptom", default = "Yes"),
                eyeField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes")),
                eyeField("other_detail", conditional = ConditionalLogic("has_symptom", "No"))
            )
        )

        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        assertTrue(eyeFieldOf("symptom_detail").visible)
        assertFalse(eyeFieldOf("other_detail").visible)
    }

    // =====================================================
    // loadFormSchemaFromJson() Tests
    // =====================================================

    @Test
    fun `loadFormSchemaFromJson applies the provided values`() = runTest {
        stubEye(eyeSchema(eyeField("visit_date"), eyeField("eye_affected")))

        viewModel.loadFormSchemaFromJson(
            1L, "EYE_01", false, """{"fields":{"visit_date":"01-05-2024"}}"""
        )
        advanceUntilIdle()

        assertEquals("01-05-2024", eyeFieldOf("visit_date").value)
        assertFalse(eyeFieldOf("eye_affected").isEditable)
        assertTrue(eyeFieldOf("visit_date").isEditable)
    }

    @Test
    fun `loadFormSchemaFromJson handles malformed json`() = runTest {
        stubEye(eyeSchema(eyeField("visit_date", default = "01-01-2024")))

        viewModel.loadFormSchemaFromJson(1L, "EYE_01", false, "{not-json")
        advanceUntilIdle()

        assertEquals("01-01-2024", eyeFieldOf("visit_date").value)
    }

    @Test
    fun `loadFormSchemaFromJson keeps schema null when nothing is available`() = runTest {
        stubEye(null)

        viewModel.loadFormSchemaFromJson(1L, "EYE_01", false, """{"fields":{}}""")
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    // =====================================================
    // updateFieldValue() / saveFormResponses() Tests
    // =====================================================

    @Test
    fun `updateFieldValue updates value and recomputes visibility`() = runTest {
        stubEye(
            eyeSchema(
                eyeField("has_symptom", default = "No"),
                eyeField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()
        assertFalse(eyeFieldOf("symptom_detail").visible)

        viewModel.updateFieldValue("has_symptom", "Yes")

        assertEquals("Yes", eyeFieldOf("has_symptom").value)
        assertTrue(eyeFieldOf("symptom_detail").visible)
    }

    @Test
    fun `saveFormResponses returns false when schema is null`() = runTest {
        assertFalse(viewModel.saveFormResponses(1L, 2L, "LEFT"))
    }

    @Test
    fun `saveFormResponses upserts the entity`() = runTest {
        stubEye(
            eyeSchema(eyeField("visit_date")),
            savedJson = """{"fields":{"visit_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        viewModel.saveFormResponses(1L, 2L, "LEFT")
        advanceUntilIdle()

        coVerify { repository.upsertByEye(any()) }
    }

    @Test
    fun `saveFormResponses stores a referral when a facility is chosen`() = runTest {
        stubEye(
            eyeSchema(eyeField("visit_date"), eyeField("referred_to"), eyeField("symptoms_observed")),
            savedJson = """{"fields":{"visit_date":"01-05-2024","referred_to":"CHC","symptoms_observed":["Blurred","Pain"]}}"""
        )
        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        viewModel.saveFormResponses(1L, 2L, "LEFT", recordId = 5)
        advanceUntilIdle()

        coVerify { repository.saveReferral(1L, "CHC", "Blurred, Pain") }
    }

    @Test
    fun `saveFormResponses skips the referral when no facility is chosen`() = runTest {
        stubEye(
            eyeSchema(eyeField("visit_date")),
            savedJson = """{"fields":{"visit_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        viewModel.saveFormResponses(1L, 2L, "LEFT")
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveReferral(any(), any(), any()) }
    }

    @Test
    fun `getVisibleFields maps visible fields only`() = runTest {
        stubEye(
            eyeSchema(
                eyeField("has_symptom", default = "No", options = listOf("Yes", "No")),
                eyeField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        viewModel.loadFormSchema(1L, "EYE_01", viewMode = false)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(1, fields.size)
        assertEquals("has_symptom", fields.first().fieldId)
        assertEquals(2, fields.first().options?.size)
    }
}
