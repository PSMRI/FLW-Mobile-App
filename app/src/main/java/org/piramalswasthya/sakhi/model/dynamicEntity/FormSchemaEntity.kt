package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "form_schema")
data class FormSchemaEntity(
    @PrimaryKey(autoGenerate = false)
    val formId: String,
    val formName: String,
    val language: String,
    val version: Int = 1,
    val schemaJson: String
)
