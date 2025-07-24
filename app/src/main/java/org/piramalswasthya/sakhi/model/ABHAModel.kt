package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.ABHAGeneratedDTO
import org.piramalswasthya.sakhi.network.ABHAProfile
import org.piramalswasthya.sakhi.network.MapHIDtoBeneficiary

@Entity(
    tableName = "ABHA_GENERATED",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("beneficiaryID" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["beneficiaryID"], unique = true)]
)
data class ABHAModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val beneficiaryID: Long,
    val beneficiaryRegID: Long,
    val benName: String,
    val createdBy: String,
    val message: String,
    val txnId: String,
    val benSurname: String? = null,
    var healthId: String = "",
    var healthIdNumber: String = "",
    var abhaProfileJson : String = "",
    var isNewAbha: Boolean= false,
    val providerServiceMapId: Int,
    var syncState: SyncState = SyncState.UNSYNCED,
) : FormDataModel {
    fun toDTO(): ABHAGeneratedDTO {
        return ABHAGeneratedDTO(
            id = 0,
            beneficiaryID = beneficiaryID,
            beneficiaryRegID = beneficiaryRegID,
            benName = benName,
            benSurname = benSurname,
            healthId = healthId,
            healthIdNumber = healthIdNumber,
            providerServiceMapId = providerServiceMapId,
            txnId = txnId,
            message = message,
            createdBy = createdBy,
            isNewAbha = isNewAbha,

        )
    }
}

data class BenWithABHAGeneratedCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "beneficiaryID"
    )
    val abha: ABHAModel?,

    ) {
    fun asBenWithABHAGeneratedDomainModel(): BenWithABHAGeneratedDomain {
        return BenWithABHAGeneratedDomain(
            ben = ben.asBasicDomainModel(),
            abha = abha
        )
    }
}

data class BenWithABHAGeneratedDomain(

    val ben: BenBasicDomain,
    val abha: ABHAModel?
)

fun ABHAModel.toMapHIDtoBeneficiaryRequest(sharedAbhaProfile: ABHAProfile): MapHIDtoBeneficiary {
    return MapHIDtoBeneficiary(
        beneficiaryRegID = this.beneficiaryRegID,
        beneficiaryID = this.beneficiaryID,
        healthId = this.healthId,
        healthIdNumber = this.healthIdNumber,
        providerServiceMapId = this.providerServiceMapId,
        createdBy = this.createdBy,
        message = this.message,
        txnId = this.txnId,
        ABHAProfile = sharedAbhaProfile,
        isNew = this.isNewAbha
    )
}