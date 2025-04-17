package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.AESScreeningDTO

@Entity(
    tableName = "AES_SCREENING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_aessn", value = ["benId"/* "hhId"*/])]
)
data class AESScreeningCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var visitDate: Long = System.currentTimeMillis(),
    val houseHoldDetailsId: Long,
    var beneficiaryStatus: String ? = null,
    var beneficiaryStatusId: Int = 0,
    var dateOfDeath: Long = System.currentTimeMillis(),
    var placeOfDeath: String ? = null,
    var otherPlaceOfDeath: String ? = null,
    var reasonForDeath: String ?  = null,
    var otherReasonForDeath: String ?  = null,
    var aesJeCaseStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String ? = null,
    var otherReferredFacility: String ? = null,
    var diseaseTypeID: Int ? = 0,
    var createdDate: Long = System.currentTimeMillis(),
    var createdBy: String ? = null,
    var followUpPoint: Int ? = 0,
    var syncState: SyncState = SyncState.UNSYNCED,
): FormDataModel {
    fun toDTO(): AESScreeningDTO {
        return AESScreeningDTO(
            id = 0,
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate).toString(),
            aesJeCaseStatus = aesJeCaseStatus,
            houseHoldDetailsId = houseHoldDetailsId,
            referredTo = referredTo,
            referToName = referToName.toString(),
            otherReferredFacility = otherReferredFacility,
            createdDate = getDateTimeStringFromLong(createdDate).toString(),
            syncState = SyncState.SYNCED,
            diseaseTypeID = diseaseTypeID,
            beneficiaryStatusId = beneficiaryStatusId,
            beneficiaryStatus = beneficiaryStatus,
            createdBy = createdBy,
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath).toString(),
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            placeOfDeath = placeOfDeath,
            followUpPoint = followUpPoint

        )
    }
}

data class BenWithAESScreeningCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val aesScreening: AESScreeningCache?,

    ) {
    fun asAESScreeningDomainModel(): BenWithAESScreeningDomain {
        return BenWithAESScreeningDomain(
            ben = ben.asBasicDomainModel(),
            aes = aesScreening
        )
    }
}

data class BenWithAESScreeningDomain(
    val ben: BenBasicDomain,
    val aes: AESScreeningCache?
)