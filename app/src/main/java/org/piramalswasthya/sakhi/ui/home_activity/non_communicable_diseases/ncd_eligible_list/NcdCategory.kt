package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

import androidx.annotation.StringRes
import org.piramalswasthya.sakhi.R

enum class NcdCategory(@StringRes val labelResId: Int) {
    ALL(R.string.ncd_cat_all),
    SCREENED(R.string.ncd_cat_screened),
    NOT_SCREENED(R.string.ncd_cat_not_screened)
}
