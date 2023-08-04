package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.HRPNonPregnantAssessDTO

@Entity(
    tableName = "HRP_NON_PREGNANT_ASSESS",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_hnpa", value = ["benId"/* "hhId"*/])]
)

data class HRPNonPregnantAssessCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var noOfDeliveries: String? = null,
    var timeLessThan18m: String? = null,
    var heightShort: String? = null,
    var age: String? = null,
    var misCarriage: String? = null,
    var homeDelivery: String? = null,
    var medicalIssues: String? = null,
    var pastCSection: String? = null,
    var isHighRisk: Boolean = false,
    var visitDate: Long = System.currentTimeMillis(),
    var syncState: SyncState = SyncState.UNSYNCED
) : FormDataModel {
    fun toDTO(): HRPNonPregnantAssessDTO {
        return HRPNonPregnantAssessDTO(
            id = 0,
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            misCarriage = misCarriage,
            homeDelivery = homeDelivery,
            medicalIssues = medicalIssues,
            pastCSection = pastCSection,
            isHighRisk = isHighRisk,
            visitDate = getDateTimeStringFromLong(visitDate)
        )
    }
}