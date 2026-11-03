package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration62To63 : Migration(62, 63) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            if (!columnExists(database, "NOTIFICATION", "appType"))
                database.execSQL("ALTER TABLE NOTIFICATION ADD COLUMN appType TEXT")
            if (!columnExists(database, "NOTIFICATION", "redirect"))
                database.execSQL("ALTER TABLE NOTIFICATION ADD COLUMN redirect TEXT")
            if (!columnExists(database, "NOTIFICATION", "readDate"))
                database.execSQL("ALTER TABLE NOTIFICATION ADD COLUMN readDate TEXT")
        } catch (_: Exception) {
        }
    }
}
