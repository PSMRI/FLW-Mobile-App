package org.piramalswasthya.sakhi.utils

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import org.piramalswasthya.sakhi.R
import java.util.Calendar

class MonthYearPickerDialog : DialogFragment() {

    private val MAX_YEAR = 2099
    private val MIN_YEAR = 2020
    private var listener: DatePickerDialog.OnDateSetListener? = null

    fun setListener(listener: DatePickerDialog.OnDateSetListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        val cal = Calendar.getInstance()

        val dialog = inflater.inflate(R.layout.month_year_picker, null)
        val monthPicker = dialog.findViewById<NumberPicker>(R.id.picker_month)
        val yearPicker = dialog.findViewById<NumberPicker>(R.id.picker_year)

        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.value = cal.get(Calendar.MONTH)
        monthPicker.displayedValues = resources.getStringArray(R.array.months)

        val year = cal.get(Calendar.YEAR)
        yearPicker.minValue = MIN_YEAR
        yearPicker.maxValue = year
        yearPicker.value = year

        builder.setView(dialog)
            // Add action buttons
            .setPositiveButton(R.string.ok) { _, _ ->
                listener?.onDateSet(null, yearPicker.value, monthPicker.value, 0)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                this@MonthYearPickerDialog.dialog?.cancel()
            }
        return builder.create()
    }
}
