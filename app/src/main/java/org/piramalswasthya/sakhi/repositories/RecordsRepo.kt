package org.piramalswasthya.sakhi.repositories

import androidx.paging.PagingSource
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.HomeVisitUiState
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.filterMdsr
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.HomeVisitHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityRetainedScoped
class RecordsRepo @Inject constructor(
    private val householdDao: HouseholdDao,
    private val benDao: BenDao,
    private val ancHomeVisitDao : FormResponseANCJsonDao,
    private val vaccineDao: ImmunizationDao,
    private val maternalHealthDao: MaternalHealthDao,
    private val childRegistrationDao: ChildRegistrationDao,
    preferenceDao: PreferenceDao
) {
    private val selectedVillage = preferenceDao.getLocationRecord()!!.village.id

    val hhList = householdDao.getAllHouseholdWithNumMembers(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val hhListCount = householdDao.getAllHouseholdsCount(selectedVillage)

    val hhListforAsha = householdDao.getAllHouseholdForAshaFamilyMembers(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }

    val allBenList =
        benDao.getAllBen(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    val childCountsByBen: Flow<Map<Long, Int>> =
        benDao.getChildCountsForAllBen(selectedVillage)
            .map { list -> list.associate { it.benId to it.childCount } }

    fun searchBen(query: String, filterType: Int, source: Int): Flow<List<BenBasicDomain>> =
        benDao.searchBen(selectedVillage, source, filterType, query)
            .map { list -> list.map { it.asBasicDomainModel() } }

    fun searchBenPagedSource(query: String, filterType: Int, source: Int): PagingSource<Int, BenBasicCache> =
        benDao.searchBenPaged(selectedVillage, source, filterType, query)

    suspend fun searchBenOnce(query: String, filterType: Int, source: Int): List<BenBasicDomain> =
        benDao.searchBenOnce(selectedVillage, source, filterType, query)
            .map { it.asBasicDomainModel() }

    val allBenListCount = benDao.getAllBenCount(selectedVillage)
    val allBenWithoutAbhaList =
        benDao.getAllBenWithoutAbha(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }
    val allBenWithAbhaList =
        benDao.getAllBenWithAbha(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    val benWithAbhaListCount = benDao.getAllBenWithAbhaCount(selectedVillage)
    val benWithOldAbhaListCount = benDao.getAllBenWithOldAbhaCount(selectedVillage)
    val benWithNewAbhaListCount = benDao.getAllBenWithNewAbhaCount(selectedVillage)

    val allBenWithRchList =
        benDao.getAllBenWithRch(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    val allBenAboveThirtyList = benDao.getAllBenAboveThirty(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    val allBenWARAList = benDao.getAllBenWARA(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    val benWithRchListCount = benDao.getAllBenWithRchCount(selectedVillage)
    fun getBenList() =
        benDao.getAllBen(selectedVillage).map { list -> list.map { it.asBasicDomainModel() } }

    fun getBenListCHO() = benDao.getAllBenGender(selectedVillage, "FEMALE")
        .map { list -> list.map { it.asBasicDomainModelCHO() } }

    fun getBenListCount() = benDao.getAllBenGenderCount(selectedVillage, "FEMALE")

    val ncdList = allBenList
    val ncdListCount = allBenListCount

    val getNcdEligibleList = benDao.getBenWithCbac(selectedVillage)
    val getNcdrefferedList = benDao.getBenWithReferredCbac(selectedVillage)
    val getHwcRefferedList = benDao.getReferredHWCBenList(selectedVillage)



    val getNcdEligibleListCount = benDao.getBenWithCbacCount(selectedVillage)
    val getNcdrefferedListCount = benDao.getReferredBenCount(selectedVillage)
    val getHwcReferedListCount = benDao.getReferredHWCBenCount(selectedVillage)

    val getNcdPriorityList = getNcdEligibleList.map {
        it.filter { it.savedCbacRecords.isNotEmpty() && it.savedCbacRecords.maxBy { it.createdDate }.total_score > 4 }
    }

    val getNcdPriorityListCount = getNcdPriorityList.map { it.count() }
    val getNcdNonEligibleList = getNcdEligibleList.map {
        it.filter { it.savedCbacRecords.isNotEmpty() && it.savedCbacRecords.maxBy { it.createdDate }.total_score <= 4 }
    }

    val getNcdNonEligibleListCount = getNcdNonEligibleList.map { it.count() }


     fun malariaScreeningList(hhId:Long) = benDao.getAllMalariaScreeningBen(selectedVillage, hhId = hhId)
        .map { list -> list.map { it.asMalariaScreeningDomainModel() } }

    fun aesScreeningList(hhId:Long) = benDao.getAllAESScreeningBen(selectedVillage, hhId = hhId)
        .map { list -> list.map { it.asAESScreeningDomainModel() } }

    fun iRSRoundList(hhId:Long) = benDao.getAllIRSRoundBen(hhId = hhId)
    fun getLastIRSRoundBen(hhId:Long) = benDao.getLastIRSRoundBen(hhId = hhId)


    fun KalazarScreeningList(hhId:Long) = benDao.getAllKALAZARScreeningBen(selectedVillage, hhId = hhId)
        .map { list -> list.map { it.asKALAZARScreeningDomainModel() } }

    fun LeprosyScreeningList(hhId:Long) = benDao.getAllLeprosyScreeningBen(selectedVillage, hhId = hhId)
        .map { list -> list.map { it.asLeprosyScreeningDomainModel() } }

    fun LeprosySuspectedList() = benDao.getLeprosyScreeningBenBySymptoms(selectedVillage,0)
        .map { list -> list.map { it.asLeprosyScreeningDomainModel() } }

    fun LeprosyConfirmedList() = benDao.getConfirmedLeprosyCases(selectedVillage =selectedVillage)
        .map {list -> list.map { it.asLeprosyScreeningDomainModel()}}

    fun filariaScreeningList(hhId:Long) = benDao.getAllFilariaScreeningBen(selectedVillage, hhId = hhId)
        .map { list -> list.map { it.asFilariaScreeningDomainModel() } }


    val tbScreeningList = benDao.getAllTbScreeningBen(selectedVillage)
        .map { list -> list.map { it.asTbScreeningDomainModel() } }
    val tbScreeningListCount = tbScreeningList.map { it.size }


    val tbSuspectedList = benDao.getTbScreeningList(selectedVillage)
        .map { list -> list.map { it.asTbSuspectedDomainModel() } }
    val tbSuspectedListCount = tbSuspectedList.map { it.size }

    val tbConfirmedList = benDao.getTbConfirmedList(selectedVillage)
        .map { list -> list.map { it.asTbSuspectedDomainModel() } }
    val tbConfirmedListCount = tbConfirmedList.map { it.size }




    val malariaConfirmedCasesList = benDao.getMalariaConfirmedCasesList(selectedVillage)
        .map { list -> list.map { it.asMalariaConfirmedDomainModel() } }
    val leprosySuspectedListCount = benDao.getLeprosyScreeningBenCountBySymptoms(selectedVillage,0)
    val leprosyConfirmedCasesListCount = benDao.getConfirmedLeprosyCaseCount(selectedVillage =selectedVillage)


    val malariaConfirmedCasesListCount = malariaConfirmedCasesList.map { it.size }

    val menopauseList = benDao.getAllMenopauseStageList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val menopauseListCount = menopauseList.map { it.size }

    val reproductiveAgeList = benDao.getAllReproductiveAgeList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModelForFpotForm() } }
    val reproductiveAgeListCount = reproductiveAgeList.map { it.size }

    val infantList = benDao.getAllInfantList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val infantListCount = infantList.map { it.size }

    val childList = benDao.getAllChildList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }
    val childListCount = childList.map { it.size }


    val childCard = benDao.getAllInfantList(selectedVillage)
        .map { list -> list.map { it.asBasicDomainModel() } }

    val childFilteredList = benDao.getAllChildList(selectedVillage, 0, 5 * 365)
        .map { list ->
            list
                .filter { !it.isDeath }
                .map { it.asBasicDomainModel() }
        }
    val childFilteredListCount = childFilteredList.map { it.size }

    val adolescentList =
        benDao.getAllAdolescentList(selectedVillage)
            .map { list -> list.map { it.asAdolescentDomainModel() } }
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

    val gdrList = benDao.getAllGeneralDeathsList(selectedVillage)
        .map { list ->list.map{ it.asBenBasicDomainModelForCdrForm()} }

    fun getGeneralDeathCount() = benDao.getAllGeneralDeathsCount(selectedVillage)


    val nmdrList = benDao.getAllNonMaternalDeathsList(selectedVillage)
        .map {list -> list.map{it.asBenBasicDomainModelForCdrForm()}}

    val mdsrList = benDao.getAllMDSRList(selectedVillage)
        .map { list -> list.filterMdsr() }

    val childrenImmunizationDueListCount = vaccineDao.getChildrenImmunizationDueListCount()

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

    var hrpPregnantWomenList = benDao.getAllPregnancyWomenForHRList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }

    val hrpPregnantWomenListCount = benDao.getAllPregnancyWomenForHRListCount(selectedVillage)

    var hrpTrackingPregList = benDao.getAllHRPTrackingPregList(selectedVillage)
        .map { list -> list.map { it.asDomainModel() } }

    val hrpTrackingPregListCount = benDao.getAllHRPTrackingPregListCount(selectedVillage)

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
    fun getAbortionPregnantWomanCount() = benDao.getAllAbortionWomenListCount(selectedVillage)
    fun getHighRiskWomenCount() = benDao.getHighRiskWomenCount(selectedVillage)
    fun getMaternalDeathCount() = benDao.getAllMDSRCount(selectedVillage)
    fun getNonMaternalDeathCount() = benDao.getAllNonMaternalDeathsCount(selectedVillage)
    fun getChildDeathCount() = benDao.getAllCDRListCount(selectedVillage)

    fun getRegisteredPmsmaWomenList() =
        benDao.getAllRegisteredPmsmaWomenList(selectedVillage)
            .map { list ->
                list.map { it.asDomainModel() }
            }
    fun getRegisteredPregnantWomanList() =
        benDao.getAllRegisteredPregnancyWomenList(selectedVillage)
            .map { list ->
                list.filter { !it.savedAncRecords.any { it.maternalDeath == true } }
                    .map { it.asDomainModel() }
            }
    fun getHighRiskPregnantWomanList() =
        benDao.getAllHighRiskPregnancyWomenList(selectedVillage)
            .map { list ->
                list.filter { !it.savedAncRecords.any { it.maternalDeath == true } }
                    .map { it.asDomainModel() }
            }

    fun getAbortionPregnantWomanList(): Flow<List<BenWithAncListDomain>> =
        benDao.getAllAbortionWomenList(selectedVillage)
            .map { benList ->
                benList
                    .filter { woman ->
                        woman.savedAncRecords.any { anc ->
                            anc.isAborted == true && anc.abortionDate != null
                        }
                    }
                    .map { it.asDomainModel() }
            }


    suspend fun getBenById(benId: Long): BenBasicDomain? {
        return benDao.getBenById(benId)?.asBasicDomainModel()
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


    suspend fun getHomeVisitUiState(benId: Long): HomeVisitUiState {
        val formResponses = ancHomeVisitDao.getSyncedVisitsByRchId(benId)

        val visits = HomeVisitHelper.getANCSortedHomeVisits(formResponses)
       // val visits = ancHomeVisitDao.getVisitsForBen(benId)
        val visitCount = visits.size

        val canView = visitCount > 0
        val canAdd = canShowAddButton(visits)

        return HomeVisitUiState(
            canAddHomeVisit = canAdd,
            canViewHomeVisit = canView
        )
    }

    private fun canShowAddButton(visits: List<ANCFormResponseJsonEntity>): Boolean {
        if (visits.size >= 9) return false

        if (visits.isEmpty()) return true

        val lastVisitDateStr = visits.first().visitDate ?: return true
        val lastVisitMillis = HelperUtil.parseDateToMillis(lastVisitDateStr)

        if (lastVisitMillis == 0L) return true

        val diffDays = getDaysDiff(lastVisitMillis, System.currentTimeMillis())
        return diffDays >= 30
    }

    private fun getDaysDiff(from: Long, to: Long): Long {
        val diff = to - from
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}