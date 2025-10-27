package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.Vaccine

class ImmunizationDataset(context: Context, language: Languages) : Dataset(context, language) {

    private var vaccineId: Int = 0


    /*private val name = FormElement(
        id = 100,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.name_ben),
        required = false
    )


    private val motherName = FormElement(
        id = 101,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.mother_s_name),
        required = false
    )
    private val dateOfBirth = FormElement(
        id = 102,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.date_of_birth),
        required = false
    )*/

    //    private val dateOfPrevVaccination = FormElement(
//        id = 103,
//        inputType = InputType.EDIT_TEXT,
//        title = "Date of vaccination",
//        required = false
//    )
//    private val numDoses = FormElement(
//        id = 104,
//        inputType = InputType.EDIT_TEXT,
//        title = "No. of Doses Taken",
//        required = false
//    )
    private val vaccineName = FormElement(
        id = 105,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.vaccine_name),
        required = false
    )
    private val doseNumber = FormElement(
        id = 106,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.dose_number),
        required = false
    )
    private val expectedDate = FormElement(
        id = 107,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.expected_date),
        required = false
    )
    private val dateOfVaccination = FormElement(
        id = 108,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_vaccination),
        max = System.currentTimeMillis(),
        required = true
    )
    private val vaccinatedPlace = FormElement(
        id = 109,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.vaccinated_place),
        arrayId = R.array.imm_vaccinated_place_array,
        entries = resources.getStringArray(R.array.imm_vaccinated_place_array),
        required = false
    )
    private val vaccinatedBy = FormElement(
        id = 110,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.vaccinated_by),
        arrayId = R.array.imm_vaccinated_by_array,
        entries = resources.getStringArray(R.array.imm_vaccinated_by_array),
        required = false
    )

    private val doseName = FormElement(
        id = 113,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.dose_name),
        required = false
    )

    private val vaccinationDueDate = FormElement(
        id = 114,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.vaccination_due_date),
        required = false
    )

    val mcpCard1 = FormElement(
        id = 111,
        inputType = InputType.FILE_UPLOAD,
        required = false,
        title = context.getString(R.string.mcp_card_1)
    )
    val mcpCard2 = FormElement(
        id = 112,
        inputType = InputType.FILE_UPLOAD,
        required = false,
        title = context.getString(R.string.mcp_card_2)
    )


    suspend fun setFirstPage(ben: BenRegCache, vaccine: Vaccine, imm: ImmunizationCache?) {
        val list = listOf(
//            name,
//            motherName,
//            dateOfBirth,
//            vaccineName,
//            doseNumber,
//            expectedDate,
            vaccineName,
            vaccinationDueDate,
            dateOfVaccination,
            vaccinatedPlace,
            vaccinatedBy,
            mcpCard1,
            mcpCard2
        )
        vaccineId = vaccine.vaccineId
//        name.value = ben.firstName ?: "Baby of ${ben.motherName}"
//        motherName.value = ben.motherName
//        dateOfBirth.value = getDateFromLong(ben.dob)
//        doseName.value = vaccine.immunizationService.name
        vaccineName.value = vaccine.vaccineName.dropLastWhile { it.isDigit() }
        doseNumber.value = vaccine.vaccineName.takeLastWhile { it.isDigit() }
        vaccinationDueDate.value = getDateFromLong(ben.dob + vaccine.maxAllowedAgeInMillis)
        expectedDate.value =
            getDateFromLong(ben.dob + vaccine.maxAllowedAgeInMillis)
        dateOfVaccination.value = getDateFromLong(System.currentTimeMillis())
        dateOfVaccination.min = ben.dob + vaccine.minAllowedAgeInMillis
        if (System.currentTimeMillis() > ben.dob + vaccine.maxAllowedAgeInMillis) {
            dateOfVaccination.max = ben.dob + vaccine.maxAllowedAgeInMillis
        }


        imm?.let { saved ->
            dateOfVaccination.value = saved.date?.let { getDateFromLong(it) }
            vaccinatedPlace.value = getLocalValueInArray(vaccinatedPlace.arrayId, saved.place)
            vaccinatedBy.value = getLocalValueInArray(vaccinatedBy.arrayId, saved.byWho)
            mcpCard1.value = imm.mcpCardSummary1
            mcpCard2.value = imm.mcpCardSummary2
        }
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int) = -1

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as ImmunizationCache).let {
            it.date = dateOfVaccination.value?.let { getLongFromDate(it) }
//            it.placeId= vaccinatedPlace.getPosition()
            //     it.vaccineId = vaccineId
            it.place =
                vaccinatedPlace.getEnglishStringFromPosition(vaccinatedPlace.getPosition()) ?: ""
//            it.byWhoId= vaccinatedBy.getPosition()
            it.byWho = vaccinatedBy.getEnglishStringFromPosition(vaccinatedBy.getPosition()) ?: ""
            it.mcpCardSummary1 = mcpCard1.value?.takeIf { it.isNotEmpty() }
            it.mcpCardSummary2 = mcpCard2.value?.takeIf { it.isNotEmpty() }

        }

    }


    fun getIndexMCPCard1() = getIndexById(mcpCard1.id)
    fun getIndexMCPCard2() = getIndexById(mcpCard2.id)

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            mcpCard1.id -> {
                mcpCard1.value = dpUri.toString()
                mcpCard1.errorText = null
            }

            mcpCard2.id -> {
                mcpCard2.value = dpUri.toString()
                mcpCard2.errorText = null
            }
        }
    }

}