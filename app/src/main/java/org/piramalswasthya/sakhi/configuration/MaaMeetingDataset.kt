package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import java.util.Calendar

class MaaMeetingDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    val meetingDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = "MAA Meeting Date",
        required = true,
        max = System.currentTimeMillis(),
    ).apply {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -2)
        min = cal.timeInMillis
    }


    val meetingPlace = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.place_of_maa_meeting),
        required = true,
        entries = arrayOf("Anganwadi Centre", "HWC","School","Community Center")
    )

    val villageName = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.village_name),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
     val noOfPW = FormElement(
        id = 17,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_pregnant_woman_attended),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )

     val noOfLM = FormElement(
        id = 16,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_lactating_mothers_attended),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )

     val duringBreastfeeding  = FormElement(
        id = 15,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.during_breastfeeding_counseling_check_list),
        arrayId = -1,
        entries = resources.getStringArray(R.array.maa_breastfeeding),
        required = false,
    )


    val participants = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.total_no_b_attended),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3,
        min = 0,
        max = 999
    )

    val upload1 = FormElement(
        id = 10,
        inputType = InputType.FILE_UPLOAD,
        title = "Meeting Photos / MoM (1)",
        required = false
    )
    val upload2 = upload1.copy(id = 11, title = "Meeting Photos / MoM (2)")
    val upload3 = upload1.copy(id = 12, title = "Meeting Photos / MoM (3)")
    val upload4 = upload1.copy(id = 13, title = "Meeting Photos / MoM (4)")
    val upload5 = upload1.copy(id = 14, title = "Meeting Photos / MoM (5)")

    suspend fun setUpPage(recordExists: Boolean) {
        val list = mutableListOf(
            meetingDate,
            meetingPlace,
        )

        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            list.add(villageName)
            list.add(participants)
            list.add(noOfPW)
            list.add(noOfLM)
            list.add(duringBreastfeeding)
        }
        else{
            list.add(participants)
        }

        val uploadList = listOf(upload1, upload2, upload3, upload4, upload5)

        if (recordExists) {
            val filledUploads = uploadList.filter { !it.value.isNullOrEmpty() }
            if (filledUploads.isEmpty()) {
                list.addAll(uploadList)
            } else {
                list.addAll(filledUploads)
            }
        } else {
            list.addAll(uploadList)
        }

        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            duringBreastfeeding.id -> {

                val allItems = resources.getStringArray(R.array.maa_breastfeeding)
                val totalCount = allItems.size

                val selectedIndexes = duringBreastfeeding.value
                    ?.split("|")
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.toMutableSet()
                    ?: mutableSetOf()

                if (index == 0) {

                    if (selectedIndexes.contains(0)) {
                        selectedIndexes.clear()
                        selectedIndexes.addAll(0 until totalCount)
                    } else {
                        selectedIndexes.clear()
                    }

                    duringBreastfeeding.value =
                        if (selectedIndexes.isEmpty()) null
                        else selectedIndexes.sorted().joinToString("|")

                    return -1
                }
                val normalIndexes = (1 until totalCount)

                if (selectedIndexes.contains(0) &&
                    normalIndexes.any { !selectedIndexes.contains(it) }
                ) {
                    selectedIndexes.remove(0)
                }

                if (normalIndexes.all { selectedIndexes.contains(it) }) {
                    selectedIndexes.add(0)
                }

                duringBreastfeeding.value =
                    if (selectedIndexes.isEmpty()) null
                    else selectedIndexes.sorted().joinToString("|")

                -1
            }




            participants.id -> {
                validateAllDigitOnEditText(participants)
                validateIntMinMax(participants)
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {

    }


}


