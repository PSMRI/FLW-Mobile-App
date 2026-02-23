package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.MDSRCache

class MDSRFormDataset(
    context: Context,
    currentLanguage: Languages,
    val preferences: PreferenceDao,
    val pregnancyDeath: Boolean = false,
    val abortionDeath: Boolean = false,
    val deliveryDeath: Boolean = false,
    val pncDeath: Boolean = false,
    val pncDeathCause: Boolean = false,
    val ancDeathCause: Boolean = false

) : Dataset(context, currentLanguage) {


    private val dateOfDeath = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = "Date of death ",
        min = 0L,
        max = System.currentTimeMillis(),
        required = true
    )
    private val address = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = "Address",
        required = false
    )
    private val husbandName = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        etMaxLength = 50,
        title = "Husband’s Name",
        required = false
    )
    private val causeOfDeath = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Cause of death",
        required = true,
        isEnabled = false,
        hasDependants = true,
        entries = arrayOf("Suspected Maternal death", "Non-maternal death")
    )
    private val reasonOfDeath = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = "Specify Reason",
        required = true
    )
    private val investigationDate = FormElement(
        id = 6,
        inputType = InputType.DATE_PICKER,
        title = "Date of field investigation",
        min = 0L,
        max = System.currentTimeMillis(),
        required = false
    )
    private val actionTaken = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Action Take",
        required = false,
        orientation = LinearLayout.VERTICAL,
        entries = arrayOf("Yes", "No")
    )
    private val blockMOSign = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = "Signature of MO I/C of the block",
        required = false
    )
    private val dateIc = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        min = 0L,
        max = System.currentTimeMillis(),
        title = "Date",
        required = false
    )
    private val duringPregnancy = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.a_during_pregnancy),
        required = false,
        isEnabled = false,
        entries = resources.getStringArray(R.array.yes_no)
    )

    private val duringDelivery = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.b_during_delivery),
        required = false,
        isEnabled = false,
        entries = resources.getStringArray(R.array.yes_no)
    )
    private val duringDelivery42Days = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.c_within_42_days_after_delivery),
        required = false,
        isEnabled = false,
        entries = resources.getStringArray(R.array.yes_no)
    )
    private val duringAbortion6Weeks = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.d_during_abortion_or_within_6_weeks_after_abortion),
        required = false,
        isEnabled = false,
        entries = resources.getStringArray(R.array.yes_no)
    )

    private val nameOfReportingPerson = FormElement(
        id = 18,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.name_of_reporting_person),
        required = false
    )

    private val designation = FormElement(
        id = 18,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.designation),
        required = false
    )

    private val veryficationByANM = FormElement(
        id = 18,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.verification_by_anm_of_the_respective_sub_center_that_death_of_women_occurred_during_pregnancy_or_within_42_days_of_delivery_abortion),
        required = false
    )
    private val nameOdANM = FormElement(
        id = 18,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.name_of_anm),
        required = false
    )
    private val nameOfSubCenter = FormElement(
        id = 18,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.name_of_the_sub_center),
        required = false
    )

    private val mdsrFileUpload1 = FormElement(
        id = 21,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.mdsr_form_from_anm_1),
        required = false,
    )
    private val mdsrFileUpload2 = FormElement(
        id = 22,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.mdsr_form_from_anm_2),
        required = false,
    )
    private val mdsrDeathFileUpload = FormElement(
        id = 23,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.death_certificate),
        required = false,
    )

    //................changes BRD................................
    private val state = FormElement(
        id = 24,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.state),
        required = false
    )
    private val district = FormElement(
        id = 25,
        inputType = InputType.TEXT_VIEW,
        title =  context.getString(R.string.district),
        required = false
    )
    private val block = FormElement(
        id = 26,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.block),
        required = false
    )
    private val village = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.village_town_city),
        required = false
    )

    private val fatherName = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.father_name),
        required = false
    )
    private val ageOfWomen = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.age_of_woman),
        required = false
    )

    private val rchID = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.rch_id),
        required = false
    )


    private val mobileNo = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.mobile_number),
        required = false
    )

    private val timeOfDeath = FormElement(
        id = 16,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.time_of_death),
        required = false
    )
    private val placeOfDate = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.place_of_death),
        required = false
    )

    private val whenDidDeathOccur = FormElement(
        id = 27,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.when_did_death_occur),
        required = false
    )
    private val nameOfDeceased  = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.name_of_the_deceased_woman),
        required = false
    )


