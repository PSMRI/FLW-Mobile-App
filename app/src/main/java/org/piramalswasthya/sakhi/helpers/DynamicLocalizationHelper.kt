package org.piramalswasthya.sakhi.helpers

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.VaccineType

object DynamicLocalizationHelper {


     fun ChildImmunizationCategory.toLocalizedString(ctx: Context): String {
        return ctx.getString(when (this) {
            ChildImmunizationCategory.BIRTH -> R.string.imm_cat_birth_dose
            ChildImmunizationCategory.WEEK_6 -> R.string.imm_cat_6_weeks
            ChildImmunizationCategory.WEEK_10 -> R.string.imm_cat_10_weeks
            ChildImmunizationCategory.WEEK_14 -> R.string.imm_cat_14_weeks
            ChildImmunizationCategory.MONTH_9_12 -> R.string.imm_cat_9_12_months
            ChildImmunizationCategory.MONTH_16_24 -> R.string.imm_cat_16_24_months
            ChildImmunizationCategory.YEAR_5_6 -> R.string.imm_cat_5_6_years
            ChildImmunizationCategory.YEAR_10 -> R.string.imm_cat_10_years
            ChildImmunizationCategory.YEAR_16 -> R.string.imm_cat_16_years
            ChildImmunizationCategory.CATCH_UP -> R.string.imm_cat_catch_up
        })
    }

    fun VaccineType.toLocalizedString(ctx: Context): String {
        return ctx.getString(
            when (this) {
                VaccineType.BCG -> R.string.vac_bcg
                VaccineType.HEPB_BIRTH -> R.string.vac_hepb_birth
                VaccineType.OPV_0 -> R.string.vac_opv_0

                VaccineType.PENTA_1 -> R.string.vac_penta_1
                VaccineType.OPV_1 -> R.string.vac_opv_1
                VaccineType.RVV_1 -> R.string.vac_rvv_1
                VaccineType.FIPV_1 -> R.string.vac_fipv_1

                VaccineType.PENTA_2 -> R.string.vac_penta_2
                VaccineType.OPV_2 -> R.string.vac_opv_2
                VaccineType.RVV_2 -> R.string.vac_rvv_2

                VaccineType.PENTA_3 -> R.string.vac_penta_3
                VaccineType.OPV_3 -> R.string.vac_opv_3
                VaccineType.RVV_3 -> R.string.vac_rvv_3
                VaccineType.FIPV_2 -> R.string.vac_fipv_2

                VaccineType.MR_1 -> R.string.vac_mr_1
                VaccineType.JE_1 -> R.string.vac_je_1
                VaccineType.VIT_A_1 -> R.string.vac_vit_a_1

                VaccineType.DPT_BOOSTER_1 -> R.string.vac_dpt_booster_1
                VaccineType.MR_2 -> R.string.vac_mr_2
                VaccineType.JE_2 -> R.string.vac_je_2
                VaccineType.OPV_BOOSTER -> R.string.vac_opv_booster
                VaccineType.VIT_A_2 -> R.string.vac_vit_a_2

                VaccineType.DPT_BOOSTER_2 -> R.string.vac_dpt_booster_2

                VaccineType.TD -> R.string.vac_td_1

                VaccineType.VIT_K -> R.string.vac_vit_k

                VaccineType.VIT_A_3 -> R.string.vac_vit_a_3
                VaccineType.VIT_A_4 -> R.string.vac_vit_a_4
                VaccineType.VIT_A_5 -> R.string.vac_vit_a_5
                VaccineType.VIT_A_6 -> R.string.vac_vit_a_6
                VaccineType.VIT_A_7 -> R.string.vac_vit_a_7
                VaccineType.VIT_A_8 -> R.string.vac_vit_a_8

                VaccineType.PCV_1 -> R.string.vac_pcv_1
                VaccineType.PCV_2 -> R.string.vac_pcv_2
                VaccineType.PCV_BOOSTER -> R.string.vac_pcv_booster

                VaccineType.UNKNOWN -> R.string.unknown // fallback

            }
        )
    }




}