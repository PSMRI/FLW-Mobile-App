package org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mosquito_net_visit")
data class MosquitoNetFormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val benId: Long,
    val hhId: Long,
    val visitDay: String,
    val visitDate: String,
    val formId: String,
    val version: Int,
    val formDataJson: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
