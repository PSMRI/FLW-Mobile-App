package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.HRPNonPregnantTrackDTO
import java.text.SimpleDateFormat
import java.util.Locale

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



data class BenWithHRNPTrackingCache(

    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val assessCache: HRPNonPregnantAssessCache,

    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = HRPNonPregnantTrackCache::class
    )
    val savedTrackings: List<HRPNonPregnantTrackCache>
) {

    companion object {
        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())

        private fun getHRNPTFilledDateFromLong(long: Long?): String {
            return "Visited on ${dateFormat.format(long)}"
        }
    }

    fun asDomainModel(): BenWithHRNPTListDomain {
        return BenWithHRNPTListDomain(
            ben.asBasicDomainModel(),
            savedTrackings.map {
                HRNPTDomain(
                    it.benId,
                    it.visitDate,
                    getHRNPTFilledDateFromLong(it.visitDate),
                    it.syncState
                )
            }
        )
    }
}

data class HRNPTDomain(
    val benId: Long,
    val visited: Long?,
    val filledOnString: String,
    val syncState: SyncState
)

data class BenWithHRNPTListDomain(
    val ben: BenBasicDomain,
    val savedTrackings: List<HRNPTDomain>,
    val allSynced: SyncState? = if (savedTrackings.isEmpty()) null else
        if (savedTrackings.map { it.syncState }
                .all { it == SyncState.SYNCED}) SyncState.SYNCED else SyncState.UNSYNCED

)