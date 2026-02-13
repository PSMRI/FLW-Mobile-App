package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DewormingDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {
    private val formElementList = mutableListOf<FormElement>()
    private val dewormingDone = FormElement(
        id = 6,
        inputType = RADIO,
        title = resources.getString(R.string.deworming_done),
        entries = resources.getStringArray(R.array.yes_no_options),
        required = true,
        hasDependants = true
    )

    private val dewormingDate = FormElement(
        id = 5,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.deworming_date),
        required = true,
        min = System.currentTimeMillis(),
        max = System.currentTimeMillis(),
    )

    private val dewormingLocation = FormElement(
        id = 4,
        inputType = RADIO,
        title = resources.getString(R.string.deworming_location),
        entries = resources.getStringArray(R.array.deworming_location_options),
        required = true,
    )

    private val ageGroup = FormElement(
        id = 3,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.age_group),
        etMaxLength = 2,
        etInputType = InputType.TYPE_CLASS_NUMBER,
        required = true
    )

    private val pic1 = FormElement(
        id = 1,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.upload_image),
        arrayId = -1,
        required = false
    )

    private val pic2 = FormElement(
        id = 2,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.upload_image),
        arrayId = -1,
        required = false
    )

    suspend fun setUpPage(deworming: DewormingCache?) {

        if (pic1.value.isNullOrBlank()) {
            pic1.value = "default"
        }

        if (pic2.value.isNullOrBlank()) {
            pic2.value = "default"
        }
        val list = mutableListOf(
            dewormingDone,
            ageGroup,
            pic1,
            pic2
        )
        deworming?.let { loadCachedData(it,list) }
        formElementList.addAll(list)
        setUpPage(list)
    }
    fun getFormElementList(): List<FormElement> = formElementList
    private fun loadCachedData(deworming: DewormingCache, list: MutableList<FormElement>) {
        dewormingDone.value = deworming.dewormingDone
        dewormingDate.value = deworming.dewormingDate
        dewormingLocation.value = deworming.dewormingLocation
        ageGroup.value = deworming.ageGroup?.toString()
        pic1.value = deworming.image1
        pic2.value = deworming.image2

        if (dewormingDone.value == "Yes") {
            list.add(dewormingDone.getPosition() ,dewormingDate)
            list.add(dewormingDone.getPosition() + 1,dewormingLocation)
        }
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dewormingDone.id -> {
                if (dewormingDone.value == "Yes") {
                    if (!formElementList.contains(dewormingDate)) {
                        formElementList.add(dewormingDate)
                    }
                    if (!formElementList.contains(dewormingLocation)) {
                        formElementList.add(dewormingLocation)
                    }
                }
                else{
                    formElementList.remove(dewormingDate)
                    formElementList.remove(dewormingLocation)
                }
                triggerDependants(
                    source = dewormingDone,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = listOf(dewormingDate,dewormingLocation),
                )
            }
            ageGroup.id -> {
                validateEmptyOnEditText(ageGroup)
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as DewormingCache).let { form ->
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val currentDate = formatter.format(Date())
            form.dewormingDone = dewormingDone.value!!
            form.dewormingDate = dewormingDate.value
            form.dewormingLocation = dewormingLocation.value
            form.ageGroup = ageGroup.value?.toInt()
            form.image1 = pic1.value
            form.image2 = pic2.value
            form.regDate = currentDate
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