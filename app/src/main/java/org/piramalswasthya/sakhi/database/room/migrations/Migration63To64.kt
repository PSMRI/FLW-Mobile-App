package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration63To64 : Migration(63, 64) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
              ALTER TABLE Adolescent_Health_Form_Data
            ADD COLUMN isSanitaryNapkinUsed INTEGER
            """.trimIndent()
        )
    }
}
