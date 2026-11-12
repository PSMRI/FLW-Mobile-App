package org.piramalswasthya.sakhi.ui.design

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import org.piramalswasthya.sakhi.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Redesign (Figma M2.11-03 "Summary" stat tile): amount 22/28 Bold in the tile colour,
 * label 12/16 Medium on-surface below it. The tile keeps its own text colour for the amount.
 */
fun TextView.dsStatTile(amount: Number, label: String) {
    val amountText = "₹" + NumberFormat.getIntegerInstance(Locale("en", "IN")).format(amount)
    val sb = SpannableStringBuilder(amountText)
    sb.setSpan(AbsoluteSizeSpan(22, true), 0, amountText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.setSpan(StyleSpan(Typeface.BOLD), 0, amountText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.append("\n")
    val start = sb.length
    sb.append(label)
    sb.setSpan(AbsoluteSizeSpan(12, true), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.ds_on_surface)),
        start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    text = sb
}

/**
 * Redesign (Figma M2.11-03 "Table head"): ASHA incentives show 3 columns — S.No · Activity · No. of claims
 * (the cards carry the amount). Mitanin keeps the old 5 columns because its rows are still column-based.
 */
fun ConstraintLayout.dsIncentiveHeaderThreeColumns(
    activity: TextView, count: TextView, amount: View, doc: View
) {
    amount.visibility = View.GONE
    doc.visibility = View.GONE
    count.setText(R.string.ds_no_of_claims)
    count.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
    ConstraintSet().apply {
        clone(this@dsIncentiveHeaderThreeColumns)
        clear(count.id, ConstraintSet.START)
        constrainWidth(count.id, ConstraintSet.WRAP_CONTENT)
        connect(count.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(activity.id, ConstraintSet.END, count.id, ConstraintSet.START, 8.dp(context))
        applyTo(this@dsIncentiveHeaderThreeColumns)
    }
}

private fun Int.dp(ctx: android.content.Context) = (this * ctx.resources.displayMetrics.density).toInt()

/** "₹1,000" — Indian grouping, same as the group header amounts. Used from data binding. */
fun dsRupees(amount: Long): String =
    "₹" + NumberFormat.getIntegerInstance(Locale("en", "IN")).format(amount)
