package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.getWeeksOfPregnancy
import org.piramalswasthya.sakhi.network.getLongFromDate
import java.text.SimpleDateFormat
import java.util.Locale


data class PregnantWomenVisitCache(
    val benId: Long,
    val name: String,
    val dob: Long,
    val spouseName: String,
    val lmp: Long,
) {
    fun asDomainModel() =
        PregnantWomenVisitDomain(
            benId = benId,
            name = name,
            age = "${BenBasicCache.getAgeFromDob(dob)} Years",
            spouseName = spouseName,
            lmp = lmp,
            weeksOfPregnancy = getWeeksOfPregnancy(System.currentTimeMillis(), lmp)
        )

}

data class PregnantWomenVisitDomain(
    val benId: Long,
    val name: String,
    val age: String,
    val spouseName: String,
    val lmp: Long,
    val weeksOfPregnancy: Int,
    val weeksOfPregnancyString: String = if(weeksOfPregnancy<=40) weeksOfPregnancy.toString() else "NA",
)

data class AncStatus(
    val benId: Long,
    val visitNumber: Int,
    val formState: AncFormState,
    val filledWeek : Int
)

enum class AncFormState {
    ALLOW_FILL,
    ALREADY_FILLED,
    NO_FILL
}

@Entity(
    tableName = "PREGNANCY_REGISTER",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_pwc", value = ["benId"/* "hhId"*/])]
)

data class PregnantWomanRegistrationCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
    var dateOfRegistration: Long = System.currentTimeMillis(),
    var mcpCardNumber: Long? = 0,
    var rchId: Long? = 0,
    var lmpDate: Long = 0,
