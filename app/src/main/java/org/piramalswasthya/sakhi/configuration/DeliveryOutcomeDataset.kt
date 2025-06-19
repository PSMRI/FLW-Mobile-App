package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

open class DeliveryOutcomeDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object{
        @Throws(Exception::class)
        fun getOneMonthLater(deliveryDate: String?): String {
            val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
            val date: Date = sdf.parse(deliveryDate)

            val calendar: Calendar = Calendar.getInstance()
            calendar.setTime(date)
            calendar.add(Calendar.MONTH, 1) // Add 1 month

            return sdf.format(calendar.getTime())
        }
    }

    private val dateOfDelivery = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_delivery_date),
        arrayId = -1,
        required = true,
        hasDependants = true
    )

    private val timeOfDelivery = FormElement(
        id = 2,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.do_delivery_time),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private var placeOfDelivery = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_delivery_place),
        entries = resources.getStringArray(R.array.do_place_of_delivery_array),
        required = false,
        hasDependants = false
    )

    private var typeOfDelivery = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_delivery_type),
        entries = resources.getStringArray(R.array.do_type_of_delivery_array),
        required = true,
        hasDependants = false
    )

    private var hadComplications = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_had_complication),
        entries = resources.getStringArray(R.array.do_had_complications_array),
        required = false,
        hasDependants = true
    )

    private var complication = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_delivery_complication),
        entries = resources.getStringArray(R.array.do_complications_array),
        required = true,
        hasDependants = true
    )

    private var causeOfDeath = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_death_cause),
        entries = resources.getStringArray(R.array.do_cause_of_death_array),
        required = true,
        hasDependants = true
    )

    private var otherCauseOfDeath = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_other_death_cause),
        etMaxLength = 50,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        required = true,
        hasDependants = false
    )

    private var otherComplication = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_other_delivery_complication),
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        required = true,
        etMaxLength = 50,
        hasDependants = false
    )

    private var deliveryOutcome = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_delivery_outcome),
        required = true,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 6,
        min = 0,
    )

    private var liveBirth = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_live_birth),
        required = true,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 6,
        min = 0,
    )

    private var stillBirth = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_still_birth),
        required = true,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 6,
        min = 0,
    )

    private val dateOfDischarge = FormElement(
        id = 13,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_discharge_date),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val timeOfDischarge = FormElement(
        id = 14,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.do_discharge_time),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private var isJSYBenificiary = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_is_jsy_beneficiary),
        entries = resources.getStringArray(R.array.do_is_jsy_beneficiary_array),
        required = false,
        hasDependants = false
    )

    suspend fun setUpPage(
        pwr: PregnantWomanRegistrationCache,
        anc: PregnantWomanAncCache,
        saved: DeliveryOutcomeCache?
    ) {
        var list = mutableListOf(
            dateOfDelivery,
            timeOfDelivery,
            placeOfDelivery,
            typeOfDelivery,
            hadComplications,
//            complication,
//            causeOfDeath,
//            otherCauseOfDeath,
//            otherComplication,
            deliveryOutcome,
            liveBirth,
            stillBirth,
            dateOfDischarge,
            timeOfDischarge,
            isJSYBenificiary
        )
        if (saved == null) {
            dateOfDelivery.value = getDateFromLong(System.currentTimeMillis())
            //dateOfDischarge.min = System.currentTimeMillis()
            stillBirth.value = "0"

            dateOfDischarge.min = getLongFromDate(dateOfDelivery.value)
            dateOfDischarge.max =getLongFromDate(getOneMonthLater(dateOfDelivery.value))
        } else {
            list = mutableListOf(
                dateOfDelivery,
                timeOfDelivery,
                placeOfDelivery,
                typeOfDelivery,
                hadComplications,
//                complication,
//                causeOfDeath,
//                otherCauseOfDeath,
//                otherComplication,
                deliveryOutcome,
                liveBirth,
                stillBirth,
                dateOfDischarge,
                timeOfDischarge,
                isJSYBenificiary
            )
            dateOfDelivery.value = saved.dateOfDelivery?.let { getDateFromLong(it) }
            timeOfDelivery.value = saved.timeOfDelivery
            placeOfDelivery.value =
                getLocalValueInArray(R.array.do_place_of_delivery_array, saved.placeOfDelivery)
            typeOfDelivery.value =
                getLocalValueInArray(R.array.do_type_of_delivery_array, saved.typeOfDelivery)
            hadComplications.value = if (saved.hadComplications == true) "Yes" else "No"
            complication.value =
                getLocalValueInArray(R.array.do_complications_array, saved.complication)
            causeOfDeath.value =
                getLocalValueInArray(R.array.do_cause_of_death_array, saved.causeOfDeath)
            otherCauseOfDeath.value = saved.otherCauseOfDeath
            otherComplication.value = saved.otherComplication
            deliveryOutcome.value = saved.deliveryOutcome.toString()
            liveBirth.value = saved.liveBirth.toString()
            stillBirth.value = saved.stillBirth.toString()
            dateOfDischarge.value = saved.dateOfDischarge?.let { getDateFromLong(it) }
            timeOfDischarge.value = saved.timeOfDischarge
            isJSYBenificiary.value = if (saved.isJSYBenificiary == true) "Yes" else "No"
        }
        dateOfDelivery.min = maxOf(pwr.lmpDate + TimeUnit.DAYS.toMillis(21 * 7), anc.ancDate)
        dateOfDelivery.max =
            minOf(
                System.currentTimeMillis(),
                getEddFromLmp(pwr.lmpDate) + TimeUnit.DAYS.toMillis(25)
            )
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dateOfDelivery.id -> {
                dateOfDischarge.min = getLongFromDate(dateOfDelivery.value)
                dateOfDischarge.max =getLongFromDate(getOneMonthLater(dateOfDelivery.value))
                -1
            }

            hadComplications.id -> {
                triggerDependants(
                    source = hadComplications,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = complication,
                    targetSideEffect = listOf(causeOfDeath, otherComplication, otherCauseOfDeath)
                )
            }

            complication.id -> {
                if (index == 6) {
                    liveBirth.value = "0"
                    liveBirth.isEnabled =false

                    handleListOnValueChanged(liveBirth.id, 0)

                    triggerDependants(
                        source = complication,
                        addItems = listOf(causeOfDeath),
                        removeItems = listOf(otherComplication, otherCauseOfDeath)
                    )
                } else if (index == 7) {
                    triggerDependants(
                        source = complication,
                        addItems = listOf(otherComplication),
                        removeItems = listOf(causeOfDeath, otherCauseOfDeath)
                    )
                } else {
                    triggerDependants(
                        source = complication,
                        addItems = listOf(),
                        removeItems = listOf(otherComplication, otherCauseOfDeath, causeOfDeath)
                    )
                }
            }

            causeOfDeath.id -> {
                triggerDependants(
                    source = causeOfDeath,
                    passedIndex = index,
                    triggerIndex = 4,
                    target = otherCauseOfDeath
                )
            }

            otherCauseOfDeath.id -> {
                validateAllAlphabetsSpecialOnEditText(otherCauseOfDeath)
            }

            otherComplication.id -> {
                validateAllAlphabetsSpecialOnEditText(otherComplication)
            }

            deliveryOutcome.id -> {
                validateDeliveryOutcome(deliveryOutcome)
            }

            liveBirth.id -> {
                validateDeliveryOutcome(liveBirth)
            }

            stillBirth.id -> {
                validateDeliveryOutcome(stillBirth)
            }

            else -> -1
        }
    }

    private fun validateDeliveryOutcome(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.toLong()?.let {
            formElement.min?.let { min ->
                formElement.max?.let { max ->
                    if (it < min) {
                        resources.getString(
                            R.string.form_input_min_limit_error, formElement.title, min
                        )
                    } else if (it > max) {
                        resources.getString(
                            R.string.form_input_max_limit_error, formElement.title, max
                        )
                    } else null
                }
            }
        }
        if (!liveBirth.value.isNullOrEmpty() && !stillBirth.value.isNullOrEmpty() &&
            !deliveryOutcome.value.isNullOrEmpty() && formElement.errorText.isNullOrEmpty()
        ) {
            if (deliveryOutcome.value!!.toInt() != liveBirth.value!!.toInt() + stillBirth.value!!.toInt()) {
                formElement.errorText =
                    "Outcome of Delivery should be equal to sum of Live and Still births"
            }
        }
        if (!deliveryOutcome.value.isNullOrEmpty()) {
            stillBirth.max = deliveryOutcome.value?.toLong()
            liveBirth.max = deliveryOutcome.value?.toLong()
        }
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as DeliveryOutcomeCache).let { form ->
            form.dateOfDelivery = getLongFromDate(dateOfDelivery.value)
            form.timeOfDelivery = timeOfDelivery.value
            form.placeOfDelivery = placeOfDelivery.value
            form.typeOfDelivery = typeOfDelivery.value
            form.hadComplications = hadComplications.value == "Yes"
            form.complication = complication.value
            form.causeOfDeath = causeOfDeath.value
            form.otherCauseOfDeath = otherCauseOfDeath.value
            form.otherComplication = otherComplication.value
            form.deliveryOutcome = deliveryOutcome.value?.toInt()
            form.liveBirth = liveBirth.value?.toInt()
            form.stillBirth = stillBirth.value?.toInt()
            form.dateOfDischarge = (dateOfDischarge.value?.let {
                getLongFromDate(it)
            })
            form.timeOfDischarge = timeOfDischarge.value
            form.isJSYBenificiary = isJSYBenificiary.value == "Yes"
        }
    }
}