package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.LeprosyFollowUpDTO
import org.piramalswasthya.sakhi.network.LeprosyScreeningDTO


@Entity(
    tableName = "LEPROSY_SCREENING",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("benId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_leprosysn", value = ["benId"], unique = true)]
)
data class LeprosyScreeningCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    var homeVisitDate: Long = System.currentTimeMillis(),
    var leprosyStatusDate: Long = System.currentTimeMillis(),
    var dateOfDeath: Long = System.currentTimeMillis(),
    var houseHoldDetailsId: Long,
    var leprosyStatus: String? = "",
    var referredTo: Int? = 0,
    var leprosyState: String? = "Screening",
    var referToName: String? = null,
    var otherReferredTo: String? = null,
    var typeOfLeprosy: String? = null,
    var remarks: String? = null,
    var beneficiaryStatus: String? = null,
    var placeOfDeath: String? = null,
    var otherPlaceOfDeath: String? = null,
    var reasonForDeath: String? = null,
    var otherReasonForDeath: String? = null,
    var diseaseTypeID: Int? = 0,
    var beneficiaryStatusId: Int? = 0,
    var leprosySymptoms: String? = null,
    var leprosySymptomsPosition: Int? = 1,
    var lerosyStatusPosition: Int? = 0,
    var currentVisitNumber: Int = 1,
    var visitLabel: String? = "Visit -1",
    var visitNumber: Int? = 1,
    var isConfirmed: Boolean = false,
    var treatmentStartDate: Long = System.currentTimeMillis(),
    var totalFollowUpMonthsRequired: Int = 0,
    var treatmentEndDate: Long = System.currentTimeMillis(),
    val mdtBlisterPackRecived: String? = null,
    var treatmentStatus: String? = null,
    var createdBy: String,
    var createdDate: Long = System.currentTimeMillis(),
    var modifiedBy: String,
    var lastModDate: Long = System.currentTimeMillis(),
    var recurrentUlceration: String? = null,
    var recurrentUlcerationId: Int? = 1,
    var recurrentTingling: String? = null,
    var recurrentTinglingId: Int? = 1,
    var hypopigmentedPatch: String? = null,
    var hypopigmentedPatchId: Int? = 1,
    var thickenedSkin: String? = null,
    var thickenedSkinId: Int? = 1,
    var skinNodules: String? = null,
    var skinNodulesId: Int? = 1,
    var skinPatchDiscoloration: String? = null,
    var skinPatchDiscolorationId: Int? = 1,
    var recurrentNumbness: String? = null,
    var recurrentNumbnessId: Int? = 1,
    var clawingFingers: String? = null,
    var clawingFingersId: Int? = 1,
    var tinglingNumbnessExtremities: String? = null,
    var tinglingNumbnessExtremitiesId: Int? = 1,
    var inabilityCloseEyelid: String? = null,
    var inabilityCloseEyelidId: Int? = 1,
    var difficultyHoldingObjects: String? = null,
    var difficultyHoldingObjectsId: Int? = 1,
    var weaknessFeet: String? = null,
    var weaknessFeetId: Int? = 1,

    var syncState: SyncState = SyncState.UNSYNCED,
) : FormDataModel {

    fun toDTO(): LeprosyScreeningDTO {
        return LeprosyScreeningDTO(
            benId = benId,
            homeVisitDate = getDateTimeStringFromLong(homeVisitDate).toString(),
            leprosyStatusDate = getDateTimeStringFromLong(leprosyStatusDate).toString(),
            dateOfDeath = getDateTimeStringFromLong(dateOfDeath).toString(),
            houseHoldDetailsId = houseHoldDetailsId,
            leprosyStatus = leprosyStatus,
            referredTo = referredTo,
            referToName = referToName,
            otherReferredTo = otherReferredTo,
            typeOfLeprosy = typeOfLeprosy,
            remarks = remarks,
            beneficiaryStatus = beneficiaryStatus,
            placeOfDeath = placeOfDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            diseaseTypeID = diseaseTypeID,
            beneficiaryStatusId = beneficiaryStatusId,
            leprosySymptoms = leprosySymptoms,
            leprosySymptomsPosition = leprosySymptomsPosition,
            lerosyStatusPosition = lerosyStatusPosition,
            currentVisitNumber = currentVisitNumber,
            visitLabel = visitLabel,
            visitNumber = visitNumber,
            isConfirmed = isConfirmed,
            leprosyState = leprosyState,
            treatmentStartDate = getDateTimeStringFromLong(treatmentStartDate).toString(),
            totalFollowUpMonthsRequired = totalFollowUpMonthsRequired,
            treatmentEndDate = getDateTimeStringFromLong(treatmentEndDate).toString(),
            mdtBlisterPackRecived = mdtBlisterPackRecived,
            createdBy = createdBy,
            createdDate =getDateTimeStringFromLong(createdDate).toString(),
            modifiedBy = modifiedBy,
            lastModDate = getDateTimeStringFromLong(lastModDate).toString(),
            treatmentStatus = treatmentStatus,
            recurrentUlceration = recurrentUlceration,
            recurrentUlcerationId = recurrentUlcerationId,
            recurrentTingling = recurrentTingling,
            recurrentTinglingId = recurrentTinglingId,
            hypopigmentedPatchId = hypopigmentedPatchId,
            hypopigmentedPatch = hypopigmentedPatch,
            thickenedSkin = thickenedSkin,
            thickenedSkinId = thickenedSkinId,
            skinNodules = skinNodules,
            skinNodulesId = skinNodulesId,
            skinPatchDiscoloration = skinPatchDiscoloration,
            skinPatchDiscolorationId = skinPatchDiscolorationId,
            recurrentNumbness = recurrentNumbness,
            recurrentNumbnessId = recurrentNumbnessId,
            clawingFingers = clawingFingers,
            clawingFingersId = clawingFingersId,
            tinglingNumbnessExtremities = tinglingNumbnessExtremities,
            tinglingNumbnessExtremitiesId = tinglingNumbnessExtremitiesId,
            inabilityCloseEyelid = inabilityCloseEyelid,
            inabilityCloseEyelidId = inabilityCloseEyelidId,
            difficultyHoldingObjects = difficultyHoldingObjects,
            difficultyHoldingObjectsId = difficultyHoldingObjectsId,
            weaknessFeet = weaknessFeet,
            weaknessFeetId = weaknessFeetId,
        )
    }
}


