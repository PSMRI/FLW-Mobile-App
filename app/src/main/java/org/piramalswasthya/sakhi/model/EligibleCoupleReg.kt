package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "ELIGIBLE_COUPLE_REG",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId",/* "householdId"*/),
        childColumns = arrayOf("benId", /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ecrInd", value = ["benId",/* "hhId"*/])])

data class EligibleCoupleRegCache (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
//    val hhId: Long,
    var dateOfReg: Long = 0L,
    var rchId: Long? = 0L,
    var name: String? = null,
    var husbandName: String? = null,
    var age: Int? = 0,
    var ageAtMarriage: Int? = 0,
    var aadharNo: Long? = 0L,
    var bankAccount: Long? = null,
    var bankName: String? = null,
    var branchName: String? = null,
    var ifsc: String? = null,
    var noOfChildren: Int? = 0,
    var noOfLiveChildren: Int? = 0,
    var noOfMaleChildren: Int? = 0,
    var noOfFemaleChildren: Int? = 0,
    var dob1: Long? = 0L,
    var age1: Int? = 0,
    var gender1: Gender? = null,
    var marriageFirstChildGap: Int? = 0,
    var dob2: Long? = 0L,
    var age2: Int? = 0,
    var gender2: Gender? = null,
    var firstAndSecondChildGap: Int? = 0,
    var dob3: Long? = 0L,
    var age3: Int? = 0,
    var gender3: Gender? = null,
    var secondAndThirdChildGap: Int? = 0,
    var dob4: Long? = 0L,
    var age4: Int? = 0,
    var gender4: Gender? = null,
    var thirdAndFourthChildGap: Int? = 0,
    var dob5: Long? = 0L,
    var age5: Int? = 0,
    var gender5: Gender? = null,
    var fourthAndFifthChildGap: Int? = 0,
    var dob6: Long? = 0L,
    var age6: Int? = 0,
    var gender6: Gender? = null,
    var fifthANdSixthChildGap: Int? = 0,
    var dob7: Long? = 0L,
    var age7: Int? = 0,
    var gender7: Gender? = null,
    var sixthAndSeventhChildGap: Int? = 0,
    var dob8: Long? = 0L,
    var age8: Int? = 0,
    var gender8: Gender? = null,
    var seventhAndEighthChildGap: Int? = 0,
    var dob9: Long? = 0L,
    var age9: Int? = 0,
    var gender9: Gender? = null,
    var eighthAndNinthChildGap: Int? = 0,
    var processed: String? = "N",
    var syncState: SyncState
): FormDataModel {
    fun asPostModel(): EcrPost {
        return EcrPost(
            benId = benId,
            dateOfReg = getDtStringFromLong(dateOfReg),
            rchId = rchId,
            name = name,
            husbandName = husbandName,
            age = age,
            ageAtMarriage = ageAtMarriage,
            aadharNo = aadharNo,
            bankAccount = bankAccount,
            bankName = bankName,
            branchName = branchName,
            ifsc = ifsc,
            noOfChildren = noOfChildren,
            noOfLiveChildren = noOfLiveChildren,
            noOfMaleChildren = noOfMaleChildren,
            noOfFemaleChildren = noOfFemaleChildren,
            dob1 = getDtStringFromLong(dob1),
            age1 = age1,
            gender1 = gender1,
            marriageFirstChildGap = marriageFirstChildGap,
            dob2 = getDtStringFromLong(dob2),
            age2 = age2,
            gender2 = gender2,
            firstAndSecondChildGap = firstAndSecondChildGap,
            dob3 = getDtStringFromLong(dob3),
            age3 = age3,
            gender3 = gender3,
            secondAndThirdChildGap = secondAndThirdChildGap,
            dob4 = getDtStringFromLong(dob4),
            age4 = age4,
            gender4 = gender4,
            thirdAndFourthChildGap = thirdAndFourthChildGap,
            dob5 = getDtStringFromLong(dob5),
            age5 = age5,
            gender5 = gender5,
            fourthAndFifthChildGap = fourthAndFifthChildGap,
            dob6 = getDtStringFromLong(dob6),
            age6 = age6,
            gender6 = gender6,
            fifthANdSixthChildGap = fifthANdSixthChildGap,
            dob7 = getDtStringFromLong(dob7),
            age7 = age7,
            gender7 = gender7,
            sixthAndSeventhChildGap = sixthAndSeventhChildGap,
            dob8 = getDtStringFromLong(dob8),
            age8 = age8,
            gender8 = gender8,
            seventhAndEighthChildGap = seventhAndEighthChildGap,
            dob9 = getDtStringFromLong(dob9),
            age9 = age9,
            gender9 = gender9,
            eighthAndNinthChildGap = eighthAndNinthChildGap
        )
    }
}

private fun getDtStringFromLong(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateLong?.let {
        val dateString = dateFormat.format(dateLong)
        return dateString
    } ?: run {
        return null
    }

}

data class EcrPost(
    val id: Int = 0,
    val benId: Long = 0,
    val dateOfReg: String? = null,
    val rchId: Long? = 0L,
    val name: String? = null,
    val husbandName: String? = null,
    val age: Int? = 0,
    val ageAtMarriage: Int? = 0,
    val aadharNo: Long? = 0L,
    val bankAccount: Long? = null,
    val bankName: String? = null,
    val branchName: String? = null,
    val ifsc: String? = null,
    val noOfChildren: Int? = 0,
    val noOfLiveChildren: Int? = 0,
    val noOfMaleChildren: Int? = 0,
    val noOfFemaleChildren: Int? = 0,
    val dob1: String? = null,
    val age1: Int? = 0,
    val gender1: Gender? = null,
    val marriageFirstChildGap: Int? = 0,
    val dob2: String? = null,
    val age2: Int? = 0,
    val gender2: Gender? = null,
    val firstAndSecondChildGap: Int? = 0,
    val dob3: String? = null,
    val age3: Int? = 0,
    val gender3: Gender? = null,
    val secondAndThirdChildGap: Int? = 0,
    val dob4: String? = null,
    val age4: Int? = 0,
    val gender4: Gender? = null,
    val thirdAndFourthChildGap: Int? = 0,
    val dob5: String? = null,
    val age5: Int? = 0,
    val gender5: Gender? = null,
    val fourthAndFifthChildGap: Int? = 0,
    val dob6: String? = null,
    val age6: Int? = 0,
    val gender6: Gender? = null,
    val fifthANdSixthChildGap: Int? = 0,
    val dob7: String? = null,
    val age7: Int? = 0,
    val gender7: Gender? = null,
    val sixthAndSeventhChildGap: Int? = 0,
    val dob8: String? = null,
    val age8: Int? = 0,
    val gender8: Gender? = null,
    val seventhAndEighthChildGap: Int? = 0,
    val dob9: String? = null,
    val age9: Int? = 0,
    val gender9: Gender? = null,
    val eighthAndNinthChildGap: Int? = 0
)