package org.piramalswasthya.sakhi.helpers

import androidx.core.text.isDigitsOnly
import org.piramalswasthya.sakhi.model.AncFormState
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.model.PregnantWomenVisitDomain
import timber.log.Timber
import java.util.Calendar
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

fun filterForBen(
    ben: BenBasicDomain,
    filterText: String
) = ben.hhId.toString().lowercase().contains(filterText) ||
        ben.benId.toString().lowercase().contains(filterText) ||
        ben.abhaId.toString().lowercase().contains(filterText) ||
        ben.regDate.lowercase().contains((filterText)) ||
        ben.age.lowercase().contains(filterText) ||
        ben.benFullName.lowercase().contains(filterText) ||
        ben.familyHeadName.lowercase().contains(filterText) ||
        ben.benSurname?.lowercase()?.contains(filterText) ?: false ||
        ben.rchId.takeIf { it.isDigitsOnly() }?.contains(filterText) ?: false ||
        ben.mobileNo.lowercase().contains(filterText) ||
        ben.gender.lowercase().contains(filterText) ||
        ben.spouseName?.lowercase()?.contains(filterText) == true ||
        ben.fatherName?.lowercase()?.contains(filterText) ?: false


fun filterBenFormList(
    list: List<PregnantWomenVisitDomain>,
    filterText: String
) =
    list.filter { ben ->
        ben.benId.toString().lowercase().contains(filterText) ||
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
                it.ben.benFullName.lowercase().contains(filterText) ||
                it.numChildren.contains(filterText)
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
                    it.rchId.takeIf { it1 -> it1.isDigitsOnly() }?.contains(filterText) ?: false ||
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

fun getWeeksOfPregnancy(regLong: Long, lmpLong: Long) =
    (TimeUnit.MILLISECONDS.toDays(regLong - lmpLong) / 7).toInt()

private fun getAncStatus(
    list: List<AncStatus>, lmpDate: Long, visitNumber: Int, benId: Long, at: Long
): AncStatus {

    val currentAnc = list.firstOrNull { it.visitNumber == visitNumber }?.let { return it }
    val lastAnc =
        if (visitNumber > 1) list.firstOrNull { it.visitNumber == visitNumber - 1 } else null
    val lastAncFilledWeek = lastAnc?.filledWeek ?: 0
    val weeks = getWeeksOfPregnancy(at, lmpDate)
    val weekRange = when (visitNumber) {
        1 -> Konstants.minAnc1Week//..Konstants.maxAnc1Week
        2 -> getMinAncFillDate(Konstants.minAnc2Week, lastAncFilledWeek) //..Konstants.maxAnc2Week
        3 -> getMinAncFillDate(Konstants.minAnc3Week, lastAncFilledWeek)  //..Konstants.maxAnc2Week//..Konstants.maxAnc3Week
        4 -> getMinAncFillDate(Konstants.minAnc4Week, lastAncFilledWeek)  //..Konstants.maxAnc2Week//..Konstants.maxAnc4Week
        else -> throw IllegalStateException("visit number not in [1,4]")
    }
    return if (weeks >= weekRange) AncStatus(
        benId,
        visitNumber,
        if (visitNumber == 1) AncFormState.ALLOW_FILL else {
            if (lastAnc == null) AncFormState.NO_FILL else AncFormState.ALLOW_FILL
        },
        0
    )
    else AncStatus(
        benId,
        visitNumber,
        AncFormState.NO_FILL,
        0
    )
}

fun getMinAncFillDate(minWeek: Int, lastAncFilledWeek: Int) =
    if (minWeek - lastAncFilledWeek <= 4) lastAncFilledWeek + 4 else minWeek

fun getAncStatusList(
    list: List<AncStatus>, lmpDate: Long, benId: Long, at: Long
) =
    listOf(1, 2, 3, 4).map {
        getAncStatus(list, lmpDate, it, benId, at)
    }

fun hasPendingAncVisit(
    list: List<AncStatus>, lmpDate: Long, benId: Long, at: Long
): Boolean {
    val l = getAncStatusList(list, lmpDate, benId, at).map { it.formState }
    Timber.tag("MaternalHealthRepo").d("Emitted : at CommonUtls : $l")
    return l.contains(AncFormState.ALLOW_FILL)

}

fun getTodayMillis() = Calendar.getInstance().setToStartOfTheDay().timeInMillis

fun Calendar.setToStartOfTheDay() = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}


sealed class NetworkResponse<T>(val data: T? = null, val message: String? = null) {

    class Idle<T> : NetworkResponse<T>(null, null)
    class Loading<T> : NetworkResponse<T>(null, null)
    class Success<T>(data: T) : NetworkResponse<T>(data = data)
    class Error<T>(message: String) : NetworkResponse<T>(data = null, message = message)

}


