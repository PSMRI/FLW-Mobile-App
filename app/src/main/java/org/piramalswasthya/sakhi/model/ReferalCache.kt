package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.NCDReferalDTO

@Entity(
    tableName = "NCD_REFER",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_refcache", value = ["benId"/* "hhId"*/, "referralReason",], unique = true)]
)
data class ReferalCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var benId: Long,
    var referredToInstituteID: Int? = 0,
    var refrredToAdditionalServiceList: List<String>? = null,
    var referredToInstituteName: String? = null,
    var referralReason: String? = null,
    var revisitDate: Long = System.currentTimeMillis(),
    var vanID: Int? = 0,
    var parkingPlaceID: Int? = 0,
    var beneficiaryRegID: Long? = 0L,
    var benVisitID: Long? = 0L,
    var visitCode: Long? = 0L,
    var providerServiceMapID: Int? = 0,
    var createdBy: String? = "",
    var type: String? = null,
    var isSpecialist: Boolean? = false,
    var syncState: SyncState
) : FormDataModel {
    fun toDTO(): NCDReferalDTO {
        return NCDReferalDTO(
            id = 0,
            benId = benId,
            revisitDate = getDateTimeStringFromLong(revisitDate).toString(),
            referralReason = referralReason,
            referredToInstituteID = referredToInstituteID,
            referredToInstituteName = referredToInstituteName,
            vanID = vanID,
            parkingPlaceID = parkingPlaceID,
            refrredToAdditionalServiceList = refrredToAdditionalServiceList,
            isSpecialist = isSpecialist,
            syncState = SyncState.SYNCED,
            providerServiceMapID = providerServiceMapID,
            createdBy = createdBy,
            benVisitID = benVisitID,
            visitCode = visitCode,
            beneficiaryRegID = benId,
            type = type


        )
    }
}

data class BenWithNCDReferalCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val refcache: ReferalCache?,

    ) {
    fun ncdreferalDomainModel(): BenWithNCDReferalDomain {
        return BenWithNCDReferalDomain(
            ben = ben.asBasicDomainModel(),
            refcache = refcache
        )
    }
}

data class BenWithNCDReferalDomain(
    val ben: BenBasicDomain,
    val refcache: ReferalCache?
)

data class ReferralRequest(
    val refer: NCDReferalDTO,
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
}