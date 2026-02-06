package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.VHNCDTO

@Entity(tableName = "VHNC")
data class VHNCCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var vhncDate: String,
    var place: String? = null,
    var noOfBeneficiariesAttended: Int? = null,
    var image1: String? = null,
    var image2: String? = null,
    var villageName: String? = null,
    var anm: Int? = 0,
    var aww: Int? = 0,
    var noOfPragnentWoment: Int? = 0,
    var noOfLactingMother: Int? = 0,
    var noOfCommittee: Int? = 0,
    var followupPrevius: Boolean? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
    var isDraft: Boolean = false
) : FormDataModel {
    fun toDTO(): VHNCDTO {
        return VHNCDTO(
            id = id,
            vhncDate = (vhncDate),
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            Image1 = image1,
            Image2 = image2,
            villageName = villageName,
            anm = anm,
            aww = aww,
            noOfPragnentWoment = noOfPragnentWoment,
            noOfLactingMother = noOfLactingMother,
            noOfCommittee = noOfCommittee,
            followupPrevius = followupPrevius,

        )
    }

    fun toVhncDTODTO(): VHNCCache {
        return VHNCCache(
            id = id,
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            image2 = image1,
            image1 = image2,
            vhncDate = (vhncDate),
            villageName = villageName,
            anm = anm,
            aww = aww,
            noOfPragnentWoment = noOfPragnentWoment,
            noOfLactingMother = noOfLactingMother,
            noOfCommittee = noOfCommittee,
            followupPrevius = followupPrevius,
        )
    }
}
