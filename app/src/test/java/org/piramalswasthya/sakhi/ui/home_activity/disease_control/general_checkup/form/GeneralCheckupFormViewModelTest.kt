package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.piramalswasthya.sakhi.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.CheckupPriority
import org.piramalswasthya.sakhi.model.GeneralCheckupCache
import org.piramalswasthya.sakhi.model.HealthProblemOwnerModule
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.formDataFromJson
import org.piramalswasthya.sakhi.repositories.GeneralCheckupRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.FIELD_DATE
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.FIELD_OTHER
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.FIELD_TYPE
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class GeneralCheckupFormViewModelTest : BaseViewModelTest() {

    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val benDao: BenDao = mockk(relaxed = true)
    private val formRepository: FormRepository = mockk(relaxed = true)
    private val generalCheckupRepo: GeneralCheckupRepo = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val schemaJson = javaClass.classLoader!!.getResource("general_check_up_schema.json").readText()
    private val ownerTitle: (HealthProblemOwnerModule) -> String = { it.name }

    @Before
    override fun setUp() {
        super.setUp()
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true).also {
            every { it.userId } returns 42
            every { it.userName } returns "asha42"
        }
        coEvery { benDao.getBen(any<Long>()) } returns null
        every { generalCheckupRepo.getHistory(any()) } returns flowOf(emptyList())
        coEvery { formRepository.getSavedSchema(FormConstants.GENERAL_CHECKUP_FORM_ID) } returns savedSchema("en")
        every { context.getString(any()) } answers { "err:${firstArg<Int>()}" }
        every { context.getString(any(), *anyVararg()) } answers { "err:${firstArg<Int>()}" }
        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerAmritPushWorker(any(), any()) } just Runs
    }

    private fun savedSchema(language: String) = FormSchemaEntity(
        formId = FormConstants.GENERAL_CHECKUP_FORM_ID,
        formName = "General Check-up",
        language = language,
        version = 1,
        schemaJson = schemaJson
    )

    private fun viewModel() = GeneralCheckupFormViewModel(
        SavedStateHandle(mapOf("benId" to 101L, "hhId" to 7L)),
        preferenceDao, benDao, formRepository, generalCheckupRepo, context
    )

    private fun GeneralCheckupFormViewModel.fieldIds() = visibleFields(ownerTitle).map { it.fieldId }

    private fun today() = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(System.currentTimeMillis())

    // ── Loading ────────────────────────────────────────────────────────────────

    @Test
    fun `saved schema in the user's language is used without a network call`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(GeneralCheckupFormViewModel.FormState.Ready, vm.formState.value)
        coVerify(exactly = 0) { formRepository.getFormSchema(any(), any()) }
    }

    @Test
    fun `saved schema in another language is fetched again`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.HINDI
        coEvery { formRepository.getSavedSchema(any()) } returns savedSchema("en")
        coEvery { formRepository.getFormSchema(FormConstants.GENERAL_CHECKUP_FORM_ID, "hi") } returns
                FormSchemaDto.fromJson(schemaJson)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(GeneralCheckupFormViewModel.FormState.Ready, vm.formState.value)
        coVerify { formRepository.getFormSchema(FormConstants.GENERAL_CHECKUP_FORM_ID, "hi") }
    }

    @Test
    fun `no schema anywhere marks the form unavailable`() = runTest {
        coEvery { formRepository.getSavedSchema(any()) } returns null
        coEvery { formRepository.getFormSchema(any(), any()) } returns null
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(GeneralCheckupFormViewModel.FormState.Unavailable, vm.formState.value)
        assertTrue(vm.visibleFields(ownerTitle).isEmpty())
    }

    @Test
    fun `unusable server form is reported as invalid, not as not downloaded`() = runTest {
        val draft = """{"formId":"general_check_up","formName":"GC","version":1,"sections":[{"fields":[
            {"fieldId":"type_of_problem","type":"dropdown"}]}]}"""
        coEvery { formRepository.getSavedSchema(any()) } returns savedSchema("en").copy(schemaJson = draft)
        coEvery { formRepository.getFormSchema(any(), any()) } returns FormSchemaDto.fromJson(draft)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(GeneralCheckupFormViewModel.FormState.Invalid, vm.formState.value)
    }

    @Test
    fun `unusable saved form is replaced by a fixed one from the server`() = runTest {
        val draft = """{"formId":"general_check_up","formName":"GC","version":1,"sections":[{"fields":[]}]}"""
        coEvery { formRepository.getSavedSchema(any()) } returns savedSchema("en").copy(schemaJson = draft)
        coEvery { formRepository.getFormSchema(FormConstants.GENERAL_CHECKUP_FORM_ID, "en") } returns
                FormSchemaDto.fromJson(schemaJson)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(GeneralCheckupFormViewModel.FormState.Ready, vm.formState.value)
    }

    // ── Fields ─────────────────────────────────────────────────────────────────

    @Test
    fun `only date and type show before a type is chosen, date defaults to today`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(listOf(FIELD_DATE, FIELD_TYPE), vm.fieldIds())
        assertEquals(today(), vm.visibleFields(ownerTitle).first().value)
        assertNull(vm.selectedProblemType)
    }

    @Test
    fun `choosing a type shows only that type's problem list`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        assertEquals(listOf(FIELD_DATE, FIELD_TYPE, "problems_g6"), vm.fieldIds())
    }

    @Test
    fun `changing the type clears the ticked problems and Other text`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_other"))
        vm.onFieldChanged(FIELD_OTHER, "Rash")
        vm.onFieldChanged(FIELD_TYPE, "g11")
        assertFalse(vm.hasSelection)
        vm.onFieldChanged(FIELD_TYPE, "g6")
        assertFalse(vm.hasSelection)
        assertEquals(listOf(FIELD_DATE, FIELD_TYPE, "problems_g6"), vm.fieldIds())
    }

    @Test
    fun `ticking Other shows a required Other box`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_fever", "g6_other"))
        val other = vm.visibleFields(ownerTitle).last()
        assertEquals(FIELD_OTHER, other.fieldId)
        assertTrue(other.isRequired)
    }

    @Test
    fun `Other box shows no error until Refer is tapped, and clears it as she types`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_other"))
        assertNull(vm.visibleFields(ownerTitle).last().errorMessage)

        vm.validate(context, ownerTitle)
        assertEquals("err:${R.string.gc_other_required}", vm.visibleFields(ownerTitle).last().errorMessage)

        vm.onFieldChanged(FIELD_OTHER, "R")
        assertNull(vm.visibleFields(ownerTitle).last().errorMessage)
    }

    @Test
    fun `Other box error is dropped when the box hides, so ticking Other again shows it clean`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_other"))
        vm.validate(context, ownerTitle)
        vm.onFieldChanged("problems_g6", setOf("g6_fever"))
        vm.onFieldChanged("problems_g6", setOf("g6_fever", "g6_other"))
        assertNull(vm.visibleFields(ownerTitle).last().errorMessage)
    }

    @Test
    fun `emergency problem sets priority and is highlighted`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g11")
        val options = vm.visibleFields(ownerTitle).last().options!!
        assertTrue(options.filter { it.value != "g11_other" }.all { it.highlight })
        assertFalse(options.first { it.value == "g11_other" }.highlight)
        vm.onFieldChanged("problems_g11", setOf("e_chest"))
        assertEquals(CheckupPriority.EMERGENCY, vm.priority)
    }

    @Test
    fun `routine problem keeps routine priority`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", listOf("g6_fever"))
        assertEquals(CheckupPriority.ROUTINE, vm.priority)
        assertNull(vm.ownedProblem)
    }

    @Test
    fun `owner module problem is tagged and routed`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g4")
        val pregnancy = vm.visibleFields(ownerTitle).last().options!!.first { it.value == "preg" }
        assertTrue(pregnancy.label.endsWith("(MATERNAL)"))
        vm.onFieldChanged("problems_g4", setOf("g4_mens", "preg"))
        assertEquals(HealthProblemOwnerModule.MATERNAL, vm.ownedProblem!!.ownerModule)
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    @Test
    fun `validation runs in the prototype order`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertNotNull(vm.validate(context, ownerTitle))
        assertNotNull(vm.visibleFields(ownerTitle).first { it.fieldId == FIELD_TYPE }.errorMessage)

        vm.onFieldChanged(FIELD_TYPE, "g6")
        assertNotNull(vm.validate(context, ownerTitle))
        assertNotNull(vm.visibleFields(ownerTitle).first { it.fieldId == "problems_g6" }.errorMessage)

        vm.onFieldChanged("problems_g6", setOf("g6_other"))
        assertNotNull(vm.validate(context, ownerTitle))
        assertNotNull(vm.visibleFields(ownerTitle).first { it.fieldId == FIELD_OTHER }.errorMessage)

        vm.onFieldChanged(FIELD_OTHER, "x".repeat(251))
        assertNotNull(vm.validate(context, ownerTitle))

        vm.onFieldChanged(FIELD_OTHER, "Skin rash")
        assertNull(vm.validate(context, ownerTitle))
        assertTrue(vm.visibleFields(ownerTitle).all { it.errorMessage == null })
    }

    @Test
    fun `a date older than one month is rejected`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        val old = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }
        vm.onFieldChanged(FIELD_DATE, SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(old.time))
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_fever"))
        assertNotNull(vm.validate(context, ownerTitle))
        assertNotNull(vm.visibleFields(ownerTitle).first { it.fieldId == FIELD_DATE }.errorMessage)
    }

    @Test
    fun `editing a field clears its error`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.validate(context, ownerTitle)
        vm.onFieldChanged(FIELD_TYPE, "g6")
        assertNull(vm.visibleFields(ownerTitle).first { it.fieldId == FIELD_TYPE }.errorMessage)
    }

    // ── Save ───────────────────────────────────────────────────────────────────

    @Test
    fun `referral reason is English and includes the Other text`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_other", "g6_fever"))
        vm.onFieldChanged(FIELD_OTHER, "  Skin rash ")
        assertEquals("General Check-up: Fever, Other: Skin rash", vm.referralReason())
    }

    @Test
    fun `submitted referral saves the check-up with English labels, version and answers`() = runTest {
        val checkup = slot<GeneralCheckupCache>()
        val referralSlot = slot<ReferalCache>()
        coEvery { generalCheckupRepo.saveCheckupWithReferral(capture(checkup), capture(referralSlot), any()) } returns
                GeneralCheckupRepo.SaveResult.Saved
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g11")
        vm.onFieldChanged("problems_g11", setOf("e_chest"))

        vm.onReferralSubmitted(ReferalCache(benId = 0, referralReason = "General Check-up: Chest pain", syncState = SyncState.UNSYNCED))
        advanceUntilIdle()

        assertEquals(GeneralCheckupFormViewModel.SaveState.Saved, vm.saveState.value)
        with(checkup.captured) {
            assertEquals(101L, benId)
            assertEquals(7L, hhId)
            assertEquals(42, ashaId)
            assertEquals("g11", problemTypeId)
            assertEquals("Emergency", problemTypeLabel)
            assertEquals(listOf("e_chest"), problemIds)
            assertEquals(listOf("Chest pain"), problemLabels)
            assertEquals(CheckupPriority.EMERGENCY.name, priority)
            assertEquals(1, masterVersion)
            assertNull(otherText)
            assertEquals(
                mapOf(FIELD_DATE to today(), FIELD_TYPE to "g11", "problems_g11" to "e_chest"),
                formDataFromJson(formData!!)
            )
        }
        assertEquals(GeneralCheckupFormViewModel.REFERRAL_TYPE, referralSlot.captured.type)
        assertEquals(CheckupPriority.EMERGENCY.name, referralSlot.captured.priority)
        assertEquals(101L, referralSlot.captured.benId)
    }

    @Test
    fun `saveForm saves the check-up with the referral kept by addReferral`() = runTest {
        val referralSlot = slot<ReferalCache>()
        coEvery { generalCheckupRepo.saveCheckupWithReferral(any(), capture(referralSlot), any()) } returns
                GeneralCheckupRepo.SaveResult.Saved
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_fever"))

        val referral = ReferalCache(benId = 0, referralReason = "General Check-up: Fever", syncState = SyncState.UNSYNCED)
        vm.addReferral(referral)
        vm.saveForm()
        advanceUntilIdle()

        assertEquals(GeneralCheckupFormViewModel.SaveState.Saved, vm.saveState.value)
        assertEquals(referral, referralSlot.captured)
    }

    @Test
    fun `saveForm without a referral saves nothing`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.saveForm()
        advanceUntilIdle()
        assertNull(vm.saveState.value)
        coVerify(exactly = 0) { generalCheckupRepo.saveCheckupWithReferral(any(), any(), any()) }
    }

    @Test
    fun `duplicate from the save is reported`() = runTest {
        val existing = mockk<GeneralCheckupCache>(relaxed = true)
        coEvery { generalCheckupRepo.saveCheckupWithReferral(any(), any(), any()) } returns
                GeneralCheckupRepo.SaveResult.Duplicate(existing)
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_fever"))
        vm.onReferralSubmitted(ReferalCache(benId = 0, syncState = SyncState.UNSYNCED))
        advanceUntilIdle()
        assertEquals(GeneralCheckupFormViewModel.SaveState.Duplicate(existing), vm.saveState.value)
    }

    @Test
    fun `duplicate check passes the Other problem ids and text`() = runTest {
        coEvery { generalCheckupRepo.findDuplicate(any(), any(), any(), any(), any()) } returns null
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_other"))
        vm.onFieldChanged(FIELD_OTHER, "Rash")
        vm.findDuplicate()
        coVerify { generalCheckupRepo.findDuplicate(101L, any(), listOf("g6_other"), listOf("g6_other"), "Rash") }
    }

    // ── Other box from the schema's conditional ────────────────────────────────

    private val conditionalOtherSchema = """{"formId":"general_check_up","formName":"GC","version":2,"sections":[{"fields":[
            {"fieldId":"checkup_date","type":"date"},
            {"fieldId":"problem_type","type":"dropdown","options":[{"value":"t1","label":"Fever type","labelEn":"Fever type"}]},
            {"fieldId":"problems_t1","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"t1"},
             "options":[{"value":"p1","label":"Fever","labelEn":"Fever"},{"value":"t1_other","label":"Other","labelEn":"Other"}]},
            {"fieldId":"other_problem","type":"text","conditional":{"dependsOn":"problems_t1","expectedValue":"t1_other"}}]}]}"""

    private fun conditionalOtherViewModel(): GeneralCheckupFormViewModel {
        coEvery { formRepository.getSavedSchema(FormConstants.GENERAL_CHECKUP_FORM_ID) } returns
                savedSchema("en").copy(schemaJson = conditionalOtherSchema)
        return viewModel()
    }

    @Test
    fun `Other box shows only while the checkbox matching its expectedValue is ticked`() = runTest {
        val vm = conditionalOtherViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "t1")
        vm.onFieldChanged("problems_t1", setOf("p1"))
        assertFalse("other_problem" in vm.fieldIds())

        vm.onFieldChanged("problems_t1", setOf("p1", "t1_other"))
        val other = vm.visibleFields(ownerTitle).last()
        assertEquals("other_problem", other.fieldId)
        assertTrue(other.isRequired)

        vm.onFieldChanged("problems_t1", setOf("p1"))
        assertFalse("other_problem" in vm.fieldIds())
    }

    @Test
    fun `conditional Other box is required and goes into the referral reason`() = runTest {
        val vm = conditionalOtherViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "t1")
        vm.onFieldChanged("problems_t1", setOf("t1_other"))
        assertEquals("err:${R.string.gc_other_required}", vm.validate(context, ownerTitle))

        vm.onFieldChanged("other_problem", " Skin rash ")
        assertNull(vm.validate(context, ownerTitle))
        assertEquals("General Check-up: Other: Skin rash", vm.referralReason())
    }

    @Test
    fun `changing the type clears the conditional Other box`() = runTest {
        val vm = conditionalOtherViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "t1")
        vm.onFieldChanged("problems_t1", setOf("t1_other"))
        vm.onFieldChanged("other_problem", "Rash")
        vm.onFieldChanged(FIELD_TYPE, null)
        vm.onFieldChanged(FIELD_TYPE, "t1")
        vm.onFieldChanged("problems_t1", setOf("t1_other"))
        assertNull(vm.visibleFields(ownerTitle).last().value)
    }

    @Test
    fun `type dropdown leaves out inactive types and types without active problems`() = runTest {
        coEvery { formRepository.getSavedSchema(FormConstants.GENERAL_CHECKUP_FORM_ID) } returns savedSchema("en").copy(
            schemaJson = """{"formId":"general_check_up","formName":"GC","version":2,"sections":[{"fields":[
            {"fieldId":"checkup_date","type":"date"},
            {"fieldId":"problem_type","type":"dropdown","options":[
             {"value":"t1","label":"T1"},{"value":"t2","label":"T2","isActive":false},{"value":"t3","label":"T3"}]},
            {"fieldId":"problems_t1","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"t1"},
             "options":[{"value":"p1","label":"P1"}]},
            {"fieldId":"problems_t2","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"t2"},
             "options":[{"value":"p2","label":"P2"}]},
            {"fieldId":"problems_t3","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"t3"},
             "options":[{"value":"p3","label":"P3","isActive":false}]}]}]}"""
        )
        val vm = viewModel()
        advanceUntilIdle()
        val typeField = vm.visibleFields(ownerTitle).first { it.fieldId == FIELD_TYPE }
        assertEquals(listOf("t1"), typeField.options!!.map { it.value })
    }

    // ── Live UAT schema v24 (one shared "other_problem" box per list) ──────────

    private fun uatV24ViewModel(): GeneralCheckupFormViewModel {
        val json = javaClass.classLoader!!.getResource("general_check_up_uat_v24.json").readText()
        coEvery { formRepository.getSavedSchema(FormConstants.GENERAL_CHECKUP_FORM_ID) } returns
                savedSchema("en").copy(schemaJson = json)
        return viewModel()
    }

    @Test
    fun `v24 - Other box shows for Fever type when Other is ticked`() = runTest {
        val vm = uatV24ViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_other"))
        assertEquals(1, vm.fieldIds().count { it == "other_problem" })
    }

    @Test
    fun `v24 - Other box shows for Emergency type when Other is ticked`() = runTest {
        val vm = uatV24ViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g11")
        // Server's Emergency list carries the mental-health options, so its "Other" is g12_other.
        vm.onFieldChanged("problems_g11", setOf("g12_other"))
        assertEquals(1, vm.fieldIds().count { it == "other_problem" })
    }

    @Test
    fun `v24 - Emergency Other box is required, hides on untick and its text goes in the reason`() = runTest {
        val vm = uatV24ViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g11")
        vm.onFieldChanged("problems_g11", setOf("g12_sleep"))
        assertFalse("other_problem" in vm.fieldIds())

        vm.onFieldChanged("problems_g11", setOf("g12_sleep", "g12_other"))
        assertTrue(vm.visibleFields(ownerTitle).single { it.fieldId == "other_problem" }.isRequired)
        assertEquals("err:${R.string.gc_other_required}", vm.validate(context, ownerTitle))

        vm.onFieldChanged("other_problem", "Snake bite")
        assertNull(vm.validate(context, ownerTitle))
        assertEquals("General Check-up: Cannot sleep, Other: Snake bite", vm.referralReason())

        vm.onFieldChanged("problems_g11", setOf("g12_sleep"))
        assertFalse("other_problem" in vm.fieldIds())
    }

    @Test
    fun `v24 - Emergency type follows the server's priority field, other types stay routine`() = runTest {
        val vm = uatV24ViewModel()
        advanceUntilIdle()
        vm.onFieldChanged(FIELD_TYPE, "g11")
        vm.onFieldChanged("problems_g11", setOf("g12_sleep"))
        assertEquals(CheckupPriority.EMERGENCY, vm.priority)

        vm.onFieldChanged(FIELD_TYPE, "g6")
        vm.onFieldChanged("problems_g6", setOf("g6_fever"))
        assertEquals(CheckupPriority.ROUTINE, vm.priority)
    }
}
