package org.piramalswasthya.sakhi.configuration

import android.content.Context
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
        required = false
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

    suspend fun setFirstPage(ben: BenRegCache, vaccine: Vaccine, imm: ImmunizationCache?) {
        val list = listOf(
//            name,
//            motherName,
//            dateOfBirth,
//            vaccineName,
//            doseNumber,
//            expectedDate,
            dateOfVaccination,
            vaccinatedPlace,
            vaccinatedBy
        )
        vaccineId = vaccine.vaccineId
//        name.value = ben.firstName ?: "Baby of ${ben.motherName}"
//        motherName.value = ben.motherName
//        dateOfBirth.value = getDateFromLong(ben.dob)
        vaccineName.value = vaccine.vaccineName.dropLastWhile { it.isDigit() }
        doseNumber.value = vaccine.vaccineName.takeLastWhile { it.isDigit() }
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


        }

    }



}