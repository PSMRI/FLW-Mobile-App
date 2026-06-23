package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.text.isDigitsOnly
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.ChildRegDomain
import org.piramalswasthya.sakhi.model.BenPncDomain
import org.piramalswasthya.sakhi.model.BenWithAdolescentDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenWithEcrDomain
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain
import org.piramalswasthya.sakhi.model.BenWithPwrDomain
import org.piramalswasthya.sakhi.model.BenWithHRNPADomain
import org.piramalswasthya.sakhi.model.BenWithHRNPTListDomain
import org.piramalswasthya.sakhi.model.BenWithHRPADomain
import org.piramalswasthya.sakhi.model.BenWithHRPTListDomain
import org.piramalswasthya.sakhi.model.BenWithMalariaConfirmedDomain
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain
import org.piramalswasthya.sakhi.model.BenWithTbSuspectedDomain
import org.piramalswasthya.sakhi.model.Gender
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
    if (text.isBlank()) return list
    val filterText = text.trim().lowercase().replace(" ", "")
    return list.filter { filterForBen(it, filterText) }
}

fun filterOPDBenList(list: List<GeneralOPEDBeneficiary>, text: String): List<GeneralOPEDBeneficiary> {
    if (text.isBlank()) return list
    val filterText = text.trim().lowercase().replace(" ", "")
    return list.filter { filterOPD(it, filterText) }
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
    if (text.isBlank()) return list
    val filterText = text.trim().lowercase().replace(" ", "")
    return list.filter { filterAdolesent(it, filterText) }
}

fun filterForBen(
    ben: BenBasicDomain,
    filterText: String
) = ben.hhId.toString().contains(filterText) ||
        ben.benId.toString().contains(filterText) ||
        ben.abhaId.toString().replace("-", "").contains(filterText) ||
        ben.regDate.lowercase().contains(filterText) ||
        ben.age.lowercase().contains(filterText) ||
        ben.benFullName.lowercase().replace(" ", "").contains(filterText) ||
        ben.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
        ben.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||
        ben.rchId.takeIf { it?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
        ben.mobileNo.contains(filterText) ||
        ben.gender.lowercase() == filterText ||
        ben.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
        ben.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false

fun filterAdolesent(
    ben: BenWithAdolescentDomain,
    filterText: String
) = ben.ben.hhId.toString().contains(filterText) ||
        ben.ben.benId.toString().contains(filterText) ||
        ben.ben.abhaId.toString().replace("-", "").contains(filterText) ||  // ✅ "-" remove
        ben.ben.regDate.lowercase().contains(filterText) ||
        ben.ben.age.lowercase() == filterText ||                                                    // ✅ double lowercase remove
        ben.ben.benFullName.lowercase().replace(" ", "").contains(filterText) ||                    // ✅
        ben.ben.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||                 // ✅
        ben.ben.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||         // ✅
        ben.ben.rchId.takeIf { it?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
        ben.ben.mobileNo.contains(filterText) ||
        ben.ben.gender.lowercase() == filterText ||                                                  // ✅ double lowercase remove
        ben.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||          // ✅
        ben.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false            // ✅

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
) = ben.benName.toString().lowercase().replace(" ", "").contains(filterText)

fun filterEcTrackingList(
    list: List<BenWithEctListDomain>,
    filterText: String
): List<BenWithEctListDomain> {
    if (filterText.isBlank()) return list
    val query = filterText.trim().lowercase().replace(" ", "")
    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().replace(" ", "").contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.numChildren.contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.replace(" ", "").contains(query) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }
}

enum class EcFilterType {
    NEWEST_FIRST, OLDEST_FIRST, AGE_WISE, SYNCING_FIRST, UNSYNCED_FIRST
}

