package org.piramalswasthya.sakhi.ui.design

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.piramalswasthya.sakhi.R

/**
 * Redesign (Figma M2.10): colours the 5-step ABHA progress strip (layout_ds_abha_steps).
 * Steps before [current] = done (green bar), [current] = primary bar + primary label, after = grey.
 * [current] = 6 marks every step done.
 */
object AbhaSteps {
    private val bars = intArrayOf(R.id.step_bar_1, R.id.step_bar_2, R.id.step_bar_3, R.id.step_bar_4, R.id.step_bar_5)
    private val labels = intArrayOf(R.id.step_label_1, R.id.step_label_2, R.id.step_label_3, R.id.step_label_4, R.id.step_label_5)

    /** Finds the strip (id ll_abha_steps) anywhere under [root]; hides it when [visible] is false (find-ABHA flow). */
    fun show(root: View, current: Int, visible: Boolean = true) {
        val strip = root.findViewById<View>(R.id.ll_abha_steps) ?: return
        strip.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) bind(strip, current)
    }

    fun bind(root: View, current: Int) {
        val ctx = root.context
        for (i in bars.indices) {
            val step = i + 1
            root.findViewById<View>(bars[i])?.setBackgroundResource(
                when {
                    step < current -> R.drawable.bg_ds_step_done
                    step == current -> R.drawable.bg_ds_step_current
                    else -> R.drawable.bg_ds_step_todo
                }
            )
            root.findViewById<TextView>(labels[i])?.setTextColor(
                ContextCompat.getColor(ctx, if (step == current) R.color.ds_primary else R.color.ds_on_surface_variant)
            )
        }
    }
}

/** Redesign: Figma "Segmented option" on a plain TextView toggle (bg_ds_segment_toggle, ✓ when selected). */
fun TextView.dsSegment(selected: Boolean) {
    isActivated = selected
    setBackgroundResource(R.drawable.bg_ds_segment_toggle)
    setTextColor(ContextCompat.getColorStateList(context, R.color.ds_segment_toggle_text))
    val check = if (selected) ContextCompat.getDrawable(context, R.drawable.ic_ds_check)?.mutate()?.also {
        val px = (18 * resources.displayMetrics.density).toInt()
        it.setBounds(0, 0, px, px)
        it.setTint(ContextCompat.getColor(context, R.color.ds_on_primary))
    } else null
    setCompoundDrawablesRelative(check, null, null, null)
    val gap = (4 * resources.displayMetrics.density).toInt()
    compoundDrawablePadding = gap
    // ✓ + label centred as one group (a compound drawable alone would sit at the left edge)
    post {
        val content = paint.measureText(text.toString()) + (check?.bounds?.width()?.plus(gap) ?: 0)
        val side = ((width - content) / 2f).toInt().coerceAtLeast(0)
        gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
        setPaddingRelative(side, paddingTop, side, paddingBottom)
    }
}
