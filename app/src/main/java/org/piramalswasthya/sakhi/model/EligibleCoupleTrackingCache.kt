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
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "ELIGIBLE_COUPLE_TRACKING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_ect", value = ["benId"/* "hhId"*/])]
)

data class EligibleCoupleTrackingCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var visitDate: Long = System.currentTimeMillis(),
    var isPregnancyTestDone: String? = null,
    var pregnancyTestResult: String? = null,
    var isPregnant: String? = null,
    var usingFamilyPlanning: Boolean? = null,
    var methodOfContraception: String? = null,
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var processed: String? = "N",
    var isActive: Boolean = true,
    var syncState: SyncState
) : FormDataModel {

    fun asNetworkModel(): ECTNetwork {
        return ECTNetwork(
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate)!!,
            isPregnancyTestDone = isPregnancyTestDone,
            pregnancyTestResult = pregnancyTestResult,
            isPregnant = isPregnant,
            usingFamilyPlanning = usingFamilyPlanning,
            methodOfContraception = methodOfContraception,
            isActive = isActive,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)!!,
        )
    }
}

@JsonClass(generateAdapter = true)
data class ECTNetwork(
    val benId: Long,
    val visitDate: String,
    val isPregnancyTestDone: String?,
    val pregnancyTestResult: String?,
    val isPregnant: String?,
    val usingFamilyPlanning: Boolean?,
    val methodOfContraception: String?,
    var isActive: Boolean?,
    val createdBy: String,
    val createdDate: String,
    val updatedBy: String,
    val updatedDate: String,
)

data class BenWithEcTrackingCache(
//    @ColumnInfo(name = "benId")
//    val ecBenId: Long,

    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val ecr: EligibleCoupleRegCache,

    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = EligibleCoupleTrackingCache::class
    )
    val savedECTRecords: List<EligibleCoupleTrackingCache>
) {

    companion object {
        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())

        private fun getECTFilledDateFromLong(long: Long): String {
            return "Visited on ${dateFormat.format(long)}"
        }
    }

    fun asDomainModel(): BenWithEctListDomain {
        return BenWithEctListDomain(
//            ecBenId,
            ben.asBasicDomainModel(),
            ecr.noOfChildren.toString(),
            savedECTRecords.map {
                ECTDomain(
                    it.benId,
                    it.createdDate,
                    it.visitDate,
                    getECTFilledDateFromLong(it.visitDate),
                    it.syncState
                )
            }
        )
    }
}

data class ECTDomain(
    val benId: Long,
    val created: Long,
    val visited: Long,
    val filledOnString: String,
    val syncState: SyncState
)

data class BenWithEctListDomain(
//    val benId: Long,
    val ben: BenBasicDomain,
    val numChildren: String,
    val savedECTRecords: List<ECTDomain>,
    val allSynced: SyncState? = if (savedECTRecords.isEmpty()) null else
        if (savedECTRecords.map { it.syncState }
                .all { it == SyncState.SYNCED}) SyncState.SYNCED else SyncState.UNSYNCED

)