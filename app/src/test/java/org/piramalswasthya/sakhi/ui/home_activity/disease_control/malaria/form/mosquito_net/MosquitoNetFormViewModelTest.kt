package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.mosquito_net

import android.content.Context
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
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.MosquitoNetFormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MosquitoNetFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: MosquitoNetFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: MosquitoNetFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = MosquitoNetFormViewModel(repository, context)
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
        formId = "MN1",
        formName = "MosquitoNet",
        version = 2,
        sections = listOf(FormSectionDto(sectionId = "s1", sectionTitle = "S", fields = fields.toList()))
    )

    private fun entity(hhId: Long = 1L, visitDate: String = "01-01-2026", json: String) =
        MosquitoNetFormResponseJsonEntity(
            hhId = hhId,
            formId = "MN1",
            version = 1,
            visitDate = visitDate,
            formDataJson = json,
            isSynced = true,
            syncedAt = null
        )

    private suspend fun TestScope.loadSchema(
        schema: FormSchemaDto,
        savedJson: String? = null,
        viewMode: Boolean = false
    ) {
        coEvery { repository.getAllByHhId(any()) } returns emptyList()
        coEvery { repository.getSavedSchema("MN1") } returns FormSchemaEntity(
            formId = "MN1",
            formName = "MosquitoNet",
            language = "en",
            version = 2,
            schemaJson = schema.toJson()
        )
        coEvery { repository.loadFormResponseJson(any(), any()) } returns savedJson
        viewModel.loadFormSchema(1L, "MN1", viewMode)
        advanceUntilIdle()
    }

    @Test
    fun `loadFormSchema publishes the cached schema`() = runTest {
        loadSchema(schemaOf(field("a")))
        assertEquals("MN1", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema falls back to the repository when nothing is cached`() = runTest {
        coEvery { repository.getAllByHhId(any()) } returns emptyList()
        coEvery { repository.getSavedSchema("MN1") } returns null
        coEvery { repository.getFormSchema("MN1") } returns schemaOf(field("a"))
        coEvery { repository.loadFormResponseJson(any(), any()) } returns null
        viewModel.loadFormSchema(1L, "MN1", false)
        advanceUntilIdle()
        assertNotNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema leaves schema null when nothing is available`() = runTest {
        coEvery { repository.getAllByHhId(any()) } returns emptyList()
        coEvery { repository.getSavedSchema("MN1") } returns null
        coEvery { repository.getFormSchema("MN1") } returns null
        viewModel.loadFormSchema(1L, "MN1", false)
        advanceUntilIdle()
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema applies saved values over defaults`() = runTest {
        loadSchema(
            schemaOf(field("nets", default = "1"), field("rooms", default = "2")),
            savedJson = """{"fields":{"nets":"5"}}"""
        )
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("5", fields.first { it.fieldId == "nets" }.value)
        assertEquals("2", fields.first { it.fieldId == "rooms" }.value)
    }

    @Test
    fun `loadFormSchema ignores malformed saved json`() = runTest {
        loadSchema(schemaOf(field("nets", default = "1")), savedJson = "not-json")
        assertEquals("1", viewModel.schema.value!!.sections.flatMap { it.fields }.first().value)
    }

    @Test
    fun `loadFormSchema injects visitDay and locks non-editable fields`() = runTest {
        viewModel.visitDay = "Day 2"
        loadSchema(schemaOf(field("visit_day"), field("due_date"), field("nets")))
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("Day 2", fields.first { it.fieldId == "visit_day" }.value)
        assertFalse(fields.first { it.fieldId == "visit_day" }.isEditable)
        assertFalse(fields.first { it.fieldId == "due_date" }.isEditable)
        assertTrue(fields.first { it.fieldId == "nets" }.isEditable)
    }

    @Test
    fun `loadFormSchema locks every field in view mode`() = runTest {
        loadSchema(schemaOf(field("nets"), field("rooms")), viewMode = true)
        assertTrue(viewModel.schema.value!!.sections.flatMap { it.fields }.none { it.isEditable })
    }

    @Test
    fun `loadFormSchema hides conditional fields whose dependency does not match`() = runTest {
        loadSchema(
            schemaOf(
                field("hasNet", value = "No"),
                field("netCount", conditional = ConditionalLogic(dependsOn = "hasNet", expectedValue = "Yes"))
            )
        )
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertFalse(fields.first { it.fieldId == "netCount" }.visible)
    }

    @Test
    fun `updateFieldValue writes the value and re-evaluates visibility`() = runTest {
        loadSchema(
            schemaOf(
                field("hasNet", value = "No"),
                field("netCount", conditional = ConditionalLogic(dependsOn = "hasNet", expectedValue = "Yes"))
            )
        )
        viewModel.updateFieldValue("hasNet", "Yes")
        val fields = viewModel.schema.value!!.sections.flatMap { it.fields }
        assertEquals("Yes", fields.first { it.fieldId == "hasNet" }.value)
        assertTrue(fields.first { it.fieldId == "netCount" }.visible)
    }

    @Test
    fun `updateFieldValue is a no-op when no schema is loaded`() {
        viewModel.updateFieldValue("x", "y")
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `getVisibleFields maps only visible fields`() = runTest {
        loadSchema(
            schemaOf(
                field("hasNet", value = "No"),
                field("netCount", conditional = ConditionalLogic(dependsOn = "hasNet", expectedValue = "Yes"))
            )
        )
        val visible = viewModel.getVisibleFields()
        assertEquals(1, visible.size)
        assertEquals("hasNet", visible.first().fieldId)
    }

    @Test
    fun `saveFormResponses returns false when no schema is loaded`() = runTest {
        assertFalse(viewModel.saveFormResponses(1L))
    }

    @Test
    fun `saveFormResponses persists visible fields and enqueues the sync worker`() = runTest {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
        loadSchema(
            schemaOf(
                field("visit_date", value = "05-01-2026"),
                field("nets", value = "3"),
                field("hidden", value = "x", conditional = ConditionalLogic(dependsOn = "nets", expectedValue = "nope"))
            )
        )
        val saved = slot<MosquitoNetFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(saved)) } returns true

        assertTrue(viewModel.saveFormResponses(77L))
        advanceUntilIdle()

        assertEquals(77L, saved.captured.hhId)
        assertEquals("05-01-2026", saved.captured.visitDate)
        assertEquals("MN1", saved.captured.formId)
        assertEquals(2, saved.captured.version)
        assertFalse(saved.captured.isSynced)
        val fields = JSONObject(saved.captured.formDataJson).getJSONObject("fields")
        assertEquals("3", fields.getString("nets"))
        assertFalse(fields.has("hidden"))
        unmockkObject(WorkManager.Companion)
    }

    @Test
    fun `saveFormResponses records N-A when visit_date is absent`() = runTest {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
        loadSchema(schemaOf(field("nets", value = "3")))
        val saved = slot<MosquitoNetFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(saved)) } returns true
        assertTrue(viewModel.saveFormResponses(1L))
        assertEquals("N/A", saved.captured.visitDate)
        unmockkObject(WorkManager.Companion)
    }

    @Test
    fun `saveFormResponses returns false when the repository rejects the insert`() = runTest {
        loadSchema(schemaOf(field("visit_date", value = "05-01-2026")))
        coEvery { repository.insertFormResponse(any()) } returns false
        assertFalse(viewModel.saveFormResponses(1L))
    }

    @Test
    fun `saveFormResponses returns false when the repository throws`() = runTest {
        loadSchema(schemaOf(field("visit_date", value = "05-01-2026")))
        coEvery { repository.insertFormResponse(any()) } throws RuntimeException("db down")
        assertFalse(viewModel.saveFormResponses(1L))
    }

    @Test
    fun `loadBottleData publishes the repository list`() = runTest {
        val bottles = listOf(BottleItem(srNo = 1, bottleNumber = "2", dateOfProvision = "01-01-2026"))
        coEvery { repository.getBottleList(9L) } returns bottles
        viewModel.loadBottleData(9L)
        advanceUntilIdle()
        assertEquals(bottles, viewModel.bottleList.value)
    }

    @Test
    fun `getMaxVisitDate is today when today has no visit yet`() = runTest {
        coEvery { repository.getAllByHhId(1L) } returns emptyList()
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
        coEvery { repository.getAllByHhId(1L) } returns listOf(
            entity(json = """{"fields":{"visit_date":"$today"}}""")
        )
        viewModel.loadSyncedVisitList(1L)
        advanceUntilIdle()
        assertTrue(viewModel.getMaxVisitDate().before(Date()))
    }

    @Test
    fun `getMaxVisitDate ignores rows with malformed or blank dates`() = runTest {
        coEvery { repository.getAllByHhId(1L) } returns listOf(
            entity(json = "not-json"),
            entity(json = """{"fields":{"visit_date":""}}""")
        )
        viewModel.loadSyncedVisitList(1L)
        advanceUntilIdle()
        assertNotNull(viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate is the day after the previous visit`() {
        val previous = Calendar.getInstance().apply { add(Calendar.DATE, -5) }.time
        viewModel.previousVisitDate = previous
        val expected = Calendar.getInstance().apply {
            time = previous
            add(Calendar.DATE, 1)
        }.time
        assertEquals(expected, viewModel.getMinVisitDate())
    }

    @Test
    fun `isBenDead defaults to false`() {
        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `infant is initially null`() {
        assertNull(viewModel.infant.value)
    }
}
