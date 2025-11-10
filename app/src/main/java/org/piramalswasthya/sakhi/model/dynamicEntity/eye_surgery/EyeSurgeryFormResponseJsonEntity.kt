package org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ALL_EYE_SURGERY_VISIT_HISTORY",
    indices = [
        Index(value = ["benId", "formId", "visitMonth"], unique = true),
        Index(value = ["benId", "visitDate"])
    ]
)
data class EyeSurgeryFormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val benId: Long,
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
