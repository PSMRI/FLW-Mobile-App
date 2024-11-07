package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType

class FacilitatorActivityFromDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {


    private val subCenter = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.sub_center),
        required = false,
    )

    private var ashaName = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.asha_name),
        required = true,
    )

    private val dateOfEvent = FormElement(
        id = 3,
        inputType = InputType.DATE_PICKER,
        arrayId = -1,
        title = resources.getString(R.string.date_of_event),
        max = System.currentTimeMillis(),
        required = false,
    )

    private val activityNames = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.activity_heading),
        arrayId = R.array.activity_names,
        entries = resources.getStringArray(R.array.activity_names),
        required = true,
    )

    private var activityDetailsHeader = FormElement(
        id = 5,
        inputType = InputType.TEXT_HEADLINE,
        value = resources.getString(R.string.details_activity),
        title = resources.getString(R.string.details_activity),
        required = false,
    )

    private val dateOfVisit = FormElement(
        id = 6,
        inputType = InputType.DATE_PICKER,
        arrayId = -1,
        title = resources.getString(R.string.date_of_visit),
        max = System.currentTimeMillis(),
        required = false,
    )

    private val activityConducted = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.activity_conducted),
        entries = resources.getStringArray(R.array.activity_conducted_choice),
        required = false,
    )

    private var issuesIdentified = FormElement(
        id = 8,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.issues_identified),
        required = true,
    )

    private var actionTaken = FormElement(
        id = 9,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.action_taken),
        required = true,
    )

    private val followUpWithAsha = FormElement(
        id = 10,
        inputType = InputType.DATE_PICKER,
        arrayId = -1,
        title = resources.getString(R.string.follow_up_date),
        max = System.currentTimeMillis(),
        required = false,
    )

    private val attendance = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.attendance),
        entries = resources.getStringArray(R.array.present_absent),
        required = true,
    )

    private var topic = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.topic),
        required = true,
    )

    private var actionItem = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.action_item),
        required = true,
    )




    suspend fun setUpPage() {
        val list = mutableListOf(
            subCenter,
            ashaName,
            dateOfEvent,
            activityNames,
            activityDetailsHeader
        )

        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            activityNames.id -> {
                if (activityNames.value == resources.getStringArray(R.array.activity_names)[0]) {
                    activityDetailsHeader.value = activityNames.value
                    triggerDependants(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 0,
                        target = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha),
                        targetSideEffect = listOf(attendance,topic,actionItem)
                    )
                    triggerforHide(
                        source = activityNames,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = attendance,
                        targetSideEffect = listOf(attendance,topic,actionItem)
                    )

                } else if (activityNames.value == resources.getStringArray(R.array.activity_names)[1]) {
                    activityDetailsHeader.value = activityNames.value
                    triggerDependants(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = listOf(attendance,topic,actionItem),
                        targetSideEffect = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha)
                    )
                    triggerforHide(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = dateOfVisit,
                        targetSideEffect = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha)
                    )

                } else if (activityNames.value == resources.getStringArray(R.array.activity_names)[2]) {
                    activityDetailsHeader.value = activityNames.value
                    triggerDependants(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 2,
                        target = listOf(attendance,topic,actionItem),
                        targetSideEffect = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha)
                    )
                    triggerforHide(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = dateOfVisit,
                        targetSideEffect = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha)
                    )

                } else if (activityNames.value == resources.getStringArray(R.array.activity_names)[3]) {
                    activityDetailsHeader.value = activityNames.value
                    triggerDependants(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 2,
                        target = listOf(attendance,topic,actionItem),
                        targetSideEffect = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha,attendance,topic,actionItem)
                    )
                    triggerforHide(
                        source = activityDetailsHeader,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = dateOfVisit,
                        targetSideEffect = listOf(dateOfVisit,activityConducted,issuesIdentified,actionTaken,followUpWithAsha,attendance,topic,actionItem)
                    )


                }
                return 0
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {

    }

    fun updateBen(benRegCache: BenRegCache) {

    }
}
