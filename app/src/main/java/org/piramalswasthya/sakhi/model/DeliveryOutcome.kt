package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "DELIVERY_OUTCOME",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("benId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "delOutInd", value = ["benId"])]
)

data class DeliveryOutcomeCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
    var isActive: Boolean,
    var dateOfDelivery: Long? = null,
    var timeOfDelivery: String? = null,
    var placeOfDelivery: String? = null,
    var typeOfDelivery: String? = null,
    var hadComplications: Boolean? = null,
    var complication: String? = null,
    var causeOfDeath: String? = null,
    var otherCauseOfDeath: String? = null,
    var otherComplication: String? = null,
    var deliveryOutcome: Int? = 0,
    var liveBirth: Int? = 0,
    var stillBirth: Int? = 0,
    var dateOfDischarge: Long? = null,
    var timeOfDischarge: String? = null,
    var isJSYBenificiary: Boolean? = false,

    var isDeath: Boolean? = null,
    var isDeathValue: String? = null,
    var dateOfDeath: String? = null,
    var placeOfDeath: String? = null,
    var placeOfDeathId:Int ? = 0,
    var otherPlaceOfDeath: String? = null,
    var mcp1File: String? = null,
    var mcp2File: String? = null,
    var jsyFile: String? = null,



//    var isActive: Boolean? = true,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {

    private fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        dateLong?.let {
            return dateFormat.format(dateLong)
        } ?: run {
            return null
        }
    }

    fun asPostModel(): DeliveryOutcomePost {
        return DeliveryOutcomePost(
            id = id,
            benId = benId,
            isActive = isActive,
            dateOfDelivery = dateOfDelivery?.let { getDateStringFromLong(it) },
            timeOfDelivery = timeOfDelivery,
            placeOfDelivery = placeOfDelivery,
            typeOfDelivery = typeOfDelivery,
            hadComplications = hadComplications,
            complication = complication,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            otherComplication = otherComplication,
            deliveryOutcome = deliveryOutcome,
            liveBirth = liveBirth,
            stillBirth = stillBirth,
            dateOfDischarge = dateOfDischarge?.let { getDateStringFromLong(it) },
            timeOfDischarge = timeOfDischarge,
            isJSYBenificiary = isJSYBenificiary,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy,
            isDeath = isDeath,
            isDeathValue = isDeathValue,
            dateOfDeath = dateOfDeath,
            placeOfDeath = placeOfDeath,
            placeOfDeathId = placeOfDeathId,
            otherPlaceOfDeath = otherPlaceOfDeath,
            mcp1File = mcp1File,
            mcp2File = mcp2File,
            jsyFile = jsyFile

        )
    }
}

data class DeliveryOutcomePost(
    val id: Long = 0,
    val benId: Long,
    val isActive: Boolean,
    val dateOfDelivery: String? = null,
    val timeOfDelivery: String? = null,
    val placeOfDelivery: String? = null,
    val typeOfDelivery: String? = null,
    val hadComplications: Boolean? = null,
    val complication: String? = null,
    val causeOfDeath: String? = null,
    val otherCauseOfDeath: String? = null,
    val otherComplication: String? = null,
    val deliveryOutcome: Int? = 0,
    val liveBirth: Int? = 0,
    val stillBirth: Int? = 0,
    val dateOfDischarge: String? = null,
    val timeOfDischarge: String? = null,
    val isJSYBenificiary: Boolean? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String,

    var isDeath: Boolean? = null,
    var isDeathValue: String? = null,
    var dateOfDeath: String? = null,
    var placeOfDeath: String? = null,
    var placeOfDeathId: Int?=0,
    var otherPlaceOfDeath: String? = null,
    var mcp1File: String? = null,
    var mcp2File: String? = null,
    var jsyFile: String? = null,

    ) {
    fun toDeliveryCache(): DeliveryOutcomeCache {
        return DeliveryOutcomeCache(
            id = id,
            benId = benId,
            isActive = isActive,
            dateOfDelivery = getLongFromDate(dateOfDelivery),
            timeOfDelivery = timeOfDelivery,
            placeOfDelivery = placeOfDelivery,
            typeOfDelivery = typeOfDelivery,
            hadComplications = hadComplications,
            complication = complication,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            otherComplication = otherComplication,
            deliveryOutcome = deliveryOutcome,
            liveBirth = liveBirth,
            stillBirth = stillBirth,
            dateOfDischarge = getLongFromDate(dateOfDischarge),
            timeOfDischarge = timeOfDischarge,
            isJSYBenificiary = isJSYBenificiary,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED,
            isDeath = isDeath,
            isDeathValue = isDeathValue,
            dateOfDeath = dateOfDeath,
            placeOfDeath = placeOfDeath,
            placeOfDeathId = placeOfDeathId,
            otherPlaceOfDeath = otherPlaceOfDeath,
            mcp1File = mcp1File,
            mcp2File = mcp2File,
            jsyFile = jsyFile
        )
    }
}