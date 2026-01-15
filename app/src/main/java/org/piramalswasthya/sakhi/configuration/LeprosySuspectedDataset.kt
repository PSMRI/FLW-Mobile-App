package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache

class LeprosySuspectedDataset (
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var homeVisitDateLong: Long = System.currentTimeMillis()
    private var treatmentStartDateLong: Long = 0L
    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.home_visit_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true,
        isEnabled = false

    )

    private val leprosyStatus = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_leprosy_status),
        arrayId = R.array.leprosy_status,
        entries = resources.getStringArray(R.array.leprosy_status),
        required = false,
        hasDependants = true,
        isEnabled = false

    )

    private var referredTo = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.refer_to),
        arrayId = R.array.dc_refer,
        entries = resources.getStringArray(R.array.dc_refer),
        required = false,
        hasDependants = true,
        isEnabled = false
    )
    private var other = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )
   /* private var typeOfLeprosy = FormElement(
        id = 11,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.type_of_leprocy),
        arrayId = R.array.type_of_leprocy,
        entries = resources.getStringArray(R.array.type_of_leprocy),
        required = false,
        hasDependants = true
    )*/

    private val followUpdate = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.follow_up_date),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true

    )
    private val remarks = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,
        isEnabled = false
        )

    private val leprosySymptoms = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = context.getString(R.string.any_leprosy_symptoms_present),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = false
    )

    private val visitLabel = FormElement(
        id = 15,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.visits),
        required = true,
        isEnabled = false
    )

    private val typeOfLeprosy = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        isEnabled = true,
        required = true,
        title = resources.getString(R.string.type_of_leprocy),
        arrayId = R.array.type_of_leprocy,
        entries = resources.getStringArray(R.array.type_of_leprocy),
        hasDependants = true
    )

    private val treatmentStartDate = FormElement(
        id = 17,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.treatment_start_date),
        isEnabled = true,
        required = false,
        max = System.currentTimeMillis()

    )

    private val leprosyConfirmed = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = context.getString(R.string.has_leprosy_been_confirmed),
        required = true,
        isEnabled = true,
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true)


    suspend fun setUpPage(ben: BenRegCache?, saved: LeprosyScreeningCache?) {
        val list = mutableListOf(
            dateOfCase,
            leprosySymptoms,
            visitLabel,
            leprosyStatus,
            referredTo,
            leprosyConfirmed,
            //typeOfLeprosy,



            )
        homeVisitDateLong = saved?.homeVisitDate ?: System.currentTimeMillis()
        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
            visitLabel.value = "Visit -1"
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no)[1]

            updateDateConstraints()

        } else {
            dateOfCase.value = getDateFromLong(saved.homeVisitDate)
            val symptomsPosition = saved.leprosySymptomsPosition ?: 1
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no).getOrNull(symptomsPosition)
                ?: resources.getStringArray(R.array.yes_no)[1]
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
            leprosyStatus.value =
                getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)

            if (leprosyStatus.value == leprosyStatus.entries!!.last()) {
                leprosyStatus.value = saved.leprosyStatus
                list.add(list.indexOf(leprosyStatus) + 1, other)
            } else {
                leprosyStatus.value =
                    getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)
            }
            remarks.value = saved.remarks
            referredTo.value =
                getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value =
                    getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }

            other.value = saved.otherReferredTo


            updateDateConstraints()

        }



        setUpPage(list)

    }
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
         when (formId) {

             leprosyConfirmed.id ->{
                 if (leprosyConfirmed.value == resources.getStringArray(R.array.yes_no)[0]) {
                     triggerDependants(source = leprosyConfirmed, addItems =listOf(typeOfLeprosy),removeItems = listOf())
                 }
                 else{
                     triggerDependants(source = leprosyConfirmed, addItems = listOf(),removeItems =listOf(typeOfLeprosy))

                 }

             }

            typeOfLeprosy.id -> {
                leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[4]

                treatmentStartDate.value = getDateFromLong(System.currentTimeMillis())

                triggerDependants(
                    source = typeOfLeprosy,
                    addItems = listOf(treatmentStartDate),
                    removeItems = listOf(treatmentStartDate)
                )


            }


        }
        return 0
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as LeprosyScreeningCache).let { form ->
            form.homeVisitDate = getLongFromDate(dateOfCase.value)
            form.referToName = referredTo.value
            form.referredTo = referredTo.getPosition()
            form.otherReferredTo = other.value
            form.remarks = remarks.value
            form.leprosyStatus = leprosyStatus.value
            form.typeOfLeprosy = typeOfLeprosy.value
            form.diseaseTypeID = 5
            form.leprosySymptoms = leprosySymptoms.value
            form.visitLabel = visitLabel.value
            form.leprosySymptomsPosition = when (leprosySymptoms.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.isConfirmed = !typeOfLeprosy.value.isNullOrEmpty()
            form.treatmentStartDate = getLongFromDate(treatmentStartDate.value)
            form.syncState = SyncState.UNSYNCED

        }
    }


    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array2)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }

//

    fun getIndexOfDate(): Int {
        return getIndexById(dateOfCase.id)
    }

    private fun updateDateConstraints() {
        treatmentStartDate.min = homeVisitDateLong


    }
}
