package org.piramalswasthya.sakhi.repositories

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.filterMdsr
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityRetainedScoped
class RecordsRepo @Inject constructor(
    private val householdDao: HouseholdDao,
    private val benDao: BenDao,
    private val vaccineDao: ImmunizationDao,
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
    val allBenWithoutAbhaList =
        benDao.getAllBenWithoutAbha(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }
    val allBenWithAbhaList =
        benDao.getAllBenWithAbha(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }
    val benWithAbhaListCount = benDao.getAllBenWithAbhaCount(selectedVillage)
    val allBenWithRchList =
        benDao.getAllBenWithRch(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }
    val benWithRchListCount = benDao.getAllBenWithRchCount(selectedVillage)
    fun getBenList() =
        benDao.getAllBen(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    fun getBenListCHO() = benDao.getAllBenGender(selectedVillage, "FEMALE")
        .map { list -> list.map { it.asBasicDomainModelCHO() } }

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
        .map { list -> list.map { it.asBasicDomainModel() } }
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
        .map { list -> list.map { it.asBasicDomainModelForPNC() } }
    val pncMotherListCount = pncMotherList.map { it.size }

    val pncMotherNonFollowUpList = benDao.getAllPNCMotherList(selectedVillage)
        .map { list ->
            list.filter {
                if (!it.savedPncRecords.isNullOrEmpty()) {
                    it.savedPncRecords.last().pncDate != 0L &&
                            it.savedPncRecords.last().pncDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                        90
                    ) &&
                            it.savedPncRecords.last().pncDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                        365
                    )
//                it.savedPncRecords.any { it1 ->
//                    it1.pncDate != 0L &&
//                    it1.pncDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
//                            it1.pncDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
//                }
                } else {
                    false
                }
            }
                .map { it.asBasicDomainModelForPNC() } }

    val pncMotherNonFollowUpListCount = pncMotherNonFollowUpList.map { it.size }

    val cdrList = benDao.getAllCDRList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForCdrForm() } }
//    val cdrListCount = cdrList.map { it.size }

    val mdsrList = benDao.getAllMDSRList(selectedVillage)
        .map { list -> list.filterMdsr() }

    val childrenImmunizationDueListCount = vaccineDao.getChildrenImmunizationDueListCount()

    //    val childrenImmunizationList = benDao.getAllChildrenImmunizationList(selectedVillage)
//        .map { list -> list.map { it.asBasicDomainModel() } }
    val childrenImmunizationList = vaccineDao.getBenWithImmunizationRecords(
        minDob = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.YEAR, -16)
        }.timeInMillis, maxDob = System.currentTimeMillis()
    )
    val childrenImmunizationListCount = childrenImmunizationList.map { it.size }

    val motherImmunizationList = benDao.getAllMotherImmunizationList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val motherImmunizationListCount = motherImmunizationList.map { it.size }

    val eligibleCoupleList = benDao.getAllEligibleRegistrationList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }
    val eligibleCoupleListCount = eligibleCoupleList.map { it.size }

    val eligibleCoupleMissedPeriodList = benDao.getAllEligibleRegistrationList(selectedVillage)
        .map { list ->
            list.filter {
                it.ecr != null && it.ecr.lmpDate != 0L &&
                        System.currentTimeMillis() - it.ecr.lmpDate > TimeUnit.DAYS.toMillis(35)
//                if (it.ecr != null && it.ecr.lmpDate != 0L) {
//                    System.currentTimeMillis() - it.ecr.lmpDate > TimeUnit.DAYS.toMillis(35)
//                } else {
//                    true
//                }
                }
                .map { it.asDomainModel() } }
    val eligibleCoupleMissedPeriodListCount = eligibleCoupleMissedPeriodList.map { it.size }

    val eligibleCoupleTrackingList = benDao.getAllEligibleTrackingList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }

    //        .map { list -> list.map { it.asBenBasicDomainModelECTForm() } }
    val eligibleCoupleTrackingListCount = eligibleCoupleTrackingList.map { it.size }

    val eligibleCoupleTrackingNonFollowUpList = benDao.getAllEligibleTrackingList(selectedVillage)
        .map { list ->
            list.filter {
                if (!it.savedECTRecords.isNullOrEmpty()) {
                    it.savedECTRecords.last().visitDate != 0L &&
                            it.savedECTRecords.last().visitDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                        90
                    ) &&
                            it.savedECTRecords.last().visitDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                        365
                    )
//                it.savedECTRecords.any { it1 ->
//                    it1.visitDate != 0L &&
//                    it1.visitDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
//                            it1.visitDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
//                }
                } else {
                    false
                }
            }
                .map { it.asDomainModel() } }

    val eligibleCoupleTrackingNonFollowUpListCount = eligibleCoupleTrackingNonFollowUpList.map { it.size }

    val eligibleCoupleTrackingMissedPeriodList = benDao.getAllEligibleTrackingList(selectedVillage)
        .map { list ->
            list.filter {
                if (!it.savedECTRecords.isNullOrEmpty() && it.savedECTRecords.last().lmpDate != 0L) {
                    System.currentTimeMillis() - it.savedECTRecords.last().lmpDate > TimeUnit.DAYS.toMillis(35)
                } else {
                    false
                }
//                 it.savedECTRecords.any { record ->
//                     if (record.lmpDate != 0L) {
//                         System.currentTimeMillis() - record.lmpDate > TimeUnit.DAYS.toMillis(35)
//                     } else {
//                         false
//                     }
//                }
            }
                .map { it.asDomainModel() } }
    val eligibleCoupleTrackingMissedPeriodListCount = eligibleCoupleTrackingMissedPeriodList.map { it.size }

