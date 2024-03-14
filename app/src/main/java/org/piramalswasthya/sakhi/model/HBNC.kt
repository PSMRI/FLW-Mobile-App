package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.utils.HelperUtil

@Entity(
    tableName = "HBNC",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_hbnc", value = ["benId"/* "hhId"*/])]
)

data class HBNCCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    val hhId: Long,
    val homeVisitDate: Int,


    @Embedded(prefix = "card_")
    var visitCard: HbncVisitCard? = null,

    @Embedded(prefix = "part_I_")
    var part1: HbncPartI? = null,

    @Embedded(prefix = "part_II_")
    var part2: HbncPartII? = null,

    @Embedded(prefix = "visit_")
    var homeVisitForm: HbncHomeVisit? = null,

//    @Embedded(prefix = "part_D")
//    var partD: HbncPartD? = null,

//    var ashaName: String? = null,
//    var supervisorRemark: String? = null,
//    var supervisorName: String? = null,
//    var supervisorComments: String? = null,
//    var dateSupervisorVisit: Long = 0,
    var processed: String? = null,
    var syncState: SyncState

) : FormDataModel {

    fun asPostModel(
        user: User
    ): HBNCPost {
        var hbncVisitCardDTO: HBNCVisitCardPost? = null
        var hbncPart1DTO: HBNCPart1Post? = null
        var hbncPart2DTO: HBNCPart2Post? = null
        var hbncVisitDTO: HBNCVisitPost? = null

        visitCard?.let {
            hbncVisitCardDTO = HBNCVisitCardPost(
                id = 0L,
                benId = benId,
                visitNo = homeVisitDate,
                ashaName = it.ashaName,
                villageName = it.villageName,
                subCenterName = it.subCenterName,
                blockName = it.blockName,
                motherName = it.motherName,
                fatherName = it.fatherName,
                dateOfDelivery = HelperUtil.getDateStrFromLong(it.dateOfDelivery),
                placeOfDelivery = it.placeOfDelivery.toString(),
                babyGender = it.babyGender.toString(),
                typeOfDelivery = it.typeOfDelivery.toString(),
                stillBirth = it.stillBirth != 0,
                startedBreastFeeding = it.startedBreastFeeding.toString(),
                dischargeDateMother = HelperUtil.getDateStrFromLong(it.dischargeDateMother),
                dischargeDateBaby = HelperUtil.getDateStrFromLong(it.dischargeDateBaby),
                weightInGrams = it.weightInGrams,
                registrationOfBirth = it.registrationOfBirth != 0,
                createdBy = user.userName,
                createdDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
                updatedBy = user.userName,
                updatedDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
            )
        }

        part1?.let {
            hbncPart1DTO = HBNCPart1Post(
                id = 0L,
                benId = benId,
                visitNo = homeVisitDate,
                dateOfVisit = HelperUtil.getDateStrFromLong(it.dateOfVisit),
                babyAlive = it.babyAlive != 0,
                dateOfBabyDeath = HelperUtil.getDateStrFromLong(it.dateOfBabyDeath),
                timeOfBabyDeath = it.timeOfBabyDeath,
                placeOfBabyDeath = it.placeOfBabyDeath.toString(),
                otherPlaceOfBabyDeath = it.otherPlaceOfBabyDeath,
                isBabyPreterm = it.isBabyPreterm != 0,
                gestationalAge = it.gestationalAge.toString(),
                dateOfFirstExamination = HelperUtil.getDateStrFromLong(it.dateOfFirstExamination),
                timeOfFirstExamination = it.timeOfFirstExamination,
                motherAlive = it.motherAlive != 0,
                dateOfMotherDeath = HelperUtil.getDateStrFromLong(it.dateOfMotherDeath),
                timeOfMotherDeath = it.timeOfMotherDeath,
                placeOfMotherDeath = it.placeOfMotherDeath.toString(),
                otherPlaceOfMotherDeath = it.otherPlaceOfMotherDeath,
                motherAnyProblem = it.motherAnyProblem,
                babyFirstFed = it.babyFirstFed.toString(),
                otherBabyFirstFed = it.otherBabyFirstFed,
                timeBabyFirstFed = it.timeBabyFirstFed,
                howBabyTookFirstFeed = it.howBabyTookFirstFeed.toString(),
                motherHasBreastFeedProblem = it.motherHasBreastFeedProblem != 0,
                motherBreastFeedProblem = it.motherBreastFeedProblem,
                createdBy = user.userName,
                createdDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
                updatedBy = user.userName,
                updatedDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
            )
        }

        part2?.let {
            hbncPart2DTO = HBNCPart2Post(
                id = 0L,
                benId = benId,
                visitNo = homeVisitDate,
                dateOfVisit = HelperUtil.getDateStrFromLong(it.dateOfVisit),
                babyTemperature = it.babyTemperature,
                babyEyeCondition = it.babyEyeCondition.toString(),
                babyUmbilicalBleed = it.babyUmbilicalBleed != 0,
                actionBabyUmbilicalBleed = it.actionBabyUmbilicalBleed != 0,
                babyWeight = it.babyWeight.toFloat(),
                babyWeightMatchesColor = it.babyWeightMatchesColor != 0,
                babyWeightColorOnScale = it.babyWeightColorOnScale.toString(),
                allLimbsLimp = it.allLimbsLimp != 0,
                feedLessStop = it.feedLessStop != 0,
                cryWeakStop = it.cryWeakStop != 0,
                dryBaby = it.dryBaby != 0,
                wrapClothCloseToMother = it.wrapClothCloseToMother != 0,
                exclusiveBreastFeeding = it.exclusiveBreastFeeding != 0,
                cordCleanDry = it.cordCleanDry != 0,
                unusualInBaby = it.unusualInBaby.toString(),
                otherUnusualInBaby = it.otherUnusualInBaby,
                createdBy = user.userName,
                createdDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
                updatedBy = user.userName,
                updatedDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
            )
        }

        homeVisitForm?.let {
            hbncVisitDTO = HBNCVisitPost(
                id = 0L,
                benId = benId,
                visitNo = homeVisitDate,
                dateOfVisit = HelperUtil.getDateStrFromLong(it.dateOfVisit),
                babyAlive = it.babyAlive != 0,
                numTimesFullMeal24hr = it.numTimesFullMeal24hr,
                numPadChanged24hr = it.numPadChanged24hr,
                babyKeptWarmWinter = it.babyKeptWarmWinter != 0,
                babyFedProperly = it.babyFedProperly != 0,
                babyCryContinuously = it.babyCryContinuously != 0,
                motherTemperature = if (it.motherTemperature != null) it.motherTemperature.toInt() else 0,
                foulDischargeFever = it.foulDischargeFever != 0,
                motherSpeakAbnormallyFits = it.motherSpeakAbnormallyFits != 0,
                motherLessNoMilk = it.motherLessNoMilk != 0,
                motherBreastProblem = it.motherBreastProblem != 0,
                babyEyesSwollen = it.babyEyesSwollen != 0,
                babyWeight = (if (it.babyWeight != null) it.babyWeight.toFloat() else null),
                babyTemperature = if (it.babyTemperature != null) it.babyTemperature.toInt() else 0,
                babyYellow = it.babyYellow != 0,
                babyImmunizationStatus = it.babyImmunizationStatus,
                babyReferred = it.babyReferred != 0,
                dateOfBabyReferral = it.dateOfBabyReferral.toString(),
                placeOfBabyReferral = it.placeOfBabyReferral.toString(),
                otherPlaceOfBabyReferral = it.otherPlaceOfBabyReferral,
                motherReferred = it.motherReferred != 0,
                dateOfMotherReferral = HelperUtil.getDateStrFromLong(it.dateOfMotherReferral),
                placeOfMotherReferral = it.placeOfMotherReferral.toString(),
                otherPlaceOfMotherReferral = it.otherPlaceOfMotherReferral,
                allLimbsLimp = it.allLimbsLimp != 0,
                feedingLessStopped = it.feedingLessStopped != 0,
                cryWeakStopped = it.cryWeakStopped != 0,
                bloatedStomach = it.bloatedStomach != 0,
                coldOnTouch = it.coldOnTouch != 0,
                chestDrawing = it.chestDrawing != 0,
                breathFast = it.breathFast != 0,
                pusNavel = it.pusNavel.toString(),
                supervisor = it.sup.toString(),
                supervisorName = it.supName,
                supervisorComment = it.supComment,
                supervisorSignDate = HelperUtil.getDateStrFromLong(it.supSignDate),
                createdBy = user.userName,
                createdDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
                updatedBy = user.userName,
                updatedDate = HelperUtil.getDateStrFromLong(System.currentTimeMillis()),
            )
        }

        return HBNCPost(
            id = id,
            benId = benId,
            hhId = hhId,
            homeVisitDate = homeVisitDate,
            hbncVisitCardDTO = hbncVisitCardDTO,
            hbncPart1DTO = hbncPart1DTO,
            hbncPart2DTO = hbncPart2DTO,
            hbncVisitDTO = hbncVisitDTO
        )
    }
}

