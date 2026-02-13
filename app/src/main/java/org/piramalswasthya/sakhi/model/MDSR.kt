package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.getLongFromDate
import java.text.SimpleDateFormat
import java.util.Calendar

@Entity(
    tableName = "MDSR",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId" /*"householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_mdsr", value = ["benId"/* "hhId"*/])]
)

data class MDSRCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var mdsr1File: String? = null,
    var mdsr2File: String? = null,
    var mdsrDeathCertFile: String? = null,
    var dateOfDeath: Long? = System.currentTimeMillis(),
    var address: String? = null,
    var husbandName: String? = null,
    var causeOfDeath: String? = null,
    var reasonOfDeath: String? = null,
    var investigationDate: Long? = System.currentTimeMillis(),
    var actionTaken: Boolean? = null,
    var blockMOSign: String? = null,
    var dateIc: Long? = System.currentTimeMillis(),
    var processed: String,
    var syncState: SyncState,
    var createdBy: String? = null,
    var createdDate: Long? = System.currentTimeMillis(),
    var updatedBy: String? = null,
    var updatedDate: Long? = System.currentTimeMillis(),
) : FormDataModel {
    fun asPostModel(): MdsrPost {
        return MdsrPost(
            id = id,
            benId = benId,
            mdsr1File=mdsr1File,
            mdsr2File=mdsr2File,
            mdsrDeathCertFile=mdsrDeathCertFile,
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath),
            address = address,
            husbandName = husbandName,
            causeOfDeath = causeOfDeath,
            reasonDeath = reasonOfDeath,
            investigationDate = getDateTimeStringFromLong(investigationDate),
            actionTaken = actionTaken,
            signature = blockMOSign,
            dateIc = getDateTimeStringFromLong(dateIc),
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate),
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate),


            )
    }
}

@JsonClass(generateAdapter = true)
data class MdsrPost(

    val id: Int = 0,
    val benId: Long,
    var mdsr1File: String? = null,
    var mdsr2File: String? = null,
    var mdsrDeathCertFile: String? = null,
    val dateOfDeath: String? = null,
    val address: String? = null,
    val husbandName: String? = null,
    val causeOfDeath: String? = null,
    val reasonDeath: String? = null,
    val investigationDate: String? = null,
    val actionTaken: Boolean?,
    val signature: String? = null,
    val dateIc: String? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val updatedBy: String? = null,
    val updatedDate: String? = null,
) {
    fun asCacheModel(): MDSRCache {
        return MDSRCache(
            id = id,
            benId = benId,
            mdsr1File=mdsr1File,
            mdsr2File=mdsr2File,
            mdsrDeathCertFile=mdsrDeathCertFile,
            dateOfDeath = getLongFromDate(dateOfDeath),
            address = address,
            husbandName = husbandName,
            causeOfDeath = causeOfDeath,
            reasonOfDeath = reasonDeath,
            investigationDate = getLongFromDate(investigationDate),
            actionTaken = actionTaken,
            blockMOSign = signature,
            dateIc = getLongFromDate(dateIc),
            processed = "N",
            syncState = SyncState.SYNCED,
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
        )

    }
}

fun getMonth(): String {
    val calendar = Calendar.getInstance()
    val date = calendar.time
    val month = SimpleDateFormat("MM").format(date) // always 2 digits
    return month
}

fun getYear(): String {
    val calendar = Calendar.getInstance()
    val date = calendar.time
    val year = SimpleDateFormat("yyyy").format(date) // 4 digit y
    return year
}

fun getDays(): String {
    val calendar = Calendar.getInstance()
    val date = calendar.time
    val days = SimpleDateFormat("dd").format(date) // 2 digit y
    return days
}


data class BenWithAncDoPncCache(
//    @ColumnInfo(name = "benId")
//    val ecBenId: Long,

    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val anc: List<PregnantWomanAncCache>,

    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = DeliveryOutcomeCache::class
    )
    val deliveryOutcome: List<DeliveryOutcomeCache>,

    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = PNCVisitCache::class
    )
    val pnc: List<PNCVisitCache>
)

fun List<BenWithAncDoPncCache>.filterMdsr(): List<BenBasicDomainForForm> {
    return filter {
        it.anc.any { it.maternalDeath == true } || it.deliveryOutcome.any { it.complication == "DEATH" } || it.pnc.any { it.motherDeath }
    }.map { it.ben.asBenBasicDomainModelForMdsrForm() }
}