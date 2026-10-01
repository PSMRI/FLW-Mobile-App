package org.piramalswasthya.sakhi.adapters

import com.google.common.truth.Truth.assertThat
import org.junit.Test
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
