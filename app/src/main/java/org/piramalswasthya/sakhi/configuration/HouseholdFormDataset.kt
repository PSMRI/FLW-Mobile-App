package org.piramalswasthya.sakhi.configuration


import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HouseholdAmenities
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.HouseholdDetails
import org.piramalswasthya.sakhi.model.HouseholdFamily
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.HEADLINE
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HouseholdFormDataset(context: Context, language: Languages) : Dataset(context, language) {
    companion object {
        private fun getCurrentDate(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

        private fun getCurrentTime(): String {
            val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val date = Date()
            return dateFormat.format(date)
        }

        private fun getHHidFromUserId(userId: Int): Long {
            val date = getCurrentDate().replace("-", "")
            val time = getCurrentTime().replace(":", "")
            return (date + time + userId).toLong()
        }
    }

    //////////////////////////////// First Page /////////////////////////////////////////

    private val familyHeading = FormElement(
        id = 17498,
        inputType = HEADLINE,
        title = resources.getString(R.string.nhhr_title_page_1),
        required = false,
        headingLine = false

    )

    private val firstNameHeadOfFamily = FormElement(
        id = 0,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_first_name_hof),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val lastNameHeadOfFamily = FormElement(
        id = 1,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_last_name_hof),
        arrayId = -1,
        required = false,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val mobileNoHeadOfFamily = FormElement(
        id = 2,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_mob_no_hof),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )
    private val houseNo = FormElement(
        id = 3,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_house_no),
        arrayId = -1,
        required = false,
        etMaxLength = 100
    )
    private val wardNo = FormElement(
        id = 4,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_ward_no),
        arrayId = -1,
        required = false,
        etMaxLength = 100
    )
    private val wardName = FormElement(
        id = 5,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_ward_name),
        arrayId = -1,
        required = false,
        etMaxLength = 100
    )
    private val mohallaName = FormElement(
        id = 6,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_mohalla_name),
        arrayId = -1,
        required = false,
        etMaxLength = 100
    )
    private val povertyLine = FormElement(
        id = 7,
        inputType = RADIO,
        title = resources.getString(R.string.nhhr_poverty_line),
        arrayId = R.array.nhhr_poverty_line_array,
        entries = resources.getStringArray(R.array.nhhr_poverty_line_array),
        required = true
    )

    suspend fun setupPage(hh: HouseholdCache?) {

        val list = mutableListOf<FormElement>()
        val firstPage by lazy {
            listOf(
                familyHeading,
                firstNameHeadOfFamily,
                lastNameHeadOfFamily,
                mobileNoHeadOfFamily,
                houseNo,
                wardNo,
                wardName,
                mohallaName,
                povertyLine
            )
        }
        list.addAll(firstPage)
        hh?.family?.let { saved ->
            firstNameHeadOfFamily.value = saved.familyHeadName
            lastNameHeadOfFamily.value = saved.familyName
            mobileNoHeadOfFamily.value = saved.familyHeadPhoneNo.toString()
            saved.familyHeadName.takeIf { it!!.isNotEmpty() }?.let { firstNameHeadOfFamily.inputType = TEXT_VIEW }
            saved.familyName.takeIf { it!!.isNotEmpty() }?.let { lastNameHeadOfFamily.inputType = TEXT_VIEW }
            saved.familyHeadPhoneNo.takeIf { it != null }?.let { mobileNoHeadOfFamily.inputType = TEXT_VIEW }
            houseNo.value = saved.houseNo
            wardNo.value = saved.wardNo
            wardName.value = saved.wardName
            mohallaName.value = saved.mohallaName
            povertyLine.value = povertyLine.getStringFromPosition(saved.povertyLineId)
        }


        val secondPage =
            listOf(
                houseHoldDetails, residentialArea, typeOfHouse, houseOwnership
            )
        list.addAll(secondPage)
        hh?.details?.let { saved ->
            residentialArea.value = residentialArea.getStringFromPosition(saved.residentialAreaId)
            otherResidentialArea.value = saved.otherResidentialArea
            typeOfHouse.value = typeOfHouse.getStringFromPosition(saved.houseTypeId)
            houseOwnership.value = houseOwnership.getStringFromPosition(saved.isHouseOwnedId)
        }
        if (residentialArea.value == residentialArea.entries!![3]) {
            list.add(list.indexOf(residentialArea) + 1, otherResidentialArea)
        }
        val thirdPage =
            listOf(
                houseHoldDAmenities,
                separateKitchen,
                fuelForCooking,
                sourceOfWater,
                sourceOfElectricity,
                availOfToilet
            )
        list.addAll(thirdPage)
        hh?.amenities?.let { saved ->
            separateKitchen.value = separateKitchen.getStringFromPosition(saved.separateKitchenId)
            fuelForCooking.value = fuelForCooking.getStringFromPosition(saved.fuelUsedId)
            otherFuelForCooking.value = saved.otherFuelUsed
            sourceOfWater.value = sourceOfWater.getStringFromPosition(saved.sourceOfDrinkingWaterId)
            otherSourceOfWater.value = saved.otherSourceOfDrinkingWater
            sourceOfElectricity.value =
                sourceOfElectricity.getStringFromPosition(saved.availabilityOfElectricityId)
            otherSourceOfElectricity.value = saved.otherAvailabilityOfElectricity
            availOfToilet.value = availOfToilet.getStringFromPosition(saved.availabilityOfToiletId)
            otherAvailOfToilet.value = saved.otherAvailabilityOfToilet
        }
        if (fuelForCooking.value == fuelForCooking.entries!!.last()) {
            list.add(list.indexOf(fuelForCooking) + 1, otherFuelForCooking)
        }
        if (sourceOfWater.value == sourceOfWater.entries!!.last()) {
            list.add(list.indexOf(sourceOfWater) + 1, otherSourceOfWater)
        }
        if (sourceOfElectricity.value == sourceOfElectricity.entries!!.last()) {
            list.add(list.indexOf(sourceOfElectricity) + 1, otherSourceOfElectricity)
        }
        if (availOfToilet.value == availOfToilet.entries!!.let { it[it.size - 2] }) {
            list.add(list.indexOf(availOfToilet) + 1, otherAvailOfToilet)
        }
        setUpPage(list)
    }

    suspend fun setFirstPage(family: HouseholdFamily?) {
        val firstPage by lazy {
            listOf(
                firstNameHeadOfFamily,
                lastNameHeadOfFamily,
                mobileNoHeadOfFamily,
                houseNo,
                wardNo,
                wardName,
                mohallaName,
                povertyLine
            )
        }
        setUpPage(firstPage)
        family?.let { saved ->
            firstNameHeadOfFamily.value = saved.familyHeadName
            lastNameHeadOfFamily.value = saved.familyName
            mobileNoHeadOfFamily.value = saved.familyHeadPhoneNo.toString()
            houseNo.value = saved.houseNo
            wardNo.value = saved.wardNo
            wardName.value = saved.wardName
            mohallaName.value = saved.mohallaName
            povertyLine.value = povertyLine.getStringFromPosition(saved.povertyLineId)
        }
    }

