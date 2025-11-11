package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Dao
interface MosquitoNetFormResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: MosquitoNetFormResponseJsonEntity)

    @Query("SELECT COUNT(*) FROM mosquito_net_visit WHERE hhId = :hhId AND visitDate = :visitDate")
    suspend fun exists(hhId: Long, visitDate: String): Int


    @Transaction
    suspend fun insertWithLimit(entity: MosquitoNetFormResponseJsonEntity): Boolean {
        val visitYear = extractYear(entity.visitDate)
        val count = getCountForYear(entity.hhId, entity.formId, visitYear)

        val alreadyExists = exists(entity.hhId, entity.visitDate) > 0
        if (alreadyExists) return false

        if (count < 4) {
            insertFormResponse(entity)
            return true
        } else {
            val oldest = getOldestForYear(entity.hhId, entity.formId, visitYear)
            if (oldest != null) {
                val updated = entity.copy(id = oldest.id)
                insertFormResponse(updated)
                return true
            }
        }
        return false
    }

    @Query("UPDATE mosquito_net_visit SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM mosquito_net_visit WHERE hhId = :hhId AND formId = :formId ORDER BY visitDate DESC LIMIT 1")
    suspend fun getLatestForHhForm(hhId: Long, formId: String): MosquitoNetFormResponseJsonEntity?

    @Query("SELECT * FROM mosquito_net_visit WHERE hhId = :hhId")
    suspend fun getAllByHhId(hhId: Long): List<MosquitoNetFormResponseJsonEntity>

    @Query("SELECT * FROM mosquito_net_visit WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<MosquitoNetFormResponseJsonEntity>

    @Query("SELECT COUNT(*) FROM mosquito_net_visit WHERE hhId = :hhId AND formId = :formId AND strftime('%Y', visitDate) = :year")
    suspend fun getCountForYear(hhId: Long, formId: String, year: String): Int

    @Query("SELECT * FROM mosquito_net_visit WHERE hhId = :hhId AND formId = :formId AND strftime('%Y', visitDate) = :year ORDER BY visitDate ASC LIMIT 1")
    suspend fun getOldestForYear(hhId: Long, formId: String, year: String): MosquitoNetFormResponseJsonEntity?

    @Query("SELECT formDataJson FROM mosquito_net_visit WHERE hhId = :hhId ORDER BY date(visitDate) DESC")
    suspend fun getFormJsonList(hhId: Long): List<String>
    private fun extractYear(dateStr: String): String {
        return try {
            val inputFormats = listOf("yyyy-MM-dd", "dd-MM-yyyy")
            for (fmt in inputFormats) {
                try {
                    val parsed = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)
                    if (parsed != null) {
                        return SimpleDateFormat("yyyy", Locale.getDefault()).format(parsed)
                    }
                } catch (_: Exception) {}
            }
            Calendar.getInstance().get(Calendar.YEAR).toString()
        } catch (_: Exception) {
            Calendar.getInstance().get(Calendar.YEAR).toString()
        }
    }
}
