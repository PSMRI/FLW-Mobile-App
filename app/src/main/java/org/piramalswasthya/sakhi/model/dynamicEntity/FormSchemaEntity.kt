package org.piramalswasthya.sakhi.model.dynamicEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "form_schema")
data class FormSchemaEntity(
    @PrimaryKey(autoGenerate = false)
    val formId: String,                  // e.g., "HBNC_FORM"
    val formName: String,                // e.g., "HBNC Visit Form"
    val version: Int = 1,               // can track if schema changes
    val schemaJson: String               // Whole form schema from API (as raw JSON)
)
