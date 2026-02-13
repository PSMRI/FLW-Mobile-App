package org.piramalswasthya.sakhi.adapters

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.LayoutMultiFileUploadBinding
import org.piramalswasthya.sakhi.databinding.LayoutUploafFormBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormAgePickerViewV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormBtnBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormCheckV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormDatepickerV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormDropdownV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormEditTextV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormHeadlineV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormImageViewV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormNumberPickerBinding
import org.piramalswasthya.sakhi.databinding.RvItemFormRadioV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormTextViewV2Binding
import org.piramalswasthya.sakhi.databinding.RvItemFormTimepickerV2Binding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.helpers.isInternetAvailable
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.AGE_PICKER
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
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.AgePickerDialog
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.NewBenRegViewModel.Companion.isOtpVerified
import org.piramalswasthya.sakhi.utils.HelperUtil.getAgeStrFromAgeUnit
import org.piramalswasthya.sakhi.utils.HelperUtil.getDobFromAge
import org.piramalswasthya.sakhi.utils.HelperUtil.getLongFromDate
import org.piramalswasthya.sakhi.utils.HelperUtil.updateAgeDTO
import org.piramalswasthya.sakhi.utils.Log
import timber.log.Timber
import java.util.Calendar


class FormInputAdapter(
    private val imageClickListener: ImageClickListener? = null,
    private val ageClickListener: AgeClickListener? = null,
    private val sendOtpClickListener: SendOtpClickListener? = null,
    private val formValueListener: FormValueListener? = null,
    private val isEnabled: Boolean = true,
    private val selectImageClickListener: SelectUploadImageClickListener? = null,
    private val viewDocumentListner: ViewDocumentOnClick? = null,
    var  fileList: MutableList<Uri>? = null,
) : ListAdapter<FormElement, ViewHolder>(FormInputDiffCallBack) {
    var disableUpload = false

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
            } else {
                binding.et.isClickable = true
                binding.et.isFocusable = true
                binding.et.isFocusableInTouchMode = true
            }
            if (isOtpVerified && item.id == 44 && item.title.equals("Contact Number")) {
                binding.et.isClickable = false
                binding.et.isFocusable = false
            }

            if (item.title.contains("first name", true) ||
                item.title.contains("last name", true) ||
                item.title.contains("father's name", true) ||
                item.title.contains("mother's name", true)
            ) {
//                edittext.setFilters(arrayOf<InputFilter>(AllCaps()))
                val editFilters = binding.et.filters
                var newFilters = arrayOfNulls<InputFilter>(editFilters.size + 1)
                editFilters.forEachIndexed { index, inputFilter ->
                    newFilters[index] = editFilters[index]
                }
                newFilters[editFilters.size] = AllCaps()
//                newFilters.set(editFilters.size, AllCaps())
//                binding.et.filters = arrayOf<InputFilter>(AllCaps())
                binding.et.filters = newFilters
            }
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


            //binding.et.setText(item.value.value)
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
//                    editable?.length?.let {
//                        if (it > item.etMaxLength) {
////                            editable.delete(item.etMaxLength + 1, it)
//                            "This field cannot have more than ${item.etMaxLength} characters".let {
//                                item.errorText = it
//                                binding.tilEditText.error = it
//                            }
//                            return
//                        } else
//                            item.errorText = null
//                    }
                    item.value = editable?.toString()
                    Timber.d("editable : $editable Current value : ${item.value}  isNull: ${item.value == null} isEmpty: ${item.value == ""}")
                    formValueListener?.onValueChanged(item, -1)
                    if (item.errorText != binding.tilEditText.error) {
                        binding.tilEditText.isErrorEnabled = item.errorText != null
                        binding.tilEditText.error = item.errorText
                    }
