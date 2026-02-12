package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.piramalswasthya.sakhi.database.converters.StringListConverter
import org.piramalswasthya.sakhi.database.room.SyncState

@Entity(
    tableName = "SAAS_BAHU_ACTIVITY",
)
data class SaasBahuSammelanCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ashaId: Int,
    var place: String ? = null,
    var participants: Int ? = 0,
    var date: Long? = 0L,
    @TypeConverters(StringListConverter::class)
    var sammelanImages: List<String>? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
    var isDraft: Boolean = false

)
