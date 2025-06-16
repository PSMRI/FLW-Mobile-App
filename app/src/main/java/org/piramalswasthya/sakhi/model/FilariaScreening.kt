package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.FilariaScreeningDTO

@Entity(
    tableName = "FILARIA_SCREENING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_filariasn", value = ["benId"/* "hhId"*/])]
)
data class FilariaScreeningCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var mdaHomeVisitDate: Long = System.currentTimeMillis(),
    var houseHoldDetailsId: Long,
    var sufferingFromFilariasis: Boolean ? = false,
    var doseStatus: String ? = null,
    var affectedBodyPart: String ? = null,
    var otherDoseStatusDetails: String ? = null,
    var filariasisCaseCount: String ? = null,
    var medicineSideEffect: String ? = "",
    var otherSideEffectDetails: String ? = "",
    var createdBy: String ?  = "",
    var diseaseTypeID: Int ? = 0,
    var createdDate: Long = System.currentTimeMillis(),
    var syncState: SyncState = SyncState.UNSYNCED,
): FormDataModel {
    fun toDTO(): FilariaScreeningDTO {
        return FilariaScreeningDTO(
            id = 0,
            benId = benId,
            mdaHomeVisitDate = getDateTimeStringFromLong(mdaHomeVisitDate).toString(),
            houseHoldDetailsId = houseHoldDetailsId,
            doseStatus = doseStatus.toString(),
            sufferingFromFilariasis = sufferingFromFilariasis!!,
            affectedBodyPart = affectedBodyPart.toString(),
            otherDoseStatusDetails = otherDoseStatusDetails.toString(),
            medicineSideEffect = medicineSideEffect.toString(),
            otherSideEffectDetails = otherSideEffectDetails.toString(),
            createdBy = createdBy.toString(),
            createdDate = getDateTimeStringFromLong(createdDate).toString(),
            diseaseTypeID = diseaseTypeID!!,



        )
    }
}

data class BenWithFilariaScreeningCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val filariaScreeningCache: FilariaScreeningCache?,

    ) {
    fun asFilariaScreeningDomainModel(): BenWithFilariaScreeningDomain {
        return BenWithFilariaScreeningDomain(
            ben = ben.asBasicDomainModel(),
            filaria = filariaScreeningCache
        )
    }
}

data class BenWithFilariaScreeningDomain(
    val ben: BenBasicDomain,
    val filaria: FilariaScreeningCache?
)