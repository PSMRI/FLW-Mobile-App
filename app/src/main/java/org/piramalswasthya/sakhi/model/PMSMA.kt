package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.HelperUtil.getDateStringFromLong
import java.text.SimpleDateFormat
import java.util.Locale


@Entity(
    tableName = "PMSMA",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_pmsma", value = ["benId"/* "hhId"*/])]
)

data class PMSMACache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
//    val hhId: Long,

    var visitDate: Long? = null,
    var visitNumber: Int,
    var anyOtherHighRiskCondition: String? = null,

    var isActive: Boolean,
    var mctsNumberOrRchNumber: String? = null,
    var haveMCPCard: Boolean = false,
    var givenMCPCard: Boolean? = null,
    var husbandName: String? = null,
    var address: String? = null,
    var mobileNumber: Long? = null,
    var numANC: Int = 0,
    var weight: Int = 0,
    var systolicBloodPressure: String? = null,
    var bloodPressure: String? = null,
    var abdominalCheckUp: String? = null,
    var fetalHRPM: Int = 0,
    var twinPregnancy: Boolean = false,
    var urineAlbumin: String? = null,
    var haemoglobinAndBloodGroup: String? = null,
    var hiv: String? = null,
    var vdrl: String? = null,
    var hbsc: String? = null,
    var malaria: String? = null,
    var hivTestDuringANC: Boolean? = null,
    var swollenCondtion: Boolean? = null,
    var bloodSugarTest: Boolean? = null,
    var ultraSound: Boolean? = null,
    var ironFolicAcid: Boolean? = null,
    var calciumSupplementation: Boolean? = null,
    var tetanusToxoid: String? = null,
    var lastMenstrualPeriod: Long = 0,
    var expectedDateOfDelivery: Long = 0,
    var highriskSymbols: Boolean? = null,
    var highRiskReason: String? = null,
    var highRiskPregnant: Boolean? = null,
    var highRiskPregnancyReferred: Boolean? = null,
    var birthPrepAndNutritionAndFamilyPlanning: Boolean? = null,
    var medicalOfficerSign: String? = null,
    var processed: String? = "N",
    var createdBy: String,
    var createdDate: Long = System.currentTimeMillis(),
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

    fun asPostModel(): PmsmaPost {
        return PmsmaPost(
            id = id,
            benId = benId,
            isActive = isActive,
            visitDate =  visitDate?.let { HelperUtil.getDateStringFromLong(it) },
            visitNumber = visitNumber,
            anyOtherHighRiskCondition = anyOtherHighRiskCondition,
            rchNumber = mctsNumberOrRchNumber,
            haveMCPCard = haveMCPCard,
            givenMCPCard = givenMCPCard,
            husbandName = husbandName,
            address = address,
            mobileNumber = mobileNumber,
            numANC = numANC,
            weight = weight,
            systolicBloodPressure = systolicBloodPressure?.toInt(),
            diastolicBloodPressure = bloodPressure?.toInt(),
            abdominalCheckUp = abdominalCheckUp,
            fetalHRPM = fetalHRPM,
            twinPregnancy = twinPregnancy,
            urineAlbumin = urineAlbumin,
            haemoglobinAndBloodGroup = haemoglobinAndBloodGroup,
            hiv = hiv,
            vdrl = vdrl,
            hbsc = hbsc,
            malaria = malaria,
            hivTestDuringANC = hivTestDuringANC,
            swollenCondition = swollenCondtion,
            bloodSugarTest = bloodSugarTest,
            ultraSound = ultraSound,
            ironFolicAcid = ironFolicAcid,
            calciumSupplementation = calciumSupplementation,
            tetanusToxoid = tetanusToxoid,
            lastMenstrualPeriod = getDateStringFromLong(lastMenstrualPeriod),
            expectedDateOfDelivery = getDateStringFromLong(expectedDateOfDelivery),
            highriskSymbols = highriskSymbols,
            highRiskReason = highRiskReason,
            highRiskPregnant = highRiskPregnant,
            highRiskPregnancyReferred = highRiskPregnancyReferred,
            birthPrepNutriAndFamilyPlanning = birthPrepAndNutritionAndFamilyPlanning,
            medicalOfficerSign = medicalOfficerSign,
            createdBy = createdBy,
            createdDate = getDateStringFromLong(createdDate),
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

@JsonClass(generateAdapter = true)
data class PmsmaPost(

    val id: Long = 0,
    val benId: Long = 0,
    var visitDate: String? = null,
    var visitNumber: Int,
    var anyOtherHighRiskCondition: String? = null,
    val isActive: Boolean,
    val rchNumber: String? = null,
    val haveMCPCard: Boolean = false,
    val givenMCPCard: Boolean? = null,
    val husbandName: String? = null,
    val address: String? = null,
    val mobileNumber: Long? = null,
    val numANC: Int = 0,
    val weight: Int = 0,
    val systolicBloodPressure: Int? = null,
    val diastolicBloodPressure: Int? = null,
    val abdominalCheckUp: String? = null,
    val fetalHRPM: Int = 0,
    val twinPregnancy: Boolean = false,
    val urineAlbumin: String? = null,
    val haemoglobinAndBloodGroup: String? = null,
    val hiv: String? = null,
    val vdrl: String? = null,
    val hbsc: String? = null,
    val malaria: String? = null,
    val hivTestDuringANC: Boolean? = null,
    val swollenCondition: Boolean? = null,
    val bloodSugarTest: Boolean? = null,
    val ultraSound: Boolean? = null,
    val ironFolicAcid: Boolean? = null,
    val calciumSupplementation: Boolean? = null,
    val tetanusToxoid: String? = null,
    val lastMenstrualPeriod: String? = null,
    val expectedDateOfDelivery: String? = null,
    val highriskSymbols: Boolean? = null,
    val highRiskReason: String? = null,
    val highRiskPregnant: Boolean? = null,
    val highRiskPregnancyReferred: Boolean? = null,
    val birthPrepNutriAndFamilyPlanning: Boolean? = null,
    val medicalOfficerSign: String? = null,
    val createdBy: String,
    val createdDate: String? = null,
    val updatedDate: String? = null,
    val updatedBy: String
) {
    fun toPmsmaCache(): PMSMACache {
        return PMSMACache(
            id = id,
            benId = benId,
            isActive = isActive,
            visitDate =  getLongFromDate(visitDate),
            visitNumber = visitNumber,
            anyOtherHighRiskCondition = anyOtherHighRiskCondition,
//            hhId
            mctsNumberOrRchNumber = rchNumber,
            haveMCPCard = haveMCPCard,
            givenMCPCard = givenMCPCard,
            husbandName = husbandName,
            address = address,
            mobileNumber = mobileNumber,
            numANC = numANC,
            weight = weight,
            systolicBloodPressure = systolicBloodPressure.toString(),
            bloodPressure = diastolicBloodPressure.toString(),
            abdominalCheckUp = abdominalCheckUp,
            fetalHRPM = fetalHRPM,
            twinPregnancy = twinPregnancy,
            urineAlbumin = urineAlbumin,
            haemoglobinAndBloodGroup = haemoglobinAndBloodGroup,
            hiv = hiv,
            vdrl = vdrl,
            hbsc = hbsc,
            malaria = malaria,
            hivTestDuringANC = hivTestDuringANC,
            swollenCondtion = swollenCondition,
            bloodSugarTest = bloodSugarTest,
            ultraSound = ultraSound,
            ironFolicAcid = ironFolicAcid,
            calciumSupplementation = calciumSupplementation,
            tetanusToxoid = tetanusToxoid,
            lastMenstrualPeriod = getLongFromDate(lastMenstrualPeriod),
            expectedDateOfDelivery = getLongFromDate(expectedDateOfDelivery),
            highriskSymbols = highriskSymbols,
            highRiskReason = highRiskReason,
            highRiskPregnant = highRiskPregnant,
            highRiskPregnancyReferred = highRiskPregnancyReferred,
            birthPrepAndNutritionAndFamilyPlanning = birthPrepNutriAndFamilyPlanning,
            medicalOfficerSign = medicalOfficerSign,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}