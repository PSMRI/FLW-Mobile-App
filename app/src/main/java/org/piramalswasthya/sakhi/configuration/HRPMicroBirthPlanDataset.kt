package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.*

class HRPMicroBirthPlanDataset  (context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {
    private val name = FormElement(
        id = 1,
        inputType = InputType.EDIT_TEXT,
        title = "Name of the PW",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val nearestSc = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = "Nearest SC/HWC",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bloodGroup = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = "Blood Group",
        arrayId = R.array.maternal_health_blood_group,
        entries = resources.getStringArray(R.array.maternal_health_blood_group),
        required = true
    )

    private val contactNumber1 = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = "Contact Number 1",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )

    private val contactNumber2 = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = "Contact Number 2",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )
    private val scHosp = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = "SC/HWC/TG Hosp",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val block = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = "Block",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bankac = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = "Bank Acc No",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12,
        max = 999999999999,
        min = 100000000000
    )

    private val nearestPhc = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = "Nearest 24x7 PHC",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val nearestFru = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = "Nearest FRU",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bloodDonors1 = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "Blood donors identified 1",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bloodDonors2 = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = "Blood donors identified 2",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val birthCompanion = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = "Birth Companion",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val careTaker = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = "Person who will take care of children, if any when the PW is admitted for delivery",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val communityMember = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = "Name of VHSND/Community member for support during emergency",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val communityMemberContact = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = "Contact Number of VHSND/Community member for support during emergency",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )
    private val modeOfTransportation = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = "Mode of transportation in case of labour pain",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val usg = FormElement(
        id = 18,
        inputType = InputType.EDIT_TEXT,
        title = "Nearest USG centre:",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: HRPMicroBirthPlanCache?) {
        val list = mutableListOf(
                nearestSc,
                bloodGroup,
                contactNumber1,
                contactNumber2,
                scHosp,
                usg,
                block,
                bankac,
                nearestPhc,
                nearestFru,
                bloodDonors1,
                bloodDonors2,
                birthCompanion,
                careTaker,
                communityMember,
                communityMemberContact,
                modeOfTransportation
        )

        saved?.let {
            nearestSc.value = it.nearestSc
            bloodGroup.value = it.bloodGroup
            contactNumber1.value = it.contactNumber1
            contactNumber2.value = it.contactNumber2
            scHosp.value = it.scHosp
            block.value = it.block
            bankac.value = it.bankac
            nearestPhc.value = it.nearestPhc
            nearestFru.value = it.nearestFru
            bloodDonors1.value = it.bloodDonors1
            bloodDonors2.value = it.bloodDonors2
            birthCompanion.value = it.birthCompanion
            careTaker.value = it.careTaker
            communityMember.value = it.communityMember
            communityMemberContact.value = it.communityMemberContact
            modeOfTransportation.value = it.modeOfTransportation
            usg.value = it.usg
        }
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPMicroBirthPlanCache).let { form ->
            form.nearestSc = nearestSc.value
            form.bloodGroup = bloodGroup.value
            form.contactNumber1 = contactNumber1.value
            form.contactNumber2 = contactNumber2.value
            form.scHosp = scHosp.value
            form.block = block.value
            form.bankac = bankac.value
            form.nearestPhc = nearestPhc.value
            form.nearestFru = nearestFru.value
            form.bloodDonors1 = bloodDonors1.value
            form.bloodDonors2 = bloodDonors2.value
            form.birthCompanion = birthCompanion.value
            form.careTaker = careTaker.value
            form.communityMember = communityMember.value
            form.communityMemberContact = communityMemberContact.value
            form.modeOfTransportation = modeOfTransportation.value
            form.usg = usg.value
        }
    }

}