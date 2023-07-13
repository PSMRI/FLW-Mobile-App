package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel

@Entity(
    tableName = "HRP_MICRO_BIRTH_PLAN",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId",/* "householdId"*/),
        childColumns = arrayOf("benId", /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_mbp", value = ["benId",/* "hhId"*/])]
)

data class HRPMicroBirthPlanCache (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId : Long,
    var nearestSc: String? = null,
    var bloodGroup: String? = null,
    var contactNumber1: String? = null,
    var contactNumber2: String? = null,
    var scHosp: String? = null,
    var usg: String? = null,
    var block: String? = null,
    var bankac: String? = null,
    var nearestPhc: String? = null,
    var nearestFru: String? = null,
    var bloodDonors1: String? = null,
    var bloodDonors2: String? = null,
    var birthCompanion: String? = null,
    var careTaker: String? = null,
    var communityMember: String? = null,
    var communityMemberContact: String? = null,
    var modeOfTransportation: String? = null
) : FormDataModel
