package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "FPOT",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId" /*"householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_fpot", value = ["benId"/* "hhId"*/])]
)

data class FPOTCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    val hhId: Long,
    var monthlySerialNumber: String? = null,
    var annualSerialNumber: String? = null,
    var spouseName: String? = null,
    var category: String? = null,
    var benAddress: String? = null,
    var contactNumber: String? = null,
    var educationalQualification: String? = null,
    var numChildren: String? = null,
    var youngestChildAge: String? = null,
    var sterilization: Boolean? = null,
    var mrCheckListFilled: Boolean? = null,
    var dateOfOperation: Long = 0,
    var femaleSterilization: String? = null,
    var secondFollowUpExpectedDate: Long = 0,
    var followUpActualDate: Long = 0,
    var followUpDetails: String? = null,
    var secondPostFollowUpCounselling: String? = null,
    var thirdFollowUpExpectedDate: Long = 0,
    var menstruationStarted: Boolean? = null,
    var spermatozoaFoundInSemen: String? = null,
    var thirdPostFollowUpCounselling: String? = null,
    var sterilizationOrVasectomyIssueDate: Long = 0,
    var notIssuedReason: String? = null,
    var sterilizationOrVasectomyDocSubmitted: String? = null,
    var remarks: String? = null,
    var createdBy: String? = null,
    var createdDate: Long? = System.currentTimeMillis(),
    var processed: String? = null,
)

@JsonClass(generateAdapter = true)
data class FPOTPost(
    val abortionPost: String? = null,
    val ageOfYoung: String? = null,
    val annualSerialNumber: String? = null,
    val beneficiaryAddress: String? = null,
    val beneficiaryAge: String? = null,
    val beneficiaryName: String? = null,
    val beneficiaryid: Long,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val educationalQualification: String? = null,
    val exceptlaproscopy: String? = null,
    val grade: String? = null,
    val houseoldId: String? = null,
    val husbandWifeName: String? = null,
    val inTheDateOperation: String? = null,
    val intervalMiniLap: String? = null,
    val latitude: Double? = 0.0,
    val loginId: Int? = 0,
    val longitude: Double? = 0.0,
    val medicalRecord: String? = null,
    val monthlySerialNumber: String? = null,
    val phoneNumber: String? = null,
    val postMiniLap: String? = null,
    val postnatalSterlization: String? = null,
    val sterilizationConsent: String? = null,
    val totalLiveChild: String? = null,
    val traditional: String? = null,
    val updatedBy: String? = null,
    val updatedDate: Long? = System.currentTimeMillis(),
)