data class HbncVisitCard(
    val ashaName: String?,
    val villageName: String?,
    val subCenterName: String?,
    val blockName: String?,
    val motherName: String?,
    val fatherName: String?,
    val dateOfDelivery: Long,
    val placeOfDelivery: Int,
    val babyGender: Int,
    val typeOfDelivery: Int,
    val stillBirth: Int,
    val startedBreastFeeding: Int,
    val dischargeDateMother: Long,
    val dischargeDateBaby: Long,
    val weightInGrams: Int,
    val registrationOfBirth: Int
)

data class HbncPartI(
    val dateOfVisit: Long,
    val babyAlive: Int,
    val dateOfBabyDeath: Long,
    val timeOfBabyDeath: String?,
    val placeOfBabyDeath: Int,
    val otherPlaceOfBabyDeath: String?,
    val isBabyPreterm: Int,
    val gestationalAge: Int,
    val dateOfFirstExamination: Long,
    val timeOfFirstExamination: String?,
    val motherAlive: Int,
    val dateOfMotherDeath: Long,
    val timeOfMotherDeath: String?,
    val placeOfMotherDeath: Int,
    val otherPlaceOfMotherDeath: String?,
    val motherAnyProblem: String?,
    val babyFirstFed: Int,
    val otherBabyFirstFed: String?,
    val timeBabyFirstFed: String?,
    val howBabyTookFirstFeed: Int,
    val motherHasBreastFeedProblem: Int,
    val motherBreastFeedProblem: String?,
)

