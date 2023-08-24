package org.piramalswasthya.sakhi.configuration

import android.content.res.Resources
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.Icon
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.child_care.ChildCareFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.communicable_diseases.CdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.HRPNonPregnantFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.HRPPregnantFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.EligibleCoupleFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeFragmentDirections
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

    fun getHomeIconDataset(resources: Resources) : List<Icon> {
        val showAll = preferenceDao.isDevModeEnabled
        Timber.d("currently : $showAll")
        return if (/*showAll*/true) {
            listOf(
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
                    HomeFragmentDirections.actionNavHomeToAllBenFragment(),
                ),
                Icon(
                    R.drawable.ic__eligible_couple,
                    resources.getString(R.string.icon_title_ec),
                    recordsRepo.eligibleCoupleListCount,
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
                    count = recordsRepo.hrpCount,
                    navAction = HomeFragmentDirections.actionNavHomeToHrpCasesFragment(),
                    allowRedBorder = true

                ),
                Icon(
                    R.drawable.ic__general_op,
                    resources.getString(R.string.icon_title_gop),
                    null,
                    HomeFragmentDirections.actionNavHomeToGeneralOpCareFragment(),
                ),
//                Icon(
//                    R.drawable.ic__menopause,
//                    resources.getString(R.string.icon_title_msl),
//                    recordsRepo.menopauseListCount,
//                    HomeFragmentDirections.actionNavHomeToMenopauseStageFragment()
//                ),
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
        } else {
            listOf(
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
                    HomeFragmentDirections.actionNavHomeToAllBenFragment(),
                ),
                Icon(
                    R.drawable.ic__ncd,
                    resources.getString(R.string.icon_title_ncd),
                    null,
                    HomeFragmentDirections.actionNavHomeToNcdFragment(),
                )
            )
        }.apply {
            forEachIndexed { index, icon ->
                icon.colorPrimary = index % 2 == 0
            }
        }
    }

    fun getCHOIconDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__ben,
            resources.getString(R.string.icon_title_ben),
            recordsRepo.getBenListCount(),
            HomeFragmentDirections.actionHomeFragmentToBenListCHOFragment()
        ),
        Icon(
            R.drawable.ic__pregnancy,
            resources.getString(R.string.icon_title_hrp_pregnant),
            recordsRepo.hrpPregnantWomenListCount,
            HomeFragmentDirections.actionHomeFragmentToHRPPregnantFragment()
        ),
        Icon(
            R.drawable.ic__maternal_health,
            resources.getString(R.string.icon_title_hrp_non_pregnant),
            recordsRepo.hrpNonPregnantWomenListCount,
            HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantFragment()
        ),
    )

    fun getHRPPregnantWomenDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_hrp_pregnant_assess),
            recordsRepo.hrpPregnantWomenListCount,
            HRPPregnantFragmentDirections.actionHRPPregnantFragmentToPregnantListFragment()
        ),
        Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_hrp_pregnant_track),
            recordsRepo.hrpTrackingPregListCount,
            HRPPregnantFragmentDirections.actionHRPPregnantFragmentToHRPPregnantListFragment()
        )
    )

    fun getHRPNonPregnantWomenDataset(resources: Resources) = listOf(
        Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_hrp_non_pregnant_assess),
            recordsRepo.hrpNonPregnantWomenListCount,
            HRPNonPregnantFragmentDirections.actionHRPNonPregnantFragmentToNonPregnantListFragment()
        ),
        Icon(
            R.drawable.ic__eligible_couple,
            resources.getString(R.string.icon_title_hrp_non_pregnant_track),
            recordsRepo.hrpTrackingNonPregListCount,
            HRPNonPregnantFragmentDirections.actionHRPNonPregnantFragmentToHRPNonPregnantListFragment()
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
            R.drawable.ic__pregnancy,
            resources.getString(R.string.icon_title_pmr),
            recordsRepo.hrpPregnantWomenListCount,
            MotherCareFragmentDirections.actionMotherCareFragmentToPwRegistrationFragment()
        ),
        Icon(
            R.drawable.ic__pregnancy,
            resources.getString(R.string.icon_title_pmt),
            recordsRepo.getRegisteredPregnantWomanListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToPwAncVisitsFragment()
        ),
        Icon(
            R.drawable.ic__pregnancy,
            resources.getString(R.string.icon_title_pmdo),
            recordsRepo.getDeliveredWomenListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToDeliveryOutcomeListFragment()
        ),
        Icon(
            R.drawable.ic__pregnancy,
            resources.getString(R.string.icon_title_pmir),
            recordsRepo.getDeliveredWomenListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToInfantRegListFragment()
        ),
        Icon(
            R.drawable.ic__pregnancy,
            resources.getString(R.string.icon_title_pmcr),
            recordsRepo.getRegisteredPregnantWomanListCount(),
            MotherCareFragmentDirections.actionMotherCareFragmentToChildRegListFragment()
        ),
//        , Icon(
//            R.drawable.ic__delivery,
//            resources.getString(R.string.icon_title_dmc),
//            recordsRepo.deliveryListCount,
//            MotherCareFragmentDirections.actionMotherCareFragmentToDeliveryStageListFragment()
//        ), Icon(
//            R.drawable.ic__pnc,
//            resources.getString(R.string.icon_title_pncmc),
//            recordsRepo.pncMotherListCount,
//            MotherCareFragmentDirections.actionMotherCareFragmentToPncMotherListFragment()
//        ), Icon(
//            R.drawable.ic__reproductive_age,
//            resources.getString(R.string.icon_title_rmc),
//            recordsRepo.reproductiveAgeListCount,
//            MotherCareFragmentDirections.actionMotherCareFragmentToReproductiveAgeListFragment()
//        )
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
//        Icon(
//            R.drawable.ic__immunization,
//            "Mother Immunization",
//            recordsRepo.motherImmunizationListCount,
//            ImmunizationDueTypeFragmentDirections.actionImmunizationDueTypeFragmentToMotherImmunizationListFragment()
//        ),

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

//    fun getChildImmunizationCategories(resources: Resources) = listOf(
//        Icon(
//            R.drawable.ic__infant,
//            "Birth Dose Vaccines Babies",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.BIRTH
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "6 Weeks Vaccines Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.WEEK_6
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "10 Weeks Vaccines Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.WEEK_10
//            ),
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "14 Weeks Vaccines Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.WEEK_14
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "9-12 Months Vaccines Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.MONTH_9_12
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "16-24 Months Vaccines Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.MONTH_16_24
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "5-6 Years Vaccine Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.YEAR_5_6
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "10 Years Vaccine Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.YEAR_10
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "16 Years Vaccine Children",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.YEAR_16
//            )
//        ),
//        Icon(
//            R.drawable.ic__infant,
//            "Catch-Up Vaccines (Missing Vaccines)",
//            null,
//            ChildImmunizationCategoriesFragmentDirections.actionChildImmunizationFragmentToChildImmunizationListFragment(
//                ChildImmunizationCategory.CATCH_UP
//            )
//        )
//    )

}