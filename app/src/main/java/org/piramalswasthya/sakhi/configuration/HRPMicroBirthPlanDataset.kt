package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW

class HRPMicroBirthPlanDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {
    private val name = FormElement(
        id = 1,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.name_of_the_pw),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val nearestSc = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nearest_sc_hwc),
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bloodGroup = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.blood_group),
        arrayId = R.array.maternal_health_blood_group,
        entries = resources.getStringArray(R.array.maternal_health_blood_group),
        required = true
    )

    private val contactNumber1 = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.contact_number_1),
        required = true,
        isEnabled = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )

    private val contactNumber2 = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.contact_number_2),
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )
    private val scHosp = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.sc_hwc_tg_hosp),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val block = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.block),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val nearestPhc = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nearest_24x7_phc),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val nearestFru = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nearest_fru),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bloodDonors1 = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.blood_donors_identified_1),
        required = false,
        etMaxLength = 50,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val bloodDonors2 = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.blood_donors_identified_2),
        required = false,
        etMaxLength = 50,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val birthCompanion = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.birth_companion),
        required = false,
        etMaxLength = 50,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val careTaker = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.person_who_will_take_care_of_children_if_any_when_the_pw_is_admitted_for_delivery),
        required = false,
        etMaxLength = 50,

        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val communityMember = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.name_of_vhsnd_community_member_for_support_during_emergency),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val communityMemberContact = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.contact_number_of_vhsnd_community_member_for_support_during_emergency),
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )
    private val modeOfTransportation = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.mode_of_transportation_in_case_of_labour_pain),
        required = false,
        etMaxLength = 20,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val usg = FormElement(
        id = 18,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nearest_usg_centre),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val mbp_label = FormElement(
        id = 19,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.micro_birth_plan_for_all_pregnant_women),
        required = false
    )

    private val husbandName = FormElement(
        id = 20,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.husband_s_name),
        required = true,
        isEnabled = false,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: HRPMicroBirthPlanCache?) {
        val list = mutableListOf(
            mbp_label,
            nearestSc,
            bloodGroup,
            contactNumber1,
            contactNumber2,
            scHosp,
            usg,
            block,
            husbandName,
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


        contactNumber1.value=ben?.contactNumber.toString()
        husbandName.value= ben?.genDetails?.spouseName.toString()
        saved?.let {
            nearestSc.value = it.nearestSc
            bloodGroup.value =
                getLocalValueInArray(R.array.maternal_health_blood_group, it.bloodGroup)
            contactNumber1.value = ben?.contactNumber?.toString()
            contactNumber2.value = it.contactNumber2
            scHosp.value = it.scHosp
            block.value = it.block
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
        return when (formId) {
            nearestSc.id -> {
                validateAllAlphaNumericSpaceOnEditText(nearestSc)
                -1
            }

            contactNumber1.id -> {
                validateMobileNumberOnEditText(contactNumber1)
                -1
            }

            contactNumber2.id -> {
                validateMobileNumberOnEditText(contactNumber2)
            }

            scHosp.id -> {
                validateAllAlphaNumericSpaceOnEditText(scHosp)
            }

            usg.id -> {
                validateAllAlphaNumericSpaceOnEditText(usg)
            }

            block.id -> {
                validateAllAlphaNumericSpaceOnEditText(block)
            }

            nearestPhc.id -> {
                validateAllAlphaNumericSpaceOnEditText(nearestPhc)
            }

            nearestFru.id -> {
                validateAllAlphaNumericSpaceOnEditText(nearestFru)
            }

            bloodDonors1.id -> {
                validateAllAlphabetsSpaceOnEditText(bloodDonors1)
            }

            bloodDonors2.id -> {
                validateAllAlphabetsSpaceOnEditText(bloodDonors2)
            }

            birthCompanion.id -> {
                validateAllAlphabetsSpaceOnEditText(birthCompanion)
            }

            careTaker.id -> {
                validateAllAlphabetsSpaceOnEditText(careTaker)
            }

            communityMember.id -> {
                validateAllAlphabetsSpaceOnEditText(communityMember)
            }

            communityMemberContact.id -> {
                validateMobileNumberOnEditText(communityMemberContact)
            }


            else -> {
                -1
            }
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPMicroBirthPlanCache).let { form ->
            form.nearestSc = nearestSc.value
            form.bloodGroup =
                getEnglishValueInArray(R.array.maternal_health_blood_group, bloodGroup.value)
            form.contactNumber1 = contactNumber1.value
            form.contactNumber2 = contactNumber2.value
            form.scHosp = scHosp.value
            form.block = block.value
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