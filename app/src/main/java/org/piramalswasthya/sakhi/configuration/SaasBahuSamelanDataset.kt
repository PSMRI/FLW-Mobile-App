package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import java.util.Calendar
import org.piramalswasthya.sakhi.utils.HelperUtil.getDateStringFromLong


class SaasBahuSamelanDataset(context: Context, language: Languages) : Dataset(context, language) {

    private fun getTwoMonthsBackDate(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -2)
        return cal.timeInMillis
    }

    private val dateD = FormElement(
            id = 1,
            inputType = InputType.DATE_PICKER,
            title = resources.getString(R.string.saas_date),
            required = true,
            min = getTwoMonthsBackDate(),
            max = System.currentTimeMillis()
        )

    private val place = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.place),
        entries = resources.getStringArray(R.array.place_array),
        arrayId = -1,
        required = false,
        hasDependants = false
    )
    private val noOfParticipante = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_participent),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 999,
        min = 0,
    )

    val upload1 = FormElement(
        id = 10,
        inputType = InputType.FILE_UPLOAD,
        title = "Sammelan Photos / MoM (1)",
        required = false,
    )
    val upload2 = upload1.copy(id = 11, title = "Sammelan Photos / MoM (2)")
    val upload3 = upload1.copy(id = 12, title = "Sammelan Photos / MoM (3)")
    val upload4 = upload1.copy(id = 13, title = "Sammelan Photos / MoM (4)")
    val upload5 = upload1.copy(id = 14, title = "Sammelan Photos / MoM (5)")


    suspend fun setUpPage(
        saasBahu: SaasBahuSammelanCache?,
        recordExists: Boolean

        ) {
        val list = mutableListOf(
            dateD,
            place,
            noOfParticipante,
        )

        val uploadList = listOf(upload1, upload2, upload3, upload4, upload5)

        if (recordExists) {
            if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                dateD.value = getDateStringFromLong(saasBahu?.date)
                place.value = saasBahu?.place
                noOfParticipante.value = saasBahu?.participants?.toString()
                val imgs = saasBahu?.sammelanImages ?: emptyList()
                upload1.value = imgs.getOrNull(0)
                upload2.value = imgs.getOrNull(1)
                upload3.value = imgs.getOrNull(2)
                upload4.value = imgs.getOrNull(3)
                upload5.value = imgs.getOrNull(4)
                val filledUploads = uploadList.filter { !it.value.isNullOrEmpty() }

                if (filledUploads.isEmpty()) {
                    list.addAll(uploadList)
                } else {
                    list.addAll(filledUploads)
                }
            }

        } else {
            list.addAll(uploadList)
        }

        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(
        formId: Int,
        index: Int
    ): Int {
        return when (formId) {
            noOfParticipante.id -> {
                validateAllDigitOnEditText(noOfParticipante)
            }

            else -> -1
        }

    }

    override fun mapValues(
        cacheModel: FormDataModel,
        pageNumber: Int
    ) {

    }

     fun mapSaasBahuValues(
        cacheModel: SaasBahuSammelanCache,
    ) {
         cacheModel.place = place.value
         cacheModel.sammelanImages = listOfNotNull(upload1.value, upload2.value, upload3.value, upload4.value, upload5.value)
         cacheModel.participants = noOfParticipante.value?.toInt()

         cacheModel.date =  getLongFromDate(dateD.value.toString())

    }


}