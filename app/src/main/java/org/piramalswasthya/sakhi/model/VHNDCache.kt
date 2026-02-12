package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.VHNDDTO

@Entity(tableName = "VHND")
data class VHNDCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var vhndDate: String,
    var place: String? = null,
    var noOfBeneficiariesAttended: Int? = null,
    var image1: String? = null,
    var image2: String? = null,
    var vhndPlaceId: Int? = 0,
    var pregnantWomenAnc: String? = null,
    var lactatingMothersPnc: String? = null,
    var childrenImmunization: String? = null,
    var knowledgeBalancedDiet: String? = null,
    var careDuringPregnancy: String? = null,
    var importanceBreastfeeding: String? = null,
    var complementaryFeeding: String? = null,
    var hygieneSanitation: String? = null,
    var familyPlanningHealthcare: String? = null,
    var selectAllEducation: Boolean? = false,
    var syncState: SyncState = SyncState.UNSYNCED,
    var isDraft: Boolean = false
) : FormDataModel {
    fun toDTO(): VHNDDTO {
        return VHNDDTO(
            id = id,
            vhndDate = (vhndDate),
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            Image1 = image1,
            Image2 = image2,
            vhndPlaceId = vhndPlaceId,
            pregnantWomenAnc = pregnantWomenAnc,
            lactatingMothersPnc = lactatingMothersPnc,
            childrenImmunization = childrenImmunization,
            knowledgeBalancedDiet = knowledgeBalancedDiet,
            careDuringPregnancy = careDuringPregnancy,
            importanceBreastfeeding = importanceBreastfeeding,
            complementaryFeeding = complementaryFeeding,
            hygieneSanitation = hygieneSanitation,
            familyPlanningHealthcare = familyPlanningHealthcare,

        )
    }

    fun toVhndDTODTO(): VHNDCache {
        return VHNDCache(
            id = id,
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            image2 = image1,
            image1 = image2,
            vhndDate = (vhndDate),
            vhndPlaceId = vhndPlaceId,
            pregnantWomenAnc = pregnantWomenAnc,
            lactatingMothersPnc = lactatingMothersPnc,
            childrenImmunization = childrenImmunization,
            knowledgeBalancedDiet = knowledgeBalancedDiet,
            careDuringPregnancy = careDuringPregnancy,
            importanceBreastfeeding = importanceBreastfeeding,
            complementaryFeeding = complementaryFeeding,
            hygieneSanitation = hygieneSanitation,
            familyPlanningHealthcare = familyPlanningHealthcare,
        )
    }
}
