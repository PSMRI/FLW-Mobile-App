package org.piramalswasthya.sakhi.ui.home_activity.infant.hbyc

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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicEntity.optionItems
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HBYCFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FormRepository
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var infantRegRepo: InfantRegRepo

    private lateinit var viewModel: HBYCFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = HBYCFormViewModel(repository, benRepo, infantRegRepo)
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
    fun `initial syncedVisitList is empty`() {
        assertNotNull(viewModel.syncedVisitList.value)
        assert(viewModel.syncedVisitList.value.isEmpty())
    }

    // =====================================================
    // Fixtures / helpers
    // =====================================================

    private val benIdFixture = 31L
    private val hhIdFixture = 41L
    private val dobFixture = 1_700_000_000_000L
    private val formIdFixture = "hbyc_form"

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
        formId: String = "hbyc_form",
        version: Int = 4
    ) = FormSchemaDto(
        formId = formId,
        formName = "HBYC",
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
    ) = FormResponseJsonEntityHBYC(
        benId = benId,
        hhId = hhIdFixture,
        visitDay = visitDay,
        visitDate = "05-01-2024",
        formId = formIdFixture,
        version = 1,
        formDataJson = json
    )

    private fun stubSchema(
        schema: FormSchemaDto?,
        savedJson: String? = null,
        synced: List<FormResponseJsonEntityHBYC> = emptyList()
    ) {
        coEvery { repository.getSavedSchema(formIdFixture) } returns null
        coEvery { repository.getFormSchema(formIdFixture, "en") } returns schema
        coEvery { repository.loadFormResponseJsonHBYC(any(), any()) } returns savedJson
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns synced
    }

    private fun loadedFields() = viewModel.schema.value!!.sections.first().fields

    private fun fieldNamed(id: String) = loadedFields().first { it.fieldId == id }

    private fun dobPlusMonths(months: Int): Long =
        Calendar.getInstance().apply {
            time = Date(dobFixture)
            add(Calendar.MONTH, months)
        }.timeInMillis

    private fun monthsAgo(months: Int): Long =
        Calendar.getInstance().apply { add(Calendar.MONTH, -months) }.timeInMillis

    private fun todayMidnight(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun ddMMyyyy() = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    // =====================================================
    // Static configuration Tests
    // =====================================================

    @Test
    fun `visitOrder lists the five hbyc visits`() {
        assertEquals(
            listOf("3 Months", "6 Months", "9 Months", "12 Months", "15 Months"),
            viewModel.visitOrder
        )
    }

    @Test
    fun `initial isBenDead is false`() {
        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `initial isSNCU is false`() {
        assertFalse(viewModel.isSNCU.value)
    }

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

    // =====================================================
    // loadSyncedVisitList() Tests
    // =====================================================

    @Test
    fun `loadSyncedVisitList publishes visits and invokes the callback`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(benIdFixture) } returns
                listOf(visitEntity("3 Months"))
        var completed = false

        viewModel.loadSyncedVisitList(benIdFixture) { completed = true }
        advanceUntilIdle()

        assertEquals(1, viewModel.syncedVisitList.value.size)
        assertTrue(completed)
    }

    @Test
    fun `loadSyncedVisitList works without a callback`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns emptyList()

        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.syncedVisitList.value.isEmpty())
    }

    // =====================================================
    // loadInfant() Tests
    // =====================================================

    @Test
    fun `loadInfant publishes the first stored response`() = runTest {
        coEvery { repository.getInfantByRchIdHBYC(benIdFixture) } returns
                listOf(visitEntity("3 Months"), visitEntity("6 Months"))

        viewModel.loadInfant(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("3 Months", viewModel.infant.value?.visitDay)
    }

    @Test
    fun `loadInfant publishes null when nothing is stored`() = runTest {
        coEvery { repository.getInfantByRchIdHBYC(any()) } returns emptyList()

        viewModel.loadInfant(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertNull(viewModel.infant.value)
    }

    // =====================================================
    // loadFormSchema() Tests
    // =====================================================

    @Test
    fun `loadFormSchema publishes remote schema when nothing is cached`() = runTest {
        stubSchema(schemaOf(fieldOf("weight", type = "number", defaultValue = "6")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals(formIdFixture, viewModel.schema.value?.formId)
        assertEquals("3 Months", viewModel.visitMonth)
        assertEquals("6", fieldNamed("weight").value)
    }

    @Test
    fun `loadFormSchema prefers the cached schema over the remote one`() = runTest {
        val cached = schemaOf(fieldOf("weight"), formId = "cached_hbyc")
        coEvery { repository.getSavedSchema(formIdFixture) } returns FormSchemaEntity(
            formId = formIdFixture,
            formName = "HBYC",
            language = "en",
            version = 1,
            schemaJson = cached.toJson()
        )
        coEvery { repository.getFormSchema(any(), any()) } returns null
        coEvery { repository.loadFormResponseJsonHBYC(any(), any()) } returns null
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns emptyList()

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("cached_hbyc", viewModel.schema.value?.formId)
    }

    @Test
    fun `loadFormSchema returns early when no schema can be resolved`() = runTest {
        stubSchema(null)

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertNull(viewModel.schema.value)
    }

    @Test
    fun `loadFormSchema fills visit_day and due_date and locks them`() = runTest {
        stubSchema(schemaOf(fieldOf("visit_day"), fieldOf("due_date")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "6 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("6 Months", fieldNamed("visit_day").value)
        assertEquals(ddMMyyyy().format(Date(dobPlusMonths(6))), fieldNamed("due_date").value)
        assertFalse(fieldNamed("visit_day").isEditable)
        assertFalse(fieldNamed("due_date").isEditable)
    }

    @Test
    fun `loadFormSchema falls back to the default due date for an unknown visit month`() = runTest {
        stubSchema(schemaOf(fieldOf("due_date", defaultValue = "01-01-2024")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "99 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("01-01-2024", fieldNamed("due_date").value)
    }

    @Test
    fun `loadFormSchema restores previously saved values`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("weight", type = "number", defaultValue = "0"),
                fieldOf("muac", defaultValue = "12")
            ),
            savedJson = responseJson("weight" to "7.4")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("7.4", fieldNamed("weight").value)
        assertEquals("12", fieldNamed("muac").value)
    }

    @Test
    fun `loadFormSchema falls back to defaults when saved json is malformed`() = runTest {
        stubSchema(schemaOf(fieldOf("weight", defaultValue = "0")), savedJson = "not-json")

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertEquals("0", fieldNamed("weight").value)
    }

    @Test
    fun `loadFormSchema leaves radio fields unset when nothing was saved`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("radio_field", type = "radio", defaultValue = "Yes"),
                fieldOf("text_field", type = "text", defaultValue = "T")
            )
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertNull(fieldNamed("radio_field").value)
        assertEquals("T", fieldNamed("text_field").value)
    }

    @Test
    fun `loadFormSchema marks every field read only in view mode`() = runTest {
        stubSchema(schemaOf(fieldOf("weight")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", true, dobFixture, "en")
        advanceUntilIdle()

        assertFalse(fieldNamed("weight").isEditable)
    }

    @Test
    fun `loadFormSchema resolves visibility from the dependency default value`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive", type = "radio", defaultValue = "Yes"),
                fieldOf("growth_chart", conditional = SchemaConditional("is_baby_alive", "Yes"))
            )
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertTrue(fieldNamed("growth_chart").visible)
    }

    @Test
    fun `loadFormSchema hides a conditional field when the dependency does not match`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("is_baby_alive"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertFalse(fieldNamed("reason_for_death").visible)
    }

    @Test
    fun `loadFormSchema hides the measles vaccine field for early visits`() = runTest {
        stubSchema(schemaOf(fieldOf("measles_vaccine", type = "radio")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertFalse(fieldNamed("measles_vaccine").visible)
    }

    @Test
    fun `loadFormSchema keeps the measles vaccine field for later visits`() = runTest {
        stubSchema(schemaOf(fieldOf("measles_vaccine", type = "radio")))

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        assertTrue(fieldNamed("measles_vaccine").visible)
    }

    @Test
    fun `loadFormSchema removes already submitted months from the visit day options`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf(
                    "visit_day",
                    type = "dropdown",
                    options = listOf("3 Months", "6 Months", "9 Months")
                )
            ),
            synced = listOf(visitEntity("6 Months"))
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "3 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val values = fieldNamed("visit_day").optionItems()!!.map { it.value }
        assertEquals(listOf("3 Months", "9 Months"), values)
    }

    @Test
    fun `loadFormSchema keeps the current month in the visit day options`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf(
                    "visit_day",
                    type = "dropdown",
                    options = listOf("3 Months", "6 Months")
                )
            ),
            synced = listOf(visitEntity("6 Months"))
        )

        viewModel.loadFormSchema(benIdFixture, formIdFixture, "6 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val values = fieldNamed("visit_day").optionItems()!!.map { it.value }
        assertTrue(values.contains("6 Months"))
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue does nothing when the schema is not loaded`() {
        viewModel.updateFieldValue("weight", "7.1")

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
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        viewModel.updateFieldValue("is_baby_alive", "No")

        assertEquals("No", fieldNamed("is_baby_alive").value)
        assertTrue(fieldNamed("reason_for_death").visible)
    }

    @Test
    fun `updateFieldValue keeps the measles vaccine field hidden for early visits`() = runTest {
        stubSchema(schemaOf(fieldOf("measles_vaccine", type = "radio"), fieldOf("weight")))
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "6 Months", false, dobFixture, "en")
        advanceUntilIdle()

        viewModel.updateFieldValue("weight", "7.4")

        assertFalse(fieldNamed("measles_vaccine").visible)
        assertEquals("7.4", fieldNamed("weight").value)
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
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
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
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
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

        coVerify(exactly = 0) { repository.insertFormResponseHBYC(any()) }
    }

    @Test
    fun `saveFormResponses stores the response for a live baby`() = runTest {
        stubSchema(
            schemaOf(
                fieldOf("visit_date"),
                fieldOf("is_baby_alive", type = "radio"),
                fieldOf("reason_for_death", conditional = SchemaConditional("is_baby_alive", "No"))
            ),
            savedJson = responseJson("visit_date" to "05-01-2024", "is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val captured = slot<FormResponseJsonEntityHBYC>()
        coEvery { repository.insertFormResponseHBYC(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("05-01-2024", captured.captured.visitDate)
        assertEquals("9 Months", captured.captured.visitDay)
        assertEquals(benIdFixture, captured.captured.benId)
        assertEquals(hhIdFixture, captured.captured.hhId)
        assertEquals(4, captured.captured.version)
        assertFalse(captured.captured.isSynced)
        assertFalse(captured.captured.formDataJson.contains("reason_for_death"))
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveFormResponses uses N A when no visit date was captured`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val captured = slot<FormResponseJsonEntityHBYC>()
        coEvery { repository.insertFormResponseHBYC(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals("N/A", captured.captured.visitDate)
    }

    @Test
    fun `saveFormResponses marks the beneficiary dead when the baby is not alive`() = runTest {
        stubSchema(
            schemaOf(
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
                "is_baby_alive" to "No",
                "reason_for_death" to "Illness",
                "place_of_death" to "Other",
                "other_place_of_death" to "Roadside",
                "date_of_death" to "04-01-2024"
            )
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(benIdFixture) } returns ben
        coEvery { repository.insertFormResponseHBYC(any()) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        coVerify(exactly = 1) { benRepo.updateRecord(ben) }
    }

    @Test
    fun `saveFormResponses still stores the response when the death update fails`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "No")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        coEvery { benRepo.getBenFromId(any()) } throws RuntimeException("db down")
        val captured = slot<FormResponseJsonEntityHBYC>()
        coEvery { repository.insertFormResponseHBYC(capture(captured)) } returns Unit

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertTrue(captured.isCaptured)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveFormResponses refreshes the synced visit list`() = runTest {
        stubSchema(
            schemaOf(fieldOf("is_baby_alive", type = "radio")),
            savedJson = responseJson("is_baby_alive" to "Yes")
        )
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        coEvery { repository.insertFormResponseHBYC(any()) } returns Unit
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns
                listOf(visitEntity("9 Months"))

        viewModel.saveFormResponses(benIdFixture, hhIdFixture)
        advanceUntilIdle()

        assertEquals(1, viewModel.syncedVisitList.value.size)
    }

    // =====================================================
    // calculateDueDate() / formatDate() Tests
    // =====================================================

    @Test
    fun `calculateDueDate adds the right number of months`() {
        assertEquals(dobPlusMonths(3), viewModel.calculateDueDate(dobFixture, "3 Months")!!)
        assertEquals(dobPlusMonths(6), viewModel.calculateDueDate(dobFixture, "6 Months")!!)
        assertEquals(dobPlusMonths(9), viewModel.calculateDueDate(dobFixture, "9 Months")!!)
        assertEquals(dobPlusMonths(12), viewModel.calculateDueDate(dobFixture, "12 Months")!!)
        assertEquals(dobPlusMonths(15), viewModel.calculateDueDate(dobFixture, "15 Months")!!)
    }

    @Test
    fun `calculateDueDate trims the visit month before matching`() {
        assertEquals(dobPlusMonths(9), viewModel.calculateDueDate(dobFixture, " 9 Months ")!!)
    }

    @Test
    fun `calculateDueDate returns null for an unknown visit month`() {
        assertNull(viewModel.calculateDueDate(dobFixture, "24 Months"))
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
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val visible = viewModel.getVisibleFields()

        assertEquals(1, visible.size)
        val mapped = visible.first()
        assertEquals("is_baby_alive", mapped.fieldId)
        assertEquals(2, mapped.options?.size)
        assertTrue(mapped.isRequired)
        assertEquals("pick one", mapped.placeholder)
        assertEquals("bad value", mapped.validation?.errorMessage)
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
        viewModel.loadFormSchema(benIdFixture, formIdFixture, "9 Months", false, dobFixture, "en")
        advanceUntilIdle()

        val dependent = viewModel.getVisibleFields().first { it.fieldId == "reason_for_death" }

        assertEquals("is_baby_alive", dependent.conditional?.dependsOn)
        assertEquals("No", dependent.conditional?.expectedValue)
    }

    // =====================================================
    // getVisitCardList() / getBabyAgeMonths() Tests
    // =====================================================

    @Test
    fun `getVisitCardList marks only the first pending eligible month editable`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns
                listOf(visitEntity("3 Months"))
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val cards = viewModel.getVisitCardList(benIdFixture, monthsAgo(24)).associateBy { it.visitDay }

        assertEquals(5, cards.size)
        assertTrue(cards.getValue("3 Months").isCompleted)
        assertFalse(cards.getValue("3 Months").isEditable)
        assertTrue(cards.getValue("6 Months").isEditable)
        assertFalse(cards.getValue("9 Months").isEditable)
        assertEquals("05-01-2024", cards.getValue("3 Months").visitDate)
    }

    @Test
    fun `getVisitCardList lists future months as locked placeholders`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns emptyList()
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val cards = viewModel.getVisitCardList(benIdFixture, System.currentTimeMillis())

        assertEquals(5, cards.size)
        assertTrue(cards.none { it.isEditable })
        assertTrue(cards.all { it.visitDate == "-" })
        assertTrue(cards.none { it.isCompleted })
    }

    @Test
    fun `getVisitCardList flags a baby death recorded in the stored response`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("3 Months", json = responseJson("is_baby_alive" to "No"))
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val card = viewModel.getVisitCardList(benIdFixture, monthsAgo(24))
            .first { it.visitDay == "3 Months" }

        assertTrue(card.isBabyDeath)
    }

    @Test
    fun `getVisitCardList ignores visits of another beneficiary`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns
                listOf(visitEntity("3 Months", benId = 999L))
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        val cards = viewModel.getVisitCardList(benIdFixture, monthsAgo(24)).associateBy { it.visitDay }

        assertFalse(cards.getValue("3 Months").isCompleted)
        assertTrue(cards.getValue("3 Months").isEditable)
    }

    @Test
    fun `getBabyAgeMonths returns zero for a newborn`() {
        assertEquals(0, viewModel.getBabyAgeMonths(System.currentTimeMillis()))
    }

    @Test
    fun `getBabyAgeMonths counts whole months since birth`() {
        assertEquals(6, viewModel.getBabyAgeMonths(monthsAgo(6)))
        assertEquals(15, viewModel.getBabyAgeMonths(monthsAgo(15)))
    }

    // =====================================================
    // getLastVisitDay() / getLastVisitDate() Tests
    // =====================================================

    @Test
    fun `getLastVisitDay returns the furthest visit in the configured order`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(benIdFixture) } returns listOf(
            visitEntity("3 Months"), visitEntity("12 Months"), visitEntity("6 Months")
        )

        assertEquals("12 Months", viewModel.getLastVisitDay(benIdFixture))
    }

    @Test
    fun `getLastVisitDay returns null when there are no visits`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns emptyList()

        assertNull(viewModel.getLastVisitDay(benIdFixture))
    }

    @Test
    fun `getLastVisitDay ignores visits outside the known order`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns
                listOf(visitEntity("24 Months"))

        assertNull(viewModel.getLastVisitDay(benIdFixture))
    }

    @Test
    fun `getLastVisitDate parses the visit date of the last visit`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("6 Months", json = responseJson("visit_date" to "05-01-2024"))
        )

        assertEquals(ddMMyyyy().parse("05-01-2024"), viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `getLastVisitDate returns null when the visit date is missing`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("6 Months", json = responseJson())
        )

        assertNull(viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `getLastVisitDate returns null when the stored json is malformed`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("6 Months", json = "broken")
        )

        assertNull(viewModel.getLastVisitDate(benIdFixture))
    }

    @Test
    fun `loadVisitDates caches the last visit day and date`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("6 Months", json = responseJson("visit_date" to "05-01-2024"))
        )

        viewModel.loadVisitDates(benIdFixture)
        advanceUntilIdle()

        assertEquals("6 Months", viewModel.lastVisitDay)
        assertEquals(ddMMyyyy().parse("05-01-2024"), viewModel.previousVisitDate)
    }

    // =====================================================
    // getMaxVisitDate() / getMinVisitDate() Tests
    // =====================================================

    @Test
    fun `getMaxVisitDate returns today when no visit was recorded today`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("3 Months", json = responseJson("visit_date" to "05-01-2020"))
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertEquals(todayMidnight(), viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMaxVisitDate rolls back a day when today is already recorded`() = runTest {
        val today = ddMMyyyy().format(Date())
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("3 Months", json = responseJson("visit_date" to today))
        )
        viewModel.loadSyncedVisitList(benIdFixture)
        advanceUntilIdle()

        assertTrue(viewModel.getMaxVisitDate().before(todayMidnight()))
    }

    @Test
    fun `getMaxVisitDate ignores malformed stored responses`() = runTest {
        coEvery { repository.getSyncedVisitsByRchIdHBYC(any()) } returns listOf(
            visitEntity("3 Months", json = "broken"),
            visitEntity("6 Months", json = responseJson())
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
    fun `checkIfBenDead falls back to false when the lookup fails`() = runTest {
        coEvery { benRepo.isBenDead(any()) } throws RuntimeException("db down")

        viewModel.checkIfBenDead(benIdFixture)
        advanceUntilIdle()

        assertFalse(viewModel.isBenDead.value)
    }
}
