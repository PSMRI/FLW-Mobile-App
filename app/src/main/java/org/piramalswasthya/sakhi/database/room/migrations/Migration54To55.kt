package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration54To55 : Migration(54, 55) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE ELIGIBLE_COUPLE_TRACKING 
            ADD COLUMN dateOfSterilisation INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}
