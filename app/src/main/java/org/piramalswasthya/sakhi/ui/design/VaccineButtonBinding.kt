package org.piramalswasthya.sakhi.ui.design

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.VaccineState

/**
 * Redesign vaccine row button — same rules as BindingUtils `Button.setVaccineState` (text / visibility), plus the
 * design-system colours: due / overdue = filled primary "Fill", done = tonal "View", missed / unavailable = hidden.
 */
@BindingAdapter("dsVaccineState")
fun MaterialButton.setDsVaccineState(state: VaccineState?) {
    state ?: return
    visibility = View.VISIBLE
    val filled = when (state) {
        VaccineState.PENDING, VaccineState.OVERDUE -> { text = resources.getString(R.string.ds_vaccine_fill); true }
        VaccineState.DONE -> { text = resources.getString(R.string.ds_view); false }
        VaccineState.MISSED, VaccineState.UNAVAILABLE -> { visibility = View.GONE; return }
    }
    backgroundTintList = null
    backgroundTintList = ContextCompat.getColorStateList(context, if (filled) R.color.ds_filled_bg else R.color.ds_tonal_bg)
    setTextColor(ContextCompat.getColorStateList(context, if (filled) R.color.ds_filled_fg else R.color.ds_tonal_fg))
}
