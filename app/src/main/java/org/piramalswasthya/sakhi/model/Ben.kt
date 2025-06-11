package org.piramalswasthya.sakhi.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.model.BenBasicCache.Companion.getAgeFromDob
import org.piramalswasthya.sakhi.model.BenBasicCache.Companion.getAgeUnitFromDob
import org.piramalswasthya.sakhi.utils.HelperUtil.getDateStringFromLong
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

//enum class TypeOfList {
//    INFANT,
//    CHILD,
//    ADOLESCENT,
//    GENERAL,
//    ELIGIBLE_COUPLE,
//    ANTENATAL_MOTHER,
//    DELIVERY_STAGE,
//    POSTNATAL_MOTHER,
//    MENOPAUSE,
//    TEENAGER,
//    OTHER,
//
//}

enum class AgeUnit {
    DAYS,
    MONTHS,
    YEARS
}

enum class Gender {
    MALE,
    FEMALE,
    TRANSGENDER
}

// In your BenBasicCache.kt file, REPLACE the old @DatabaseView with this one.
@DatabaseView(
    viewName = "BEN_BASIC_CACHE",
    value = "SELECT b.beneficiaryId as benId, b.householdId as hhId, b.regDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob, b.familyHeadRelationPosition as relToHeadId" +
            ", b.contactNumber as mobileNo, b.fatherName, h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod" +
            ", b.isHrpStatus as hrpStatus, b.syncState, b.gen_reproductiveStatusId as reproductiveStatusId, b.isKid, b.immunizationStatus" +
            ", b.loc_village_id as villageId, b.abha_healthIdNumber as abhaId" +
            ", b.isNewAbha" + // FIX: Using only one, correct source for isNewAbha.
            ", IFNULL(cbac.benId IS NOT NULL, 0) as cbacFilled, cbac.syncState as cbacSyncState" +
            ", IFNULL(cdr.benId IS NOT NULL, 0) as cdrFilled, cdr.syncState as cdrSyncState" +
            ", IFNULL(mdsr.benId IS NOT NULL, 0) as mdsrFilled, mdsr.syncState as mdsrSyncState" +
            ", IFNULL(pmsma.benId IS NOT NULL, 0) as pmsmaFilled, pmsma.syncState as pmsmaSyncState" +
            ", IFNULL(hbnc.benId IS NOT NULL, 0) as hbncFilled" +
            ", IFNULL(hbyc.benId IS NOT NULL, 0) as hbycFilled" +
            ", IFNULL(pwr.benId IS NOT NULL, 0) as pwrFilled, pwr.syncState as pwrSyncState" +
            ", IFNULL(pwa.pregnantWomanDelivered, 0) as isDelivered, IFNULL(pwa.hrpConfirmed, 0) as pwHrp" +
            ", IFNULL(ecr.benId IS NOT NULL, 0) as ecrFilled" +
            ", IFNULL(ect.benId IS NOT NULL, 0) as ectFilled" + // FIX: Removed duplicate ectFilled and used a safe version.
            ", IFNULL((pwa.maternalDeath OR do.complication = 'DEATH' OR pnc.motherDeath), 0) as isMdsr" +
            ", IFNULL(tbsn.benId IS NOT NULL, 0) as tbsnFilled, tbsn.syncState as tbsnSyncState" +
            ", IFNULL(tbsp.benId IS NOT NULL, 0) as tbspFilled, tbsp.syncState as tbspSyncState" +
            ", IFNULL(ir.motherBenId IS NOT NULL, 0) as irFilled, ir.syncState as irSyncState" +
            ", IFNULL(cr.motherBenId IS NOT NULL, 0) as crFilled, cr.syncState as crSyncState" +
            ", IFNULL(do.benId IS NOT NULL, 0) as doFilled, do.syncState as doSyncState" +
            ", IFNULL((hrppa.benId IS NOT NULL AND hrppa.noOfDeliveries IS NOT NULL AND hrppa.timeLessThan18m IS NOT NULL AND hrppa.heightShort IS NOT NULL AND hrppa.age IS NOT NULL AND hrppa.rhNegative IS NOT NULL AND hrppa.homeDelivery IS NOT NULL AND hrppa.badObstetric IS NOT NULL AND hrppa.multiplePregnancy IS NOT NULL), 0) as hrppaFilled, hrppa.syncState as hrppaSyncState" +
            ", IFNULL((hrpnpa.benId IS NOT NULL AND hrpnpa.noOfDeliveries IS NOT NULL AND hrpnpa.timeLessThan18m IS NOT NULL AND hrpnpa.heightShort IS NOT NULL AND hrpnpa.age IS NOT NULL AND hrpnpa.misCarriage IS NOT NULL AND hrpnpa.homeDelivery IS NOT NULL AND hrpnpa.medicalIssues IS NOT NULL AND hrpnpa.pastCSection IS NOT NULL), 0) as hrpnpaFilled, hrpnpa.syncState as hrpnpaSyncState" +
            ", IFNULL(hrpmbp.benId IS NOT NULL, 0) as hrpmbpFilled, hrpmbp.syncState as hrpmbpSyncState" +
            ", IFNULL(hrpt.benId IS NOT NULL, 0) as hrptFilled, IFNULL(((count(distinct hrpt.id) > 3) OR (((JulianDay('now')) - JulianDay(date(max(hrpt.visitDate)/1000,'unixepoch','localtime'))) < 1)), 0) as hrptrackingDone, hrpt.syncState as hrptSyncState" +
            ", IFNULL(hrnpt.benId IS NOT NULL, 0) as hrnptFilled, IFNULL(((JulianDay('now') - JulianDay(date(max(hrnpt.visitDate)/1000,'unixepoch','localtime'))) < 1), 0) as hrnptrackingDone, hrnpt.syncState as hrnptSyncState " +
            "FROM BENEFICIARY b " +
            "JOIN HOUSEHOLD h ON b.householdId = h.householdId " +
            "LEFT OUTER JOIN CBAC cbac ON b.beneficiaryId = cbac.benId " +
            "LEFT OUTER JOIN CDR cdr ON b.beneficiaryId = cdr.benId " +
            "LEFT OUTER JOIN MDSR mdsr ON b.beneficiaryId = mdsr.benId " +
            "LEFT OUTER JOIN PMSMA pmsma ON b.beneficiaryId = pmsma.benId " +
            "LEFT OUTER JOIN HBNC hbnc ON b.beneficiaryId = hbnc.benId " +
            "LEFT OUTER JOIN HBYC hbyc ON b.beneficiaryId = hbyc.benId " +
            "LEFT OUTER JOIN PREGNANCY_REGISTER pwr ON b.beneficiaryId = pwr.benId " +
            "LEFT OUTER JOIN PREGNANCY_ANC pwa ON b.beneficiaryId = pwa.benId " +
            "LEFT OUTER JOIN pnc_visit pnc ON b.beneficiaryId = pnc.benId " +
            "LEFT OUTER JOIN ELIGIBLE_COUPLE_REG ecr ON b.beneficiaryId = ecr.benId " +
            "LEFT OUTER JOIN ELIGIBLE_COUPLE_TRACKING ect ON (b.beneficiaryId = ect.benId AND CAST((strftime('%s','now') - ect.visitDate/1000)/60/60/24 AS INTEGER) < 30) " +
            "LEFT OUTER JOIN TB_SCREENING tbsn ON b.beneficiaryId = tbsn.benId " +
            "LEFT OUTER JOIN TB_SUSPECTED tbsp ON b.beneficiaryId = tbsp.benId " +
            "LEFT OUTER JOIN HRP_PREGNANT_ASSESS hrppa ON b.beneficiaryId = hrppa.benId " +
            "LEFT OUTER JOIN HRP_NON_PREGNANT_ASSESS hrpnpa ON b.beneficiaryId = hrpnpa.benId " +
            "LEFT OUTER JOIN HRP_MICRO_BIRTH_PLAN hrpmbp ON b.beneficiaryId = hrpmbp.benId " +
            "LEFT OUTER JOIN HRP_NON_PREGNANT_TRACK hrnpt ON b.beneficiaryId = hrnpt.benId " +
            "LEFT OUTER JOIN HRP_PREGNANT_TRACK hrpt ON b.beneficiaryId = hrpt.benId " +
            "LEFT OUTER JOIN DELIVERY_OUTCOME do ON b.beneficiaryId = do.benId " +
            "LEFT OUTER JOIN INFANT_REG ir ON b.beneficiaryId = ir.motherBenId " +
            "LEFT OUTER JOIN CHILD_REG cr ON b.beneficiaryId = cr.motherBenId " +
            "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC"
)
data class BenBasicCache(
    val benId: Long,
    val hhId: Long,
    val regDate: Long,
    val benName: String,
    val benSurname: String? = null,
    val gender: Gender,
    val dob: Long,
    val relToHeadId: Int,
    val mobileNo: Long,
    val fatherName: String? = null,
    val familyHeadName: String? = null,
//    val typeOfList: TypeOfList,
    val spouseName: String? = null,
    val rchId: String? = null,
    val hrpStatus: Boolean,
    val syncState: SyncState?,
    val reproductiveStatusId: Int,
    val lastMenstrualPeriod: Long?,
    val isKid: Boolean,
    val immunizationStatus: Boolean,
    val villageId: Int,
    val abhaId: String?,
    val isNewAbha: Boolean,
    val cbacFilled: Boolean,
    val cbacSyncState: SyncState?,
    val cdrFilled: Boolean,
    val cdrSyncState: SyncState?,
    val mdsrFilled: Boolean,
    val mdsrSyncState: SyncState?,
    val pmsmaSyncState: SyncState?,
    val pmsmaFilled: Boolean,
    val hbncFilled: Boolean,
    val hbycFilled: Boolean,
    val pwrFilled: Boolean,
    val pwrSyncState: SyncState?,
    val doSyncState: SyncState?,
    val irSyncState: SyncState?,
    val crSyncState: SyncState?,
    val ecrFilled: Boolean,
    val ectFilled: Boolean,
    val tbsnFilled: Boolean,
    val tbsnSyncState: SyncState?,
    val tbspFilled: Boolean,
    val tbspSyncState: SyncState?,
    val hrppaFilled: Boolean,
    val hrpnpaFilled: Boolean,
    val hrpmbpFilled: Boolean,
    val hrptFilled: Boolean,
    val hrptrackingDone: Boolean,
    val hrnptrackingDone: Boolean,
    val hrnptFilled: Boolean,
    val hrppaSyncState: SyncState?,
    val hrpnpaSyncState: SyncState?,
    val hrpmbpSyncState: SyncState?,
    val hrptSyncState: SyncState?,
    val hrnptSyncState: SyncState?,
    val isDelivered: Boolean,
    val pwHrp: Boolean,
    val irFilled: Boolean,
    val isMdsr: Boolean,
    val crFilled: Boolean,
    val doFilled: Boolean,
) {
    companion object {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        fun getAgeFromDob(dob: Long): Int {
            val diffLong = System.currentTimeMillis() - dob
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffLong).toInt()
            if (diffDays < 31)
                return diffDays
            val yearDiff = (diffDays / 365)
            if (yearDiff > 0)
                return yearDiff
            val calDob = Calendar.getInstance()
            calDob.timeInMillis = dob
            val calNow = Calendar.getInstance()
            return (calNow.get(Calendar.YEAR) * 12 + calNow.get(Calendar.MONTH)) - (calDob.get(
                Calendar.YEAR
            ) * 12 + calDob.get(
                Calendar.MONTH
            )
                    )


        }

        fun getAgeUnitFromDob(dob: Long): AgeUnit {
            val calDob = Calendar.getInstance().apply {
                timeInMillis = dob
            }
            val calNow = Calendar.getInstance()
            val diffYears = getDiffYears(calDob, calNow)
            val diffLong = System.currentTimeMillis() - dob
            return if (TimeUnit.MILLISECONDS.toDays(diffLong).toInt() in 0..31) {
                AgeUnit.DAYS
            } else if (diffYears == 0)
                AgeUnit.MONTHS
            else
                AgeUnit.YEARS

        }



        private fun getDiffYears(a: Calendar, b: Calendar): Int {
            var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
            if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) || a.get(Calendar.MONTH) == b.get(
                    Calendar.MONTH
                ) && a.get(
                    Calendar.DAY_OF_MONTH
                ) > b.get(
                    Calendar.DAY_OF_MONTH
                )
            ) {
                diff--
            }
            return diff
        }


    }


    fun asBasicDomainModel(): BenBasicDomain {
        return BenBasicDomain(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "",
            gender = gender.name,
            dob = dob,
            abhaId = abhaId,
            isNewAbha = isNewAbha,
            relToHeadId = relToHeadId,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName?.takeIf { it.isNotEmpty() } ?: "Not Available",
         /*   motherName = motherName,*/
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            spouseName = spouseName?.takeIf { it.isNotEmpty() } ?: "Not Available",
            rchId = rchId?.takeIf { it.isNotEmpty() } ?: "Not Available",
            hrpStatus = hrpStatus,
            syncState = syncState
        )
    }

    fun asBasicDomainModelCHO(): BenBasicDomain {
        return BenBasicDomain(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "",
            spouseName = spouseName ?: "Not Available",
            gender = gender.name,
            dob = dob,
            abhaId = abhaId,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "",
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            relToHeadId = 0,
            syncState = syncState
        )
    }


    fun asBenBasicDomainModelForTbsnForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = tbsnFilled,
            syncState = tbsnSyncState
                ?: throw IllegalStateException("Sync state for tbsn is null!!")
        )
    }

    fun asBenBasicDomainModelForTbspForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = tbspFilled,
            syncState = tbspSyncState
                ?: throw IllegalStateException("Sync state for tbsp is null!!")
        )
    }

    fun asBenBasicDomainModelForCdrForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = cdrFilled,
            syncState = cdrSyncState
                ?: throw IllegalStateException("Sync state for cbac is null!!")
        )
    }

    fun asBenBasicDomainModelForMdsrForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = mdsrFilled,
            syncState = mdsrSyncState
                ?: throw IllegalStateException("Sync state for mdsr is null!!")
        )
    }

    fun asBenBasicDomainModelForPmsmaForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = pmsmaFilled,
            syncState = syncState
        )
    }

    fun asBenBasicDomainModelECTForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = ectFilled,
            syncState = syncState
        )
    }

    fun asBasicDomainModelForPmjayForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = false,
            syncState = syncState
        )
    }

    fun asBenBasicDomainModelForHbncForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = false,
            form1Enabled = hbncFilled || dob > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                42
            )),
            syncState = syncState
        )
    }

    fun asBenBasicDomainModelForHbycForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            syncState = syncState,
            form1Filled = false,
            form1Enabled = hbycFilled || dob > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                490
            )),
        )
    }

    fun asBenBasicDomainModelForPregnantWomanRegistrationForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = pwrFilled,
            syncState = pwrSyncState
        )
    }

    fun asBenBasicDomainModelForHRPPregAssessmentForm(): BenBasicDomainForForm {

        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "",
            spouseName = spouseName ?: "",
            lastMenstrualPeriod = getDateStringFromLong(lastMenstrualPeriod),
            edd = getEddFromLmp(lastMenstrualPeriod),
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = hrppaFilled,
            syncState = hrppaSyncState,
            form2Enabled = true,
            form2Filled = hrpmbpFilled
        )
    }

    fun asBenBasicDomainModelForHRPNonPregAssessmentForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "",
            spouseName = spouseName ?: "",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = hrpnpaFilled,
            syncState = hrpnpaSyncState
        )
    }

    fun asBenBasicDomainModelForHRPNonPregTrackForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "",
            spouseName = spouseName ?: "",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = hrnptrackingDone,
            form1Enabled = !hrnptrackingDone,
            form2Filled = hrnptFilled,
            form2Enabled = hrnptFilled,
            syncState = hrnptSyncState
        )
    }

    fun asBenBasicDomainModelForHRPPregTrackForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "",
            spouseName = spouseName ?: "",
            gender = gender.name,
            dob = dob,
            lastMenstrualPeriod = getDateStringFromLong(lastMenstrualPeriod),
            edd = getEddFromLmp(lastMenstrualPeriod),
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = hrptrackingDone,
            form1Enabled = !hrptrackingDone,
            form2Filled = hrptFilled,
            form2Enabled = hrptFilled,
            syncState = hrptSyncState
        )
    }

    fun asBenBasicDomainModelForInfantRegistrationForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = irFilled,
            syncState = irSyncState
        )
    }

    fun asBenBasicDomainModelForChildRegistrationForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = irFilled,
            syncState = crSyncState
        )
    }

    fun asBenBasicDomainModelForDeliveryOutcomeForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = doFilled,
            syncState = doSyncState
        )
    }

    fun asBenBasicDomainModelForEligibleCoupleRegistrationForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = ecrFilled,
            syncState = syncState
        )
    }

    fun asBasicDomainModelForFpotForm(): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = benId,
            hhId = hhId,
            regDate = dateFormat.format(Date(regDate)),
            benName = benName,
            benSurname = benSurname ?: "Not Available",
            gender = gender.name,
            dob = dob,
            mobileNo = mobileNo.toString(),
            fatherName = fatherName,
            familyHeadName = familyHeadName ?: "Not Available",
