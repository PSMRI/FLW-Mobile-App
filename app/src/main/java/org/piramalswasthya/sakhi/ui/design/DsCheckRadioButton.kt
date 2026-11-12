package org.piramalswasthya.sakhi.ui.design

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.graphics.drawable.DrawableCompat
import org.piramalswasthya.sakhi.R

/**
 * Redesign (Figma "Segmented option" / "Filter chip"): a radio button drawn as an option box that shows a ✓ before
 * its label while selected, with "✓ + label" centred as one group. Selection behaviour is plain RadioButton /
 * RadioGroup — only the drawing changes. The ✓ takes the current text colour (white on the filled segmented option).
 */
class DsCheckRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.radioButtonStyle,
) : AppCompatRadioButton(context, attrs, defStyleAttr) {

    private val sidePadding = (4 * resources.displayMetrics.density).toInt() // narrow 3-up options need the room

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        refreshCheck()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { centreContent() }
    }

    private fun refreshCheck() {
        val icon = if (isChecked) AppCompatResources.getDrawable(context, R.drawable.ic_ds_check)?.mutate()?.also {
            DrawableCompat.setTint(it, currentTextColor)
        } else null
        setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
        if (width > 0) centreContent()
    }

    private fun centreContent() {
        val base = sidePadding
        val icon = compoundDrawablesRelative[0]
        if (icon == null || width == 0) {
            gravity = Gravity.CENTER
            setPaddingRelative(base, paddingTop, base, paddingBottom)
            return
        }
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        val content = icon.bounds.width() + compoundDrawablePadding + paint.measureText(text.toString()).toInt()
        val start = ((width - content) / 2).coerceAtLeast(base)
        setPaddingRelative(start, paddingTop, base, paddingBottom)
    }
}
