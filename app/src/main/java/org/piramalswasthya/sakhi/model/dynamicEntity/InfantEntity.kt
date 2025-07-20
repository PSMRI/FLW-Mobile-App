package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "infant")
data class InfantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rchId: String,
    val name: String,
    val motherName: String,
    val fatherName: String?,
    val dob: String,
    val gender: String,
    val phoneNumber: String,
    val sncuDischarged: Boolean = false
)