data class HbncPartII(
    val dateOfVisit: Long,
    val babyTemperature: String?,
    val babyEyeCondition: Int,
    val babyUmbilicalBleed: Int,
    val actionBabyUmbilicalBleed: Int,
    val babyWeight: String,
    val babyWeightMatchesColor: Int,
    val babyWeightColorOnScale: Int,

    val allLimbsLimp: Int,
    val feedLessStop: Int,
    val cryWeakStop: Int,
    val dryBaby: Int,
    val wrapClothCloseToMother: Int,
    val exclusiveBreastFeeding: Int,
    val cordCleanDry: Int,
    val unusualInBaby: Int,
    val otherUnusualInBaby: String?,

    )

data class HbncHomeVisit(
    val dateOfVisit: Long,
    val babyAlive: Int,
    val numTimesFullMeal24hr: Int,
    val numPadChanged24hr: Int,
    val babyKeptWarmWinter: Int,
    val babyFedProperly: Int,
    val babyCryContinuously: Int,

    val motherTemperature: String?,
    val foulDischargeFever: Int,
    val motherSpeakAbnormallyFits: Int,
    val motherLessNoMilk: Int,
    val motherBreastProblem: Int,

    val babyEyesSwollen: Int,
    val babyWeight: String?,
    val babyTemperature: String?,
    val babyYellow: Int,
    val babyImmunizationStatus: String?,

    val babyReferred: Int,
    val dateOfBabyReferral: Long?,
    val placeOfBabyReferral: Int,
    val otherPlaceOfBabyReferral: String?,
    val motherReferred: Int,
    val dateOfMotherReferral: Long,
    val placeOfMotherReferral: Int,
    val otherPlaceOfMotherReferral: String?,
    val allLimbsLimp: Int,
    val feedingLessStopped: Int,
    val cryWeakStopped: Int,
    val bloatedStomach: Int,
    val coldOnTouch: Int,
    val chestDrawing: Int,
    val breathFast: Int,
    val pusNavel: Int,
    val sup: Int,
    val supName: String?,
    val supComment: String?,
    val supSignDate: Long,
)

