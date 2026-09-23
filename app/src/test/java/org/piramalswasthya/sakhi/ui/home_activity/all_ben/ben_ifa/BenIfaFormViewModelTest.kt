package org.piramalswasthya.sakhi.ui.home_activity.all_ben.ben_ifa

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.BenIfaFormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class BenIfaFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: BenIfaFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: BenIfaFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        viewModel = BenIfaFormViewModel(repository, context)
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
    fun `getMinVisitDate is not null`() {
        assertNotNull(viewModel.getMinVisitDate())
    }

    @Test
    fun `bottleList live data is not null`() {
        assertNotNull(viewModel.bottleList)
    }

    private fun field(
        id: String,
        value: Any? = null,
        default: Any? = null,
        conditional: ConditionalLogic? = null
    ) = FormFieldDto(
        fieldId = id,
        label = "L-$id",
        type = "text",
        conditional = conditional,
        default = default ?: value,
        value = value
    )

    private fun schemaOf(vararg fields: FormFieldDto) = FormSchemaDto(
        formId = "IFA1",
        formName = "IFA",
        version = 3,
        sections = listOf(FormSectionDto(sectionId = "s1", sectionTitle = "S", fields = fields.toList()))
    )

    private suspend fun TestScope.loadSchema(
        schema: FormSchemaDto,
        savedJson: String? = null,
        viewMode: Boolean = false
    ) {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()
        coEvery { repository.getSavedSchema("IFA1") } returns FormSchemaEntity(
            formId = "IFA1",
            formName = "IFA",
            language = "en",
            version = 3,
            schemaJson = schema.toJson()
        )
        coEvery { repository.loadFormResponseJson(any(), any()) } returns savedJson
        viewModel.loadFormSchema(1L, "IFA1", viewMode, "01-01-2026")
        advanceUntilIdle()
    }

    @Test
    fun `loadFormSchema publishes the cached schema`() = runTest {
        loadSchema(schemaOf(field("a")))
        assertNotNull(viewModel.schema.value)
        assertEquals("IFA1", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema falls back to repository when no cached schema`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()
        coEvery { repository.getSavedSchema("IFA1") } returns null
        coEvery { repository.getFormSchema("IFA1") } returns schemaOf(field("a"))
        coEvery { repository.loadFormResponseJson(any(), any()) } returns null
        viewModel.loadFormSchema(1L, "IFA1", false, "01-01-2026")
        advanceUntilIdle()
        assertNotNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema leaves schema null when nothing is available`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()
        coEvery { repository.getSavedSchema("IFA1") } returns null
        coEvery { repository.getFormSchema("IFA1") } returns null
        viewModel.loadFormSchema(1L, "IFA1", false, "01-01-2026")
        advanceUntilIdle()
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema applies saved values over field defaults`() = runTest {
        loadSchema(
            schemaOf(field("weight", default = "10"), field("height", default = "70")),
            savedJson = """{"fields":{"weight":"12"}}"""
        )
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("12", fields.first { it.fieldId == "weight" }.value)
        assertEquals("70", fields.first { it.fieldId == "height" }.value)
    }

    @Test
    fun `loadFormSchema ignores malformed saved json and uses defaults`() = runTest {
        loadSchema(schemaOf(field("weight", default = "10")), savedJson = "not-json")
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("10", fields.first { it.fieldId == "weight" }.value)
    }

    @Test
    fun `loadFormSchema injects visitDay and locks non-editable fields`() = runTest {
        viewModel.visitDay = "Day 3"
        loadSchema(schemaOf(field("visit_day"), field("due_date"), field("weight")))
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("Day 3", fields.first { it.fieldId == "visit_day" }.value)
        assertFalse(fields.first { it.fieldId == "visit_day" }.isEditable)
        assertFalse(fields.first { it.fieldId == "due_date" }.isEditable)
        assertTrue(fields.first { it.fieldId == "weight" }.isEditable)
    }

    @Test
    fun `loadFormSchema locks every field in view mode`() = runTest {
        loadSchema(schemaOf(field("weight"), field("height")), viewMode = true)
        assertTrue(viewModel.schema.value!!.sections.flatMap { it.fields }.none { it.isEditable })
    }

    @Test
    fun `loadFormSchema hides conditional fields whose dependency does not match`() = runTest {
        loadSchema(
            schemaOf(
                field("hasIfa", value = "No"),
                field("bottles", conditional = ConditionalLogic(dependsOn = "hasIfa", expectedValue = "Yes"))
            )
        )
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertTrue(fields.first { it.fieldId == "hasIfa" }.visible)
        assertFalse(fields.first { it.fieldId == "bottles" }.visible)
    }

    @Test
    fun `loadFormSchema shows conditional fields whose dependency matches ignoring case`() = runTest {
        loadSchema(
            schemaOf(
                field("hasIfa", value = "yes"),
                field("bottles", conditional = ConditionalLogic(dependsOn = "hasIfa", expectedValue = "Yes"))
            )
        )
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertTrue(fields.first { it.fieldId == "bottles" }.visible)
    }

    @Test
    fun `loadFormSchema treats a blank dependsOn as unconditional`() = runTest {
        loadSchema(schemaOf(field("bottles", conditional = ConditionalLogic(dependsOn = "", expectedValue = "Yes"))))
        assertTrue(viewModel.schema.value!!.sections.flatMap { it.fields }.first().visible)
    }

    @Test
    fun `updateFieldValue writes the value and re-evaluates visibility`() = runTest {
        loadSchema(
            schemaOf(
                field("hasIfa", value = "No"),
                field("bottles", conditional = ConditionalLogic(dependsOn = "hasIfa", expectedValue = "Yes"))
            )
        )
        viewModel.updateFieldValue("hasIfa", "Yes")
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("Yes", fields.first { it.fieldId == "hasIfa" }.value)
        assertTrue(fields.first { it.fieldId == "bottles" }.visible)
    }

    @Test
    fun `updateFieldValue is a no-op when no schema is loaded`() {
        viewModel.updateFieldValue("anything", "x")
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `updateFieldValue ignores an unknown field id`() = runTest {
        loadSchema(schemaOf(field("weight", value = "10")))
        viewModel.updateFieldValue("nope", "x")
        assertEquals("10", viewModel.schema.value!!.sections.flatMap { it.fields }.first().value)
    }

    @Test
    fun `getVisibleFields maps only visible fields`() = runTest {
        loadSchema(
            schemaOf(
                field("hasIfa", value = "No"),
                field("bottles", conditional = ConditionalLogic(dependsOn = "hasIfa", expectedValue = "Yes"))
            )
        )
        val visible = viewModel.getVisibleFields()
        assertEquals(1, visible.size)
        assertEquals("hasIfa", visible.first().fieldId)
        assertEquals("L-hasIfa", visible.first().label)
        assertEquals("text", visible.first().type)
    }

    @Test
    fun `getVisibleFields drops a conditional with blank expectedValue`() = runTest {
        loadSchema(schemaOf(field("weight", conditional = ConditionalLogic(dependsOn = "", expectedValue = ""))))
        val mapped = viewModel.getVisibleFields()
        assertEquals(1, mapped.size)
        assertNull(mapped.first().conditional)
    }

    @Test
    fun `saveFormResponses returns false when no schema is loaded`() = runTest {
        assertFalse(viewModel.saveFormResponses(1L, 2L))
    }

    @Test
    fun `saveFormResponses persists visible fields and enqueues the sync worker`() = runTest {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
        loadSchema(
            schemaOf(
                field("visit_date", value = "05-01-2026"),
                field("bottles", value = "2"),
                field("hidden", value = "x", conditional = ConditionalLogic(dependsOn = "visit_date", expectedValue = "nope"))
            )
        )
        val saved = slot<BenIfaFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(saved)) } returns Unit

        assertTrue(viewModel.saveFormResponses(11L, 22L))
        advanceUntilIdle()

        assertEquals(11L, saved.captured.benId)
        assertEquals(22L, saved.captured.hhId)
        assertEquals("05-01-2026", saved.captured.visitDate)
        assertEquals("IFA1", saved.captured.formId)
        assertEquals(3, saved.captured.version)
        assertFalse(saved.captured.isSynced)
        val fields = JSONObject(saved.captured.formDataJson).getJSONObject("fields")
        assertEquals("2", fields.getString("bottles"))
        assertFalse(fields.has("hidden"))
        unmockkObject(WorkManager.Companion)
    }

    @Test
    fun `saveFormResponses records N-A when visit_date is absent`() = runTest {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
        loadSchema(schemaOf(field("bottles", value = "2")))
        val saved = slot<BenIfaFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(saved)) } returns Unit
        assertTrue(viewModel.saveFormResponses(1L, 2L))
        assertEquals("N/A", saved.captured.visitDate)
        unmockkObject(WorkManager.Companion)
    }

    @Test
    fun `saveFormResponses returns false when the repository throws`() = runTest {
        loadSchema(schemaOf(field("visit_date", value = "05-01-2026")))
        coEvery { repository.insertFormResponse(any()) } throws RuntimeException("db down")
        assertFalse(viewModel.saveFormResponses(1L, 2L))
    }

    @Test
    fun `loadBottleData publishes the repository list`() = runTest {
        val bottles = listOf(BottleItem(srNo = 1, bottleNumber = "2", dateOfProvision = "01-01-2026"))
        coEvery { repository.getBottleList(1L, "IFA1") } returns bottles
        viewModel.loadBottleData(1L, "IFA1")
        advanceUntilIdle()
        assertEquals(bottles, viewModel.bottleList.value)
    }

    @Test
    fun `checkIfCanAdd publishes the repository verdict`() = runTest {
        coEvery { repository.canAddNewVisit(1L) } returns true
        viewModel.checkIfCanAdd(1L)
        advanceUntilIdle()
        assertEquals(true, viewModel.canAddNewVisit.value)
    }

    @Test
    fun `checkIfCanAdd publishes false when a new visit is not allowed`() = runTest {
        coEvery { repository.canAddNewVisit(1L) } returns false
        viewModel.checkIfCanAdd(1L)
        advanceUntilIdle()
        assertEquals(false, viewModel.canAddNewVisit.value)
    }

    @Test
    fun `getMaxVisitDate is today when today has no visit yet`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(1L) } returns emptyList()
        viewModel.loadSyncedVisitList(1L)
        advanceUntilIdle()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        assertEquals(today, viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMaxVisitDate steps back a day when today already has a visit`() = runTest {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
        coEvery { repository.getSyncedVisitsByRchId(1L) } returns listOf(
            BenIfaFormResponseJsonEntity(
                benId = 1L,
                hhId = 2L,
                visitDate = today,
                formId = "IFA1",
                version = 1,
                formDataJson = """{"fields":{"visit_date":"$today"}}""",
                isSynced = true,
                syncedAt = null
            )
        )
        viewModel.loadSyncedVisitList(1L)
        advanceUntilIdle()
        assertTrue(viewModel.getMaxVisitDate().before(Date()))
    }

    @Test
    fun `getMaxVisitDate ignores rows with malformed or blank dates`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(1L) } returns listOf(
            BenIfaFormResponseJsonEntity(
                benId = 1L, hhId = 2L, visitDate = "x", formId = "IFA1", version = 1,
                formDataJson = "not-json", isSynced = true, syncedAt = null
            ),
            BenIfaFormResponseJsonEntity(
                benId = 1L, hhId = 2L, visitDate = "y", formId = "IFA1", version = 1,
                formDataJson = """{"fields":{"visit_date":""}}""", isSynced = true, syncedAt = null
            )
        )
        viewModel.loadSyncedVisitList(1L)
        advanceUntilIdle()
        assertNotNull(viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate is one month back`() {
        val expected = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val actual = Calendar.getInstance().apply { time = viewModel.getMinVisitDate() }
        assertEquals(expected.get(Calendar.MONTH), actual.get(Calendar.MONTH))
        assertEquals(expected.get(Calendar.YEAR), actual.get(Calendar.YEAR))
    }

    @Test
    fun `isBenDead defaults to false`() {
        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `benIdList live data is exposed`() {
        assertNotNull(viewModel.benIdList)
    }

    @Test
    fun `previousVisitDate is initially null`() {
        assertNull(viewModel.previousVisitDate)
    }
}
