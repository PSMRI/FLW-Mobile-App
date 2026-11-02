package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.TBSuspectedCache

@Entity(
    tableName = "tb_referral_follow_up",
    indices = [Index(value = ["benId", "followUpDate"], unique = true)]
)
data class TBReferralFollowUpEntity(
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

data class TBReferralFollowUpRequest(
    val benId: Long,
    val houseHoldId: Long,
    val fields: Map<String, Any?>
)

data class BenWithTbReferralFollowUpCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId",
        entityColumn = "benId",
        entity = TBReferralFollowUpEntity::class
    )
    val referralFollowUps: TBReferralFollowUpEntity,
    @Relation(
        parentColumn = "benId",
        entityColumn = "benId"
    )
    val tbSuspected: TBSuspectedCache?

) {
    fun asDomainModel(status: String = "RECOMMEND_TPT"): BenWithTbReferralFollowUpDomain =
        BenWithTbReferralFollowUpDomain(
            ben = ben.asBasicDomainModel(),
            referralFollowUps = referralFollowUps,
            tbSuspected = tbSuspected
        )
}

data class BenWithTbReferralFollowUpDomain(
    val ben: BenBasicDomain,
    val referralFollowUps: TBReferralFollowUpEntity,
    val tbSuspected: TBSuspectedCache?

)
