package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.text.isDigitsOnly
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.BenPncDomain
import org.piramalswasthya.sakhi.model.BenWithAdolescentDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.BenWithEcrDomain
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.model.BenWithHRNPADomain
import org.piramalswasthya.sakhi.model.BenWithHRNPTListDomain
import org.piramalswasthya.sakhi.model.BenWithHRPADomain
import org.piramalswasthya.sakhi.model.BenWithHRPTListDomain
import org.piramalswasthya.sakhi.model.BenWithMalariaConfirmedDomain
import org.piramalswasthya.sakhi.model.BenWithPwrDomain
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain
import org.piramalswasthya.sakhi.model.BenWithTbSuspectedDomain
import org.piramalswasthya.sakhi.model.GeneralOPEDBeneficiary
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.InfantRegDomain
import org.piramalswasthya.sakhi.model.PregnantWomenVisitDomain
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


fun filterBenList(list: List<BenBasicDomain>, text: String): List<BenBasicDomain> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            filterForBen(it, filterText)
        }
    }
}


fun filterOPDBenList(list: List<GeneralOPEDBeneficiary>, text: String): List<GeneralOPEDBeneficiary> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            filterOPD(it, filterText)
        }
    }
}

fun filterBenList(
    list: List<BenBasicDomain>,
    rchPresent: Boolean
) =
    if (rchPresent) {
        list.filter {
            it.rchId.takeIf { it1 -> it1.toString().isDigitsOnly() }?.contains("") ?: false
        }
    } else {
        list
    }

    fun filterBenList(
        list: List<BenBasicDomain>,
        filterType: Int
    ): List<BenBasicDomain> {
        return when (filterType) {
            1 -> list.filter { !it.abhaId.isNullOrEmpty() }

            2 -> list.filter { it.abhaId.isNullOrEmpty() }

            3 -> list.filter { ben ->
                val age = getAgeFromDob(ben.dob)
                age >= 30 && ben.isDeathValue == "false"
            }
            4 -> list.filter(::isWARA)
            else -> list
        }
    }

private fun isWARA(ben: BenBasicDomain): Boolean {
    val age = getAgeFromDob(ben.dob)
    val alive = ben.isDeathValue.equals("false", ignoreCase = true)
    val genderOk = ben.gender?.equals("female", ignoreCase = true) == true
    val reproOk = (ben.reproductiveStatusId == 1 || ben.reproductiveStatusId == 2)
    return genderOk && alive && age in 20..49 && reproOk
}

fun getAgeFromDob(dob: Long?): Int {
    if (dob == null) return 0
    val currentTimeMillis = System.currentTimeMillis()
    val diffMillis = currentTimeMillis - dob
    return (diffMillis / (1000L * 60 * 60 * 24 * 365)).toInt()
}

fun filterAdolescentList(list: List<BenWithAdolescentDomain>) = list

fun filterAdolescentList(list: List<BenWithAdolescentDomain>, text: String): List<BenWithAdolescentDomain> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            filterAdolesent(it, filterText)
        }
    }
}


fun filterForBen(
    ben: BenBasicDomain,
    filterText: String
) = ben.hhId.toString().lowercase().contains(filterText) ||
        ben.benId.toString().lowercase().contains(filterText.replace(" ","")) ||
        ben.abhaId.toString().replace("-","").lowercase().contains(filterText.replace(" ","")) ||
        ben.regDate.lowercase().contains((filterText)) ||
        ben.age.lowercase() == filterText.lowercase() ||
        ben.benFullName.lowercase().contains(filterText) ||
        ben.familyHeadName.lowercase().contains(filterText) ||
        ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
        ben.rchId.takeIf { it?.isDigitsOnly() == true }?.contains(filterText.replace(" ","")) ?: false ||
        ben.mobileNo.lowercase().contains(filterText.replace(" ","")) ||
        ben.gender.lowercase() == filterText.lowercase() ||
        ben.spouseName?.lowercase()?.contains(filterText) == true ||
        ben.fatherName?.lowercase()?.contains(filterText) ?: false


