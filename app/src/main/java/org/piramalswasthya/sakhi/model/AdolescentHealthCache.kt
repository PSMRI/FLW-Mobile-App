package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.AdolscentHealthDTO

@Entity(
    tableName = "Adolescent_Health_Form_Data",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_adolescentsn", value = ["benId"/* "hhId"*/])]
)
data class AdolescentHealthCache(
    @PrimaryKey(autoGenerate = true)
    var id :Int? = null,
    var userID :Int? =null,
    var benId:Long?=null,
    var visitDate: Long = System.currentTimeMillis(),
    var healthStatus: String? = null,
    var ifaTabletDistributed: Boolean? = null,
    var quantityOfIfaTablets: Int? = null,
    var menstrualHygieneAwarenessGiven: Boolean? = null,
    var sanitaryNapkinDistributed: Boolean? = null,
    var noOfPacketsDistributed: Int? = null,
    var place: String? = null,
    var distributionDate: Long = System.currentTimeMillis(),
    var referredToHealthFacility: String? = null,
    var counselingProvided: Boolean? = null,
    var counselingType: String? = null,
    var followUpDate: Long = System.currentTimeMillis(),
    var referralStatus: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
) : FormDataModel{
    fun toDTO(): AdolscentHealthDTO {
        return AdolscentHealthDTO(
            id = 0,
            benId = benId!!,
            visitDate = getDateTimeStringFromLong(visitDate).toString(),
            healthStatus = healthStatus,
            ifaTabletDistributed = ifaTabletDistributed,
            quantityOfIfaTablets = quantityOfIfaTablets,
            menstrualHygieneAwarenessGiven = menstrualHygieneAwarenessGiven,
            sanitaryNapkinDistributed = sanitaryNapkinDistributed,
            noOfPacketsDistributed = noOfPacketsDistributed,
            place = place,
            distributionDate = getDateTimeStringFromLong(distributionDate),
            referredToHealthFacility = referredToHealthFacility,
            counselingProvided = counselingProvided,
            counselingType = counselingType,
            followUpDate = getDateTimeStringFromLong(followUpDate).toString(),
            referralStatus = referralStatus


        )
    }
}
data class BenWithAdolescentCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val tb: AdolescentHealthCache?,

    ) {
    fun asAdolescentDomainModel(): BenWithAdolescentDomain {
        return BenWithAdolescentDomain(
            ben = ben.asBasicDomainModel(),
            adolescent = tb
        )
    }
}

data class BenWithAdolescentDomain(
    val ben: BenBasicDomain,
    val adolescent: AdolescentHealthCache?
)