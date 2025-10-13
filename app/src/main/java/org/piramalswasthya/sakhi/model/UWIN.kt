package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate
import org.piramalswasthya.sakhi.model.getDateTimeStringFromLong



@Entity(tableName = "UWIN_SESSION")
data class UwinCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var sessionDate: Long,
    var place: String?,
    var participantsCount: Int,
    var uploadedFiles1: String? = null,
    var uploadedFiles2: String? = null,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun asDomainModel(): UwinNetwork {
        return UwinNetwork(
            id = id,
            sessionDate = sessionDate,
            place = place,
            participantsCount = participantsCount,
            uploadedFiles1 = uploadedFiles1,
            uploadedFiles2 = uploadedFiles2,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)!!,
        )
    }
}


data class UwinNetwork(
    val id: Int,
    val sessionDate: Long,
    val place: String?,
    val participantsCount: Int,
    var uploadedFiles1: String? = null,
    var uploadedFiles2: String? = null,
    var createdBy: String,
    val createdDate: String,
    var updatedBy: String,
    val updatedDate: String,
) {
    fun asCacheModel(): UwinCache {
        return UwinCache(
            id = id,
            sessionDate = sessionDate,
            place = place,
            participantsCount = participantsCount,
            uploadedFiles1 = uploadedFiles1,
            uploadedFiles2 = uploadedFiles2,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED,
        )
    }
}


data class UwinGetAllRequest(
    val villageID: Int,
    val fromDate: String,
    val toDate: String,
    val pageNo: Int,
    val userId: Int,
    val userName: String,
    val ashaId: Int
)