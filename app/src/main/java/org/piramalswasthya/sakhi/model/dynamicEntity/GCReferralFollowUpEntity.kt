package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Follow-up of a General Check-up referral (form general_check_follow_up). Same shape as [TBReferralFollowUpEntity]. */
@Entity(
    tableName = "gc_referral_follow_up",
    indices = [Index(value = ["benId", "followUpDate"], unique = true)]
)
data class GCReferralFollowUpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
    val houseHoldId: Long,
    val referredOnDate: String,
    val followUpDate: String,
    val followUpStatus: String?,
    val fieldsJson: String,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

/** One record of generalCheckUpFollowUp/saveAll (sent as a list). Dates are yyyy-MM-dd. */
data class GCReferralFollowUpRequest(
    val id: Long = 0,
    val beneficiaryId: Long,
    val houseHoldId: Long,
    val visitDate: String,
    val fields: Map<String, Any?>
)
