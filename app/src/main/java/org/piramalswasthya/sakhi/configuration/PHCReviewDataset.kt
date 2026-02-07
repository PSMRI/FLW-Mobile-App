package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.utils.HelperUtil.parseSelections
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PHCReviewDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

    }

    private val formElementList = mutableListOf<FormElement>()

    private val phcReviewDate = FormElement(
        id = 3,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.phc_review_meeting_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (60L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
    )

    private val place = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.place_of_meeting),
        entries = resources.getStringArray(R.array.place_of_vhsnc),
        arrayId = -1,
        required = true,
        allCaps = true,
    )

    private var villageName = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.village_name),
        required = false,
        hasDependants = false
    )

    private val noOfParticipantsAttended = FormElement(
        id = 6,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.total_no_of_participants_attended),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        value = "0",
        etMaxLength = 3,
        max = 999,
        min = 0
    )

    private val mitanin = FormElement(
        id = 8,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.participants_attended),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.mitanin_array),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )

    private val mT = FormElement(
        id = 9,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.mt),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.activity_checklist),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )

    private val pic1 = FormElement(
        id = 1,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.upload_image),
        arrayId = -1,
        required = false,
    )
    private val pic2 = FormElement(
        id = 2,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.upload_image),
        arrayId = -1,
        required = false,
    )

    suspend fun setUpPage(phc: PHCReviewMeetingCache?) {
        if (pic1.value.isNullOrBlank()) {
            pic1.value = "default"
        }

        if (pic2.value.isNullOrBlank()) {
            pic2.value = "default"
        }
        val list = mutableListOf(
            phcReviewDate,
            place,
            villageName,
            noOfParticipantsAttended,
        )

        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            list.addAll(
                listOf(
                   mitanin,
                    mT,
                )
            )
        }

        list.addAll(
            listOf(
                pic1,
                pic2
            )
        )
        phcReviewDate.value = getCurrentDateString()
        phc?.let {
            phcReviewDate.value = it.phcReviewDate
            pic1.value = it.image1
            pic2.value = it.image2
            place.value = it.place
            villageName.value = it.villageName
            noOfParticipantsAttended.value = it.noOfBeneficiariesAttended.toString()
            if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                val parsedMitaninHistoryList = parseSelections(it.mitaninHistory, mitanin.entries!!)
                mitanin.value = if (parsedMitaninHistoryList.isNotEmpty()) {
                    parsedMitaninHistoryList.joinToString("|")
                } else {
                    it.mitaninHistory ?: ""
                }

                val parsedMitaninCheckListList = parseSelections(it.mitaninActivityCheckList, mT.entries!!)
                mT.value = if (parsedMitaninCheckListList.isNotEmpty()) {
                    parsedMitaninCheckListList.joinToString("|")
                } else {
                    it.mitaninActivityCheckList ?: ""
                }
            }

        }
        formElementList.clear()
        formElementList.addAll(list)
        setUpPage(list)
    }

    fun getFormElementList(): List<FormElement> = formElementList

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {

        return when (formId) {

            mT.id -> {

                val allItems = resources.getStringArray(R.array.activity_checklist)
                val itemCount = allItems.size

                val selectedIndexes = mT.value
                    ?.split("|")
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.toMutableSet()
                    ?: mutableSetOf()

                if (index == 0) {

                    if (selectedIndexes.contains(0)) {
                        selectedIndexes.clear()
                        selectedIndexes.addAll(0 until itemCount)
                    } else {
                        selectedIndexes.clear()
                    }

                    mT.value =
                        if (selectedIndexes.isEmpty()) null
                        else selectedIndexes.sorted().joinToString("|")

                    return -1
                }
                val normalIndexes = (1 until itemCount)

                if (selectedIndexes.contains(0) &&
                    normalIndexes.any { !selectedIndexes.contains(it) }
                ) {
                    selectedIndexes.remove(0)
                }

                if (normalIndexes.all { selectedIndexes.contains(it) }) {
                    selectedIndexes.add(0)
                }

                mT.value =
                    if (selectedIndexes.isEmpty()) null
                    else selectedIndexes.sorted().joinToString("|")


                -1
            }
            place.id -> {
                validateEmptyOnEditText(place)
                validateAllAlphabetsSpecialAndNumericOnEditText(place)
                -1
            }
            villageName.id -> {
                validateEmptyOnEditText(villageName)
                validateAllAlphabetsSpecialAndNumericOnEditText(villageName)
                -1
            }

            noOfParticipantsAttended.id -> {
                validateEmptyOnEditText(noOfParticipantsAttended)
                -1
            }

            else -> {
                -1
            }
        }
    }

    private fun toCsv(rawValue: String?, entries: Array<String>): String {
        return parseSelections(rawValue, entries).joinToString("|")
    }
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PHCReviewMeetingCache).let { form ->

            form.phcReviewDate = phcReviewDate.value!!
            form.place = place.value
            form.placeId = place.getPosition()
            form.noOfBeneficiariesAttended = noOfParticipantsAttended.value?.toIntOrNull() ?: 0
            form.image1 = pic1.value
            form.image2 = pic2.value
            form.villageName = villageName.value
            form.mitaninHistory = toCsv(mitanin.value,mitanin.entries!!)
            form.mitaninActivityCheckList = toCsv(mT.value,mT.entries!!)

        }

    }
    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            pic1.id -> {
                pic1.value = dpUri.toString()
                pic1.errorText = null
            }
            pic2.id -> {
                pic2.value = dpUri.toString()
                pic2.errorText = null
            }
        }

    }

}