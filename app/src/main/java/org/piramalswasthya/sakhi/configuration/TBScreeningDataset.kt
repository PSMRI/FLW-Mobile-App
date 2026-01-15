package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.TBScreeningCache

class TBScreeningDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val isCoughing = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_coughing),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var bloodInSputum = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_blsputum),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var isFever = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_feverwks),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var lossOfWeight = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_lsweight),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var nightSweats = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_ntswets),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var historyOfTB = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_histb),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var currentlyTakingDrugs = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_taking_tb_drug),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        doubleStar = true,
        hasDependants = false
    )

    private var familyHistoryTB = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_fh_tb),
        entries = resources.getStringArray(R.array.yes_no),
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
            isCoughing.value =
                if (saved.coughMoreThan2Weeks == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            bloodInSputum.value =
                if (saved.bloodInSputum == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isFever.value =
                if (saved.feverMoreThan2Weeks == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            lossOfWeight.value =
                if (saved.lossOfWeight == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            nightSweats.value =
                if (saved.nightSweats == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            historyOfTB.value =
                if (saved.historyOfTb == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            currentlyTakingDrugs.value =
                if (saved.takingAntiTBDrugs == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            familyHistoryTB.value =
                if (saved.familySufferingFromTB == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
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
            form.coughMoreThan2Weeks =
                isCoughing.value == resources.getStringArray(R.array.yes_no)[0]
            form.bloodInSputum = bloodInSputum.value == resources.getStringArray(R.array.yes_no)[0]
            form.feverMoreThan2Weeks = isFever.value == resources.getStringArray(R.array.yes_no)[0]
            form.nightSweats = nightSweats.value == resources.getStringArray(R.array.yes_no)[0]
            form.lossOfWeight = lossOfWeight.value == resources.getStringArray(R.array.yes_no)[0]
            form.historyOfTb = historyOfTB.value == resources.getStringArray(R.array.yes_no)[0]
            form.takingAntiTBDrugs =
                currentlyTakingDrugs.value == resources.getStringArray(R.array.yes_no)[0]
            form.familySufferingFromTB =
                familyHistoryTB.value == resources.getStringArray(R.array.yes_no)[0]
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

    fun isTbSuspected(): String? {
        return if (isCoughing.value == resources.getStringArray(R.array.yes_no)[0] ||
            bloodInSputum.value == resources.getStringArray(R.array.yes_no)[0] ||
            isFever.value == resources.getStringArray(R.array.yes_no)[0] ||
            nightSweats.value == resources.getStringArray(R.array.yes_no)[0] ||
            lossOfWeight.value == resources.getStringArray(R.array.yes_no)[0] ||
            historyOfTB.value == resources.getStringArray(R.array.yes_no)[0]
        )
            resources.getString(R.string.tb_suspected_alert) else null
    }

    fun isTbSuspectedFamily(): String? {
        return if (currentlyTakingDrugs.value == resources.getStringArray(R.array.yes_no)[0] || familyHistoryTB.value == resources.getStringArray(
                R.array.yes_no
            )[0]
        )
            resources.getString(R.string.tb_suspected_family_alert) else null
    }

    fun getIndexOfDate(): Int {
        return getIndexById(dateOfVisit.id)
    }
}