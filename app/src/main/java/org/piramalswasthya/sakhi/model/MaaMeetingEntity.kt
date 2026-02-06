package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.piramalswasthya.sakhi.database.converters.StringListConverter
import org.piramalswasthya.sakhi.database.room.SyncState

@Entity(tableName = "MAA_MEETING",  indices = [Index(value = ["id"], unique = true)])
data class MaaMeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var meetingDate: String?,
    var place: String?,
    var participants: Int?,
    var mitaninActivityCheckList : String? = null,
    var villageName: String? = null,
    var noOfPragnentWomen: String? = null,
    var noOfLactingMother: String? = null,
    val ashaId: Int?,
    @TypeConverters(StringListConverter::class)
    var meetingImages: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var syncState: SyncState = SyncState.UNSYNCED,
    var isDraft: Boolean = false
)
