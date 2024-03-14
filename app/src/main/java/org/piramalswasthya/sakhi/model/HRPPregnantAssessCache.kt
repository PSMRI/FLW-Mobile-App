package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.helpers.getTodayMillis
import org.piramalswasthya.sakhi.network.HRPPregnantAssessDTO
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

@Entity(
    tableName = "HRP_PREGNANT_ASSESS",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_hpa", value = ["benId"/* "hhId"*/])]
)

data class HRPPregnantAssessCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val benId: Long,
    var noOfDeliveries: String? = null,
    var timeLessThan18m: String? = null,
    var heightShort: String? = null,
    var age: String? = null,
    var rhNegative: String? = null,
    var homeDelivery: String? = null,
    var badObstetric: String? = null,
    var multiplePregnancy: String? = null,
    var lmpDate: Long = 0L,
    var edd: Long = 0L,
    var isHighRisk: Boolean = false,
    var visitDate: Long = System.currentTimeMillis(),
    var syncState: SyncState = SyncState.UNSYNCED

) : FormDataModel {
    fun toDTO(): HRPPregnantAssessDTO {
        return HRPPregnantAssessDTO(
            id = 0,
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            rhNegative = rhNegative,
            homeDelivery = homeDelivery,
            badObstetric = badObstetric,
            multiplePregnancy = multiplePregnancy,
            lmpDate = getDateTimeStringFromLong(lmpDate),
            edd = getDateTimeStringFromLong(edd),
            isHighRisk = isHighRisk,
            visitDate = getDateTimeStringFromLong(visitDate)
        )
    }

    fun toHighRiskAssessDTO(): HighRiskAssessDTO {
        return HighRiskAssessDTO(
            id = id,
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            createdDate = HelperUtil.getDateStringFromLong(visitDate)
        )
    }
}

data class AgeUnitDTO(
    var years: Int,
    var months: Int,
    var days: Int
)

data class HighRiskAssessDTO(
    val id: Int = 0,
    val benId: Long = 0,
    var noOfDeliveries: String? = null,
    var timeLessThan18m: String? = null,
    var heightShort: String? = null,
    var age: String? = null,
    val createdDate: String? = null,
    val createdBy: String? = null,
    val updatedDate: String? = null,
    val updatedBy: String? = null
) {
    fun toPregnantAssess(): HRPPregnantAssessCache {
        return HRPPregnantAssessCache(
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            isHighRisk = noOfDeliveries == "Yes" || timeLessThan18m == "Yes" || heightShort == "Yes" || age == "Yes",
            syncState = SyncState.SYNCED
        )
    }

    fun toNonPregnantAssess(): HRPNonPregnantAssessCache {
        return HRPNonPregnantAssessCache(
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            isHighRisk = noOfDeliveries == "Yes" || timeLessThan18m == "Yes" || heightShort == "Yes" || age == "Yes",
            syncState = SyncState.SYNCED
        )
    }
}

data class BenWithHRPACache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val assess: HRPPregnantAssessCache?,

    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val mbp: HRPMicroBirthPlanCache?,

    ) {
    fun asDomainModel(): BenWithHRPADomain {
        var lmpString: String? = null
        var eddString: String? = null
        var weeksOfPregnancy: String? = null
        assess?.lmpDate?.let { lmp ->
            lmpString = getDateString(lmp)
            eddString = getDateString(lmp + TimeUnit.DAYS.toMillis(280))
            weeksOfPregnancy =
                (TimeUnit.MILLISECONDS.toDays(getTodayMillis() - lmp) / 7).takeIf { it <= 40 }
                    ?.toString() ?: "NA"

        }
        return BenWithHRPADomain(
            ben = ben.asBasicDomainModel(),
            assess = assess,
            lmpString = lmpString,
            eddString = eddString,
            weeksOfPregnancy = weeksOfPregnancy,
            mbp = mbp
        )
    }
}

data class BenWithHRPADomain(
    val ben: BenBasicDomain,
    val assess: HRPPregnantAssessCache?,
    val lmpString: String?,
    val eddString: String?,
    val weeksOfPregnancy: String?,
    val mbp: HRPMicroBirthPlanCache?
)