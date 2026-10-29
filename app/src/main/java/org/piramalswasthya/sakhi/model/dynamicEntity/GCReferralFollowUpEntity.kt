package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Follow-up of a General Check-up referral (form general_check_follow_up). Same shape as [TBReferralFollowUpEntity],
 * plus the referral it belongs to: one follow-up per referral, keyed like NCD_REFER (benId, referralReason, month).
 */
@Entity(
    tableName = "gc_referral_follow_up",
    indices = [Index(value = ["benId", "referralReason", "referralMonth"], unique = true)]
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
    val updatedAt: Long = System.currentTimeMillis(),
    /** ReferalCache.referralReason of the referral this follow-up closes. Empty for an old record not yet linked. */
    @ColumnInfo(defaultValue = "")
    val referralReason: String = "",
    /** yyyyMM of the referral date (ReferalCache.revisitMonth). */
    @ColumnInfo(defaultValue = "0")
    val referralMonth: Int = 0
)

/** One record of generalCheckUpFollowUp/saveAll (sent as a list). Dates are yyyy-MM-dd. */
data class GCReferralFollowUpRequest(
    val id: Long = 0,
    val beneficiaryId: Long,
    val houseHoldId: Long,
    val visitDate: String,
    val fields: Map<String, Any?>,
    val referralReason: String? = null,
    /** yyyy-MM-dd */
    val referredOnDate: String? = null
)
