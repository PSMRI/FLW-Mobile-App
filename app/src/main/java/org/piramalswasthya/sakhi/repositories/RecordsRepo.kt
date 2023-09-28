package org.piramalswasthya.sakhi.repositories

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import javax.inject.Inject

@ActivityRetainedScoped
class RecordsRepo @Inject constructor(
    private val householdDao: HouseholdDao,
    private val benDao: BenDao,
    private val maternalHealthDao: MaternalHealthDao,
    private val childRegistrationDao: ChildRegistrationDao,
    preferenceDao: PreferenceDao
) {
    private val selectedVillage = preferenceDao.getLocationRecord()!!.village.id

    val hhList = householdDao.getAllHouseholdWithNumMembers(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val hhListCount = householdDao.getAllHouseholdsCount(selectedVillage)

    val allBenList =
        benDao.getAllBen(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }
    val allBenListCount = benDao.getAllBenCount(selectedVillage)
    fun getBenList() = benDao.getAllBen(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }
    fun getBenListCHO() = benDao.getAllBenGender(selectedVillage, "FEMALE").map { list -> list.map { it.asBasicDomainModelCHO() } }
    fun getBenListCount() = benDao.getAllBenGenderCount(selectedVillage, "FEMALE")

//    val pregnantList = benDao.getAllPregnancyWomenList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelForPmsmaForm() } }
//    val pregnantListCount = pregnantList.map { it.size }

    val ncdList = allBenList
    val ncdListCount = allBenListCount

    val getNcdEligibleList = benDao.getBenWithCbac(selectedVillage)
    val getNcdEligibleListCount = benDao.getBenWithCbacCount(selectedVillage)
    val getNcdPriorityList = getNcdEligibleList.map {
        it.filter { it.savedCbacRecords.isNotEmpty() && it.savedCbacRecords.maxBy { it.createdDate }.total_score > 4 }
    }

    val getNcdPriorityListCount = getNcdPriorityList.map { it.count() }
    val getNcdNonEligibleList = getNcdEligibleList.map {
        it.filter { it.savedCbacRecords.isNotEmpty() && it.savedCbacRecords.maxBy { it.createdDate }.total_score <= 4 }
    }

    val getNcdNonEligibleListCount = getNcdNonEligibleList.map { it.count() }

//    val ncdPriorityList = benDao.getAllNCDPriorityList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelForCbacForm() } }
//    val ncdPriorityListCount = ncdPriorityList.map { it.size }

//    val ncdNonEligibleList = benDao.getAllNCDNonEligibleList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelForCbacForm() } }
//    val ncdNonEligibleListCount = ncdNonEligibleList.map { it.size }

    val tbScreeningList = benDao.getAllTbScreeningBen(selectedVillage)
        .map { list -> list.map { it.asTbScreeningDomainModel() } }
    val tbScreeningListCount = tbScreeningList.map { it.size }

    val tbSuspectedList = benDao.getTbScreeningList(selectedVillage)
        .map { list -> list.map { it.asTbSuspectedDomainModel() } }
    val tbSuspectedListCount = tbSuspectedList.map { it.size }

    val menopauseList = benDao.getAllMenopauseStageList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val menopauseListCount = menopauseList.map { it.size }

    val reproductiveAgeList = benDao.getAllReproductiveAgeList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModelForFpotForm() } }
    val reproductiveAgeListCount = reproductiveAgeList.map { it.size }

//    val infantList = benDao.getAllInfantList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelForHbncForm() } }
//    val infantListCount = infantList.map { it.size }
    val infantList = benDao.getAllInfantList(selectedVillage)
    .map { list -> list.map { it.asBasicDomainModel()} }
    val infantListCount = infantList.map { it.size }

//    val childList = benDao.getAllChildList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelForHbycForm() } }
//    val childListCount = childList.map { it.size }
    val childList = benDao.getAllChildList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val childListCount = childList.map { it.size }

    val adolescentList =
        benDao.getAllAdolescentList(selectedVillage)
            .map { list -> list.map { it.asBasicDomainModel() } }
    val adolescentListCount = adolescentList.map { it.size }

    val immunizationList = benDao.getAllImmunizationDueList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val immunizationListCount = menopauseList.map { it.size }

    val hrpList =
        benDao.getAllHrpCasesList(selectedVillage)
            .map { list -> list.map { it.asBasicDomainModel() } }
    val hrpListCount = menopauseList.map { it.size }

    val pncMotherList = benDao.getAllPNCMotherList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModelForPmjayForm() } }
    val pncMotherListCount = pncMotherList.map { it.size }

    val cdrList = benDao.getAllCDRList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForCdrForm() } }