fun filterAdolesent(
    ben: BenWithAdolescentDomain,
    filterText: String
) = ben.ben.hhId.toString().lowercase().contains(filterText) ||
        ben.ben.benId.toString().lowercase().contains(filterText.replace(" ","")) ||
        ben.ben.abhaId.toString().lowercase().contains(filterText) ||
        ben.ben.regDate.lowercase().contains((filterText)) ||
        ben.ben.age.lowercase() == filterText.lowercase() ||
        ben.ben.benFullName.lowercase().contains(filterText) ||
        ben.ben.familyHeadName.lowercase().contains(filterText) ||
        ben.ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
        ben.ben.rchId.takeIf { it?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
        ben.ben.mobileNo.lowercase().contains(filterText) ||
        ben.ben.gender.lowercase() == filterText.lowercase() ||
        ben.ben.spouseName?.lowercase()?.contains(filterText) == true ||
        ben.ben.fatherName?.lowercase()?.contains(filterText) ?: false



fun filterBenFormList(
    list: List<PregnantWomenVisitDomain>,
    filterText: String
) =
    list.filter { ben ->
        ben.benId.toString().lowercase().contains(filterText) ||
                ben.familyHeadName.lowercase().contains(filterText) ||
                ben.age.lowercase().contains(filterText) ||
                ben.name.lowercase().contains(filterText) ||
                ben.spouseName.lowercase().contains(filterText) ||
                ben.weeksOfPregnancy.toString().lowercase().contains(filterText)
    }


fun filterOPD(
    ben: GeneralOPEDBeneficiary,
    filterText: String
) = ben.benName.toString().lowercase().contains(filterText)

fun filterEcTrackingList(
    list: List<BenWithEctListDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.numChildren.contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false
    }

fun filterEcRegistrationList(
    list: List<BenWithEcrDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false


//                ||
//                it.numChildren.contains(filterText)
    }

fun filterPwrRegistrationList(
    list: List<BenWithPwrDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false
    }

fun filterPwrRegistrationList(
    list: List<BenWithPwrDomain>,
    rchPresent: Boolean
) =
    if (rchPresent) {
        list.filter {
//            it.ben.rchId.isNotEmpty()
            it.ben.rchId.takeIf { it1 -> it1.toString().isDigitsOnly() }?.contains("") ?: false
        }
    } else {
        list
    }


fun filterPwAncList(
    list: List<BenWithAncListDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.lmpString?.contains(filterText) ?: false ||
                it.eddString?.contains(filterText) ?: false ||
                it.weeksOfPregnancy?.contains(filterText) ?: false ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false

    }

fun filterAbortionList(
    list: List<BenWithAncListDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
//                it.abortionDate?.toString()?.contains(filterText) ?: false ||
                it.ben.rchId.takeIf { id -> id?.isDigitsOnly() == true }?.contains(filterText) ?: false
    }


fun filterPncDomainList(
    list: List<BenPncDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.deliveryDate.contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false
    }

fun filterInfantDomainList(
    list: List<InfantRegDomain>,
    filterText: String
) =
    list.filter {
        it.motherBen.benId.toString().lowercase().contains(filterText) ||
                it.motherBen.age.lowercase().contains(filterText) ||
                it.motherBen.familyHeadName.lowercase().contains(filterText) ||
                it.motherBen.benFullName.lowercase().contains(filterText) ||
                it.motherBen.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.motherBen.benId.toString().lowercase().contains(filterText) ||
                it.motherBen.mobileNo.lowercase().contains(filterText) ||
                it.babyName.contains(filterText) ||
                it.motherBen.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }
                    ?.contains(filterText) ?: false
    }


fun filterTbScreeningList(
    list: List<BenWithTbScreeningDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.fatherName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false
    }

fun filterTbSuspectedList(
    list: List<BenWithTbSuspectedDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.fatherName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false
    }


fun filterMalariaConfirmedList(
    list: List<BenWithMalariaConfirmedDomain>,
    filterText: String
) =
    list.filter {
        it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.familyHeadName.lowercase().contains(filterText) ||
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.ben.spouseName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.fatherName?.lowercase()?.contains(filterText) ?: false ||
                it.ben.benId.toString().lowercase().contains(filterText) ||
                it.ben.mobileNo.lowercase().contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1.toString().isDigitsOnly() }?.contains(filterText) ?: false
    }

@JvmName("filterBenList1")
fun filterBenFormList(
    list: List<BenBasicDomainForForm>,
    text: String
): List<BenBasicDomainForForm> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            it.hhId.toString().lowercase().contains(filterText) ||
                    it.benId.toString().lowercase().contains(filterText) ||
                    it.regDate.lowercase().contains((filterText)) ||
                    it.age.lowercase().contains(filterText) ||
                    it.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
                    it.benName.lowercase().contains(filterText) ||
                    it.familyHeadName.lowercase().contains(filterText) ||
                    it.spouseName?.lowercase()?.contains(filterText) == true ||
                    it.benSurname?.lowercase()?.contains(filterText) ?: false ||