fun sortEcRegistrationList(list: List<BenWithEcrDomain>, sort: EcFilterType): List<BenWithEcrDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenWithEcrDomain> { it.ecr?.createdDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenWithEcrDomain> { it.ecr?.createdDate ?: 0L }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenWithEcrDomain> { if (it.ecr?.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.ecr?.createdDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenWithEcrDomain> { if (it.ecr?.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.ecr?.createdDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
    }

fun sortEcTrackingList(list: List<BenWithEctListDomain>, sort: EcFilterType): List<BenWithEctListDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenWithEctListDomain> { it.ectDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenWithEctListDomain> { it.ectDate }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenWithEctListDomain> { if (it.savedECTRecords.any { r -> r.syncState == SyncState.SYNCING }) 0 else 1 }
                .thenByDescending { it.ectDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenWithEctListDomain> { if (it.allSynced == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.ectDate }
                .thenByDescending { it.ben.benId }
        )
    }

fun sortPncList(list: List<BenPncDomain>, sort: EcFilterType): List<BenPncDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenPncDomain> { it.pncDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenPncDomain> { it.pncDate }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenPncDomain> { if (it.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.pncDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenPncDomain> { if (it.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.pncDate }
                .thenByDescending { it.ben.benId }
        )
    }

fun sortPwrList(list: List<BenWithPwrDomain>, sort: EcFilterType): List<BenWithPwrDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenWithPwrDomain> { it.pwr?.createdDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenWithPwrDomain> { it.pwr?.createdDate ?: 0L }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenWithPwrDomain> { if (it.pwr?.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.pwr?.createdDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenWithPwrDomain> { if (it.pwr?.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.pwr?.createdDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
    }

fun sortAncList(list: List<BenWithAncListDomain>, sort: EcFilterType): List<BenWithAncListDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenWithAncListDomain> { it.ancDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenWithAncListDomain> { it.ancDate }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenWithAncListDomain> { if (it.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.ancDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenWithAncListDomain> { if (it.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.ancDate }
                .thenByDescending { it.ben.benId }
        )
    }

fun sortAbortionList(list: List<BenWithAncListDomain>, sort: EcFilterType): List<BenWithAncListDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenWithAncListDomain> { it.abortionDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenWithAncListDomain> { it.abortionDate ?: 0L }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenWithAncListDomain> { if (it.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.abortionDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenWithAncListDomain> { if (it.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.abortionDate ?: 0L }
                .thenByDescending { it.ben.benId }
        )
    }

fun sortChildRegList(list: List<ChildRegDomain>, sort: EcFilterType): List<ChildRegDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<ChildRegDomain> { it.infant.createdDate }
                .thenByDescending { it.motherBen.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<ChildRegDomain> { it.infant.createdDate }
                .thenBy { it.motherBen.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.motherBen.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<ChildRegDomain> { if (it.infant.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.infant.createdDate }
                .thenByDescending { it.motherBen.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<ChildRegDomain> { if (it.infant.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.infant.createdDate }
                .thenByDescending { it.motherBen.benId }
        )
    }

fun sortInfantRegList(list: List<InfantRegDomain>, sort: EcFilterType): List<InfantRegDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<InfantRegDomain> { it.savedIr?.createdDate ?: 0L }
                .thenByDescending { it.motherBen.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<InfantRegDomain> { it.savedIr?.createdDate ?: 0L }
                .thenBy { it.motherBen.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.motherBen.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<InfantRegDomain> { if (it.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.savedIr?.createdDate ?: 0L }
                .thenByDescending { it.motherBen.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<InfantRegDomain> { if (it.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.savedIr?.createdDate ?: 0L }
                .thenByDescending { it.motherBen.benId }
        )
    }

fun sortHwcList(list: List<BenWithCbacReferDomain>, sort: EcFilterType): List<BenWithCbacReferDomain> =
    when (sort) {
        EcFilterType.NEWEST_FIRST   -> list.sortedWith(
            compareByDescending<BenWithCbacReferDomain> { it.referalCac.revisitDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.OLDEST_FIRST   -> list.sortedWith(
            compareBy<BenWithCbacReferDomain> { it.referalCac.revisitDate }
                .thenBy { it.ben.benId }
        )
        EcFilterType.AGE_WISE       -> list.sortedBy { it.ben.dob }
        EcFilterType.SYNCING_FIRST  -> list.sortedWith(
            compareBy<BenWithCbacReferDomain> { if (it.referalCac.syncState == SyncState.SYNCING) 0 else 1 }
                .thenByDescending { it.referalCac.revisitDate }
                .thenByDescending { it.ben.benId }
        )
        EcFilterType.UNSYNCED_FIRST -> list.sortedWith(
            compareBy<BenWithCbacReferDomain> { if (it.referalCac.syncState == SyncState.UNSYNCED) 0 else 1 }
                .thenByDescending { it.referalCac.revisitDate }
                .thenByDescending { it.ben.benId }
        )
    }

fun filterEcRegistrationList(
    list: List<BenWithEcrDomain>,
    filterText: String
): List<BenWithEcrDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "") // ✅ normalize

    return list.filter {
        it.ben.benId.toString().contains(query) ||                                      // ✅ duplicate remove kiya
                it.ben.age.lowercase().replace(" ", "").contains(query) ||                      // ✅
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||           // ✅
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||              // ✅
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||  // ✅
                it.ben.mobileNo.replace(" ", "").contains(query) ||                             // ✅
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }
}

fun filterPwrRegistrationList(
    list: List<BenWithPwrDomain>,
    filterText: String
): List<BenWithPwrDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().replace(" ", "").contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.replace(" ", "").contains(query) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }
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
): List<BenWithAncListDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list
        .filter {
            it.ben.benId.toString().contains(query) ||
                    it.ben.age.lowercase().replace(" ", "").contains(query) ||
                    it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                    it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                    it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                    it.ben.mobileNo.replace(" ", "").contains(query) ||
                    it.lmpString?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                    it.eddString?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                    it.weeksOfPregnancy?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                    it.ben.rchId.takeIf { id -> id?.isDigitsOnly() == true }?.contains(query) ?: false
        }
        .sortedByDescending { it.ancDate }
}

fun filterAbortionList(
    list: List<BenWithAncListDomain>,
    filterText: String
): List<BenWithAncListDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.contains(query) ||
                it.ben.rchId.takeIf { id -> id?.isDigitsOnly() == true }?.contains(query) ?: false
    }
}

fun filterPncDomainList(
    list: List<BenPncDomain>,
    filterText: String
): List<BenPncDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.contains(query) ||
                it.deliveryDate.replace(" ", "").contains(query) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }.sortedByDescending { it.pncDate }
}

fun filterInfantDomainList(
    list: List<InfantRegDomain>,
    filterText: String
): List<InfantRegDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        val motherFullName = "${it.motherBen.benName} ${it.motherBen.benSurname ?: ""}".lowercase().replace(" ", "")
        val babyFullName = "${it.babyName} ${it.motherBen.benSurname ?: ""}".lowercase().replace(" ", "")

        motherFullName.contains(query) ||
                babyFullName.contains(query) ||
                it.motherBen.benId.toString().contains(query) ||
                it.motherBen.age.lowercase().contains(query) ||
                it.motherBen.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.motherBen.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.motherBen.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.motherBen.mobileNo.contains(query) ||
                it.babyName.lowercase().replace(" ", "").contains(query) ||
                it.motherBen.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }
}


fun filterTbScreeningList(
    list: List<BenWithTbScreeningDomain>,
    filterText: String
): List<BenWithTbScreeningDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.contains(query) ||
                it.ben.gender.lowercase().contains(query) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }
}

fun filterTbSuspectedList(
    list: List<BenWithTbSuspectedDomain>,
    filterText: String
): List<BenWithTbSuspectedDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.contains(query) ||
                it.ben.gender.lowercase().contains(query) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(query) ?: false
    }
}

fun filterMalariaConfirmedList(
    list: List<BenWithMalariaConfirmedDomain>,
    filterText: String
): List<BenWithMalariaConfirmedDomain> {
    if (filterText.isBlank()) return list

    val query = filterText.trim().lowercase().replace(" ", "")

    return list.filter {
        it.ben.benId.toString().contains(query) ||
                it.ben.age.lowercase().contains(query) ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(query) ||
                it.ben.benFullName.lowercase().replace(" ", "").contains(query) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(query) ?: false ||
                it.ben.mobileNo.contains(query) ||
                it.ben.gender.lowercase().contains(query) ||
                it.ben.rchId.takeIf { it1 -> it1.toString().isDigitsOnly() }?.contains(query) ?: false
    }
}

@JvmName("filterBenList1")
fun filterBenFormList(
    list: List<BenBasicDomainForForm>,
    text: String
): List<BenBasicDomainForForm> {
    if (text.isBlank()) return list

    val filterText = text.trim().lowercase().replace(" ", "")

    return list.filter {
        val fullName = "${it.benName} ${it.benSurname ?: ""}".lowercase().replace(" ", "")

        fullName.contains(filterText) ||
                it.hhId.toString().contains(filterText) ||
                it.benId.toString().contains(filterText) ||
                it.regDate.lowercase().contains(filterText) ||
                it.age.lowercase().contains(filterText) ||
                it.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
                it.benName.lowercase().replace(" ", "").contains(filterText) ||
                it.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||
                it.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
                it.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.dateOfDeath?.lowercase()?.contains(filterText) ?: false ||
                it.mobileNo.contains(filterText) ||
                it.gender.lowercase().contains(filterText) ||
                it.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false
    }
}


@JvmName("filterChildRegList")
fun filterBenFormList(
    list: List<ChildRegDomain>,
    text: String
): List<ChildRegDomain> {
    if (text.isBlank()) return list

    val filterText = text.trim().lowercase().replace(" ", "")

    return list.filter {
        val motherFullName = "${it.motherBen.benName} ${it.motherBen.benSurname ?: ""}".lowercase().replace(" ", "")
        val childFullName = "${it.childBen?.benName ?: ""} ${it.childBen?.benSurname ?: ""}".lowercase().replace(" ", "")

        motherFullName.contains(filterText) ||
                childFullName.contains(filterText) ||
                it.motherBen.benId.toString().contains(filterText) ||
                it.motherBen.benName.lowercase().replace(" ", "").contains(filterText) ||
                it.motherBen.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.motherBen.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
                it.motherBen.age.lowercase().contains(filterText) ||
                it.motherBen.mobileNo.contains(filterText) ||
                it.motherBen.rchId?.contains(filterText) == true ||
                it.childBen?.benName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.childBen?.benId?.toString()?.contains(filterText) == true
    }
}

fun filterBenHRPFormList(
    list: List<BenWithHRPADomain>,
    text: String
): List<BenWithHRPADomain> {
    if (text.isBlank()) return list

    val filterText = text.trim().lowercase().replace(" ", "")

    return list.filter {
        val fullName = "${it.ben.benName} ${it.ben.benSurname ?: ""}".lowercase().replace(" ", "")

        fullName.contains(filterText) ||
                it.ben.hhId.toString().contains(filterText) ||
                it.ben.benId.toString().contains(filterText) ||
                it.ben.regDate.lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
                it.ben.benName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.ben.mobileNo.contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false
    }
}

fun filterBenHRNPFormList(
    list: List<BenWithHRNPADomain>,
    text: String
): List<BenWithHRNPADomain> {
    if (text.isBlank()) return list

    val filterText = text.trim().lowercase().replace(" ", "")

    return list.filter {
        val fullName = "${it.ben.benName} ${it.ben.benSurname ?: ""}".lowercase().replace(" ", "")

        fullName.contains(filterText) ||
                it.ben.hhId.toString().contains(filterText) ||
                it.ben.benId.toString().contains(filterText) ||
                it.ben.regDate.lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
                it.ben.benName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.ben.mobileNo.contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false
    }
}

fun filterBenHRPTFormList(
    list: List<BenWithHRPTListDomain>,
    text: String
): List<BenWithHRPTListDomain> {
    if (text.isBlank()) return list

    val filterText = text.trim().lowercase().replace(" ", "")

    return list.filter {

        val fullName = "${it.ben.benName} ${it.ben.benSurname ?: ""}".lowercase().replace(" ", "")

        fullName.contains(filterText) ||
                it.ben.hhId.toString().contains(filterText) ||
                it.ben.benId.toString().contains(filterText) ||
                it.ben.regDate.lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
                it.ben.benName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.ben.mobileNo.contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false
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
    val token = filterText.trim().lowercase().replace(" ", "")

    val age = imm.ben.age?.lowercase() ?: ""

    val name = "${imm.ben.benName} ${imm.ben.benSurname ?: ""}".lowercase().replace(" ", "")
    val mother = imm.ben.motherName?.lowercase()?.replace(" ", "") ?: ""
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
    if (text.isBlank()) return list

    val filterText = text.trim().lowercase().replace(" ", "")

    return list.filter {
        val fullName = "${it.ben.benName} ${it.ben.benSurname ?: ""}".lowercase().replace(" ", "")

        fullName.contains(filterText) ||
                it.ben.hhId.toString().contains(filterText) ||
                it.ben.benId.toString().contains(filterText) ||
                it.ben.regDate.lowercase().contains(filterText) ||
                it.ben.age.lowercase().contains(filterText) ||
                it.ben.rchId.takeIf { it1 -> it1?.isDigitsOnly() == true }?.contains(filterText) ?: false ||
                it.ben.benName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.benSurname?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false ||
                it.ben.familyHeadName.lowercase().replace(" ", "").contains(filterText) ||
                it.ben.spouseName?.lowercase()?.replace(" ", "")?.contains(filterText) == true ||
                it.ben.mobileNo.contains(filterText) ||
                it.ben.gender.lowercase().contains(filterText) ||
                it.ben.fatherName?.lowercase()?.replace(" ", "")?.contains(filterText) ?: false
    }
}

fun getWeeksOfPregnancy(regLong: Long, lmpLong: Long?): Int {
    return lmpLong?.let {
        (TimeUnit.MILLISECONDS.toDays(regLong - it) / 7).toInt()
    } ?: 0
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



fun Context.getLocalizedAgeUnit(ageUnit: AgeUnit?): String {
    return when (ageUnit?.name) {
        "YEAR", "YEARS" -> resources.getString(R.string.years)
        "MONTH", "MONTHS" -> resources.getString(R.string.months)
        "DAY", "DAYS" -> resources.getString(R.string.days)
        else -> ageUnit?.name ?: ""
    }
}

fun Context.getLocalizedVisit(visitNumber: Int): String {
    return resources.getString(
        R.string.visit,
        visitNumber
    )
}




fun Context.getLocalizedMonthText(value: String?): String {
    if (value.isNullOrBlank()) return ""

    return value
        .replace("Months", getString(R.string.months), ignoreCase = true)
        .replace("Month", getString(R.string.month), ignoreCase = true)
}

fun Context.getLocalizedGender(gender: Gender?): String {
    return when (gender) {
        Gender.MALE -> resources.getString(R.string.male)
        Gender.FEMALE -> resources.getString(R.string.female)
        Gender.TRANSGENDER -> resources.getString(R.string.transgender)
        null -> ""
    }
}

fun getLocalizedAge(context: Context, dob: Long): String {

    val calDob = Calendar.getInstance().apply {
        timeInMillis = dob
    }

    val calNow = Calendar.getInstance()

    val diffDays =
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - dob).toInt()

    if (diffDays < 31) {
        return "$diffDays ${
            context.getString(
                if (diffDays == 1) R.string.day else R.string.days
            )
        }"
    }

    var years = calNow.get(Calendar.YEAR) - calDob.get(Calendar.YEAR)
    var months = calNow.get(Calendar.MONTH) - calDob.get(Calendar.MONTH)
    var days = calNow.get(Calendar.DAY_OF_MONTH) - calDob.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months--
    }

    if (months < 0) {
        years--
        months += 12
    }

    return buildString {

        if (years > 0) {
            append("$years ")
            append(
                context.getString(
                    if (years == 1) R.string.year else R.string.years
                )
            )
        }

        if (months > 0) {
            append(" $months ")
            append(
                context.getString(
                    if (months == 1) R.string.month else R.string.months
                )
            )
        }

        if (days > 0) {
            append(" $days ")
            append(
                context.getString(
                    if (days == 1) R.string.day else R.string.days
                )
            )
        }
    }
}


