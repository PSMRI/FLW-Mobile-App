package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InputType
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FilariaFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.home_visit_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )
    private val isSuffering = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.suffering_from_filaries),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true

    )

    private var whichPartOfBodyMale = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.body_part),
        arrayId = R.array.body_part_male,
        entries = resources.getStringArray(R.array.body_part_male),
        required = false,
        hasDependants = true
    )

    private var whichPartOfBodyFemale = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.body_part),
        arrayId = R.array.body_part_female,
        entries = resources.getStringArray(R.array.body_part_female),
        required = false,
        hasDependants = true
    )
    private var decAndAlbDoseStatus = FormElement(
        id = 5,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.dose_status),
        arrayId = R.array.dec_albendazole_array,
        entries = resources.getStringArray(R.array.dec_albendazole_array),
        required = false,
        hasDependants = true
    )
    private var other = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )

    private var medicineSideEffect = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.medicine_side_effect),
        arrayId = R.array.side_effect_of_medicine,
        entries = resources.getStringArray(R.array.side_effect_of_medicine),
        required = false,
        hasDependants = true
    )
    private var sideEffectOther = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )

    var isMale = false;
    var benAge = 0L;
    fun isMaleFemale(ben: BenRegCache?): Boolean {
         if (ben?.gender == Gender.MALE) {
             isMale = true
             return true
         } else if (ben?.gender == Gender.FEMALE) {
             isMale = false
             return false
         } else {
             return false
         }
    }
    suspend fun setUpPage(ben: BenRegCache?, saved: FilariaScreeningCache?) {
        val list = mutableListOf(
            dateOfCase,
            isSuffering,

        )
        isMaleFemale(ben)
        benAge = ben!!.dob
        decAndAllDoseFieldValidation(benAge)


        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
        } else {
            dateOfCase.value = getDateFromLong(saved.mdaHomeVisitDate)
            isSuffering.value =  getLocalValueInArray(R.array.yes_no, if (saved.sufferingFromFilariasis == true) "Yes" else "No")
            if (isSuffering.value == "Yes"){
                list.add(list.indexOf(isSuffering) + 1, if (isMale) whichPartOfBodyMale else whichPartOfBodyFemale)
                list.add(list.indexOf(isSuffering) + 2, decAndAlbDoseStatus)
                list.add(list.indexOf(isSuffering) + 3, medicineSideEffect)

                if (isMale) whichPartOfBodyMale.value = getLocalValueInArray(whichPartOfBodyMale.arrayId,saved.affectedBodyPart)
                else whichPartOfBodyFemale.value = getLocalValueInArray(whichPartOfBodyFemale.arrayId,saved.affectedBodyPart)


                decAndAlbDoseStatus.value =
                    getLocalValueInArray(decAndAlbDoseStatus.arrayId, saved.doseStatus)
                if (decAndAlbDoseStatus.value == decAndAlbDoseStatus.entries!!.last()) {
                    list.add(list.indexOf(decAndAlbDoseStatus) + 1, other)
                    other.value = saved.otherDoseStatusDetails
                }
                medicineSideEffect.value =
                    getLocalValueInArray(medicineSideEffect.arrayId, saved.medicineSideEffect)
                if (medicineSideEffect.value == medicineSideEffect.entries!![medicineSideEffect.entries!!.size - 2]) {
                    list.add(list.indexOf(medicineSideEffect) + 1, sideEffectOther)
                    sideEffectOther.value = saved.otherSideEffectDetails
                }
            }

        }


        setUpPage(list)

    }

    private fun decAndAllDoseFieldValidation(age: Long) {
            if (isYoung(age)) {
                decAndAlbDoseStatus.isEnabled = false
                decAndAlbDoseStatus.value = getLocalValueInArray(decAndAlbDoseStatus.arrayId,"Y")
            } else {
                decAndAlbDoseStatus.isEnabled = true

            }

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            isSuffering.id -> {
                isSuffering.isEnabled = true
                if (isSuffering.value == resources.getStringArray(R.array.yes_no)[0]) {
                    if (isMale ) {
                        triggerDependants(
                            source = isSuffering,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = listOf(whichPartOfBodyMale,decAndAlbDoseStatus,medicineSideEffect),

                            )
                    } else {
                        triggerDependants(
                            source = isSuffering,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = listOf(whichPartOfBodyFemale,decAndAlbDoseStatus,medicineSideEffect),

                            )
                        whichPartOfBodyFemale.isEnabled = true
                        decAndAlbDoseStatus.isEnabled = true
                        decAndAllDoseFieldValidation(benAge)
                    }

                } else {
                    if (isMale ) {
                        triggerforHide(
                            source = isSuffering,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = whichPartOfBodyMale,
                            targetSideEffect = listOf(
                                whichPartOfBodyMale,
                                decAndAlbDoseStatus,
                                medicineSideEffect,
                                other,
                                sideEffectOther
                            )
                        )
                    } else {
                        triggerforHide(
                            source = isSuffering,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = whichPartOfBodyFemale,
                            targetSideEffect = listOf(
                                whichPartOfBodyFemale,
                                decAndAlbDoseStatus,
                                medicineSideEffect,
                                other,
                                sideEffectOther
                            )
                        )
                    }
                }

                return 0


            }

            medicineSideEffect.id -> {
                if (medicineSideEffect.value == medicineSideEffect.entries!![medicineSideEffect.entries!!.size - 2]) {
                    triggerDependants(
                        source = medicineSideEffect,
                        addItems = listOf(sideEffectOther),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = medicineSideEffect,
                        addItems = listOf(),
                        removeItems = listOf(sideEffectOther)
                    )
                }
                0
            }


            decAndAlbDoseStatus.id -> {
                if (decAndAlbDoseStatus.value == decAndAlbDoseStatus.entries!!.last()) {
                    triggerDependants(
                        source = decAndAlbDoseStatus,
                        addItems = listOf(other),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = decAndAlbDoseStatus,
                        addItems = listOf(),
                        removeItems = listOf(other)
                    )
                }
                0
            }

            other.id -> {
                validateEmptyOnEditText(other)
            }

            sideEffectOther.id -> {
                validateEmptyOnEditText(sideEffectOther)
            }

            else -> -1
        }

    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as FilariaScreeningCache).let { form ->
            form.mdaHomeVisitDate = getLongFromDate(dateOfCase.value)
            form.createdDate = getLongFromDate(dateOfCase.value)
            form.sufferingFromFilariasis = isSuffering()
            form.diseaseTypeID = 4
            form.affectedBodyPart = if (isMale) whichPartOfBodyMale.value else whichPartOfBodyFemale.value
            form.otherSideEffectDetails = sideEffectOther.value
            form.otherDoseStatusDetails = other.value
            form.doseStatus = getEnglishValueInArray(R.array.dec_albendazole_array, decAndAlbDoseStatus.value)
            form.medicineSideEffect = getEnglishValueInArray(R.array.side_effect_of_medicine, medicineSideEffect.value)

        }
    }

    fun isSuffering(): Boolean {
        return isSuffering.value == "Yes"
    }

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }


    fun getIndexOfDate(): Int {
        return getIndexById(dateOfCase.id)
    }

    fun isYoung(age: Long): Boolean {
        val calDob = Calendar.getInstance().apply { timeInMillis = age }
        val calNow = Calendar.getInstance()

        val years = calNow.get(Calendar.YEAR) - calDob.get(Calendar.YEAR)
        val months = calNow.get(Calendar.MONTH) - calDob.get(Calendar.MONTH)
        val days = calNow.get(Calendar.DAY_OF_MONTH) - calDob.get(Calendar.DAY_OF_MONTH)

        var totalYears = years
        var totalMonths = months
        var totalDays = days

        if (totalMonths < 0 || (totalMonths == 0 && totalDays < 0)) {
            totalYears--
            totalMonths += 12
        }
        return (totalYears < 2)
    }


}