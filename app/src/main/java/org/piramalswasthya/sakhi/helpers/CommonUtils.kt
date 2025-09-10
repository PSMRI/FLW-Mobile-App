package org.piramalswasthya.sakhi.helpers

import android.app.Activity
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
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
) =
    if (filterType == 1) {
        list.filter {
            !it.abhaId.isNullOrEmpty()
        }
    } else if (filterType == 2) {
        list.filter {
            it.abhaId.isNullOrEmpty()
        }
    } else {
        list
    }

fun filterAdolesenctList(list: List<BenWithAdolescentDomain>, text: String): List<BenWithAdolescentDomain> {
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
        ben.abhaId.toString().lowercase().contains(filterText) ||
        ben.regDate.lowercase().contains((filterText)) ||
        ben.age.lowercase() == filterText.lowercase() ||
        ben.benFullName.lowercase().contains(filterText) ||
        ben.familyHeadName.lowercase().contains(filterText) ||
        ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
        ben.rchId.takeIf { it?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
        ben.mobileNo.lowercase().contains(filterText) ||
        ben.gender.lowercase() == filterText.lowercase() ||
        ben.spouseName?.lowercase()?.contains(filterText) == true ||
        ben.fatherName?.lowercase()?.contains(filterText) ?: false


fun filterAdolesent(
    ben: BenWithAdolescentDomain,
    filterText: String
) = ben.ben.benFullName.toString().lowercase().contains(filterText)


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


fun filterImmunList(list: List<ImmunizationDetailsDomain>, text: String): List<ImmunizationDetailsDomain> {
    if (text == "")
        return list
    else {
        var filterText = text.lowercase()
        var secondFilterText = ""
        var thirdFilterText = ""
        var fourthFilterText = ""

        if (filterText.contains("5-6 years")) {
            secondFilterText = "${filterText.split("-")[0]} years"
            filterText = filterText.split("-")[1]
        } else if (filterText.contains("16-24 months")) {
            secondFilterText = "1 years"
            filterText = "2 years"
        } else if (filterText.contains("9-12 months")) {
            secondFilterText = "${filterText.split("-")[0]} months"
            thirdFilterText = "10 months"
            fourthFilterText = "11 months"
            filterText = filterText.split("-")[1]
        } else if (filterText.contains("6 weeks")) {
            secondFilterText = "1 months"
            filterText = "2 months"
        } else if (filterText.contains("10 weeks")) {
            filterText = "3 months"
        } else if (filterText.contains("14 weeks")) {
            filterText = "4 months"
        }else{

            val filterText = text.lowercase()
            return list.filter {
                filterForImm(
                    it,
                    filterText,
                    secondFilterText,
                    thirdFilterText,
                    fourthFilterText
                )
            }

        }
        return list.filter {
            filterForImm(
                it,
                filterText,
                secondFilterText,
                thirdFilterText,
                fourthFilterText
            )
        }
    }
}

fun filterForImm(
    imm: ImmunizationDetailsDomain,
    filterText: String,
    firstVal: String,
    secondVal: String,
    thirdVal: String
) = imm.ben.age.lowercase() == filterText ||
        imm.ben.age.lowercase() == firstVal ||
        imm.ben.age.lowercase() == secondVal ||
        imm.ben.age.lowercase() == thirdVal ||
        imm.ben.benFullName.lowercase() ==filterText ||
        imm.ben.mobileNo.lowercase() ==filterText ||
        imm.ben.motherName?.lowercase() ==filterText

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

//private fun getAncStatus(
//    list: List<AncStatus>, lmpDate: Long, visitNumber: Int, benId: Long, at: Long
//): AncStatus {
//
//    val currentAnc = list.firstOrNull { it.visitNumber == visitNumber }?.let { return it }
//    val lastAnc =
//        if (visitNumber > 1) list.firstOrNull { it.visitNumber == visitNumber - 1 } else null
//    val lastAncFilledWeek = lastAnc?.filledWeek ?: 0
//    val weeks = getWeeksOfPregnancy(at, lmpDate)
//    val weekRange = when (visitNumber) {
//        1 -> Konstants.minAnc1Week//..Konstants.maxAnc1Week
//        2 -> getMinAncFillDate(Konstants.minAnc2Week, lastAncFilledWeek) //..Konstants.maxAnc2Week
//        3 -> getMinAncFillDate(Konstants.minAnc3Week, lastAncFilledWeek)  //..Konstants.maxAnc2Week//..Konstants.maxAnc3Week
//        4 -> getMinAncFillDate(Konstants.minAnc4Week, lastAncFilledWeek)  //..Konstants.maxAnc2Week//..Konstants.maxAnc4Week
//        else -> throw IllegalStateException("visit number not in [1,4]")
//    }
//    return if (weeks >= weekRange) AncStatus(
//        benId,
//        visitNumber,
////        if (visitNumber == 1) AncFormState.ALLOW_FILL else {
////            if (lastAnc == null) AncFormState.NO_FILL else AncFormState.ALLOW_FILL
////        },
//        0
//    )
//    else AncStatus(
//        benId,
//        visitNumber,
////        AncFormState.NO_FILL,
//        0
//    )
//}

fun getMinAncFillDate(minWeek: Int, lastAncFilledWeek: Int) =
    if (minWeek - lastAncFilledWeek <= 4) lastAncFilledWeek + 4 else minWeek

//fun getAncStatusList(
//    list: List<AncStatus>, lmpDate: Long, benId: Long, at: Long
//) =
//    listOf(1, 2, 3, 4).map {
//        getAncStatus(list, lmpDate, it, benId, at)
//    }

fun hasPendingAncVisit(
    list: List<AncStatus>, lmpDate: Long, benId: Long, at: Long
): Boolean {
//    val l = getAncStatusList(list, lmpDate, benId, at).map { it.formState }
//    Timber.tag("MaternalHealthRepo").d("Emitted : at CommonUtls : $l")
//    return l.contains(AncFormState.ALLOW_FILL)
    return true
}

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
    val birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val currentDate = LocalDate.now()

    val period = Period.between(birthDate, currentDate)
    val years = period.years
    val months = period.months % 12
    val days = period.days % 30

    var type= ""
    if((days <= 30 || months<=1) && years<1 ){
        type = "new_born_baby"
    }
    if((years<1 && months<=12) || (months==0 && days==0 && years==1)){
        type = "infant"
    }

    if(years>=1&& years<=12){
        type ="child"
    }

    if(years>=12 && years<=18){
        type = "adolescence"
    }

    if(years>=18){
        type = "adult"
    }

    return type
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