//                    it.typeOfList.lowercase().contains(filterText) ||
                    it.mobileNo.lowercase().contains(filterText) ||
                    it.gender.lowercase().contains(filterText) ||
                    it.fatherName?.lowercase()?.contains(filterText) ?: false
        }
    }
}


fun filterBenHRPFormList(
    list: List<BenWithHRPADomain>,
    text: String
): List<BenWithHRPADomain> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            it.ben.hhId.toString().lowercase().contains(filterText) ||
                    it.ben.benId.toString().lowercase().contains(filterText) ||
                    it.ben.regDate.lowercase().contains((filterText)) ||
                    it.ben.age.lowercase().contains(filterText) ||
                    it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }
                        ?.contains(filterText) ?: false ||
                    it.ben.benName.lowercase().contains(filterText) ||
                    it.ben.familyHeadName.lowercase().contains(filterText) ||
                    it.ben.spouseName?.lowercase()?.contains(filterText) == true ||
                    it.ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
//                    it.typeOfList.lowercase().contains(filterText) ||
                    it.ben.mobileNo.lowercase().contains(filterText) ||
                    it.ben.gender.lowercase().contains(filterText) ||
                    it.ben.fatherName?.lowercase()?.contains(filterText) ?: false
        }
    }
}

fun filterBenHRNPFormList(
    list: List<BenWithHRNPADomain>,
    text: String
): List<BenWithHRNPADomain> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            it.ben.hhId.toString().lowercase().contains(filterText) ||
                    it.ben.benId.toString().lowercase().contains(filterText) ||
                    it.ben.regDate.lowercase().contains((filterText)) ||
                    it.ben.age.lowercase().contains(filterText) ||
                    it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }
                        ?.contains(filterText) ?: false ||
                    it.ben.benName.lowercase().contains(filterText) ||
                    it.ben.familyHeadName.lowercase().contains(filterText) ||
                    it.ben.spouseName?.lowercase()?.contains(filterText) == true ||
                    it.ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
//                    it.typeOfList.lowercase().contains(filterText) ||
                    it.ben.mobileNo.lowercase().contains(filterText) ||
                    it.ben.gender.lowercase().contains(filterText) ||
                    it.ben.fatherName?.lowercase()?.contains(filterText) ?: false
        }
    }
}

fun filterBenHRPTFormList(
    list: List<BenWithHRPTListDomain>,
    text: String
): List<BenWithHRPTListDomain> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            it.ben.hhId.toString().lowercase().contains(filterText) ||
                    it.ben.benId.toString().lowercase().contains(filterText) ||
                    it.ben.regDate.lowercase().contains((filterText)) ||
                    it.ben.age.lowercase().contains(filterText) ||
                    it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }
                        ?.contains(filterText) ?: false ||
                    it.ben.benName.lowercase().contains(filterText) ||
                    it.ben.familyHeadName.lowercase().contains(filterText) ||
                    it.ben.spouseName?.lowercase()?.contains(filterText) == true ||
                    it.ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
//                    it.typeOfList.lowercase().contains(filterText) ||
                    it.ben.mobileNo.lowercase().contains(filterText) ||
                    it.ben.gender.lowercase().contains(filterText) ||
                    it.ben.fatherName?.lowercase()?.contains(filterText) ?: false
        }
    }
}


fun filterImmunList(
    list: List<ImmunizationDetailsDomain>,
    text: String
): List<ImmunizationDetailsDomain> {
    val raw = text.trim()
    if (raw.isEmpty()) return list

    var filterText = raw.lowercase()
    var alt1 = ""
    var alt2 = ""
    var alt3 = ""

    when {
        filterText.contains("5-6") || filterText.contains("5-6 years") -> {
            alt1 = "5 years"
            filterText = "6 years"
        }

        filterText.contains("16-24") || filterText.contains("16-24 months") -> {
            alt1 = "1 year"
            filterText = "2 years"
        }

        filterText.contains("9-12") || filterText.contains("9-12 months") -> {
            alt1 = "9 months"
            alt2 = "10 months"
            alt3 = "11 months"
            filterText = "12 months"
        }

        filterText.contains("6 weeks") -> {
            alt1 = "1 month"
            filterText = "2 months"
        }

        filterText.contains("birth dose") -> {
            alt1 = "1 day"
            filterText = "1 month"

            // special case: also match beneficiaries whose age contains "day"
            return list.filter { imm ->
                val age = imm.ben.age.lowercase()
                age.contains("day") || filterForImm(imm, filterText, alt1, alt2, alt3)
            }
        }

        filterText.contains("10 weeks") -> filterText = "3 months"
        filterText.contains("14 weeks") -> filterText = "4 months"
    }

    return list.filter { filterForImm(it, filterText, alt1, alt2, alt3) }
}

