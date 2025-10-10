package org.piramalswasthya.sakhi.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
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
}