data class HBNCPart1Post(
    var id: Long,
    var benId: Long,
    var visitNo: Int,
    var dateOfVisit: String?,
    var babyAlive: Boolean,
    var dateOfBabyDeath: String?,
    var timeOfBabyDeath: String?,
    var placeOfBabyDeath: String,
    var otherPlaceOfBabyDeath: String?,
    var isBabyPreterm: Boolean,
    var gestationalAge: String,
    var dateOfFirstExamination: String?,
    var timeOfFirstExamination: String?,
    var motherAlive: Boolean,
    var dateOfMotherDeath: String?,
    var timeOfMotherDeath: String?,
    var placeOfMotherDeath: String,
    var otherPlaceOfMotherDeath: String?,
    var motherAnyProblem: String?,
    var babyFirstFed: String,
    var otherBabyFirstFed: String?,
    var timeBabyFirstFed: String?,
    var howBabyTookFirstFeed: String,
    var motherHasBreastFeedProblem: Boolean,
    var motherBreastFeedProblem: String?,
    var createdBy: String?,
    var createdDate: String?,
    var updatedDate: String?,
    var updatedBy: String?
) {
    fun toCache(): HbncPartI {
        return HbncPartI(
            dateOfVisit = HelperUtil.getLongFromDateMDY(dateOfVisit),
            babyAlive = if (babyAlive) 1 else 0,
            dateOfBabyDeath = HelperUtil.getLongFromDateMDY(dateOfBabyDeath),
            timeOfBabyDeath = timeOfBabyDeath,
            placeOfBabyDeath = placeOfBabyDeath.toInt(),
            otherPlaceOfBabyDeath = otherPlaceOfBabyDeath,
            isBabyPreterm = if (isBabyPreterm) 1 else 0,
            gestationalAge = gestationalAge.toInt(),
            dateOfFirstExamination = HelperUtil.getLongFromDateMDY(dateOfFirstExamination),
            timeOfFirstExamination = timeOfFirstExamination,
            motherAlive = if (motherAlive) 1 else 0,
            dateOfMotherDeath = HelperUtil.getLongFromDateMDY(dateOfMotherDeath),
            timeOfMotherDeath = timeOfMotherDeath,
            placeOfMotherDeath = placeOfMotherDeath.toInt(),
            otherPlaceOfMotherDeath = otherPlaceOfMotherDeath,
            motherAnyProblem = motherAnyProblem,
            babyFirstFed = babyFirstFed.toInt(),
            otherBabyFirstFed = otherBabyFirstFed,
            timeBabyFirstFed = timeBabyFirstFed,
            howBabyTookFirstFeed = howBabyTookFirstFeed.toInt(),
            motherHasBreastFeedProblem = if (motherHasBreastFeedProblem) 1 else 0,
            motherBreastFeedProblem = motherBreastFeedProblem,
        )
    }
}

