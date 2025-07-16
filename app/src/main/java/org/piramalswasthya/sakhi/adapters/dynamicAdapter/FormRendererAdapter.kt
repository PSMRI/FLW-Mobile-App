package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormRendererAdapter(
    private val fields: MutableList<FormField>,
    private val isViewOnly: Boolean = false,
    private val onValueChanged: (FormField, Any?) -> Unit
) : RecyclerView.Adapter<FormRendererAdapter.FormViewHolder>() {

    fun getUpdatedFields(): List<FormField> = fields

    fun updateFields(newFields: List<FormField>) {
        if (fields.size != newFields.size) {
            fields.clear()
            fields.addAll(newFields)
            notifyDataSetChanged()
            return
        }

        newFields.forEachIndexed { index, newField ->
            val oldField = fields.getOrNull(index)

            // Compare visibility, value, error message
            val shouldUpdate = oldField == null ||
                    oldField.value != newField.value ||
                    oldField.errorMessage != newField.errorMessage ||
                    oldField.visible != newField.visible

            if (shouldUpdate) {
                fields[index] = newField
                Log.d("NotifyUpdate", "Updating field: ${newField.fieldId}, visible=${newField.visible}, error=${newField.errorMessage}")
                notifyItemChanged(index)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_form_field, parent, false)
        return FormViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        val field = fields[position]
        holder.itemView.visibility = View.VISIBLE // Always let bind() control it
        try {
            holder.bind(field)
        } catch (e: Exception) {
            Log.e("FormAdapter", "Error binding fieldId=${field.fieldId}: ${e.message}")
        }
    }

    override fun getItemCount(): Int = fields.size

    inner class FormViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val label: TextView = view.findViewById(R.id.tvLabel)
        private val inputContainer: ViewGroup = view.findViewById(R.id.inputContainer)

        fun bind(field: FormField) {
//            itemView.visibility = if (field.visible || isViewOnly) View.VISIBLE else View.GONE

            itemView.visibility = View.VISIBLE
            if (!field.visible) {
                itemView.visibility = View.GONE
                return
            } else {
                itemView.visibility = View.VISIBLE
            }
            if (field.isRequired) {
                val labelText = "${field.label} *"
                Log.d("BindLabel", "Label yes: ${label.text}")
                val spannable = SpannableString(labelText)
                spannable.setSpan(
                    ForegroundColorSpan(Color.RED),
                    labelText.length - 1,
                    labelText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                label.text = spannable
            } else {
                Log.d("BindLabel", "Label no: ${label.text}")
                label.text = field.label
            }


            inputContainer.removeAllViews()

            fun addWithError(inputView: View, field: FormField) {
                val wrapper = LinearLayout(itemView.context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                wrapper.addView(inputView)

                val errorTextView = TextView(itemView.context).apply {
                    setTextColor(android.graphics.Color.RED)
                    textSize = 12f
                    text = field.errorMessage ?: ""
                    visibility = if (field.errorMessage.isNullOrBlank()) View.GONE else View.VISIBLE
                }

                wrapper.addView(errorTextView)

                inputContainer.removeAllViews()
                inputContainer.addView(wrapper)

                Log.d("RenderError", "Field: ${field.label}, Error: ${field.errorMessage}")
            }

            when (field.type) {
                "text" -> {
                    val context = itemView.context

                    val textInputLayout = TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 16, 0, 8)
                        }

                        hint = field.placeholder ?: "Enter ${field.label}"
                        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                        boxStrokeColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
                        boxStrokeWidthFocused = 2
                        setBoxCornerRadii(12f, 12f, 12f, 12f)
                    }

                    val editText = TextInputEditText(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(32, 24, 32, 24)

                        background = null
                        setText(field.value as? String ?: "")
                        inputType = InputType.TYPE_CLASS_TEXT
                        isEnabled = !isViewOnly
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    }

                    if (!isViewOnly) {
                        editText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                field.value = s.toString()
                                onValueChanged(field, s.toString())
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        })
                    }

                    textInputLayout.addView(editText)
                    addWithError(textInputLayout, field)
                }



                "number" -> {
                    val context = itemView.context

                    val textInputLayout = TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 16, 0, 8)
                        }

                        hint = field.placeholder ?: "Enter ${field.label}"
                        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                        boxStrokeColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
                        boxStrokeWidthFocused = 2
                        setBoxCornerRadii(12f, 12f, 12f, 12f)
                    }

                    val editText = TextInputEditText(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(32, 24, 32, 24)

                        background = null
                        setText((field.value as? Number)?.toString() ?: "")
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                        isEnabled = !isViewOnly
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    }

                    if (!isViewOnly) {
                        editText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                val value = s.toString().toFloatOrNull()
                                field.value = value
                                onValueChanged(field, value)
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        })
                    }

                    textInputLayout.addView(editText)
                    addWithError(textInputLayout, field)
                }


                "dropdown" -> {
                    val context = itemView.context
                    val isEditableField  = field.fieldId != "visit_day" && field.isEditable && !isViewOnly

                    val textInputLayout = TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 16, 0, 8)
                        }
                        hint = field.placeholder ?: "Select ${field.label}"
                        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                        boxStrokeColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
                        boxStrokeWidthFocused = 2
                        setBoxCornerRadii(12f, 12f, 12f, 12f)
                    }

                    val editText = TextInputEditText(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        isFocusable = false
                        isClickable = isEditableField
                        isEnabled = isEditableField
                        setText(field.value?.toString() ?: "")
                        background = null
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                        setPadding(16, 24, 16, 24)

                        // 🟡 Show dropdown icon in active or gray state
                        val dropdownIcon = AppCompatResources.getDrawable(
                            context,
                            R.drawable.ic_arrow_drop_down
                        )
                        dropdownIcon?.setTint(
                            ContextCompat.getColor(
                                context,
                                if (isEnabled) R.color.md_theme_light_primary else android.R.color.darker_gray
                            )
                        )
                        setCompoundDrawablesWithIntrinsicBounds(null, null, dropdownIcon, null)
                        compoundDrawablePadding = 16
                    }

                    if (isEditableField) {
                        editText.setOnClickListener {
                            val options = field.options ?: emptyList()
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Select ${field.label}")
                            builder.setItems(options.toTypedArray()) { _, which ->
                                val selected = options[which]
                                editText.setText(selected)
                                field.value = selected
                                onValueChanged(field, selected)
                            }
                            builder.show()
                        }
                    }

                    textInputLayout.addView(editText)
                    addWithError(textInputLayout, field)
                }


                "date" -> {
                    val context = itemView.context
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val today = Calendar.getInstance().time
                    val todayStr = sdf.format(today)

                    // Set default today's date for visit_date if blank
                    if (field.fieldId == "visit_date" && (field.value == null || (field.value as? String)?.isBlank() == true)) {
                        field.value = todayStr
                    }

                    val textInputLayout = TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 16, 0, 8)
                        }
                        hint = field.placeholder ?: "Select ${field.label}"
                        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                        boxStrokeColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
                        boxStrokeWidthFocused = 2
                        setBoxCornerRadii(12f, 12f, 12f, 12f)
                    }

                    val editText = TextInputEditText(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(32, 24, 32, 24)
                        background = null
                        setText(field.value as? String ?: "")
                        isFocusable = false
                        isClickable = true
                        isEnabled = !isViewOnly
                        setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            ContextCompat.getDrawable(context, R.drawable.ic_calendar),
                            null
                        )
                        compoundDrawablePadding = 24
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    }

                    textInputLayout.addView(editText)

                    if (!isViewOnly) {
                        editText.setOnClickListener {
                            val calendar = Calendar.getInstance()

                            // 🔁 Check fieldId
                            val (minDate, maxDate) = if (field.fieldId == "visit_date") {
                                today to today
                            } else {
                                val minDateStr = field.validation?.minDate
                                val maxDateStr = field.validation?.maxDate

                                val min = when (minDateStr) {
                                    "today" -> today
                                    "dob", "due_date" -> {
                                        val ref = fields.find { it.fieldId == minDateStr }
                                        (ref?.value as? String)?.let { sdf.parse(it) }
                                    }
                                    else -> minDateStr?.let { sdf.parse(it) }
                                }

                                val max = when (maxDateStr) {
                                    "today" -> today
                                    "dob", "due_date" -> {
                                        val ref = fields.find { it.fieldId == maxDateStr }
                                        (ref?.value as? String)?.let { sdf.parse(it) } ?: today
                                    }
                                    else -> maxDateStr?.let { sdf.parse(it) } ?: today
                                }

                                min to max
                            }

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val dateStr = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                                    editText.setText(dateStr)
                                    field.value = dateStr
                                    onValueChanged(field, dateStr)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).apply {
                                minDate?.let { datePicker.minDate = it.time }
                                maxDate?.let { datePicker.maxDate = it.time }
                            }.show()
                        }
                    }

                    addWithError(textInputLayout, field)
                }




                "radio" -> {
                    val context = itemView.context

                    val radioGroup = RadioGroup(context).apply {
                        orientation = RadioGroup.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                    }

                    field.options?.forEachIndexed { index, option ->
                        val radioButton = RadioButton(context).apply {
                            text = option
                            isChecked = field.value == option
                            isEnabled = !isViewOnly

                            // 🟡 Add horizontal spacing between buttons
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, if (index != field.options!!.lastIndex) 24 else 0, 0) // spacing between radios
                            }
                        }

                        if (!isViewOnly) {
                            radioButton.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked && field.value != option) {
                                    field.value = option
                                    onValueChanged(field, option)
                                }
                            }
                        }

                        radioGroup.addView(radioButton)
                    }

                    addWithError(radioGroup, field)
                }


                "image" -> {
                    val context = itemView.context

                    val container = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                    }

                    val imageView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(300, 300)
                        scaleType = ImageView.ScaleType.CENTER_CROP

                        val filePath = field.value?.toString()
                        if (!filePath.isNullOrBlank()) {
                            try {
                                val uri = Uri.parse(filePath)
                                setImageURI(uri)
                                Log.d("ImageLoad", "Loaded image from URI: $filePath")
                            } catch (e: Exception) {
                                Log.e("ImageLoad", "Failed to load image from URI: $filePath", e)
                            }
                        } else {
                            setImageResource(R.drawable.ic_person) // Optional fallback
                        }
                    }

                    val pickButton = Button(context).apply {
                        text = "Pick Image"
                        isEnabled = !isViewOnly
                        setOnClickListener {
                            if (!isViewOnly) {
                                onValueChanged(field, "pick_image")
                            }
                        }
                    }

                    container.addView(imageView)
                    if (!isViewOnly) container.addView(pickButton)

                    addWithError(container, field)
                }


                else -> {
                    inputContainer.addView(TextView(itemView.context).apply {
                        text = field.value?.toString() ?: ""
                        textSize = 16f
                    })
                }
            }
        }
    }
    private fun createOutlinedInputLayout(context: Context, hint: String): TextInputLayout {
        return TextInputLayout(context, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            this.hint = hint
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxStrokeColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
            boxStrokeWidthFocused = 2
            setBoxCornerRadii(12f, 12f, 12f, 12f) // rounded corners
            setPadding(12, 12, 12, 12)
        }
    }

}
