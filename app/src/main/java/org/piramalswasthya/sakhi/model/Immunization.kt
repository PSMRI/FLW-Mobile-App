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

enum class ChildImmunizationCategory {
    BIRTH, WEEK_6, WEEK_10, WEEK_14, MONTH_9_12, MONTH_16_24, YEAR_5_6, YEAR_10, YEAR_16, CATCH_UP
}

enum class VaccineType {
    BCG,
    HEPB_BIRTH,
    OPV_0,
    PENTA_1, OPV_1, RVV_1, FIPV_1,
    PENTA_2, OPV_2, RVV_2,
    PENTA_3, OPV_3, RVV_3, FIPV_2,
    MR_1, JE_1, VIT_A_1,
    DPT_BOOSTER_1, MR_2, JE_2, OPV_BOOSTER, VIT_A_2,
    DPT_BOOSTER_2,
    TD,
    VIT_K,
    VIT_A_3, VIT_A_4, VIT_A_5, VIT_A_6, VIT_A_7, VIT_A_8,
    PCV_1, PCV_2, PCV_BOOSTER,
    UNKNOWN
}

fun String.toVaccineType(): VaccineType {
    return when (this.trim()) {
        "BCG Vaccine" -> VaccineType.BCG
        "Hepatitis-B Vaccine (HBV)-Birth" -> VaccineType.HEPB_BIRTH
        "Oral Polio Vaccine (OPV)-0" -> VaccineType.OPV_0
        "Pentavalent-1" -> VaccineType.PENTA_1
        "OPV-1" -> VaccineType.OPV_1
        "Rotavirus Vaccine (RVV)-1" -> VaccineType.RVV_1
        "fractional Dose of IPV (fIPV)-1" -> VaccineType.FIPV_1
        "Pentavalent-2" -> VaccineType.PENTA_2
        "OPV-2" -> VaccineType.OPV_2
        "Rotavirus Vaccine (RVV)-2" -> VaccineType.RVV_2
        "Pentavalent-3" -> VaccineType.PENTA_3
        "OPV-3" -> VaccineType.OPV_3
        "Rotavirus Vaccine (RVV)-3" -> VaccineType.RVV_3
        "fractional Dose of IPV (fIPV)-2" -> VaccineType.FIPV_2
        "Measles & Rubella (MR)-1" -> VaccineType.MR_1
        "JE-1" -> VaccineType.JE_1
        "Oral Vitamin A (1st Dose)" -> VaccineType.VIT_A_1
        "DPT Booster-1" -> VaccineType.DPT_BOOSTER_1
        "Measles & Rubella (MR)-2" -> VaccineType.MR_2
        "JE-2" -> VaccineType.JE_2
        "OPV Booster" -> VaccineType.OPV_BOOSTER
        "Oral Vitamin A (2nd Dose)" -> VaccineType.VIT_A_2
        "DPT Booster-2" -> VaccineType.DPT_BOOSTER_2
        "Tetanus & adult Diphtheria (Td)" -> VaccineType.TD
        "Vitamin K" -> VaccineType.VIT_K
        "Oral Vitamin A (3rd Dose)" -> VaccineType.VIT_A_3
        "Oral Vitamin A (4th Dose)" -> VaccineType.VIT_A_4
        "Oral Vitamin A (5th Dose)" -> VaccineType.VIT_A_5
        "Oral Vitamin A (6th Dose)" -> VaccineType.VIT_A_6
        "Oral Vitamin A (7th Dose)" -> VaccineType.VIT_A_7
        "Oral Vitamin A (8th Dose)" -> VaccineType.VIT_A_8
        "PCV-1" -> VaccineType.PCV_1
        "PCV-2" -> VaccineType.PCV_2
        "PCV-Booster" -> VaccineType.PCV_BOOSTER
        else -> VaccineType.UNKNOWN
    }
}

enum class ImmunizationCategory {
    CHILD,
    MOTHER
}

@Entity(tableName = "VACCINE")
data class Vaccine(
    @PrimaryKey
    val vaccineId: Int,
    val vaccineName: String,
    val minAllowedAgeInMillis: Long,
    val maxAllowedAgeInMillis: Long,
    val category: ImmunizationCategory,
    val immunizationService: ChildImmunizationCategory,
//    val dueDuration: Long,
    val overdueDurationSinceMinInMillis: Long = maxAllowedAgeInMillis,
    val dependantVaccineId: Int? = null,
    val dependantCoolDuration: Long? = null,
)

