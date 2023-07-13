package org.piramalswasthya.sakhi.adapters

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Resources
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemFormCheckV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormDatepickerV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormDropdownV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormEditTextV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormHeadlineV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormImageViewV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormRadioV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormTextViewV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormTimepickerV2Binding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.CHECKBOXES
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.HEADLINE
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import org.piramalswasthya.sakhi.model.InputType.TIME_PICKER
import org.piramalswasthya.sakhi.model.InputType.values
import timber.log.Timber
import java.util.Calendar

class FormInputAdapter(
    private val imageClickListener: ImageClickListener? = null,
    private val formValueListener: FormValueListener? = null,
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


    class EditTextInputViewHolder private constructor(private val binding: RvItemFormEditTextV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormEditTextV2Binding.inflate(layoutInflater, parent, false)
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
            }
            else {
                binding.et.isClickable = true
                binding.et.isFocusable = true
            }
            binding.form = item
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


            //binding.et.setText(item.value.value)
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
                    if (item.errorText != binding.tilEditText.error) binding.tilEditText.error =
                        item.errorText
//                        binding.tilEditText.error = null
//                    else if(item.errorText!= null && binding.tilEditText.error==null)
//                        binding.tilEditText.error = item.errorText


//                    if(item.etInputType == InputType.TYPE_CLASS_NUMBER && (item.hasDependants|| item.hasAlertError)){
//                        formValueListener?.onValueChanged(item,-1)
//                    }

//                    editable.let { item.value = it.toString() }
//                    item.value = editable.toString()
//                    Timber.d("Item ET : $item")
//                    if (item.isMobileNumber) {
//                        if (item.etMaxLength == 10) {
//                            if (editable.first().toString()
//                                    .toInt() < 6 || editable.length != item.etMaxLength
//                            ) {
//                                item.errorText = "Invalid Mobile Number !"
//                                binding.tilEditText.error = item.errorText
//                            } else {
//                                item.errorText = null
//                                binding.tilEditText.error = item.errorText
//                            }
//                        } else if (item.etMaxLength == 12) {
//                            if (editable.first().toString()
//                                    .toInt() == 0 || editable.length != item.etMaxLength
//                            ) {
//                                item.errorText = "Invalid ${item.title} !"
//                                binding.tilEditText.error = item.errorText
//                            } else {
//                                item.errorText = null
//                                binding.tilEditText.error = item.errorText
//                            }
//                        }
//                    }
//                else if (item.etInputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
//                        val entered = editable.toString().toDouble()
//                        item.minDecimal?.let {
//                            if (entered < it) {
//                                binding.tilEditText.error = "Field value has to be at least $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        item.maxDecimal?.let {
//                            if (entered > it) {
//                                binding.tilEditText.error =
//                                    "Field value has to be less than $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        if (item.minDecimal != null && item.maxDecimal != null && entered >= item.minDecimal!! && entered <= item.maxDecimal!!) {
//                            binding.tilEditText.error = null
//                            item.errorText = null
//                        }

//                    } else if (item.etInputType == (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)) {
//                        val age = editable.toString().toLong()
//                        item.min?.let {
//                            if (age < it) {
//                                binding.tilEditText.error = "Field value has to be at least $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        item.max?.let {
//                            if (age > it) {
//                                binding.tilEditText.error =
//                                    "Field value has to be less than $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        if (item.min != null && item.max != null && age >= item.min!! && age <= item.max!!) {
//                            binding.tilEditText.error = null
//                            item.errorText = null
//                        }
//                    } else {
//                        if (item.errorText != null && editable.isNotBlank()) {
//                            item.errorText = null
//                            binding.tilEditText.error = null
//                        }

//                    }

                }
            }
            binding.et.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) binding.et.addTextChangedListener(textWatcher)
                else binding.et.removeTextChangedListener(textWatcher)
            }
