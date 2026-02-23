package org.piramalswasthya.sakhi.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.piramalswasthya.sakhi.network.getLongFromDate

@Entity(tableName = "INCENTIVE_ACTIVITY")
@Parcelize
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
    val groupName: String,
    val fmrCode: String?,
    val fmrCodeOld: String?,
//    val createdDate: Long,
//    val createdBy: String,
//    val updatedDate: Long,
//    val updatedBy: String,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class IncentiveActivityNetwork(
    val id: Long,
    val name: String,
    val description: String,
    val paymentParam: String,
    val rate: Int,
    val state: Int,
    val district: Int,
    val group: String,
    val groupName: String,
    val fmrCode: String?,
    val fmrCodeOld: String?,
    val createdDate: String,
    val createdBy: String,
    val updatedDate: String,
    val updatedBy: String,
) : Parcelable {
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
            groupName = groupName,
            fmrCode = fmrCode,
            fmrCodeOld = fmrCodeOld
        )
    }
}

@JsonClass(generateAdapter = true)
@Parcelize
data class IncentiveActivityListRequest(
    @Json(name = "state")
    val stateId: Int,

    @Json(name = "district")
    val districtId: Int,
    @Json(name = "langCode")
    val language: String
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class IncentiveActivityListResponse(
    val data: List<IncentiveActivityNetwork>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
) : Parcelable

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
@Parcelize
data class IncentiveRecordCache(
    @PrimaryKey
    val id: Long,
    @ColumnInfo
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
    @ColumnInfo(defaultValue = "0")
    val isEligible : Boolean
) : Parcelable

@Parcelize
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
    val isEligible : Boolean
) : Parcelable {
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
            isEligible = isEligible
        )
    }
}

@JsonClass(generateAdapter = true)
@Parcelize
data class IncentiveRecordListRequest(
    @Json(name = "ashaId")
    val userId: Int,
    val fromDate: String,
    val toDate: String,
    val villageID : Int

) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class IncentiveRecordListResponse(
    val data: List<IncentiveRecordNetwork>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
) : Parcelable

@Parcelize
data class IncentiveCache(
    @Embedded
    val record: IncentiveRecordCache,
    @Relation(parentColumn = "activityId", entityColumn = "id")
    val activity: IncentiveActivityCache,
    @Relation(parentColumn = "benId", entityColumn = "benId")
    val ben: BenBasicCache?,
) : Parcelable {
    fun asDomainModel(): IncentiveDomain {
        return IncentiveDomain(
            record,
            activity,
            ben?.asBasicDomainModel()
        )
    }
}
@Parcelize
data class IncentiveActivityWithRecords(
    @Embedded
    val activity: IncentiveActivityCache,

    @Relation(
        parentColumn = "id",
        entityColumn = "activityId"
    )
    val records: List<IncentiveRecordCache>
) : Parcelable {
    fun asDomainModel() = IncentiveActivityDomain(activity, records)
}


@Parcelize
data class IncentiveDomain(
    val record: IncentiveRecordCache,
    val activity: IncentiveActivityCache,
    val ben: BenBasicDomain?,
    var uploadedFiles: List<String> = emptyList(),
    var fileCount: Int = 0,
    var isSubmitted: Boolean = false,
    var submittedAt: Long = 0L,
    var serverFileUrls: List<String> = emptyList()
) : Parcelable

@Parcelize
data class IncentiveActivityDomain(
    val activity: IncentiveActivityCache,
    val records: List<IncentiveRecordCache>
) : Parcelable
@Parcelize
data class IncentiveDomainDTO(
    val id: Long = 0,
    var group: String,
    var groupName : String,
    val name: String,
    var description: String,
    val paymentParam: String,
    val rate: Long,
    var noOfClaims: Int,
    var amountClaimed: Long,
    var fmrCode: String?,
    val documentsSubmitted: String? = null
) : Parcelable

@Parcelize
data class IncentiveGrouped(
    val activityName: String,
    val totalAmount: Long,
    val count: Int,
    val groupName: String,
    val description: String,
    val activity: IncentiveActivityCache,
    val hasZeroBen: Boolean = false,
    val defaultIncentive : Boolean = false,
    val isEligible: Boolean = false
) : Parcelable



