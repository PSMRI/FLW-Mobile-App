package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate

@Entity(
    tableName = "PNC_VISIT",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_pnc", value = ["benId"/* "hhId"*/])],
)
data class PNCVisitCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
    var pncPeriod: Int,
    var isActive: Boolean,
    var pncDate: Long = System.currentTimeMillis(),
    var ifaTabsGiven: Int? = 0,
    var anyContraceptionMethod: Boolean? = null,
    var contraceptionMethod: String? = null,
    var otherPpcMethod: String? = null,
    var motherDangerSign: String? = null,
    var otherDangerSign: String? = null,
    var referralFacility: String? = null,
    var motherDeath: Boolean = false,
    var deathDate: Long? = System.currentTimeMillis(),
    var causeOfDeath: String? = null,
    var otherDeathCause: String? = null,
    var placeOfDeath: String? = null,
    var remarks: String? = null,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun asDomainModel(): PncDomain = PncDomain(
        benId = benId,
        visitNumber = pncPeriod,
        syncState
    )


    fun asNetworkModel(): PNCNetwork {
        return PNCNetwork(
            id = id,
            benId = benId,
            pncPeriod = pncPeriod,
            isActive = isActive,
            pncDate = getDateTimeStringFromLong(pncDate)!!,
            ifaTabsGiven = ifaTabsGiven,
            anyContraceptionMethod = anyContraceptionMethod,
            contraceptionMethod = contraceptionMethod,
            otherPpcMethod = otherPpcMethod,
            motherDangerSign = motherDangerSign,
            otherDangerSign = otherDangerSign,
            referralFacility = referralFacility,
            motherDeath = motherDeath,
            deathDate = getDateTimeStringFromLong(deathDate) ?: null,
            causeOfDeath = causeOfDeath,
            otherDeathCause = otherDeathCause,
            placeOfDeath = placeOfDeath,
            remarks = remarks,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)!!,
        )
    }
}

@JsonClass(generateAdapter = true)
data class PNCNetwork(
    val id: Long,
    val benId: Long,
    var pncPeriod: Int,
    var isActive: Boolean,
    var pncDate: String,
    var ifaTabsGiven: Int?,
    var anyContraceptionMethod: Boolean?,
    var contraceptionMethod: String?,
    var otherPpcMethod: String?,
    var motherDangerSign: String?,
    var otherDangerSign: String?,
    var referralFacility: String?,
    var motherDeath: Boolean,
    var deathDate: String?,
    var causeOfDeath: String?,
    var otherDeathCause: String?,
    var placeOfDeath: String?,
    var remarks: String?,
    var createdBy: String,
    val createdDate: String,
    var updatedBy: String,
    val updatedDate: String,
) {
    fun asCacheModel(): PNCVisitCache {
        return PNCVisitCache(
            benId = benId,
            pncPeriod = pncPeriod,
            isActive = isActive,
            pncDate = getLongFromDate(pncDate),
            ifaTabsGiven = ifaTabsGiven,
            anyContraceptionMethod = anyContraceptionMethod,
            contraceptionMethod = contraceptionMethod,
            otherPpcMethod = otherPpcMethod,
            motherDangerSign = motherDangerSign,
            otherDangerSign = otherDangerSign,
            referralFacility = referralFacility,
            motherDeath = motherDeath,
            deathDate = getLongFromDate(deathDate),
            causeOfDeath = causeOfDeath,
            otherDeathCause = otherDeathCause,
            placeOfDeath = placeOfDeath,
            remarks = remarks,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED,
        )
    }
}

data class BenWithDoAndPncCache(
//    @ColumnInfo(name = "benId")
//    val ecBenId: Long,

    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = DeliveryOutcomeCache::class
    )
    val deliveryOutcomeCache: List<DeliveryOutcomeCache>,

    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = PNCVisitCache::class
    )
    val savedPncRecords: List<PNCVisitCache>
) {
    fun asBasicDomainModelForPNC(): BenPncDomain {
        return BenPncDomain(
            ben.asBasicDomainModel(),
            deliveryOutcomeCache.firstOrNull { it.isActive }?.dateOfDelivery?.let {
                getDateStrFromLong(
                    it
                )
            } ?: "",
            savedPncRecords
        )
    }

}

data class BenPncDomain(

    val ben: BenBasicDomain,
    val deliveryDate: String,
    val savedPncRecords: List<PNCVisitCache>,
    val syncState: SyncState? = savedPncRecords.takeIf { it.isNotEmpty() }?.map { it.syncState }
        ?.let {
            if (it.any { it != SyncState.SYNCED })
                SyncState.UNSYNCED
            else
                SyncState.SYNCED
        }
)

data class PncDomain(
    val benId: Long,
    val visitNumber: Int,
    val syncState: SyncState? = null
)