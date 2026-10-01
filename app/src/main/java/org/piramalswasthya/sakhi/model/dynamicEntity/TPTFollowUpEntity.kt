package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "tb_tpt_follow_up",
    indices = [Index(value = ["benId", "visitNo", "followUpNo"], unique = true)]
)
data class TPTFollowUpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val benId: Long,
    val houseHoldId: Long,
    val visitNo: Int,
    val followUpNo: Int,
    val treatmentType: String?,
    val treatmentStartDate: String?,
    val followUpDate: String?,
    val formId: String = "tb_tpt_follow_up",
    val version: Int = 1,
    val fieldsJson: String,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class TPTFollowUpApiRequest(
    @SerializedName("ben_id") val benId: Long,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("fields") val fields: Map<String, String>
)

data class TPTFollowUpApiRecord(
    @SerializedName(value = "ben_id", alternate = ["benId"]) val benId: Long = 0,
    @SerializedName(value = "fields", alternate = ["fieldsJson"]) val fields: Map<String, Any?> = emptyMap()
)
