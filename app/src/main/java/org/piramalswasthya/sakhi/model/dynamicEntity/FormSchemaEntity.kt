package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "form_schema")
data class FormSchemaEntity(
    @PrimaryKey(autoGenerate = false)
    val formId: String,
    val formName: String,
    val version: Int = 1,
    val schemaJson: String
)
