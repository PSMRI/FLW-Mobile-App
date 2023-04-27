package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.model.FormInputOld
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.MDSRCache
import java.text.SimpleDateFormat
import java.util.*

class MDSRFormDataset(context: Context) {

    companion object {
        private fun getCurrentDate(): String {
            val calendar = Calendar.getInstance()
            val mdFormat =
                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

        private fun getLongFromDate(dateString: String): Long {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }

    fun mapValues(mdsrCache: MDSRCache) {
        mdsrCache.dateOfDeath = getLongFromDate(dateOfDeath.value.value!!)
        mdsrCache.address = address.value.value
        mdsrCache.husbandName = husbandName.value.value
        mdsrCache.causeOfDeath = if(causeOfDeath.value.value == "Maternal") 1 else 2
        mdsrCache.reasonOfDeath = reasonOfDeath.value.value
        mdsrCache.actionTaken = if(actionTaken.value.value == "Yes") 1 else 2
        mdsrCache.investigationDate = investigationDate.value.value?.let { getLongFromDate(it) }
        mdsrCache.blockMOSign = blockMOSign.value.value
        mdsrCache.date = date.value.value?.let { getLongFromDate(it) }
        mdsrCache.createdDate = System.currentTimeMillis()
    }

    private var mdsr: MDSRCache? = null

    constructor(context: Context, mdsr: MDSRCache? = null) : this(context) {
        this.mdsr = mdsr
        //TODO(SETUP THE VALUES)
    }

    val dateOfDeath = FormInputOld(
        inputType = InputType.DATE_PICKER,
        title = "Date of death ",
        min = 0L,
        max = System.currentTimeMillis(),
        required = true
    )
    val address = FormInputOld(
        inputType = InputType.TEXT_VIEW,
        title = "Address",
        required = false
    )
    val husbandName = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        etMaxLength = 50,
        title = "Husband’s Name",
        required = false
    )
    val causeOfDeath = FormInputOld(
        inputType = InputType.RADIO,
        title = "Cause of death",
        required = true,
        orientation = LinearLayout.VERTICAL,
        entries = arrayOf("Maternal", "Non-maternal")
    )
    val reasonOfDeath = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Specify Reason",
        required = true
    )
    val investigationDate = FormInputOld(
        inputType = InputType.DATE_PICKER,
        title = "Date of field investigation",
        min = 0L,
        max = System.currentTimeMillis(),
        required = false
    )
    val actionTaken = FormInputOld(
        inputType = InputType.RADIO,
        title = "Action Take",
        required = false,
        orientation = LinearLayout.VERTICAL,
        entries = arrayOf("Yes", "No")
    )
    val blockMOSign = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Signature of MO I/C of the block",
        required = false
    )
    val date = FormInputOld(
        inputType = InputType.DATE_PICKER,
        min = 0L,
        max = System.currentTimeMillis(),
        title = "Date",
        required = false
    )

    val firstPage by lazy {
        listOf(dateOfDeath, address, husbandName, causeOfDeath, investigationDate, actionTaken,
            blockMOSign, date)
    }
}