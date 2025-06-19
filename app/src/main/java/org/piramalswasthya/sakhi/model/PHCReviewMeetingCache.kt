package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.PHCReviewDTO
import java.util.Date

@Entity(tableName = "PHCReviewMeeting",
)

data class PHCReviewMeetingCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int ,
    var phcReviewDate: String,
    var place: String? = null,
    var noOfBeneficiariesAttended: Int? = null,
    var image1: String? = null,
    var image2: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED
) : FormDataModel {
    fun toDTO(): PHCReviewDTO {
        return PHCReviewDTO(
            id = id,
            phcReviewDate = (phcReviewDate),
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            Image1 = image1,
            Image2 = image2
        )
    }

    fun toPHCDTODTO(): PHCReviewMeetingCache {
        return PHCReviewMeetingCache(
            id = id,
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            image2 = image1,
            image1 = image2,
            phcReviewDate = (phcReviewDate)
        )
    }
}


//data class BenWithHRNPACache(
//    @Embedded
//    val ben: BenBasicCache,
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId"
//    )
//    val assess: HRPNonPregnantAssessCache?,
//
//    ) {
//    fun asDomainModel(): BenWithHRNPADomain {
//        return BenWithHRNPADomain(
//            ben = ben.asBasicDomainModel(),
//            assess = assess
//        )
//    }
//}
//
//data class VHNDDomain(
//    val vhndChache: VHNDCache?
//)
