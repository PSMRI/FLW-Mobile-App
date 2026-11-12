package org.piramalswasthya.sakhi.ui.design

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.sakhi.R

/**
 * Redesign: fields that FormRendererAdapter builds in code (server-driven forms) get the same look as the XML form kit
 * (Figma 07 · Register member): 56dp box r12, outline #72787E → primary 2dp when focused, white box, 16/24 value text
 * in on-surface, error text 12/16 in the error colour, check / radio boxes tinted primary.
 *
 * Runs after a field is bound and only touches colours, corners and text styles — never values, listeners or visibility.
 * Colours the adapter set on purpose (e.g. the red "highlight" option) are kept: only pure black / RED defaults are replaced.
 */
fun ViewGroup.dsStyleDynamicInputs() {
    val ctx = context
    val onSurface = ContextCompat.getColor(ctx, R.color.ds_on_surface)
    val onSurfaceVariant = ContextCompat.getColor(ctx, R.color.ds_on_surface_variant)
    val primary = ContextCompat.getColor(ctx, R.color.ds_primary)
    val outline = ContextCompat.getColor(ctx, R.color.ds_outline)
    val outlineVariant = ContextCompat.getColor(ctx, R.color.ds_outline_variant)
    val error = ContextCompat.getColor(ctx, R.color.ds_error)
    val radius = 12f * resources.displayMetrics.density

    fun walk(v: View) {
        when (v) {
            is TextInputLayout -> {
                v.setBoxStrokeColorStateList(
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_focused),
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf()
                        ),
                        intArrayOf(primary, outlineVariant, outline)
                    )
                )
                v.boxStrokeWidth = (1 * resources.displayMetrics.density).toInt()
                v.boxStrokeWidthFocused = (2 * resources.displayMetrics.density).toInt()
                // the adapter passes 12f as pixels (≈6dp on the test phone) — Figma radius is 12dp
                v.setBoxCornerRadii(radius, radius, radius, radius)
                v.boxBackgroundColor = Color.WHITE
                v.defaultHintTextColor = ColorStateList.valueOf(onSurfaceVariant)
                v.hintTextColor = ColorStateList.valueOf(primary)
                v.setErrorTextColor(ColorStateList.valueOf(error))
            }
            is CompoundButton -> { // CheckBox / RadioButton / Switch
                CompoundButtonCompat.setButtonTintList(
                    v, ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                        intArrayOf(primary, outline)
                    )
                )
                v.textSize = 16f
                if (isDefaultTextColor(v)) v.setTextColor(onSurface)
            }
            is EditText -> {
                if (v.currentTextColor == Color.BLACK) v.setTextColor(onSurface)
            }
            is TextView -> {
                if (v.tag == "field_error_tv") {
                    TextViewCompat.setTextAppearance(v, R.style.TextAppearance_Sakhi_BodySmall)
                    v.setTextColor(error)
                } else if (v.currentTextColor == Color.BLACK) {
                    v.setTextColor(onSurface)
                }
            }
        }
        if (v is ViewGroup && v !is TextInputLayout) for (i in 0 until v.childCount) walk(v.getChildAt(i))
        if (v is TextInputLayout) v.editText?.let { walk(it) }
    }

    for (i in 0 until childCount) walk(getChildAt(i))
}

/** True when the text colour is still the platform / theme default (not a colour the adapter chose on purpose). */
private fun isDefaultTextColor(v: TextView): Boolean {
    val c = v.currentTextColor
    return c == Color.BLACK || Color.alpha(c) < 255 || (Color.red(c) == Color.green(c) && Color.green(c) == Color.blue(c))
}
