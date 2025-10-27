package org.piramalswasthya.sakhi.adapters

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.button.MaterialButton
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter.SelectUploadImageClickListener
import org.piramalswasthya.sakhi.adapters.FormInputAdapter.ViewDocumentOnClick
import org.piramalswasthya.sakhi.configuration.FormEditTextDefaultInputFilter
import org.piramalswasthya.sakhi.databinding.RvItemFormBtnBinding
import org.piramalswasthya.sakhi.databinding.LayoutMultiFileUploadBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormCheckBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormDatepickerBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormDropdownBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormEditTextBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormHeadlineBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormImageViewBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormRadioBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormTextViewBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormTimepickerBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormUploadImageBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.FormInputOld
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
import timber.log.Timber
import java.util.Calendar

class FormInputAdapterOld(
    private val imageClickListener: ImageClickListener? = null,
    private val isEnabled: Boolean = true
) :
    ListAdapter<FormInputOld, ViewHolder>(FormInputDiffCallBack) {
    object FormInputDiffCallBack : DiffUtil.ItemCallback<FormInputOld>() {
        override fun areItemsTheSame(oldItem: FormInputOld, newItem: FormInputOld) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: FormInputOld, newItem: FormInputOld) =
            (oldItem == newItem)

    }

    class EditTextInputViewHolder private constructor(private val binding: RvItemFormEditTextBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormEditTextBinding.inflate(layoutInflater, parent, false)
                return EditTextInputViewHolder(binding)
            }
        }

        fun bind(item: FormInputOld, isEnabled: Boolean) {

            binding.et.isClickable = isEnabled
            binding.et.isFocusable = isEnabled
            binding.form = item
            if (item.title.length > Konstants.editTextHintLimit) {
                binding.tvHint.visibility = View.VISIBLE
                binding.et.hint = null
                binding.tilEditText.hint = null
                binding.tilEditText.isHintEnabled = false
            } else {
                binding.tvHint.visibility = View.GONE
                binding.tilEditText.isHintEnabled = true
            }
            if (!isEnabled) {
                binding.executePendingBindings()
                return
            }
            //binding.et.setText(item.value.value)
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    if (editable == null || editable.toString() == "") {
                        if (!item.required) {
                            item.errorText = null
                            binding.tilEditText.error = null
                        }
                        item.value.value = null
                        return
                    }
                    editable.let { item.value.value = it.toString() }
                    item.value.value = editable.toString()
                    Timber.d("Item ET : $item")
                    if (item.isMobileNumber) {
                        if (item.etMaxLength == 10) {
                            if (editable.first().toString()
                                    .toInt() < 6 || editable.length != item.etMaxLength
                            ) {
                                item.errorText = "Invalid Mobile Number !"
                                binding.tilEditText.error = item.errorText
                            } else {
                                item.errorText = null
                                binding.tilEditText.error = item.errorText
                            }
                        } else if (item.etMaxLength == 12) {
                            if (editable.first().toString()
                                    .toInt() == 0 || editable.length != item.etMaxLength
                            ) {
                                item.errorText = "Invalid ${item.title} !"
                                binding.tilEditText.error = item.errorText
                            } else {
                                item.errorText = null
                                binding.tilEditText.error = item.errorText
                            }
                        }
                    } else if (item.etInputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                        val entered = editable.toString().toDouble()
                        item.minDecimal?.let {
                            if (entered < it) {
                                binding.tilEditText.error = "Field value has to be at least $it"
                                item.errorText = binding.tilEditText.error.toString()
                            }
                        }
                        item.maxDecimal?.let {
                            if (entered > it) {
                                binding.tilEditText.error =
                                    "Field value has to be less than $it"
                                item.errorText = binding.tilEditText.error.toString()
                            }
                        }
                        if (item.minDecimal != null && item.maxDecimal != null && entered >= item.minDecimal!! && entered <= item.maxDecimal!!) {
                            binding.tilEditText.error = null
                            item.errorText = null
                        }

                    } else if (item.etInputType == (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)) {
                        val age = editable.toString().toLong()
                        item.min?.let {
                            if (age < it) {
                                binding.tilEditText.error = "Field value has to be at least $it"
                                item.errorText = binding.tilEditText.error.toString()
                            }
                        }
                        item.max?.let {
                            if (age > it) {
                                binding.tilEditText.error =
                                    "Field value has to be less than $it"
                                item.errorText = binding.tilEditText.error.toString()
                            }
                        }
                        if (item.min != null && item.max != null && age >= item.min!! && age <= item.max!!) {
                            binding.tilEditText.error = null
                            item.errorText = null
                        }
                    } else {
                        if (item.errorText != null && editable.isNotBlank()) {
                            item.errorText = null
                            binding.tilEditText.error = null
                        }

                    }

                }
            }
            binding.et.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus)
                    binding.et.addTextChangedListener(textWatcher)
                else
                    binding.et.removeTextChangedListener(textWatcher)
            }
            item.errorText?.also { binding.tilEditText.error = it }
                ?: run { binding.tilEditText.error = null }
            val etFilters = mutableListOf<InputFilter>(InputFilter.LengthFilter(item.etMaxLength))
            binding.et.inputType = item.etInputType
            if (item.etInputType == InputType.TYPE_CLASS_TEXT && item.allCaps) {
                etFilters.add(AllCaps())
                etFilters.add(FormEditTextDefaultInputFilter)

            }
