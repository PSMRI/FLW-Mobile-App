package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.AHDDTO

@Entity(tableName = "AHDMeeting")
data class AHDCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var mobilizedForAHD: String? = null,
    var ahdPlace: String? = null,
    var ahdDate: String? = null,
    var image1: String? = null,
    var image2: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED,
    var isDraft: Boolean = false
) : FormDataModel {
    
    fun toDTO(): AHDDTO {
        return AHDDTO(
            id = id,
            mobilizedForAHD = mobilizedForAHD,
            ahdPlace = ahdPlace,
            ahdDate = ahdDate,
            image1 = image1,
            image2 = image2,
        )
    }

    fun toAHDCache(): AHDCache {
        return AHDCache(
            id = id,
            mobilizedForAHD = mobilizedForAHD,
            ahdPlace = ahdPlace,
            ahdDate = ahdDate,
            image1 = image1,
            image2 = image2,
        )
    }
}