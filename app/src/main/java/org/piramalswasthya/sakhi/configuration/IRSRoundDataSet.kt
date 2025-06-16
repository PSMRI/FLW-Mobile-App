package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.InputType

class IRSRoundDataSet(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val headline = FormElement(
        id = 1,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.irs_round),
        headingLine = false,
        required = false,
        hasDependants = false


    )

    private val dateOfCase = FormElement(
        id = 2,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date),
        required = false,
        min = System.currentTimeMillis(),
        max = System.currentTimeMillis(),
        hasDependants = false

    )


    private val rounds = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.round),
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        hasDependants = false
    )


    suspend fun setUpPage(
        saved: IRSRoundScreening?,
    ) {
        val list = mutableListOf(
            headline,
            dateOfCase,
            rounds,
        )
        if(saved != null) {
           /* dateOfCase.value = getDateFromLong(saved?.date!!)
            rounds.value = saved?.rounds.toString()*/


        } else {

            dateOfCase.value = getDateFromLong(System.currentTimeMillis())

        }


        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
       /* return when (formId) {




        }*/
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as IRSRoundScreening).let { form ->
            form.rounds = rounds.value!!.toInt()
            form.date = getLongFromDate(dateOfCase.value)
        }
    }

    fun getIndexOfDate(): Int {
        return getIndexById(dateOfCase.id)
    }
}