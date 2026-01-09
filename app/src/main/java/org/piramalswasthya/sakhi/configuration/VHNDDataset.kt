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
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.utils.HelperUtil.parseSelections
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VHNDDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

    }


    private val vhndDate = FormElement(
        id = 2,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.vhnd_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis(),
        max = System.currentTimeMillis()
    )
    private val place = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.place_of_vhnd),
        entries = resources.getStringArray(R.array.place_of_vhsnc),
        arrayId = -1,
        required = true,
        allCaps = true,
    )

    private val noOfBeneficiariesAttended = FormElement(
        id = 4,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.total_no_b_attended),
        arrayId = -1,
        required = true,
        value = "0",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 0
    )

    private val noOfPWAttended = FormElement(
        id = 5,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.total_no_pw_attended),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 0
    )
    private val noOflactingMotherAttended = FormElement(
        id = 6,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.total_no_lacting_mother_attended),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 999,
        min = 0
    )
    private val noOfchildrenAttended = FormElement(
        id = 7,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.total_no_children_attended),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 999,
        min = 0
    )
    private val heading = FormElement(
        id = 8,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.health_nutrition),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 0
    )

    private val knowOfBalanceDiet = FormElement(
        id = 8,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.know_balance_diet),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.diet_plane),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )
    private val careDuringPregnancy = FormElement(
        id = 9,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.care_d_pregnancy),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.care_pregnancy),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )
    private val importBreastFeeding = FormElement(
        id = 10,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.imp_brestfeeding_title),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.import_brestfeeding),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )
    private val complementFeeding = FormElement(
        id = 11,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.com_feeding_title),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.complementary_feeding),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )
    private val hygieneSenitation = FormElement(
        id = 12,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.hygiene_title),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.hygiene_senetigation),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
    )
    private val fPlanning = FormElement(
        id = 13,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.family_planning_title),
        arrayId = -1,
        required = false,
        entries = resources.getStringArray(R.array.family_planning_health),
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

    suspend fun setUpPage(vhnd: VHNDCache?) {


        if (pic1.value.isNullOrBlank()) {
            pic1.value = "default"
        }

        if (pic2.value.isNullOrBlank()) {
            pic2.value = "default"
        }

        val list = mutableListOf<FormElement>(
            vhndDate,
            place,
            noOfBeneficiariesAttended
        )

        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            list.addAll(
                listOf(
                    noOfPWAttended,
                    noOflactingMotherAttended,
                    noOfchildrenAttended,
                    heading,
                    knowOfBalanceDiet,
                    careDuringPregnancy,
                    importBreastFeeding,
                    complementFeeding,
                    hygieneSenitation,
                    fPlanning

                )
            )
        }

        list.addAll(
            listOf(
                pic1,
                pic2
            )
        )


        vhndDate.value = getCurrentDateString()
        vhnd?.let {
            vhndDate.value = it.vhndDate
            pic1.value = it.image1
            pic2.value = it.image2
            place.value = it.place
            noOfBeneficiariesAttended.value = it.noOfBeneficiariesAttended.toString()
            if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                noOfPWAttended.value = it.pregnantWomenAnc
                noOfPWAttended.value = it.pregnantWomenAnc
                noOflactingMotherAttended.value = it.lactatingMothersPnc
                noOfchildrenAttended.value = it.childrenImmunization
                val parsedKBDList = parseSelections(it.knowledgeBalancedDiet, knowOfBalanceDiet.entries!!)
                knowOfBalanceDiet.value = if (parsedKBDList.isNotEmpty()) {
                    parsedKBDList.joinToString(", ")
                } else {
                    it.knowledgeBalancedDiet ?: ""
                }
                val parsedCDPList = parseSelections(it.careDuringPregnancy, careDuringPregnancy.entries!!)
                careDuringPregnancy.value = if (parsedCDPList.isNotEmpty()) {
                    parsedCDPList.joinToString(", ")
                } else {
                    it.careDuringPregnancy ?: ""
                }
                val parsedBrestFeedingList = parseSelections(it.importanceBreastfeeding, importBreastFeeding.entries!!)
                importBreastFeeding.value = if (parsedBrestFeedingList.isNotEmpty()) {
                    parsedBrestFeedingList.joinToString(", ")
                } else {
                    it.importanceBreastfeeding ?: ""
                }

                val parsedComplementaryFeedingList = parseSelections(it.complementaryFeeding, complementFeeding.entries!!)
                complementFeeding.value = if (parsedComplementaryFeedingList.isNotEmpty()) {
                    parsedComplementaryFeedingList.joinToString(", ")
                } else {
                    it.complementaryFeeding ?: ""
                }

                val parsedHygieneSanitiList = parseSelections(it.hygieneSanitation, hygieneSenitation.entries!!)
                hygieneSenitation.value = if (parsedHygieneSanitiList.isNotEmpty()) {
                    parsedHygieneSanitiList.joinToString(", ")
                } else {
                    it.hygieneSanitation ?: ""
                }

                val parsedFamilyPlanList = parseSelections(it.familyPlanningHealthcare, fPlanning.entries!!)
                fPlanning.value = if (parsedFamilyPlanList.isNotEmpty()) {
                    parsedFamilyPlanList.joinToString(", ")
                } else {
                    it.familyPlanningHealthcare ?: ""
                }

            }

        }
        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            place.id -> {
                validateEmptyOnEditText(place)
                validateAllAlphabetsSpecialAndNumericOnEditText(place)
                -1
            }

            noOfBeneficiariesAttended.id -> {
                validateEmptyOnEditText(noOfBeneficiariesAttended)
                -1
            }
            noOfPWAttended.id -> {
                validateEmptyOnEditText(noOfPWAttended)
                -1
            }
            noOflactingMotherAttended.id -> {
                validateEmptyOnEditText(noOflactingMotherAttended)
                -1
            }
            noOfchildrenAttended.id -> {
                validateEmptyOnEditText(noOfchildrenAttended)
                -1
            }


            else -> {
                -1
            }
        }
    }



    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as VHNDCache).let { form ->

            form.vhndDate = vhndDate.value!!
            form.place = place.value
            form.vhndPlaceId = place.getPosition()
            form.noOfBeneficiariesAttended = noOfBeneficiariesAttended.value!!.toInt()
            if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                form.pregnantWomenAnc = noOfPWAttended.value.toString()
                form.lactatingMothersPnc = noOflactingMotherAttended.value.toString()
                form.childrenImmunization = noOfchildrenAttended.value.toString()
                form.knowledgeBalancedDiet = toCsv(knowOfBalanceDiet.value,knowOfBalanceDiet.entries!!)
                form.careDuringPregnancy = toCsv(careDuringPregnancy.value,careDuringPregnancy.entries!!)
                form.importanceBreastfeeding = toCsv(importBreastFeeding.value,importBreastFeeding.entries!!)
                form.complementaryFeeding = toCsv(complementFeeding.value,complementFeeding.entries!!)
                form.hygieneSanitation = toCsv(hygieneSenitation.value,hygieneSenitation.entries!!)
                form.familyPlanningHealthcare = toCsv(fPlanning.value,fPlanning.entries!!)

            }
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
    private fun toCsv(rawValue: String?, entries: Array<String>): String {
        return parseSelections(rawValue, entries).joinToString(", ")
    }
}