fun filterForImm(
    imm: ImmunizationDetailsDomain,
    filterText: String,
    firstVal: String = "",
    secondVal: String = "",
    thirdVal: String = ""
): Boolean {
    val token = filterText.trim().lowercase()

    val age = imm.ben.age?.lowercase() ?: ""
    val name = imm.ben.benFullName?.lowercase() ?: ""
    val mother = imm.ben.motherName?.lowercase() ?: ""
    val mobile = imm.ben.mobileNo ?: ""

    if (age.contains(token)) return true
    if (firstVal.isNotEmpty() && age.contains(firstVal)) return true
    if (secondVal.isNotEmpty() && age.contains(secondVal)) return true
    if (thirdVal.isNotEmpty() && age.contains(thirdVal)) return true

    if (name.contains(token)) return true
    if (mother.contains(token)) return true

    if (mobile.contains(token)) return true

    return false
}

fun filterBenHRNPTFormList(
    list: List<BenWithHRNPTListDomain>,
    text: String
): List<BenWithHRNPTListDomain> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            it.ben.hhId.toString().lowercase().contains(filterText) ||
                    it.ben.benId.toString().lowercase().contains(filterText) ||
                    it.ben.regDate.lowercase().contains((filterText)) ||
                    it.ben.age.lowercase().contains(filterText) ||
                    it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }
                        ?.contains(filterText) ?: false ||
                    it.ben.benName.lowercase().contains(filterText) ||
                    it.ben.familyHeadName.lowercase().contains(filterText) ||
                    it.ben.spouseName?.lowercase()?.contains(filterText) == true ||
                    it.ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
//                    it.typeOfList.lowercase().contains(filterText) ||
                    it.ben.mobileNo.lowercase().contains(filterText) ||
                    it.ben.gender.lowercase().contains(filterText) ||
                    it.ben.fatherName?.lowercase()?.contains(filterText) ?: false
        }
    }
}

fun getWeeksOfPregnancy(regLong: Long, lmpLong: Long) =
    (TimeUnit.MILLISECONDS.toDays(regLong - lmpLong) / 7).toInt()

fun getTodayMillis() = Calendar.getInstance().setToStartOfTheDay().timeInMillis

fun Calendar.setToStartOfTheDay() = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.setToEndOfTheDay() = apply {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 0)
}

fun getDateFromLong(time: Long) : Date {
    val pattern: String = "dd/MM/yyyy HH:mm:ss"
    val date = Date(time)
//    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return date
}
fun getPatientTypeByAge(dateOfBirth: Date): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        val period = Period.between(birthDate, currentDate)

        val years = period.years
        val months = period.months
        val days = period.days

        when {
            years == 0 && months == 0 && days <= 30 -> "new_born_baby"
            years == 0 && (months > 0 || days > 30) -> "infant"
            years in 1..12 -> "child"
            years in 13..18 -> "adolescence"
            else -> "adult"
        }
    } else {

        val current = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { time = dateOfBirth }

        var years = current.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = current.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        var days = current.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)


        if (days < 0) {
            months -= 1
            days += current.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }

        when {
            years == 0 && months <= 1 -> "new_born_baby"
            years == 0 && months <= 12 -> "infant"
            years in 1..12 -> "child"
            years in 13..18 -> "adolescence"
            else -> "adult"
        }
    }
}



sealed class NetworkResponse<T>(val data: T? = null, val message: String? = null) {

    class Idle<T> : NetworkResponse<T>(null, null)
    class Loading<T> : NetworkResponse<T>(null, null)
    class Success<T>(data: T) : NetworkResponse<T>(data = data)
    class Error<T>(message: String) : NetworkResponse<T>(data = null, message = message)

}

fun getDateString(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    dateLong?.let {
        return dateFormat.format(Date(dateLong))
    } ?: run {
        return null
    }
}


@Suppress("deprecation")
fun isInternetAvailable(activity: Context): Boolean {
    val conMgr = activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = conMgr.activeNetwork
        val networkCapabilities = conMgr.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    } else {
        // below API Level 23
        return (conMgr.activeNetworkInfo != null && conMgr.activeNetworkInfo!!.isAvailable
                && conMgr.activeNetworkInfo!!.isConnected)
    }
}


