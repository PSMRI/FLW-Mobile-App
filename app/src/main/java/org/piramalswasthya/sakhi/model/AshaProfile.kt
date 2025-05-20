package org.piramalswasthya.sakhi.model

import android.content.Context
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.helpers.ImageUtils


@Entity(tableName = "PROFILE_ACTIVITY")
data class ProfileActivityCache(
    @PrimaryKey
    val id: Long,
    var name: String?="",
    var profileImage: String? = null,
    var village:String?="",
    val employeeId:Int?=0,
    var dob:String?="",
    var age:Int?=0,
    var mobileNumber:String?=null,
    var alternateMobileNumber:String?=null,
    var fatherOrSpouseName:String?=null,
    var dateOfJoining: String?=null,
    var bankAccount: String?=null,
    var ifsc: String?=null,
    var populationCovered: Int?=0,
    var choName: String?=null,
    var choMobile: String?=null,
    var awwName: String?=null,
    var awwMobile: String?=null,
    var anm1Name: String?=null,
    var anm1Mobile: String?=null,
    var anm2Name: String?=null,
    var anm2Mobile: String?=null,
    var abhaNumber: String?=null,
    var ashaHouseholdRegistration: String?=null,
    var ashaFamilyMember: String?=null,
    var providerServiceMapID: String?=null,
    var isFatherOrSpouse: Boolean?=false,
    var supervisorName: String?="",
    var supervisorMobile: String?="",
    )

@JsonClass(generateAdapter = true)
data class ProfileActivityNetwork(
    val id: Long=0L,
    val name: String?=null,
    var profileImage: String? = null,
    val village:String?=null,
    val employeeId:Int?=0,
    val dob:String?=null,
    val age:Int?=0,
    val mobileNumber:String?=null,
    val alternateMobileNumber:String?=null,
    val fatherOrSpouseName:String?=null,
    val dateOfJoining: String?=null,
    val bankAccount: String?=null,
    val ifsc: String?=null,
    val populationCovered: Int?=0,
    val choName: String?=null,
    val choMobile: String?=null,
    val awwName: String?=null,
    val awwMobile: String?=null,
    val anm1Name: String?=null,
    val anm1Mobile: String?=null,
    val anm2Name: String?=null,
    val anm2Mobile: String?=null,
    val abhaNumber: String?=null,
    val ashaHouseholdRegistration: String?=null,
    val ashaFamilyMember: String?=null,
    val providerServiceMapID: String?=null,
    val isFatherOrSpouse: Boolean?=null,
    var supervisorName: String?="",
    var supervisorMobile: String?="",
) {
    fun asCacheModel(): ProfileActivityCache {
        return ProfileActivityCache(
            id = id,
            name = name,
            profileImage = profileImage,
            village = village,
            employeeId = employeeId,
            dob = dob,
            age = age,
            mobileNumber = mobileNumber,
            alternateMobileNumber = alternateMobileNumber,
            fatherOrSpouseName = fatherOrSpouseName,
            dateOfJoining = dateOfJoining,
            bankAccount = bankAccount,
            ifsc = ifsc,
            populationCovered = populationCovered,
            choName = choName,
            choMobile = choMobile,
            awwName = awwName,
            awwMobile = awwMobile,
            anm1Name = anm1Name,
            anm1Mobile = anm1Mobile,
            anm2Name = anm2Name,
            anm2Mobile = anm2Mobile,
            abhaNumber = abhaNumber,
            ashaHouseholdRegistration = ashaHouseholdRegistration,
            ashaFamilyMember = ashaFamilyMember,
            providerServiceMapID = providerServiceMapID,
            isFatherOrSpouse = isFatherOrSpouse,
            supervisorName = supervisorName,
            supervisorMobile = supervisorMobile,

        )
    }
}


@JsonClass(generateAdapter = true)
data class ProfileActivityListResponse(
    val data: ProfileActivityNetwork,
    val statusCode: Int,
    val status: String
)



data class ProfileCache(
    @Embedded
    val activity: ProfileActivityCache,
) {
    fun asDomainModel(): ProfileDomain {
        return ProfileDomain(
            activity,
        )
    }
}

data class ProfileDomain(
    val activity: ProfileActivityCache,
)

data class ProfileDomainDTO(
    val id: Long,
    val name: String,
    var profileImage: String? = null,
    val village:String,
    val employeeId:Int,
    val dob:String,
    val age:Int,
    val mobileNumber:String,
    val alternateMobileNumber:String,
    val fatherOrSpouseName:String,
    val dateOfJoining: String,
    val bankAccount: String,
    val ifsc: String,
    val populationCovered: Int,
    val choName: String,
    val choMobile: String,
    val awwName: String,
    val awwMobile: String,
    val anm1Name: String,
    val anm1Mobile: String,
    val anm2Name: String,
    val anm2Mobile: String,
    val abhaNumber: String,
    val ashaHouseholdRegistration: String,
    val ashaFamilyMember: String,
    val providerServiceMapID: String,
    val isFatherOrSpouse: Boolean,
    val supervisorName: String,
    val supervisorMobile: String
)



