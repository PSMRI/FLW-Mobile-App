package org.piramalswasthya.sakhi.ui.design

import android.util.TypedValue
import android.view.Gravity
import android.widget.RadioButton
import androidx.core.widget.TextViewCompat
import androidx.appcompat.content.res.AppCompatResources
import org.piramalswasthya.sakhi.R

/**
 * Redesign (Figma 07 · Register member, "Gender" chips): the ✓ shows only on the selected radio chip.
 * Set from code because a state-list compound drawable keeps the size it had when first measured (0 when unchecked).
 */
fun RadioButton.dsRefreshChipCheck() {
    val check = if (isChecked) AppCompatResources.getDrawable(context, R.drawable.ic_ds_check) else null
    setCompoundDrawablesRelativeWithIntrinsicBounds(check, null, null, null)
    // Figma centres "✓ + label" as one group; a compound drawable sits at the padding edge, so pad it inwards.
    if (width > 0) dsCentreChipContent() else post { dsCentreChipContent() }
}

private fun RadioButton.dsCentreChipContent() {
    val base = (12 * resources.displayMetrics.density).toInt()
    val icon = compoundDrawablesRelative[0]
    if (icon == null || width == 0) {
        gravity = Gravity.CENTER
        setPaddingRelative(base, paddingTop, base, paddingBottom)
        return
    }
    gravity = Gravity.START or Gravity.CENTER_VERTICAL // label right after the ✓; the padding centres the pair
    val content = icon.bounds.width() + compoundDrawablePadding + paint.measureText(text.toString()).toInt()
    val side = ((width - content) / 2).coerceAtLeast(base)
    setPaddingRelative(side, paddingTop, base, paddingBottom)
}

/** Chips sharing a row stay on one line; a long label ("Transgender") shrinks to fit (14sp → 11sp) instead of breaking. */
fun RadioButton.dsFitChipInRow() {
    maxLines = 1
    TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 11, 14, 1, TypedValue.COMPLEX_UNIT_SP)
}
