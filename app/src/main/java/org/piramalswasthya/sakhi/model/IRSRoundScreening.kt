package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.network.ScreeningRoundDTO


@Entity(
    tableName = "IRS_ROUND",
    indices = [Index(name = "ind_irs_round", value = ["householdId"])]
)
data class IRSRoundScreening(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var date: Long = System.currentTimeMillis(),
    var rounds: Int = 0,
    var householdId: Long = 0L
) : FormDataModel {
    fun toDTO(): ScreeningRoundDTO {
        return ScreeningRoundDTO(
            date = getDateTimeStringFromLong(date).toString(),
            rounds = rounds,
            householdId = householdId
        )
    }
}

data class BenWithScreeningRound(
    @Embedded
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId",
        entityColumn = "householdId"
    )
    val round: List<IRSRoundScreening?>
) {
    fun asScreeningRoundDomainModel(): BenWithScreeningRoundDomain {
        return BenWithScreeningRoundDomain(
            round = round
        )
    }
}

data class BenWithScreeningRoundDomain(
    val round: List<IRSRoundScreening?>
)