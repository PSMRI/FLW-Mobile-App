package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
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
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic as SchemaConditional
import org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: CUFYFormRepository

    private lateinit var viewModel: CUFYFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = CUFYFormViewModel(repository)
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
    fun `initial infant is null`() {
        assertNull(viewModel.infant.value)
    }

    @Test
    fun `initial isBenDead is false`() {
        assertEquals(false, viewModel.isBenDead.value)
    }

    @Test
    fun `initial saveFormState is Idle`() {
        assertEquals(CUFYFormViewModel.SaveFormState.Idle, viewModel.saveFormState.value)
    }

    @Test
    fun `initial visitDay is empty`() {
        assertEquals("", viewModel.visitDay)
    }

    // =====================================================
    // setRecordId() Tests
    // =====================================================

    @Test
    fun `setRecordId does not throw`() {
        viewModel.setRecordId(42)
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields returns empty when no schema`() {
        val result = viewModel.getVisibleFields()
        assertEquals(0, result.size)
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
    // Fixtures / helpers
    // =====================================================

    private val benIdFixture = 51L
    private val hhIdFixture = 61L
    private val formIdFixture = "cufy_form"

    private fun fieldOf(
        id: String,
        type: String = "text",
        defaultValue: String? = null,
        options: Any? = null,
        required: Boolean = false,
        placeholder: String? = null,
        validation: FieldValidationDto? = null,
        conditional: SchemaConditional? = null
    ) = FormFieldDto(
        fieldId = id,
        label = "label-$id",
        type = type,
        options = options,
        required = required,
        conditional = conditional,
        validation = validation,
        placeholder = placeholder,
        defaultValue = defaultValue,
        value = null
    )

    private fun schemaOf(
        vararg fields: FormFieldDto,
        formId: String = "cufy_form",
        version: Int = 5
    ) = FormSchemaDto(
        formId = formId,
        formName = "CUFY",
        version = version,
        sections = listOf(
            FormSectionDto(
                sectionId = "s1",
                sectionTitle = "Section 1",
                fields = fields.toList()
            )
        )
    )

    private fun responseJson(vararg pairs: Pair<String, String>): String {
        val fields = pairs.joinToString(",") { "\"${it.first}\":\"${it.second}\"" }
        return "{\"visitDate\":\"05-01-2024\",\"fields\":{$fields}}"
    }

    private fun savedEntity(
        json: String = responseJson("visit_date" to "05-01-2024"),
        id: Int = 0,
        formId: String = formIdFixture
    ) = CUFYFormResponseJsonEntity(
        id = id,
        benId = benIdFixture,
        hhId = hhIdFixture,
        visitDate = "2024-01-05",
        formId = formId,
        version = 1,
        formDataJson = json
    )

    private fun stubSchema(
        schema: FormSchemaDto?,
        savedJson: String? = null,
        formId: String = formIdFixture
    ) {
        coEvery { repository.getSavedSchema(formId) } returns null
        coEvery { repository.getFormSchema(formId) } returns schema
        coEvery { repository.loadFormResponseJson(any(), any()) } returns savedJson
        coEvery { repository.getSavedDataByFormId(any(), any()) } returns emptyList()
    }

    private fun loadedFields() = viewModel.schema.value!!.sections.first().fields

    private fun fieldNamed(id: String) = loadedFields().first { it.fieldId == id }

    private fun todayMidnight(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun ddMMyyyy() = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    // =====================================================
    // loadBottleData() Tests
    // =====================================================

    @Test
    fun `loadBottleData publishes the bottle list`() = runTest {
        coEvery { repository.getBottleList(benIdFixture, "ifa_form_001") } returns
                listOf(BottleItem(1, "B-1", "01-01-2024"), BottleItem(2, "B-2", "01-02-2024"))

        viewModel.loadBottleData(benIdFixture, "ifa_form_001")
        advanceUntilIdle()

        assertEquals(2, viewModel.bottleList.value?.size)
        assertEquals("B-1", viewModel.bottleList.value?.first()?.bottleNumber)
    }

    @Test
    fun `loadBottleData publishes an empty list when nothing is stored`() = runTest {
        coEvery { repository.getBottleList(any(), any()) } returns emptyList()

        viewModel.loadBottleData(benIdFixture, "ifa_form_001")
        advanceUntilIdle()

        assertTrue(viewModel.bottleList.value?.isEmpty() == true)
    }

    // =====================================================
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema publishes the remote schema when nothing is cached`() = runTest {
        stubSchema(schemaOf(fieldOf("visit_day"), fieldOf("weight")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertEquals(formIdFixture, viewModel.schema.value?.formId)
        assertEquals("Visit 1", viewModel.visitDay)
        assertEquals("Visit 1", fieldNamed("visit_day").value)
        assertFalse(fieldNamed("visit_day").isEditable)
        assertTrue(fieldNamed("weight").isEditable)
    }

    @Test
    fun `loadFormSchema prefers the cached schema`() = runTest {
        val cached = schemaOf(fieldOf("weight"), formId = "cached_cufy")
        coEvery { repository.getSavedSchema(formIdFixture) } returns FormSchemaEntity(
            formId = formIdFixture,
            formName = "CUFY",
            language = "en",
            version = 1,
            schemaJson = cached.toJson()
        )
        coEvery { repository.getFormSchema(any()) } returns null
        coEvery { repository.loadFormResponseJson(any(), any()) } returns null

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertEquals("cached_cufy", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema returns early when no schema can be resolved`() = runTest {
        stubSchema(null)

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema restores previously saved values`() = runTest {
        stubSchema(
            schemaOf(fieldOf("weight"), fieldOf("height")),
            savedJson = responseJson("weight" to "9.5")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertEquals("9.5", fieldNamed("weight").value)
        assertNull(fieldNamed("height").value)
    }

    @Test
    fun `loadFormSchema ignores malformed saved json`() = runTest {
        stubSchema(schemaOf(fieldOf("weight")), savedJson = "broken")

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertNull(fieldNamed("weight").value)
    }

    @Test
    fun `loadFormSchema marks fields read only in view mode`() = runTest {
        stubSchema(schemaOf(fieldOf("weight")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", true)
        advanceUntilIdle()

        assertFalse(fieldNamed("weight").isEditable)
    }

    @Test
    fun `loadFormSchema evaluates conditional visibility`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("has_ors"),
                fieldOf("ors_packets", conditional = SchemaConditional("has_ors", "Yes"))
            ),
            savedJson = responseJson("has_ors" to "Yes")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertTrue(fieldNamed("ors_packets").visible)
    }

    @Test
    fun `loadFormSchema hides a conditional field when the dependency does not match`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("has_ors"),
                fieldOf("ors_packets", conditional = SchemaConditional("has_ors", "Yes"))
            ),
            savedJson = responseJson("has_ors" to "No")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        assertFalse(fieldNamed("ors_packets").visible)
    }

    @Test
    fun `loadFormSchema numbers the ifa bottle from previous visits`() = runTest {
        val ifaId = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID
        coEvery { repository.getSavedSchema(ifaId) } returns null
        coEvery { repository.getFormSchema(ifaId) } returns
                schemaOf(fieldOf("ifa_bottle_count", type = "number"), formId = ifaId)
        coEvery { repository.loadFormResponseJson(any(), any()) } returns null
        coEvery { repository.getSavedDataByFormId(ifaId, benIdFixture) } returns
                listOf(savedEntity(formId = ifaId), savedEntity(formId = ifaId))

        viewModel.loadFormSchema(benIdFixture, ifaId, "Visit 1", false)
        advanceUntilIdle()

        assertEquals(3, fieldNamed("ifa_bottle_count").value)
    }

    // =====================================================
    // loadFormSchemaFromJson() Tests
    // =====================================================

    @Test
    fun `loadFormSchemaFromJson maps ors numbers dates and text`() = runTest {
        val orsId = FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID
        coEvery { repository.getSavedSchema(orsId) } returns null
        coEvery { repository.getFormSchema(orsId) } returns schemaOf(
            fieldOf("packets", type = "number", defaultValue = "1"),
            fieldOf("missing_number", type = "number", defaultValue = "7"),
            fieldOf("given_on", type = "date"),
            fieldOf("missing_date", type = "date"),
            fieldOf("remark", defaultValue = "none"),
            formId = orsId
        )

        viewModel.loadFormSchemaFromJson(
            benIdFixture,
            orsId,
            "Visit 1",
            false,
            responseJson("packets" to "3", "given_on" to "05-01-2024")
        )
        advanceUntilIdle()

        assertEquals(3.0, fieldNamed("packets").value as Double, 0.0001)
        assertEquals("7", fieldNamed("missing_number").value)
        assertEquals("05-01-2024", fieldNamed("given_on").value)
        assertEquals("", fieldNamed("missing_date").value)
        assertEquals("none", fieldNamed("remark").value)
    }

    @Test
    fun `loadFormSchemaFromJson keeps only content uris for ifa images`() = runTest {
        val ifaId = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID
        coEvery { repository.getSavedSchema(ifaId) } returns null
        coEvery { repository.getFormSchema(ifaId) } returns schemaOf(
            fieldOf("proof", type = "image"),
            fieldOf("other_proof", type = "image"),
            fieldOf("bottle_no", type = "number", defaultValue = "1"),
            formId = ifaId
        )

        viewModel.loadFormSchemaFromJson(
            benIdFixture,
            ifaId,
            "Visit 1",
            false,
            responseJson(
                "proof" to "content://media/1",
                "other_proof" to "http://example.com/a.png",
                "bottle_no" to "2"
            )
        )
        advanceUntilIdle()

        assertEquals("content://media/1", fieldNamed("proof").value)
        assertEquals("", fieldNamed("other_proof").value)
        assertEquals(2.0, fieldNamed("bottle_no").value as Double, 0.0001)
    }

    @Test
    fun `loadFormSchemaFromJson keeps only content uris for sam images`() = runTest {
        val samId = FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID
        coEvery { repository.getSavedSchema(samId) } returns null
        coEvery { repository.getFormSchema(samId) } returns schemaOf(
            fieldOf("proof", type = "image"),
            fieldOf("preview", type = "view_image"),
            fieldOf("remark"),
            formId = samId
        )

        viewModel.loadFormSchemaFromJson(
            benIdFixture,
            samId,
            "Visit 1",
            false,
            responseJson(
                "proof" to "content://media/2",
                "preview" to "file://tmp/x.png",
                "remark" to "ok"
            )
        )
        advanceUntilIdle()

        assertEquals("content://media/2", fieldNamed("proof").value)
        assertEquals("", fieldNamed("preview").value)
        assertEquals("ok", fieldNamed("remark").value)
    }

    @Test
    fun `loadFormSchemaFromJson falls back to defaults for an unknown form`() = runTest {
        stubSchema(schemaOf(fieldOf("weight", defaultValue = "5"), fieldOf("height")))

        viewModel.loadFormSchemaFromJson(
            benIdFixture, formIdFixture, "Visit 2", false, responseJson("height" to "70")
        )
        advanceUntilIdle()

        assertEquals("5", fieldNamed("weight").value)
        assertEquals("70", fieldNamed("height").value)
        assertEquals("Visit 2", viewModel.visitDay)
    }

    @Test
    fun `loadFormSchemaFromJson tolerates malformed json`() = runTest {
        stubSchema(schemaOf(fieldOf("weight", defaultValue = "5")))

        viewModel.loadFormSchemaFromJson(benIdFixture, formIdFixture, "Visit 1", false, "broken")
        advanceUntilIdle()

        assertEquals("5", fieldNamed("weight").value)
    }

    @Test
    fun `loadFormSchemaFromJson returns early when no schema can be resolved`() = runTest {
        stubSchema(null)

        viewModel.loadFormSchemaFromJson(benIdFixture, formIdFixture, "Visit 1", false, "{}")
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue stores the value and re-evaluates visibility`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("has_ors"),
                fieldOf("ors_packets", conditional = SchemaConditional("has_ors", "Yes"))
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        viewModel.updateFieldValue("has_ors", "Yes")

        assertEquals("Yes", fieldNamed("has_ors").value)
        assertTrue(fieldNamed("ors_packets").visible)
    }

    @Test
    fun `updateFieldValue ignores an unknown field id`() = runTest {
        stubSchema(schemaOf(fieldOf("weight")))
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        viewModel.updateFieldValue("nope", "x")

        assertNull(fieldNamed("weight").value)
    }

    // =====================================================
    // saveFormResponses() Tests
    // =====================================================

    @Test
    fun `saveFormResponses does nothing when the schema is not loaded`() = runTest {
        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.insertFormResponse(any()) }
        assertEquals(CUFYFormViewModel.SaveFormState.Idle, viewModel.saveFormState.value)
    }

    @Test
    fun `saveFormResponses converts the visit date and reports success`() = runTest {
        stubSchema(
            schemaOf(fieldOf("visit_date"), fieldOf("weight")),
            savedJson = responseJson("visit_date" to "15-03-2024", "weight" to "9.5")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        val captured = slot<CUFYFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("2024-03-15", captured.captured.visitDate)
        assertEquals(0, captured.captured.id)
        assertEquals(benIdFixture, captured.captured.benId)
        assertEquals(hhIdFixture, captured.captured.hhId)
        assertEquals(5, captured.captured.version)
        assertFalse(captured.captured.isSynced)
        assertEquals(CUFYFormViewModel.SaveFormState.Success, viewModel.saveFormState.value)
    }

    @Test
    fun `saveFormResponses reuses the record id when updating`() = runTest {
        stubSchema(
            schemaOf(fieldOf("visit_date")),
            savedJson = responseJson("visit_date" to "15-03-2024")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        val captured = slot<CUFYFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture, 7)
        advanceUntilIdle()

        assertEquals(7, captured.captured.id)
    }

    @Test
    fun `saveFormResponses keeps the raw value when the date cannot be parsed`() = runTest {
        stubSchema(schemaOf(fieldOf("weight")), savedJson = responseJson("weight" to "9.5"))
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        val captured = slot<CUFYFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("N/A", captured.captured.visitDate)
    }

    @Test
    fun `saveFormResponses falls back to the ifa provision date`() = runTest {
        val ifaId = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID
        coEvery { repository.getSavedSchema(ifaId) } returns null
        coEvery { repository.getFormSchema(ifaId) } returns
                schemaOf(fieldOf("ifa_provision_date"), formId = ifaId)
        coEvery { repository.loadFormResponseJson(any(), any()) } returns
                responseJson("ifa_provision_date" to "15-03-2024")
        coEvery { repository.getSavedDataByFormId(any(), any()) } returns emptyList()

        viewModel.loadFormSchema(benIdFixture, ifaId, "Visit 1", false)
        advanceUntilIdle()

        val captured = slot<CUFYFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("2024-03-15", captured.captured.visitDate)
    }

    @Test
    fun `saveFormResponses serialises follow up dates as an array`() = runTest {
        stubSchema(
            schemaOf(fieldOf("visit_date"), fieldOf("follow_up_visit_date")),
            savedJson = responseJson(
                "visit_date" to "15-03-2024",
                "follow_up_visit_date" to "16-03-2024, 17-03-2024"
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        val captured = slot<CUFYFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertTrue(captured.captured.formDataJson.contains("16-03-2024"))
        assertTrue(captured.captured.formDataJson.contains("17-03-2024"))
        assertTrue(captured.captured.formDataJson.contains("["))
    }

    @Test
    fun `saveFormResponses reports an error when persisting fails`() = runTest {
        stubSchema(
            schemaOf(fieldOf("visit_date")),
            savedJson = responseJson("visit_date" to "15-03-2024")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        coEvery { repository.insertFormResponse(any()) } throws RuntimeException("disk full")

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.saveFormState.value is CUFYFormViewModel.SaveFormState.Error)
    }

    @Test
    fun `saveFormResponses skips hidden and empty fields`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("visit_date"),
                fieldOf("hidden_field", conditional = SchemaConditional("visit_date", "never"))
            ),
            savedJson = responseJson("visit_date" to "15-03-2024", "hidden_field" to "secret")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        val captured = slot<CUFYFormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertFalse(captured.captured.formDataJson.contains("hidden_field"))
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields maps options validation and drops hidden fields`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf(
                    "has_ors",
                    type = "radio",
                    defaultValue = "Yes",
                    options = listOf("Yes", "No"),
                    required = true,
                    placeholder = "pick one",
                    validation = FieldValidationDto(
                        min = 1f,
                        max = 5f,
                        maxLength = 3,
                        regex = "\\d+",
                        errorMessage = "bad value",
                        decimalPlaces = 1,
                        maxSizeMB = 2,
                        afterField = "a",
                        beforeField = "b"
                    )
                ),
                fieldOf("ors_packets", conditional = SchemaConditional("has_ors", "No"))
            ),
            savedJson = responseJson("has_ors" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "Visit 1", false)
        advanceUntilIdle()

        val visible = viewModel.getVisibleFields()

        assertEquals(1, visible.size)
        val mapped = visible.first()
        assertEquals("has_ors", mapped.fieldId)
        assertEquals("Yes", mapped.defaultValue)
        assertEquals(2, mapped.options?.size)
        assertTrue(mapped.isRequired)
        assertEquals("pick one", mapped.placeholder)
        assertEquals("bad value", mapped.validation?.errorMessage)
        assertNull(mapped.conditional)
    }

    // =====================================================
    // Visit date range Tests
    // =====================================================

    @Test
    fun `getMaxVisitDate returns today when no visit was recorded today`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns
                listOf(savedEntity(responseJson("visit_date" to "05-01-2020")))
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(todayMidnight(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMaxVisitDate rolls back a day when today is already recorded`() = runTest {
        val today = ddMMyyyy().format(Date())
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns
                listOf(savedEntity(responseJson("visit_date" to today)))
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.getMaxVisitDate().before(todayMidnight()))
    }

    @Test
    fun `getMaxVisitDate ignores malformed stored responses`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns
                listOf(savedEntity("broken"), savedEntity(responseJson()))
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(todayMidnight(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate returns the day after the previous visit`() {
        val previous = ddMMyyyy().parse("05-01-2024")!!
        viewModel.previousVisitDate = previous

        val expected = Calendar.getInstance().apply {
            time = previous
            add(Calendar.DATE, 1)
        }.time

        assertEquals(expected, viewModel.getMinVisitDate())
    }

    // =====================================================
    // getPreviousIFAVisitDate() / getFormsDataByFormID() Tests
    // =====================================================

    @Test
    fun `getPreviousIFAVisitDate returns the latest provision date`() = runTest {
        coEvery {
            repository.getSavedDataByFormId(
                FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID, benIdFixture
            )
        } returns listOf(
            savedEntity(responseJson("ifa_provision_date" to "01-01-2024")),
            savedEntity(responseJson("ifa_provision_date" to "05-02-2024")),
            savedEntity(responseJson("ifa_provision_date" to "10-01-2024"))
        )

        assertEquals(
            ddMMyyyy().parse("05-02-2024"),
            viewModel.getPreviousIFAVisitDate(benIdFixture)
        )
    }

    @Test
    fun `getPreviousIFAVisitDate returns null when nothing was saved`() = runTest {
        coEvery { repository.getSavedDataByFormId(any(), any()) } returns emptyList()

        assertNull(viewModel.getPreviousIFAVisitDate(benIdFixture))
    }

    @Test
    fun `getPreviousIFAVisitDate skips malformed and blank entries`() = runTest {
        coEvery { repository.getSavedDataByFormId(any(), any()) } returns listOf(
            savedEntity("broken"),
            savedEntity(responseJson())
        )

        assertNull(viewModel.getPreviousIFAVisitDate(benIdFixture))
    }

    @Test
    fun `getFormsDataByFormID delegates to the repository`() = runTest {
        val stored = listOf(savedEntity())
        coEvery { repository.getSavedDataByFormId("ors_form_001", benIdFixture) } returns stored

        assertEquals(stored, viewModel.getFormsDataByFormID("ors_form_001", benIdFixture))
    }
}
