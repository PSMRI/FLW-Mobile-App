package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "all_visit_history",
    indices = [Index(value = ["rchId", "visitDay", "formId"], unique = true)]
)
data class FormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rchId: String,
    val visitDay: String,
    val formId: String,
    val version: Int,
    val formDataJson: String,
    val isSynced: Boolean = false,                // false = not yet synced
    val createdAt: Long = System.currentTimeMillis(),  // when form was filled
    val syncedAt: Long? = null
)


