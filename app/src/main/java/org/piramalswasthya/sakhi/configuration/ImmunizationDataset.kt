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

    private val name = FormElement(
        id = 100,
        inputType = InputType.TEXT_VIEW,
        title = "Name",
        required = false
    )


    private val motherName = FormElement(
        id = 101,
        inputType = InputType.TEXT_VIEW,
        title = "Mother's Name",
        required = false
    )
    private val dateOfBirth = FormElement(
        id = 102,
        inputType = InputType.TEXT_VIEW,
        title = "Date Of Birth",
        required = false
    )

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
        title = "Vaccine Name",
        required = false
    )
    private val doseNumber = FormElement(
        id = 106,
        inputType = InputType.TEXT_VIEW,
        title = "Dose Number",
        required = false
    )
    private val expectedDate = FormElement(
        id = 107,
        inputType = InputType.TEXT_VIEW,
        title = "Expected Date",
        required = false
    )
    private val dateOfVaccination = FormElement(
        id = 108,
        inputType = InputType.DATE_PICKER,
        title = "Date of Vaccination",
        min = System.currentTimeMillis(),
        required = false
    )
    private val vaccinatedPlace = FormElement(
        id = 109,
        inputType = InputType.DROPDOWN,
        title = "Vaccinated Place",
        arrayId = R.array.imm_vaccinated_place_array,
        entries = resources.getStringArray(R.array.imm_vaccinated_place_array),
        required = false
    )
    private val vaccinatedBy = FormElement(
        id = 110,
        inputType = InputType.DROPDOWN,
        title = "Vaccinated By",
        arrayId = R.array.imm_vaccinated_by_array,
        entries = resources.getStringArray(R.array.imm_vaccinated_by_array),
        required = false
    )

    suspend fun setFirstPage(ben: BenRegCache, vaccine: Vaccine, imm: ImmunizationCache?) {
        val list = listOf(
            name,
            motherName,
            dateOfBirth,
            vaccineName,
            doseNumber,
            expectedDate,
            dateOfVaccination,
            vaccinatedPlace,
            vaccinatedBy
        )
        name.value = ben.firstName ?: "Baby of ${ben.motherName}"
        motherName.value = ben.motherName
        dateOfBirth.value = getDateFromLong(ben.dob)
        vaccineName.value = vaccine.vaccineName.dropLastWhile { it.isDigit() }
        doseNumber.value = vaccine.vaccineName.takeLastWhile { it.isDigit() }
        expectedDate.value =
            getDateFromLong(ben.dob + vaccine.minAllowedAgeInMillis + vaccine.overdueDurationSinceMinInMillis)
        dateOfVaccination.value = getDateFromLong(System.currentTimeMillis())

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
            it.place = vaccinatedPlace.getEnglishStringFromPosition(vaccinatedPlace.getPosition())?:""
//            it.byWhoId= vaccinatedBy.getPosition()
            it.byWho = vaccinatedBy.getEnglishStringFromPosition(vaccinatedBy.getPosition())?:""


        }

    }

}