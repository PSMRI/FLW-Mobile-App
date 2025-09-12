package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.MDSRCache

class MDSRFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {


    private val dateOfDeath = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title =resources.getString(R.string.str_date_of_death),
        min = 0L,
        max = System.currentTimeMillis(),
        required = true
    )
    private val address = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title =resources.getString(R.string.mdsr_address),
        required = false
    )
    private val husbandName = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        etMaxLength = 50,
        title =resources.getString(R.string.mdsr_husband_name),
        required = false
    )
    private val causeOfDeath = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.mdsr_cause),
        required = true,
        orientation = LinearLayout.VERTICAL,
        hasDependants = true,
        entries = arrayOf("Maternal", "Non-maternal")
    )
    private val reasonOfDeath = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.mdsr_reason_death),
        required = true
    )
    private val investigationDate = FormElement(
        id = 6,
        inputType = InputType.DATE_PICKER,
        title =resources.getString(R.string.mdsr_field_investigation),
        min = 0L,
        max = System.currentTimeMillis(),
        required = false
    )
    private val actionTaken = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.mdsr_action),
        required = false,
        orientation = LinearLayout.VERTICAL,
        entries = arrayOf("Yes", "No")
    )
    private val blockMOSign = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.mdsr_signature),
        required = false
    )
    private val dateIc = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        min = 0L,
        max = System.currentTimeMillis(),
        title =resources.getString(R.string.mdsr_ic_date),
        required = false
    )


    suspend fun setUpPage(
        ben: BenRegCache,
        address: String,
        saved: MDSRCache?
    ) {
        val list = mutableListOf(
            dateOfDeath,
            this.address,
            husbandName,
            causeOfDeath,
            investigationDate,
            actionTaken,
            blockMOSign,
            dateIc
        )
        this.address.value = address
        husbandName.value = ben.genDetails?.spouseName
        saved?.let { mdsr ->
            dateOfDeath.value = mdsr.dateOfDeath?.let { getDateFromLong(it) }
            this.address.value = mdsr.address
            husbandName.value = mdsr.husbandName
            causeOfDeath.value = mdsr.causeOfDeath
            reasonOfDeath.value = mdsr.reasonOfDeath
            investigationDate.value = mdsr.investigationDate?.let { getDateFromLong(it) }
            actionTaken.value = mdsr.actionTaken?.let {
                if (it) resources.getString(R.string.yes) else resources.getString(R.string.no)
            }
            blockMOSign.value = mdsr.blockMOSign
            dateIc.value = mdsr.dateIc?.let { getDateFromLong(it) }
        }
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            causeOfDeath.id -> triggerDependants(
                source = causeOfDeath,
                passedIndex = index,
                triggerIndex = 0,
                target = listOf(reasonOfDeath)
            )

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as MDSRCache).let { mdsrCache ->
            mdsrCache.dateOfDeath = getLongFromDate(dateOfDeath.value!!)
            mdsrCache.address = address.value
            mdsrCache.husbandName = husbandName.value
            mdsrCache.causeOfDeath = causeOfDeath.value
            mdsrCache.reasonOfDeath = reasonOfDeath.value
            mdsrCache.actionTaken = actionTaken.value?.let { actionTaken.value == "Yes" }
            mdsrCache.investigationDate = investigationDate.value?.let { getLongFromDate(it) }
            mdsrCache.blockMOSign = blockMOSign.value
            mdsrCache.dateIc = dateIc.value?.let { getLongFromDate(it) }
            mdsrCache.createdDate = System.currentTimeMillis()
        }
    }
}