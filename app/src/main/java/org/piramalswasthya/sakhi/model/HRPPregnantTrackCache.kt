package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "HRP_PREGNANT_TRACK",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId",/* "householdId"*/),
        childColumns = arrayOf("benId", /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_hpt", value = ["benId",/* "hhId"*/])]
)

data class HRPPregnantTrackCache (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId : Long,
    var dateOfVisit: Long? = null,
    var rdPmsa : String? = null,
    var severeAnemia: String? = null,
    var pregInducedHypertension: String? = null,
    var gestDiabetesMellitus: String? = null,
    var hypothyrodism: String? = null,
    var polyhydromnios: String? = null,
    var oligohydromnios: String? = null,
    var antepartumHem: String? = null,
    var malPresentation: String? = null,
    var hivsyph: String? = null,
    var visit: String? = null
) : FormDataModel {

    fun asDomainModel(): HRPPregnantTrackDomain {
        return HRPPregnantTrackDomain(
            id = id,
            dateOfVisit = visit + " : " +getDateStringFromLong(dateOfVisit)
        )
    }
}

data class HRPPregnantTrackDomain (
    val id: Int = 0,
    val dateOfVisit: String?
    )
data class HRPPregnantTrackBen(
    val ben : BenBasicDomain,
    val trackList: List<HRPPregnantTrackCache>,
//    val onClick: (Long, Int) -> Unit
)

fun getDateStringFromLong(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    dateLong?.let {
        val dateString = dateFormat.format(dateLong)
        val timeString = timeFormat.format(dateLong)
        return dateString
    } ?: run {
        return null
    }

}