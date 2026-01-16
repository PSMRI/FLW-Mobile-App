package org.piramalswasthya.sakhi.configuration

import android.content.res.Resources
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.Icon
import org.piramalswasthya.sakhi.repositories.AdolescentHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.SupervisorFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.child_care.ChildCareFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.communicable_diseases.CdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.death_reports.DeathReportsFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.DiseaseControlFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.LeprosyFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.MalariaIconsFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.EligibleCoupleFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.hrp_cases.HrpCasesFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.ImmunizationDueTypeFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.lms.LmsFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.MotherCareFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.NcdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.VillageLevelFormsFragmentDirections
import timber.log.Timber
import javax.inject.Inject

@ActivityRetainedScoped
class IconDataset @Inject constructor(
    private val recordsRepo: RecordsRepo,
    private val preferenceDao: PreferenceDao,
    private val adolescentHealthRepo: AdolescentHealthRepo
) {

    enum class Modules {
        ALL,
        HRP

    }

    enum class Disease {
        MALARIA, KALA_AZAR, AES_JE, FILARIA, LEPROSY, DEWARMING
    }

    fun getHomeIconDataset(resources: Resources): List<Icon> {
        val showAll = preferenceDao.isDevModeEnabled
        val vlfTitle = if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            resources.getString(R.string.mitanin_village_meetings)
        } else {
            resources.getString(R.string.asha_village_meetings)
        }

        Timber.d("currently : $showAll")
        lateinit var showModules:Modules
        if (BuildConfig.FLAVOR.equals("xushrukha", true)) {
            showModules = Modules.HRP
        }else{
            showModules = Modules.ALL
        }
        return when (showModules) {
            Modules.ALL -> listOf(
                Icon(
                    R.drawable.ic__hh,
                    resources.getString(R.string.icon_title_household),
                    recordsRepo.hhListCount,
                    HomeFragmentDirections.actionNavHomeToAllHouseholdFragment()
                ),
                Icon(
                    R.drawable.ic__ben,
                    resources.getString(R.string.icon_title_ben),
                    recordsRepo.allBenListCount,
                    HomeFragmentDirections.actionNavHomeToAllBenFragment(0),
                ),
                Icon(
                    R.drawable.ic__eligible_couple,
                    resources.getString(R.string.icon_title_ec),
                    null,
                    HomeFragmentDirections.actionNavHomeToEligibleCoupleFragment()
                ),
                Icon(
                    R.drawable.ic__maternal_health,
                    resources.getString(R.string.icon_title_mc),
                    null,
                    HomeFragmentDirections.actionNavHomeToMotherCareFragment(),
                ),
                Icon(
                    R.drawable.ic__child_care,
                    resources.getString(R.string.icon_title_cc),
                    null,
                    HomeFragmentDirections.actionNavHomeToChildCareFragment()
                ),
                Icon(
                    R.drawable.ic__ncd,
                    resources.getString(R.string.icon_title_disease),
                    null,
                    HomeFragmentDirections.actionHomeFragmentToDiseaseControlFragment()
                ),
                Icon(
                    R.drawable.ic__ncd,
                    resources.getString(R.string.icon_title_cd),
                    null,
                    HomeFragmentDirections.actionHomeFragmentToCdFragment()
                ),
                Icon(
                    R.drawable.ic_vaccines,
                    resources.getString(R.string.icon_title_imm),
                    null,
                    HomeFragmentDirections.actionNavHomeToImmunizationDueFragment(),
                ),
                Icon(
                    icon = R.drawable.ic__hrp,
                    title = resources.getString(R.string.icon_title_hrp),
//                    count = recordsRepo.hrpCount,
                    count = null,
                    navAction = HomeFragmentDirections.actionNavHomeToHrpCasesFragment(),
                    allowRedBorder = false

                ),
                Icon(
                    R.drawable.ic__general_op,
                    resources.getString(R.string.icon_title_gop),
                    null,
                    HomeFragmentDirections.actionNavHomeToGeneralOpCareFragment(),
                ),
                Icon(
                    R.drawable.ic__death,
                    resources.getString(R.string.icon_title_dr),
                    null,
                    HomeFragmentDirections.actionNavHomeToDeathReportsFragment(),
                ),
                Icon(
                    R.drawable.ic__village_level_form,
                    vlfTitle,
                    null,
                    HomeFragmentDirections.actionNavHomeToVillageLevelFormsFragment()
                ),

            )

            Modules.HRP -> listOf(
                Icon(
                    R.drawable.ic__hh,
                    resources.getString(R.string.icon_title_household),
                    recordsRepo.hhListCount,
                    HomeFragmentDirections.actionNavHomeToAllHouseholdFragment()
                ),
                Icon(
                    R.drawable.ic__ben,
                    resources.getString(R.string.icon_title_ben),
                    recordsRepo.allBenListCount,
                    HomeFragmentDirections.actionNavHomeToAllBenFragment(0),
                ),
                Icon(
                    R.drawable.ic__eligible_couple,
                    resources.getString(R.string.icon_title_ec),
                    null,
                    HomeFragmentDirections.actionNavHomeToEligibleCoupleFragment()
                ),
                Icon(
                    R.drawable.ic__maternal_health,
                    resources.getString(R.string.icon_title_mc),
                    null,
                    HomeFragmentDirections.actionNavHomeToMotherCareFragment(),
                ),
                Icon(
                    icon = R.drawable.ic__hrp,
                    title = resources.getString(R.string.icon_title_hrp),
//                    count = recordsRepo.hrpCount,
                    count = null,
                    navAction = HomeFragmentDirections.actionNavHomeToHrpCasesFragment(),
                    allowRedBorder = false

                )
            )

        }.apply {
            forEachIndexed { index, icon ->
                icon.colorPrimary = index % 2 == 0
            }
        }
    }

    fun getHrpIconsDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__high_risk_preg,
            resources.getString(R.string.icon_title_hrp_pregnant),
            recordsRepo.hrpPregnantWomenListCount,
            HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPPregnantFragment()
        ),
        Icon(
            R.drawable.ic__high_risk_non_prg,
            resources.getString(R.string.icon_title_hrp_non_pregnant),
            recordsRepo.hrpNonPregnantWomenListCount,
            HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPNonPregnantFragment()
        ),
    )

    fun getCHOIconDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__ben,
            resources.getString(R.string.icon_title_ben),
            recordsRepo.getBenListCount(),
            HomeFragmentDirections.actionHomeFragmentToBenListCHOFragment()
        ),
        Icon(
            R.drawable.ic__high_risk_preg,
            resources.getString(R.string.icon_title_hrp_pregnant),
            recordsRepo.hrpPregnantWomenListCount,
            HomeFragmentDirections.actionHomeFragmentToHRPPregnantFragment()
        ),
        Icon(
            R.drawable.ic__high_risk_non_prg,
            resources.getString(R.string.icon_title_hrp_non_pregnant),
            recordsRepo.hrpNonPregnantWomenListCount,
            HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantFragment()
        ),
    )

    fun getSupervisorIconsDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__hh,
            resources.getString(R.string.sup_households),
            recordsRepo.hhListCount,
            SupervisorFragmentDirections.actionNavSupervisorToAllHouseholdFragments()
        ),
        Icon(
            R.drawable.ic__ben,
            resources.getString(R.string.sup_beneficiaries),
            recordsRepo.allBenListCount,
            SupervisorFragmentDirections.actionNavSupervisorToAllBenFragments(0)
        ),
        Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.sup_eligible_couples),
            recordsRepo.eligibleCoupleTrackingListCount,
            SupervisorFragmentDirections.actionNavSupervisorToEligibleCoupleTrackingListFragments()
        ),
        Icon(
            R.drawable.ic__maternal_health,
            resources.getString(R.string.sup_pregnant_women),
            recordsRepo.getPregnantWomenListCount(),
            SupervisorFragmentDirections.actionNavSupervisorToPwRegistrationFragments()
        ),
        Icon(
            R.drawable.ic__anc_visit,
            resources.getString(R.string.sup_anc_visits),
            recordsRepo.getRegisteredPregnantWomanListCount(),
            SupervisorFragmentDirections.actionNavSupervisorToPwAncVisitsFragments()
        ),
        Icon(
            R.drawable.ic__hrp,
            resources.getString(R.string.sup_hrp_woman),
            recordsRepo.hrpPregnantWomenListCount,
            SupervisorFragmentDirections.actionNavSupervisorToPregnantListFragments()
        ),
        Icon(
            R.drawable.ic__delivery_outcome,
            resources.getString(R.string.sup_deliveries),
            recordsRepo.getDeliveredWomenListCount(),
            SupervisorFragmentDirections.actionNavSupervisorToDeliveryOutcomeListFragments()
        ),
        Icon(
            R.drawable.ic__immunization,
            resources.getString(R.string.sup_routine_immunization),
            recordsRepo.childrenImmunizationListCount,
            SupervisorFragmentDirections.actionNavSupervisorToChildImmunizationListFragments()
        ),
        Icon(
            R.drawable.ic__ncd_list,
            resources.getString(R.string.sup_ncd_screened),
            recordsRepo.ncdListCount,
            SupervisorFragmentDirections.actionNavSupervisorToNcdListFragments()
        ),
        Icon(
            R.drawable.ic__ncd_priority,
            resources.getString(R.string.sup_ncd_priority),
            recordsRepo.getNcdPriorityListCount,
            SupervisorFragmentDirections.actionNavSupervisorToNcdPriorityListFragments()
        ),
        Icon(
            R.drawable.ic__death,
            resources.getString(R.string.sup_tb_cases),
            recordsRepo.tbSuspectedListCount,
            SupervisorFragmentDirections.actionNavSupervisorToTBSuspectedListFragments()
        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 3 == 0
        }
    }

    fun getHRPPregnantWomenDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__assess_high_risk,
            resources.getString(R.string.icon_title_hrp_pregnant_assess),
            recordsRepo.hrpPregnantWomenListCount,
            HrpCasesFragmentDirections.actionHrpCasesFragmentToPregnantListFragment()
        ),
        Icon(
            R.drawable.ic__follow_up_hrp,
            resources.getString(R.string.icon_title_hrp_pregnant_track),
            recordsRepo.hrpTrackingPregListCount,
            HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPPregnantListFragment()
        )
    )

    fun getVLFDataset(resources: Resources): List<Icon> {

        val phcReviewIcon = Icon(
            R.drawable.phc_review,
            if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true))
                resources.getString(R.string.cluster_review)
            else
                resources.getString(R.string.phc_review),
            null,
            VillageLevelFormsFragmentDirections
                .actionVillageLevelFormsFragmentToPHCReviewListFragement()
        )

        return listOf(
            Icon(
                R.drawable.ic__assess_high_risk,
                resources.getString(R.string.vhnd),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToVHNDListFragement()
            ),
            Icon(
                R.drawable.vhnc,
                resources.getString(R.string.vnhc),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToVHNCListFragement()
            ),
            phcReviewIcon,
            Icon(
                R.drawable.ahd,
                resources.getString(R.string.ahd),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToAHDListFragment()
            ),
            Icon(
                R.drawable.dewarming,
                resources.getString(R.string.national_deworming_day),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToDewormingListFragment()
            ),
            Icon(
                R.drawable.dewarming,
                resources.getString(R.string.maa_meeting),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToAllMaaMeetingFragment()
            ),
            Icon(
                R.drawable.dewarming,
                resources.getString(R.string.u_win_session),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToUwinListFragment()
            ),
            Icon(
                R.drawable.dewarming,
                resources.getString(R.string.pulse_polio_campaign),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToPulsePolioCampaignListFragment()
            ),
            Icon(
                R.drawable.dewarming,
                resources.getString(R.string.ors_distribution_campaign),
                null,
                VillageLevelFormsFragmentDirections
                    .actionVillageLevelFormsFragmentToORSCampaignListFragment()
            )
        )
    }


    fun getHRPNonPregnantWomenDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__assess_high_risk,
            resources.getString(R.string.icon_title_hrp_non_pregnant_assess),
            recordsRepo.hrpNonPregnantWomenListCount,
            HrpCasesFragmentDirections.actionHrpCasesFragmentToNonPregnantListFragment()
        ),
        Icon(
            R.drawable.ic__follow_up_high_risk_non_preg,
            resources.getString(R.string.icon_title_hrp_non_pregnant_track),
            recordsRepo.hrpTrackingNonPregListCount,
            HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPNonPregnantListFragment()
        )
    )

    fun getChildCareDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__infant,
            resources.getString(R.string.icon_title_icc),
            recordsRepo.infantListCount,
            ChildCareFragmentDirections.actionChildCareFragmentToInfantListFragment()
        ), Icon(
            R.drawable.ic__child,
            resources.getString(R.string.icon_title_ccc),
            recordsRepo.childListCount,
            ChildCareFragmentDirections.actionChildCareFragmentToChildListFragment()
        ), Icon(
            R.drawable.ic__adolescent,
            resources.getString(R.string.icon_title_acc),
            recordsRepo.adolescentListCount,
            ChildCareFragmentDirections.actionChildCareFragmentToAdolescentListFragment()
        ),
        Icon(
            R.drawable.ic__adolescent,
            resources.getString(R.string.children_under_five_years),
            recordsRepo.childFilteredListCount,
            ChildCareFragmentDirections.actionChildCareFragmentToChildrenUnderFiveYearListFragment()
        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getLmsDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic_guide_icon,
            resources.getString(R.string.icon_title_user_guid),
            null,
            EligibleCoupleFragmentDirections.actionEligibleCoupleFragmentToEligibleCoupleListFragment()
        ), Icon(
            R.drawable.ic_video_icon,
            resources.getString(R.string.icon_title_video_tutorial),
            null,
            LmsFragmentDirections.actionLmsFragmentToVideoTutorialFragmet()
        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getEligibleCoupleDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_ecr),
            recordsRepo.eligibleCoupleListCount,
            EligibleCoupleFragmentDirections.actionEligibleCoupleFragmentToEligibleCoupleListFragment()
        ), Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_ect),
            recordsRepo.eligibleCoupleTrackingListCount,
            EligibleCoupleFragmentDirections.actionEligibleCoupleFragmentToEligibleCoupleTrackingListFragment()
        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }


    fun getLeprosyDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.leprocy,
            resources.getString(R.string.leprosy_screening),
            recordsRepo.tbScreeningListCount,
            LeprosyFragmentDirections.actionLeprosyFragmentToAllHouseHoldDiseaseControlFragment(
                Disease.LEPROSY.toString()
            )
        ),
        Icon(
            R.drawable.leprocy,
            resources.getString(R.string.leprosy_suspected),
             recordsRepo.leprosySuspectedListCount,
            LeprosyFragmentDirections.actionLeprosyFragmenToLeprosySuspectedListFragment()
        ),
        Icon(
            R.drawable.leprocy,
            resources.getString(R.string.leprosy_confirmed),
            recordsRepo.leprosyConfirmedCasesListCount,
            LeprosyFragmentDirections.actionLeprosyDragmentToLeprosyConfirmedListFragment()
        ),

    )


    fun getDiseaseControlDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__ncd,
            resources.getString(R.string.icon_title_ncd),
            null,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToNcdFragment(),

        ),
        Icon(
            R.drawable.maleria,
            resources.getString(R.string.icon_title_maleria),
            recordsRepo.tbScreeningListCount,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToMalariaIconsFragment(

            )
        ), Icon(
            R.drawable.kala,
            resources.getString(R.string.icon_title_ka),
            recordsRepo.tbScreeningListCount,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToAllHouseHoldDiseaseControlFragment(
                Disease.KALA_AZAR.toString()
            )
        ),

        Icon(
            R.drawable.aes,
            resources.getString(R.string.icon_title_aes),
            recordsRepo.tbScreeningListCount,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToAllHouseHoldDiseaseControlFragment(
                Disease.AES_JE.toString()
            )
        ),
        Icon(
            R.drawable.filaria,
            resources.getString(R.string.icon_title_filaria),
            recordsRepo.tbScreeningListCount,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToAllHouseHoldDiseaseControlFragment(
                Disease.FILARIA.toString()
            )
        ),
        Icon(
            R.drawable.leprocy,
            resources.getString(R.string.icon_title_leprosy),
            recordsRepo.tbScreeningListCount,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToLeprosyFragment()
        ),
        /*Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_dearming),
            recordsRepo.eligibleCoupleTrackingListCount,
            DiseaseControlFragmentDirections.actionDiseaseControlFragmentToAllHouseHoldDiseaseControlFragment(
                Disease.DEWARMING.toString()
            )
        )*/
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getDeathReportDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__death,
            resources.getString(R.string.general_deaths),
            recordsRepo.getGeneralDeathCount(),
            DeathReportsFragmentDirections.actionDeathReportsFragmentToGdrListFragment()
        ),
        Icon(
            R.drawable.ic__death,
            resources.getString(R.string.maternal_deaths),
            recordsRepo.getMaternalDeathCount(),
            DeathReportsFragmentDirections.actionDeathReportsFragmentToMdsrListFragment()
        ),
        Icon(
            R.drawable.ic__death,
            resources.getString(R.string.non_maternal_deaths),
            recordsRepo.getNonMaternalDeathCount(),
            DeathReportsFragmentDirections.actionDeathReportsFragmentToNmdsrListFragment()
        ),
        Icon(
            R.drawable.ic__death,
            resources.getString(R.string.child_deaths),
            recordsRepo.getChildDeathCount(),
            DeathReportsFragmentDirections.actionDeathReportsFragmentToCdrListFragment()
        )
    )

    fun getMotherCareDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__pwr,
            resources.getString(R.string.icon_title_pmr),
            recordsRepo.getPregnantWomenListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToPwRegistrationFragment()
        ),
        Icon(
            R.drawable.ic__anc_visit,
            resources.getString(R.string.icon_title_pmt),
            recordsRepo.getRegisteredPregnantWomanListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToPwAncVisitsFragment()
        ),
        Icon(
            R.drawable.ic__delivery_outcome,
            resources.getString(R.string.icon_title_pmdo),
            recordsRepo.getDeliveredWomenListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToDeliveryOutcomeListFragment()
        ),
        Icon(
            R.drawable.ic__mother,
            resources.getString(R.string.icon_title_pncmc),
            recordsRepo.pncMotherListCount,
            MotherCareFragmentDirections.actionMotherCareFragmentToPncMotherListFragment()
        ),
        Icon(
            R.drawable.ic__infant_registration,
            resources.getString(R.string.icon_title_pmir),
            recordsRepo.getInfantRegisterCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToInfantRegListFragment()
        ),
        Icon(
            R.drawable.ic__child_registration,
            resources.getString(R.string.icon_title_pmcr),
            recordsRepo.getRegisteredInfantsCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToChildRegListFragment()
        ),
        Icon(
            R.drawable.ic__child_registration,
            resources.getString(R.string.icon_title_abortion),
            recordsRepo.getAbortionPregnantWomanCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToAbortionListFragment()
        ),
        Icon(
            R.drawable.ic__child_registration,
            resources.getString(R.string.icon_title_pmsma),
            recordsRepo.getHighRiskWomenCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToPmsmaHighRiskListFragment()
        ),
        Icon(
            R.drawable.ic_ncd_noneligible,
            resources.getString(R.string.hwc_referred_list),
            recordsRepo.getHwcReferedListCount,
            MotherCareFragmentDirections.actionMotherCareFragmentToHwcReferredListFragment()
        ),
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getNCDDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__ncd_list,
            resources.getString(R.string.icon_title_ncd_list),
            recordsRepo.ncdListCount,
            NcdFragmentDirections.actionNcdFragmentToNcdListFragment()
        ),
        Icon(
            R.drawable.ic__ncd_eligibility,
            resources.getString(R.string.icon_title_ncd_eligible_list),
            recordsRepo.getNcdEligibleListCount,
            NcdFragmentDirections.actionNcdFragmentToNcdEligibleListFragment()
        ),
        Icon(
            R.drawable.ic__ncd_priority,
            resources.getString(R.string.icon_title_ncd_priority_list),
            recordsRepo.getNcdPriorityListCount,
            NcdFragmentDirections.actionNcdFragmentToNcdPriorityListFragment()
        ),
        Icon(
            R.drawable.ic_ncd_noneligible,
            resources.getString(R.string.icon_title_ncd_non_eligible_list),
            recordsRepo.getNcdNonEligibleListCount,
            NcdFragmentDirections.actionNcdFragmentToNcdNonEligibleListFragment()
        ),
        Icon(
            R.drawable.ic_ncd_noneligible,
            resources.getString(R.string.ncd_refer_list),
            recordsRepo.getNcdrefferedListCount,
            NcdFragmentDirections.actionNcdFragmentToNcdReferredListFragment()
        ),
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getImmunizationDataset() = listOf(
        Icon(
            R.drawable.ic_vaccines,
            "Child Immunization",
            recordsRepo.childrenImmunizationListCount,
            ImmunizationDueTypeFragmentDirections.actionImmunizationDueTypeFragmentToChildImmunizationListFragment()
        ),

        ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getVillageLevelFormsDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic_person,
            resources.getString(R.string.icon_title_sr),
            null,
            VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSurveyRegisterFragment()
        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getCDDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__ncd_eligibility,
            resources.getString(R.string.icon_title_ncd_tb_screening),
            recordsRepo.tbScreeningListCount,
            CdFragmentDirections.actionCdFragmentToTBScreeningListFragment()
        ), Icon(
            R.drawable.ic__death,
            resources.getString(R.string.icon_title_ncd_tb_suspected),
            recordsRepo.tbSuspectedListCount,
            CdFragmentDirections.actionCdFragmentToTBSuspectedListFragment()

        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getMalariaDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.malaria_list,
            resources.getString(R.string.icon_title_maleria),
            recordsRepo.tbScreeningListCount,
            MalariaIconsFragmentDirections.actionMalariaIconsFragmentToAllHouseHoldDiseaseControlFragment(Disease.MALARIA.toString())
        ), Icon(
            R.drawable.confirmed,
            resources.getString(R.string.icon_title_malaria_confirmed),
            recordsRepo.malariaConfirmedCasesListCount,
            MalariaIconsFragmentDirections.actionMalariaIconsFragmentToConfirmedMalariaLIstFragment()

        )
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

}