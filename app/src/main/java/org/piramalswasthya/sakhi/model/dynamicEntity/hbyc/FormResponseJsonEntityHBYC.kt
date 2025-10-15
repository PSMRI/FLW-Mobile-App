package org.piramalswasthya.sakhi.model.dynamicEntity.hbyc

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ALL_VISIT_HISTORY_HBYC",
    indices = [Index(value = ["benId","hhId", "visitDay","visitDate", "formId"], unique = true)]
)
data class FormResponseJsonEntityHBYC(
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