data class HBNCPart2Post(
    var id: Long,
    var benId: Long,
    var visitNo: Int,
    var dateOfVisit: String?,
    var babyTemperature: String?,
    var babyEyeCondition: String,
    var babyUmbilicalBleed: Boolean,
    var actionBabyUmbilicalBleed: Boolean,
    var babyWeight: Float?,
    var babyWeightMatchesColor: Boolean,
    var babyWeightColorOnScale: String,
    var allLimbsLimp: Boolean,
    var feedLessStop: Boolean,
    var cryWeakStop: Boolean,
    var dryBaby: Boolean,
    var wrapClothCloseToMother: Boolean,
    var exclusiveBreastFeeding: Boolean,
    var cordCleanDry: Boolean,
    var unusualInBaby: String,
    var otherUnusualInBaby: String?,
    var createdBy: String?,
    var createdDate: String?,
    var updatedDate: String?,
    var updatedBy: String?
) {
    fun toCache(): HbncPartII {
        return HbncPartII(
            dateOfVisit = HelperUtil.getLongFromDateMDY(dateOfVisit),
            babyTemperature = babyTemperature,
            babyEyeCondition = babyEyeCondition.toInt(),
            babyUmbilicalBleed = if (babyUmbilicalBleed) 1 else 0,
            actionBabyUmbilicalBleed = if (actionBabyUmbilicalBleed) 1 else 0,
            babyWeight = babyWeight.toString(),
            babyWeightMatchesColor = if (babyWeightMatchesColor) 1 else 0,
            babyWeightColorOnScale = babyWeightColorOnScale.toInt(),
            allLimbsLimp = if (allLimbsLimp) 1 else 0,
            feedLessStop = if (feedLessStop) 1 else 0,
            cryWeakStop = if (cryWeakStop) 1 else 0,
            dryBaby = if (dryBaby) 1 else 0,
            wrapClothCloseToMother = if (wrapClothCloseToMother) 1 else 0,
            exclusiveBreastFeeding = if (exclusiveBreastFeeding) 1 else 0,
            cordCleanDry = if (cordCleanDry) 1 else 0,
            unusualInBaby = unusualInBaby.toInt(),
            otherUnusualInBaby = otherUnusualInBaby
        )
    }
}

data class HBNCVisitPost(
    var id: Long,
    var benId: Long,
    var visitNo: Int,
    var dateOfVisit: String?,
    var babyAlive: Boolean,
    var numTimesFullMeal24hr: Int,
    var numPadChanged24hr: Int,
    var babyKeptWarmWinter: Boolean,
    var babyFedProperly: Boolean,
    var babyCryContinuously: Boolean,
    var motherTemperature: Int,
    var foulDischargeFever: Boolean,
    var motherSpeakAbnormallyFits: Boolean,
    var motherLessNoMilk: Boolean,
    var motherBreastProblem: Boolean,
    var babyEyesSwollen: Boolean,
    var babyWeight: Float?,
    var babyTemperature: Int,
    var babyYellow: Boolean,
    var babyImmunizationStatus: String?,
    var babyReferred: Boolean,
    var dateOfBabyReferral: String?,
    var placeOfBabyReferral: String,
    var otherPlaceOfBabyReferral: String?,
    var motherReferred: Boolean,
    var dateOfMotherReferral: String?,
    var placeOfMotherReferral: String,
    var otherPlaceOfMotherReferral: String?,
    var allLimbsLimp: Boolean,
    var feedingLessStopped: Boolean,
    var cryWeakStopped: Boolean,
    var bloatedStomach: Boolean,
    var coldOnTouch: Boolean,
    var chestDrawing: Boolean,
    var breathFast: Boolean,
    var pusNavel: String,
    var supervisor: String,
    var supervisorName: String?,
    var supervisorComment: String?,
    var supervisorSignDate: String?,
    var createdBy: String?,
    var createdDate: String?,
    var updatedDate: String?,
    var updatedBy: String?
) {
    fun toCache(): HbncHomeVisit {
        return HbncHomeVisit(
            dateOfVisit = HelperUtil.getLongFromDateMDY(dateOfVisit),
            babyAlive = if (babyAlive) 1 else 0,
            numTimesFullMeal24hr = numTimesFullMeal24hr,
            numPadChanged24hr = numPadChanged24hr,
            babyKeptWarmWinter = if (babyKeptWarmWinter) 1 else 0,
            babyFedProperly = if (babyFedProperly) 1 else 0,
            babyCryContinuously = if (babyCryContinuously) 1 else 0,
            motherTemperature = motherTemperature.toString(),
            foulDischargeFever = if (foulDischargeFever) 1 else 0,
            motherSpeakAbnormallyFits = if (motherSpeakAbnormallyFits) 1 else 0,
            motherLessNoMilk = if (motherLessNoMilk) 1 else 0,
            motherBreastProblem = if (motherBreastProblem) 1 else 0,
            babyEyesSwollen = if (babyEyesSwollen) 1 else 0,
            babyWeight = babyWeight.toString(),
            babyTemperature = babyTemperature.toString(),
            babyYellow = if (babyYellow) 1 else 0,
            babyImmunizationStatus = babyImmunizationStatus,
            babyReferred = if (babyReferred) 1 else 0,
            dateOfBabyReferral = dateOfBabyReferral?.toLong(),
            placeOfBabyReferral = placeOfBabyReferral.toInt(),
            otherPlaceOfBabyReferral = otherPlaceOfBabyReferral,
            motherReferred = if (motherBreastProblem) 1 else 0,
            dateOfMotherReferral = HelperUtil.getLongFromDateMDY(dateOfMotherReferral),
            placeOfMotherReferral = placeOfMotherReferral.toInt(),
            otherPlaceOfMotherReferral = otherPlaceOfMotherReferral,
            allLimbsLimp = if (motherBreastProblem) 1 else 0,
            feedingLessStopped = if (motherBreastProblem) 1 else 0,
            cryWeakStopped = if (motherBreastProblem) 1 else 0,
            bloatedStomach = if (motherBreastProblem) 1 else 0,
            coldOnTouch = if (motherBreastProblem) 1 else 0,
            chestDrawing = if (motherBreastProblem) 1 else 0,
            breathFast = if (motherBreastProblem) 1 else 0,
            pusNavel = pusNavel.toInt(),
            sup = supervisor.toInt(),
            supName = supervisorName,
            supComment = supervisorComment,
            supSignDate = HelperUtil.getLongFromDateMDY(supervisorSignDate)
        )
    }
}

