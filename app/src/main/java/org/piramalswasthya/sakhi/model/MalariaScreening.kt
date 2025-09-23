package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.MalariaScreeningDTO

@Entity(
    tableName = "MALARIA_SCREENING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
        childColumns = arrayOf("benId" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_malariasn", value = ["benId"/* "hhId"*/])]
)
data class MalariaScreeningCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var caseDate: Long = System.currentTimeMillis(),
    val houseHoldDetailsId: Long,
    val screeningDate: Long = System.currentTimeMillis(),
    var beneficiaryStatus: String ? = null,
    var beneficiaryStatusId: Int = 0,
    var dateOfDeath: Long = System.currentTimeMillis(),
    var placeOfDeath: String ? = null,
    var otherPlaceOfDeath: String ? = null,
    var reasonForDeath: String ?  = null,
    var otherReasonForDeath: String ?  = null,
    var rapidDiagnosticTest: String ? = null,
    var dateOfRdt: Long = System.currentTimeMillis(),
    var slideTestPf: String ?  = null,
    var slideTestPv: String ?  = null,
    var slideTestName: String ?  = null,
    var dateOfSlideTest: Long = System.currentTimeMillis(),
    var dateOfVisitBySupervisor: Long = System.currentTimeMillis(),
    var caseStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String ? = null,
    var otherReferredFacility: String ? = null,
    var remarks: String ? = null,
    var diseaseTypeID: Int ? = 0,
    var followUpDate: Long = System.currentTimeMillis(),
    var feverMoreThanTwoWeeks: Boolean ? = false,
    var fluLikeIllness: Boolean ? = false,
    var shakingChills: Boolean ? = false,
    var headache: Boolean ? = false,
    var muscleAches: Boolean ? = false,
    var tiredness: Boolean ? = false,
    var nausea: Boolean ? = false,
    var vomiting: Boolean ? = false,
    var diarrhea: Boolean ? = false,
    var createdBy: String ? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
): FormDataModel {
    fun toDTO(): MalariaScreeningDTO {
        return MalariaScreeningDTO(
            id = 0,
            benId = benId,
            caseDate = getDateTimeStringFromLong(caseDate).toString(),
            houseHoldDetailsId = houseHoldDetailsId,
            caseStatus = caseStatus.toString(),
            referredTo = referredTo!!,
            otherReferredFacility = otherReferredFacility.toString(),
            referToName = referToName.toString(),
            remarks = remarks.toString(),
            followUpDate = getDateTimeStringFromLong(followUpDate).toString(),
            diseaseTypeID = diseaseTypeID!!,
            feverMoreThanTwoWeeks = feverMoreThanTwoWeeks,
            fluLikeIllness = fluLikeIllness,
            shakingChills = shakingChills,
            headache = headache,
            muscleAches = muscleAches,
            tiredness = tiredness,
            nausea = nausea,
            vomiting = vomiting,
            diarrhea = diarrhea,
            beneficiaryStatusId = beneficiaryStatusId,
            beneficiaryStatus = beneficiaryStatus.toString(),
            createdBy = createdBy,
            screeningDate = getDateTimeStringFromLong(screeningDate).toString(),
            rapidDiagnosticTest = rapidDiagnosticTest.toString(),
            slideTestName = slideTestName.toString(),
            slideTestPf = slideTestPf.toString(),
            slideTestPv = slideTestPv.toString(),
            dateOfSlideTest = getDateTimeStringFromLong(dateOfSlideTest).toString(),
            dateOfRdt = getDateTimeStringFromLong(dateOfRdt).toString(),
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath).toString(),
            dateOfVisitBySupervisor = getDateTimeStringFromLong(dateOfVisitBySupervisor).toString(),
            reasonForDeath = reasonForDeath.toString(),
            otherReasonForDeath = otherReasonForDeath.toString(),
            otherPlaceOfDeath = otherPlaceOfDeath.toString(),
            placeOfDeath = placeOfDeath.toString()


        )
    }
}

data class BenWithMalariaScreeningCache(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId"
    )
    val malaria: MalariaScreeningCache?,

    ) {
    fun asMalariaScreeningDomainModel(): BenWithMalariaScreeningDomain {
        return BenWithMalariaScreeningDomain(
            ben = ben.asBasicDomainModel(),
            tb = malaria
        )
    }
}

data class BenWithMalariaScreeningDomain(
    val ben: BenBasicDomain,
    val tb: MalariaScreeningCache?
)