//            item.errorText?.also { binding.tilEditText.error = it }
//                ?: run { binding.tilEditText.error = null }
//            val etFilters = mutableListOf<InputFilter>(InputFilter.LengthFilter(item.etMaxLength))
//            binding.et.inputType = item.etInputType
//            if (item.etInputType == InputType.TYPE_CLASS_TEXT && item.allCaps) {
//                etFilters.add(AllCaps())
//                etFilters.add(FormEditTextDefaultInputFilter)
//            }
//            else if(item.etInputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
//                etFilters.add(DecimalDigitsInputFilter)
//            binding.et.filters = etFilters.toTypedArray()
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

    class DropDownInputViewHolder private constructor(private val binding: RvItemFormDropdownV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDropdownV2Binding.inflate(layoutInflater, parent, false)
                return DropDownInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?) {
            binding.form = item
            if (!isEnabled) {
                binding.tilRvDropdown.visibility = View.GONE
                binding.tilEditText.visibility = View.VISIBLE
                binding.et.isFocusable = false
                binding.et.isClickable = false
                binding.executePendingBindings()
                return
            }

            binding.actvRvDropdown.setOnItemClickListener { _, _, index, _ ->
                item.value = item.entries?.get(index)
                Timber.d("Item DD : $item")
                if (item.hasDependants || item.hasAlertError) {
                    formValueListener?.onValueChanged(item, index)
                }
                item.errorText = null
                binding.tilRvDropdown.error = null
            }

            item.errorText?.let { binding.tilRvDropdown.error = it }
            binding.executePendingBindings()

        }
    }

    class RadioInputViewHolder private constructor(private val binding: RvItemFormRadioV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormRadioV2Binding.inflate(layoutInflater, parent, false)
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
                        rdBtn.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            1.0F
                        ).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                        rdBtn.id = View.generateViewId()

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
//                    item.value?.let { value ->
//                        children.forEach {
//                            if ((it as RadioButton).text == value) {
//                                clearCheck()
//                                check(it.id)
//                            }
//                        }
//                    }
                }
            }



            if (!isEnabled) {
                binding.rg.children.forEach {
                    it.isClickable = false
                }
            }
            if (item.errorText != null) binding.llContent.setBackgroundResource(R.drawable.state_errored)
            else binding.llContent.setBackgroundResource(0)

            //item.errorText?.let { binding.rg.error = it }
            binding.executePendingBindings()

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
//                                    formValueListener?.onValueChanged(
//                                        item, item.entries!!.indexOf(it)
//                                    )
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

    class DatePickerInputViewHolder private constructor(private val binding: RvItemFormDatepickerV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDatepickerV2Binding.inflate(layoutInflater, parent, false)
                return DatePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
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
                item.value?.let { value ->
                    thisYear = value.substring(6).toInt()
                    thisMonth = value.substring(3, 5).trim().toInt() - 1
                    thisDay = value.substring(0, 2).trim().toInt()
                }
                val datePickerDialog = DatePickerDialog(
                    it.context, { _, year, month, day ->
                        item.value =
                            "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
                        binding.invalidateAll()
                        if (item.hasDependants) formValueListener?.onValueChanged(item, -1)
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

    class TimePickerInputViewHolder private constructor(private val binding: RvItemFormTimepickerV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTimepickerV2Binding.inflate(layoutInflater, parent, false)
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

    class TextViewInputViewHolder private constructor(private val binding: RvItemFormTextViewV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTextViewV2Binding.inflate(layoutInflater, parent, false)
                return TextViewInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement) {
            binding.form = item
            binding.executePendingBindings()
        }
    }

    class ImageViewInputViewHolder private constructor(private val binding: RvItemFormImageViewV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormImageViewV2Binding.inflate(layoutInflater, parent, false)
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
            formValueListener?.onValueChanged(item, -1)
            binding.executePendingBindings()

        }
    }

    class ImageClickListener(private val imageClick: (formId: Int) -> Unit) {

        fun onImageClick(form: FormElement) = imageClick(form.id)

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
        currentList.forEach {
            Timber.d("Error text for ${it.title} ${it.errorText}")
            if (it.errorText != null) {
                retVal = currentList.indexOf(it)
                return@forEach
            }
        }
        Timber.d("Validation : $retVal")
        if (retVal != -1) return retVal
        currentList.forEach {
            if (it.required) {
                if (it.value.isNullOrBlank()) {
                    Timber.d("validateInput called for item $it, with index ${currentList.indexOf(it)}")
                    it.errorText = resources.getString(R.string.form_input_empty_error)
                    notifyItemChanged(currentList.indexOf(it))
                    if (retVal == -1) retVal = currentList.indexOf(it)
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