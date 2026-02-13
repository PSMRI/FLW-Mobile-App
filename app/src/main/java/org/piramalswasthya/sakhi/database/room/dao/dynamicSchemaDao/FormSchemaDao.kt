package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity

@Dao
interface FormSchemaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(schema: FormSchemaEntity)

    @Query("SELECT * FROM form_schema WHERE formId = :formId LIMIT 1")
    fun getFormSchemaLive(formId: String): LiveData<FormSchemaEntity?>

    @Query("SELECT * FROM form_schema WHERE formId = :formId LIMIT 1")
    suspend fun getSchema(formId: String): FormSchemaEntity?
}