@JsonClass(generateAdapter = true)
data class HBNCVisitCardPost(
    val id: Long,
    val benId: Long,
    val visitNo: Int,
    val ashaName: String?,
    val villageName: String?,
    val subCenterName: String?,
    val blockName: String?,
    val motherName: String?,
    val fatherName: String?,
    val dateOfDelivery: String?,
    val placeOfDelivery: String?,
    val babyGender: String?,
    val typeOfDelivery: String?,
    val stillBirth: Boolean,
    val startedBreastFeeding: String?,
    val dischargeDateMother: String?,
    val dischargeDateBaby: String?,
    val weightInGrams: Int,
    val registrationOfBirth: Boolean,
    val createdBy: String?,
    val createdDate: String?,
    val updatedDate: String?,
    val updatedBy: String?
) {
    fun toCache(): HbncVisitCard {
        return HbncVisitCard(
            ashaName = ashaName,
            villageName = villageName,
            subCenterName = subCenterName,
            blockName = blockName,
            motherName = motherName,
            fatherName = fatherName,
            dateOfDelivery = HelperUtil.getLongFromDateMDY(dateOfDelivery),
            placeOfDelivery = placeOfDelivery?.toInt() ?: 0,
            babyGender = babyGender?.toInt() ?: 0,
            typeOfDelivery = typeOfDelivery?.toInt() ?: 0,
            stillBirth = if (stillBirth) 1 else 0,
            startedBreastFeeding = startedBreastFeeding?.toInt() ?: 0,
            dischargeDateMother = HelperUtil.getLongFromDateMDY(dischargeDateMother),
            dischargeDateBaby = HelperUtil.getLongFromDateMDY(dischargeDateBaby),
            weightInGrams = weightInGrams,
            registrationOfBirth = if (registrationOfBirth) 1 else 0
        )
    }
}