data class BenWithLeprosyScreeningCache(
    @Embedded
    val ben: BenBasicCache,

    @Relation(
        parentColumn = "benId",
        entityColumn = "benId"
    )
    val leprosyScreening: LeprosyScreeningCache?,

    @Relation(
        parentColumn = "benId",
        entityColumn = "benId"
    )
    val followUps: List<LeprosyFollowUpCache>
) {
    fun getCurrentFollowUp(): LeprosyFollowUpCache? {
        return followUps.find { it.visitNumber == leprosyScreening?.currentVisitNumber }
    }

    fun getAllFollowUpsForCurrentVisit(): List<LeprosyFollowUpCache> {
        return followUps.filter { it.visitNumber == leprosyScreening?.currentVisitNumber }
    }

    fun getLastFollowUpForCurrentVisit(): LeprosyFollowUpCache? {
        return getAllFollowUpsForCurrentVisit().maxByOrNull { it.followUpDate }
    }

    fun asLeprosyScreeningDomainModel(): BenWithLeprosyScreeningDomain {
        return BenWithLeprosyScreeningDomain(
            ben = ben.asBasicDomainModel(),
            leprosy = leprosyScreening,
            followUps = followUps,
            currentFollowUp = getCurrentFollowUp(),
            currentVisitFollowUps = getAllFollowUpsForCurrentVisit(),
            lastFollowUp = getLastFollowUpForCurrentVisit()
        )
    }
}

data class BenWithLeprosyScreeningDomain(
    val ben: BenBasicDomain,
    val leprosy: LeprosyScreeningCache?,
    val followUps: List<LeprosyFollowUpCache>,
    val currentFollowUp: LeprosyFollowUpCache?,
    val currentVisitFollowUps: List<LeprosyFollowUpCache>,
    val lastFollowUp: LeprosyFollowUpCache?
)@Entity(
    tableName = "LEPROSY_FOLLOW_UP",
    indices = [
        Index(name = "ind_leprosy_followup_ben", value = ["benId"]),
        Index(name = "ind_leprosy_followup_visit", value = ["benId", "visitNumber"])
    ]
)
data class LeprosyFollowUpCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benId: Long,
    val visitNumber: Int,
    var followUpDate: Long = System.currentTimeMillis(),
    var treatmentStatus: String? = null,
    var mdtBlisterPackReceived: String? = null,
    var treatmentCompleteDate: Long = 0,
    var remarks: String? = null,
    var homeVisitDate: Long = System.currentTimeMillis(),
    var leprosySymptoms: String? = null,
    var typeOfLeprosy: String? = null,
    var leprosySymptomsPosition: Int? = 1,
    var visitLabel: String? = "Visit -1",
    var leprosyStatus: String? = "",
    var referredTo: Int? = 0,
    var referToName: String? = null,
    var treatmentEndDate: Long = System.currentTimeMillis(),
    val mdtBlisterPackRecived: String? = null,
    var treatmentStartDate: Long = System.currentTimeMillis(),
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    val modifiedBy: String,
    val lastModDate: Long = System.currentTimeMillis(),
    var syncState: SyncState = SyncState.UNSYNCED
) : FormDataModel {

    fun toDTO(): LeprosyFollowUpDTO {
        return LeprosyFollowUpDTO(
            benId = benId,
            visitNumber = visitNumber,
            followUpDate = getDateTimeStringFromLong(followUpDate).toString(),
            treatmentStatus = treatmentStatus,
            mdtBlisterPackReceived = mdtBlisterPackReceived,
            treatmentCompleteDate = getDateTimeStringFromLong(treatmentCompleteDate).toString(),
            remarks = remarks,
            homeVisitDate = getDateTimeStringFromLong(homeVisitDate).toString(),
            leprosySymptoms = leprosySymptoms,
            typeOfLeprosy = typeOfLeprosy,
            leprosySymptomsPosition = leprosySymptomsPosition,
            visitLabel = visitLabel,
            leprosyStatus = leprosyStatus,
            referredTo = referredTo,
            referToName = referToName,
            treatmentEndDate = getDateTimeStringFromLong(treatmentEndDate).toString(),
            mdtBlisterPackRecived = mdtBlisterPackRecived,
            createdBy = createdBy,
            createdDate =getDateTimeStringFromLong(createdDate).toString(),
            modifiedBy = modifiedBy,
            lastModDate = getDateTimeStringFromLong(lastModDate).toString(),
            treatmentStartDate = getDateTimeStringFromLong(treatmentStartDate).toString()
        )
    }
}

data class LeprosyFollowUpRequestDTO(
    val userName: String,
    val leprosyFollowUpLists: List<LeprosyFollowUpDTO>
)
