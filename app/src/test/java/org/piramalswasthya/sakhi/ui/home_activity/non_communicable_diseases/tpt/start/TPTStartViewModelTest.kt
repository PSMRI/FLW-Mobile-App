package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tpt.start

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSectionDto
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository

@OptIn(ExperimentalCoroutinesApi::class)
class TPTStartViewModelTest : BaseRepositoryTest() {

    @Test
    fun saveFormResponses_whenOutcomeIsDeath_marksBeneficiaryDeadAndUnsynced() = runTest {
        val repository = mockk<TPTStartRepository>(relaxed = true)
        val benRepo = mockk<BenRepo>()
        val beneficiary = mockk<BenRegCache>(relaxed = true)
        every { beneficiary.processed } returns "Y"
        coEvery { repository.getFollowUps(474849020466) } returns emptyList()
        coEvery { repository.getFormSchema(any()) } returns deathOutcomeSchema()
        coEvery { repository.saveLocally(any()) } returns Unit
        coEvery { benRepo.getBenFromId(474849020466) } returns beneficiary
        coEvery { benRepo.updateRecord(any()) } returns Unit

        val viewModel = TPTStartViewModel(
            repository,
            benRepo,
            SavedStateHandle(mapOf(
                "benId" to 474849020466L,
                "hhId" to 12345L,
                "referralFollowUpDate" to "2026-09-29 00:00:00"
            ))
        )
        viewModel.loadForm()
        advanceUntilIdle()

        viewModel.saveFormResponses(
            listOf(
                field("tpt_outcome", "Death"),
                field("date_of_death", "30-09-2026"),
                field("place_of_death", "Home"),
                field("reason_for_death", "Tuberculosis")
            )
        )

        verify { beneficiary.isDeath = true }
        verify { beneficiary.isDeathValue = "Death" }
        verify { beneficiary.dateOfDeath = "30-09-2026" }
        verify { beneficiary.placeOfDeath = "Home" }
        verify { beneficiary.reasonOfDeath = "Tuberculosis" }
        verify { beneficiary.processed = "U" }
        verify { beneficiary.syncState = org.piramalswasthya.sakhi.database.room.SyncState.UNSYNCED }
        coVerify(exactly = 1) { benRepo.updateRecord(beneficiary) }
    }

    @Test
    fun isFollowUpLimitReached_allowsSecondMonthForShortRegimen() = runTest {
        val history = (1..6).map { month ->
            org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity(
                benId = 474849020466,
                houseHoldId = 12345,
                visitNo = 1,
                followUpNo = month,
                treatmentType = "1HP",
                treatmentStartDate = "29-09-2026",
                followUpDate = "${month}-10-2026",
                fieldsJson = "{\"monthly_follow_up\":\"Month-$month\"}"
            )
        }
        val secondVisitViewModel = viewModelWithHistory(history.take(1))
        secondVisitViewModel.loadForm()
        advanceUntilIdle()
        assertEquals(false, secondVisitViewModel.isFollowUpLimitReached())

        val seventhVisitViewModel = viewModelWithHistory(history)
        seventhVisitViewModel.loadForm()
        advanceUntilIdle()
        assertEquals(true, seventhVisitViewModel.isFollowUpLimitReached())
    }

    private fun viewModelWithHistory(
        history: List<org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity>
    ): TPTStartViewModel {
        val repository = mockk<TPTStartRepository>(relaxed = true)
        coEvery { repository.getFollowUps(474849020466) } returns history
        coEvery { repository.getFormSchema(any()) } returns FormSchemaDto(
            formId = "tb_tpt_follow_up",
            formName = "TPT Follow-up",
            sections = listOf(FormSectionDto(fields = listOf(
                FormFieldDto(fieldId = "regimen_type", type = "dropdown", value = "1HP")
            )))
        )
        return TPTStartViewModel(
            repository,
            mockk(relaxed = true),
            SavedStateHandle(mapOf(
                "benId" to 474849020466L,
                "hhId" to 12345L,
                "referralFollowUpDate" to "2026-09-29 00:00:00"
            ))
        )
    }

    private fun deathOutcomeSchema() = FormSchemaDto(
        formId = "tb_tpt_follow_up",
        formName = "TPT Follow-up",
        sections = listOf(FormSectionDto(fields = listOf(
            FormFieldDto(fieldId = "tpt_outcome", type = "dropdown"),
            FormFieldDto(fieldId = "date_of_death", type = "date"),
            FormFieldDto(fieldId = "place_of_death", type = "dropdown"),
            FormFieldDto(fieldId = "reason_for_death", type = "dropdown")
        )))
    )

    private fun field(fieldId: String, value: String) = FormField(
        fieldId = fieldId,
        label = fieldId,
        type = "text",
        isRequired = false,
        value = value
    )
}
