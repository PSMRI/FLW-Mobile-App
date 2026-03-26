package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ncd_referal_all_visit",
    indices = [
        Index(value = ["benId", "hhId"]),
        Index(
            value = ["benId", "hhId", "visitNo", "followUpNo"],
            unique = true
        )
    ]
)
data class NCDReferalFormResponseJsonEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val benId: Long,
    val hhId: Long,

    val visitNo: Int,
    val followUpNo: Int,

    val treatmentStartDate: String,
    val followUpDate: String? = null,

    val diagnosisCodes: String?,
    val formId: String,
    val version: Int,
    val formDataJson: String,

    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
