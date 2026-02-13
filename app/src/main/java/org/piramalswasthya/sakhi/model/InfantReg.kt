package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "INFANT_REG",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("motherBenId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "infRegInd", value = ["motherBenId"])]
)

data class InfantRegCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var childBenId: Long = 0,
    val motherBenId: Long,
    var isActive: Boolean,
    var babyName: String? = null,
    var babyIndex: Int,
    var infantTerm: String? = null,
    var corticosteroidGiven: String? = null,
    var gender: Gender? = null,
    var babyCriedAtBirth: Boolean? = null,
    var resuscitation: Boolean? = null,
    var referred: String? = null,
    var hadBirthDefect: String? = null,
    var birthDefect: String? = null,

    var isSNCU: String? = "No",
    var deliveryDischargeSummary1 : String? = null,
    var deliveryDischargeSummary2 : String? = null,
    var deliveryDischargeSummary3 : String? = null,
    var deliveryDischargeSummary4 : String? = null,

    var otherDefect: String? = null,
    var weight: Double? = 0.0,
    var breastFeedingStarted: Boolean? = null,
    var opv0Dose: Long? = System.currentTimeMillis(),
    var bcgDose: Long? = System.currentTimeMillis(),
    var hepBDose: Long? = System.currentTimeMillis(),
    var vitkDose: Long? = System.currentTimeMillis(),
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {

    private fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        dateLong?.let {
            return dateFormat.format(dateLong)
        } ?: run {
            return null
        }
    }

    fun asPostModel(): InfantRegPost {
        return InfantRegPost(
            id = id,
            benId = motherBenId,
            childBenId = childBenId,
            isSNCU = isSNCU,
            deliveryDischargeSummary1 = deliveryDischargeSummary1,
            deliveryDischargeSummary2 = deliveryDischargeSummary2,
            deliveryDischargeSummary3 = deliveryDischargeSummary3,
            deliveryDischargeSummary4 = deliveryDischargeSummary4,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            gender = gender?.name,
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight?:0.0,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = opv0Dose?.let { getDateStringFromLong(it) },
            bcgDose = bcgDose?.let { getDateStringFromLong(it) },
            hepBDose = hepBDose?.let { getDateStringFromLong(it) },
            vitkDose = vitkDose?.let { getDateStringFromLong(it) },
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

data class BenWithDoAndIrCache(
//    @ColumnInfo(name = "benId")
//    val ecBenId: Long,

    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = DeliveryOutcomeCache::class
    )
    val deliveryOutcomeCache: List<DeliveryOutcomeCache>,

    @Relation(
        parentColumn = "benId", entityColumn = "motherBenId", entity = InfantRegCache::class
    )
    val savedIrRecords: List<InfantRegCache>
) {

    fun asBasicDomainModel(): List<InfantRegDomain> {

        val activeDo = deliveryOutcomeCache.first { it.isActive }
        val activeIr = savedIrRecords.filter { it.isActive }
        val list = mutableListOf<InfantRegDomain>()
        val numLiveBirth = activeDo.liveBirth ?: 1
        if (numLiveBirth == 0) return emptyList()
        for (i in 0 until numLiveBirth) {
            list.add(
                InfantRegDomain(
                    motherBen = ben.asBasicDomainModel(),
                    babyIndex = i,
                    deliveryOutcome = activeDo,
                    savedIr = activeIr.firstOrNull { it.babyIndex == i },
                )
            )
        }

        return list
    }
}

data class InfantRegDomain(
    val motherBen: BenBasicDomain,
    val babyIndex: Int,
    var babyName: String = "Baby $babyIndex of ${motherBen.benFullName}",
    val deliveryOutcome: DeliveryOutcomeCache,
    val savedIr: InfantRegCache?,
    val syncState: SyncState? = savedIr?.syncState
) {
    val customName: String
        get() = if (babyIndex == 0) {
            "${babyIndex+1}st baby of ${motherBen.benName}"
        } else if (babyIndex == 1) {
            "${babyIndex+1}nd baby of ${motherBen.benName}"
        } else {
            "${babyIndex+1}rd baby of ${motherBen.benName}"
        }
}

data class InfantRegPost(
    val id: Long = 0,
    val benId: Long,
    val childBenId: Long,
    val isActive: Boolean,
    var isSNCU: String? = null,
    var deliveryDischargeSummary1 : String? = null,
    var deliveryDischargeSummary2 : String? = null,
    var deliveryDischargeSummary3 : String? = null,
    var deliveryDischargeSummary4 : String? = null,


    val babyName: String? = null,
    val babyIndex: Int,
    val infantTerm: String? = null,
    val corticosteroidGiven: String? = null,
    val gender: String? = null,
    val babyCriedAtBirth: Boolean? = null,
    val resuscitation: Boolean? = null,
    val referred: String? = null,
    val hadBirthDefect: String? = null,
    val birthDefect: String? = null,
    val otherDefect: String? = null,
    val weight: Double,
    val breastFeedingStarted: Boolean? = null,
    val opv0Dose: String? = null,
    val bcgDose: String? = null,
    val hepBDose: String? = null,
    val vitkDose: String? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String
) {
    fun toCacheModel(): InfantRegCache {
        return InfantRegCache(
            id = id,
            motherBenId = benId,
            isSNCU  =isSNCU,
            deliveryDischargeSummary1=deliveryDischargeSummary1,
            deliveryDischargeSummary2=deliveryDischargeSummary2,
            deliveryDischargeSummary3=deliveryDischargeSummary3,
            deliveryDischargeSummary4=deliveryDischargeSummary4,
            childBenId = childBenId,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
//            gender = gender,
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = getLongFromDate(opv0Dose),
            bcgDose = getLongFromDate(bcgDose),
            hepBDose = getLongFromDate(hepBDose),
            vitkDose = getLongFromDate(vitkDose),
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}

data class ChildRegDomain(
    val motherBen: BenBasicDomain,
    val infant: InfantRegCache,
    val childBen: BenBasicDomain?
)

data class InfantRegWithBen(
    @Embedded
    val infant: InfantRegCache,
    @Relation(
        parentColumn = "motherBenId", entityColumn = "benId", entity = BenBasicCache::class
    )
    val motherBen: BenBasicCache,
    @Relation(
        parentColumn = "childBenId", entityColumn = "benId", entity = BenBasicCache::class
    )
    val childBen: BenBasicCache?,
) {

    fun asBasicDomainModel(): ChildRegDomain {

        return ChildRegDomain(
            motherBen = motherBen.asBasicDomainModel(),
            infant = infant,
            childBen = childBen?.asBasicDomainModel()
        )
    }
}
