package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MaaMeetingDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

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

    suspend fun setUpPage() {
        setUpPage(
            listOf(
                meetingDate,
                meetingPlace,
                participants,
                upload1,
                upload2,
                upload3,
                upload4,
                upload5
            )
        )
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

    fun getSelectedDateCalendar(): Calendar? {
        val v = meetingDate.value ?: return null
        return try {
            Calendar.getInstance().apply { time = dateFormatter.parse(v)!! }
        } catch (_: Exception) {
            null
        }
    }

    fun countSelectedUploads(): Int {
        val uploads = listOf(upload1, upload2, upload3, upload4, upload5)
        return uploads.count { !it.value.isNullOrEmpty() }
    }
}


