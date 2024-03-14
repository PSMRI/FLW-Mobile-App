package org.piramalswasthya.sakhi.model

data class SurveyRegisterCache(
    var facilitatorSupervisor: String? = null,
    var from: String? = null,
    var till: String? = null,
    var firstHouseLandlord: String? = null,
    var fisrtHomeIdentificationSymbol: String? = null,
    var middleHouseLandlord: String? = null,
    var middleHomeIdentificationSymbol: String? = null,
    var lastHouseLandlord: String? = null,
    var lastHomeIdentificationSymbol: String? = null,
    var householdNumber: String? = null,
    var headOfHouse: String? = null,
    var numFamilyMembers: Int = 0,
    var numEligibleCouples: Int = 0,
    var numPregnantWomen: Int = 0,
    var numChildrenLessThanOneMonth: Int = 0,
    var numChildrenLessThanOneYear: Int = 0,
    var numChildrenLessThanFiveYear: Int = 0,
)