//    val deliveredWomenList = benDao.getAllEligibleTrackingList(selectedVillage)
//        .map { list -> list.map { it.asBenBasicDomainModelECTForm() } }
//    val deliveredWomenListCount = deliveredWomenList.map { it.size }

    var hrpPregnantWomenList = benDao.getAllPregnancyWomenForHRList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }
    val hrpPregnantWomenListCount = benDao.getAllPregnancyWomenForHRListCount(selectedVillage)

    var hrpTrackingPregList = benDao.getAllHRPTrackingPregList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }
    val hrpTrackingPregListCount = benDao.getAllHRPTrackingPregListCount(selectedVillage)

//    val hrpTrackingPregHistCount = hrpDao.getHRPTrackHist(ben)

    var hrpNonPregnantWomenList = benDao.getAllNonPregnancyWomenList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }
    val hrpNonPregnantWomenListCount = benDao.getAllNonPregnancyWomenListCount(selectedVillage)

    var hrpTrackingNonPregList = benDao.getAllHRPTrackingNonPregList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }
    val hrpTrackingNonPregListCount = benDao.getAllHRPTrackingNonPregListCount(selectedVillage)


    val lowWeightBabiesCount = benDao.getLowWeightBabiesCount(selectedVillage)

    fun getPregnantWomenList() = benDao.getAllPregnancyWomenList(selectedVillage)
        .map { list -> list.map { it.asPwrDomainModel() } }

    fun getPregnantWomenWithRchList() = benDao.getAllPregnancyWomenWithRchList(selectedVillage)
        .map { list -> list.map { it.asPwrDomainModel() } }

    fun getRegisteredInfants() = childRegistrationDao.getAllRegisteredInfants(selectedVillage)
        .map { it.map { it.asBasicDomainModel() } }

    fun getRegisteredInfantsCount() =
        childRegistrationDao.getAllRegisteredInfantsCount(selectedVillage)

    //        .map { list -> list.map { it.ben } }
    fun getPregnantWomenListCount() = benDao.getAllPregnancyWomenListCount(selectedVillage)
    fun getRegisteredPregnantWomanList() =
        benDao.getAllRegisteredPregnancyWomenList(selectedVillage)
            .map { list ->
                list.filter { !it.savedAncRecords.any { it.maternalDeath == true } }
                    .map { it.asDomainModel() }
            }

    fun getRegisteredPregnantWomanListCount() =
        benDao.getAllRegisteredPregnancyWomenListCount(selectedVillage)

    fun getRegisteredPregnantWomanNonFollowUpList() =
        benDao.getAllRegisteredPregnancyWomenList(selectedVillage)
            .map { list ->
                list.filter {
                    if (!it.savedAncRecords.isNullOrEmpty()) {
                        it.savedAncRecords.last().ancDate != 0L &&
                                it.savedAncRecords.last().ancDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                            90
                        ) &&
                                it.savedAncRecords.last().ancDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                            365
                        )
//                    it.savedAncRecords.any { it1 ->
//                        it1.ancDate != 0L &&
//                        it1.ancDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
//                                it1.ancDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
//                    }
                    } else {
                        false
                    }
                }
                    .map { it.asDomainModel() }
            }

    fun getRegisteredPregnantWomanNonFollowUpListCount() =
        getRegisteredPregnantWomanNonFollowUpList().map { it.size }

    val hrpCases = benDao.getHrpCases(selectedVillage)
        .map { list -> list.distinctBy { it.benId }.map { it.asBasicDomainModel() } }

    fun getDeliveredWomenList() = benDao.getAllDeliveredWomenList(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForDeliveryOutcomeForm() } }

    fun getDeliveredWomenListCount() = benDao.getAllDeliveredWomenListCount(selectedVillage)

    fun getWomenListForPmsma() = benDao.getAllWomenListForPmsma(selectedVillage)
        .map { list -> list.map { it.asBenBasicDomainModelForDeliveryOutcomeForm() } }

    fun getAllWomenForPmsmaCount() = benDao.getAllWomenListForPmsmaCount(selectedVillage)
    fun getListForInfantReg() = benDao.getListForInfantRegister(selectedVillage)
        .map { list -> list.flatMap { it.asBasicDomainModel() } }

    fun getInfantRegisterCount() = benDao.getInfantRegisterCount(selectedVillage)

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