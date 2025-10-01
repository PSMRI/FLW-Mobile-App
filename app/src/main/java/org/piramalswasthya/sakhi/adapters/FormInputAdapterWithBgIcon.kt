package org.piramalswasthya.sakhi.adapters

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.button.MaterialButton
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemFormAgePickerViewV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormBtnBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormCheckV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormDatepickerWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormDropdownWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormEditTextWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormHeadlineV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormImageViewWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormRadioWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormTextViewWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormTimepickerWithBgIconBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormUploadImageBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.FormInputOld
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.AGE_PICKER
import org.piramalswasthya.sakhi.model.InputType.BUTTON
import org.piramalswasthya.sakhi.model.InputType.CHECKBOXES
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.FILE_UPLOAD
import org.piramalswasthya.sakhi.model.InputType.HEADLINE
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import org.piramalswasthya.sakhi.model.InputType.TIME_PICKER
import org.piramalswasthya.sakhi.model.InputType.values
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.AgePickerDialog
import org.piramalswasthya.sakhi.utils.HelperUtil.getAgeStrFromAgeUnit
import org.piramalswasthya.sakhi.utils.HelperUtil.getDobFromAge
import org.piramalswasthya.sakhi.utils.HelperUtil.getLongFromDate
import org.piramalswasthya.sakhi.utils.HelperUtil.updateAgeDTO
import timber.log.Timber
import java.util.Calendar

