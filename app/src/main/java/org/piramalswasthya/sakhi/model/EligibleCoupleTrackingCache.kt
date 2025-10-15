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
import org.piramalswasthya.sakhi.utils.HelperUtil.getDateStringFromLong
import java.text.SimpleDateFormat
import java.util.Calendar
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
    var lmpDate: Long = 0L,
    var visitDate: Long = 0L,
    var dateOfAntraInjection: String? = null,
    var dueDateOfAntraInjection: String? = null,
    var mpaFile: String? = null,
    var dischargeSummary1: String? = null,
    var dischargeSummary2: String? = null,
    var antraDose: String? = null,
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
    var syncState: SyncState,
    var lmp_date: Long
) : FormDataModel {

    fun asNetworkModel(): ECTNetwork {
        return ECTNetwork(
            benId = benId,
            lmpDate = getDateStringFromLong(lmpDate)!!,
            visitDate = getDateTimeStringFromLong(visitDate)!!,
            dateOfAntraInjection = dateOfAntraInjection?.let { SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(it)?.time }?.let { getDateTimeStringFromLong(it) },
            dueDateOfAntraInjection = dueDateOfAntraInjection,
            mpaFile = mpaFile,
            antraDose = antraDose,
            dischargeSummary1=dischargeSummary1,
            dischargeSummary2=dischargeSummary2,

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
            lmp_date = benId,
        )
    }
}

@JsonClass(generateAdapter = true)
data class ECTNetwork(
    val benId: Long,
    val lmpDate: String? = null,
    val visitDate: String,
    var dateOfAntraInjection: String? = null,
    var dueDateOfAntraInjection: String? = null,
    var mpaFile: String? = null,
    var dischargeSummary1: String? = null,
    var dischargeSummary2: String? = null,
    var antraDose: String? = null,
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
    val lmp_date: Long
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
        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)

        private fun getECTFilledDateFromLong(long: Long): String {
            return "Visited on ${dateFormat.format(long)}"
        }
    }

    fun asDomainModel(): BenWithEctListDomain {
        val recentFill = savedECTRecords.maxByOrNull { it.visitDate }
        val allowFill = recentFill?.let {
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)
            cal.apply { timeInMillis = recentFill.visitDate }
            val lastVisitMonth = cal.get(Calendar.MONTH)
            val lastVisitYear = cal.get(Calendar.YEAR)
            !(currentYear == lastVisitYear && currentMonth == lastVisitMonth)
        } ?: true
        return BenWithEctListDomain(
//            ecBenId,
            ben.asBasicDomainModel(),
            ecr.noOfLiveChildren.toString(),
            allowFill,
            ectDate = recentFill?.visitDate ?: 0L,
            lmpDate = recentFill?.lmpDate ?: 0L,
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
    val allowFill: Boolean,
    val ectDate: Long = 0L,
    val lmpDate: Long = 0L,
    val savedECTRecords: List<ECTDomain>,
    val allSynced: SyncState? = if (savedECTRecords.isEmpty()) null else
        if (savedECTRecords.map { it.syncState }
                .all { it == SyncState.SYNCED }) SyncState.SYNCED else SyncState.UNSYNCED

)