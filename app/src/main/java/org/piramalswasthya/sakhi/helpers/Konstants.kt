package org.piramalswasthya.sakhi.helpers

import java.util.concurrent.TimeUnit

object Konstants {

    val minMillisBwtweenCbacFiling: Long = TimeUnit.DAYS.toMillis(365)
    const val amritTokenTimeoutDuration: Int = 100

    //Dev
    const val devCode = 112


    const val tempBenImagePrefix: String = "tmp_image_file"
    const val tempBenImageSuffix: String = ".jpeg"
    const val editTextHintLimit: Byte = 50
    const val benIdCapacity: Int = 100
    const val benIdWorkerTriggerLimit = 90

    //Ben
    const val micClickIndex = -108

    //Note: min and max both are inclusive for age ranges
    const val minAgeForEligibleCouple: Int = 15
    const val maxAgeForEligibleCouple: Int = 49
    const val minAgeForNcd: Int = 30
    const val minAgeForReproductiveAge: Int = 15
    const val maxAgeForReproductiveAge: Int = 49
    const val maxAgeForInfant: Int = 61
    const val minAgeForChild: Int = 92
    const val maxAgeForChild: Int = 456
    const val minAgeForAdolescent: Int = 10
    const val maxAgeForAdolescent: Int = 14
    const val maxAgeForAdolescentlist: Int = 19
    const val maxAgeForCdr: Int = 14
    const val minAgeForGenBen: Int = 15
    const val maxAgeForGenBen: Int = 99
    const val minAgeForMarriage: Int = 12

    //HBNC
    const val hbncCardDay = 0
    const val hbncPart1Day = -1
    const val hbncPart2Day = -2

    //PW-ANC
    const val minAnc1Week = 5
    const val maxAnc1Week = 12
    const val minAnc2Week = 14
    const val maxAnc2Week = 27
    const val minAnc3Week = 28
    const val maxAnc3Week = 35
    const val minAnc4Week = 36
    const val maxAnc4Week = 40
    const val maxAncWeek = 42

    const val english = "ENGLISH"
    const val minWeekToShowDelivered = 23


    const val babyLowWeight: Double = 2.5


    //PNC-EC cycle
    const val pncEcGap: Long = 45


    const val defaultTimeStamp = 1577817001000L

    const val childHealth = "CHILD HEALTH"

    const val immunization = "IMMUNIZATION"

    const val maternalHealth = "MATERNAL HEALTH"

    const val ashaIncetiveJSY = "ASHA Incentive under JSY"

    const val familyPlanning = "FAMILY PLANNING"

    const val adolescentHealth = "ADOLESCENT HEALTH"

    const val ashaMonthlyRActivity = "ASHA Monthly Routine Activities"

    const val umbrellaProgrames = "Umbrella Programmes"

    const val ncd = "NCD"

    const val other_Incentive = "OTHER INCENTIVES"

    const val additionalIncentivetoAshaSGovt = "ADDITIONAL INCENTIVE TO ASHA UNDER STATE GOVT. BUDGET"

    const val antaraProg = "ASHA incentive for accompanying the client for Injectable MPA(Antara Prog)administration"

}