package org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.database.room.SyncState

@Entity(
    tableName = "FILARIA_MDA_CAMPAIGN_HISTORY",
    indices = [
        Index(value = [ "formId", "visitYear"], unique = true),
        Index(value = ["visitDate"])
    ]
)
data class FilariaMDACampaignFormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val visitDate: String,
    val visitYear: String,
    val formId: String,
    val version: Int,
    val formDataJson: String,
    val isSynced: Boolean = false,
    var syncState: SyncState = SyncState.UNSYNCED,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: String? = null
)