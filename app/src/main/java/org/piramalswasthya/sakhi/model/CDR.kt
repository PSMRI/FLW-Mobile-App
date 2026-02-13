package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate

@Entity(
    tableName = "CDR",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_cdr", value = ["benId"/* "hhId"*/])]
)

data class CDRCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var visitDate: Long? = System.currentTimeMillis(),
    var cdr1File: String? = null,
    var cdr2File: String? = null,
    var cdrDeathCertFile: String? = null,
    var motherName: String? = null,
    var fatherName: String? = null,
    var address: String? = null,
    var houseNumber: String? = null,
    var mohalla: String? = null,
    var landmarks: String? = null,
    var pincode: Int? = null,
    var landline: Long? = null,
    var mobileNumber: Long = 0,
    var dateOfDeath: Long = System.currentTimeMillis(),
    var timeOfDeath: Long? = null,
    var placeOfDeath: String? = null,
    var firstInformant: String? = null,
    var ashaSign: String? = null,
    var dateOfNotification: Long? = null,
    var createdBy: String? = null,
    var createdDate: Long? = System.currentTimeMillis(),
    var updatedBy: String? = null,
    var updatedDate: Long? = System.currentTimeMillis(),
    var processed: String,
    var syncState: SyncState
) : FormDataModel {
    fun asPostModel(): CDRPost {
        return CDRPost(
            id = id,
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate),
            cdr1File = cdr1File,
            cdr2File = cdr2File,
            cdrDeathCertFile = cdrDeathCertFile,
            motherName = motherName,
            fatherName = fatherName,
            address = address,
            houseNumber = houseNumber,
            colony = mohalla,
            landmarks = landmarks,
            pincode = pincode,
            landline = landline,
            mobileNumber = mobileNumber,
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath),
            timeOfDeath = getDateTimeStringFromLong(timeOfDeath),
            placeOfDeath = placeOfDeath,
            firstInformant = firstInformant,
            ashaSign = ashaSign,
            dateOfNotification = getDateTimeStringFromLong(dateOfNotification),
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate),
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)
        )
    }
}

@JsonClass(generateAdapter = true)
data class CDRPost(
    val id: Int,
    val benId: Long,
    val visitDate: String? = null,
    var cdr1File: String? = null,
    var cdr2File: String? = null,
    var cdrDeathCertFile: String? = null,
    val motherName: String? = null,
    val fatherName: String? = null,
    val address: String? = null,
    @Json(name = "houseNo")
    val houseNumber: String? = null,
    val colony: String? = null,
    val landmarks: String? = null,
    val pincode: Int? = 0,
    val landline: Long? = 0L,
    @Json(name = "mobile")
    val mobileNumber: Long = 0L,
    @Json(name = "deathDate")
    val dateOfDeath: String? = null,
    @Json(name = "deathTime")
    val timeOfDeath: String? = null,
    val placeOfDeath: String? = null,
    val firstInformant: String? = null,
    @Json(name = "signature")
    val ashaSign: String? = null,
    @Json(name = "notificationDate")
    val dateOfNotification: String? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val updatedBy: String? = null,
    val updatedDate: String? = null,
) {
    fun asCacheModel(): CDRCache {
        return CDRCache(
            id = id,
            benId = benId,
            visitDate = visitDate?.let { getLongFromDate(it) },
            cdr1File = cdr1File,
            cdr2File = cdr2File,
            cdrDeathCertFile = cdrDeathCertFile,
            motherName = motherName,
            fatherName = fatherName,
            address = address,
            houseNumber = houseNumber,
            mohalla = colony,
            landmarks = landmarks,
            pincode = pincode,
            landline = landline,
            mobileNumber = mobileNumber,
            dateOfDeath = getLongFromDate(dateOfDeath),
            timeOfDeath = timeOfDeath?.let { getLongFromDate(it) },
            placeOfDeath = placeOfDeath,
            firstInformant = firstInformant,
            ashaSign = ashaSign,
            dateOfNotification = dateOfNotification?.let { getLongFromDate(it) },
            createdBy = createdBy,
            createdDate = createdDate?.let { getLongFromDate(it) },
            updatedBy = updatedBy,
            updatedDate = updatedDate?.let { getLongFromDate(it) },
            processed = "N",
            syncState = SyncState.SYNCED,
        )
    }
}