//.............................................................

    suspend fun setUpPage(
        ben: BenRegCache,
        address: String,
        saved: MDSRCache?
    ) {
        val list = mutableListOf(
            state,
            district,
            block,
            village,
            nameOfDeceased,
            husbandName,
            fatherName,
            ageOfWomen,
            rchID,
            mobileNo,
            dateOfDeath,
            timeOfDeath,
            placeOfDate,
            whenDidDeathOccur,
            duringPregnancy,
            duringDelivery,
            duringDelivery42Days,
            duringAbortion6Weeks,
            causeOfDeath,
            nameOfReportingPerson,
            designation,
            veryficationByANM,
            nameOdANM,
            nameOfSubCenter,
            dateIc,
            mdsrFileUpload1,
            mdsrFileUpload2,
            mdsrDeathFileUpload,
//            this.address,
//            investigationDate,
//            actionTaken,
//            blockMOSign,

        )

        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            list.remove(mdsrFileUpload1)
            list.remove(mdsrFileUpload2)
            list.remove(mdsrDeathFileUpload)
        }

        this.address.value = address
        husbandName.value = ben.genDetails?.spouseName
        val user = preferences.getLoggedInUser()
        nameOfReportingPerson.value = user?.userName ?: ""
        designation.value="Asha"
        state.value = user?.state?.name ?: ""
        district.value = user?.district?.name ?: ""
        block.value = user?.block?.name ?: ""
//        nameOfSubCenter.value = user?.subs?.name ?: ""
        village.value = user!!.villages[0].name
        placeOfDate.value = ben.placeOfDeath
        rchID.value = ben.rchId
        ageOfWomen.value = ben.age.toString()
        fatherName.value=ben.fatherName
        dateOfDeath.value=ben.dateOfDeath
        timeOfDeath.value=ben.timeOfDeath
        nameOfDeceased.value = "${ben.firstName} ${ben.lastName}"
        duringPregnancy.value = if (pregnancyDeath) "Yes" else "No"
        duringDelivery.value = if (deliveryDeath) "Yes" else "No"
        duringDelivery42Days.value = if (pncDeath) "Yes" else "No"
        duringAbortion6Weeks.value = if (abortionDeath) "Yes" else "No"

        val causeOfDeathValue = if (pregnancyDeath || abortionDeath || deliveryDeath || pncDeath) {
            if(pncDeathCause || ancDeathCause)
                "Non-maternal death"
            else
                "Suspected Maternal death"
        } else {
            "Non-maternal death"
        }
        causeOfDeath.value = causeOfDeathValue
        saved?.let { mdsr ->
            dateOfDeath.value = mdsr.dateOfDeath?.let { getDateFromLong(it) }
            this.address.value = mdsr.address
            husbandName.value = mdsr.husbandName
            causeOfDeath.value = mdsr.causeOfDeath
            reasonOfDeath.value = mdsr.reasonOfDeath
            mdsrFileUpload1.value=mdsr.mdsr1File
            mdsrFileUpload2.value=mdsr.mdsr2File
            mdsrDeathFileUpload.value=mdsr.mdsrDeathCertFile
            investigationDate.value = mdsr.investigationDate?.let { getDateFromLong(it) }
            actionTaken.value = mdsr.actionTaken?.let {
                if (it) resources.getString(R.string.yes) else resources.getString(R.string.no)
            }
            blockMOSign.value = mdsr.blockMOSign
            dateIc.value = mdsr.dateIc?.let { getDateFromLong(it) }
        }
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            causeOfDeath.id -> triggerDependants(
                source = causeOfDeath,
                passedIndex = index,
                triggerIndex = 0,
                target = listOf(reasonOfDeath)
            )

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as MDSRCache).let { mdsrCache ->
            mdsrCache.dateOfDeath = getLongFromDate(dateOfDeath.value!!)
            mdsrCache.address = address.value
            mdsrCache.husbandName = husbandName.value
            mdsrCache.mdsr1File = mdsrFileUpload1.value
            mdsrCache.mdsr2File = mdsrFileUpload2.value
            mdsrCache.mdsrDeathCertFile = mdsrDeathFileUpload.value
            mdsrCache.causeOfDeath = causeOfDeath.value
            mdsrCache.reasonOfDeath = reasonOfDeath.value
            mdsrCache.actionTaken = actionTaken.value?.let { actionTaken.value == "Yes" }
            mdsrCache.investigationDate = investigationDate.value?.let { getLongFromDate(it) }
            mdsrCache.blockMOSign = blockMOSign.value
            mdsrCache.dateIc = dateIc.value?.let { getLongFromDate(it) }
            mdsrCache.createdDate = System.currentTimeMillis()
        }
    }



    fun getIndexOfMDSR1() = getIndexById(mdsrFileUpload1.id)
    fun getIndexOfMDSR2() = getIndexById(mdsrFileUpload2.id)
    fun getIndexOfIsDeathCertificate() = getIndexById(mdsrDeathFileUpload.id)


    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {

        when (lastImageFormId) {
            21 -> {
                mdsrFileUpload1.value = dpUri.toString()
                mdsrFileUpload1.errorText = null
            }

            22 -> {
                mdsrFileUpload2.value = dpUri.toString()
                mdsrFileUpload2.errorText = null
            }

            23 -> {
                mdsrDeathFileUpload.value = dpUri.toString()
                mdsrDeathFileUpload.errorText = null
            }

        }
    }

}