package org.piramalswasthya.sakhi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState


@Entity(
    tableName = "CHILD_REG",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("motherBenId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "childRegInd", value = ["motherBenId"])]
)

data class ChildRegCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val motherBenId: Long,
    var babyName: String? = null,
    var infantTerm: String? = null,
    var corticosteroidGiven: String? = null,
    var gender: String? = null,
    var babyCriedAtBirth: Boolean? = null,
    var resuscitation: Boolean? = null,
    var referred: String? = null,
    var hadBirthDefect: String? = null,
    var birthDefect: String? = null,
    var otherDefect: String? = null,
    var weight: Double? = 0.0,
    var breastFeedingStarted: Boolean? = null,
    var opv0Dose: Long? = System.currentTimeMillis(),
    var bcgDose: Long? = System.currentTimeMillis(),
    var hepBDose: Long? = System.currentTimeMillis(),
    var vitkDose: Long? = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel

data class BenWithChildRegCache(
    @Embedded(prefix = "ben_")
    val ben: BenBasicCache,
    @Relation(
        parentColumn = "benId", entityColumn = "benId", entity = ChildRegCache::class
    )
    val childRegistration: ChildRegCache?
) {

//    fun asDomainModel(): BenWithCbacDomain {
//        return BenWithCbacDomain(
//            ben.asBasicDomainModel(), savedCbacRecords
//        )
//    }
}
