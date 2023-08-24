package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.HRPNonPregnantTrackDTO

@Entity(
    tableName = "HRP_NON_PREGNANT_TRACK",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_hnpt", value = ["benId"/* "hhId"*/])]
)

data class HRPNonPregnantTrackCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var visitDate: Long? = null,
    var anemia: String? = null,
    var hypertension: String? = null,
    var diabetes: String? = null,
    var severeAnemia: String? = null,
    var fp: String? = null,
    var lmp: Long? = null,
    var missedPeriod: String? = null,
    var isPregnant: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED
) : FormDataModel {
    fun asDomainModel(): HRPPregnantTrackDomain {
        return HRPPregnantTrackDomain(
            id = id,
            dateOfVisit = getDateStrFromLong(visitDate)
        )
    }

    fun toDTO(): HRPNonPregnantTrackDTO {
        return HRPNonPregnantTrackDTO(
            id = 0,
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate),
            anemia = anemia,
            hypertension = hypertension,
            diabetes = diabetes,
            severeAnemia = severeAnemia,
            fp = fp,
            lmp = getDateTimeStringFromLong(lmp),
            missedPeriod = missedPeriod,
            isPregnant = isPregnant
        )
    }
}