class FormInputAdapterWithBgIcon (
    private val imageClickListener: ImageClickListener? = null,
    private val ageClickListener: AgeClickListener? = null,
    private val formValueListener: FormValueListener? = null,
    private val selectImageClickListener: SelectUploadImageClickListener? = null,
    private val viewDocumentListner: ViewDocumentOnClick? = null,
    private val isEnabled: Boolean = true
) : ListAdapter<FormElement, ViewHolder>(FormInputDiffCallBack) {


    object FormInputDiffCallBack : DiffUtil.ItemCallback<FormElement>() {
        override fun areItemsTheSame(oldItem: FormElement, newItem: FormElement) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FormElement, newItem: FormElement): Boolean {
            Timber.d("${oldItem.id}   ${oldItem.errorText} ${newItem.errorText}")
            return oldItem.errorText == newItem.errorText
        }
    }



    class EditTextInputViewHolder private constructor(private val binding: RvItemFormEditTextWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormEditTextWithBgIconBinding.inflate(layoutInflater, parent, false)
                return EditTextInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?) {
            Timber.d("binding triggered!!! $isEnabled ${item.id}")
            if (!isEnabled) {
                binding.et.isClickable = false
                binding.et.isFocusable = false
                handleHintLength(item)
                binding.form = item
                binding.et.setText(item.value)
                binding.executePendingBindings()
                return
            } else {
                binding.et.isClickable = true
                binding.et.isFocusable = true
            }
            val param = binding.tilEditText.layoutParams as ViewGroup.MarginLayoutParams
//            param.setMargins(60,0,0,0)
            binding.tilEditText.layoutParams = param
            binding.form = item
            if (item.errorText == null) binding.tilEditText.isErrorEnabled = false
            Timber.d("Bound EditText item ${item.title} with ${item.required}")
            binding.tilEditText.error = item.errorText
            handleHintLength(item)
            if (item.hasSpeechToText) {
                binding.tilEditText.endIconDrawable =
                    AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_mic)
                binding.tilEditText.setEndIconOnClickListener {
                    formValueListener?.onValueChanged(item, Konstants.micClickIndex)
                }
            } else {
                binding.tilEditText.endIconDrawable = null
                binding.tilEditText.setEndIconOnClickListener(null)
            }


            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {

                    item.value = editable?.toString()
                    Timber.d("editable : $editable Current value : ${item.value}  isNull: ${item.value == null} isEmpty: ${item.value == ""}")
                    formValueListener?.onValueChanged(item, -1)
                    if (item.errorText != binding.tilEditText.error) {
                        binding.tilEditText.isErrorEnabled = item.errorText != null
                        binding.tilEditText.error = item.errorText
                    }

                }
            }
            binding.et.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) binding.et.addTextChangedListener(textWatcher)
                else {
                    binding.et.removeTextChangedListener(textWatcher)
                    val imm =
                        binding.root.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
            }
            binding.et.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && (event.action == KeyEvent.ACTION_UP || event.action == KeyEvent.ACTION_DOWN)) {
                    return@OnKeyListener true
                }
                false
            })

            binding.executePendingBindings()
        }

        private fun handleHintLength(item: FormElement) {
            if (item.title.length > Konstants.editTextHintLimit) {
                binding.tvHint.visibility = View.VISIBLE
                binding.et.hint = null
                binding.tilEditText.hint = null
                binding.tilEditText.isHintEnabled = false
            } else {
                binding.tvHint.visibility = View.GONE
                binding.tilEditText.isHintEnabled = true
            }
        }
    }

    class SelectUploadImageClickListener(private val selectImageClick: (formId: Int) -> Unit) {

        fun onSelectImageClick(form: FormElement) = selectImageClick(form.id)

    }

    class ViewDocumentOnClick(private val viewDocument: (formId: Int) -> Unit) {

        fun onViewDocumentClick(form: FormElement) = viewDocument(form.id)

    }


    class DropDownInputViewHolder private constructor(private val binding: RvItemFormDropdownWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDropdownWithBgIconBinding.inflate(layoutInflater, parent, false)
                return DropDownInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?) {
            binding.form = item
            if (item.errorText == null) {
                binding.tilRvDropdown.error = null
                binding.tilRvDropdown.isErrorEnabled = false
            }
            if (!isEnabled) {
                binding.tilRvDropdown.visibility = View.GONE
//                binding.ivIconBg.visibility = View.GONE
//                binding.ivIconTemperature.visibility = View.GONE

                binding.tilEditText.visibility = View.VISIBLE
                binding.ivIconBg1.visibility = View.VISIBLE
                binding.ivIconTemperature1.visibility = View.VISIBLE

                binding.et.isFocusable = false
                binding.et.isClickable = false
                binding.executePendingBindings()
                return
            }

            binding.actvRvDropdown.setOnItemClickListener { _, _, index, _ ->
                item.value = item.entries?.get(index)
                Timber.d("Item DD : $item")
                formValueListener?.onValueChanged(item, index)
                binding.tilRvDropdown.isErrorEnabled = item.errorText != null
                binding.tilRvDropdown.error = item.errorText
            }

            item.errorText?.let { binding.tilRvDropdown.error = it }
            binding.executePendingBindings()

        }
    }

    class RadioInputViewHolder private constructor(private val binding: RvItemFormRadioWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormRadioWithBgIconBinding.inflate(layoutInflater, parent, false)
                return RadioInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            if (!isEnabled) {
                binding.rg.isClickable = false
                binding.rg.isFocusable = false
            }
            binding.invalidateAll()
            binding.form = item

            binding.rg.removeAllViews()

            binding.rg.apply {
                item.entries?.let { items ->
                    orientation = item.orientation ?: LinearLayout.HORIZONTAL
                    weightSum = items.size.toFloat()
                    items.forEach {
                        val rdBtn = RadioButton(this.context)
                        rdBtn.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            1.0F
                        ).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                        rdBtn.id = View.generateViewId()
                        val colorStateList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ColorStateList(
                                arrayOf(
                                    intArrayOf(-android.R.attr.state_checked),
                                    intArrayOf(android.R.attr.state_checked)
                                ), intArrayOf(
                                    binding.root.resources.getColor(
                                        android.R.color.darker_gray,
                                        binding.root.context.theme
                                    ),  // disabled
                                    binding.root.resources.getColor(
                                        android.R.color.darker_gray,
                                        binding.root.context.theme
                                    ) // enabled
                                )
                            )
                        } else {
                            ColorStateList(
                                arrayOf(
                                    intArrayOf(-android.R.attr.state_checked),
                                    intArrayOf(android.R.attr.state_checked)
                                ), intArrayOf(
                                    binding.root.resources.getColor(
                                        android.R.color.darker_gray,
                                    ),  // disabled
                                    binding.root.resources.getColor(
                                        android.R.color.darker_gray,
                                    ) // enabled
                                )
                            )
                        }

                        if (!isEnabled) rdBtn.buttonTintList = colorStateList
                        rdBtn.text = it
                        addView(rdBtn)
                        if (item.value == it) rdBtn.isChecked = true
                        rdBtn.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                item.value = it
                                if (item.hasDependants || item.hasAlertError) {
                                    Timber.d(
                                        "listener trigger : ${item.id} ${
                                            item.entries!!.indexOf(
                                                it
                                            )
                                        } $it"
                                    )
                                    formValueListener?.onValueChanged(
                                        item, item.entries!!.indexOf(it)
                                    )
                                }
                            }
                            item.errorText = null
                            binding.llContent.setBackgroundResource(0)
                        }
                    }
                }
            }

            if (!isEnabled) {
                binding.rg.children.forEach {
                    it.isClickable = false
                }
            }
            if (item.errorText != null) binding.llContent.setBackgroundResource(R.drawable.state_errored)
            else binding.llContent.setBackgroundResource(0)

            binding.executePendingBindings()
            val str = binding.tvNullable.text
            val spannableString = SpannableString(str)

            val colorSpan = ForegroundColorSpan(Color.parseColor("#B00020"))

            if (item.required && item.doubleStar) {
                spannableString.setSpan(
                    colorSpan,
                    str.length - 2,
                    str.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvNullable.text = spannableString
                binding.tvNullableHr.text = spannableString
            } else if (item.required) {
                spannableString.setSpan(
                    colorSpan,
                    str.length - 1,
                    str.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvNullableHr.text = spannableString
                binding.tvNullable.text = spannableString
            }

        }
    }

    class CheckBoxesInputViewHolder private constructor(private val binding: RvItemFormCheckV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormCheckV2Binding.inflate(layoutInflater, parent, false)
                return CheckBoxesInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            binding.form = item

            if (item.errorText != null) binding.clRi.setBackgroundResource(R.drawable.state_errored)
            else binding.clRi.setBackgroundResource(0)
            binding.llChecks.removeAllViews()
            binding.llChecks.apply {
                item.entries?.let { items ->
                    orientation = item.orientation ?: LinearLayout.VERTICAL
                    weightSum = items.size.toFloat()
                    items.forEachIndexed { index, it ->
                        val cbx = CheckBox(this.context)
                        cbx.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            1.0F
                        )
                        if (!isEnabled) {
                            cbx.isClickable = false
                            cbx.isFocusable = false
                        }
                        cbx.id = View.generateViewId()
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) cbx.setTextAppearance(
                            context, android.R.style.TextAppearance_Material_Medium
                        )
                        else cbx.setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
                        cbx.text = it
                        addView(cbx)
                        if (item.value?.contains(it) == true) cbx.isChecked = true
                        cbx.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                if (item.value != null) item.value = item.value + it
                                else item.value = it
                                if (item.hasDependants || item.hasAlertError) {
                                    Timber.d(
                                        "listener trigger : ${item.id} ${
                                            item.entries!!.indexOf(
                                                it
                                            )
                                        } $it"
                                    )
                                }
                            } else {
                                if (item.value?.contains(it) == true) {
                                    item.value = item.value?.replace(it, "")
                                }
                            }
                            formValueListener?.onValueChanged(
                                item, index * (if (b) 1 else -1)
                            )
                            if (item.value.isNullOrBlank()) {
                                item.value = null
                            } else {
                                Timber.d("Called here!")
                                item.errorText = null
                                binding.clRi.setBackgroundResource(0)
                            }
                            Timber.d("Checkbox value : ${item.value}")

                        }
                    }
                }
            }
            binding.executePendingBindings()

        }
    }

    class DatePickerInputViewHolder private constructor(private val binding: RvItemFormDatepickerWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDatepickerWithBgIconBinding.inflate(layoutInflater, parent, false)
                return DatePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            binding.form = item
            binding.invalidateAll()
            if (!isEnabled) {
                binding.et.isFocusable = false
                binding.et.isClickable = false
                binding.executePendingBindings()
                return
            }
            val today = Calendar.getInstance()
            var thisYear = today.get(Calendar.YEAR)
            var thisMonth = today.get(Calendar.MONTH)
            var thisDay = today.get(Calendar.DAY_OF_MONTH)

            item.errorText?.also { binding.tilEditText.error = it }
                ?: run { binding.tilEditText.error = null }
            binding.et.setOnClickListener {
                item.value?.let { value ->
                    thisYear = value.substring(6).toInt()
                    thisMonth = value.substring(3, 5).trim().toInt() - 1
                    thisDay = value.substring(0, 2).trim().toInt()
                }
                val datePickerDialog = DatePickerDialog(
                    it.context, { _, year, month, day ->
                        val millis = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                        }.timeInMillis
                        if (item.min != null && millis < item.min!!) {
                            item.value = getDateString(item.min)
                        } else if (item.max != null && millis > item.max!!)
                            item.value = getDateString(item.max)
                        else
                            item.value = getDateString(millis)
//                            "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
                        binding.invalidateAll()
                        if (item.hasDependants) formValueListener?.onValueChanged(item, -1)
                    }, thisYear, thisMonth, thisDay
                )
                item.errorText = null
                binding.tilEditText.error = null
                datePickerDialog.datePicker.maxDate = item.max ?: 0
                datePickerDialog.datePicker.minDate = item.min ?: 0
                if (item.showYearFirstInDatePicker)
                    datePickerDialog.datePicker.touchables[0].performClick()
                if (item.max!!> item.min!!){
                    datePickerDialog.show()
                }else{
                    Toast.makeText(binding.root.context,"Something went wrong",Toast.LENGTH_SHORT).show()
                }

            }
            binding.executePendingBindings()

        }
    }

    class TimePickerInputViewHolder private constructor(private val binding: RvItemFormTimepickerWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTimepickerWithBgIconBinding.inflate(layoutInflater, parent, false)
                return TimePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean
        ) {
            binding.form = item
            binding.et.isEnabled = isEnabled
            binding.et.setOnClickListener {
                val hour: Int
                val minute: Int
                if (item.value == null) {
                    val currentTime = Calendar.getInstance()
                    hour = currentTime.get(Calendar.HOUR_OF_DAY)
                    minute = currentTime.get(Calendar.MINUTE)
                } else {
                    hour = item.value!!.substringBefore(":").toInt()
                    minute = item.value!!.substringAfter(":").toInt()
                    Timber.d("Time picker hour min : $hour $minute")
                }
                val mTimePicker = TimePickerDialog(it.context, { _, hourOfDay, minuteOfHour ->
                    item.value = "$hourOfDay:$minuteOfHour"
                    binding.invalidateAll()

                }, hour, minute, false)
                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }
            binding.executePendingBindings()

        }
    }

    class TextViewInputViewHolder private constructor(private val binding: RvItemFormTextViewWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTextViewWithBgIconBinding.inflate(layoutInflater, parent, false)
                return TextViewInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement) {
            binding.form = item
            binding.executePendingBindings()
        }
    }

    class ImageViewInputViewHolder private constructor(private val binding: RvItemFormImageViewWithBgIconBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormImageViewWithBgIconBinding.inflate(layoutInflater, parent, false)
                return ImageViewInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, clickListener: ImageClickListener?, isEnabled: Boolean
        ) {
            binding.form = item
            if (isEnabled) {
                binding.clickListener = clickListener
                if (item.errorText != null) binding.clRi.setBackgroundResource(R.drawable.state_errored)
                else binding.clRi.setBackgroundResource(0)
            }
            binding.executePendingBindings()

        }
    }



    class FileUploadInputViewHolder private constructor(private val binding: RvItemFormUploadImageBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormUploadImageBinding.inflate(layoutInflater, parent, false)
                return FileUploadInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, clickListener: SelectUploadImageClickListener?, documentOnClick: ViewDocumentOnClick?, isEnabled: Boolean) {

            binding.form = item
            binding.tvTitle.text = item.title
            binding.clickListener = clickListener
            binding.documentclickListener = documentOnClick
            if (item.value != null) binding.btnView.visibility = View.VISIBLE
            else binding.btnView.visibility = View.GONE

        }

        private lateinit var countDownTimer : CountDownTimer
        private var countdownTimers : HashMap<Int, CountDownTimer> = HashMap()

        private fun formatTimeInSeconds(millis: Long) : String {
            val seconds = millis / 1000
            return "${seconds} sec"
        }
        private fun startTimer(timerInSec: TextView, generateOtp: MaterialButton) {
            countDownTimer =  object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerInSec.visibility = View.VISIBLE
                    timerInSec.text = formatTimeInSeconds(millisUntilFinished)
                }
                override fun onFinish() {
                    timerInSec.visibility = View.INVISIBLE
                    timerInSec.text = ""
                    generateOtp.isEnabled = true
                    generateOtp.text = timerInSec.resources.getString(R.string.resend_otp)
                }
            }.start()

            countdownTimers[adapterPosition] = countDownTimer

        }
    }
    class ButtonInputViewHolder private constructor(private val binding: RvItemFormBtnBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormBtnBinding.inflate(layoutInflater, parent, false)
                return ButtonInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?) {
            binding.form = item
            binding.generateOtp.isEnabled = isEnabled
            binding.generateOtp.text = binding.generateOtp.resources.getString(R.string.generate_otp)
            binding.generateOtp.setOnClickListener {
                binding.generateOtp.isEnabled = !isEnabled
                startTimer(binding.timerInSec,binding.generateOtp)
                binding.tilEditText.visibility = View.VISIBLE
            }


        }

        private lateinit var countDownTimer : CountDownTimer
        private var countdownTimers : HashMap<Int, CountDownTimer> = HashMap()

        private fun formatTimeInSeconds(millis: Long) : String {
            val seconds = millis / 1000
            return "${seconds} sec"
        }
        private fun startTimer(timerInSec: TextView, generateOtp: MaterialButton) {
            countDownTimer =  object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerInSec.visibility = View.VISIBLE
                    timerInSec.text = formatTimeInSeconds(millisUntilFinished)
                }
                override fun onFinish() {
                    timerInSec.visibility = View.INVISIBLE
                    timerInSec.text = ""
                    generateOtp.isEnabled = true
                    generateOtp.text = timerInSec.resources.getString(R.string.resend_otp)
                }
            }.start()

            countdownTimers[adapterPosition] = countDownTimer

        }
    }

    class FileUploadInputViewHolder private constructor(private val binding: RvItemFormUploadImageBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormUploadImageBinding.inflate(layoutInflater, parent, false)
                return FileUploadInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, clickListener: SelectUploadImageClickListener?, documentOnClick: ViewDocumentOnClick?, isEnabled: Boolean) {

            binding.form = item
            binding.tvTitle.text = item.title
            binding.clickListener = clickListener
            binding.documentclickListener = documentOnClick
            if (item.value != null) binding.btnView.visibility = View.VISIBLE
            else binding.btnView.visibility = View.GONE

        }

        private lateinit var countDownTimer : CountDownTimer
        private var countdownTimers : HashMap<Int, CountDownTimer> = HashMap()

        private fun formatTimeInSeconds(millis: Long) : String {
            val seconds = millis / 1000
            return "${seconds} sec"
        }
        private fun startTimer(timerInSec: TextView, generateOtp: MaterialButton) {
            countDownTimer =  object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerInSec.visibility = View.VISIBLE
                    timerInSec.text = formatTimeInSeconds(millisUntilFinished)
                }
                override fun onFinish() {
                    timerInSec.visibility = View.INVISIBLE
                    timerInSec.text = ""
                    generateOtp.isEnabled = true
                    generateOtp.text = timerInSec.resources.getString(R.string.resend_otp)
                }
            }.start()

            countdownTimers[adapterPosition] = countDownTimer

        }
    }
    class AgePickerViewInputViewHolder private constructor(private val binding: RvItemFormAgePickerViewV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    RvItemFormAgePickerViewV2Binding.inflate(layoutInflater, parent, false)
                return AgePickerViewInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            binding.form = item
            binding.invalidateAll()
            val agePicker = AgePickerDialog(binding.root.context)

            val calDob = Calendar.getInstance()
            val ageUnitDTO = AgeUnitDTO(0, 0, 0)
            val isOk = true
            item.value?.let {
                calDob.timeInMillis = getLongFromDate(it)
                updateAgeDTO(ageUnitDTO, calDob)
                binding.etNum.setText(getAgeStrFromAgeUnit(ageUnitDTO))

            }

            if (isEnabled) {
                binding.etNum.setOnClickListener {
                    val calNow = Calendar.getInstance()
                    val calMin = Calendar.getInstance()
                    val calMax = Calendar.getInstance()
                    item.min?.let {
                        calMin.timeInMillis = it
                    }
                    item.max?.let {
                        calMax.timeInMillis = it
                    }

                    agePicker.setLimitsAndShow(
                        calNow.get(Calendar.YEAR) - calMax.get(Calendar.YEAR),
                        calNow.get(Calendar.YEAR) - calMin.get(Calendar.YEAR),
                        0,
                        11,
                        0,
                        30,
                        ageUnitDTO,
                        isOk
                    )
                }
                agePicker.setOnDismissListener {
                    binding.etNum.setText(getAgeStrFromAgeUnit(ageUnitDTO))
                    calDob.timeInMillis =
                        getDobFromAge(ageUnitDTO)
                    binding.etDate.setText(getDateString(calDob.timeInMillis))
                    item.value = getDateString(calDob.timeInMillis)
                    if (item.hasDependants) formValueListener?.onValueChanged(item, -1)
                }
            }


            if (!isEnabled) {
                binding.etDate.isFocusable = false
                binding.etNum.isFocusable = false
                binding.etDate.isClickable = false
                binding.etNum.isClickable = false
                binding.executePendingBindings()
                return
            }
            val today = Calendar.getInstance()
            var thisYear = today.get(Calendar.YEAR)
            var thisMonth = today.get(Calendar.MONTH)
            var thisDay = today.get(Calendar.DAY_OF_MONTH)

            item.errorText?.also { binding.tilEditTextDate.error = it }
                ?: run { binding.tilEditTextDate.error = null }
            binding.etDate.setOnClickListener {
                item.value?.let { value ->
                    thisYear = value.substring(6).toInt()
                    thisMonth = value.substring(3, 5).trim().toInt() - 1
                    thisDay = value.substring(0, 2).trim().toInt()
                }
                val datePickerDialog = DatePickerDialog(
                    it.context, { _, year, month, day ->
                        val millisCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        val millis = millisCal.timeInMillis
                        if (item.min != null && millis < item.min!!) {
                            item.value = getDateString(item.min)
                        } else if (item.max != null && millis > item.max!!)
                            item.value = getDateString(item.max)
                        else
                            item.value = getDateString(millis)

                        updateAgeDTO(ageUnitDTO, millisCal)
                        binding.etNum.setText(getAgeStrFromAgeUnit(ageUnitDTO))
                        binding.invalidateAll()
                        if (item.hasDependants) formValueListener?.onValueChanged(item, -1)
                    }, thisYear, thisMonth, thisDay
                )
                item.errorText = null
                binding.tilEditTextDate.error = null
                datePickerDialog.datePicker.maxDate = item.max ?: 0
                datePickerDialog.datePicker.minDate = item.min ?: 0
                if (item.showYearFirstInDatePicker)
                    datePickerDialog.datePicker.touchables[0].performClick()
                datePickerDialog.show()
            }
            binding.executePendingBindings()

        }

    }

    class HeadlineViewHolder private constructor(private val binding: RvItemFormHeadlineV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormHeadlineV2Binding.inflate(layoutInflater, parent, false)
                return HeadlineViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement,
            formValueListener: FormValueListener?,
        ) {
            binding.form = item
            if (item.subtitle == null)
                binding.textView8.visibility = View.GONE
            formValueListener?.onValueChanged(item, -1)
            binding.executePendingBindings()

        }
    }

    class ImageClickListener(private val imageClick: (formId: Int) -> Unit) {

        fun onImageClick(form: FormElement) = imageClick(form.id)

    }

    class AgeClickListener(private val ageClick: (formId: Int) -> Unit) {

        fun onAgeClick(form: FormElement) = ageClick(form.id)

    }

    class FormValueListener(private val valueChanged: (id: Int, value: Int) -> Unit) {

        fun onValueChanged(form: FormElement, index: Int) {
            valueChanged(form.id, index)

        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inputTypes = values()
        return when (inputTypes[viewType]) {
            EDIT_TEXT -> EditTextInputViewHolder.from(parent)
            DROPDOWN -> DropDownInputViewHolder.from(parent)
            RADIO -> RadioInputViewHolder.from(parent)
            DATE_PICKER -> DatePickerInputViewHolder.from(parent)
            TEXT_VIEW -> TextViewInputViewHolder.from(parent)
            IMAGE_VIEW -> ImageViewInputViewHolder.from(parent)
            CHECKBOXES -> CheckBoxesInputViewHolder.from(parent)
            TIME_PICKER -> TimePickerInputViewHolder.from(parent)
            HEADLINE -> HeadlineViewHolder.from(parent)
            AGE_PICKER -> AgePickerViewInputViewHolder.from(parent)
            BUTTON -> ButtonInputViewHolder.from(parent)
            FILE_UPLOAD -> FileUploadInputViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val isEnabled = if (isEnabled) item.isEnabled else false
        when (item.inputType) {
            EDIT_TEXT -> (holder as EditTextInputViewHolder).bind(
                item, isEnabled, formValueListener
            )

            DROPDOWN -> (holder as DropDownInputViewHolder).bind(item, isEnabled, formValueListener)
            RADIO -> (holder as RadioInputViewHolder).bind(item, isEnabled, formValueListener)
            DATE_PICKER -> (holder as DatePickerInputViewHolder).bind(
                item, isEnabled, formValueListener
            )

            TEXT_VIEW -> (holder as TextViewInputViewHolder).bind(item)
            IMAGE_VIEW -> (holder as ImageViewInputViewHolder).bind(
                item, imageClickListener, isEnabled
            )

            CHECKBOXES -> (holder as CheckBoxesInputViewHolder).bind(
                item,
                isEnabled,
                formValueListener
            )

            TIME_PICKER -> (holder as TimePickerInputViewHolder).bind(item, isEnabled)
            HEADLINE -> (holder as HeadlineViewHolder).bind(item, formValueListener)
            AGE_PICKER -> (holder as AgePickerViewInputViewHolder).bind(
                item,
                isEnabled,
                formValueListener
            )
            BUTTON -> (holder as ButtonInputViewHolder).bind(item, isEnabled,formValueListener)
            FILE_UPLOAD -> (holder as FileUploadInputViewHolder).bind(item,selectImageClickListener,viewDocumentListner, isEnabled)

        }
    }

    override fun getItemViewType(position: Int) = getItem(position).inputType.ordinal

    /**
     * Validation Result : -1 -> all good
     * else index of element creating trouble
     */
    fun validateInput(resources: Resources): Int {
        var retVal = -1
        if (!isEnabled) return retVal
        currentList.forEachIndexed { index, it ->
            Timber.d("Error text for ${it.title} ${it.errorText}")
            if (it.inputType != TEXT_VIEW && it.errorText != null) {
                retVal = index
                return@forEachIndexed
            }
        }
        Timber.d("Validation : $retVal")
        if (retVal != -1) return retVal
        currentList.forEachIndexed { index, it ->
            if (it.inputType != TEXT_VIEW && it.required) {
                if (it.value.isNullOrBlank()) {
                    Timber.d("validateInput called for item $it, with index ${index}")
                    it.errorText = resources.getString(R.string.form_input_empty_error)
                    notifyItemChanged(index)
                    if (retVal == -1) retVal = index
                }
            }
        }
        return retVal
    }



}