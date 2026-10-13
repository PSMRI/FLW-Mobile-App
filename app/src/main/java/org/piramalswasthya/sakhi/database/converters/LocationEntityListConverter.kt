package org.piramalswasthya.sakhi.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.piramalswasthya.sakhi.model.LocationEntity

object LocationEntityListConverter {

    @TypeConverter
    fun toLocationEntityList(value: String?): List<LocationEntity>? {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return null
        val listType = object : TypeToken<List<LocationEntity?>?>() {}.type
        return runCatching {
            Gson().fromJson<List<LocationEntity?>>(value, listType)?.filterNotNull()
        }.getOrNull()
    }

    @TypeConverter
    fun fromLocationEntityList(list: List<LocationEntity>): String? {
        if (list.isNullOrEmpty()) return null
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return null
        val listType = object : TypeToken<List<Int?>?>() {}.type
        return runCatching {
            Gson().fromJson<List<Int?>>(value, listType)?.filterNotNull()
        }.getOrNull()
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        if (list.isNullOrEmpty()) return null
        val gson = Gson()
        return gson.toJson(list)
    }
}