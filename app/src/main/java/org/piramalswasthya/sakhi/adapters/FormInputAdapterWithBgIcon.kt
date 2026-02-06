package org.piramalswasthya.sakhi.adapters

import android.app.DatePickerDialog
import android.content.res.Resources
import android.net.Uri
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.*
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.model.*
import org.piramalswasthya.sakhi.ui.common.DirtyState
import java.util.Calendar

class FormInputAdapterWithBgIcon(
    imageClickListener: ImageClickListener? = null,
    ageClickListener: AgeClickListener? = null,
    formValueListener: FormValueListener? = null,
    selectImageClickListener: SelectUploadImageClickListener? = null,
    viewDocumentListner: ViewDocumentOnClick? = null,
    isEnabled: Boolean = true
) : FormInputAdapter(
    imageClickListener,
    ageClickListener,
    null,
    formValueListener,
    isEnabled,
    selectImageClickListener,
    viewDocumentListner,
    null
) {

    // ---------------- CHECKBOX FIX ----------------
    class CheckBoxesInputViewHolder private constructor(
        private val binding: RvItemFormCheckV2Binding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return CheckBoxesInputViewHolder(
                    RvItemFormCheckV2Binding.inflate(inflater, parent, false)
                )
            }
        }

        fun bind(item: FormElement, enabled: Boolean, listener: FormValueListener?) {
            binding.form = item
            binding.llChecks.removeAllViews()

            item.entries?.forEach { text ->
                val cb = CheckBox(binding.root.context).apply {
                    this.text = text
                    isEnabled = enabled
                    isChecked = item.value?.contains(text) == true
                }

                cb.setOnCheckedChangeListener { _, checked ->
                    val currentValue = item.value ?: ""

                    item.value = if (checked) {
                        currentValue + text
                    } else {
                        currentValue.replace(text, "")
                    }

                    listener?.onValueChanged(item, -1)
                    (binding.root.parent as? RecyclerView)?.adapter
                        ?.let { ad -> if (ad is DirtyState) ad.markDirty() }
                }

                binding.llChecks.addView(cb)
            }
        }
    }

    // ---------------- MULTI FILE UPLOAD ----------------
    class MultiFileUploadInputViewHolder private constructor(
        private val binding: LayoutMultiFileUploadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return MultiFileUploadInputViewHolder(
                    LayoutMultiFileUploadBinding.inflate(inflater, parent, false)
                )
            }
        }

        private val selectedFiles = mutableListOf<Uri>()

        fun bind(
            item: FormElement,
            clickListener: SelectUploadImageClickListener?,
            documentClick: ViewDocumentOnClick?,
            isEnabled: Boolean
        ) {
            binding.btnSelectFiles.isEnabled = isEnabled
            binding.btnSelectFiles.alpha = if (isEnabled) 1f else 0.5f

            binding.btnSelectFiles.setOnClickListener {
                clickListener?.onSelectImageClick(item)
            }
        }
    }

    // ---------------- DATE PICKER ----------------
    class DatePickerInputViewHolder private constructor(
        private val binding: RvItemFormDatepickerWithBgIconBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return DatePickerInputViewHolder(
                    RvItemFormDatepickerWithBgIconBinding.inflate(inflater, parent, false)
                )
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, listener: FormValueListener?) {
            binding.form = item
            if (!isEnabled) return

            binding.et.setOnClickListener {
                val cal = Calendar.getInstance()
                val min = item.min
                val max = item.max

                val dialog = DatePickerDialog(
                    it.context,
                    { _, y, m, d ->
                        val millis = Calendar.getInstance().apply {
                            set(y, m, d)
                        }.timeInMillis

                        item.value = when {
                            min != null && millis < min -> getDateString(min)
                            max != null && millis > max -> getDateString(max)
                            else -> getDateString(millis)
                        }

                        listener?.onValueChanged(item, -1)
                        (binding.root.parent as? RecyclerView)?.adapter
                            ?.let { ad -> if (ad is DirtyState) ad.markDirty() }
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                min?.let { dialog.datePicker.minDate = it }
                max?.let { dialog.datePicker.maxDate = it }

                if (min != null && max != null && max > min) {
                    dialog.show()
                } else {
                    Toast.makeText(it.context, "Invalid date range", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } // ✅ MISSING BRACE FIXED HERE

    // ---------------- VIEW TYPES ----------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (InputType.values()[viewType]) {
            InputType.CHECKBOXES -> CheckBoxesInputViewHolder.from(parent)
            InputType.MULTIFILE_UPLOAD -> MultiFileUploadInputViewHolder.from(parent)
            InputType.DATE_PICKER -> DatePickerInputViewHolder.from(parent)
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int =
        getItem(position).inputType.ordinal

    override fun validateInput(resources: Resources): Int {
        var retVal = -1
        currentList.forEachIndexed { index, item ->
            if (item.inputType != InputType.TEXT_VIEW && item.required && item.value.isNullOrBlank()) {
                item.errorText = resources.getString(R.string.form_input_empty_error)
                notifyItemChanged(index)
                if (retVal == -1) retVal = index
            }
        }
        return retVal
    }
}
