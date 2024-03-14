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
    var isSputumCollected: Boolean? = null,
    var sputumSubmittedAt: String? = null,
    var nikshayId: String? = null,
    var sputumTestResult: String? = null,
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
            followUps = followUps
        )
    }
}

data class BenWithTbSuspectedCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val tb: TBSuspectedCache?,

    ) {
    fun asTbSuspectedDomainModel(): BenWithTbSuspectedDomain {
        return BenWithTbSuspectedDomain(
            ben = ben.asBasicDomainModel(),
            tb = tb
        )
    }
}

data class BenWithTbSuspectedDomain(
    val ben: BenBasicDomain,
    val tb: TBSuspectedCache?
)