//    var weeksOfPregnancy : String,
//    var weeksOfPregnancyId : Int,
//    var expectedDateOfDelivery : Long,
    var bloodGroup: String? = null,
    var bloodGroupId: Int = 0,
    var weight: Int? = null,
    var height: Int? = null,
    var vdrlRprTestResult: String? = null,
    var vdrlRprTestResultId: Int = 0,
    var dateOfVdrlRprTest: Long? = null,

    var hivTestResult: String? = null,
    var hivTestResultId: Int = 0,
    var dateOfHivTest: Long? = null,

    var hbsAgTestResult: String? = null,
    var hbsAgTestResultId: Int = 0,
    var dateOfHbsAgTest: Long? = null,

    var pastIllness: String? = null,
    var otherPastIllness: String? = null,
    var is1st: Boolean = true,
    var numPrevPregnancy: Int? = null,
    var complicationPrevPregnancy: String? = null,
    var complicationPrevPregnancyId: Int? = null,
    var otherComplication: String? = null,
    var isHrp: Boolean = false,
    var hrpIdBy: String? = null,
    var hrpIdById: Int = 0,
    var active: Boolean = true,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun asPwrPost() : PwrPost {
        return PwrPost(
            benId = benId,
            registrationDate = getDateStringFromLong(dateOfRegistration),
            rchId = rchId,
            mcpCardId = mcpCardNumber,
            lmpDate = getDateStringFromLong(lmpDate),
            bloodGroup = bloodGroup,
            weight = weight,
            height = height,
            rprTestResult = vdrlRprTestResult,
            dateOfRprTest = getDateStringFromLong(dateOfVdrlRprTest),
            hivTestResult = hivTestResult,
            hbsAgTestResult = hbsAgTestResult,
            dateOfHivTest = getDateStringFromLong(dateOfHivTest),
            dateOfHbsAgTest = getDateStringFromLong(dateOfHbsAgTest),
            pastIllness = pastIllness,
            otherPastIllness = otherPastIllness,
            isFirstPregnancyTest = is1st,
            numPrevPregnancy = numPrevPregnancy,
            pregComplication = complicationPrevPregnancy,
            otherComplication = otherComplication,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

data class PwrPost (
    val id: Long = 0,
    val benId: Long = 0,
    val registrationDate: String? = null,
    val rchId: Long? = null,
    val mcpCardId: Long? = null,
    val lmpDate: String? = null,
    val bloodGroup: String? = null,
    val weight: Int? = null,
    val height: Int? = null,
    val rprTestResult: String? = null,
    val dateOfRprTest: String? = null,
    val hivTestResult: String? = null,
    val hbsAgTestResult: String? = null,
    val dateOfHivTest: String? = null,
    val dateOfHbsAgTest: String? = null,
    val pastIllness: String? = null,
    val otherPastIllness: String? = null,
    val isFirstPregnancyTest: Boolean = true,
    val numPrevPregnancy: Int? = null,
    val pregComplication: String? = null,
    val otherComplication: String? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String
) {
    fun toPwrCache(): PregnantWomanRegistrationCache {
        return PregnantWomanRegistrationCache(
            id = id,
            benId = benId,
            dateOfRegistration = getLongFromDate(registrationDate),
            mcpCardNumber = mcpCardId,
            rchId = rchId,
            lmpDate = getLongFromDate(lmpDate),
            bloodGroup = bloodGroup,
//            bloodGroupId =
            weight = weight,
            height = height,
            vdrlRprTestResult = rprTestResult,
//            vdrlRprTestResultId
            dateOfVdrlRprTest = getLongFromDate(dateOfRprTest),
            hivTestResult = hivTestResult,
//            hivTestResultId
            dateOfHivTest = getLongFromDate(dateOfHivTest),
            hbsAgTestResult = hbsAgTestResult,
//            hbsAgTestResultId
            dateOfHbsAgTest = getLongFromDate(dateOfHbsAgTest),
            pastIllness = pastIllness,
            otherPastIllness = otherPastIllness,
            is1st = isFirstPregnancyTest,
            numPrevPregnancy = numPrevPregnancy,
            complicationPrevPregnancy = pregComplication,
//            complicationPrevPregnancyId = otherComplication,
            otherComplication = otherComplication,
//            isHrp =
//            hrpIdBy
//            hrpIdById
            active = true,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}


@Entity(
    tableName = "PREGNANCY_ANC",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_mha", value = ["benId"/* "hhId"*/])],
)

data class PregnantWomanAncCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
    val visitNumber: Int,
    var ancDate: Long = System.currentTimeMillis(),
    var isAborted: Boolean = false,
    var abortionType: String? = null,
    var abortionTypeId: Int = 0,
    var abortionFacility: String? = null,
    var abortionFacilityId: Int = 0,
    var abortionDate: Long? = null,
    var weight: Int? = null,
    var bpSystolic: Int? = null,
    var bpDiastolic: Int? = null,
    var pulseRate: String? = null,
    var hb: Double? = null,
    var fundalHeight: Int? = null,
    var urineAlbumin: String? = null,
    var urineAlbuminId: Int = 0,
    var randomBloodSugarTest: String? = null,
    var randomBloodSugarTestId: Int = 0,
    var tt1: Long? = null,
    var tt2: Long? = null,
    var ttBooster: Long? = null,
    var numFolicAcidTabGiven: Int = 0,
    var numIfaAcidTabGiven: Int = 0,
    var anyHighRisk: Boolean = false,
    var highRisk: String? = null,
    var highRiskId: Int = 0,
    var otherHighRisk: String? = null,
    var referralFacility: String? = null,
    var referralFacilityId: Int = 0,
    var hrpConfirmed: Boolean? = null,
    var hrpConfirmedBy: String? = null,
    var hrpConfirmedById : Int = 0,
    var maternalDeath : Boolean = false,
    var maternalDeathProbableCause : String? = null,
    var maternalDeathProbableCauseId : Int = 0,
    var otherMaternalDeathProbableCause : String? = null,
    var deathDate : Long? = null,
    var pregnantWomanDelivered : Boolean? = null,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun asPostModel(): ANCPost {
        return ANCPost(
            benId = benId,
            ancDate = getDateStringFromLong(ancDate),
            ancVisit = visitNumber,
            isAborted = isAborted,
            abortionType = abortionType,
            abortionFacility = abortionFacility,
            abortionDate = getDateStringFromLong(abortionDate),
            weightOfPW = weight,
            bpSystolic = bpSystolic,
            bpDiastolic = bpDiastolic,
            pulseRate = pulseRate?.toInt(),
            hb = hb,
            fundalHeight = fundalHeight,
            urineAlbuminPresent = urineAlbumin == "Present",
            bloodSugarTestDone = randomBloodSugarTest == "Done",
            tdDose1Date = getDateStringFromLong(tt1),
            tdDose2Date = getDateStringFromLong(tt2),
            tdDoseBoosterDate = getDateStringFromLong(ttBooster),
            folicAcidTabs = numFolicAcidTabGiven,
            ifaTabs = numIfaAcidTabGiven,
            isHighRisk = anyHighRisk,
            highRiskCondition = highRisk,
            otherHighRiskCondition = otherHighRisk,
            referralFacility = referralFacility,
            isHrpConfirmed = hrpConfirmed,
            hrpIdentifiedBy = hrpConfirmedBy,
            isMaternalDeath = maternalDeath,
            probableCauseOfDeath = maternalDeathProbableCause,
            otherCauseOfDeath = otherMaternalDeathProbableCause,
            deathDate = getDateStringFromLong(deathDate),
            isBabyDelivered = pregnantWomanDelivered,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

private fun getDateStringFromLong(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateLong?.let {
        val dateString = dateFormat.format(dateLong)
        return dateString
    } ?: run {
        return null
    }

}

data class ANCPost (
    val id: Long = 0,
    val benId: Long = 0,
    val ancDate: String? = null,
    val ancVisit: Int,
    val isAborted: Boolean = false,
    val abortionType: String? = null,
    val abortionFacility: String? = null,
    val abortionDate: String? = null,
    val weightOfPW: Int? = null,
    val bpSystolic: Int? = null,
    val bpDiastolic: Int? = null,
    val pulseRate: Int? = null,
    val hb: Double? = null,
    val fundalHeight: Int? = null,
    val urineAlbuminPresent: Boolean? = null,
    val bloodSugarTestDone: Boolean? = null,
    val tdDose1Date: String? = null,
    val tdDose2Date: String? = null,
    val tdDoseBoosterDate: String? = null,
    val folicAcidTabs: Int = 0,
    val ifaTabs: Int = 0,
    val isHighRisk: Boolean = false,
    val highRiskCondition: String? = null,
    val otherHighRiskCondition: String? = null,
    val referralFacility: String? = null,
    val isHrpConfirmed: Boolean? = null,
    val hrpIdentifiedBy: String? = null,
    val isMaternalDeath: Boolean = false,
    val probableCauseOfDeath: String? = null,
    val otherCauseOfDeath: String? = null,
    val deathDate: String? = null,
    val isBabyDelivered: Boolean? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String
        ) {
    fun toAncCache(): PregnantWomanAncCache {
        return PregnantWomanAncCache(
            id = id,
            benId = benId,
            visitNumber = ancVisit,
            ancDate = getLongFromDate(ancDate),
            isAborted = isAborted,
            abortionType = abortionType,
//            abortionTypeId =
            abortionFacility = abortionFacility,
//            abortionFacilityId
            abortionDate = getLongFromDate(abortionDate),
            weight = weightOfPW,
            bpSystolic = bpSystolic,
            bpDiastolic = bpDiastolic,
            pulseRate = pulseRate.toString(),
            hb = hb,
            fundalHeight = fundalHeight,
            urineAlbumin = if(urineAlbuminPresent == true) "Present" else "Absent",
//            urineAlbuminId
            randomBloodSugarTest = if(bloodSugarTestDone == true) "Done" else "Not Done",
//            randomBloodSugarTestId
            tt1 = getLongFromDate(tdDose1Date),
            tt2 = getLongFromDate(tdDose2Date),
            ttBooster = getLongFromDate(tdDoseBoosterDate),
            numFolicAcidTabGiven = folicAcidTabs,
            numIfaAcidTabGiven = ifaTabs,
            anyHighRisk = isHighRisk,
            highRisk = highRiskCondition,
//            highRiskId
            otherHighRisk = otherHighRiskCondition,
            referralFacility = referralFacility,
//            referralFacilityId
//            hrpConfirmed
//            hrpConfirmedBy
//            hrpConfirmedById
            maternalDeath = isMaternalDeath,
            maternalDeathProbableCause = probableCauseOfDeath,
//            maternalDeathProbableCauseId
            otherMaternalDeathProbableCause = otherCauseOfDeath,
            deathDate = getLongFromDate(deathDate),
            pregnantWomanDelivered = isBabyDelivered,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}