//    val cdrListCount = cdrList.map { it.size }

    val mdsrList = benDao.getAllMDSRList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForMdsrForm() } }

    val childrenImmunizationList = benDao.getAllChildrenImmunizationList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val childrenImmunizationListCount = childrenImmunizationList.map { it.size }

    val motherImmunizationList = benDao.getAllMotherImmunizationList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val motherImmunizationListCount = motherImmunizationList.map { it.size }

    val eligibleCoupleList = benDao.getAllEligibleRegistrationList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }
    val eligibleCoupleListCount = eligibleCoupleList.map { it.size }

    val eligibleCoupleTrackingList = benDao.getAllEligibleTrackingList(selectedVillage).map { list -> list.map { it.asDomainModel() } }
//        .map { list -> list.map { it.asBenBasicDomainModelECTForm() } }
    val eligibleCoupleTrackingListCount = eligibleCoupleTrackingList.map { it.size }

//    val deliveredWomenList = benDao.getAllEligibleTrackingList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelECTForm() } }
//    val deliveredWomenListCount = deliveredWomenList.map { it.size }

    var hrpPregnantWomenList = benDao.getAllPregnancyWomenList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForHRPPregAssessmentForm() } }
    val hrpPregnantWomenListCount = benDao.getAllPregnancyWomenListCount(selectedVillage)

    var hrpTrackingPregList = benDao.getAllHRPTrackingPregList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForHRPPregTrackForm() } }
    val hrpTrackingPregListCount = benDao.getAllHRPTrackingPregListCount(selectedVillage)

//    val hrpTrackingPregHistCount = hrpDao.getHRPTrackHist(ben)

    var hrpNonPregnantWomenList = benDao.getAllNonPregnancyWomenList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForHRPNonPregAssessmentForm() } }
    val hrpNonPregnantWomenListCount = benDao.getAllNonPregnancyWomenListCount(selectedVillage)

    var hrpTrackingNonPregList = benDao.getAllHRPTrackingNonPregList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForHRPNonPregTrackForm() } }
    val hrpTrackingNonPregListCount = benDao.getAllHRPTrackingNonPregListCount(selectedVillage)

    fun getPregnantWomenList() = benDao.getAllPregnancyWomenList(selectedVillage)
        .map { list -> list.map { it.asPwrDomainModel() } }

    fun getRegisteredInfants() = childRegistrationDao.getAllRegisteredInfants(selectedVillage)

    fun getRegisteredInfantsCount() = childRegistrationDao.getAllRegisteredInfantsCount(selectedVillage)

    //        .map { list -> list.map { it.ben } }
    fun getPregnantWomenListCount() = benDao.getAllPregnancyWomenListCount(selectedVillage)
    fun getRegisteredPregnantWomanList() =
        benDao.getAllRegisteredPregnancyWomenList(selectedVillage)
            .map { list -> list.map { it.asDomainModel() } }

    fun getRegisteredPregnantWomanListCount() =
        benDao.getAllRegisteredPregnancyWomenListCount(selectedVillage)

    val hrpCases = benDao.getHrpCases(selectedVillage)
        .map { list -> list.distinctBy { it.benId }.map { it.asBasicDomainModel() } }

    fun getDeliveredWomenList() = benDao.getAllDeliveredWomenList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForDeliveryOutcomeForm() } }

    fun getDeliveredWomenListCount() = benDao.getAllDeliveredWomenListCount(selectedVillage)

    fun getWomenListForPmsma() = benDao.getAllWomenListForPmsma(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForDeliveryOutcomeForm() } }

    fun getAllWomenForPmsmaCount() = benDao.getAllWomenListForPmsmaCount(selectedVillage)
    fun getListForInfantReg() = benDao.getListForInfantRegister(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForInfantRegistrationForm() } }
    fun getInfantRegisterCount() = benDao.getInfantRegisterCount(selectedVillage)

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val hrpCount = maternalHealthDao.getAllPregnancyRecords().transformLatest {
//        var count = 0
//        it.map {
//            val regis = it.key
//            val anc = it.value
//            if (regis.isHrp || anc.any { it.hrpConfirmed == true })
//                count++
//        }
//        emit(count)
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val hrpCount = maternalHealthDao.getAllPregnancyAssessRecords().transformLatest { it ->
        var count = 0
        it.map { it1 ->
            if (it1.isHighRisk)
                count++
        }
        emit(count)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val hrpNonPCount = maternalHealthDao.getAllNonPregnancyAssessRecords().transformLatest { it ->
        var count = 0
        it.map { it1 ->
            if (it1.isHighRisk)
                count++
        }
        emit(count)
    }
    fun getHRECCount() = maternalHealthDao.getAllECRecords()
}