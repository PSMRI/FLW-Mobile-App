package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.TBConfirmedTreatmentDTO
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "TB_CONFIRMED_TREATMENT",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("benId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_tb_confirmed", value = ["benId"])]
)
data class TBConfirmedTreatmentCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var regimenType: String? = null,
    var treatmentStartDate: Long = System.currentTimeMillis(),
    var expectedTreatmentCompletionDate: Long? = null,
    var followUpDate: Long? = null,
    var monthlyFollowUpDone: String? = null,
    var adherenceToMedicines: String? = null,
    var anyDiscomfort: Boolean? = null,
    var treatmentCompleted: Boolean? = null,
    var actualTreatmentCompletionDate: Long? = null,
    var treatmentOutcome: String? = null,
    var dateOfDeath: Long? = null,
    var placeOfDeath: String? = null,
    var reasonForDeath: String = "Tuberculosis",
    var reasonForNotCompleting: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : FormDataModel {

    fun toDTO(): TBConfirmedTreatmentDTO {
        return TBConfirmedTreatmentDTO(
            id = id.toLong(),
            benId = benId,
            regimenType = regimenType,
            treatmentStartDate = getDateTimeStringFromLong(treatmentStartDate),
            expectedTreatmentCompletionDate = getDateTimeStringFromLong(expectedTreatmentCompletionDate),
            followUpDate = getDateTimeStringFromLong(followUpDate),
            monthlyFollowUpDone = monthlyFollowUpDone,
            adherenceToMedicines = adherenceToMedicines,
            anyDiscomfort = anyDiscomfort,
            treatmentCompleted = treatmentCompleted,
            actualTreatmentCompletionDate = getDateTimeStringFromLong(actualTreatmentCompletionDate),
            treatmentOutcome = treatmentOutcome,
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath),
            placeOfDeath = placeOfDeath,
            reasonForDeath = reasonForDeath,
            reasonForNotCompleting = reasonForNotCompleting
        )
    }
}

data class BenWithTbConfirmedCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId",
        entityColumn = "benId"
    )
    val tb: TBConfirmedTreatmentCache?
) {
    fun asTbSuspectedDomainModel(): BenWithTbConfirmedDomain {
        return BenWithTbConfirmedDomain(
            ben = ben.asBasicDomainModel(),
            tb = tb
        )
    }
}

data class BenWithTbConfirmedDomain(
    val ben: BenBasicDomain,
    val tb: TBConfirmedTreatmentCache?
)

