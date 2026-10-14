package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration68To69 : Migration(68, 69) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val columns = listOf(
            "failureToGainWeight INTEGER DEFAULT NULL",
            "decreasedActivityOrPlayfulness INTEGER DEFAULT NULL"
        )
        columns.forEach { columnDefinition ->
            val columnName = columnDefinition.substringBefore(" ")
            if (!columnExists(database, "TB_SCREENING", columnName)) {
                database.execSQL("ALTER TABLE TB_SCREENING ADD COLUMN $columnDefinition")
            }
        }
    }
}
