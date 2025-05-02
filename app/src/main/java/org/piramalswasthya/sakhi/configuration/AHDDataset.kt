package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.widget.Toast
import okhttp3.internal.notifyAll
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.*

class AHDDataset(
   var context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {
    private val formElementList = mutableListOf<FormElement>()

    private val mobilizedForAHD = FormElement(
        id = 5,
        inputType = RADIO,
        title = resources.getString(R.string.mobilized_for_ahd),
        entries = resources.getStringArray(R.array.yes_no_options),
        required = true,
        hasDependants = true
    )

    private val ahdPlace = FormElement(
        id = 4,
        inputType = DROPDOWN,
        title = resources.getString(R.string.ahd_place),
        entries = resources.getStringArray(R.array.ahd_place_options),
        required = true,
        isEnabled = true,

    )

    private val ahdDate = FormElement(
        id = 3,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ahd_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis(),
        max = System.currentTimeMillis(),
        isEnabled = true
    )

    private val pic1 = FormElement(
        id = 1,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.nbr_image),
        subtitle = resources.getString(R.string.nbr_image_sub),
        arrayId = -1,
        required = false
    )

    private val pic2 = FormElement(
        id = 2,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.nbr_image),
        subtitle = resources.getString(R.string.nbr_image_sub),
        arrayId = -1,
        required = false
    )

    suspend fun setUpPage(ahd: AHDCache?) {
        formElementList.clear()
        val list = mutableListOf(
            mobilizedForAHD,
            pic1,
            pic2
        )

        ahd?.let { loadCachedData(it,list) }
        formElementList.addAll(list)
        setUpPage(list)

    }
    fun getFormElementList(): List<FormElement> = formElementList

    private fun loadCachedData(ahd: AHDCache, list: MutableList<FormElement>) {
        mobilizedForAHD.value = ahd.mobilizedForAHD
        ahdPlace.value = ahd.ahdPlace
        ahdDate.value = ahd.ahdDate
        pic1.value = ahd.image1
        pic2.value = ahd.image2
        if(mobilizedForAHD.value == "Yes") {
            list.add(mobilizedForAHD.getPosition() ,ahdPlace)
            list.add(mobilizedForAHD.getPosition() + 1,ahdDate)
        }
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {

        return when (formId) {

            mobilizedForAHD.id -> {
                if (mobilizedForAHD.value == "Yes") {
                    if (!formElementList.contains(ahdPlace)) {
                        formElementList.add(ahdPlace)
                    }
                    if (!formElementList.contains(ahdDate)) {
                        formElementList.add(ahdDate)
                    }
                }
                else{
                    formElementList.remove(ahdPlace)
                    formElementList.remove(ahdDate)
                }
                triggerDependants(
                    source = mobilizedForAHD,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = listOf(ahdPlace,ahdDate),
                )


            }

            ahdPlace.id -> validateEmptyOnSpinner(ahdPlace)
            else -> -1
        }

    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as AHDCache).let { form ->

            form.mobilizedForAHD = mobilizedForAHD.value!!
            form.ahdPlace = ahdPlace.value
            form.ahdDate = ahdDate.value
            form.image1 = pic1.value
            form.image2 = pic2.value

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

    private fun validateEmptyOnSpinner(formElement: FormElement): Int {
        if (formElement.value.isNullOrEmpty()) {
            formElement.errorText = resources.getString(R.string.please_select_value)
            return formElement.id
        }
        formElement.errorText = null
        return -1
    }

}