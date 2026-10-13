package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration41To42 : Migration(41, 42) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE form_schema ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
        if (tableExists(database, "HRP_MICRO_BIRTH_PLAN")) {

            if (!columnExists(database, "HRP_MICRO_BIRTH_PLAN", "processed")) {
                database.execSQL(
                    "ALTER TABLE HRP_MICRO_BIRTH_PLAN ADD COLUMN processed TEXT"
                )
            }
        }
    }
}
