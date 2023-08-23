package org.piramalswasthya.sakhi.ui

import android.net.Uri
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import android.widget.RadioGroup.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.AncFormState
import org.piramalswasthya.sakhi.model.AncFormState.*
import org.piramalswasthya.sakhi.model.FormInputOld
import org.piramalswasthya.sakhi.model.VaccineState
import org.piramalswasthya.sakhi.model.VaccineState.*
import timber.log.Timber


//@BindingAdapter("vaccine_items", "vaccine_click")
//fun VaccinationElement.setItems(
//    data: ImmunizationDetailsDomain?,
//    clickListener: VaccineClickListener?
//) {
//    setData(data, clickListener)
//}
//
//
//@BindingAdapter("vaccine_headers")
//fun VaccinationListHeader.setItems(data: ImmunizationDetailsHeader?) {
//    data?.let { setData(it) }
//}

@BindingAdapter("vaccineState")
fun ImageView.setVaccineState(syncState: VaccineState?) {
    syncState?.let {
//        visibility = View.VISIBLE
        val drawable = when (it) {
            DONE -> R.drawable.ic_check_circle
            MISSED -> R.drawable.ic_close
            PENDING -> R.drawable.ic_add_circle
            OVERDUE -> R.drawable.ic_overdue
            UNAVAILABLE -> null
        }
        drawable?.let { it1 -> setImageResource(it1) }
    }
}

@BindingAdapter("vaccineState")
fun Button.setVaccineState(syncState: VaccineState?) {

    syncState?.let {
        visibility = View.VISIBLE
        when (it) {
            PENDING,
            OVERDUE -> {
                text = "FILL"
            }

            DONE -> {
                text = "VIEW"
            }

            MISSED,
            UNAVAILABLE -> {
                visibility = View.GONE
            }
        }
    }
}


@BindingAdapter("allowRedBorder", "scope", "recordCount")
fun TextView.setRecordCount(showRedBorder: Boolean, scope: CoroutineScope, count: Flow<Int>?) {
    count?.let {
        scope.launch {
            it.collect {
                text = it.toString()
                if (it > 0 && showRedBorder) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setBackgroundColor(
                            context.resources.getColor(
                                android.R.color.holo_red_dark,
                                context.theme
                            )
                        )
                    } else
                        setBackgroundColor(
                            context.resources.getColor(
                                android.R.color.holo_red_dark,
                            )
                        )
                }
            }
        }
    } ?: run {
        text = null
    }
}

@BindingAdapter("benId", "syncState")
fun TextView.setBenIdText(benId: Long?, syncState: SyncState?) {
    if (syncState != SyncState.SYNCED)
        text = "Pending Sync"
    else
        text = benId.toString()
}

@BindingAdapter("listItems")
fun AutoCompleteTextView.setSpinnerItems(list: Array<String>?) {
    list?.let {
        this.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, it))
    }
}

@BindingAdapter("allCaps")
fun TextInputEditText.setAllAlphabetCaps(allCaps: Boolean) {
    if (allCaps) {
        isAllCaps = true
        inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    }
}

@BindingAdapter("showLayout")
fun Button.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("showLayout")
fun ViewGroup.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("radioForm")
fun ConstraintLayout.setItems(form: FormInputOld?) {
//    if(this.childCount!=0)
//        return

//    val rg = this.findViewById<RadioGroup>(R.id.rg)
//    rg.removeAllViews()
//    rg.apply {
//        form?.entries?.let { items ->
//            orientation = form.orientation ?: LinearLayout.HORIZONTAL
//            weightSum = items.size.toFloat()
//            items.forEach {
//                val rdBtn = RadioButton(this.context)
//                rdBtn.layoutParams =
//                    LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0F).apply {
//                        gravity = Gravity.CENTER_HORIZONTAL
//                    }
//                rdBtn.id = View.generateViewId()
//
//                rdBtn.text = it
//                addView(rdBtn)
//                if (form.value.value == it)
//                    rdBtn.isChecked = true
//                rdBtn.setOnCheckedChangeListener { _, b ->
//                    if (b) {
//                        form.value.value = it
//                    }
//                    form.errorText = null
//                    this@setItems.setBackgroundResource(0)
//                }
//            }
//            form.value.value?.let { value ->
//                children.forEach {
//                    if ((it as RadioButton).text == value) {
//                        clearCheck()
//                        check(it.id)
//                    }
//                }
//            }
//        }
//    }
}

