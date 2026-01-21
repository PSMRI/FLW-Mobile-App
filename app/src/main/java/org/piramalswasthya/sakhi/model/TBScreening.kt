package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.TBScreeningDTO

@Entity(
    tableName = "TB_SCREENING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_tbsn", value = ["benId"/* "hhId"*/])]
)

data class TBScreeningCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var visitDate: Long = System.currentTimeMillis(),
    var coughMoreThan2Weeks: Boolean? = null,
    var bloodInSputum: Boolean? = null,
    var feverMoreThan2Weeks: Boolean? = null,
    var lossOfWeight: Boolean? = null,
    var nightSweats: Boolean? = null,
    var historyOfTb: Boolean? = null,
    var takingAntiTBDrugs: Boolean? = null,
    var familySufferingFromTB: Boolean? = null,
    var riseOfFever: Boolean? = null,
    var lossOfAppetite: Boolean? = null,
    var age: Boolean? = null,
    var diabetic: Boolean? = null,
    var tobaccoUser: Boolean? = null,
    var bmi: Boolean? = null,
    var contactWithTBPatient: Boolean? = null,
    var historyOfTBInLastFiveYrs: Boolean? = null,
    var symptomatic :String?=null,
    var asymptomatic  :String?=null,
    var recommandedTest  :String?=null,
    var syncState: SyncState = SyncState.UNSYNCED,
) : FormDataModel {
    fun toDTO(): TBScreeningDTO {
        return TBScreeningDTO(
            id = 0,
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate),
            coughMoreThan2Weeks = coughMoreThan2Weeks,
            bloodInSputum = bloodInSputum,
            feverMoreThan2Weeks = feverMoreThan2Weeks,
            lossOfWeight = lossOfWeight,
            nightSweats = nightSweats,
            historyOfTb = historyOfTb,
            takingAntiTBDrugs = takingAntiTBDrugs,
            familySufferingFromTB = familySufferingFromTB,
            riseOfFever = riseOfFever,
            lossOfAppetite = lossOfAppetite,
            age = age,
            diabetic = diabetic,
            tobaccoUser = tobaccoUser,
            bmi = bmi,
            contactWithTBPatient = contactWithTBPatient,
            historyOfTBInLastFiveYrs = historyOfTBInLastFiveYrs,
            symptomatic = symptomatic,
            asymptomatic = asymptomatic,
            recommandedTest = recommandedTest,

        )
    }
}

data class BenWithTbScreeningCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val tb: TBScreeningCache?,

    ) {
    fun asTbScreeningDomainModel(): BenWithTbScreeningDomain {
        return BenWithTbScreeningDomain(
            ben = ben.asBasicDomainModel(),
            tb = tb
        )
    }
}

data class BenWithTbScreeningDomain(
    val ben: BenBasicDomain,
    val tb: TBScreeningCache?
)