package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "PMJAY",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_pmjay", value = ["benId"/* "hhId"*/])]
)

data class PMJAYCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    val hhId: Long,
    var registrationDate: Long? = System.currentTimeMillis(),
    var registeredHospital: String? = null,
    var contactNumber: Long? = 0,
    var communicationContactNumber: Long? = 0,
    var patientAddress: String? = null,
    var communicationAddress: String? = null,
    var hospitalAddress: String? = null,
    var familyId: Long? = 0,
    var isAadhaarBeneficiary: Long? = null,
    var memberType: String? = null,
    var patientType: String? = null,
    var scheme: String? = null,
    var createdBy: String? = null,
    var createdDate: Long? = System.currentTimeMillis(),
    var processed: String? = null,
) {
    fun asPostModel(
        user: User,
        household: HouseholdCache,
        ben: BenRegCache,
        pmjayCount: Int
    ): PMJAYPost {
        return PMJAYPost(
            age = ben.age.toString(),
            beneficiaryid = benId,
            communicationAddress = communicationAddress,
            communicationContactNumber = communicationContactNumber.toString(),
            contactNumber = contactNumber.toString(),
            createdBy = createdBy,
            houseoldId = hhId.toString(),
            villageid = household.locationRecord.village.id,
            createdDate = createdDate,
            familyId = familyId.toString(),
            gender = when (ben.gender) {
                Gender.FEMALE -> "Female"
                Gender.MALE -> "Male"
                Gender.TRANSGENDER -> "Transgender"
                else -> "Other"
            },
            hospitalAddress = hospitalAddress,
            idNumber = id.toString(),
            isAadhaarBeneficiary = isAadhaarBeneficiary.toString(),
            isEditable = true,
            loginId = pmjayCount,
            memberType = memberType,
            modifiedDate = System.currentTimeMillis(),
            name = user.userName,
            patientAddress = patientAddress,
            patientType = patientType,
            registered_hospital = registeredHospital,
            registrationDate = getDateTimeStringFromLong(registrationDate),
            scheme = scheme,
            updatedBy = user.userName,
            updatedDate = System.currentTimeMillis(),
        )
    }
}

@JsonClass(generateAdapter = true)
data class PMJAYPost(
    val age: String? = null,
    val beneficiaryid: Long,
    val bioMetricVerfied: String? = null,
    val communicationAddress: String? = null,
    val communicationContactNumber: String? = null,
    val contactNumber: String? = null,
    val createdBy: String? = null,
    val createdDate: Long? = System.currentTimeMillis(),
    val familyId: String? = null,
    val gender: String? = null,
    val hospitalAddress: String? = null,
    val houseoldId: String? = null,
    val idNumber: String? = null,
    val isAadhaarBeneficiary: String? = null,
    val isEditable: Boolean? = null,
    val loginId: Int? = 0,
    val logonCoordinates: Int? = 0,
    val memberType: String? = null,
    val modifiedDate: Long? = System.currentTimeMillis(),
    val name: String? = null,
    val patientAddress: String? = null,
    val patientType: String? = null,
    val registered_hospital: String? = null,
    val registrationDate: String? = null,
    val scheme: String? = null,
    val updatedBy: String? = null,
    val updatedDate: Long? = System.currentTimeMillis(),
    val userimage: String? = "",
    val villageid: Int? = 0
)