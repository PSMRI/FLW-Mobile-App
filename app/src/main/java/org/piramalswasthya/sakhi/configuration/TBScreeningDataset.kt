package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.*

class TBScreeningDataset (
    context: Context, currentLanguage: Languages
    ) : Dataset(context, currentLanguage) {

    private val dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val isCoughing = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_coughing),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var bloodInSputum = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_blsputum),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var isFever = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_feverwks),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var lossOfWeight = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_lsweight),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var nightSweats = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_ntswets),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var historyOfTB = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_histb),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var currentlyTakingDrugs = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_taking_tb_drug),
        entries = arrayOf("Yes", "No"),
        required = true,
        doubleStar = true,
        hasDependants = false
    )

    private var familyHistoryTB = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.cbac_fh_tb),
        entries = arrayOf("Yes", "No"),
        doubleStar = true,
        required = true,
        hasDependants = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: TBScreeningCache?) {
        val list = mutableListOf(
            dateOfVisit,
            isCoughing,
            bloodInSputum,
            isFever,
            lossOfWeight,
            nightSweats,
            historyOfTB,
            currentlyTakingDrugs,
            familyHistoryTB
        )
        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
        } else {
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            isCoughing.value = if (saved.coughMoreThan2Weeks == true) "Yes" else "No"
            bloodInSputum.value = if (saved.bloodInSputum == true) "Yes" else "No"
            isFever.value = if (saved.feverMoreThan2Weeks == true) "Yes" else "No"
            lossOfWeight.value = if (saved.lossOfWeight == true) "Yes" else "No"
            nightSweats.value = if (saved.nightSweats == true) "Yes" else "No"
            historyOfTB.value = if (saved.historyOfTb == true) "Yes" else "No"
            currentlyTakingDrugs.value = if (saved.takingAntiTBDrugs == true) "Yes" else "No"
            familyHistoryTB.value = if (saved.familySufferingFromTB == true) "Yes" else "No"
        }


        ben?.let {
            dateOfVisit.min = it.regDate
        }
        setUpPage(list)

    }
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return -1
//        return when (formId) {
//        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as TBScreeningCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.coughMoreThan2Weeks = isCoughing.value == "Yes"
            form.bloodInSputum = bloodInSputum.value == "Yes"
            form.feverMoreThan2Weeks = isFever.value == "Yes"
            form.nightSweats = nightSweats.value == "Yes"
            form.lossOfWeight = lossOfWeight.value == "Yes"
            form.historyOfTb = historyOfTB.value == "Yes"
            form.takingAntiTBDrugs = currentlyTakingDrugs.value == "Yes"
            form.familySufferingFromTB = familyHistoryTB.value == "Yes"
        }
    }


    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }

    fun isTbSuspected(): String? {
        return if ( isCoughing.value == "Yes" ||
                bloodInSputum.value == "Yes" ||
                isFever.value == "Yes" ||
                lossOfWeight.value == "Yes" ||
                nightSweats.value == "Yes" ||
                lossOfWeight.value == "Yes" ||
                historyOfTB.value == "Yes")
            resources.getString(R.string.tb_suspected_alert) else null
    }

    fun isTbSuspectedFamily(): String? {
        return if (currentlyTakingDrugs.value == "Yes" || familyHistoryTB.value == "Yes")
            resources.getString(R.string.tb_suspected_family_alert) else null
    }
}