//                    binding.tilEditText.error = null
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
                if (hasFocus){
                    binding.et.requestFocus()
                    binding.et.addTextChangedListener(textWatcher)
                    val imm =
                        binding.root.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                } else {
                    binding.et.removeTextChangedListener(textWatcher)
                    binding.et.clearFocus()
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
            binding.tilRvDropdown.clearFocus()
            binding.tilEditText.clearFocus()
            binding.et.clearFocus()
            binding.actvRvDropdown.clearFocus()
            binding.actvRvDropdown.showSoftInputOnFocus = false
            binding.form = item
            if (item.errorText == null) {
                binding.tilRvDropdown.error = null
                binding.tilRvDropdown.isErrorEnabled = false
            }
            if (!isEnabled) {
                binding.tilRvDropdown.visibility = View.GONE
                binding.tilEditText.visibility = View.VISIBLE
                binding.et.isFocusable = false
                binding.et.isClickable = false
                binding.executePendingBindings()
                return
            }

            hideKeyboardImmediately()


            binding.actvRvDropdown.setOnItemClickListener { _, _, index, _ ->
                hideKeyboardWithRetry()
                item.value = item.entries?.get(index)
                Timber.d("Item DD : $item")
//                if (item.hasDependants || item.hasAlertError) {
                formValueListener?.onValueChanged(item, index)
//                }
                binding.tilRvDropdown.isErrorEnabled = item.errorText != null
                binding.tilRvDropdown.error = item.errorText
            }

            binding.actvRvDropdown.setOnClickListener {
                hideKeyboardWithRetry()
            }

            item.errorText?.let { binding.tilRvDropdown.error = it }
            binding.executePendingBindings()

        }

        private fun hideKeyboardImmediately() {
            val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }

        private fun hideKeyboardWithRetry() {
            val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

            binding.root.postDelayed({
                imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            }, 50)

            binding.root.postDelayed({
                imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            }, 200)
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
                                binding.root.clearFocus()
                                val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
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
            val str = binding.tvNullable.text
//            val str = binding.tvNullableHr.text
            val spannableString = SpannableString(str)

            val colorSpan = ForegroundColorSpan(Color.parseColor("#B00020"))
            val sizeSpan = RelativeSizeSpan(1.2f)

            if (item.required && item.doubleStar) {
                spannableString.setSpan(
                    colorSpan,
                    str.length - 2,
                    str.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
//                spannableString.setSpan(sizeSpan, str.length - 2, str.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.tvNullable.text = spannableString
                binding.tvNullableHr.text = spannableString
            } else if (item.required) {
                spannableString.setSpan(
                    colorSpan,
                    str.length - 1,
                    str.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
//                spannableString.setSpan(sizeSpan, str.length - 1, str.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            item: FormElement,
            isEnabled: Boolean,
            formValueListener: FormValueListener?
        ) {
            binding.form = item
            binding.llChecks.removeAllViews()

            val selectedIndexes = item.value
                ?.split("|")
                ?.mapNotNull { it.toIntOrNull() }
                ?.toMutableSet()
                ?: mutableSetOf()

            item.entries?.forEachIndexed { index, text ->

                val cbx = CheckBox(binding.root.context)
                cbx.text = text
                cbx.isEnabled = isEnabled
                cbx.isChecked = selectedIndexes.contains(index)
                cbx.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked) {
                        selectedIndexes.add(index)
                    } else {
                        selectedIndexes.remove(index)
                    }

                    item.value =
                        if (selectedIndexes.isEmpty()) null
                        else selectedIndexes.sorted().joinToString("|")

                    item.errorText = null
                    binding.clRi.setBackgroundResource(0)

                    formValueListener?.onValueChanged(item, index)
                }

                binding.llChecks.addView(cbx)
            }

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

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: SendOtpClickListener?) {
            binding.form = item
            isOtpVerified(isEnabled, isInternetAvailable(binding.root.context))

            binding.generateOtp.setOnClickListener {
                formValueListener!!.onButtonClick(item,binding.generateOtp,binding.timerInSec,binding.tilEditText,isEnabled,adapterPosition,binding.et)
            }



        }

        private fun isOtpVerified(isEnabled: Boolean, internetAvailable: Boolean) {
            if(isOtpVerified) {
                binding.generateOtp.text = binding.generateOtp.resources.getString(R.string.verified)
                binding.generateOtp.isEnabled = isEnabled

            } else {
                binding.generateOtp.text = binding.generateOtp.resources.getString(R.string.send_otp)
                if (internetAvailable){
                    binding.generateOtp.isEnabled = isEnabled

                } else {
                    binding.generateOtp.isEnabled = !isEnabled

                }

            }
        }


    }



    class SelectUploadImageClickListener(private val selectImageClick: (formId: Int) -> Unit) {

        fun onSelectImageClick(form: FormElement) = selectImageClick(form.id)

    }

    class ViewDocumentOnClick(private val viewDocument: (formId: Int) -> Unit) {

        fun onViewDocumentClick(form: FormElement) = viewDocument(form.id)

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
                    item.errorText = null
                    binding.tilEditTextDate.error = null
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

    class SendOtpClickListener(private val btnClick: (formId: Int,generateOtp:MaterialButton,timerInsec: TextView,tilEditText:TextInputLayout, isEnabled: Boolean,adapterPosition:Int,otpField: TextInputEditText) -> Unit) {

        fun onButtonClick(
            form: FormElement,
            generateOtp: MaterialButton,
            timerInSec: TextView,
            tilEditText: TextInputLayout,
            isEnabled: Boolean,
            adapterPosition: Int,
            otpField: TextInputEditText
        ) = btnClick(form.id,generateOtp,timerInSec,tilEditText,isEnabled,adapterPosition,otpField)

    }

    class AgeClickListener(private val ageClick: (formId: Int) -> Unit) {

        fun onAgeClick(form: FormElement) = ageClick(form.id)

    }

    class FormValueListener(private val valueChanged: (id: Int, value: Int) -> Unit) {

        fun onValueChanged(form: FormElement, index: Int) {
            valueChanged(form.id, index)

        }

    }
    class NumberPickerInputViewHolder private constructor(
        private val binding: RvItemFormNumberPickerBinding
    ) : ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null
        private var internalUpdate = false

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormNumberPickerBinding.inflate(layoutInflater, parent, false)
                return NumberPickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement,
            isEnabled: Boolean,
            formValueListener: FormValueListener?
        ) {
            binding.form = item

            binding.etNumberInput.isEnabled = isEnabled
            binding.btnDecrement.isEnabled = isEnabled
            binding.btnIncrement.isEnabled = isEnabled
            if (!isEnabled) {
                hideError()
                return
            }

            val minValue = item.min?.toInt() ?: 0
            val maxValue = item.max?.toInt()
            val allowNegative = item.minDecimal != null && item.minDecimal!! < 0

            binding.etNumberInput.setText(minValue.toString())
            binding.etNumberInput.setSelection(binding.etNumberInput.text!!.length)
            var currentValue = item.value?.toIntOrNull() ?: minValue

            textWatcher?.let { binding.etNumberInput.removeTextChangedListener(it) }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // No action required for this implementation.
                    // This method is implemented to satisfy the interface contract.


                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // No action required for this implementation.
                    // This method is implemented to satisfy the interface contract.

                }




                override fun afterTextChanged(editable: Editable?) {

                    if (internalUpdate) return
                    if (editable.isNullOrBlank()) {
                        item.errorText = "Value cannot be empty"
                        showError(item.errorText!!)
                        return
                    }

                    val inputValue = editable.toString().toIntOrNull()
                    if (inputValue == null) {
                        item.errorText = "Enter a valid number"
                        showError(item.errorText!!)
                        return
                    }

                    val validated = validateValue(inputValue, minValue, maxValue, allowNegative)

                    if (validated != inputValue) {
                        val msg = if (maxValue != null)
                            "Allowed range: $minValue to $maxValue"
                        else
                            "Minimum allowed value: $minValue"
                        item.errorText = msg
                        showError(msg)

                        updateDisplay(validated)
                        updateValue(validated, item, formValueListener)
                        return
                    }

                    item.errorText = null
                    hideError()

                    currentValue = validated
                    updateValue(currentValue, item, formValueListener)
                }
            }

            updateDisplay(currentValue)

            binding.etNumberInput.addTextChangedListener(textWatcher)



            binding.btnDecrement.setOnClickListener {
                currentValue = (item.value?.toIntOrNull() ?: minValue) - 1
                currentValue = validateValue(currentValue, minValue, maxValue, allowNegative)
                hideError()
                updateValue(currentValue, item, formValueListener)
                updateDisplay(currentValue)
            }

            binding.btnIncrement.setOnClickListener {
                currentValue = (item.value?.toIntOrNull() ?: minValue) + 1
                currentValue = validateValue(currentValue, minValue, maxValue, allowNegative)
                hideError()
                updateValue(currentValue, item, formValueListener)
                updateDisplay(currentValue)
            }
        }


        private fun validateValue(value: Int, min: Int, max: Int?, allowNegative: Boolean): Int {
            var newValue = value

            if (!allowNegative && newValue < min) newValue = min
            if (max != null && newValue > max) newValue = max

            return newValue
        }

        private fun updateDisplay(value: Int) {
            internalUpdate = true
            binding.etNumberInput.setText(value.toString())
            binding.etNumberInput.setSelection(binding.etNumberInput.text!!.length)
            internalUpdate = false

        }

        private fun updateValue(
            newValue: Int,
            item: FormElement,
            formValueListener: FormValueListener?
        ) {
            item.value = newValue.toString()
            formValueListener?.onValueChanged(item,newValue)
        }

        private fun showError(message: String) {
            binding.tvError.apply {
                text = message
                visibility = View.VISIBLE
            }
        }

        private fun hideError() {
            binding.tvError.visibility = View.GONE
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inputTypes = values()
        val safeType = inputTypes.getOrNull(viewType) ?: TEXT_VIEW

        return when (safeType) {
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
            InputType.BUTTON -> ButtonInputViewHolder.from(parent)
            InputType.FILE_UPLOAD -> FileUploadInputViewHolder.from(parent)
            InputType.NUMBER_PICKER -> NumberPickerInputViewHolder.from(parent)
            InputType.MULTIFILE_UPLOAD -> MultiFileUploadInputViewHolder.from(parent)
        }
    }

    fun updateFileList(newList: List<Uri>) {
        this.fileList?.addAll(newList)
        notifyDataSetChanged()
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

        private lateinit var fileAdapter: FileListAdapter

        fun bind(
            item: FormElement,
            clickListener: SelectUploadImageClickListener?,
            documentOnClick: ViewDocumentOnClick?,
            isEnabled: Boolean,
            fileList : MutableList<Uri>?
        ) {
            val items = fileList ?: mutableListOf()
            fileAdapter = FileListAdapter(items)
            binding.rvFiles.adapter = fileAdapter
            fileAdapter.updateFileList(items)
            fileAdapter.notifyDataSetChanged()

            binding.btnSelectFiles.isEnabled = isEnabled
            binding.btnSelectFiles.alpha = if (isEnabled) 1f else 0.5f

            binding.btnSelectFiles.setOnClickListener {
                clickListener?.onSelectImageClick(item)
            }
        }

    }



    class FileUploadInputViewHolder private constructor(private val binding: LayoutUploafFormBinding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutUploafFormBinding.inflate(layoutInflater, parent, false)
                return FileUploadInputViewHolder(binding)
            }
        }


        fun bind(
            item: FormElement,
            clickListener: SelectUploadImageClickListener?,
            documentOnClick: ViewDocumentOnClick?,
            isEnabled: Boolean
        ) {
            binding.form = item
            binding.tvTitle.text = item.title
            binding.clickListener = clickListener
            binding.documentclickListener = documentOnClick
            binding.btnView.visibility = if (item.value != null) View.VISIBLE else View.GONE

            if (isEnabled) {
                binding.addFile.visibility = View.VISIBLE
//                binding.addFile.isEnabled = true
//                binding.addFile.alpha = 1f
            } else {
                binding.addFile.visibility = View.GONE
//                binding.addFile.isEnabled = false
//                binding.addFile.alpha = 0.5f
            }
        }

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
//        I was getting crash that's why i have moved this line inside try block
//        val isEnabled = if (isEnabled) item.isEnabled else false
        try {
            val isEnabled = if (isEnabled) item.isEnabled else false
            when (item.inputType) {
                EDIT_TEXT -> (holder as EditTextInputViewHolder).bind(
                    item, isEnabled, formValueListener
                )

                DROPDOWN -> (holder as DropDownInputViewHolder).bind(
                    item,
                    isEnabled,
                    formValueListener
                )

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
                InputType.BUTTON -> (holder as ButtonInputViewHolder).bind(
                    item,
                    isEnabled,
                    sendOtpClickListener
                )

                InputType.FILE_UPLOAD -> (holder as FileUploadInputViewHolder).bind(item,selectImageClickListener,viewDocumentListner,isEnabled = !disableUpload)
                org.piramalswasthya.sakhi.model.InputType.MULTIFILE_UPLOAD -> (holder as MultiFileUploadInputViewHolder).bind(item,selectImageClickListener,viewDocumentListner,isEnabled = !disableUpload,fileList)

                InputType.NUMBER_PICKER -> (holder as NumberPickerInputViewHolder).bind(
                    item, isEnabled, formValueListener
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    //override fun getItemViewType(position: Int) = getItem(position).inputType.ordinal
    //THis changes done to solve crashalytics issue
    override fun getItemViewType(position: Int): Int {
        if (position < 0 || position >= itemCount) {
            return TEXT_VIEW.ordinal // fallback view type
        }

        val item = getItem(position) ?: return TEXT_VIEW.ordinal
        return item.inputType.ordinal
    }
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
            /*            if(it.regex!=null){
                            Timber.d("Regex not null")
                            retVal= false
                        }*/
        }
        return retVal
    }



}