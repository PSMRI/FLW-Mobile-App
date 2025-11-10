package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.LeprosyScreeningDTO

@Entity(
    tableName = "LEPROSY_SCREENING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_leprosysn", value = ["benId"/* "hhId"*/])]
)
data class LeprosyScreeningCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var homeVisitDate: Long = System.currentTimeMillis(),
    var leprosyStatusDate: Long = System.currentTimeMillis(),
    var dateOfDeath: Long = System.currentTimeMillis(),
    var houseHoldDetailsId: Long,
    var leprosyStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String ? = null,
    var otherReferredTo: String ? = null,
    var typeOfLeprosy: String ? = null,
    var remarks: String ? = null,
    var beneficiaryStatus: String ? = null,
    var placeOfDeath: String ? = null,
    var otherPlaceOfDeath: String ? = null,
    var reasonForDeath: String ? = null,
    var otherReasonForDeath: String ? = null,
    var diseaseTypeID: Int ? = 0,
    var beneficiaryStatusId: Int ? = 0,
    var followUpDate: Long = System.currentTimeMillis(),
    var leprosySymptoms: String? = null,
    var leprosySymptomsPosition: Int? = 1,
    var lerosyStatusPosition: Int? = 0,
    var visitLabel: String? = "Visit -1",
    var visitNumber: Int? = 1,
    var isConfirmed: Boolean = false,
    val treatmentStartDate : Long = System.currentTimeMillis(),
    val treatmentEndDate : Long = System.currentTimeMillis(),
    val mdtBlisterPackRecived: String? = null,
    var treatmentStatus: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
): FormDataModel {
    fun toDTO(): LeprosyScreeningDTO {
        return LeprosyScreeningDTO(
            id = 0,
            benId = benId,
            homeVisitDate = getDateTimeStringFromLong(homeVisitDate).toString(),
            leprosyStatusDate = getDateTimeStringFromLong(leprosyStatusDate).toString(),
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath).toString(),
            houseHoldDetailsId = houseHoldDetailsId,
            leprosyStatus = leprosyStatus ,
            beneficiaryStatus = beneficiaryStatus.toString(),
            referredTo = referredTo!!,
            otherReferredTo = otherReferredTo.toString(),
            referToName = referToName.toString(),
            remarks = remarks.toString(),
            followUpDate = getDateTimeStringFromLong(followUpDate).toString(),
            diseaseTypeID = diseaseTypeID!!,
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            placeOfDeath = placeOfDeath,
            beneficiaryStatusId = beneficiaryStatusId,
            leprosySymptoms = leprosySymptoms,
            leprosySymptomsPosition = leprosySymptomsPosition,
            visitLabel = visitLabel,
            visitNumber = visitNumber




            )
    }
}

data class BenWithLeprosyScreeningCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val leprosy: LeprosyScreeningCache?,

    ) {
    fun asLeprosyScreeningDomainModel(): BenWithLeprosyScreeningDomain {
        return BenWithLeprosyScreeningDomain(
            ben = ben.asBasicDomainModel(),
            leprosy = leprosy
        )
    }
}

data class BenWithLeprosyScreeningDomain(
    val ben: BenBasicDomain,
    val leprosy: LeprosyScreeningCache?
)