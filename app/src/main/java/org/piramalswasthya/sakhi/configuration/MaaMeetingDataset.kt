package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
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
        title = "Place",
        required = true,
        entries = arrayOf("HWC", "Anganwadi Centre", "Community Center")
    )

    val participants = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = "Number of Participants Attended",
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
            participants
        )

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


