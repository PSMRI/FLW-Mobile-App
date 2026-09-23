package org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic as SchemaConditional
import org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HBNCFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FormRepository
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var infantRegRepo: InfantRegRepo

    private lateinit var viewModel: HBNCFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = HBNCFormViewModel(repository, benRepo, infantRegRepo)
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

    // =====================================================
    // Fixtures / helpers
    // =====================================================

    private val benIdFixture = 11L
    private val hhIdFixture = 22L
    private val dobFixture = 1_700_000_000_000L
    private val formIdFixture = "hbnc_form"

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
        formId: String = "hbnc_form",
        version: Int = 3
    ) = FormSchemaDto(
        formId = formId,
        formName = "HBNC",
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

    private fun visitEntity(
        visitDay: String,
        benId: Long = benIdFixture,
        json: String = responseJson("visit_date" to "05-01-2024")
    ) = FormResponseJsonEntity(
        benId = benId,
        hhId = hhIdFixture,
        visitDay = visitDay,
        visitDate = "05-01-2024",
        formId = formIdFixture,
        version = 1,
        formDataJson = json
    )

    private fun stubSchema(schema: FormSchemaDto?, savedJson: String? = null) {
        coEvery { repository.getSavedSchema(formIdFixture) } returns null
        coEvery { repository.getFormSchema(formIdFixture, "en") } returns schema
        coEvery { repository.loadFormResponseJson(any(), any()) } returns savedJson
    }

    private fun loadedFields() = viewModel.schema.value!!.sections.first().fields

    private fun fieldNamed(id: String) = loadedFields().first { it.fieldId == id }

    private fun dobPlusDays(days: Int): Long =
        Calendar.getInstance().apply {
            time = Date(dobFixture)
            add(Calendar.DAY_OF_MONTH, days)
        }.timeInMillis

    private fun todayMidnight(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun ddMMyyyy() = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    // =====================================================
    // fetchSNCUStatus() Tests
    // =====================================================

    @Test
    fun `fetchSNCUStatus sets isSNCU true when infant record is SNCU`() = runTest {
        val infant = mockk<InfantRegCache>(relaxed = true)
        every { infant.isSNCU } returns "yes"
        coEvery { infantRegRepo.getInfantReg(benIdFixture, 1) } returns infant

        viewModel.fetchSNCUStatus(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.isSNCU.value)
    }

    @Test
    fun `fetchSNCUStatus sets isSNCU false when infant record is missing`() = runTest {
        coEvery { infantRegRepo.getInfantReg(any(), any()) } returns null

        viewModel.fetchSNCUStatus(benIdFixture)
        advanceUntilIdle()

        assertFalse(viewModel.isSNCU.value)
    }

    @Test
    fun `fetchSNCUStatus sets isSNCU false when infant is not SNCU`() = runTest {
        val infant = mockk<InfantRegCache>(relaxed = true)
        every { infant.isSNCU } returns "No"
        coEvery { infantRegRepo.getInfantReg(any(), any()) } returns infant

        viewModel.fetchSNCUStatus(benIdFixture)
        advanceUntilIdle()

        assertFalse(viewModel.isSNCU.value)
    }

    // =====================================================
    // navigateToCdsr Tests
    // =====================================================

    @Test
    fun `triggerPopBack sets navigateToCdsr to false`() {
        viewModel.triggerPopBack()

        assertEquals(false, viewModel.navigateToCdsr.value)
    }

    @Test
    fun `onNavigationComplete clears navigateToCdsr`() {
        viewModel.triggerPopBack()
        viewModel.onNavigationComplete()

        assertNull(viewModel.navigateToCdsr.value)
    }

    // =====================================================
    // loadSyncedVisitList() Tests
    // =====================================================

    @Test
    fun `loadSyncedVisitList publishes visits from repository`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(benIdFixture) } returns
                listOf(visitEntity("1st Day"), visitEntity("3rd Day"))

        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(2, viewModel.syncedVisitList.value.size)
        assertEquals("1st Day", viewModel.syncedVisitList.value.first().visitDay)
    }

    @Test
    fun `loadSyncedVisitList publishes empty list when nothing synced`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()

        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.syncedVisitList.value.isEmpty())
    }

    // =====================================================
    // loadInfant() Tests
    // =====================================================

    @Test
    fun `loadInfant publishes the first stored response`() = runTest {
        coEvery { repository.getInfantByRchId(benIdFixture) } returns
                listOf(visitEntity("1st Day"), visitEntity("3rd Day"))

        viewModel.loadInfant(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("1st Day", viewModel.infant.value?.visitDay)
    }

    @Test
    fun `loadInfant publishes null when no stored response exists`() = runTest {
        coEvery { repository.getInfantByRchId(any()) } returns emptyList()

        viewModel.loadInfant(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertNull(viewModel.infant.value)
    }

    // =====================================================
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema publishes remote schema when nothing is cached`() = runTest {
        stubSchema(schemaOf(fieldOf("weight", type = "number", defaultValue = "3")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals(formIdFixture, viewModel.schema.value?.formId)
        assertEquals("1st Day", viewModel.visitDay)
    }

    @Test
    fun `loadFormSchema prefers the cached schema over the remote one`() = runTest {
        val cached = schemaOf(fieldOf("weight"), formId = "cached_form")
        coEvery { repository.getSavedSchema(formIdFixture) } returns FormSchemaEntity(
            formId = formIdFixture,
            formName = "HBNC",
            language = "en",
            version = 1,
            schemaJson = cached.toJson()
        )
        coEvery { repository.getFormSchema(any(), any()) } returns null
        coEvery { repository.loadFormResponseJson(any(), any()) } returns null

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("cached_form", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema returns early when no schema can be resolved`() = runTest {
        stubSchema(null)

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema fills visit_day and due_date and locks them`() = runTest {
        stubSchema(schemaOf(fieldOf("visit_day"), fieldOf("due_date")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3rd Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("3rd Day", fieldNamed("visit_day").value)
        assertEquals(ddMMyyyy().format(Date(dobPlusDays(2))), fieldNamed("due_date").value)
        assertFalse(fieldNamed("visit_day").isEditable)
        assertFalse(fieldNamed("due_date").isEditable)
    }

    @Test
    fun `loadFormSchema leaves due_date blank for an unknown visit day`() = runTest {
        stubSchema(schemaOf(fieldOf("due_date")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "99th Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("", fieldNamed("due_date").value)
    }

    @Test
    fun `loadFormSchema restores previously saved field values`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("weight", type = "number", defaultValue = "0"),
                fieldOf("temperature", defaultValue = "98")
            ),
            savedJson = responseJson("weight" to "3.2")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("3.2", fieldNamed("weight").value)
        assertEquals("98", fieldNamed("temperature").value)
    }

    @Test
    fun `loadFormSchema falls back to defaults when saved json is malformed`() = runTest {
        stubSchema(schemaOf(fieldOf("weight", defaultValue = "0")), savedJson = "not-a-json")

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("0", fieldNamed("weight").value)
    }

    @Test
    fun `loadFormSchema leaves radio and dropdown unset when nothing was saved`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("radio_field", type = "radio", defaultValue = "Yes"),
                fieldOf("dropdown_field", type = "dropdown", defaultValue = "A"),
                fieldOf("text_field", type = "text", defaultValue = "T")
            )
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertNull(fieldNamed("radio_field").value)
        assertNull(fieldNamed("dropdown_field").value)
        assertEquals("T", fieldNamed("text_field").value)
    }

    @Test
    fun `loadFormSchema marks every field read only in view mode`() = runTest {
        stubSchema(schemaOf(fieldOf("weight"), fieldOf("temperature")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", true, dobFixture, "en")
        advanceUntilIdle()

        assertFalse(fieldNamed("weight").isEditable)
        assertFalse(fieldNamed("temperature").isEditable)
    }

    @Test
    fun `loadFormSchema hides a conditional field when its dependency does not match`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertTrue(fieldNamed("is_baby_alive").visible)
        assertFalse(fieldNamed("reason_for_death").visible)
    }

    @Test
    fun `loadFormSchema shows a conditional field when its dependency matches`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("is_baby_alive" to "No")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        assertTrue(fieldNamed("reason_for_death").visible)
    }

    @Test
    fun `loadFormSchema keeps a field visible when the conditional has a blank dependency`() =
        runTest {
            stubSchema(schemaOf(fieldOf("notes", conditional = SchemaConditional("", "No"))))

            viewModel.loadFormSchema(
                benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en"
            )
            advanceUntilIdle()

            assertTrue(fieldNamed("notes").visible)
        }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue does nothing when the schema is not loaded`() {
        viewModel.updateFieldValue("weight", "3.2")

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `updateFieldValue stores the value and re-evaluates visibility`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive", type = "radio"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        viewModel.updateFieldValue("is_baby_alive", "No")

        assertEquals("No", fieldNamed("is_baby_alive").value)
        assertTrue(fieldNamed("reason_for_death").visible)
    }

    @Test
    fun `updateFieldValue ignores an unknown field id`() = runTest {
        stubSchema(schemaOf(fieldOf("weight")))
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        viewModel.updateFieldValue("does_not_exist", "x")

        assertNull(fieldNamed("weight").value)
    }

    @Test
    fun `updateFieldValue auto fills discharged_from_sncu for an sncu baby`() = runTest {
        val infant = mockk<InfantRegCache>(relaxed = true)
        every { infant.isSNCU } returns "Yes"
        coEvery { infantRegRepo.getInfantReg(any(), any()) } returns infant
        viewModel.fetchSNCUStatus(benIdFixture)
        advanceUntilIdle()

        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive", type = "radio"),
                fieldOf("discharged_from_sncu", type = "radio")
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        viewModel.updateFieldValue("is_baby_alive", "Yes")

        assertEquals("Yes", fieldNamed("discharged_from_sncu").value)
    }

    @Test
    fun `updateFieldValue leaves discharged_from_sncu untouched for a non sncu baby`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive", type = "radio"),
                fieldOf("discharged_from_sncu", type = "radio")
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        viewModel.updateFieldValue("is_baby_alive", "Yes")

        assertNull(fieldNamed("discharged_from_sncu").value)
    }

    // =====================================================
    // saveFormResponses() Tests
    // =====================================================

    @Test
    fun `saveFormResponses does nothing when the schema is not loaded`() = runTest {
        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.insertFormResponse(any()) }
    }

    @Test
    fun `saveFormResponses stores the response and pops back for a live baby`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("visit_date"),
                fieldOf("is_baby_alive", type = "radio"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("visit_date" to "05-01-2024", "is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        val captured = slot<FormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("05-01-2024", captured.captured.visitDate)
        assertEquals("1st Day", captured.captured.visitDay)
        assertEquals(benIdFixture, captured.captured.benId)
        assertEquals(hhIdFixture, captured.captured.hhId)
        assertEquals(3, captured.captured.version)
        assertFalse(captured.captured.isSynced)
        assertFalse(captured.captured.formDataJson.contains("reason_for_death"))
        assertEquals(false, viewModel.navigateToCdsr.value)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveFormResponses uses N A when no visit date was captured`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        val captured = slot<FormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("N/A", captured.captured.visitDate)
    }

    @Test
    fun `saveFormResponses marks the beneficiary dead and navigates to cdsr`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("visit_date"),
                fieldOf("is_baby_alive", type = "radio"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No")),
                fieldOf("place_of_death", conditional = SchemaConditional("is_baby_alive", "No")),
                fieldOf(
                    "other_place_of_death",
                    conditional = SchemaConditional("is_baby_alive", "No")
                ),
                fieldOf("date_of_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson(
                "visit_date" to "05-01-2024",
                "is_baby_alive" to "No",
                "reason_for_death" to "Illness",
                "place_of_death" to "Other",
                "other_place_of_death" to "Roadside",
                "date_of_death" to "04-01-2024"
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(benIdFixture) } returns ben
        coEvery { repository.insertFormResponse(any()) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        coVerify(exactly = 1) { benRepo.updateRecord(ben) }
        assertEquals(true, viewModel.navigateToCdsr.value)
    }

    @Test
    fun `saveFormResponses skips the death update when the beneficiary is missing`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "No")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { repository.insertFormResponse(any()) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
        assertNull(viewModel.navigateToCdsr.value)
        coVerify(exactly = 1) { repository.insertFormResponse(any()) }
    }

    @Test
    fun `saveFormResponses still stores the response when the death update fails`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "No")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        coEvery { benRepo.getBenFromId(any()) } throws RuntimeException("db down")
        val captured = slot<FormResponseJsonEntity>()
        coEvery { repository.insertFormResponse(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertTrue(captured.isCaptured)
        assertNull(viewModel.navigateToCdsr.value)
    }

    @Test
    fun `saveFormResponses refreshes the synced visit list`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        coEvery { repository.insertFormResponse(any()) } returns Unit
        coEvery { repository.getSyncedVisitsByRchId(benIdFixture) } returns
                listOf(visitEntity("1st Day"))

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals(1, viewModel.syncedVisitList.value.size)
    }

    // =====================================================
    // calculateDueDate() / formatDate() Tests
    // =====================================================

    @Test
    fun `calculateDueDate offsets the date of birth per visit day`() {
        assertEquals(dobPlusDays(0), viewModel.calculateDueDate(dobFixture, "1st Day")!!)
        assertEquals(dobPlusDays(2), viewModel.calculateDueDate(dobFixture, "3rd Day")!!)
        assertEquals(dobPlusDays(6), viewModel.calculateDueDate(dobFixture, "7th Day")!!)
        assertEquals(dobPlusDays(13), viewModel.calculateDueDate(dobFixture, "14th Day")!!)
        assertEquals(dobPlusDays(20), viewModel.calculateDueDate(dobFixture, "21st Day")!!)
        assertEquals(dobPlusDays(27), viewModel.calculateDueDate(dobFixture, "28th Day")!!)
        assertEquals(dobPlusDays(41), viewModel.calculateDueDate(dobFixture, "42nd Day")!!)
    }

    @Test
    fun `calculateDueDate trims the visit day before matching`() {
        assertEquals(dobPlusDays(6), viewModel.calculateDueDate(dobFixture, "  7th Day  ")!!)
    }

    @Test
    fun `calculateDueDate returns null for an unknown visit day`() {
        assertNull(viewModel.calculateDueDate(dobFixture, "unknown"))
    }

    @Test
    fun `formatDate renders the epoch as dd-MM-yyyy`() {
        val formatted = viewModel.formatDate(dobFixture)

        assertTrue(formatted.matches(Regex("\\d{2}-\\d{2}-\\d{4}")))
        assertEquals(ddMMyyyy().format(Date(dobFixture)), formatted)
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields returns empty list when the schema is absent`() {
        assertTrue(viewModel.getVisibleFields().isEmpty())
    }

    @Test
    fun `getVisibleFields maps options validation and drops hidden fields`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf(
                    "is_baby_alive",
                    type = "radio",
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
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        val visible = viewModel.getVisibleFields()

        assertEquals(1, visible.size)
        val mapped = visible.first()
        assertEquals("is_baby_alive", mapped.fieldId)
        assertEquals(2, mapped.options?.size)
        assertTrue(mapped.isRequired)
        assertEquals("pick one", mapped.placeholder)
        assertEquals("bad value", mapped.validation?.errorMessage)
        assertEquals(3, mapped.validation?.maxLength)
        assertNull(mapped.conditional)
        assertEquals("Yes", mapped.value)
    }

    @Test
    fun `getVisibleFields keeps conditional metadata for visible dependent fields`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("is_baby_alive" to "No")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "1st Day", false, dobFixture, "en")
        advanceUntilIdle()

        val dependent = viewModel.getVisibleFields().first { it.fieldId == "reason_for_death" }

        assertEquals("is_baby_alive", dependent.conditional?.dependsOn)
        assertEquals("No", dependent.conditional?.expectedValue)
    }

    // =====================================================
    // getVisitCardList() Tests
    // =====================================================

    @Test
    fun `getVisitCardList only unlocks the first day when nothing is done`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val cards = viewModel.getVisitCardList(benIdFixture)

        assertEquals(7, cards.size)
        assertTrue(cards.first { it.visitDay == "1st Day" }.isEditable)
        assertFalse(cards.first { it.visitDay == "3rd Day" }.isEditable)
        assertFalse(cards.first { it.visitDay == "42nd Day" }.isEditable)
        assertEquals("-", cards.first().visitDate)
        assertFalse(cards.first().isCompleted)
    }

    @Test
    fun `getVisitCardList unlocks later visits as the earlier ones complete`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("1st Day"), visitEntity("3rd Day"), visitEntity("7th Day")
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val cards = viewModel.getVisitCardList(benIdFixture).associateBy { it.visitDay }

        assertTrue(cards.getValue("1st Day").isCompleted)
        assertFalse(cards.getValue("1st Day").isEditable)
        assertTrue(cards.getValue("14th Day").isEditable)
        assertTrue(cards.getValue("21st Day").isEditable)
        assertTrue(cards.getValue("28th Day").isEditable)
        assertTrue(cards.getValue("42nd Day").isEditable)
        assertEquals("05-01-2024", cards.getValue("1st Day").visitDate)
    }

    @Test
    fun `getVisitCardList flags a baby death recorded in the stored response`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("1st Day", json = responseJson("is_baby_alive" to "No"))
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val card = viewModel.getVisitCardList(benIdFixture).first { it.visitDay == "1st Day" }

        assertTrue(card.isBabyDeath)
        assertTrue(card.isCompleted)
    }

    @Test
    fun `getVisitCardList ignores visits belonging to another beneficiary`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("1st Day", benId = 999L)
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val card = viewModel.getVisitCardList(benIdFixture).first { it.visitDay == "1st Day" }

        assertFalse(card.isCompleted)
        assertTrue(card.isEditable)
        assertEquals("-", card.visitDate)
    }

    @Test
    fun `getVisitCardList works while a schema with a due date is loaded`() = runTest {
        stubSchema(schemaOf(fieldOf("due_date")))
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3rd Day", false, dobFixture, "en")
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(7, viewModel.getVisitCardList(benIdFixture).size)
    }

    // =====================================================
    // getLastVisitDay() / getLastVisitDate() Tests
    // =====================================================

    @Test
    fun `getLastVisitDay returns the furthest visit in the configured order`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(benIdFixture) } returns listOf(
            visitEntity("3rd Day"), visitEntity("14th Day"), visitEntity("1st Day")
        )

        assertEquals("14th Day", viewModel.getLastVisitDay(benIdFixture))
    }

    @Test
    fun `getLastVisitDay returns null when there are no visits`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()

        assertNull(viewModel.getLastVisitDay(benIdFixture))
    }

    @Test
    fun `getLastVisitDay ignores visits outside the known order`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns
                listOf(visitEntity("Some Other Day"))

        assertNull(viewModel.getLastVisitDay(benIdFixture))
    }

    @Test
    fun `getLastVisitDate parses the visit date of the last visit`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("3rd Day", json = responseJson("visit_date" to "05-01-2024"))
        )

        assertEquals(ddMMyyyy().parse("05-01-2024"), viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `getLastVisitDate returns null when the visit date is missing`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("3rd Day", json = responseJson())
        )

        assertNull(viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `getLastVisitDate returns null when the stored json is malformed`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("3rd Day", json = "broken")
        )

        assertNull(viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `getLastVisitDate returns null when there is no visit at all`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns emptyList()

        assertNull(viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `loadVisitDates caches the last visit day and date`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("3rd Day", json = responseJson("visit_date" to "05-01-2024"))
        )

        viewModel.loadVisitDates(benIdFixture)
        advanceUntilIdle()

        assertEquals("3rd Day", viewModel.lastVisitDay)
        assertEquals(ddMMyyyy().parse("05-01-2024"), viewModel.previousVisitDate)
    }

    // =====================================================
    // getMaxVisitDate() / getMinVisitDate() Tests
    // =====================================================

    @Test
    fun `getMaxVisitDate returns today when no visit was recorded today`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("1st Day", json = responseJson("visit_date" to "05-01-2020"))
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(todayMidnight(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMaxVisitDate rolls back a day when today is already recorded`() = runTest {
        val today = ddMMyyyy().format(Date())
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("1st Day", json = responseJson("visit_date" to today))
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.getMaxVisitDate().before(todayMidnight()))
    }

    @Test
    fun `getMaxVisitDate ignores malformed stored responses`() = runTest {
        coEvery { repository.getSyncedVisitsByRchId(any()) } returns listOf(
            visitEntity("1st Day", json = "broken"),
            visitEntity("3rd Day", json = responseJson())
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(todayMidnight(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate returns null when there is no previous visit`() {
        assertNull(viewModel.getMinVisitDate())
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
    // checkIfBenDead() Tests
    // =====================================================

    @Test
    fun `checkIfBenDead publishes the repository result`() = runTest {
        coEvery { benRepo.isBenDead(benIdFixture) } returns true

        viewModel.checkIfBenDead(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.isBenDead.value)
    }

    @Test
    fun `checkIfBenDead publishes false when the beneficiary is alive`() = runTest {
        coEvery { benRepo.isBenDead(any()) } returns false

        viewModel.checkIfBenDead(benIdFixture)
        advanceUntilIdle()

        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `checkIfBenDead falls back to false when the lookup fails`() = runTest {
        coEvery { benRepo.isBenDead(any()) } throws RuntimeException("db down")

        viewModel.checkIfBenDead(benIdFixture)
        advanceUntilIdle()

        assertFalse(viewModel.isBenDead.value)
    }
}
