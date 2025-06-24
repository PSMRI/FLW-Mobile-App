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

@Entity(
    tableName = "ABHA_GENERATED",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["benId"], unique = true)]
)
data class ABHAModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    val hhId: Long,
    val benName: String,
    val benSurname: String? = null,
    val gender: Gender,
    val dob: Long,
    val abhaId: String?,
    var healthId: String = "",
    var healthIdNumber: String = "",
    var isNewAbha: Boolean= false,
    var syncState: SyncState = SyncState.UNSYNCED,
) : FormDataModel {
    fun toDTO(): ABHAGeneratedDTO {
        return ABHAGeneratedDTO(
            id = 0,
            benId = benId,
            hhId = hhId,
            benName = benName,
            benSurname = benSurname,
            gender = gender,
            dob = dob,
            abhaId = abhaId,
            healthId = healthId,
            healthIdNumber = healthIdNumber,
            isNewAbha = isNewAbha,

        )
    }
}

data class BenWithABHAGeneratedCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
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