@Entity(
    tableName = "IMMUNIZATION",
    primaryKeys = ["beneficiaryId", "vaccineId"],
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("beneficiaryId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Vaccine::class,
        parentColumns = arrayOf("vaccineId"),
        childColumns = arrayOf("vaccineId"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        name = "ind_imm", value = ["beneficiaryId"]
    ), Index(name = "ind_vaccine", value = ["vaccineId"])]
)
data class ImmunizationCache(
    val id: Long = 0,
    val beneficiaryId: Long,
    var vaccineId: Int,
    var date: Long? = null,
    var placeId: Int = 0,
    var place: String = "",
    var byWhoId: Int = 0,
    var byWho: String = "",
    var processed: String? = "N",
    var createdBy: String,
    var createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState,
    var mcpCardSummary1 : String ? = null,
    var mcpCardSummary2 : String ? = null
) : FormDataModel {
    fun asPostModel(): ImmunizationPost {
        return ImmunizationPost(
            id = id,
            beneficiaryId = beneficiaryId,
            vaccineId = vaccineId,
            vaccineName = "",
            receivedDate = getDateStrFromLong(date),
            vaccinationreceivedat = place,
            vaccinatedBy = byWho,
            createdDate = getDateStrFromLong(createdDate),
            createdBy = createdBy,
            modifiedBy = updatedBy,
            lastModDate = getDateStrFromLong(updatedDate),
            mcpCardSummary1 = mcpCardSummary1,
            mcpCardSummary2 = mcpCardSummary2
        )
    }
}

data class ChildImmunizationDetailsCache(
//    @ColumnInfo(name = "benId")
    @Embedded
    val ben: BenBasicCache,
//    @ColumnInfo(name = "benName") val benName : String,
    @Relation(
        parentColumn = "benId", entityColumn = "beneficiaryId"
    ) val givenVaccines: List<ImmunizationCache>
)

data class MotherImmunizationDetailsCache(
//    @ColumnInfo(name = "benId")
    @Embedded
    val ben: BenBasicCache,

    val lmp: Long,
//    @ColumnInfo(name = "benName") val benName : String,
    @Relation(
        parentColumn = "benId", entityColumn = "beneficiaryId"
    ) val givenVaccines: List<ImmunizationCache>
)

data class ImmunizationDetailsDomain(
    val ben: BenBasicDomain,
    val vaccineStateList: List<VaccineDomain>,
//    val onClick: (Long, Int) -> Unit
)

data class VaccineCategoryDomain(
    val category: ChildImmunizationCategory,
    val categoryString: String = category.name,
    val vaccineStateList: List<VaccineDomain>,
    var isBenDeath: Boolean =false
//    val onClick: (Long, Int) -> Unit
)

data class VaccineDomain(
//    val benId: Long,
    val vaccineId: Int,
    val vaccineName: String,
    val vaccineCategory: ChildImmunizationCategory,
    val state: VaccineState,
    var isSwitchChecked:Boolean = false,
    val dueDate: String = ""
)

class VaccineClickListener(private val clickListener: (benId: Long, vaccineId: Int) -> Unit) {
    fun onClick(benId: Long, vaccine: VaccineDomain) = clickListener(benId, vaccine.vaccineId)
}

data class ImmunizationDetailsHeader(
    val list: List<String>
)


enum class VaccineState {
    PENDING, OVERDUE, DONE, MISSED, UNAVAILABLE
}

data class ImmunizationPost(
    val id: Long = 0,
    val beneficiaryId: Long,
    val vaccineId: Int,
    var vaccineName: String = "",
    val receivedDate: String? = null,
    val vaccinationreceivedat: String? = null,
    val vaccinatedBy: String? = null,
    val createdDate: String? = null,
    val createdBy: String,
    var lastModDate: String? = null,
    var modifiedBy: String,
    var mcpCardSummary1 : String ? = null,
    var mcpCardSummary2 : String ? = null
) {
    fun toCacheModel(): ImmunizationCache {
        return ImmunizationCache(
            id = id,
            beneficiaryId = beneficiaryId,
            vaccineId = vaccineId,
            date = getLongFromDate(receivedDate),
//            placeId = 0,
            place = if (vaccinationreceivedat.isNullOrEmpty()) "" else vaccinationreceivedat,
//            byWhoId = 0,
            byWho = if (vaccinatedBy.isNullOrEmpty()) "" else vaccinatedBy,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = modifiedBy,
            updatedDate = getLongFromDate(lastModDate),
            syncState = SyncState.SYNCED,
            mcpCardSummary1 = mcpCardSummary1,
            mcpCardSummary2 = mcpCardSummary2,
        )
    }
}