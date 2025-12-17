package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.widget.EditText
import android.widget.NumberPicker
import org.piramalswasthya.sakhi.databinding.AlertAgePickerBinding
import org.piramalswasthya.sakhi.model.AgeUnitDTO


class AgePickerDialog(context: Context) : AlertDialog(context) {

    private var _binding: AlertAgePickerBinding? = null

    private val binding: AlertAgePickerBinding
        get() = _binding!!

    private var yearsMin: Int = 0
    private var yearsMax: Int = 0
    private var montsMin: Int = 0
    private var monthsMax: Int = 0
    private var daysMin: Int = 0
    private var daysMax: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = AlertAgePickerBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
    }

    /**
     * age picker dialog
     * - setting min and max values
     * - setting default values from dto
     * - trigger show to open the dialog
     */
    fun setLimitsAndShow(
        yearsMin: Int,
        yearsMax: Int,
        monthsMin: Int,
        monthsMax: Int,
        daysMin: Int,
        daysMax: Int,
        ageUnitDTO: AgeUnitDTO,
        isOk: Boolean
    ) {
        this.yearsMin = yearsMin
        this.yearsMax = yearsMax
        this.montsMin = monthsMin
        this.monthsMax = monthsMax
        this.daysMin = daysMin
        this.daysMax = daysMax
        show(ageUnitDTO, isOk)
    }

    fun show(ageUnitDTO: AgeUnitDTO, isOk: Boolean) {
        Handler(Looper.getMainLooper()).post {
            super.show()
            val safeYearsMin = yearsMin.coerceAtLeast(0)
            val safeYearsMax = yearsMax.coerceAtLeast(safeYearsMin)

            val safeMonthsMin = montsMin.coerceAtLeast(0)
            val safeMonthsMax = monthsMax.coerceAtLeast(safeMonthsMin)

            val safeDaysMin = daysMin.coerceAtLeast(0)
            val safeDaysMax = daysMax.coerceAtLeast(safeDaysMin)

            binding.dialogNumberPickerYears.apply {
                minValue = safeYearsMin
                maxValue = safeYearsMax
                value = ageUnitDTO.years.coerceIn(safeYearsMin, safeYearsMax)
                forceLatinDigits(this)
            }

            binding.dialogNumberPickerMonths.apply {
                minValue = safeMonthsMin
                maxValue = safeMonthsMax
                value = ageUnitDTO.months.coerceIn(safeMonthsMin, safeMonthsMax)
                forceLatinDigits(this)
            }

            binding.dialogNumberPickerDays.apply {
                minValue = safeDaysMin
                maxValue = safeDaysMax
                value = ageUnitDTO.days.coerceIn(safeDaysMin, safeDaysMax)
                forceLatinDigits(this)
            }

            binding.btnOk.setOnClickListener {
                val mInputTextYears: EditText = binding.dialogNumberPickerYears.findViewById(
                    Resources.getSystem().getIdentifier("numberpicker_input", "id", "android")
                )
                ageUnitDTO.years = mInputTextYears.text.toString().toInt()

                val mInputTextMonths: EditText = binding.dialogNumberPickerMonths.findViewById(
                    Resources.getSystem().getIdentifier("numberpicker_input", "id", "android")
                )
                ageUnitDTO.months = mInputTextMonths.text.toString().toInt()

                val mInputTextDays: EditText = binding.dialogNumberPickerDays.findViewById(
                    Resources.getSystem().getIdentifier("numberpicker_input", "id", "android")
                )
                ageUnitDTO.days = mInputTextDays.text.toString().toInt()
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                cancel()
            }
        }
    }

    private fun forceLatinDigits(picker: NumberPicker) {
        val min = picker.minValue
        val max = picker.maxValue
        picker.displayedValues = null
        picker.displayedValues = (min..max).map { it.toString() }.toTypedArray()
        val editTextId = Resources.getSystem().getIdentifier("numberpicker_input", "id", "android")
        val input = picker.findViewById<EditText>(editTextId)
        input?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            keyListener = DigitsKeyListener.getInstance("0123456789")
            isFocusable = true
            isFocusableInTouchMode = true
            isCursorVisible = true
        }
    }


    companion object {
        const val TAG = "AgePickerDialog"
    }
}