package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.room.SyncState

@Entity(tableName = "PulsePolioCampaign")
data class PulsePolioCampaignCache(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var formDataJson: String? = null,
    var syncState: SyncState = SyncState.UNSYNCED
) : FormDataModel {
    val campaignDate: String?
        get() {
            return try {
                val json = formDataJson ?: return null
                val jsonObj = JSONObject(json)
                val fieldsObj = jsonObj.optJSONObject("fields") ?: return null
                fieldsObj.optString("campaign_date").takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                null
            }
        }
}