//            typeOfList = typeOfList.name,
            rchId = rchId ?: "Not Available",
            hrpStatus = hrpStatus,
            form1Filled = false,
            syncState = syncState
        )
    }

}


data class BenBasicDomain(
    val benId: Long,
    val hhId: Long,
    val regDate: String,
    val benName: String,
    val benSurname: String? = null,
    val benFullName: String = "$benName $benSurname",
    val gender: String,
    val dob: Long,
    val ageInt: Int = getAgeFromDob(dob),
    val ageUnit: AgeUnit = getAgeUnitFromDob(dob),
    val age: String = "$ageInt ${ageUnit.name}",
    val relToHeadId: Int,
    val mobileNo: String,
    val abhaId: String? = null,
    val isNewAbha: Boolean = false,
    val fatherName: String? = null,
    val motherName: String? = null,
    val familyHeadName: String,
    val spouseName: String? = null,
//    val typeOfList: String,
    val rchId: String,
    val hrpStatus: Boolean = false,
    var syncState: SyncState?
)


data class BenBasicDomainForForm(
    val benId: Long,
    val hhId: Long,
    val regDate: String,
    val benName: String,
    val benSurname: String? = null,
    val gender: String,
    val dob: Long,
    val ageInt: Int = getAgeFromDob(dob),
    val ageUnit: AgeUnit = getAgeUnitFromDob(dob),
    val age: String = "$ageInt $ageUnit",
    val mobileNo: String,
    val fatherName: String? = null,
    val spouseName: String? = null,
    val familyHeadName: String,
    val lastMenstrualPeriod: String? = null,
    val edd: String? = null,
//    val typeOfList: String,
    val rchId: String,
    val hrpStatus: Boolean = false,
    val form1Filled: Boolean = false,
    val form2Filled: Boolean = false,
    val form3Filled: Boolean = false,
    var form1Enabled: Boolean = true,
    val form2Enabled: Boolean = true,
    val form3Enabled: Boolean = true,
    val formsFilled: Int = 0,
    var syncState: SyncState?
) {
    companion object
}

