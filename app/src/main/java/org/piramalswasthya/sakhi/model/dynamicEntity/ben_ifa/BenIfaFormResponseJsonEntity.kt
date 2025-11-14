package org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ALL_BEN_IFA_VISIT_HISTORY")

data class BenIfaFormResponseJsonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val benId: Long,
    val hhId: Long,
    val visitDate: String,
    val formId: String,
    val version: Int,
    val formDataJson: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)