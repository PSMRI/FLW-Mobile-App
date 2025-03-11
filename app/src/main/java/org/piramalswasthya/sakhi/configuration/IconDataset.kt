package org.piramalswasthya.sakhi.configuration

import android.content.res.Resources
import android.view.WindowManager
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.Icon
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.child_care.ChildCareFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.communicable_diseases.CdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.EligibleCoupleFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.hrp_cases.HrpCasesFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.ImmunizationDueTypeFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.MotherCareFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.NcdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.VillageLevelFormsFragmentDirections
import timber.log.Timber
import javax.inject.Inject

@ActivityRetainedScoped
class IconDataset @Inject constructor(
    private val recordsRepo: RecordsRepo,
    private val preferenceDao: PreferenceDao
) {

    enum class Modules {
        ALL,
        HRP
    }

    fun getHomeIconDataset(resources: Resources): List<Icon> {
        val showAll = preferenceDao.isDevModeEnabled
        Timber.d("currently : $showAll")
        lateinit var showModules:Modules
        if (BuildConfig.FLAVOR.equals("xushrukhaProd", true)) {
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
                    resources.getString(R.string.icon_title_ncd),
                    null,
                    HomeFragmentDirections.actionNavHomeToNcdFragment(),
                ),
                Icon(
                    R.drawable.ic__ncd,
                    resources.getString(R.string.icon_title_cd),
                    null,
                    HomeFragmentDirections.actionHomeFragmentToCdFragment()
                ),
                Icon(
                    R.drawable.ic__immunization,
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
                    resources.getString(R.string.icon_title_vlf),
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
    ).apply {
        forEachIndexed { index, icon ->
            icon.colorPrimary = index % 2 == 0
        }
    }

    fun getImmunizationDataset() = listOf(
        Icon(
            R.drawable.ic__immunization,
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

}