@BindingAdapter("checkBoxesForm")
fun ConstraintLayout.setItemsCheckBox(form: FormInputOld?) {
//    if (this.childCount != 0)
//        return
    val ll = this.findViewById<LinearLayout>(R.id.ll_checks)
    ll.removeAllViews()
    ll.apply {
        form?.entries?.let { items ->
            orientation = form.orientation ?: LinearLayout.VERTICAL
            weightSum = items.size.toFloat()
            items.forEach {
                val cbx = CheckBox(this.context)
                cbx.layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0F)
                cbx.id = View.generateViewId()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) cbx.setTextAppearance(
                    context, android.R.style.TextAppearance_Material_Medium
                )
                else cbx.setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
                cbx.text = it
                addView(cbx)
                if (form.value.value?.contains(it) == true) cbx.isChecked = true
                cbx.setOnCheckedChangeListener { _, b ->
                    if (b) {
                        if (form.value.value != null) form.value.value = form.value.value + it
                        else form.value.value = it
                    } else {
                        if (form.value.value?.contains(it) == true) {
                            form.value.value = form.value.value?.replace(it, "")
                        }
                    }
                    if (form.value.value.isNullOrBlank()) {
                        form.value.value = null
                    } else {
                        Timber.d("Called here!")
                        form.errorText = null
                        this@setItemsCheckBox.setBackgroundResource(0)
                    }
                    Timber.d("Checkbox values : ${form.value.value}")
                }
            }
        }
    }
}

@BindingAdapter("required")
fun TextView.setRequired(required: Boolean?) {
    required?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("imgRequired")
fun ImageView.setRequired(required: Boolean?) {
    required?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}


private val rotate = RotateAnimation(
    360F, 0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
).apply {
    duration = 1000
    interpolator = LinearInterpolator()
    repeatCount = Animation.INFINITE
}


@BindingAdapter("syncState")
fun ImageView.setSyncState(syncState: SyncState?) {
    syncState?.let {
        visibility = View.VISIBLE
        val drawable = when (it) {
            SyncState.UNSYNCED -> R.drawable.ic_unsynced
            SyncState.SYNCING -> R.drawable.ic_syncing
            SyncState.SYNCED -> R.drawable.ic_synced
        }
        setImageResource(drawable)
        isClickable = it == SyncState.UNSYNCED
        if (it == SyncState.SYNCING) startAnimation(rotate)
    } ?: run {
        visibility = View.INVISIBLE
    }
}


@BindingAdapter("benImage")
fun ImageView.setBenImage(uriString: String?) {
    if (uriString == null) setImageResource(R.drawable.ic_person)
    else {
        Glide.with(this).load(Uri.parse(uriString)).placeholder(R.drawable.ic_person).circleCrop()
            .into(this)
    }
}

//fun EditText.afterTextChanged(afterTextChanged: (String?) -> Unit) {
//    this.addTextChangedListener(object : TextWatcher {
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//        }
//
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//        }
//
//        override fun afterTextChanged(editable: Editable?) {
//            if(!editable.isNullOrBlank())
//                afterTextChanged.invoke(editable.toString())
//        }
//    })
//}

@BindingAdapter("anc_state")
fun Button.setAncState(ancFormState: AncFormState?) {
    ancFormState?.let {
        visibility = View.VISIBLE
        when (it) {
            ALLOW_FILL -> {
                text = "FILL"
            }

            ALREADY_FILLED -> {
                text = "VIEW"
            }

            NO_FILL -> {
                visibility = View.INVISIBLE
            }
        }
    }
}

@BindingAdapter("list_avail")
fun Button.setCbacListAvail(list: List<Any>?) {
    list?.let {
        if (list.isEmpty())
            visibility = View.INVISIBLE
        else
            visibility = View.VISIBLE
    }
}

@BindingAdapter("anc_state_icon")
fun ImageView.setAncState(ancFormState: AncFormState?) {
    ancFormState?.let {
        setImageResource(
            when (it) {
                ALLOW_FILL -> {
                    R.drawable.ic_pending_actions
                }

                ALREADY_FILLED -> {
                    R.drawable.ic_check_circle
                }

                NO_FILL -> {
                    R.drawable.ic_close
                }
            }
        )
    }
}


