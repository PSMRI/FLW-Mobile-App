package org.piramalswasthya.sakhi.adapters

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity

class TPTListAdapterTest {

    @Test
    fun action_isStartTreatmentWhenThereAreNoFollowUps() {
        assertThat(tptFollowUpAction(emptyList())).isEqualTo(TPTFollowUpAction.START_TREATMENT)
    }

    @Test
    fun action_isAddFollowUpWhenAVisitExistsAndTreatmentIsNotCompleted() {
        assertThat(tptFollowUpAction(listOf(visit("""{"treatment_completed":"No"}"""))))
            .isEqualTo(TPTFollowUpAction.ADD_FOLLOW_UP)
    }

    @Test
    fun action_isViewDetailsWhenTreatmentCompletedYes() {
        assertThat(tptFollowUpAction(listOf(visit("""{"treatment_completed":"Yes"}"""))))
            .isEqualTo(TPTFollowUpAction.VIEW_DETAILS)
    }

    @Test
    fun syncStatus_isNullWithoutVisitsAndTrueOnlyWhenAllVisitsSynced() {
        assertThat(tptFollowUpSyncStatus(emptyList())).isNull()
        assertThat(tptFollowUpSyncStatus(listOf(visit("{}", synced = true)))).isTrue()
        assertThat(tptFollowUpSyncStatus(listOf(visit("{}", synced = true), visit("{}")))).isFalse()
    }

    @Test
    fun referralFollowUpDate_isEmptyForPresumptiveTbTptEntryWithoutReferral() {
        assertThat(tptReferralFollowUpDate(null)).isEmpty()
    }

    @Test
    fun referralFollowUpDate_readsDateWhenReferralExists() {
        val referral = TBReferralFollowUpEntity(
            benId = 1,
            houseHoldId = 1,
            referredOnDate = "2026-09-29 00:00:00",
            followUpDate = "2026-09-29 00:00:00",
            followUpStatus = "RECOMMEND_TPT",
            fieldsJson = "{\"follow_up_date\":\"2026-09-29 00:00:00\"}"
        )

        assertThat(tptReferralFollowUpDate(referral)).isEqualTo("2026-09-29 00:00:00")
    }

    private fun visit(fieldsJson: String, synced: Boolean = false) = TPTFollowUpEntity(
        benId = 1,
        houseHoldId = 1,
        visitNo = 1,
        followUpNo = 1,
        treatmentType = "3RH",
        treatmentStartDate = "01-09-2026",
        followUpDate = "30-09-2026",
        fieldsJson = fieldsJson,
        isSynced = synced
    )
}
