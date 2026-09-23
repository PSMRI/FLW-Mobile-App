package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.Runs
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
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.model.dynamicEntity.ConditionalLogic
import org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.work.dynamicWoker.ORSCampaignPushWorker

/**
 * Unit tests for [ORSCampaignViewModel] - the dynamic (schema driven) ORS campaign form.
 *
 * The schema is a real [FormSchemaDto] so conditional visibility, field-value updates and the
 * saved-JSON rehydration path all run against production parsing logic rather than mocks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ORSCampaignViewModelTest : BaseViewModelTest() {

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

        mockkObject(ORSCampaignPushWorker.Companion)
        every { ORSCampaignPushWorker.enqueue(any()) } just Runs

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH

        coEvery { formRepository.getSavedSchema(any()) } returns null
        coEvery { formRepository.getFormSchema(any(), any()) } returns schema()
        coEvery { vlfRepo.getORSCampaign(any()) } returns null
        coEvery { vlfRepo.saveORSCampaign(any()) } returns Unit
    }

    private fun schema(): FormSchemaDto = FormSchemaDto(
        formId = "ORS",
        formName = "ORS Campaign",
        sections = listOf(
            FormSectionDto(
                sectionId = "s1",
                sectionTitle = "Section one",
                fields = listOf(
                    FormFieldDto(
                        fieldId = "distributed",
                        label = "ORS distributed",
                        type = "radio",
                        options = listOf("Yes", "No"),
                        defaultValue = "Yes"
                    ),
                    FormFieldDto(
                        fieldId = "packets",
                        label = "Packets",
                        type = "number",
                        validation = FieldValidationDto(
                            min = 1f,
                            max = 100f,
                            maxLength = 3,
                            decimalPlaces = 0,
                            maxSizeMB = 2
                        ),
                        conditional = ConditionalLogic(
                            dependsOn = "distributed",
                            expectedValue = "Yes"
                        )
                    ),
                    FormFieldDto(
                        fieldId = "reason",
                        label = "Reason",
                        type = "text",
                        placeholder = "why",
                        conditional = ConditionalLogic(
                            dependsOn = "distributed",
                            expectedValue = "No"
                        )
                    ),
                    FormFieldDto(
                        fieldId = "remarks",
                        label = "Remarks",
                        type = "text",
                        conditional = ConditionalLogic(dependsOn = "", expectedValue = "x")
                    )
                )
            )
        )
    )

    private fun buildVm(id: Int = 0): ORSCampaignViewModel = ORSCampaignViewModel(
        SavedStateHandle(mapOf("id" to id)),
        context,
        formRepository,
        vlfRepo,
        preferenceDao
    )

    @Test
    fun `viewModel exposes the ORS form id and starts in edit mode`() {
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
        assertFalse(vm.isViewOnly)
        coVerify { formRepository.getFormSchema(any(), any()) }
    }

    @Test
    fun `init prefers a schema already cached in the database`() = runTest {
        coEvery { formRepository.getSavedSchema(any()) } returns FormSchemaEntity(
            formId = "ORS",
            formName = "ORS Campaign",
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
        coEvery { vlfRepo.getORSCampaign(any()) } returns ORSCampaignCache(
            id = 4,
            formDataJson = """{"fields":{"distributed":"No","packets":7,"tags":["a","b"],"nested":{"k":"v"},"blank":null}}""",
            syncState = SyncState.UNSYNCED
        )

        val vm = buildVm(id = 4)
        advanceUntilIdle()

        assertTrue(vm.isViewOnly)
        assertNotNull(vm.schema.value)
        val reason = vm.schema.value!!.sections.first().fields.first { it.fieldId == "reason" }
        assertTrue(reason.visible)
        assertFalse(reason.isEditable)
    }

    @Test
    fun `a corrupt saved payload falls back to the schema defaults`() = runTest {
        coEvery { vlfRepo.getORSCampaign(any()) } returns ORSCampaignCache(
            id = 4,
            formDataJson = "not json at all",
            syncState = SyncState.UNSYNCED
        )

        val vm = buildVm(id = 4)
        advanceUntilIdle()

        assertNotNull(vm.schema.value)
    }

    @Test
    fun `an empty saved payload falls back to the schema defaults`() = runTest {
        coEvery { vlfRepo.getORSCampaign(any()) } returns ORSCampaignCache(
            id = 4,
            formDataJson = "",
            syncState = SyncState.UNSYNCED
        )

        val vm = buildVm(id = 4)
        advanceUntilIdle()

        assertNotNull(vm.schema.value)
    }

    @Test
    fun `getVisibleFields hides the fields whose condition is not met`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val ids = vm.getVisibleFields().map { it.fieldId }
        assertTrue(ids.contains("distributed"))
        assertTrue(ids.contains("packets"))
        assertTrue(ids.contains("remarks"))
        assertFalse(ids.contains("reason"))
    }

    @Test
    fun `updateFieldValue re-evaluates the dependent visibility`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateFieldValue("distributed", "No")

        val ids = vm.getVisibleFields().map { it.fieldId }
        assertTrue(ids.contains("reason"))
        assertFalse(ids.contains("packets"))
    }

    @Test
    fun `updateFieldValue is a no-op before the schema is loaded`() {
        coEvery { formRepository.getFormSchema(any(), any()) } returns null

        val vm = buildVm()
        vm.updateFieldValue("distributed", "No")

        assertNull(vm.schema.value)
    }

    @Test
    fun `saveForm inserts a new record and enqueues the push worker`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { vlfRepo.saveORSCampaign(any()) }
        assertEquals(true, vm.saveSuccess.value)
    }

    @Test
    fun `saveForm updates the record it was opened with`() = runTest {
        coEvery { vlfRepo.getORSCampaign(any()) } returns ORSCampaignCache(
            id = 8,
            formDataJson = """{"fields":{"distributed":"Yes"}}""",
            syncState = SyncState.SYNCED
        )

        val vm = buildVm(id = 8)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify {
            vlfRepo.saveORSCampaign(
                match<ORSCampaignCache> { it.id == 8 && it.syncState == SyncState.UNSYNCED }
            )
        }
        assertEquals(true, vm.saveSuccess.value)
    }

    @Test
    fun `saveForm reports failure when the repository throws`() = runTest {
        coEvery { vlfRepo.saveORSCampaign(any()) } throws RuntimeException("db down")

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
        coVerify(exactly = 0) { vlfRepo.saveORSCampaign(any()) }
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