//////////////////////////////// Second Page /////////////////////////////////////////

    private val houseHoldDetails = FormElement(
        id = 17498,
        inputType = HEADLINE,
        title = resources.getString(R.string.nhhr_title_page_2),
        required = false
    )

    private val residentialArea = FormElement(
        id = 8,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nhhr_type_residential_area),
        arrayId = R.array.nhhr_type_residential_area_array,
        entries = resources.getStringArray(R.array.nhhr_type_residential_area_array),
        required = false,
        hasDependants = true
    )


    private val otherResidentialArea = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_type_residential_area_other),
        arrayId = -1,
        required = true,
        etMaxLength = 100
    )

    private val typeOfHouse = FormElement(
        id = 10,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nhhr_type_of_house),
        arrayId = R.array.nhhr_type_of_house_array,
        entries = resources.getStringArray(R.array.nhhr_type_of_house_array),
        required = true
    )
    private val houseOwnership = FormElement(
        id = 11,
        inputType = RADIO,
        title = resources.getString(R.string.nhhr_house_own),
        arrayId = R.array.nhhr_house_own_array,
        entries = resources.getStringArray(R.array.nhhr_house_own_array),
        required = true
    )


    suspend fun setSecondPage(details: HouseholdDetails?) {
        val secondPage by lazy {
            listOf(
                residentialArea, typeOfHouse, houseOwnership
            )
        }
        val list = secondPage.toMutableList()
        details?.let { saved ->
            residentialArea.value = residentialArea.getStringFromPosition(saved.residentialAreaId)
            otherResidentialArea.value = saved.otherResidentialArea
            typeOfHouse.value = typeOfHouse.getStringFromPosition(saved.houseTypeId)
            houseOwnership.value = houseOwnership.getStringFromPosition(saved.isHouseOwnedId)
        }
        if (residentialArea.value == residentialArea.entries!!.last()) {
            list.add(list.indexOf(residentialArea) + 1, otherResidentialArea)
        }
        setUpPage(list)
    }


