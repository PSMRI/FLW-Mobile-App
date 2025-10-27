package org.piramalswasthya.sakhi.database.converters

import androidx.room.TypeConverter
import org.piramalswasthya.sakhi.database.room.SyncState

object SyncStateConverter {

    @TypeConverter
    fun toInt(value: SyncState): Int {
        return value.ordinal
    }

    @TypeConverter
    fun fromInt(value: Int): SyncState {
        return SyncState.values()[value]
    }

    @TypeConverter
    fun toSyncState(value: String?): SyncState? {
        return value?.let { enumValueOf<SyncState>(it) }
    }

    @TypeConverter
    fun fromSyncState(state: SyncState?): String? {
        return state?.name
    }
}