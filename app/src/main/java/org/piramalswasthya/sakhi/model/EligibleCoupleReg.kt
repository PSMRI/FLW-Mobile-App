package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.Json
import org.piramalswasthya.sakhi.configuration.Dataset.Companion.getDateFromLong
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.utils.HelperUtil.getDateStringFromLong

@Entity(
    tableName = "ELIGIBLE_COUPLE_REG",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ecrInd", value = ["benId"/* "hhId"*/])]
)

data class EligibleCoupleRegCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
//    val hhId: Long,
    var dateOfReg: Long = 0L,
    var bankAccount: Long? = null,
    var bankName: String? = null,
    var branchName: String? = null,
    var ifsc: String? = null,
    var lmpDate: Long = 0L,
    var noOfChildren: Int = 0,
    var noOfLiveChildren: Int = 0,
    var noOfMaleChildren: Int = 0,
    var noOfFemaleChildren: Int = 0,
    var isRegistered: Boolean = true,
    var dob1: Long? = null,
    var age1: Int? = null,
    var gender1: Gender? = null,
    var marriageFirstChildGap: Int? = null,
    var dob2: Long? = null,
    var age2: Int? = null,
    var gender2: Gender? = null,
    var firstAndSecondChildGap: Int? = null,
    var dob3: Long? = null,
    var age3: Int? = null,
    var gender3: Gender? = null,
    var secondAndThirdChildGap: Int? = null,
    var dob4: Long? = null,
    var age4: Int? = null,
    var gender4: Gender? = null,
    var thirdAndFourthChildGap: Int? = null,
    var dob5: Long? = null,
    var age5: Int? = null,
    var gender5: Gender? = null,
    var fourthAndFifthChildGap: Int? = null,
    var dob6: Long? = null,
    var age6: Int? = null,
    var gender6: Gender? = null,
    var fifthANdSixthChildGap: Int? = null,
    var dob7: Long? = null,
    var age7: Int? = null,
    var gender7: Gender? = null,
    var sixthAndSeventhChildGap: Int? = null,
    var dob8: Long? = null,
    var age8: Int? = null,
    var gender8: Gender? = null,
    var seventhAndEighthChildGap: Int? = null,
    var dob9: Long? = null,
    var age9: Int? = null,
    var gender9: Gender? = null,
    var eighthAndNinthChildGap: Int? = null,
    var processed: String? = "N",
    var createdBy: String,
    var createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun asPostModel(): EcrPost {
        return EcrPost(
            benId = benId,
            dateOfReg = getDateStringFromLong(dateOfReg)!!,
            bankAccount = bankAccount,
            bankName = bankName,
            branchName = branchName,
            ifsc = ifsc,
            lmpDate = getDateStringFromLong(lmpDate)!!,
            numChildren = noOfChildren,
            numLiveChildren = noOfLiveChildren,
            numMaleChildren = noOfMaleChildren,
            numFemaleChildren = noOfFemaleChildren,
            dob1 = getDateStringFromLong(dob1),
            age1 = age1,
            gender1 = gender1,
            marriageFirstChildGap = marriageFirstChildGap,
            dob2 = getDateStringFromLong(dob2),
            age2 = age2,
            gender2 = gender2,
            firstAndSecondChildGap = firstAndSecondChildGap,
            dob3 = getDateStringFromLong(dob3),
            age3 = age3,
            gender3 = gender3,
            secondAndThirdChildGap = secondAndThirdChildGap,
            dob4 = getDateStringFromLong(dob4),
            age4 = age4,
            gender4 = gender4,
            thirdAndFourthChildGap = thirdAndFourthChildGap,
            dob5 = getDateStringFromLong(dob5),
            age5 = age5,
            gender5 = gender5,
            fourthAndFifthChildGap = fourthAndFifthChildGap,
            dob6 = getDateStringFromLong(dob6),
            age6 = age6,
            gender6 = gender6,
            fifthANdSixthChildGap = fifthANdSixthChildGap,
            dob7 = getDateStringFromLong(dob7),
            age7 = age7,
            gender7 = gender7,
            sixthAndSeventhChildGap = sixthAndSeventhChildGap,
            dob8 = getDateStringFromLong(dob8),
            age8 = age8,
            gender8 = gender8,
            seventhAndEighthChildGap = seventhAndEighthChildGap,
            dob9 = getDateStringFromLong(dob9),
            age9 = age9,
            gender9 = gender9,
            eighthAndNinthChildGap = eighthAndNinthChildGap,
            createdBy = createdBy,
            createdDate = getDateStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateStringFromLong(updatedDate)!!
        )
    }
}


data class BenWithECRCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val ecr: EligibleCoupleRegCache?,

    ) {
    fun asDomainModel(): BenWithEcrDomain {
        return BenWithEcrDomain(
            ben = ben.asBasicDomainModel(),
            ecr = ecr
        )
    }
}

data class BenWithEcrDomain(
//    val benId: Long,
    val ben: BenBasicDomain,
    val ecr: EligibleCoupleRegCache?
)

data class EcrPost(
    val benId: Long,
    @Json(name = "registrationDate")
    val dateOfReg: String? = null,
    @Json(name = "bankAccountNumber")
    val bankAccount: Long? = null,
    val bankName: String? = null,
    val branchName: String? = null,
    val ifsc: String? = null,
    val numChildren: Int? = null,
    val numLiveChildren: Int? = null,
    val numMaleChildren: Int? = null,
    val numFemaleChildren: Int? = null,
    val dob1: String? = null,
    val age1: Int? = null,
    val gender1: Gender? = null,
    val lmpDate: String? = null,
    val marriageFirstChildGap: Int? = null,
    val dob2: String? = null,
    val age2: Int? = null,
    val gender2: Gender? = null,
    val firstAndSecondChildGap: Int? = null,
    val dob3: String? = null,
    val age3: Int? = null,
    val gender3: Gender? = null,
    val secondAndThirdChildGap: Int? = null,
    val dob4: String? = null,
    val age4: Int? = null,
    val gender4: Gender? = null,
    val thirdAndFourthChildGap: Int? = null,
    val dob5: String? = null,
    val age5: Int? = null,
    val gender5: Gender? = null,
    val fourthAndFifthChildGap: Int? = null,
    val dob6: String? = null,
    val age6: Int? = null,
    val gender6: Gender? = null,
    val fifthANdSixthChildGap: Int? = null,
    val dob7: String? = null,
    val age7: Int? = null,
    val gender7: Gender? = null,
    val sixthAndSeventhChildGap: Int? = null,
    val dob8: String? = null,
    val age8: Int? = null,
    val gender8: Gender? = null,
    val seventhAndEighthChildGap: Int? = null,
    val dob9: String? = null,
    val age9: Int? = null,
    val gender9: Gender? = null,
    val eighthAndNinthChildGap: Int? = null,
    var misCarriage: String? = null,
    var homeDelivery: String? = null,
    var medicalIssues: String? = null,
    var pastCSection: String? = null,
    var isHighRisk: Boolean = false,
    var isRegistered: Boolean = true,
    var createdBy: String,
    val createdDate: String,
    var updatedBy: String,
    val updatedDate: String,
)