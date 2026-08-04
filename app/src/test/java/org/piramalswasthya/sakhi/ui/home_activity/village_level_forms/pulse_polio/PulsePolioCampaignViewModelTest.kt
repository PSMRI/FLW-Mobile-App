package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.work.dynamicWoker.PulsePolioCampaignPushWorker

/**
 * Unit tests for [PulsePolioCampaignViewModel] - the dynamic (schema driven) pulse polio form.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PulsePolioCampaignViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var formRepository: FormRepository

    @MockK
    private lateinit var vlfRepo: VLFRepo

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkObject(PulsePolioCampaignPushWorker.Companion)
        every { PulsePolioCampaignPushWorker.enqueue(any()) } just Runs

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH

        coEvery { formRepository.getSavedSchema(any()) } returns null
        coEvery { formRepository.getFormSchema(any(), any()) } returns schema()
        coEvery { vlfRepo.getPulsePolioCampaign(any()) } returns null
        coEvery { vlfRepo.savePulsePolioCampaign(any()) } returns Unit
    }

    private fun schema(): FormSchemaDto = FormSchemaDto(
        formId = "PULSE_POLIO",
        formName = "Pulse Polio Campaign",
        sections = listOf(
            FormSectionDto(
                sectionId = "s1",
                sectionTitle = "Section one",
                fields = listOf(
                    FormFieldDto(
                        fieldId = "roundHeld",
                        label = "Round held",
                        type = "radio",
                        options = listOf("Yes", "No"),
                        defaultValue = "Yes"
                    ),
                    FormFieldDto(
                        fieldId = "childrenCovered",
                        label = "Children covered",
                        type = "number",
                        validation = FieldValidationDto(min = 0f, max = 500f, maxLength = 3),
                        conditional = ConditionalLogic(
                            dependsOn = "roundHeld",
                            expectedValue = "Yes"
                        )
                    ),
                    FormFieldDto(
                        fieldId = "reason",
                        label = "Reason",
                        type = "text",
                        conditional = ConditionalLogic(
                            dependsOn = "roundHeld",
                            expectedValue = "No"
                        )
                    )
                )
            )
        )
    )

    private fun buildVm(id: Int = 0): PulsePolioCampaignViewModel = PulsePolioCampaignViewModel(
        SavedStateHandle(mapOf("id" to id)),
        context,
        formRepository,
        vlfRepo,
        preferenceDao
    )

    @Test
    fun `viewModel exposes the pulse polio form id and starts in edit mode`() {
        val vm = buildVm()
        assertNotNull(vm.formId)
        assertFalse(vm.isViewOnly)
        assertNull(vm.saveSuccess.value)
    }

    @Test
    fun `init renders the downloaded schema for a new record`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.schema.value)
        coVerify { formRepository.getFormSchema(any(), any()) }
    }

    @Test
    fun `init prefers a schema already cached in the database`() = runTest {
        coEvery { formRepository.getSavedSchema(any()) } returns FormSchemaEntity(
            formId = "PULSE_POLIO",
            formName = "Pulse Polio Campaign",
            language = "en",
            schemaJson = schema().toJson()
        )

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.schema.value)
        coVerify(exactly = 0) { formRepository.getFormSchema(any(), any()) }
    }

    @Test
    fun `init leaves the schema null when nothing can be resolved`() = runTest {
        coEvery { formRepository.getFormSchema(any(), any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertNull(vm.schema.value)
        assertTrue(vm.getVisibleFields().isEmpty())
    }

    @Test
    fun `opening a saved record switches to view only mode`() = runTest {
        coEvery { vlfRepo.getPulsePolioCampaign(any()) } returns PulsePolioCampaignCache(
            id = 3,
            formDataJson = """{"fields":{"roundHeld":"No","childrenCovered":12,"tags":["a"],"nested":{"k":"v"},"blank":null}}""",
            syncState = SyncState.UNSYNCED
        )

        val vm = buildVm(id = 3)
        advanceUntilIdle()

        assertTrue(vm.isViewOnly)
        val reason = vm.schema.value!!.sections.first().fields.first { it.fieldId == "reason" }
        assertTrue(reason.visible)
        assertFalse(reason.isEditable)
    }

    @Test
    fun `a corrupt saved payload falls back to the schema defaults`() = runTest {
        coEvery { vlfRepo.getPulsePolioCampaign(any()) } returns PulsePolioCampaignCache(
            id = 3,
            formDataJson = "}{",
            syncState = SyncState.UNSYNCED
        )

        val vm = buildVm(id = 3)
        advanceUntilIdle()

        assertNotNull(vm.schema.value)
    }

    @Test
    fun `getVisibleFields hides the fields whose condition is not met`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val ids = vm.getVisibleFields().map { it.fieldId }
        assertTrue(ids.contains("roundHeld"))
        assertTrue(ids.contains("childrenCovered"))
        assertFalse(ids.contains("reason"))
    }

    @Test
    fun `updateFieldValue re-evaluates the dependent visibility`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateFieldValue("roundHeld", "No")

        val ids = vm.getVisibleFields().map { it.fieldId }
        assertTrue(ids.contains("reason"))
        assertFalse(ids.contains("childrenCovered"))
    }

    @Test
    fun `updateFieldValue is a no-op before the schema is loaded`() {
        coEvery { formRepository.getFormSchema(any(), any()) } returns null

        val vm = buildVm()
        vm.updateFieldValue("roundHeld", "No")

        assertNull(vm.schema.value)
    }

    @Test
    fun `saveForm inserts a new record and enqueues the push worker`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { vlfRepo.savePulsePolioCampaign(any()) }
        assertEquals(true, vm.saveSuccess.value)
    }

    @Test
    fun `saveForm updates the record it was opened with`() = runTest {
        coEvery { vlfRepo.getPulsePolioCampaign(any()) } returns PulsePolioCampaignCache(
            id = 6,
            formDataJson = """{"fields":{"roundHeld":"Yes"}}""",
            syncState = SyncState.SYNCED
        )

        val vm = buildVm(id = 6)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify {
            vlfRepo.savePulsePolioCampaign(
                match<PulsePolioCampaignCache> { it.id == 6 && it.syncState == SyncState.UNSYNCED }
            )
        }
    }

    @Test
    fun `saveForm reports failure when the repository throws`() = runTest {
        coEvery { vlfRepo.savePulsePolioCampaign(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(false, vm.saveSuccess.value)
    }

    @Test
    fun `saveForm is a no-op before the schema is loaded`() = runTest {
        coEvery { formRepository.getFormSchema(any(), any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertNull(vm.saveSuccess.value)
        coVerify(exactly = 0) { vlfRepo.savePulsePolioCampaign(any()) }
    }

    @Test
    fun `resetSaveState clears the save flag`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetSaveState()

        assertNull(vm.saveSuccess.value)
    }
}
