package org.piramalswasthya.sakhi.database.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        if (list == null) return null
        return list.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return null
        return value.split("|||")
    }
}


