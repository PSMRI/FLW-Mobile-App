package org.piramalswasthya.sakhi.utils

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TextViewBindingAdapters {

    @JvmStatic
    @BindingAdapter("formattedMeetingDate")
    fun setFormattedMeetingDate(view: TextView, dateString: String?) {
        if (!dateString.isNullOrBlank()) {
            try {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)

                val monthFormat = SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
                val formatted = monthFormat.format(date!!)

                view.text = "Maa Meeting - ($formatted)"
            } catch (e: Exception) {
                view.text = ""
            }
        } else {
            view.text = ""
        }
    }

//    @JvmStatic
//    @BindingAdapter("visibleForORS", "visibleForSAM", "visibleForIFA", requireAll = false)
//    fun setButtonVisibility(view: View, ors: String?, sam: String?, ifa: String?) {
//        val isVisible = when {
//            ors != null -> ors.equals("true", ignoreCase = true)
//            sam != null -> sam.equals("true", ignoreCase = true)
//            ifa != null -> ifa.equals("true", ignoreCase = true)
//            else -> false
//        }
//        view.visibility = if (isVisible) View.VISIBLE else View.GONE
//    }

    @JvmStatic
    @BindingAdapter("ageForORSVisibility")
    fun setORSVisibility(view: View, ageString: String?) {
        view.visibility = if (shouldShowORS(ageString)) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("ageForSAMVisibility")
    fun setSAMVisibility(view: View, ageString: String?) {
        view.visibility = if (shouldShowSAMorIFA(ageString)) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("ageForIFAVisibility")
    fun setIFAVisibility(view: View, ageString: String?) {
        view.visibility = if (shouldShowSAMorIFA(ageString)) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("dobToDateText")
    fun setDobToDateText(view: TextView, dob: Long?) {
        if (dob != null && dob > 0L) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formatted = sdf.format(Date(dob))
                view.text = formatted
            } catch (e: Exception) {
                view.text = ""
            }
        } else {
            view.text = ""
        }
    }


    // ---- Helper functions ----
    private fun shouldShowORS(ageString: String?): Boolean {
        val (years, months) = parseAge(ageString)
        return years in 0..5
    }

    private fun shouldShowSAMorIFA(ageString: String?): Boolean {
        val (years, months) = parseAge(ageString)
        return (years == 0 && months >= 6) || (years in 1..5)
    }

    private fun parseAge(ageString: String?): Pair<Int, Int> {
        var years = 0
        var months = 0
        if (ageString.isNullOrBlank()) return years to months

        val yearRegex = "(\\d+)\\s*YEAR".toRegex(RegexOption.IGNORE_CASE)
        val monthRegex = "(\\d+)\\s*MONTH".toRegex(RegexOption.IGNORE_CASE)

        yearRegex.find(ageString)?.groupValues?.get(1)?.toIntOrNull()?.let { years = it }
        monthRegex.find(ageString)?.groupValues?.get(1)?.toIntOrNull()?.let { months = it }

        return years to months
    }

}
