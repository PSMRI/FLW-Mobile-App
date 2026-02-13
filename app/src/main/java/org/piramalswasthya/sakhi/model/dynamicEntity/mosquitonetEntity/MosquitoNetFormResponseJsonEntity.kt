package org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mosquito_net_visit",
    indices = [Index(value = ["hhId", "visitDate","formId"], unique = true)]

)

data class MosquitoNetFormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hhId: Long,
    val formId: String,
    val version: Int,
    val visitDate: String,
    val formDataJson: String,
    val isSynced: Boolean = false,
    val syncedAt: String? = null
)
