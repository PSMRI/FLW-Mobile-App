package org.piramalswasthya.sakhi.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.DewormingDTO
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale



@Entity(tableName = "DewormingMeeting")
data class DewormingCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var dewormingDone: String? = null,
    var dewormingDate: String? = null,
    var dewormingLocation: String? = null,
    var ageGroup: Int? = null,
    var image1: String? = null,
    var image2: String? = null,
    var regDate: String? = null,

    var syncState: SyncState = SyncState.UNSYNCED
) : FormDataModel {
    
    fun toDTO(): DewormingDTO {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = formatter.format(Date())
        return DewormingDTO(
            id = id,
            dewormingDone = dewormingDone,
            dewormingDate = dewormingDate,
            dewormingLocation = dewormingLocation,
            ageGroup = ageGroup,
            image1 = image1,
            image2 = image2,
            regDate = currentDate,
        )
    }

    fun toDewormingCache(): DewormingCache {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = formatter.format(Date())
        return DewormingCache(
            id = id,
            dewormingDone = dewormingDone,
            dewormingDate = dewormingDate,
            dewormingLocation = dewormingLocation,
            ageGroup = ageGroup,
            image1 = image1,
            image2 = image2,
            regDate = currentDate,
        )
    }
}