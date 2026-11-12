package org.piramalswasthya.sakhi.ui.design

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.piramalswasthya.sakhi.R

/**
 * Redesign filter chip (NCD / referral type / birth-dose / risk-factor filters): selected = filled primary pill with
 * white text, not selected = outlined pill with dark text. Replaces tab_selection / tab_unselection, whose white text
 * only worked on the old grey/blue boxes.
 */
fun dsFilterChip(chip: View, label: TextView, selected: Boolean) {
    chip.setBackgroundResource(if (selected) R.drawable.bg_ds_filter_chip_on else R.drawable.bg_ds_filter_chip_off)
    label.setTextColor(ContextCompat.getColor(chip.context, if (selected) R.color.ds_on_primary else R.color.ds_on_surface_variant))
}
