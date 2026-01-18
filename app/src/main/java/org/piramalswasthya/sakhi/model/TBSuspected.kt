package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.TBSuspectedDTO

@Entity(
    tableName = "TB_SUSPECTED",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_tbs", value = ["benId"/* "hhId"*/])]
)

data class TBSuspectedCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var visitDate: Long = System.currentTimeMillis(),
    var visitLabel: String? = null,
    var typeOfTBCase: String? = null,
    var reasonForSuspicion: String? = null,
    var hasSymptoms: Boolean? = false,
    var isSputumCollected: Boolean? = null,
    var sputumSubmittedAt: String? = null,
    var nikshayId: String? = null,
    var sputumTestResult: String? = null,
    var isChestXRayDone: Boolean? = null,
    var chestXRayResult: String? = null,
    var referralFacility: String? = null,
    var isTBConfirmed: Boolean? = null,
    var isDRTBConfirmed: Boolean? = null,
    var isConfirmed: Boolean = false,
    var referred: Boolean? = null,
    var followUps: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
) : FormDataModel {
    fun toDTO(): TBSuspectedDTO {
        return TBSuspectedDTO(
            id = 0,
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate),
            isSputumCollected = isSputumCollected,
            sputumSubmittedAt = sputumSubmittedAt,
            nikshayId = nikshayId,
            sputumTestResult = sputumTestResult,
            referred = referred,
            followUps = followUps,
            visitLabel= visitLabel,
            typeOfTBCase = typeOfTBCase,
            reasonForSuspicion = reasonForSuspicion,
            hasSymptoms = hasSymptoms,
            isChestXRayDone = isChestXRayDone,
            chestXRayResult = chestXRayResult,
            referralFacility = referralFacility,
            isTBConfirmed = isTBConfirmed,
            isDRTBConfirmed = isDRTBConfirmed,
        )
    }
}

data class BenWithTbSuspectedCache(
    @Embedded
    val ben: BenBasicCache,

    @Relation(
        parentColumn = "benId",
        entityColumn = "benId"
    )
    val tbSuspected: TBSuspectedCache?,

    @Relation(
        parentColumn = "benId",
        entityColumn = "benId"
    )
    val tbConfirmedList: List<TBConfirmedTreatmentCache>
)
{
    fun asTbSuspectedDomainModel(): BenWithTbSuspectedDomain {
        return BenWithTbSuspectedDomain(
            ben = ben.asBasicDomainModel(),
            tbSuspected = tbSuspected,
            tbConfirmedList = tbConfirmedList
        )
    }

}

data class BenWithTbSuspectedDomain(
    val ben: BenBasicDomain,
    val tbSuspected: TBSuspectedCache?,
    val tbConfirmedList: List<TBConfirmedTreatmentCache>
) {
    val latestTbSyncState: SyncState?
        get() = tbConfirmedList
            .maxByOrNull { it.followUpDate ?: 0L }
            ?.syncState
}
