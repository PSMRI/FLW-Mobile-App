package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.network.getLongFromDate

@Entity(tableName = "INCENTIVE_ACTIVITY")
data class IncentiveActivityCache(
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String,
    val paymentParam: String,
    val isPaid: Boolean = false,
    val rate: Int,
    val state: Int,
    val district: Int,
    val group: String,
    val fmrCode: String?,
    val fmrCodeOld: String?,
//    val createdDate: Long,
//    val createdBy: String,
//    val updatedDate: Long,
//    val updatedBy: String,
)

@JsonClass(generateAdapter = true)
data class IncentiveActivityNetwork(
    val id: Long,
    val name: String,
    val description: String,
    val paymentParam: String,
    val rate: Int,
    val state: Int,
    val district: Int,
    val group: String,
    val fmrCode: String?,
    val fmrCodeOld: String?,
    val createdDate: String,
    val createdBy: String,
    val updatedDate: String,
    val updatedBy: String,
) {
    fun asCacheModel(): IncentiveActivityCache {
        return IncentiveActivityCache(
            id = id,
            name = name,
            description = description,
            paymentParam = paymentParam,
            rate = rate,
            state = state,
            district = district,
            group = group,
            fmrCode = fmrCode,
            fmrCodeOld = fmrCodeOld
        )
    }
}

@JsonClass(generateAdapter = true)
data class IncentiveActivityListRequest(
    @Json(name = "state")
    val stateId: Int,

    @Json(name = "district")
    val districtId: Int
)

@JsonClass(generateAdapter = true)
data class IncentiveActivityListResponse(
    val data: List<IncentiveActivityNetwork>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@Entity(
    tableName = "INCENTIVE_RECORD",
    foreignKeys = [ForeignKey(
        entity = IncentiveActivityCache::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("activityId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "incentiveInd", value = ["activityId"])]
)
data class IncentiveRecordCache(
    @PrimaryKey
    val id: Long,
    val activityId: Long,
    val ashaId: Int,
    val benId: Long,
    val amount: Long,
    val name: String?,
    val startDate: Long,
    val endDate: Long,
    val createdDate: Long,
    val createdBy: String,
    val updatedDate: Long,
    val updatedBy: String,
)


data class IncentiveRecordNetwork(
    val id: Long,
    val activityId: Long,
    val ashaId: Int,
    val benId: Long,
    val amount: Long,
    val name: String? = null,
    val startDate: String,
    val endDate: String,
    val createdDate: String,
    val createdBy: String,
    val updatedDate: String,
    val updatedBy: String,
) {
    fun asCacheModel(): IncentiveRecordCache {
        return IncentiveRecordCache(
            id = id,
            activityId = activityId,
            ashaId = ashaId,
            benId = benId,
            amount = amount,
            name = name,
            startDate = getLongFromDate(startDate),
            endDate = getLongFromDate(endDate),
            createdDate = getLongFromDate(createdDate),
            createdBy = createdBy,
            updatedDate = getLongFromDate(updatedDate),
            updatedBy = updatedBy,
        )
    }
}

@JsonClass(generateAdapter = true)
data class IncentiveRecordListRequest(
    @Json(name = "ashaId")
    val userId: Int,
    val fromDate: String,
    val toDate: String,
)

@JsonClass(generateAdapter = true)
data class IncentiveRecordListResponse(
    val data: List<IncentiveRecordNetwork>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

data class IncentiveCache(
    @Embedded
    val record: IncentiveRecordCache,
    @Relation(parentColumn = "activityId", entityColumn = "id")
    val activity: IncentiveActivityCache,
    @Relation(parentColumn = "benId", entityColumn = "benId")
    val ben: BenBasicCache?,
) {
    fun asDomainModel(): IncentiveDomain {
        return IncentiveDomain(
            record,
            activity,
            ben?.asBasicDomainModel()
        )
    }
}

data class IncentiveDomain(
    val record: IncentiveRecordCache,
    val activity: IncentiveActivityCache,
    val ben: BenBasicDomain?
)

data class IncentiveDomainDTO(
    val id: Long = 0,
    val group: String,
    val name: String,
    val description: String,
    val paymentParam: String,
    val rate: Long,
    var noOfClaims: Int,
    var amountClaimed: Long,
    var fmrCode: String?
)



