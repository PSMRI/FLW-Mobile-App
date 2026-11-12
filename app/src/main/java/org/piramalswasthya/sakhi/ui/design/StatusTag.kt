package org.piramalswasthya.sakhi.ui.design

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import org.piramalswasthya.sakhi.R

/** Redesign: Figma "Tag" tones (24dp, r8, 11/16 Medium). */
enum class DsTagTone(@ColorRes val bg: Int, @ColorRes val fg: Int) {
    SUCCESS(R.color.ds_success_container, R.color.ds_success),
    WARNING(R.color.ds_warning_container, R.color.ds_warning),
    ERROR(R.color.ds_error_container, R.color.ds_error),
    NEUTRAL(R.color.ds_surface_container_high, R.color.ds_on_surface_variant),
    INFO(R.color.ds_primary_subtle, R.color.ds_primary),
}

/** Applies a Figma "Tag" look to any TextView / MaterialButton (bg_ds_tag tinted, coloured label). */
fun TextView.dsTag(tone: DsTagTone) {
    // reset first: a MaterialButton skips a tint it thinks is unchanged (see REDESIGN_PROGRESS §5)
    backgroundTintList = null
    setBackgroundResource(R.drawable.bg_ds_tag)
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, tone.bg))
    setTextColor(ContextCompat.getColor(context, tone.fg))
}

/** Incentive approval status (101 verified · 102 pending · 103 rejected · 104 overdue) → tag tone. */
fun TextView.dsApprovalTag(status: Int?) = dsTag(
    when (status) {
        101 -> DsTagTone.SUCCESS
        103, 104 -> DsTagTone.ERROR
        else -> DsTagTone.WARNING
    }
)
