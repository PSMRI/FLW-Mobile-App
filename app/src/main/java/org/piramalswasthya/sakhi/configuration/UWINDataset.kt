package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.UwinCache
import java.util.Calendar

class UWINDataset(context: Context, language: Languages) : Dataset(context, language) {


    companion object {

        private fun getMinDate(monthsBack: Int = 2): Long {
            return Calendar.getInstance().apply {
                add(Calendar.MONTH, -monthsBack)
            }.timeInMillis
        }
    }

    private val uWinSessionDate = FormElement(
        id = 117,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.u_win_session_date),
        max = System.currentTimeMillis(),
        min = getMinDate(2),
        required = true,
        isEnabled = true
    )

    private val place = FormElement(
        id = 118,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.place),
        arrayId = R.array.place_of_delivery_options,
        entries = resources.getStringArray(R.array.place_of_delivery_options),
        required = true,
    )

    private val participant = FormElement(
        id = 119,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.no_of_participants_attended),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        isEnabled = true
    )

    private val uploadSummary1 = FormElement(
        id = 120,
        inputType = InputType.FILE_UPLOAD,
        title = "File Upload 1",
        required = false
    )

    private val uploadSummary2 = FormElement(
        id = 121,
        inputType = InputType.FILE_UPLOAD,
        title = "File Upload 2",
        required = false
    )

    suspend fun setFirstPage(saved: Boolean, cache: UwinCache?) {
        val list = listOf(
            uWinSessionDate,
            place,
            participant,
            uploadSummary1,
            uploadSummary2
        )

        if (saved && cache != null) {
            uWinSessionDate.value = getDateFromLong(cache.sessionDate)
            place.value = cache.place
            participant.value = cache.participantsCount.toString()
            uploadSummary1.value = cache.uploadedFiles1
            uploadSummary2.value = cache.uploadedFiles2


            uWinSessionDate.isEnabled = false
            place.isEnabled = false
            participant.isEnabled = false
        } else {

            uWinSessionDate.value = null
            place.value = null
            participant.value = null
            uploadSummary1.value = null
            uploadSummary2.value = null


            uWinSessionDate.isEnabled = true
            place.isEnabled = true
            participant.isEnabled = true
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        when (formId) {
            uWinSessionDate.id -> {
                uWinSessionDate.errorText = ((if (uWinSessionDate.value.isNullOrEmpty())
                    emitAlertErrorMessage(R.string.form_input_empty_error)
                else null) as String?)
            }


            participant.id -> {
                participant.errorText = (if (participant.value.isNullOrEmpty())
                    emitAlertErrorMessage(R.string.form_input_empty_error)
                else null) as String?
            }

            place.id ->{
                place.errorText = (if (participant.value.isNullOrEmpty())
                    emitAlertErrorMessage(R.string.form_input_empty_error)
                else null) as String?
            }
        }


        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as UwinCache).let {
            it.sessionDate = uWinSessionDate.value?.let { getLongFromDate(it) }!!
            it.place = place.getEnglishStringFromPosition(place.getPosition())
            it.participantsCount = participant.value!!.toInt()
            it.uploadedFiles1 = uploadSummary1.value?.takeIf { it.isNotEmpty() }
            it.uploadedFiles2 = uploadSummary2.value?.takeIf { it.isNotEmpty() }
        }
    }

    fun getUwinFileIndex1() = getIndexById(uploadSummary1.id)
    fun getUwinFileIndex2() = getIndexById(uploadSummary2.id)


    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            120 -> {
                uploadSummary1.value = dpUri.toString()
                uploadSummary1.errorText = null
            }

            121 -> {
                uploadSummary2.value = dpUri.toString()
                uploadSummary2.errorText = null
            }
        }
    }

}