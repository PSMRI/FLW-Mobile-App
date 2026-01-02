package org.piramalswasthya.sakhi.configuration.dynamicDataSet
import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.Dataset
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.ReferalCache

class ReferalFormDataset(context: Context, language: Languages,var preferenceDao: PreferenceDao) : Dataset(context, language) {

    private val healthCenter = FormElement(
        id = 1,
        inputType = DROPDOWN,
        title = context.getString(R.string.higher_healthcare_center),
        arrayId = -1,
        entries = arrayOf("Apolo", "CHC", "District Hospital","PHC"),
        required = true,
        hasDependants = true
    )
    private val reasonForReferal = FormElement(
        id = 2,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = context.getString(R.string.referral_reason),
        arrayId = -1,
        required = false,

    )

    private val additionalService = FormElement(
        id = 3,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = context.getString(R.string.additional_services),
        value = "FLW",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        )

    private val referDate = FormElement(
        id = 4,
        inputType = DATE_PICKER,
        title = "Refer Date",
        arrayId = -1,
        required = true,
        isEnabled = false,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
    )
    var referralTypes = ""
    suspend fun setUpPage(referral : String , referralType : String) {
        val list = mutableListOf(
            healthCenter,
            reasonForReferal,
            referDate
            )
        referralTypes = referralType
        referDate.value = getDateFromLong(System.currentTimeMillis())
        reasonForReferal.value = referral
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(
        formId: Int,
        index: Int
    ): Int {
        return when (formId) {
            reasonForReferal.id -> {
                validateEmptyOnEditText(reasonForReferal)
            }
            healthCenter.id -> {
                validateEmptyOnEditText(healthCenter)
            }

            else -> {
                -1
            }
        }



    }

    override fun mapValues(
        cacheModel: FormDataModel,
        pageNumber: Int
    ) {
        (cacheModel as ReferalCache).let { form ->
            form.revisitDate = getLongFromDate(referDate.value)
            form.referralReason = reasonForReferal.value
            form.refrredToAdditionalServiceList = listOf("FLW")
            form.referredToInstituteID = healthCenter.getPosition()
            form.referredToInstituteName = healthCenter.value
            form.createdBy =  preferenceDao.getLoggedInUser()?.userName
            form.vanID =  preferenceDao.getLoggedInUser()?.vanId
            form.providerServiceMapID =  preferenceDao.getLoggedInUser()?.serviceMapId
            form.parkingPlaceID =  preferenceDao.getLoggedInUser()?.serviceMapId
            form.type = referralTypes




        }

    }

}