@JsonClass(generateAdapter = true)
data class HBNCPost(
    val id: Int,
    val benId: Long,
    val hhId: Long,
    val homeVisitDate: Int,
    val hbncVisitCardDTO: HBNCVisitCardPost?,
    val hbncPart1DTO: HBNCPart1Post?,
    val hbncPart2DTO: HBNCPart2Post?,
    val hbncVisitDTO: HBNCVisitPost?,
) {
    fun toCache(householdId: Long): HBNCCache {
        var visitCard: HbncVisitCard? = null
        var part1: HbncPartI? = null
        var part2: HbncPartII? = null
        var homeVisitForm: HbncHomeVisit? = null

        hbncVisitCardDTO?.let {
            visitCard = HbncVisitCard(
                ashaName = it.ashaName,
                villageName = it.villageName,
                subCenterName = it.subCenterName,
                blockName = it.blockName,
                motherName = it.motherName,
                fatherName = it.fatherName,
                dateOfDelivery = HelperUtil.getLongFromDateMDY(it.dateOfDelivery),
                placeOfDelivery = if (it.placeOfDelivery != null) it.placeOfDelivery.toInt() else 0,
                babyGender = if (it.babyGender != null) it.babyGender.toInt() else 0,
                typeOfDelivery = if (it.typeOfDelivery != null) it.typeOfDelivery.toInt() else 0,
                stillBirth = if (it.stillBirth) 1 else 0,
                startedBreastFeeding = if (it.startedBreastFeeding != null) it.startedBreastFeeding.toInt() else 0,
                dischargeDateMother = HelperUtil.getLongFromDateMDY(it.dischargeDateMother),
                dischargeDateBaby = HelperUtil.getLongFromDateMDY(it.dischargeDateBaby),
                weightInGrams = it.weightInGrams,
                registrationOfBirth = if (it.registrationOfBirth) 1 else 0
            )
        }

        hbncPart1DTO?.let {
            part1 = HbncPartI(
                dateOfVisit = HelperUtil.getLongFromDateMDY(it.dateOfVisit),
                babyAlive = if (it.babyAlive) 1 else 0,
                dateOfBabyDeath = HelperUtil.getLongFromDateMDY(it.dateOfBabyDeath),
                timeOfBabyDeath = it.timeOfBabyDeath,
                placeOfBabyDeath = it.placeOfBabyDeath.toInt(),
                otherPlaceOfBabyDeath = it.otherPlaceOfBabyDeath,
                isBabyPreterm = if (it.isBabyPreterm) 1 else 0,
                gestationalAge = it.gestationalAge.toInt(),
                dateOfFirstExamination = HelperUtil.getLongFromDateMDY(it.dateOfFirstExamination),
                timeOfFirstExamination = it.timeOfFirstExamination,
                motherAlive = if (it.motherAlive) 1 else 0,
                dateOfMotherDeath = HelperUtil.getLongFromDateMDY(it.dateOfMotherDeath),
                timeOfMotherDeath = it.timeOfMotherDeath,
                placeOfMotherDeath = it.placeOfMotherDeath.toInt(),
                otherPlaceOfMotherDeath = it.otherPlaceOfMotherDeath,
                motherAnyProblem = it.motherAnyProblem,
                babyFirstFed = it.babyFirstFed.toInt(),
                otherBabyFirstFed = it.otherBabyFirstFed,
                timeBabyFirstFed = it.timeBabyFirstFed,
                howBabyTookFirstFeed = it.howBabyTookFirstFeed.toInt(),
                motherHasBreastFeedProblem = if (it.motherHasBreastFeedProblem) 1 else 0,
                motherBreastFeedProblem = it.motherBreastFeedProblem,
            )
        }

        hbncPart2DTO?.let {
            part2 = HbncPartII(
                dateOfVisit = HelperUtil.getLongFromDateMDY(it.dateOfVisit),
                babyTemperature = it.babyTemperature,
                babyEyeCondition = it.babyEyeCondition.toInt(),
                babyUmbilicalBleed = if (it.babyUmbilicalBleed) 1 else 0,
                actionBabyUmbilicalBleed = if (it.actionBabyUmbilicalBleed) 1 else 0,
                babyWeight = it.babyWeight.toString(),
                babyWeightMatchesColor = if (it.babyWeightMatchesColor) 1 else 0,
                babyWeightColorOnScale = it.babyWeightColorOnScale.toInt(),
                allLimbsLimp = if (it.allLimbsLimp) 1 else 0,
                feedLessStop = if (it.feedLessStop) 1 else 0,
                cryWeakStop = if (it.cryWeakStop) 1 else 0,
                dryBaby = if (it.dryBaby) 1 else 0,
                wrapClothCloseToMother = if (it.wrapClothCloseToMother) 1 else 0,
                exclusiveBreastFeeding = if (it.exclusiveBreastFeeding) 1 else 0,
                cordCleanDry = if (it.cordCleanDry) 1 else 0,
                unusualInBaby = it.unusualInBaby.toInt(),
                otherUnusualInBaby = it.otherUnusualInBaby
            )
        }

        hbncVisitDTO?.let {
            homeVisitForm = HbncHomeVisit(
                dateOfVisit = HelperUtil.getLongFromDateMDY(it.dateOfVisit),
                babyAlive = if (it.babyAlive) 1 else 0,
                numTimesFullMeal24hr = it.numTimesFullMeal24hr,
                numPadChanged24hr = it.numPadChanged24hr,
                babyKeptWarmWinter = if (it.babyKeptWarmWinter) 1 else 0,
                babyFedProperly = if (it.babyFedProperly) 1 else 0,
                babyCryContinuously = if (it.babyCryContinuously) 1 else 0,
                motherTemperature = it.motherTemperature.toString(),
                foulDischargeFever = if (it.foulDischargeFever) 1 else 0,
                motherSpeakAbnormallyFits = if (it.motherSpeakAbnormallyFits) 1 else 0,
                motherLessNoMilk = if (it.motherLessNoMilk) 1 else 0,
                motherBreastProblem = if (it.motherBreastProblem) 1 else 0,
                babyEyesSwollen = if (it.babyEyesSwollen) 1 else 0,
                babyWeight = it.babyWeight.toString(),
                babyTemperature = it.babyTemperature.toString(),
                babyYellow = if (it.babyYellow) 1 else 0,
                babyImmunizationStatus = it.babyImmunizationStatus,
                babyReferred = if (it.babyReferred) 1 else 0,
                dateOfBabyReferral = HelperUtil.getLongFromDateMDY(it.dateOfBabyReferral),
                placeOfBabyReferral = it.placeOfBabyReferral.toInt(),
                otherPlaceOfBabyReferral = it.otherPlaceOfBabyReferral,
                motherReferred = if (it.motherBreastProblem) 1 else 0,
                dateOfMotherReferral = HelperUtil.getLongFromDateMDY(it.dateOfMotherReferral),
                placeOfMotherReferral = it.placeOfMotherReferral.toInt(),
                otherPlaceOfMotherReferral = it.otherPlaceOfMotherReferral,
                allLimbsLimp = if (it.motherBreastProblem) 1 else 0,
                feedingLessStopped = if (it.motherBreastProblem) 1 else 0,
                cryWeakStopped = if (it.motherBreastProblem) 1 else 0,
                bloatedStomach = if (it.motherBreastProblem) 1 else 0,
                coldOnTouch = if (it.motherBreastProblem) 1 else 0,
                chestDrawing = if (it.motherBreastProblem) 1 else 0,
                breathFast = if (it.motherBreastProblem) 1 else 0,
                pusNavel = it.pusNavel.toInt(),
                sup = it.supervisor.toInt(),
                supName = it.supervisorName,
                supComment = it.supervisorComment,
                supSignDate = HelperUtil.getLongFromDateMDY(it.supervisorSignDate)
            )
        }

        return HBNCCache(
            id = 0,
            benId = benId,
            hhId = householdId,
            homeVisitDate = homeVisitDate,
            visitCard = visitCard,
            part1 = part1,
            part2 = part2,
            homeVisitForm = homeVisitForm,
            processed = "P",
            syncState = SyncState.SYNCED
        )
    }
}