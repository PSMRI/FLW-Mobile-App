    package org.piramalswasthya.sakhi.adapters.dynamicAdapter

    import android.app.AlertDialog
    import android.app.DatePickerDialog
    import android.content.ActivityNotFoundException
    import android.content.Intent
    import android.graphics.BitmapFactory
    import android.graphics.Color
    import android.graphics.Typeface
    import android.net.Uri
    import android.text.*
    import android.text.style.ForegroundColorSpan
    import android.view.*
    import android.util.Base64
    import android.widget.*
    import androidx.appcompat.content.res.AppCompatResources
    import androidx.core.content.ContextCompat
    import androidx.core.content.FileProvider
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.textfield.TextInputEditText
    import com.google.android.material.textfield.TextInputLayout
    import org.json.JSONArray
    import org.piramalswasthya.sakhi.R
    import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
    import timber.log.Timber
    import java.io.File
    import java.io.FileOutputStream
    import java.text.SimpleDateFormat
    import java.util.*

    class FormRendererAdapter(
        private val fields: MutableList<FormField>,
        private val isViewOnly: Boolean = false,
        private val minVisitDate: Date? = null,
        private val maxVisitDate: Date? = null,
        private val onValueChanged: (FormField, Any?) -> Unit,
        private val onShowAlert: ((String, String) -> Unit)? = null


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

                val shouldUpdate = oldField == null ||
                        oldField.value != newField.value ||
                        oldField.errorMessage != newField.errorMessage ||
                        oldField.visible != newField.visible

                if (shouldUpdate) {
                    fields[index] = newField
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
            holder.itemView.visibility = View.VISIBLE
            try {
                holder.bind(field)
            } catch (e: Exception) {
                Timber.tag("FormRendererAdapter").e(e, "Error binding field: " + field.fieldId)
            }
        }

        override fun getItemCount(): Int = fields.size

        inner class FormViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val label: TextView = view.findViewById(R.id.tvLabel)
            private val inputContainer: ViewGroup = view.findViewById(R.id.inputContainer)

            fun bind(field: FormField) {

                itemView.visibility = View.VISIBLE
                if (!field.visible) {
                    itemView.visibility = View.GONE
                    return
                } else {
                    itemView.visibility = View.VISIBLE
                }
                if (field.isRequired) {
                    val labelText = "${field.label} *"
                    val spannable = SpannableString(labelText)
                    spannable.setSpan(
                        ForegroundColorSpan(Color.RED),
                        labelText.length - 1,
                        labelText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    label.text = spannable
                } else {
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

                }

                when (field.type) {
                    "label" -> {
                        val context = itemView.context
                        val value = field.value.toString()

                        if (!value.isNullOrEmpty()) {
                            val textView = TextView(context).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(0, 16, 0, 8)
                                }

                                text = value
                                setTypeface(typeface, Typeface.BOLD)
                                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
                            }

                            addWithError(textView, field)
                        }
                    }

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
                                    val value = s.toString().toFloatOrNull()
                                    field.value = value
                                    onValueChanged(field, s.toString())
                                    if (field.fieldId == "muac" && value != null) {
                                        onShowAlert?.invoke("CHECK_MUAC", value.toString())
                                    }
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
                                    if (field.fieldId == "weight_for_height_status") {
                                        onShowAlert?.invoke("CHECK_WEIGHT_HEIGHT", selected)
                                    }
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

                        if (field.fieldId == "visit_date" && (field.value == null || (field.value as? String)?.isBlank() == true)) {
                            field.value = todayStr
                        }
                        val isFieldAlwaysDisabled = field.fieldId == "due_date"
                        val isFieldEditable = field.isEditable && !isViewOnly && !isFieldAlwaysDisabled

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
                            val dateValue = when (val v = field.value) {
                                is String -> {
                                    val cleanValue = v.trim().removePrefix("\"").removeSuffix("\"")
                                    if (cleanValue.startsWith("[")) {
                                        try {
                                            val jsonArray = JSONArray(cleanValue)
                                            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            val dates = (0 until jsonArray.length()).mapNotNull {
                                                runCatching { sdf.parse(jsonArray.getString(it)) }.getOrNull()
                                            }
                                            dates.maxOrNull()?.let { sdf.format(it) }
                                        } catch (e: Exception) {
                                            Log.e("FormRenderer", "Error parsing JSON array: ${e.message}")
                                            null
                                        }
                                    } else {
                                        cleanValue
                                    }
                                }
                                is List<*> -> {
                                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                    v.mapNotNull { it as? String }
                                        .mapNotNull { runCatching { sdf.parse(it) }.getOrNull() }
                                        .maxOrNull()?.let { sdf.format(it) }
                                }
                                is JSONArray -> {
                                    try {
                                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                        val dates = (0 until v.length()).mapNotNull {
                                            runCatching { sdf.parse(v.getString(it)) }.getOrNull()
                                        }
                                        dates.maxOrNull()?.let { sdf.format(it) }
                                    } catch (e: Exception) {
                                        Log.e("FormRenderer", "Error parsing JSONArray directly: ${e.message}")
                                        null
                                    }
                                }

                                else -> null
                            }
                            setText(dateValue ?: "")
                            //setText(field.value as? String ?: "")
                            isFocusable = false
                            isClickable = isFieldEditable
                            isEnabled = isFieldEditable
                           // isClickable = true
                            //isEnabled = !isFieldAlwaysDisabled && !isViewOnly
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

                     //   if (!isViewOnly && !isFieldAlwaysDisabled) {
                        if (isFieldEditable) {
                            editText.setOnClickListener {
                                val calendar = Calendar.getInstance()

                                val (minDate, maxDate) = if (field.fieldId == "visit_date") {
                                    minVisitDate to maxVisitDate
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
                                    when {
                                        filePath.endsWith(".pdf", ignoreCase = true) ||
                                                (filePath.startsWith("content://") &&
                                                        context.contentResolver.getType(Uri.parse(filePath))?.contains("pdf") == true) -> {

                                            setImageResource(R.drawable.ic_person)
                                            setOnClickListener {
                                                val uri = Uri.parse(filePath)
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: ActivityNotFoundException) {
                                                    Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                        filePath.startsWith("JVBERi0") || filePath.startsWith("%PDF") -> {
                                            setImageResource(R.drawable.ic_person)
                                            setOnClickListener {
                                                try {
                                                    val decodedBytes = Base64.decode(filePath, Base64.DEFAULT)
                                                    val tempPdf = File.createTempFile("decoded_pdf_", ".pdf", context.cacheDir)
                                                    FileOutputStream(tempPdf).use { it.write(decodedBytes) }

                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.provider",
                                                        tempPdf
                                                    )

                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "application/pdf")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Failed to open PDF", Toast.LENGTH_SHORT).show()
                                                    Timber.e(e, "Failed to open Base64 PDF")
                                                }
                                            }
                                        }

                                        filePath.startsWith("data:image", ignoreCase = true) ||
                                                filePath.startsWith("/9j/") ||
                                                filePath.startsWith("iVBOR") ||
                                                (filePath.length > 100 &&
                                                        !filePath.startsWith("content://") &&
                                                        !filePath.startsWith("file://")) -> {

                                            try {
                                                val base64String = filePath.substringAfter(",")
                                                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                                setImageBitmap(bitmap)
                                            } catch (e: Exception) {
                                                Timber.e(e, "Failed to decode base64 image")
                                                setImageResource(R.drawable.ic_person)
                                            }
                                        }

                                        else -> {
                                            val uri = Uri.parse(filePath)
                                            setImageURI(uri)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.tag("FormRendererAdapter").e(e, "Failed to load file: $filePath")
                                    setImageResource(R.drawable.ic_person)
                                }
                            } else {
                                setImageResource(R.drawable.ic_person)
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

    }
