package org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.form

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
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FilariaMDAFormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMDAFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FilariaMDAFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: FilariaMDAFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FilariaMDAFormViewModel(repository, context)
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

    @Test
    fun `wasDuplicate defaults to false`() {
        assertTrue(!viewModel.wasDuplicate)
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun mdaField(
        id: String,
        default: Any? = null,
        conditional: ConditionalLogic? = null,
        options: Any? = null,
        validation: FieldValidationDto? = null
    ) = FormFieldDto(
        fieldId = id,
        label = id,
        type = "text",
        options = options,
        conditional = conditional,
        default = default,
        validation = validation
    )

    private fun mdaSchema(vararg fields: FormFieldDto) = FormSchemaDto(
        formId = "MDA_01",
        formName = "Filaria MDA",
        version = 2,
        sections = listOf(
            FormSectionDto(sectionId = "s1", sectionTitle = "Section 1", fields = fields.toList())
        )
    )

    private fun mdaVisit(visitMonth: String) = FilariaMDAFormResponseJsonEntity(
        hhId = 2L,
        visitDate = "01-05-2024",
        visitMonth = visitMonth,
        formId = "MDA_01",
        version = 1,
        formDataJson = """{"fields":{}}"""
    )

    private fun stubMda(
        dto: FormSchemaDto?,
        savedJson: String? = null,
        visits: List<FilariaMDAFormResponseJsonEntity> = emptyList(),
        cached: FormSchemaEntity? = null
    ) {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns visits
        coEvery { repository.getSavedSchema(any()) } returns cached
        coEvery { repository.getFormSchema(any()) } returns dto
        coEvery { repository.loadFormResponseJson(any(), any()) } returns savedJson
    }

    private fun mdaFieldOf(id: String): FormFieldDto =
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
        viewModel.updateFieldValue("mda_distribution_date", "01-05-2024")
        assertNull(viewModel.schema.value)
    }

    // =====================================================
    // Bottle list / visit list Tests
    // =====================================================

    @Test
    fun `loadBottleData posts the repository bottles`() = runTest {
        val bottles = listOf(BottleItem(1, "B-1", "01-05-2024"))
        coEvery { repository.getBottleList(2L) } returns bottles

        viewModel.loadBottleData(2L)
        advanceUntilIdle()

        assertEquals(bottles, viewModel.bottleList.value)
    }

    @Test
    fun `getMaxVisitDate returns yesterday when a visit exists this month`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(2L) } returns listOf(mdaVisit(currentMonthKey()))
        viewModel.loadSyncedVisitList(2L)
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
        coEvery { repository.getSyncedVisitsByRchId(2L) } returns listOf(mdaVisit("1999-01"))
        viewModel.loadSyncedVisitList(2L)
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
        stubMda(mdaSchema(mdaField("visit_day"), mdaField("due_date"), mdaField("bottle_no")))

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        assertNotNull(viewModel.schema.value)
        assertEquals("Day 1", mdaFieldOf("visit_day").value)
        assertFalse(mdaFieldOf("visit_day").isEditable)
        assertFalse(mdaFieldOf("due_date").isEditable)
        assertTrue(mdaFieldOf("bottle_no").isEditable)
    }

    @Test
    fun `loadFormSchema keeps schema null when nothing is available`() = runTest {
        stubMda(null)

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema loads the schema from the cached entity`() = runTest {
        val dto = mdaSchema(mdaField("bottle_no", default = "1"))
        stubMda(
            dto = null,
            cached = FormSchemaEntity(
                formId = "MDA_01",
                formName = "Filaria MDA",
                language = "en",
                version = 2,
                schemaJson = dto.toJson()
            )
        )

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("MDA_01", viewModel.schema.value?.formId)
        assertEquals("1", mdaFieldOf("bottle_no").value)
    }

    @Test
    fun `loadFormSchema applies saved field values`() = runTest {
        stubMda(
            mdaSchema(mdaField("bottle_no", default = "1"), mdaField("mda_distribution_date")),
            savedJson = """{"fields":{"bottle_no":"7","mda_distribution_date":"01-05-2024"}}"""
        )

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("7", mdaFieldOf("bottle_no").value)
        assertEquals("01-05-2024", mdaFieldOf("mda_distribution_date").value)
    }

    @Test
    fun `loadFormSchema falls back to defaults on malformed saved json`() = runTest {
        stubMda(mdaSchema(mdaField("bottle_no", default = "1")), savedJson = "{not-json")

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        assertEquals("1", mdaFieldOf("bottle_no").value)
    }

    @Test
    fun `loadFormSchema in view mode disables fields`() = runTest {
        stubMda(mdaSchema(mdaField("bottle_no")))

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = true)
        advanceUntilIdle()

        assertFalse(mdaFieldOf("bottle_no").isEditable)
    }

    @Test
    fun `loadFormSchema evaluates conditional visibility`() = runTest {
        stubMda(
            mdaSchema(
                mdaField("has_symptom", default = "Yes"),
                mdaField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes")),
                mdaField("other_detail", conditional = ConditionalLogic("has_symptom", "No"))
            )
        )

        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        assertTrue(mdaFieldOf("symptom_detail").visible)
        assertFalse(mdaFieldOf("other_detail").visible)
    }

    // =====================================================
    // updateFieldValue() / saveFormResponses() Tests
    // =====================================================

    @Test
    fun `updateFieldValue updates value and recomputes visibility`() = runTest {
        stubMda(
            mdaSchema(
                mdaField("has_symptom", default = "No"),
                mdaField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()
        assertFalse(mdaFieldOf("symptom_detail").visible)

        viewModel.updateFieldValue("has_symptom", "Yes")

        assertEquals("Yes", mdaFieldOf("has_symptom").value)
        assertTrue(mdaFieldOf("symptom_detail").visible)
    }

    @Test
    fun `saveFormResponses returns false when schema is null`() = runTest {
        assertFalse(viewModel.saveFormResponses(1L, 2L))
    }

    @Test
    fun `saveFormResponses flags a duplicate submission`() = runTest {
        stubMda(
            mdaSchema(mdaField("mda_distribution_date")),
            savedJson = """{"fields":{"mda_distribution_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()
        coEvery { repository.insertFormResponse(any()) } returns false

        val result = viewModel.saveFormResponses(1L, 2L)
        advanceUntilIdle()

        assertFalse(result)
        assertTrue(viewModel.wasDuplicate)
        assertEquals(
            "You have already submitted this form for this month",
            viewModel.showToastLiveData.value
        )
    }

    @Test
    fun `saveFormResponses inserts the entity when it is not a duplicate`() = runTest {
        stubMda(
            mdaSchema(mdaField("mda_distribution_date")),
            savedJson = """{"fields":{"mda_distribution_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()
        coEvery { repository.insertFormResponse(any()) } returns true

        viewModel.saveFormResponses(1L, 2L)
        advanceUntilIdle()

        coVerify { repository.insertFormResponse(any()) }
        assertFalse(viewModel.wasDuplicate)
    }

    @Test
    fun `saveFormResponses returns false when repository throws`() = runTest {
        stubMda(
            mdaSchema(mdaField("mda_distribution_date")),
            savedJson = """{"fields":{"mda_distribution_date":"01-05-2024"}}"""
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()
        coEvery { repository.insertFormResponse(any()) } throws RuntimeException("db error")

        val result = viewModel.saveFormResponses(1L, 2L)
        advanceUntilIdle()

        assertFalse(result)
    }

    @Test
    fun `saveFormResponses falls back to an empty month key when the distribution date is missing`() = runTest {
        stubMda(
            mdaSchema(mdaField("symptom_detail")),
            savedJson = """{"fields":{"symptom_detail":"none"}}"""
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()
        coEvery { repository.insertFormResponse(any()) } returns true

        viewModel.saveFormResponses(1L, 2L)
        advanceUntilIdle()

        coVerify { repository.insertFormResponse(any()) }
    }

    @Test
    fun `getVisibleFields maps visible fields only`() = runTest {
        stubMda(
            mdaSchema(
                mdaField("has_symptom", default = "No", options = listOf("Yes", "No")),
                mdaField("symptom_detail", conditional = ConditionalLogic("has_symptom", "Yes"))
            )
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(1, fields.size)
        assertEquals("has_symptom", fields.first().fieldId)
        assertEquals(2, fields.first().options?.size)
    }

    @Test
    fun `getVisibleFields maps the full validation block`() = runTest {
        stubMda(
            mdaSchema(
                mdaField(
                    "age",
                    validation = FieldValidationDto(
                        min = 1f,
                        max = 120f,
                        maxLength = 3,
                        regex = "\\d+",
                        errorMessage = "invalid",
                        decimalPlaces = 0,
                        maxSizeMB = 5,
                        afterField = "dob",
                        beforeField = "gender"
                    )
                )
            )
        )
        viewModel.loadFormSchema(2L, "MDA_01", viewMode = false)
        advanceUntilIdle()

        val fields = viewModel.getVisibleFields()

        assertEquals(1, fields.size)
        assertEquals(120f, fields.first().validation?.max)
        assertEquals("invalid", fields.first().validation?.errorMessage)
    }
}