//            else if(item.etInputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
//                etFilters.add(DecimalDigitsInputFilter)
            binding.et.filters = etFilters.toTypedArray()
            binding.executePendingBindings()
        }
    }

    class DropDownInputViewHolder private constructor(private val binding: RvItemFormDropdownBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDropdownBinding.inflate(layoutInflater, parent, false)
                return DropDownInputViewHolder(binding)
            }
        }

        fun bind(item: FormInputOld, isEnabled: Boolean) {
            if (!isEnabled) {
                binding.form = item
                binding.tilRvDropdown.visibility = View.GONE
                binding.tilEditText.visibility = View.VISIBLE
                binding.et.isFocusable = false
                binding.et.isClickable = false
//                binding.clContent.isClickable = false

                binding.executePendingBindings()
                return
            }
            val savedValue = item.value.value
            item.value.value = null
            item.value.value = savedValue
            binding.form = item

            binding.actvRvDropdown.setOnItemClickListener { _, _, index, _ ->
                item.value.value = item.entries?.get(index)
                Timber.d("Item DD : $item")
                item.errorText = null
                binding.tilRvDropdown.error = null
            }

            item.errorText?.let { binding.tilRvDropdown.error = it }
            binding.executePendingBindings()

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

        fun bind(item: FormInputOld, isEnabled: Boolean) {


        }
    }


    class MultiFileUploadInputViewHolder private constructor(private val binding: LayoutMultiFileUploadBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutMultiFileUploadBinding.inflate(layoutInflater, parent, false)
                return MultiFileUploadInputViewHolder(binding)
            }
        }
        var selectedFiles = mutableListOf<Uri>()

        private lateinit var fileAdapter: FileListAdapter

        fun bind(
            item: FormElement,
            clickListener: SelectUploadImageClickListener?,
            documentOnClick: ViewDocumentOnClick?,
            isEnabled: Boolean
        ) {
            /* binding.form = item
             binding.tvTitle.text = item.title
             binding.clickListener = clickListener
             binding.documentclickListener = documentOnClick
             binding.btnView.visibility = if (item.value != null) View.VISIBLE else View.GONE

             if (isEnabled) {
                 binding.addFile.isEnabled = true
                 binding.addFile.alpha = 1f
             } else {
                 binding.addFile.isEnabled = false
                 binding.addFile.alpha = 0.5f
             }*/

             fileAdapter = FileListAdapter(selectedFiles)
            binding.rvFiles.adapter = fileAdapter

            binding.btnSelectFiles.isEnabled = isEnabled
            binding.btnSelectFiles.alpha = if (isEnabled) 1f else 0.5f

            binding.btnSelectFiles.setOnClickListener {
                clickListener?.onSelectImageClick(item)
            }

            // Show view button only if images exist
//            binding.btnView.visibility = if (selectedFiles.isNotEmpty()) View.VISIBLE else View.GONE

            /*binding.btnView.setOnClickListener {
                documentOnClick?.onViewDocumentClicked(selectedFiles)
            }*/
        }

        fun updateSelectedFiles(files: List<Uri>) {
            selectedFiles.clear()
            selectedFiles.addAll(files)
            fileAdapter.notifyDataSetChanged()
//            binding.btnView.visibility = if (files.isNotEmpty()) View.VISIBLE else View.GONE

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

        fun bind(item: FormInputOld, isEnabled: Boolean) {

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
    class RadioInputViewHolder private constructor(private val binding: RvItemFormRadioBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormRadioBinding.inflate(layoutInflater, parent, false)
                return RadioInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormInputOld, isEnabled: Boolean
        ) {
            if (!isEnabled) {
                binding.rg.isClickable = false
                binding.rg.isFocusable = false
            }
//            binding.rg.isEnabled = isEnabled
            binding.invalidateAll()
            binding.form = item

            binding.rg.removeAllViews()
            binding.rg.apply {
                item.entries?.let { items ->
                    orientation = item.orientation ?: LinearLayout.HORIZONTAL
                    weightSum = items.size.toFloat()
                    items.forEach {
                        val rdBtn = RadioButton(this.context)
                        rdBtn.layoutParams =
                            RadioGroup.LayoutParams(
                                RadioGroup.LayoutParams.WRAP_CONTENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT,
                                1.0F
                            ).apply {
                                gravity = Gravity.CENTER_HORIZONTAL
                            }
                        rdBtn.id = View.generateViewId()

                        rdBtn.text = it
                        addView(rdBtn)
                        if (item.value.value == it)
                            rdBtn.isChecked = true
                        rdBtn.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                item.value.value = it
                            }
                            item.errorText = null
                            binding.clRi.setBackgroundResource(0)
                        }
                    }
                    item.value.value?.let { value ->
                        children.forEach {
                            if ((it as RadioButton).text == value) {
                                clearCheck()
                                check(it.id)
                            }
                        }
                    }
                }
            }



            if (!isEnabled) {
                binding.rg.children.forEach {
                    it.isClickable = false
                }
            }
            if (item.errorText != null)
                binding.clRi.setBackgroundResource(R.drawable.state_errored)
            else
                binding.clRi.setBackgroundResource(0)

            //item.errorText?.let { binding.rg.error = it }
            binding.executePendingBindings()

        }
    }

    class CheckBoxesInputViewHolder private constructor(private val binding: RvItemFormCheckBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormCheckBinding.inflate(layoutInflater, parent, false)
                return CheckBoxesInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormInputOld
        ) {
            binding.form = item
            if (item.errorText != null)
                binding.clRi.setBackgroundResource(R.drawable.state_errored)
            else
                binding.clRi.setBackgroundResource(0)
            binding.executePendingBindings()

        }
    }

    class DatePickerInputViewHolder private constructor(private val binding: RvItemFormDatepickerBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDatepickerBinding.inflate(layoutInflater, parent, false)
                return DatePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormInputOld, isEnabled: Boolean
        ) {
            binding.form = item
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
                item.value.value?.let { value ->
                    thisYear = value.substring(6).toInt()
                    thisMonth = value.substring(3, 5).trim().toInt() - 1
                    thisDay = value.substring(0, 2).trim().toInt()
                }
                val datePickerDialog = DatePickerDialog(
                    it.context,
                    { _, year, month, day ->
                        item.value.value =
                            "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
                        binding.invalidateAll()
                    }, thisYear, thisMonth, thisDay
                )
                item.errorText = null
                binding.tilEditText.error = null
                datePickerDialog.datePicker.maxDate = item.max ?: 0
                datePickerDialog.datePicker.minDate = item.min ?: 0
                datePickerDialog.datePicker.touchables[0].performClick()
                datePickerDialog.show()
            }
            binding.executePendingBindings()

        }
    }

    class TimePickerInputViewHolder private constructor(private val binding: RvItemFormTimepickerBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTimepickerBinding.inflate(layoutInflater, parent, false)
                return TimePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormInputOld, isEnabled: Boolean
        ) {
            binding.form = item
            binding.et.isEnabled = isEnabled
            binding.et.setOnClickListener {
                val hour: Int
                val minute: Int
                if (item.value.value == null) {
                    val currentTime = Calendar.getInstance()
                    hour = currentTime.get(Calendar.HOUR_OF_DAY)
                    minute = currentTime.get(Calendar.MINUTE)
                } else {
                    hour = item.value.value!!.substringBefore(":").toInt()
                    minute = item.value.value!!.substringAfter(":").toInt()
                    Timber.d("Time picker hour min : $hour $minute")
                }
                val mTimePicker = TimePickerDialog(it.context, { _, hourOfDay, minuteOfHour ->
                    item.value.value =
                        "$hourOfDay:$minuteOfHour"
                    binding.invalidateAll()

                }, hour, minute, false)
                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }
            binding.executePendingBindings()

        }
    }

    class TextViewInputViewHolder private constructor(private val binding: RvItemFormTextViewBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTextViewBinding.inflate(layoutInflater, parent, false)
                return TextViewInputViewHolder(binding)
            }
        }

        fun bind(item: FormInputOld) {
            binding.form = item
            binding.executePendingBindings()
        }
    }

    class ImageViewInputViewHolder private constructor(private val binding: RvItemFormImageViewBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormImageViewBinding.inflate(layoutInflater, parent, false)
                return ImageViewInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormInputOld,
            clickListener: ImageClickListener?,
            isEnabled: Boolean
        ) {
            binding.form = item
            if (isEnabled) {
                binding.clickListener = clickListener
                if (item.errorText != null)
                    binding.clRi.setBackgroundResource(R.drawable.state_errored)
                else
                    binding.clRi.setBackgroundResource(0)
            }
            binding.executePendingBindings()

        }
    }


    class HeadlineViewHolder private constructor(private val binding: RvItemFormHeadlineBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormHeadlineBinding.inflate(layoutInflater, parent, false)
                return HeadlineViewHolder(binding)
            }
        }

        fun bind(
            item: FormInputOld,
        ) {
            binding.form = item
            binding.executePendingBindings()

        }
    }

    class ImageClickListener(private val imageClick: (form: FormInputOld) -> Unit) {

        fun onImageClick(form: FormInputOld) = imageClick(form)

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
            AGE_PICKER -> FormInputAdapter.AgePickerViewInputViewHolder.from(parent)
            BUTTON -> FormInputAdapter.ButtonInputViewHolder.from(parent)
            FILE_UPLOAD -> FileUploadInputViewHolder.from(parent)
            org.piramalswasthya.sakhi.model.InputType.MULTIFILE_UPLOAD -> MultiFileUploadInputViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (item.inputType) {
            EDIT_TEXT -> (holder as EditTextInputViewHolder).bind(item, isEnabled)
            DROPDOWN -> (holder as DropDownInputViewHolder).bind(item, isEnabled)
            RADIO -> (holder as RadioInputViewHolder).bind(item, isEnabled)
            DATE_PICKER -> (holder as DatePickerInputViewHolder).bind(item, isEnabled)
            TEXT_VIEW -> (holder as TextViewInputViewHolder).bind(item)
            IMAGE_VIEW -> (holder as ImageViewInputViewHolder).bind(
                item,
                imageClickListener,
                isEnabled
            )

            CHECKBOXES -> (holder as CheckBoxesInputViewHolder).bind(item)
            TIME_PICKER -> (holder as TimePickerInputViewHolder).bind(item, isEnabled)
            HEADLINE -> (holder as HeadlineViewHolder).bind(item)
            AGE_PICKER -> null
            BUTTON -> (holder as ButtonInputViewHolder).bind(item, isEnabled)
            FILE_UPLOAD -> (holder as FileUploadInputViewHolder).bind(item, isEnabled)
            org.piramalswasthya.sakhi.model.InputType.MULTIFILE_UPLOAD -> (holder as FileUploadInputViewHolder).bind(item, isEnabled)

        }
    }

    override fun getItemViewType(position: Int) =
        getItem(position).inputType.ordinal

    /**
     * Validation Result : -1 -> all good
     * else index of element creating trouble
     */
    fun validateInput(): Int {
        var retVal = -1
        if (!isEnabled)
            return retVal
        currentList.forEach {
            Timber.d("Error text for ${it.title} ${it.errorText}")
            if (it.errorText != null) {
                retVal = currentList.indexOf(it)
                return@forEach
            }
        }
        Timber.d("Validation : $retVal")
        if (retVal != -1)
            return retVal
        currentList.forEach {
            if (it.required) {
                if (it.value.value.isNullOrBlank()) {
                    Timber.d("validateInput called for item $it, with index ${currentList.indexOf(it)}")
                    it.errorText = "Required field cannot be empty !"
                    notifyItemChanged(currentList.indexOf(it))
                    if (retVal == -1)
                        retVal = currentList.indexOf(it)
                }
            }
            /*            if(it.regex!=null){
                            Timber.d("Regex not null")
                            retVal= false
                        }*/
        }
        return retVal
    }
}