//////////////////////////////// Third Page /////////////////////////////////////////

    private val houseHoldDAmenities = FormElement(
        id = 17498,
        inputType = HEADLINE,
        title = resources.getString(R.string.nhhr_title_page_3),
        required = false
    )

    private val separateKitchen = FormElement(
        id = 12,
        inputType = RADIO,
        title = resources.getString(R.string.nhhr_separate_kitchen),
        arrayId = R.array.nhhr_separate_kitchen_array,
        entries = resources.getStringArray(R.array.nhhr_separate_kitchen_array),
        required = true
    )

    private val fuelForCooking = FormElement(
        id = 13,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nhhr_fuel_cooking),
        arrayId = R.array.nhhr_fuel_cooking_array,
        entries = resources.getStringArray(R.array.nhhr_fuel_cooking_array),
        required = true,
        hasDependants = true,
    )

    private val otherFuelForCooking = FormElement(
        id = 14,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_fuel_cooking_other),
        arrayId = -1,
        required = true,
        etMaxLength = 100
    )


    private val sourceOfWater = FormElement(
        id = 15,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nhhr_primary_water),
        arrayId = R.array.nhhr_primary_water_array,
        entries = resources.getStringArray(R.array.nhhr_primary_water_array),
        required = true,
        hasDependants = true,
    )

    private val otherSourceOfWater = FormElement(
        id = 16,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_primary_water_other),
        arrayId = -1,
        required = true,
        etMaxLength = 100
    )


    private val sourceOfElectricity = FormElement(
        id = 17,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nhhr_avail_electricity),
        arrayId = R.array.nhhr_avail_electricity_array,
        entries = resources.getStringArray(R.array.nhhr_avail_electricity_array),
        required = true,
        hasDependants = true
    )

    private val otherSourceOfElectricity = FormElement(
        id = 18,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_avail_electricity_other),
        arrayId = -1,
        required = true,
        etMaxLength = 100
    )


    private val availOfToilet = FormElement(
        id = 19,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nhhr_avail_toilet),
        arrayId = R.array.nhhr_avail_toilet_array,
        entries = resources.getStringArray(R.array.nhhr_avail_toilet_array),
        required = true,
        hasDependants = true,
    )

    private val otherAvailOfToilet = FormElement(
        id = 20,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nhhr_avail_toilet_other),
        arrayId = -1,
        required = true,
        etMaxLength = 100
    )


    suspend fun setThirdPage(amenities: HouseholdAmenities?) {
        val thirdPage =
            listOf(
                separateKitchen, fuelForCooking, sourceOfWater, sourceOfElectricity, availOfToilet
            )
        val list = thirdPage.toMutableList()
        amenities?.let { saved ->
            separateKitchen.value = separateKitchen.getStringFromPosition(saved.separateKitchenId)
            fuelForCooking.value = fuelForCooking.getStringFromPosition(saved.fuelUsedId)
            otherFuelForCooking.value = saved.otherFuelUsed
            sourceOfWater.value = sourceOfWater.getStringFromPosition(saved.sourceOfDrinkingWaterId)
            otherSourceOfWater.value = saved.otherSourceOfDrinkingWater
            sourceOfElectricity.value =
                sourceOfElectricity.getStringFromPosition(saved.availabilityOfElectricityId)
            otherSourceOfElectricity.value = saved.otherAvailabilityOfElectricity
            availOfToilet.value = availOfToilet.getStringFromPosition(saved.availabilityOfToiletId)
            otherAvailOfToilet.value = saved.otherAvailabilityOfToilet
        }
        if (fuelForCooking.value == fuelForCooking.entries!!.last()) {
            list.add(list.indexOf(fuelForCooking) + 1, otherFuelForCooking)
        }
        if (sourceOfWater.value == sourceOfWater.entries!!.last()) {
            list.add(list.indexOf(sourceOfWater) + 1, otherSourceOfWater)
        }
        if (sourceOfElectricity.value == sourceOfElectricity.entries!!.last()) {
            list.add(list.indexOf(sourceOfElectricity) + 1, otherSourceOfElectricity)
        }
        if (availOfToilet.value == availOfToilet.entries!!.let { it[it.size - 2] }) {
            list.add(list.indexOf(availOfToilet) + 1, otherAvailOfToilet)
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            firstNameHeadOfFamily.id -> {
                validateEmptyOnEditText(firstNameHeadOfFamily)
                 //  validateAllCapsOrSpaceOnEditText(firstNameHeadOfFamily)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(firstNameHeadOfFamily)
            }
            /*These validations commented because Hindi and Asamese language name and sirname should store in db
            *
            * */
            lastNameHeadOfFamily.id ->{
                validateEmptyOnEditText(lastNameHeadOfFamily)
            // validateAllCapsOrSpaceOnEditText(lastNameHeadOfFamily)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(lastNameHeadOfFamily)
            }
            mobileNoHeadOfFamily.id -> {
                validateEmptyOnEditText(mobileNoHeadOfFamily)
                validateMobileNumberOnEditText(mobileNoHeadOfFamily)
            }

            residentialArea.id -> triggerDependants(
                source = residentialArea,
                passedIndex = index,
                triggerIndex = residentialArea.entries!!.size - 2,
                target = otherResidentialArea
            )

            fuelForCooking.id -> triggerDependants(
                source = fuelForCooking,
                passedIndex = index,
                triggerIndex = fuelForCooking.entries!!.size - 1,
                target = otherFuelForCooking
            )

            sourceOfWater.id -> triggerDependants(
                source = sourceOfWater,
                passedIndex = index,
                triggerIndex = sourceOfWater.entries!!.size - 1,
                target = otherSourceOfWater
            )

            sourceOfElectricity.id -> triggerDependants(
                source = sourceOfElectricity,
                passedIndex = index,
                triggerIndex = sourceOfElectricity.entries!!.size - 1,
                target = otherSourceOfElectricity
            )

            availOfToilet.id -> triggerDependants(
                source = availOfToilet,
                passedIndex = index,
                triggerIndex = availOfToilet.entries!!.size - 2,
                target = otherAvailOfToilet
            )

            otherResidentialArea.id -> {
                validateEmptyOnEditText(otherResidentialArea)
                //validateAllAlphabetsSpaceOnEditText(otherResidentialArea)
            }

            otherFuelForCooking.id -> {
                validateEmptyOnEditText(otherFuelForCooking)
                //validateAllAlphabetsSpaceOnEditText(otherFuelForCooking)
            }

            otherSourceOfWater.id -> {
                validateEmptyOnEditText(otherSourceOfWater)
                // validateAllAlphabetsSpaceOnEditText(otherSourceOfWater)
            }

            otherSourceOfElectricity.id -> {
                validateEmptyOnEditText(otherSourceOfElectricity)
                //  validateAllAlphabetsSpaceOnEditText(otherSourceOfElectricity)
            }

            otherAvailOfToilet.id -> {
                validateEmptyOnEditText(otherAvailOfToilet)
                // validateAllAlphabetsSpaceOnEditText(otherAvailOfToilet)
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        when (pageNumber) {
            1 -> {
                mapValuesForPage1(cacheModel)
            }

            2 -> {
                mapValuesForPage2(cacheModel)
            }

            3 -> {
                mapValuesForPage3(cacheModel)
            }
        }

    }

    private fun mapValuesForPage1(cacheModel: FormDataModel) {
        val family = HouseholdFamily()
        family.let { family ->
            family.familyHeadName = firstNameHeadOfFamily.value
            family.familyName = lastNameHeadOfFamily.value
            family.familyHeadPhoneNo = mobileNoHeadOfFamily.value?.toLong()
            family.houseNo = houseNo.value
            family.wardNo = wardNo.value
            family.wardName = wardName.value
            family.mohallaName = mohallaName.value
            family.povertyLineId = povertyLine.getPosition()
            family.povertyLine =
                povertyLine.getEnglishStringFromPosition(family.povertyLineId)
        }
        (cacheModel as HouseholdCache).family = family
    }

    private fun mapValuesForPage2(cacheModel: FormDataModel) {
        val details = HouseholdDetails()
        details.let { details ->
            details.residentialAreaId = residentialArea.getPosition()
            details.residentialArea =
                residentialArea.getEnglishStringFromPosition(details.residentialAreaId)
            details.otherResidentialArea = otherResidentialArea.value
            details.houseTypeId = typeOfHouse.getPosition()
            details.houseType = typeOfHouse.getEnglishStringFromPosition(details.houseTypeId)
            details.isHouseOwnedId = houseOwnership.getPosition()
            details.isHouseOwned =
                houseOwnership.getEnglishStringFromPosition(details.isHouseOwnedId)
        }
        (cacheModel as HouseholdCache).details = details
    }

    private fun mapValuesForPage3(cacheModel: FormDataModel) {
        val amenity = HouseholdAmenities()
        amenity.let { amenities ->
            amenities.separateKitchenId = separateKitchen.getPosition()
            amenities.separateKitchen =
                separateKitchen.getEnglishStringFromPosition(amenities.separateKitchenId)


            amenities.fuelUsedId = fuelForCooking.getPosition()
            amenities.fuelUsed = fuelForCooking.getEnglishStringFromPosition(amenities.fuelUsedId)
            amenities.otherFuelUsed = otherFuelForCooking.value

            amenities.sourceOfDrinkingWaterId = sourceOfWater.getPosition()
            amenities.sourceOfDrinkingWater =
                sourceOfWater.getEnglishStringFromPosition(amenities.sourceOfDrinkingWaterId)
            amenities.otherSourceOfDrinkingWater = otherSourceOfWater.value

            amenities.availabilityOfElectricityId = sourceOfElectricity.getPosition()
            amenities.availabilityOfElectricity =
                sourceOfElectricity.getEnglishStringFromPosition(amenities.availabilityOfElectricityId)
            amenities.otherAvailabilityOfElectricity = otherSourceOfElectricity.value

            amenities.availabilityOfToiletId = availOfToilet.getPosition()
            amenities.availabilityOfToilet =
                availOfToilet.getEnglishStringFromPosition(amenities.availabilityOfToiletId)
            amenities.otherAvailabilityOfToilet = otherAvailOfToilet.value
        }
        (cacheModel as HouseholdCache).apply {
            this.amenities = amenity
        }
    }

    fun freezeHouseholdId(household: HouseholdCache, userId: Int) {
        household.householdId = getHHidFromUserId(userId)
    }


}