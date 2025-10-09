package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.piramalswasthya.sakhi.database.converters.StringListConverter
import org.piramalswasthya.sakhi.database.room.SyncState

@Entity(tableName = "MAA_MEETING")
data class MaaMeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val meetingDate: String?,
    val place: String?,
    val participants: Int?,
    val ashaId: Int?,
    @TypeConverters(StringListConverter::class)
    val meetingImages: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.UNSYNCED
)


