package org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.database.room.SyncState

@Entity(
    tableName = "FILARIA_MDA_VISIT_HISTORY",
    indices = [
        Index(value = ["hhId", "formId", "visitMonth"], unique = true),
        Index(value = ["hhId", "visitDate"])
    ]
)
data class FilariaMDAFormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hhId: Long,
    val visitDate: String,
    val visitMonth: String,
    val formId: String,
    val version: Int,
    val formDataJson: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: String? = null
)
