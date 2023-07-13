package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel

@Entity(
    tableName = "HRP_NON_PREGNANT_TRACK",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId",/* "householdId"*/),
        childColumns = arrayOf("benId", /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_hnpt", value = ["benId",/* "hhId"*/])]
)

data class HRPNonPregnantTrackCache (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId : Long,
    var dateOfVisit: Long? = null,
    var anemia: String? = null,
    var hypertension: String? = null,
    var diabetes: String? = null,
    var severeAnemia: String? = null,
    var fp: String? = null,
    var lmp: Long?= null,
    var missedPeriod: String? = null,
    var isPregnant: String? = null
) : FormDataModel {
    fun asDomainModel(): HRPPregnantTrackDomain {
    return HRPPregnantTrackDomain(
        id = id,
        dateOfVisit = getDateStringFromLong(dateOfVisit)
    )
}
}