data class BenRegKid(
    var childName: String? = null,
    var childRegisteredAWC: String? = null,
    var childRegisteredAWCId: Int = 0,
    var childRegisteredSchool: String? = null,
    var childRegisteredSchoolId: Int = 0,
    var typeOfSchool: String? = null,
    var typeOfSchoolId: Int = 0,
    var birthPlace: String? = null,
    var birthPlaceId: Int = 0,
    var facilityName: String? = null,
    var facilityId: Int = 0,
    var facilityOther: String? = null,
    var placeName: String? = null,
    var conductedDelivery: String? = null,
    var conductedDeliveryId: Int = 0,
    var conductedDeliveryOther: String? = null,
    var deliveryType: String? = null,
    var deliveryTypeId: Int = 0,
    var complications: String? = null,
    var complicationsId: Int = 0,
    var complicationsOther: String? = null,
    var term: String? = null,
    var termId: Int = 0,
    var gestationalAge: String? = null,
    var gestationalAgeId: Int = 0,
    var corticosteroidGivenMother: String? = null,
    var corticosteroidGivenMotherId: Int = 0,
    var criedImmediately: String? = null,
    var criedImmediatelyId: Int = 0,
    var birthDefects: String? = null,
    var birthDefectsId: Int = 0,
    var birthDefectsOthers: String? = null,
    var heightAtBirth: Double = 0.0,
    var weightAtBirth: Double = 0.0,
    var feedingStarted: String? = null,
    var feedingStartedId: Int = 0,
    var birthDosage: String? = null,
    var birthDosageId: Int = 0,
    var opvBatchNo: String? = null,
    var opvGivenDueDate: String? = null,
    var opvDate: String? = null,
    var bcdBatchNo: String? = null,
    var bcgGivenDueDate: String? = null,
    var bcgDate: String? = null,
    var hptBatchNo: String? = null,
    var hptGivenDueDate: String? = null,
    var hptDate: String? = null,
    var vitaminKBatchNo: String? = null,
    var vitaminKGivenDueDate: String? = null,
    var vitaminKDate: String? = null,
    var deliveryTypeOther: String? = null,
    var birthCertificateNumber: String? = null,
    var motherBenId: Long? = null,
    var childMotherName: String? = null,
    var motherPosition: Int? = null,
    var birthBCG: Boolean = false,
    var birthHepB: Boolean = false,
    var birthOPV: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class BenRegKidNetwork(
    val id: Int = 0,
    val benficieryid: Long,
    val childName: String? = null,
    val birthPlace: String? = null,
    val birthPlaceid: Int = 0,
    val facilityName: String? = null,
    val facilityid: Int = 0,
    val ashaid: Int = 0,
    val facilityOther: String? = null,
    val placeName: String? = null,
    val conductedDelivery: String? = null,
    val conductedDeliveryid: Int = 0,
    val conductedDeliveryOther: String? = null,
    val deliveryType: String? = null,
    val deliveryTypeid: Int = 0,
    val deliveryTypeOther: String? = null,
    @Json(name = "complecations")
    val complications: String? = null,
    @Json(name = "complecationsid")
    val complicationsid: Int = 0,
    val complicationsOther: String? = null,
    val term: String? = null,
    val termid: Int = 0,
    val gestationalAge: String? = null,
    val gestationalAgeid: Int = 0,
    val corticosteroidGivenMother: String? = null,
    val corticosteroidGivenMotherid: Int = 0,
    val criedImmediately: String? = null,
    val criedImmediatelyid: Int = 0,
    val birthDefects: String? = null,
    val birthDefectsid: Int = 0,
    val birthDefectsOthers: String? = null,
    val heightAtBirth: Int = 0,
    val weightAtBirth: Float = 0F,
    val feedingStarted: String? = null,
    val feedingStartedid: Int = 0,
    val birthDosage: String? = null,
    val birthDosageid: Int = 0,
    val opvBatchNo: String? = null,
    val opvGivenDueDate: String? = null,
    val opvDate: String? = null,
    val bcdBatchNo: String? = null,
    val bcgGivenDueDate: String? = null,
    val bcgDate: String? = null,
    val hptdBatchNo: String? = null,
    val hptGivenDueDate: String? = null,
    val hptDate: String? = null,
    val vitaminkBatchNo: String? = null,
    val vitaminkGivenDueDate: String? = null,
    val vitaminkDate: String? = null,

    val createdBy: String? = null,
    val createdDate: String? = null,
    val serverUpdatedStatus: Int = 0,
    val updatedBy: String? = null,
    val updatedDate: String? = null,

    val ProviderServiceMapID: Int = 0,
    val VanID: Int = 4,
    val Processed: String? = null,
    val Countyid: Int = 0,
    val stateid: Int = 0,
    val districtid: Int = 0,
    val districtname: String? = null,
    val villageid: Int = 0,

    val motherBenId: Long? = null,
    val motherName: String? = null,
    val motherposition: Int? = 0,


    val birthBCG: Boolean? = null,
    val birthHepB: Boolean? = null,
    val birthOPV: Boolean? = null,

    )

data class BenHealthIdDetails(
    var healthId: String = "",
    var healthIdNumber: String = "",
    var isNewAbha: Boolean= false
)

data class BenRegGen(

    var maritalStatus: String? = null,
    var maritalStatusId: Int = 0,
    var spouseName: String? = null,
    var ageAtMarriage: Int = 0,
//    var dateOfMarriage: Long = 0,
    var marriageDate: Long? = null,
    //Menstrual details
//    var menstrualStatus: String? = null,
//    var menstrualStatusId: Int? = 0,
//    var regularityOfMenstrualCycle: String? = null,
//    var regularityOfMenstrualCycleId: Int = 0,
//    var lengthOfMenstrualCycle: String? = null,
//    var lengthOfMenstrualCycleId: Int = 0,
//    var menstrualBFD: String? = null,
//    var menstrualBFDId: Int = 0,
//    var menstrualProblem: String? = null,
//    var menstrualProblemId: Int = 0,
    var lastMenstrualPeriod: Long? = null,
    var reproductiveStatus: String? = null,
    var reproductiveStatusId: Int = 0,
//    var lastDeliveryConducted: String? = null,
//    var lastDeliveryConductedId: Int = 0,
//    var otherLastDeliveryConducted: String? = null,
//    var facilityName: String? = null,
//    var whoConductedDelivery: String? = null,
//    var whoConductedDeliveryId: Int = 0,
//    var noOfDaysForDelivery: Int? = null,
//    var otherWhoConductedDelivery: String? = null,
//    var deliveryDate: String? = null,
//    var expectedDateOfDelivery: Long? = null,
//    var numPreviousLiveBirth: Int = 0,
//    var formStatus: String? = null,
//    var formType: String? = null,
//    var ancCount: Int = 0,
//    var hrpCount: Int = 0,
//    var hrpSuspected: Boolean = false,
//    var isDeathStatus: Boolean = false,
)

@Entity(
    tableName = "BENEFICIARY",
    primaryKeys = ["beneficiaryId"],
    foreignKeys = [
        ForeignKey(
            entity = HouseholdCache::class,
            parentColumns = arrayOf("householdId"),
            childColumns = arrayOf("householdId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(name = "ind_ben", value = ["beneficiaryId"/*, "householdId"*/])]
)

data class BenRegCache(

    @ColumnInfo(index = true)
    var householdId: Long,

    var beneficiaryId: Long,

    var benRegId: Long = 0,

    @ColumnInfo(index = true)
    var ashaId: Int,

    var isKid: Boolean,

    var isAdult: Boolean,

    var userImage: String? = null,

//    @Suppress("ArrayInDataClass")
//    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
//    var userImageBlob: ByteArray? = null,

    var regDate: Long = 0,

    var firstName: String? = null,

    var lastName: String? = null,

    var gender: Gender? = null,

    var genderId: Int = 0,

    var dob: Long = 0,

    var age: Int = 0,

    var ageUnit: AgeUnit? = null,

    var ageUnitId: Int = 0,

    var fatherName: String? = null,

    var motherName: String? = null,

    var familyHeadRelation: String? = null,

    var familyHeadRelationPosition: Int = 0,

    var familyHeadRelationOther: String? = null,

    var mobileNoOfRelation: String? = null,

    var mobileNoOfRelationId: Int = 0,

    var mobileOthers: String? = null,

    var contactNumber: Long = 0,

    var literacy: String? = null,

    var literacyId: Int = 0,

    var community: String? = null,

    var communityId: Int = 0,

    var religion: String? = null,

    var religionId: Int = 0,

    var religionOthers: String? = null,

    var rchId: String? = null,

//    var registrationType: TypeOfList? = null,

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    ///////////////////////////Bank details Start///////////////////////////
    var hasAadhar: Boolean? = false,

    var hasAadharId: Int = 0,

    var aadharNum: String? = null,

    var aadharNumId: Int = 0,

    var bankAccountId: Int = 0,

    var bankAccount: String? = null,

    var nameOfBank: String? = null,

    var nameOfBranch: String? = null,

    var ifscCode: String? = null,

    ///////////////////////////Bank details End///////////////////////////
    var needOpCare: String? = null,

    var needOpCareId: Int = 0,

    var ncdPriority: Int = 0,

    var cbacAvailable: Boolean = false,

    var guidelineId: String? = null,

    var isHrpStatus: Boolean = false,

    var immunizationStatus: Boolean = false,

    var hrpIdentificationDate: String? = null,

    var hrpLastVisitDate: String? = null,

    var suspectedNcd: String? = null,

    var suspectedNcdDiseases: String? = null,

    var suspectedTb: String? = null,

    var confirmed_Ncd: String? = null,

    var confirmedHrp: String? = null,

    var suspectedHrp: String? = null,

    var confirmedTb: String? = null,

    var confirmedNcdDiseases: String? = null,

    var diagnosisStatus: String? = null,


    /*
    5 Skipped:
        Aadhar, lastHrpVisitDate, marriageDate ( 2 copies)
        vanId and serviceMap ID, ( Can get from Foreign key)
     */


    @Embedded(prefix = "kid_")
    var kidDetails: BenRegKid? = null,

    @Embedded(prefix = "gen_")
    var genDetails: BenRegGen? = null,

    @Embedded(prefix = "loc_")
    var locationRecord: LocationRecord,

    @Embedded(prefix = "abha_")
    var healthIdDetails: BenHealthIdDetails? = null,

    var processed: String? = null,

    var serverUpdatedStatus: Int = 0,

    var createdBy: String? = null,

    var createdDate: Long? = null,

    var updatedBy: String? = null,

    var updatedDate: Long? = null,

    var syncState: SyncState,

    var isDraft: Boolean,

    var isNewAbha: Boolean=false,

    ) : FormDataModel {

    fun asNetworkPostModel(context: Context, user: User): BenPost {
        return BenPost(
            householdId = householdId.toString(),
            benRegId = benRegId,
            countyid = locationRecord.country.id,
            processed = processed,
            providerServiceMapID = user.serviceMapId,
            vanID = user.vanId,
            aadhaNo = aadharNum ?: "",
            aadha_no = when (hasAadhar) {
                true -> "Yes"
                false -> "No"
                else -> "null"
            },
            aadha_noId = hasAadharId,
            age = age,
            ageAtMarriage = genDetails?.ageAtMarriage ?: 0,
            age_unit = when (ageUnit) {
                AgeUnit.YEARS -> "Year(s)"
                AgeUnit.MONTHS -> "Month(s)"
                AgeUnit.DAYS -> "Day(s)"
                else -> throw IllegalStateException("age_unit enum invalid data!")
            },
            age_unitId = ageUnit!!.ordinal,
            ashaId = ashaId,
            benId = beneficiaryId,
            registrationDate = getDateTimeStringFromLong(regDate)!!,
            marriageDate = getDateTimeStringFromLong(genDetails?.marriageDate),
            mobileNoOfRelation = mobileNoOfRelation,
            mobileNoOfRelationId = mobileNoOfRelationId,
            mobileOthers = mobileOthers ?: "",
            literacy = literacy,
            literacyId = literacyId,
            religionOthers = religionOthers ?: "",
            rchId = rchId ?: "",
            registrationType = if (kidDetails == null) "General Beneficiary" else "NewBorn",
            /*when (registrationType) {
                TypeOfList.INFANT,
                TypeOfList.CHILD,
                TypeOfList.ADOLESCENT -> "NewBorn"
                TypeOfList.GENERAL,
                TypeOfList.ELIGIBLE_COUPLE,
                TypeOfList.ANTENATAL_MOTHER,
                TypeOfList.DELIVERY_STAGE,
                TypeOfList.POSTNATAL_MOTHER,
                TypeOfList.MENOPAUSE,
                TypeOfList.TEENAGER -> "General Beneficiary"
                else -> "Other"
            }*/
            latitude = latitude,
            longitude = longitude,
            needOpCare = needOpCare ?: "null",
            needOpCareId = needOpCareId,
//            menstrualStatusId = 0,
//            regularityofMenstrualCycleId = 0,
//            lengthofMenstrualCycleId =  0,
//            menstrualBFDId = 0,
//            menstrualProblemId = 0,
            lastMenstrualPeriod = getDateTimeStringFromLong(genDetails?.lastMenstrualPeriod),
            /**
             * part of reproductive status id mapping on @since Aug 7
             */
            reproductiveStatusId = genDetails?.reproductiveStatusId?.let {
                when (it) {
                    0 -> 0
                    1 -> 1
                    2 -> 2
                    3 -> 4
                    4 -> 5
                    else -> 6
                }
            } ?: 0,
            reproductiveStatus = genDetails?.reproductiveStatusId?.let {
                when (it) {
                    0 -> ""
                    1 -> "Eligible Couple"
                    2 -> "Antenatal Mother"
                    3 -> "Postnatal Mother-Lactating Mother"
                    4 -> "Menopause Stage"
                    else -> "Teenager"
                }
            } ?: "",
//            noOfDaysForDelivery = if (genDetails?.reproductiveStatusId ==2) getNumDaysForDeliveryFromLastMenstrualPeriod(
//                genDetails?.lastMenstrualPeriod
//            ) else null,
//            formStatus = null,
//            formType = null,
//            childRegisteredAWCID = kidDetails?.childRegisteredAWCId ?: 0,
            childRegisteredSchoolID = kidDetails?.childRegisteredSchoolId ?: 0,
            typeOfSchoolId = kidDetails?.typeOfSchoolId ?: 0,
            childRegisteredSchool = kidDetails?.childRegisteredSchool,
            typeofSchool = kidDetails?.typeOfSchool,
//            previousLiveBirth = genDetails?.numPreviousLiveBirth?.toString() ?: "",
//            lastDeliveryConductedID = genDetails?.lastDeliveryConductedId ?: 0,
//            whoConductedDeliveryID = genDetails?.whoConductedDeliveryId ?: 0,
            familyHeadRelation = familyHeadRelation ?: "Other",
            familyHeadRelationPosition = familyHeadRelationPosition,
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            fatherName = fatherName ?: "",
            motherName = motherName ?: "",
            spouseName = genDetails?.spouseName ?: "",
//            whoConductedDelivery = genDetails?.whoConductedDelivery,
//            lastDeliveryConducted = genDetails?.lastDeliveryConducted,

//            facilitySelection = genDetails?.facilityName ?: "",
            serverUpdatedStatus = serverUpdatedStatus,
            createdBy = createdBy!!,
            createdDate = getDateTimeStringFromLong(createdDate!!)!!,
            ncdPriority = ncdPriority,
            guidelineId = guidelineId ?: "0",
            villageName = locationRecord.village.name,
            currSubDistrictId = locationRecord.block.id,
            villageId = locationRecord.village.id,
//            expectedDateOfDelivery = getDateTimeStringFromLong(genDetails?.expectedDateOfDelivery),
            isHrpStatus = isHrpStatus,
//            menstrualStatus = genDetails?.menstrualStatus,
            dateMarriage = genDetails?.marriageDate?.let { getDateTimeStringFromLong(it) },
//            deliveryDate = genDetails?.deliveryDate,
//            suspected_hrp = genDetails?.hrpSuspected.toString(),
//            suspected_ncd = suspectedNcd,
//            suspected_tb = suspectedTb,
//            suspected_ncd_diseases = suspectedNcdDiseases,
//            confirmed_ncd = confirmed_Ncd,
//            confirmed_hrp = confirmedHrp,
//            confirmed_tb = confirmedTb,
//            confirmed_ncd_diseases = confirmedNcdDiseases,
//            diagnosis_status = diagnosisStatus,
//            nishchayPregnancyStatus = nishchayPregnancyStatus ?: "select",
//            nishchayPregnancyStatusPosition = nishchayPregnancyStatusPosition,
//            nishchayDeliveryStatus = nishchayDeliveryStatus ?: "select",
//            nishchayDeliveryStatusPosition = nishchayDeliveryStatusPosition,
//            nayiPahalDeliveryStatusPosition = nayiPahalDeliveryStatusPosition,
//            isImmunizationStatus = immunizationStatus,
            userImage = ImageUtils.getEncodedStringForBenImage(
                context,
                beneficiaryId
            ) ?: "",// Base64.encodeToString(userImageBlob, Base64.DEFAULT),
            benDemographics = BenDemographics(
                communityID = communityId.toString(),
                communityName = community ?: "",
                religionID = religionId.toString(),
                religionName = religion ?: "",
                countryID = 1,
                countryName = "India",
                stateID = locationRecord.state.id,
                stateName = locationRecord.state.name,
                districtID = locationRecord.district.id,
                districtName = locationRecord.district.name,
                blockID = locationRecord.block.id,
                districtBranchID = locationRecord.village.id,
                districtBranchName = locationRecord.village.name,
//            zoneID = user.zoneId,
//            zoneName = user.zoneName,
//            parkingPlaceName = user.parkingPlaceName,
//            parkingPlaceID = user.parkingPlaceId,
//            servicePointID = user.servicePointId.toString(),
//            servicePointName = user.servicePointName,
                addressLine1 = "D.No 3-160E",
                addressLine2 = "ARS Road",
                addressLine3 = "Neggipudi",
            ),
            benPhoneMaps = arrayOf(
                BenPhoneMaps(
                    phoneNo = contactNumber.toString(),
                    createdBy = user.userName,
                )
            ),
            dob = getDateStringFromLong(dob) ?: "",
            gender = gender.toString(),
            genderId = genderId,
            maritalStatusID = if (isKid) null else genDetails?.maritalStatusId?.toString() ?: "",
            maritalStatusName = if (isKid) null else genDetails?.maritalStatus ?: "",
        )
    }

    private fun getNumDaysForDeliveryFromLastMenstrualPeriod(lastMenstrualPeriod: Long?): Int? {
        if (lastMenstrualPeriod == null)
            return null
        val millisCurrent = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = lastMenstrualPeriod
        cal.add(Calendar.WEEK_OF_YEAR, 40)
        return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis - millisCurrent).toInt()
    }

    fun asKidNetworkModel(user: User): BenRegKidNetwork {
        return BenRegKidNetwork(
            benficieryid = beneficiaryId,
            childName = firstName,
            birthPlace = kidDetails!!.birthPlace,
            birthPlaceid = kidDetails!!.birthPlaceId,
            facilityName = kidDetails!!.facilityName,
            facilityid = kidDetails!!.facilityId,
            ashaid = ashaId,
            facilityOther = kidDetails!!.facilityOther,
            placeName = kidDetails!!.placeName,
            conductedDelivery = kidDetails!!.conductedDelivery,
            conductedDeliveryid = kidDetails!!.conductedDeliveryId,
            conductedDeliveryOther = kidDetails!!.conductedDeliveryOther,
            deliveryType = kidDetails!!.deliveryType,
            deliveryTypeid = kidDetails!!.deliveryTypeId,
            deliveryTypeOther = kidDetails!!.deliveryTypeOther,
            complications = kidDetails!!.complications,
            complicationsid = kidDetails!!.complicationsId,
            complicationsOther = kidDetails!!.complicationsOther,
            term = kidDetails!!.term,
            termid = kidDetails!!.termId,
            gestationalAge = kidDetails!!.gestationalAge,
            gestationalAgeid = kidDetails!!.gestationalAgeId,
            corticosteroidGivenMother = kidDetails!!.corticosteroidGivenMother,
            corticosteroidGivenMotherid = kidDetails!!.corticosteroidGivenMotherId,
            criedImmediately = kidDetails!!.criedImmediately,
            criedImmediatelyid = kidDetails!!.criedImmediatelyId,
            birthDefects = kidDetails!!.birthDefects,
            birthDefectsid = kidDetails!!.birthDefectsId,
            birthDefectsOthers = kidDetails!!.birthDefectsOthers,
            heightAtBirth = kidDetails!!.heightAtBirth.toInt(),
            weightAtBirth = kidDetails!!.weightAtBirth.toFloat(),
            feedingStarted = kidDetails!!.feedingStarted,
            feedingStartedid = kidDetails!!.feedingStartedId,
            birthDosage = kidDetails!!.birthDosage,
            birthDosageid = kidDetails!!.birthDosageId,
            birthBCG = kidDetails!!.birthBCG,
            birthOPV = kidDetails!!.birthOPV,
            birthHepB = kidDetails!!.birthHepB,
            motherBenId = kidDetails!!.motherBenId,
            motherName = kidDetails!!.childMotherName,
            motherposition = kidDetails!!.motherPosition,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate),
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate),
            Processed = processed,
            serverUpdatedStatus = serverUpdatedStatus,
            VanID = 4,
            ProviderServiceMapID = user.serviceMapId,
            Countyid = locationRecord.country.id,
            stateid = locationRecord.state.id,
            districtid = locationRecord.district.id,
            villageid = locationRecord.village.id,

            )
    }
}

fun getEddFromLmp(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateLong?.let {
        return dateFormat.format(dateLong + TimeUnit.DAYS.toMillis(280))
    } ?: run {
        return null
    }

}

fun getDateTimeStringFromLong(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    dateLong?.let {
        val dateString = dateFormat.format(dateLong)
        val timeString = timeFormat.format(dateLong)
        return "${dateString}T${timeString}.000Z"
    } ?: run {
        return null
    }

}
/*

data class BenRegNetwork(
    @Json(name = "houseoldId")
    var houseoldId: String,

    @PrimaryKey
    @Json(name = "benficieryid")
    var benficieryid: Long,

    //Benficiery Registration fields
    @Json(name = "user_image")
    var user_image: String? = null,

    @Json(name = "ashaid")
    var ashaid: Int,

    @Suppress("ArrayInDataClass")
    var user_image1: String? = null,

    @Json(name = "registrationDate")
    var registrationDate: String? = null,

    @Json(name = "firstName")
    var firstName: String? = null,

    @Json(name = "lastName")
    var lastName: String? = null,

    @Json(name = "gender")
    var gender: String? = null,

    @Json(name = "genderId")
    var genderId: Int = 0,

    @Json(name = "dob")
    var dob: String? = null,

    @Json(name = "age")
    var age: Int = 0,

    @Json(name = "age_unit")
    var age_unit: String? = null,

    @Json(name = "ageUnitId")
    var ageUnitId: Int = 0,

    @Json(name = "maritalstatus")
    var maritalstatus: String? = null,

    @Json(name = "maritalstatusId")
    var maritalstatusId: Int = 0,

    @Json(name = "spousename")
    var spousename: String? = null,

    @Json(name = "ageAtMarriage")
    var ageAtMarriage: Int = 0,

    @Json(name = "dateMarriage")
    var dateMarriage: String? = null,

    @Json(name = "marriageDate")
    var marriageDate: String? = null,

    @Json(name = "fatherName")
    var fatherName: String? = null,

    @Json(name = "motherName")
    var motherName: String? = null,

    @Json(name = "contact_number")
    var contact_number: String? = null,

    @Json(name = "mobileNoOfRelation")
    var mobileNoOfRelation: String? = null,

    @Json(name = "mobileNoOfRelationId")
    var mobileNoOfRelationId: Int = 0,

    @Json(name = "mobileOthers")
    var mobileOthers: String? = null,

    @Json(name = "literacy")
    var literacy: String? = null,

    @Json(name = "literacyId")
    var literacyId: Int = 0,

    @Json(name = "community")
    var community: String? = null,

    @Json(name = "communityId")
    var communityId: Int = 0,

    @Json(name = "religion")
    var religion: String? = null,

    @Json(name = "religionID")
    var religionID: Int = 0,

    @Json(name = "religionOthers")
    var religionOthers: String? = null,

    @Json(name = "rchid")
    var rchid: String? = null,

    @Json(name = "registrationType")
    var registrationType: String? = null,

    @Json(name = "latitude")
    var latitude: Double = 0.0,

    @Json(name = "longitude")
    var longitude: Double = 0.0,

    //Bank details
    @Json(name = "aadha_no")
    var aadha_no: String? = null,

    @Json(name = "aadha_noId")
    var aadha_noId: Int = 0,

    @Json(name = "aadhaNo")
    var aadhaNo: String? = null,

    @Json(name = "bank_account")
    var bank_account: String? = null,

    @Json(name = "bank_accountId")
    var bank_accountId: Int = 0,

    @Json(name = "bankAccount")
    var bankAccount: String? = null,

    @Json(name = "nameOfBank")
    var nameOfBank: String? = null,

    @Json(name = "nameOfBranch")
    var nameOfBranch: String? = null,

    @Json(name = "ifscCode")
    var ifscCode: String? = null,

    @Json(name = "need_opcare")
    var need_opcare: String? = null,

    @Json(name = "need_opcareId")
    var need_opcareId: Int = 0,

    //Menstral details
    @Json(name = "menstrualStatus")
    var menstrualStatus: String? = null,

    @Json(name = "menstrualStatusId")
    var menstrualStatusId: Int = 0,

    @Json(name = "regularityofMenstrualCycle")
    var regularityofMenstrualCycle: String? = null,

    @Json(name = "regularityofMenstrualCycleId")
    var regularityofMenstrualCycleId: Int = 0,

    @Json(name = "lengthofMenstrualCycle")
    var lengthofMenstrualCycle: String? = null,

    @Json(name = "lengthofMenstrualCycleId")
    var lengthofMenstrualCycleId: Int = 0,

    @Json(name = "menstrualBFD")
    var menstrualBFD: String? = null,

    @Json(name = "menstrualBFDId")
    var menstrualBFDId: Int = 0,

    @Json(name = "menstrualProblem")
    var menstrualProblem: String? = null,

    @Json(name = "menstrualProblemId")
    var menstrualProblemId: Int = 0,

    @Json(name = "lastMenstrualPeriod")
    var lastMenstrualPeriod: String? = null,

    @Json(name = "reproductiveStatus")
    var reproductiveStatus: String? = null,

    @Json(name = "reproductiveStatusId")
    var reproductiveStatusId: Int = 0,

    @Json(name = "formStatus")
    var formStatus: String? = null,

    @Json(name = "formType")
    var formType: String? = null,

    @Json(name = "ANCCount")
    var aNCCount: Int = 0,

    @Json(name = "HRPCount")
    var hRPCount: Int = 0,

    @Json(name = "hrp_suspected")
    var hrp_suspected: Boolean? = null,

    @Json(name = "death_status")
    var isDeath_status: Boolean = false,

    @Json(name = "childRegisteredAWC")
    var childRegisteredAWC: String? = null,

    @Json(name = "childRegisteredAWCID")
    var childRegisteredAWCID: Int = 0,

    @Json(name = "childRegisteredSchool")
    var childRegisteredSchool: String? = null,

    @Json(name = "childRegisteredSchoolID")
    var childRegisteredSchoolID: Int = 0,

    @Json(name = "TypeofSchool")
    var typeofSchool: String? = null,

    @Json(name = "TypeofSchoolID")
    var typeofSchoolID: Int = 0,

    @Json(name = "expectedDateOfDelivery")
    var expectedDateOfDelivery: String? = null,

    @Json(name = "PreviousLiveBirth")
    var previousLiveBirth: String? = null,

    //these are new fields of new borr registartion
    @Json(name = "LastDeliveryConducted")
    var lastDeliveryConducted: String? = null,

    @Json(name = "LastDeliveryConductedID")
    var lastDeliveryConductedID: Int = 0,

    @Json(name = "facilitySelection")
    var facilitySelection: String? = null,

    //    @Json(name = "FacilitySectionID")
    //    private int facilitySectionID;

    @Json(name = "WhoConductedDelivery")
    var whoConductedDelivery: String? = null,

    @Json(name = "WhoConductedDeliveryID")
    var whoConductedDeliveryID: Int = 0,

    //these are new fields of registration for asha login
    @Json(name = "FamilyHeadRelation")
    var familyHeadRelation: String? = null,

    @Json(name = "FamilyHeadRelationPosition")
    var familyHeadRelationPosition: Int = 0,

    */
/*@Json(name = "PreviousLiveBirthID")
      private int PreviousLiveBirthID;*//*

    @Json(name = "ServerUpdatedStatus")
    var serverUpdatedStatus: Int = 0,

    @Json(name = "createdBy")
    var createdBy: String? = null,

    @Json(name = "createdDate")
    var createdDate: String? = null,

    @Json(name = "updatedBy")
    var updatedBy: String? = null,

    @Json(name = "updatedDate")
    var updatedDate: String? = null,

    @Json(name = "ncd_priority")
    var ncd_priority: Int = 0,

    @Json(name = "cbac_available")
    var cbac_available: Boolean = false,

    @Json(name = "guidelineId")
    var guidelineId: String? = null,

    @Json(name = "villagename")
    var villagename: String? = null,

    @Json(name = "deliveryDate")
    var deliveryDate: String? = null,

    @Json(name = "BenRegId")
    var benRegId: Int = 0,

    @Json(name = "ProviderServiceMapID")
    var providerServiceMapID: Int = 0,

    @Json(name = "VanID")
    var vanID: Int = 0,

    @Json(name = "Processed")
    var processed: String? = null,

    @Json(name = "Countyid")
    var countyid: Int = 0,

    @Json(name = "stateid")
    var stateid: Int = 0,

    @Json(name = "districtid")
    var districtid: Int = 0,

    @Json(name = "districtname")
    var districtname: String? = null,

    @Json(name = "currSubDistrictId")
    var currSubDistrictId: Int = 0,

    @Json(name = "villageid")
    var villageid: Int = 0,

    @Json(name = "childname")
    var childname: String? = null,

    @Json(name = "childBenId")
    var childBenId: Int = 0,

    @Json(name = "childpos")
    var childpos: Int = 0,

    @Json(name = "motherBenId")
    var motherBenId: Int = 0,

    @Json(name = "hrpStatus")
    var isHrpStatus: Boolean = false,

    //    @Json(name = "relatedBeneficiaryIds")
    //    List<RelatedBenIds> relatedBeneficiaryIds;

    @Json(name = "hrp_identification_date")
    var hrp_identification_date: String? = null,

    @Json(name = "hrp_last_vist_date")
    var hrp_last_vist_date: String? = null,

    @Json(name = "lastHrpVisitDate")
    var lastHrpVisitDate: String? = null,

    @Json(name = "nishchayPregnancyStatus")
    var nishchayPregnancyStatus: String? = null,

    @Json(name = "nishchayPregnancyStatusPosition")
    var nishchayPregnancyStatusPosition: Int = 0,

    @Json(name = "nishchayDeliveryStatus")
    var nishchayDeliveryStatus: String? = null,

    @Json(name = "nishchayDeliveryStatusPosition")
    var nishchayDeliveryStatusPosition: Int = 0,

    @Json(name = "nayiPahalDeliveryStatus")
    var nayiPahalDeliveryStatus: String? = null,

    @Json(name = "nayiPahalDeliveryStatusPosition")
    var nayiPahalDeliveryStatusPosition: Int = 0,

    @Json(name = "suspected_hrp")
    var suspected_hrp: String? = null,

    @Json(name = "suspected_ncd")
    var suspected_ncd: String? = null,

    @Json(name = "suspected_tb")
    var suspected_tb: String? = null,

    @Json(name = "suspected_ncd_diseases")
    var suspected_ncd_diseases: String? = null,

    @Json(name = "confirmed_ncd")
    var confirmed_ncd: String? = null,

    @Json(name = "confirmed_hrp")
    var confirmed_hrp: String? = null,

    @Json(name = "confirmed_tb")
    var confirmed_tb: String? = null,

    @Json(name = "confirmed_ncd_diseases")
    var confirmed_ncd_diseases: String? = null,

    @Json(name = "diagnosis_status")
    var diagnosis_status: String? = null,

    @Json(name = "facilityOther")
    var facilityOther: String? = null,

    @Json(name = "noOfDaysForDelivery")
    var noOfDaysForDelivery: Int? = null,

    //    public String getFacility_other() {
    @Json(name = "FamilyHeadRelationOther")
    var familyHeadRelationOther: String? = null,

    @Json(name = "immunizationStatus")
    var isImmunizationStatus: Boolean = false
)
fun asCacheModel(benRegNetwork: BenRegNetwork, newBornRegNetwork: NewBornRegNetwork?): BenRegCache {

    benRegNetwork.apply {
       val ben = BenRegCache(
           householdId = houseoldId.toLong(),
           beneficiaryId = benficieryid,
           ashaId = ashaid,
           age = age,
           ageUnit = when (age_unit) {
               "Year(s)" -> AgeUnit.YEARS
               "Month(s)" -> AgeUnit.MONTHS
               "Day(s)" -> AgeUnit.DAYS
               else -> AgeUnit.YEARS
           },
           isKid = !(age_unit == "Year(s)" && age > 14),
           isAdult = (age_unit == "Year(s)" && age > 14),
           userImage = user_image,
           userImageBlob = user_image1!!.toByteArray(),
           regDate = getLongFromDate(registrationDate),
           firstName = firstName,
           lastName = lastName,
           gender = when (gender) {
               "Male" -> Gender.MALE
               "Female" -> Gender.FEMALE
               "Transgender" -> Gender.TRANSGENDER
               else -> Gender.MALE
           },
           genderId = genderId,
           dob = getLongFromDate(dob),
           ageUnitId = ageUnitId,
           fatherName = fatherName,
           motherName = motherName,
           familyHeadRelation = familyHeadRelation,
           familyHeadRelationPosition = familyHeadRelationPosition,
           familyHeadRelationOther = familyHeadRelationOther,
           mobileNoOfRelation = mobileNoOfRelation,
           mobileNoOfRelationId = mobileNoOfRelationId,
           mobileOthers = mobileOthers,
           contactNumber = contact_number?.toLong() ?:0L,
           literacy = literacy,
           literacyId = literacyId,
           community = community,
           communityId = communityId,
           religion = religion,
           religionId = religionID,
           religionOthers = religionOthers,
           rchId = rchid,
           registrationType = when(registrationType) {
               "Infant" -> TypeOfList.INFANT
               "Child" -> TypeOfList.CHILD
               "Adolescent" -> TypeOfList.ADOLESCENT
               "General" -> TypeOfList.GENERAL
               "Eligible Couple" -> TypeOfList.ELIGIBLE_COUPLE
               "Antenatal Mother" -> TypeOfList.ANTENATAL_MOTHER
               "Delivery Stage" -> TypeOfList.DELIVERY_STAGE
               "Postnatal Mother" -> TypeOfList.POSTNATAL_MOTHER
               "Menopause" -> TypeOfList.MENOPAUSE
               "Teenager" -> TypeOfList.TEENAGER
               else -> TypeOfList.OTHER
           },
           latitude = latitude,
           longitude = longitude,
           aadharNum = aadhaNo,
           aadharNumId = aadha_noId,
           hasAadhar = (aadhaNo != null),
           hasAadharId = aadha_noId,
           bankAccountId = bank_accountId,
           bankAccount = bankAccount,
           nameOfBank = nameOfBank,
           nameOfBranch = nameOfBranch,
           ifscCode = ifscCode,
           needOpCare = need_opcare,
           needOpCareId = need_opcareId,
           ncdPriority = ncd_priority,
           cbacAvailable = cbac_available,
           guidelineId = guidelineId,
           isHrpStatus = isHrpStatus,
           hrpIdentificationDate = hrp_identification_date,
           hrpLastVisitDate = hrp_last_vist_date,
           nishchayPregnancyStatus = nishchayPregnancyStatus,
           nishchayPregnancyStatusPosition = nishchayPregnancyStatusPosition,
           nishchayDeliveryStatus = nishchayDeliveryStatus,
           nishchayDeliveryStatusPosition = nishchayDeliveryStatusPosition,
           nayiPahalDeliveryStatus = nayiPahalDeliveryStatus,
           nayiPahalDeliveryStatusPosition = nayiPahalDeliveryStatusPosition,
           suspectedNcd = suspected_ncd,
           suspectedNcdDiseases = suspected_ncd_diseases,
           suspectedTb = suspected_tb,
           confirmed_Ncd = confirmed_ncd,
           confirmedHrp = confirmed_hrp,
           confirmedTb = confirmed_tb,
           confirmedNcdDiseases = confirmed_ncd_diseases,
           diagnosisStatus = diagnosis_status,
           countryId = countyid,
           stateId = stateid,
           districtId = districtid,
           districtName = districtname,
           currSubDistrictId = currSubDistrictId,
           villageId = villageid,
           villageName = villagename,
           processed = processed,
           serverUpdatedStatus = serverUpdatedStatus,
           createdBy = createdBy,
           createdDate = getLongFromDate(updatedDate),
           kidDetails = BenRegKid(
               childRegisteredAWC = childRegisteredAWC,
               childRegisteredAWCId = childRegisteredAWCID,
               childRegisteredSchool = childRegisteredSchool,
               childRegisteredSchoolId = childRegisteredSchoolID,
               typeOfSchool = typeofSchool,
               typeOfSchoolId = typeofSchoolID
               ),
           genDetails = BenRegGen(
               maritalStatus = maritalstatus,
               maritalStatusId = maritalstatusId,
               spouseName = spousename,
               ageAtMarriage = ageAtMarriage,
               dateOfMarriage = getLongFromDate(dateMarriage),
               marriageDate = marriageDate,
               menstrualStatus = menstrualStatus,
               menstrualStatusId = menstrualStatusId,
               regularityOfMenstrualCycle = regularityofMenstrualCycle,
               regularityOfMenstrualCycleId = regularityofMenstrualCycleId,
               lengthOfMenstrualCycle = lengthofMenstrualCycle,
               lengthOfMenstrualCycleId = lengthofMenstrualCycleId,
               menstrualBFD = menstrualBFD,
               menstrualBFDId = menstrualBFDId,
               menstrualProblem = menstrualProblem,
               menstrualProblemId = menstrualProblemId,
               lastMenstrualPeriod = lastMenstrualPeriod,
               reproductiveStatus = reproductiveStatus,
               reproductiveStatusId = reproductiveStatusId,
               lastDeliveryConducted = lastDeliveryConducted,
               lastDeliveryConductedId = lastDeliveryConductedID,
               facilityName = facilitySelection,
               whoConductedDelivery = whoConductedDelivery,
               whoConductedDeliveryId = whoConductedDeliveryID,
               deliveryDate = deliveryDate,
               expectedDateOfDelivery = expectedDateOfDelivery,
               noOfDaysForDelivery = noOfDaysForDelivery,
               ),
           syncState = SyncState.UNSYNCED,
           isDraft = false
       )
        newBornRegNetwork?.let {
            ben.kidDetails?.childName = it.childName
            ben.kidDetails?.birthPlace = it.birthPlace
            ben.kidDetails?.birthPlaceId = it.birthPlaceid.toString()
            ben.kidDetails?.facilityName = it.facilityName
            ben.kidDetails?.facilityid = it.facilityid.toString()
            ben.kidDetails?.facilityOther = it.facilityOther
            ben.kidDetails?.placeName = it.placeName
            ben.kidDetails?.conductedDelivery = it.conductedDelivery
            ben.kidDetails?.conductedDeliveryId = it.conductedDeliveryid.toString()
            ben.kidDetails?.conductedDeliveryOther = it.conductedDeliveryOther
            ben.kidDetails?.deliveryType = it.deliveryType
            ben.kidDetails?.deliveryTypeId = it.deliveryTypeid.toString()
            ben.kidDetails?.complications = it.complecations
            ben.kidDetails?.complicationsId = it.complecationsid.toString()
            ben.kidDetails?.complicationsOther = it.complicationsOther
            ben.kidDetails?.term = it.term
            ben.kidDetails?.termid = it.termid.toString()
            ben.kidDetails?.gestationalAge = it.gestationalAge
            ben.kidDetails?.gestationalAgeId = it.gestationalAgeid.toString()
            ben.kidDetails?.corticosteroidGivenMother = it.corticosteroidGivenMother
            ben.kidDetails?.corticosteroidGivenMotherId = it.corticosteroidGivenMotherid.toString()
            ben.kidDetails?.criedImmediately = it.criedImmediately
            ben.kidDetails?.criedImmediatelyId = it.criedImmediatelyid.toString()
            ben.kidDetails?.birthDefects = it.birthDefects
            ben.kidDetails?.birthDefectsId = it.birthDefectsid.toString()
            ben.kidDetails?.heightAtBirth = it.heightAtBirth.toString()
            ben.kidDetails?.weightAtBirth = it.weightAtBirth.toString()
            ben.kidDetails?.feedingStarted = it.feedingStarted
            ben.kidDetails?.feedingStartedId = it.feedingStartedid.toString()
            ben.kidDetails?.birthDosage = it.birthDosage
            ben.kidDetails?.birthDosageId = it.birthDosageid.toString()
            ben.kidDetails?.opvBatchNo = it.opvBatchNo
            ben.kidDetails?.opvGivenDueDate = it.opvGivenDueDate
            ben.kidDetails?.opvDate = it.opvDate
            ben.kidDetails?.bcdBatchNo = it.bcdBatchNo
            ben.kidDetails?.bcgGivenDueDate = it.bcgGivenDueDate
            ben.kidDetails?.bcgDate = it.bcgDate
            ben.kidDetails?.hptBatchNo = it.hptdBatchNo
            ben.kidDetails?.hptGivenDueDate = it.hptGivenDueDate
            ben.kidDetails?.hptDate = it.hptDate
            ben.kidDetails?.vitaminKBatchNo = it.vitaminkBatchNo
            ben.kidDetails?.vitaminKGivenDueDate = it.vitaminkGivenDueDate
            ben.kidDetails?.vitaminKDate = it.vitaminkDate
            ben.kidDetails?.deliveryTypeOther = it.deliveryTypeOther
            ben.kidDetails?.motherBenId = it.motherBenId.toString()
            ben.kidDetails?.childMotherName = it.motherName
            ben.kidDetails?.motherPosition = it.motherposition.toString()
            ben.kidDetails?.birthBCG = it.birthBCG
            ben.kidDetails?.birthHepB = it.birthHepB
            ben.kidDetails?.birthOPV = it.birthOPV
        }
        return ben
   }

}

*/

